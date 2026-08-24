/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reports.libintegration;

import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.LlmQueryParameterNames;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads band data for the {@link DataSetType#LLM} data set type: reads the query stored in the data set, binds
 * what the run holds under the names the query references, and executes it.
 * <p>
 * A run never generates a query. The query is generated in the report designer of a running application and
 * stored with the report, which is what makes a run reproducible, free of model calls and independent of
 * whether a model is reachable at all.
 * <p>
 * The rules by which a cell of a cross-tab band finds its place in the matrix live in {@link LlmCrossTabAxes}.
 */
@NullMarked
public class LlmDataLoader implements ReportDataLoader {

    private static final Logger log = LoggerFactory.getLogger(LlmDataLoader.class);

    /**
     * The calls with which an EclipseLink JPQL select reaches past the query language: {@code SQL} inlines
     * database SQL, {@code FUNCTION}, {@code FUNC} and {@code OPERATOR} call a database function, and
     * {@code COLUMN} and {@code TABLE} read a column or a table the entity model does not map at all.
     * <p>
     * {@code CAST}, {@code EXTRACT}, {@code TREAT} and {@code REGEXP} — the rest of what the EclipseLink
     * grammar adds — stay within the model and are left alone.
     */
    protected static final Pattern NATIVE_ESCAPE_PATTERN = Pattern.compile(
            "\\b(sql|function|func|operator|column|table)\\s*\\(", Pattern.CASE_INSENSITIVE);

    /**
     * The marker with which ordinary Jmix JPQL asks for a parameter to be matched case-insensitively, as in
     * {@code e.name like :(?i)name}. A stored query cannot use it: the parameters of a query are read from its
     * text by name, and a name written like this is not one — neither this loader nor the add-on's validator
     * recognises it, so nothing would ever be bound to it.
     */
    protected static final Pattern CASE_INSENSITIVE_PARAMETER_PATTERN = Pattern.compile(":\\(\\?i\\)");

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected LlmDataQuerySerializer llmDataQuerySerializer;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected Metadata metadata;

    /**
     * Holds what one report run has already generated and already warned about. Bound to the thread the run
     * executes on: the next run reaching that thread replaces its contents, and the reference is soft, so what
     * the last run left there is reclaimed under memory pressure instead of sitting on a pooled thread for the
     * lifetime of the process.
     */
    protected final ThreadLocal<SoftReference<RunScope>> runScope =
            ThreadLocal.withInitial(() -> new SoftReference<>(new RunScope()));

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, @Nullable BandData parentBand,
                                              Map<String, Object> params) {
        RunScope scope = runScopeOf(parentBand);
        Map<String, Object> additionalParams = reportQuery.getAdditionalParams();
        // The params of a run are one map shared by every band of it, so the axes of a cross-tab band stay in
        // them for every band extracted afterwards — and for its own next extraction. Which of them this data
        // set reads, if any, follows from the band it serves: its orientation and its own name.
        String bandName = (String) additionalParams.get(DataSet.BAND_NAME);
        boolean readsAxes = LlmCrossTabAxes.areReadBy(reportQuery.getName(),
                (BandOrientation) additionalParams.get(DataSet.BAND_ORIENTATION));
        // Values are offered on the strength of a name; nothing is demanded of a query, and no band is left
        // unexecuted, unless the band those axes belong to is known to be this one. A report assembled in code
        // may leave a data set unaware of its band, and an axis is then no more this band's than any other's.
        boolean enforcesAxes = readsAxes && bandName != null;

        String emptyAxis = enforcesAxes ? LlmCrossTabAxes.firstEmptyAxis(params, bandName) : null;
        if (emptyAxis != null) {
            // A cross-tab has no cells along an axis that produced no values, so there is nothing for this
            // query to return — and its parameters, which name that axis, have no values to bind either.
            scope.warnOnce(reportQuery, "empty-axis:" + emptyAxis,
                    () -> log.debug("The cross-tab axis [{}] produced no values, so data set [{}] is not executed",
                            emptyAxis, reportQuery.getName()));
            return List.of();
        }

        CollectedParameters collectedParameters =
                collectAvailableParameters(reportQuery, params, parentBand, bandName, readsAxes, scope);
        Map<String, Object> availableParameters = collectedParameters.availableParameters();

        LlmDataQuery query = resolveQuery(reportQuery, additionalParams, scope);
        if (enforcesAxes) {
            LlmCrossTabAxes.checkAxesAreLinkable(reportQuery.getName(), query, params,
                    collectedParameters.requiredResultProperties(), bandName);
        }

        Map<String, Object> arguments = resolveArguments(reportQuery, query, availableParameters, params);
        String storeName = scope.storeName(query.getJpql(), () -> resolveStoreName(query));
        log.debug("Executing the query of data set [{}] in store [{}]: {}",
                reportQuery.getName(), storeName, query.getJpql());

        List<Map<String, @Nullable Object>> rows;
        try {
            rows = executeQuery(query, arguments, storeName);
        } catch (AccessDeniedException e) {
            // Being refused the data is not a failure of this data set: the engine reports it as what it is.
            throw e;
        } catch (RuntimeException e) {
            // A query written against the data model of another moment is the likely reason a run fails here,
            // and the report tells its author where such a query is fixed.
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] failed: %s. Generate it again in the report designer if it "
                            + "no longer fits the data model", reportQuery.getName(), e.getMessage()), e);
        }

        //noinspection NullableProblems
        return rows;
    }

    /**
     * Executes the stored query through {@code DataManager}, so that the entity and attribute permissions of the
     * current user and the row-level policies of the query's root entity apply as they do to any other data set.
     * An attribute the user may not read comes back as {@code null}.
     * <p>
     * The query runs in the store of the entity it reads, which {@link #resolveStoreName} works out from the
     * query itself.
     * <p>
     * Values are bound as named JPQL parameters, never inlined into the text: the query is written once and run
     * with whatever the report parameters and the parent band hold this time.
     * <p>
     * A row comes out as the report engine needs it: keyed by the query's columns in select-clause order (a
     * cross-tab links its cells by the first matching column) and mutable ({@link ReportDataLoader} states so,
     * and merging several data sets of one band writes into it). Values, {@code null} and an empty string
     * included, are kept as they came.
     *
     * @param query     stored query to execute
     * @param arguments value to bind per parameter the query references
     * @param storeName data store to execute in, the one the query's entity belongs to
     * @return one row per row the query returned
     */
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               Map<String, Object> arguments,
                                                               String storeName) {
        FluentValuesLoader valuesLoader = dataManager.loadValues(query.getJpql())
                .store(storeName)
                .properties(query.getResultProperties());
        arguments.forEach(valuesLoader::parameter);

        List<Map<String, @Nullable Object>> rows = new ArrayList<>();
        for (KeyValueEntity row : valuesLoader.list()) {
            Map<String, @Nullable Object> bandRow = new LinkedHashMap<>();
            for (String property : query.getResultProperties()) {
                bandRow.put(property, row.getValue(property));
            }
            rows.add(bandRow);
        }
        return rows;
    }

    /**
     * Refuses a stored query a run will not execute, before it executes anything: one that does more than read
     * the entity model through {@code DataManager}, one whose parameters could not be bound, and one that names
     * no column to key its rows by.
     * <p>
     * The designer checks a query the add-on generated or the author edited, and refuses some of this. A report
     * also arrives by import, though, bringing whatever text the file holds and no add-on to check it with — so
     * what a run promises about a query, a run has to establish itself.
     */
    protected void checkQueryIsFitToExecute(ReportQuery reportQuery, LlmDataQuery query) {
        // Blanked of literals once, so that a call or a marker spelled inside one is read as the text it is.
        String withoutLiterals = LlmQueryParameterNames.stripStringLiterals(query.getJpql());

        checkQueryOnlyReads(reportQuery, query, withoutLiterals);
        checkParametersCanBeBound(reportQuery, withoutLiterals);
        checkQueryReturnsColumns(reportQuery, query);
    }

    /**
     * Refuses a stored query that does more than read the entity model through {@code DataManager}: one that is
     * not a select, and one that reaches past JPQL into the database itself, whose text this loader would
     * otherwise hand over as it stands. See {@link #NATIVE_ESCAPE_PATTERN} for what counts as reaching past.
     * <p>
     * Write keywords are not looked for: a JPQL query is a single statement, so {@code update} or {@code delete}
     * inside a select is a word rather than an operation, and refusing a query for the name of an attribute
     * would cost more than it saves.
     *
     * @param withoutLiterals the query text with its string literals blanked
     */
    protected void checkQueryOnlyReads(ReportQuery reportQuery, LlmDataQuery query, String withoutLiterals) {
        if (!Strings.CI.startsWith(query.getJpql().stripLeading(), "select")) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] is not a select, so it is not executed", reportQuery.getName()));
        }

        Matcher nativeEscape = NATIVE_ESCAPE_PATTERN.matcher(withoutLiterals);
        if (nativeEscape.find()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] calls [%s], which reaches the database directly and would "
                            + "leave the data access constraints of the current user behind, so it is not executed",
                    reportQuery.getName(), nativeEscape.group(1)));
        }
    }

    /**
     * Refuses a stored query holding a parameter marker this data set type cannot read at all. See
     * {@link #CASE_INSENSITIVE_PARAMETER_PATTERN} for the one there is.
     * <p>
     * A parameter the text references and the document does not declare is left to the JPA provider, which
     * fails on it: the run then reports that failure the way it reports any other, naming the data set and the
     * way out. Refusing it here would demand of every stored document that its parameters match its text
     * exactly, which is a promise the format does not make.
     *
     * @param withoutLiterals the query text with its string literals blanked
     */
    protected void checkParametersCanBeBound(ReportQuery reportQuery, String withoutLiterals) {
        if (CASE_INSENSITIVE_PARAMETER_PATTERN.matcher(withoutLiterals).find()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] uses the case-insensitive parameter marker [:(?i)], whose "
                            + "parameter cannot be bound; write [lower(...) like :name] and generate the query "
                            + "again in the report designer", reportQuery.getName()));
        }
    }

    /**
     * Refuses a stored query whose columns cannot key the rows of a band: one naming no column at all, one
     * naming a blank column, and one naming the same column twice.
     * <p>
     * A row of a band is a map keyed by those columns, and {@code KeyValueEntity} holds one value per property,
     * so a duplicate name loses one of the values the query selected — and, in a cross-tab cell, shifts which
     * column the matrix is linked by. None of it is reported by anything downstream: the band simply prints
     * something else than the query asked for. The designer refuses to save a data set in any of these states; a
     * report also arrives by import, bringing whatever the file holds.
     */
    protected void checkQueryReturnsColumns(ReportQuery reportQuery, LlmDataQuery query) {
        List<String> columns = query.getResultProperties();
        if (columns.isEmpty() || columns.stream().anyMatch(StringUtils::isBlank)) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] names no result columns, so it would return empty rows: "
                            + "generate it again in the report designer. It names %s",
                    reportQuery.getName(), columns));
        }

        if (new HashSet<>(columns).size() != columns.size()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] names the same result column twice, so a value it selects "
                            + "would be lost: generate it again in the report designer. It names %s",
                    reportQuery.getName(), columns));
        }
    }

    /**
     * Reads the query stored with the data set. A run never generates one: the query is generated in the report
     * designer of a running application and stored with the report, which is what makes a run reproducible and
     * free of model calls. A data set without a stored query therefore has nothing to execute, and says so
     * rather than quietly asking a model for one.
     * <p>
     * Read once per document per run: the same data set is loaded once per row of its parent band, and parsing
     * the same JSON for every row would be work done to reach the same result. A query read here is also a query
     * found fit to execute — {@link #checkQueryIsFitToExecute} judges the document, which no row of a parent band
     * changes, so it is judged where the document is read.
     */
    protected LlmDataQuery resolveQuery(ReportQuery reportQuery, Map<String, Object> additionalParams,
                                        RunScope scope) {
        String storedDocument = (String) additionalParams.get(DataSet.LLM_GENERATED_QUERY);
        if (StringUtils.isBlank(storedDocument)) {
            throw new DataLoadingException(String.format(
                    "Data set [%s] has no generated query stored: generate it in the report designer",
                    reportQuery.getName()));
        }

        LlmDataQuery storedQuery;
        try {
            storedQuery = scope.storedQuery(storedDocument, () -> {
                LlmDataQuery read = llmDataQuerySerializer.fromJson(storedDocument);
                if (read != null) {
                    checkQueryIsFitToExecute(reportQuery, read);
                }
                return read;
            });
        } catch (LlmDataQueryException e) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] cannot be read: generate it again in the report designer",
                    reportQuery.getName()), e);
        }
        if (storedQuery == null) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] cannot be read: generate it again in the report designer",
                    reportQuery.getName()));
        }

        return storedQuery;
    }

    /**
     * Returns the data store the stored query has to run in: the one the entity it reads from belongs to.
     * <p>
     * Query generation is offered the whole entity model — {@code JpaDomainModelIntrospector} keeps every JPA
     * entity, whichever store it belongs to — so a stored query may well read an entity of an additional store,
     * and only that store can execute it. Nothing asks the author which store that is: the entity says so, and
     * the data set's own {@code dataStore} is not offered for this type.
     * <p>
     * The store is worked out once per query text per run ({@link RunScope#storeName}), so a band under a parent
     * pays for it once rather than per row. A text the platform's parser cannot read, or an entity name the
     * model does not know, leaves the main store — the query then fails on its own terms, saying what is wrong
     * with it, which is more use than a failure about a store.
     */
    protected String resolveStoreName(LlmDataQuery query) {
        String entityName;
        try {
            entityName = queryTransformerFactory.parser(query.getJpql()).getEntityName();
        } catch (RuntimeException e) {
            log.debug("The store of [{}] cannot be told from the query, so the main one is used", query.getJpql(), e);
            return Stores.MAIN;
        }

        MetaClass entity = metadata.findClass(entityName);
        return entity == null ? Stores.MAIN : entity.getStore().getName();
    }

    /**
     * Returns what the run this call belongs to has already resolved. Runs are told apart by the band data the
     * hierarchy is rooted at, which is created once per run and shared by every band of it.
     */
    protected RunScope runScopeOf(@Nullable BandData parentBand) {
        BandData rootBand = parentBand;
        while (rootBand != null && rootBand.getParentBand() != null) {
            rootBand = rootBand.getParentBand();
        }

        RunScope scope = runScope.get().get();
        if (scope == null) {
            scope = new RunScope();
            runScope.set(new SoftReference<>(scope));
        }

        scope.rootedAt(rootBand);
        return scope;
    }

    /**
     * Collects the values a query may bind, keyed by the name it would reference them by: the run parameters
     * first, then the fields of every parent row up the band hierarchy, then the values of the cross-tab axes
     * this band is built from.
     * A parameter with no value is left out: there is nothing to bind, and a query that references it fails
     * saying so, which is more use than a query that binds null.
     */
    protected CollectedParameters collectAvailableParameters(ReportQuery reportQuery,
                                                             Map<String, Object> params,
                                                             @Nullable BandData parentBand,
                                                             @Nullable String bandName,
                                                             boolean readsAxes,
                                                             RunScope scope) {
        Map<String, Object> availableParameters = new LinkedHashMap<>();
        Map<String, List<?>> crossTabAxes = new LinkedHashMap<>();
        List<String> requiredResultProperties = new ArrayList<>();

        for (Map.Entry<String, Object> param : params.entrySet()) {
            Object value = param.getValue();
            // A report parameter left unfilled arrives as a null value, whatever the map's declared type says.
            //noinspection ConstantValue
            if (value == null || !LlmQueryParameterNames.isValid(param.getKey())) {
                continue;
            }

            // An entry that is named like an axis and shaped like one holds the rows of another data set: rows
            // are no value to bind, whoever is reading. What it is instead depends on the reader — an axis of
            // this band is offered field by field, an axis of another band is none of this band's business, and
            // a data set that reads no axes at all leaves it alone.
            if (LlmQueryParameterNames.isCrossTabAxis(param.getKey()) && LlmCrossTabAxes.isAxisRows(value)) {
                if (readsAxes && LlmCrossTabAxes.isAxisOf(param.getKey(), bandName)) {
                    crossTabAxes.put(param.getKey(), (List<?>) value);
                }
                continue;
            }

            Object bindable = toBindableValue(value);
            if (bindable != null) {
                availableParameters.put(param.getKey(), bindable);
            }
        }

        // The walk stops short of the root band: its data is the run parameters, which are already offered
        // under their own names, and offering them a second time as Root_<name> would double every parameter
        // and describe a dictionary the designer never shows.
        for (BandData band = parentBand; band != null && !BandData.ROOT_BAND_NAME.equals(band.getName());
             band = band.getParentBand()) {
            addParentBandFields(reportQuery, band, availableParameters, scope);
        }

        crossTabAxes.forEach((axisName, rows) -> LlmCrossTabAxes.addAxisValues(axisName, rows,
                availableParameters, requiredResultProperties,
                (reason, warning) -> scope.warnOnce(reportQuery, reason, warning)));

        return new CollectedParameters(availableParameters, List.copyOf(requiredResultProperties));
    }

    /**
     * Returns the value to bind under a name, or {@code null} when there is nothing to bind. A parameter
     * holding several values — a "list of entities" parameter, for instance — is bound as the collection it is,
     * which a query matches with {@code IN}. A collection holding nothing to match is not a value.
     */
    @Nullable
    protected Object toBindableValue(Object value) {
        if (!(value instanceof Collection<?> values)) {
            return value;
        }

        return values.stream().anyMatch(Objects::nonNull) ? value : null;
    }

    /**
     * Offers the fields of one parent row under names flattened to {@code <band>_<field>}, because a JPQL
     * parameter name cannot contain the dot that SQL and JPQL data sets use in {@code ${Band.field}}.
     * <p>
     * A name already taken is kept as it is: a run parameter outranks a band field, and a nearer parent
     * outranks a more distant one. Skipping a field whose flattened name is not an identifier is deliberate —
     * sanitizing the name would let two different bands collapse onto one parameter.
     */
    protected void addParentBandFields(ReportQuery reportQuery, BandData band,
                                       Map<String, Object> availableParameters, RunScope scope) {
        Map<String, Object> row = band.getData();
        if (row == null) {
            return;
        }

        for (Map.Entry<String, Object> field : row.entrySet()) {
            Object value = field.getValue();
            String name = LlmQueryParameterNames.ofBandField(band.getName(), field.getKey());
            //noinspection ConstantValue
            if (value == null || !LlmQueryParameterNames.isValid(name)) {
                continue;
            }

            Object bindable = toBindableValue(value);
            if (bindable == null) {
                continue;
            }

            if (availableParameters.putIfAbsent(name, bindable) != null) {
                // The band is loaded once per parent row, and the collision is the same every time.
                scope.warnOnce(reportQuery, "shadowed-band-field:" + name,
                        () -> log.warn("Parameter [{}] is already available, so the field [{}] of band [{}] is not "
                                + "offered to the query; rename one of them to make both usable",
                                name, field.getKey(), band.getName()));
            }
        }
    }







    /**
     * Collects the value to bind for every parameter the query references. The type the query declares for a
     * parameter is not consulted: it was written to tell a model what the value would be, and a value is bound
     * as the run holds it.
     */
    protected Map<String, Object> resolveArguments(ReportQuery reportQuery, LlmDataQuery query,
                                                   Map<String, Object> availableParameters,
                                                   Map<String, Object> params) {
        Map<String, Object> arguments = new LinkedHashMap<>();

        for (LlmQueryParameter parameter : query.getParameters()) {
            Object available = availableParameters.get(parameter.getName());
            if (available == null) {
                throw new DataLoadingException(describeMissingArgument(reportQuery, parameter.getName(), params));
            }

            arguments.put(parameter.getName(), available);
        }

        return arguments;
    }

    /**
     * Says why a parameter the query references cannot be bound. A parameter the run knows but left empty is a
     * different matter from one the run has never heard of: an unfilled optional report parameter is the common
     * case, and it says so, because a query is generated once and binds every parameter it references — unlike a
     * JPQL or SQL data set, which drops the condition an empty parameter is used in.
     */
    protected String describeMissingArgument(ReportQuery reportQuery, String name, Map<String, Object> params) {
        if (params.containsKey(name)) {
            return String.format("The query of data set [%s] references parameter [%s], which this run left "
                    + "empty. The query binds every parameter it references, so fill the parameter in or "
                    + "regenerate the query without it", reportQuery.getName(), name);
        }

        return String.format("The query of data set [%s] references parameter [%s], but the report run provides "
                + "no value for it", reportQuery.getName(), name);
    }

    protected record CollectedParameters(Map<String, Object> availableParameters,
                                         List<String> requiredResultProperties) {
    }

    /**
     * What one report run has already done, so that loading a band once per parent row does not repeat it: the
     * queries already read for it and the warnings already written for it.
     * <p>
     * A run is recognised by the band data its hierarchy is rooted at. Reaching a different root means another
     * run has started on this thread, and everything remembered for the previous one is dropped — which is also
     * what keeps the scope from outliving the run on a pooled thread.
     */
    protected static class RunScope {

        protected WeakReference<@Nullable BandData> rootBand = new WeakReference<>(null);
        protected Map<String, Optional<LlmDataQuery>> storedQueries = new LinkedHashMap<>();
        protected Map<String, String> storeNames = new LinkedHashMap<>();
        /**
         * Held weakly, as the root band is and for the same reason: a data set belongs to a report, whose bands,
         * parameters and templates — the content of a template included — would otherwise stay reachable from a
         * pooled thread until another run replaces the scope.
         */
        protected Map<ReportQuery, Set<String>> warnings = new WeakHashMap<>();

        protected void rootedAt(@Nullable BandData rootBand) {
            if (this.rootBand.get() == rootBand && rootBand != null) {
                return;
            }

            this.rootBand = new WeakReference<>(rootBand);
            storedQueries = new LinkedHashMap<>();
            storeNames = new LinkedHashMap<>();
            warnings = new WeakHashMap<>();
        }

        /**
         * Returns what a stored document reads as, reading each document once. A band under a parent is loaded
         * once per parent row, and the document does not change while the run reads it, so parsing it again per
         * row is work the run does not need. A document that reads as nothing is remembered as such.
         */
        @Nullable
        protected LlmDataQuery storedQuery(String document, Supplier<@Nullable LlmDataQuery> read) {
            return storedQueries.computeIfAbsent(document, key -> Optional.ofNullable(read.get())).orElse(null);
        }

        /**
         * Returns the store a query text runs in, working it out once per text: telling the store means parsing
         * the query, and a band under a parent is loaded once per parent row.
         */
        protected String storeName(String jpql, Supplier<String> resolve) {
            return storeNames.computeIfAbsent(jpql, key -> resolve.get());
        }

        /**
         * Writes a warning the first time this data set runs into it. Remembered per data set rather than per
         * name: a data set name is unique only within its band, so two bands can each hold a {@code dataSet1},
         * and what one of them has already said must not silence the other. The data set is the one the engine
         * is loading, so it stays reachable for as long as the run needs the entry.
         */
        protected void warnOnce(ReportQuery reportQuery, String reason, Runnable warning) {
            if (warnings.computeIfAbsent(reportQuery, query -> new LinkedHashSet<>()).add(reason)) {
                warning.run();
            }
        }
    }
}
