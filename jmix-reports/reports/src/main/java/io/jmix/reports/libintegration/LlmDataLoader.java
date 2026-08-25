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

import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformer;
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
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
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

    /**
     * A parameter matched with {@code IN}, with or without parentheses around it. Such a parameter cannot be
     * given an empty value: an {@code IN} over nothing matches nothing, and a {@code (:name is null or …)} guard
     * does not switch it off.
     */
    protected static final Pattern IN_PARAMETER_PATTERN = Pattern.compile(
            "\\bin\\s*\\(?\\s*:([A-Za-z_][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected LlmDataQuerySerializer llmDataQuerySerializer;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected PolicyStore policyStore;

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
        // Permission first, and that means before the query is judged as a query: the diagnostics of the barrier
        // below name the entities and the columns of the text, which a user who may not read them should not be
        // told either. The platform does not stop a value load over an entity the current user may not read, so
        // this type checks it itself. Checked once per query text per run — the permissions of a run do not
        // change between the rows of a parent band.
        scope.entityReadChecked(query.getJpql(), () -> checkEntityReadPermitted(query));

        // Then whether the text can be executed at all, which is what its own diagnostics are about — and which
        // has to be settled before anything rewrites it: a text refused here is not one to weave conditions into.
        // Once per text per run, like the check above.
        scope.queryChecked(query.getJpql(), () -> checkQueryIsFitToExecute(reportQuery, query));

        // Then the rest of the permissions: the platform weaves the row-level policies of the query's own entity
        // into a value load, and the policies of everything else it reads are this loader's to apply — or, where
        // they cannot be applied, to refuse the query over.
        String jpql = scope.rowLevelJpql(query.getJpql(),
                () -> applyRowLevelPolicies(reportQuery, query.getJpql()));

        if (enforcesAxes) {
            LlmCrossTabAxes.checkAxesAreLinkable(reportQuery.getName(), query, params,
                    collectedParameters.requiredResultProperties(), bandName);
        }

        Map<String, @Nullable Object> arguments =
                resolveArguments(reportQuery, query, availableParameters, params, scope);
        String storeName = scope.storeName(query.getJpql(), () -> resolveStoreName(query));
        log.debug("Executing the query of data set [{}] in store [{}]: {}",
                reportQuery.getName(), storeName, jpql);

        List<Map<String, @Nullable Object>> rows;
        try {
            rows = executeQuery(query, jpql, arguments, storeName);
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
     * Executes the stored query through {@code DataManager}, so that the attribute permissions of the current
     * user and the row-level policies of the query's root entity apply as they do to any other data set: an
     * attribute the user may not read comes back as {@code null}. Entity READ is checked before this, by
     * {@link #checkEntityReadPermitted}, because the platform does not act on it for a value load.
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
     * @param query     stored query, which names the columns the rows are keyed by and the count it is limited to
     * @param jpql      text to execute — the stored one with the row-level policies of what it reads woven in
     * @param arguments value to bind per parameter the query references
     * @param storeName data store to execute in, the one the query's entity belongs to
     * @return one row per row the query returned
     */
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               String jpql,
                                                               Map<String, @Nullable Object> arguments,
                                                               String storeName) {
        FluentValuesLoader valuesLoader = dataManager.loadValues(jpql)
                .store(storeName)
                .properties(query.getResultProperties());
        arguments.forEach(valuesLoader::parameter);

        // What the prompt asked for as a count — "the top 5 customers" — is carried by the query rather than by
        // its text, JPQL having no `limit`, and is applied per execution: a band under a parent gets that many
        // rows for each parent row, which is what a nested band asking for the top few means.
        if (query.getFirstResult() != null) {
            valuesLoader.firstResult(query.getFirstResult());
        }
        if (query.getMaxResults() != null) {
            valuesLoader.maxResults(query.getMaxResults());
        }

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
     * the entity model through {@code DataManager}, one whose parameters could not be bound, one that names no
     * column to key its rows by, and one that selects an entity rather than its attributes.
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
        checkQuerySelectsValues(reportQuery, query);
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
     * the same JSON for every row would be work done to reach the same result. Reading is all this does — whether
     * the query may run, and whether its text can run at all, is asked afterwards and in that order, so that a
     * user refused the data is told about the permission rather than about the query
     * ({@link #checkEntityReadPermitted}, then {@link #checkQueryIsFitToExecute}).
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
            storedQuery = scope.storedQuery(storedDocument, () -> llmDataQuerySerializer.fromJson(storedDocument));
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
     * <p>
     * A run parameter the report knows is offered even when this run left it empty, so that a query guarding its
     * condition with {@code (:name is null or …)} has a {@code null} to switch on. A collection with nothing in it
     * is the exception, and so are a {@code null} parent-band field and an axis value: see
     * {@code decisions/0016}.
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
            if (!LlmQueryParameterNames.isValid(param.getKey())) {
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

            // A collection with nothing to match has no value a query could use: an IN over an empty list is a
            // syntax error, and — measured against EclipseLink — an (:names is null or … in :names) guard does
            // not rescue it either, it just matches nothing. Such a parameter is left out, and a query
            // referencing it says so, rather than emptying the band in silence.
            if (isEmptyCollection(value)) {
                continue;
            }

            // A run parameter the report knows is offered even when this run left it empty: an optional
            // parameter arrives as null, and a query generated for it guards its condition with
            // (:name is null or …), which needs that null bound. Binding nothing at all would fail a query
            // written precisely to survive an empty value.
            availableParameters.put(param.getKey(), value);
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
     * Refuses a query that selects an entity itself rather than its attributes.
     * <p>
     * Two reasons, and either alone would be enough. A band row is a tabular value — a template prints it, and an
     * entity prints as whatever its {@code toString} says. And, measured: the attribute permissions of the
     * current user are applied by masking *selected columns*, so an entity handed back whole carries the
     * attributes that masking would have hidden. Selecting {@code p} where {@code p.name} is denied really does
     * return the name.
     * <p>
     * The add-on's own generation prompt says the same ("Do not return the root entity alias itself as the
     * selected value"), so this refuses what generation was already told not to produce — and what an import or a
     * hand edit can still carry in.
     */
    protected void checkQuerySelectsValues(ReportQuery reportQuery, LlmDataQuery query) {
        List<String> selectedEntities;
        try {
            // The parser reads the text lazily, so the walk belongs inside: a text it cannot read fails on its
            // own terms when it executes, naming the data set, which says more than a parse error here would.
            selectedEntities = selectedEntitiesOf(queryTransformerFactory.parser(query.getJpql()));
        } catch (RuntimeException e) {
            log.debug("The selected values of [{}] cannot be told, so they are not checked", query.getJpql(), e);
            return;
        }

        if (!selectedEntities.isEmpty()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] selects the entities %s themselves rather than their "
                            + "attributes. A band prints values, and an entity would also carry the attributes "
                            + "the current user may not read: select the attributes the report needs",
                    reportQuery.getName(), selectedEntities));
        }
    }

    /**
     * Names what a query selects whole instead of as a value — an alias, or a path that ends in an entity or an
     * embeddable.
     * <p>
     * An alias is told by the selected path itself: {@code select p.name} gives a path whose property is the
     * attribute {@code name}, while {@code select p} gives one whose property is the variable's own alias
     * {@code p}. So a selected path whose property *is* its variable is the entity — which holds for a joined
     * alias exactly as for the root one, where asking the parser whether this is an "entity select" only ever
     * answers about the root.
     * <p>
     * A path is told by what the data model says it ends in: {@code select g.publisher} names a property, not an
     * alias, and yet — measured — hands back a whole {@code Publisher} whose denied attributes read fine, since
     * masking is applied to the selected column and the column here *is* the entity. {@code Range#isClass} covers an
     * embeddable too, which is an object a band cannot print either.
     * <p>
     * A property the entity does not have is deliberately not treated as one: {@code select p.noSuchAttribute} is
     * a query written against another data model, which has its own and better failure. A constant or an
     * aggregate ({@code select 1}, {@code select count(p)}) contributes no selected path at all, so neither is
     * mistaken for an entity.
     */
    protected List<String> selectedEntitiesOf(QueryParser parser) {
        List<String> selected = new ArrayList<>();
        for (QueryParser.QueryPath path : parser.getQueryPaths()) {
            if (!path.isSelectedPath()) {
                continue;
            }

            String selectedWhole = path.getPropertyPath().equals(path.getVariableName())
                    ? path.getEntityName()
                    : entityValuedPathOf(path);
            if (selectedWhole != null && !selected.contains(selectedWhole)) {
                selected.add(selectedWhole);
            }
        }
        return selected;
    }

    /**
     * Returns the description of a selected path that ends in an entity or an embeddable, or {@code null} when it
     * ends in a value or cannot be placed in the data model at all.
     */
    @Nullable
    protected String entityValuedPathOf(QueryParser.QueryPath path) {
        MetaClass entity = metadata.findClass(path.getEntityName());
        if (entity == null) {
            return null;
        }

        MetaPropertyPath propertyPath = entity.getPropertyPath(path.getPropertyPath());
        if (propertyPath == null || !propertyPath.getRange().isClass()) {
            return null;
        }
        return path.getVariableName() + "." + path.getPropertyPath()
                + " (" + propertyPath.getRange().asClass().getName() + ")";
    }

    /**
     * Weaves the row-level policies of the entities a query reads into its text, so that a band shows the rows
     * the current user is allowed to see rather than all of them.
     * <p>
     * The platform does this for one entity only. {@code JpaDataStore#createLoadQuery} builds a single
     * {@code ReadEntityQueryContext}, for the entity the parser calls the query's own, and
     * {@code ReadEntityQueryConstraint} weaves that entity's {@code JPQL} policies into it. An entity reached by
     * a join is left untouched — and a report query joins as a matter of course, which would show the rows of a
     * joined entity that its policies exist to hide.
     * <p>
     * So the same thing is done here for everything else the query reads: the policy's {@code {E}} placeholder is
     * replaced with that entity's own alias — {@code QueryParser#getEntityAlias} gives it — and the condition is
     * added to the {@code where} through the platform's own {@code QueryTransformer}, which places it ahead of a
     * {@code group by}, a {@code having} or an {@code order by} the query already has. Only conditions are added,
     * never joins (see below). Nothing at all is added for a query whose entities carry no policies, which is
     * every query in an application that has none.
     * <p>
     * Three shapes cannot be filtered, and are refused rather than executed unfiltered:
     * <ul>
     *     <li>a {@code PREDICATE} policy, which is a Java predicate evaluated against an entity instance. A value
     *     load returns rows, not instances, so the platform does not apply such a policy even to the query's own
     *     entity — which is why this is asked before that entity is left to the platform;</li>
     *     <li>an entity that appears <em>only inside a subquery</em>, whose alias is not in scope where a
     *     condition would have to be added. {@code getEntityAlias} answers {@code null} for exactly that, and an
     *     entity carrying several aliases is refused for the same reason — a condition on one of them would leave
     *     the others unfiltered;</li>
     *     <li>a policy with a {@code joinClause}, on any entity but the query's own: measured,
     *     {@code QueryTransformer#addJoinAndWhere} re-bases the join onto the root alias, so the condition would
     *     filter another entity or name a path that does not exist.</li>
     * </ul>
     * Each refusal names the entity and says what to do about it, because there is something to do: make that
     * entity the query's own — {@code select p.name from Publisher p where …} rather than a join into it — and the
     * platform filters it, or, for a predicate policy, write that policy as JPQL.
     * <p>
     * Parameters a policy references need no binding here: {@code session_*} names are resolved by the platform's
     * own {@code QueryParamValuesManager} when the query executes. Rows are not de-duplicated either — nothing
     * woven in here can multiply them, and a join the platform itself adds for the query's own entity does not
     * make it de-duplicate a list-returning load.
     * <p>
     * A text this parser cannot read — one using a construct EclipseLink accepts and the platform's own JPQL
     * grammar does not — is left as it is. Nothing is lost by that: the platform builds its own
     * {@code ReadEntityQueryContext} by parsing the text with the same parser, so such a query gets no policies
     * from the platform either, and does not execute through {@code loadValues} at all.
     *
     * @param jpql the stored query text
     * @return the text to execute, unchanged when nothing had to be woven in
     */
    protected String applyRowLevelPolicies(ReportQuery reportQuery, String jpql) {
        QueryParser parser;
        List<String> readEntities;
        String ownEntity;
        try {
            parser = queryTransformerFactory.parser(jpql);
            readEntities = readEntitiesOf(parser);
            // The entity the platform applies the policies of itself.
            ownEntity = parser.getEntityName();
        } catch (RuntimeException e) {
            // A text this parser cannot read gets no policies woven in — and gets none from the platform either,
            // whose own context parses it with the very same parser before it can execute anything.
            log.debug("The entities read by [{}] cannot be told, so no row-level policy is applied", jpql, e);
            return jpql;
        }

        QueryTransformer transformer = null;
        for (String entityName : readEntities) {
            MetaClass entity = metadata.findClass(entityName);
            if (entity == null) {
                continue;
            }

            List<RowLevelPolicy> policies = policyStore.getRowLevelPolicies(entity)
                    .filter(policy -> policy.getAction() == RowLevelPolicyAction.READ)
                    .toList();
            if (policies.isEmpty()) {
                continue;
            }

            // Before anything about aliases, and for the query's own entity as much as for the rest: a predicate
            // policy is evaluated against an entity instance, and no value load has one to evaluate it against —
            // the platform applies none of it either, root included.
            if (policies.stream().anyMatch(policy -> policy.getType() != RowLevelPolicyType.JPQL)) {
                throw new DataLoadingException(String.format(
                        "The stored query of data set [%s] reads entity [%s], which has a predicate row-level "
                                + "policy. Such a policy is evaluated against an entity instance, while this data "
                                + "set type reads values, so the rows it may show cannot be narrowed by it. A "
                                + "report over [%s] needs its row-level policy written as JPQL",
                        reportQuery.getName(), entity.getName(), entity.getName()));
            }

            String alias = aliasOf(parser, entityName);
            if (alias == null) {
                throw new DataLoadingException(String.format(
                        "The stored query of data set [%s] reads entity [%s], whose row-level policies cannot be "
                                + "applied to it: the entity is used in a subquery, or under more than one alias, "
                                + "so the rows it may show cannot be narrowed. Rewrite the query with [%s] as the "
                                + "entity it selects from",
                        reportQuery.getName(), entity.getName(), entity.getName()));
            }

            if (entityName.equals(ownEntity)) {
                // The platform weaves this one in — its where and its join alike, both written against the
                // alias the query is rooted at, which is the alias the transformer binds a join to. Reaching here
                // means that alias is the only one the entity has: an entity named both as the root and again
                // under a second alias was refused above, the platform narrowing the root and leaving the other.
                continue;
            }

            if (policies.stream().anyMatch(policy -> StringUtils.isNotBlank(policy.getJoinClause()))) {
                // Measured: `QueryTransformer#addJoinAndWhere` re-bases the join onto the query's root alias —
                // `join p.games x` on a joined `p` comes out as `join g.games x` — so the condition would filter
                // another entity, or the path would not exist at all. Refusing beats filtering the wrong rows,
                // and applying it properly needs a transformer that takes the alias to join from.
                throw new DataLoadingException(String.format(
                        "The stored query of data set [%s] reads entity [%s], whose row-level policy joins another "
                                + "entity. Such a policy can only be applied to the entity a query selects from, "
                                + "so rewrite the query with [%s] as that entity",
                        reportQuery.getName(), entity.getName(), entity.getName()));
            }

            for (RowLevelPolicy policy : policies) {
                String where = withAlias(policy.getWhereClause(), alias);
                if (StringUtils.isBlank(where)) {
                    // A policy without a condition narrows nothing. Its join, if it had one, was refused above.
                    continue;
                }

                if (transformer == null) {
                    transformer = queryTransformerFactory.transformer(jpql);
                }
                transformer.addWhere(where);
            }
        }

        return transformer != null ? transformer.getResult() : jpql;
    }

    /**
     * Names the entities of a query graph in a stable order.
     * <p>
     * Sorted, so that which of several entities a message blames does not depend on hashing, and without the
     * nulls the parser leaves for a name it could not resolve — it honours no nullness contract, whatever the
     * declared element type says, and sorting would trip over one.
     */
    protected List<String> readEntitiesOf(QueryParser parser) {
        //noinspection ConstantValue
        return parser.getAllEntityNames().stream().filter(Objects::nonNull).sorted().toList();
    }

    /**
     * Returns the alias a condition on this entity can be written against, or {@code null} when there is none to
     * write against: the entity lives in a subquery, or carries more than one alias, of which filtering one would
     * leave the others as they were.
     */
    @Nullable
    protected String aliasOf(QueryParser parser, String entityName) {
        String alias = parser.getEntityAlias(entityName);
        // Declared non-null, measured to answer null for an entity that only a subquery names: the parser honours
        // no nullness contract here, as it does not for the entity names either.
        //noinspection ConstantValue
        if (alias == null) {
            return null;
        }

        long aliases = parser.getQueryPaths().stream()
                .filter(path -> entityName.equals(path.getEntityName()))
                .map(QueryParser.QueryPath::getVariableName)
                .distinct()
                .count();
        return aliases > 1 ? null : alias;
    }

    @Nullable
    protected String withAlias(@Nullable String clause, String alias) {
        return clause == null ? null : clause.replace(QueryTransformer.ALIAS_PLACEHOLDER, alias);
    }

    /**
     * Refuses a query that reads an entity the current user may not read — every entity in it, not only the one
     * whose attributes it selects.
     * <p>
     * The platform leaves this open twice over. {@code DataStoreCrudValuesListener} consumes only the denied
     * *columns* of a value load and never reads {@code isPermitted()}, so a denied entity does not stop the
     * query; and {@code LoadValuesAccessContext#getEntityClasses()} — what the platform's own constraint judges
     * by — is built from the *selected* paths alone, so it sees neither the root of
     * {@code select p.name from GameTitle g join g.publisher p} nor any entity of
     * {@code select 1 as marker from Publisher p}.
     * <p>
     * So the question is asked here, of every entity the query graph names
     * ({@code QueryParser#getAllEntityNames}, which reaches into subqueries as well), through the platform's own
     * {@code CrudEntityContext} — the context entity READ is actually decided by. Denied *columns* are still left to the platform, which masks
     * them.
     */
    protected void checkEntityReadPermitted(LlmDataQuery query) {
        List<String> entityNames;
        try {
            entityNames = readEntitiesOf(queryTransformerFactory.parser(query.getJpql()));
        } catch (RuntimeException e) {
            // A query written against another data model tells the check nothing, cannot execute either, and
            // fails on its own terms with a message naming the data set. Nothing is let through that would not
            // have been: the query is about to fail.
            log.debug("The entities read by [{}] cannot be told, so the entity read check is skipped",
                    query.getJpql(), e);
            return;
        }

        for (String entityName : entityNames) {
            MetaClass entity = metadata.findClass(entityName);
            if (entity == null) {
                // A name the model does not know: the query cannot execute either, and says so itself.
                continue;
            }

            CrudEntityContext entityContext = new CrudEntityContext(entity);
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isReadPermitted()) {
                throw new AccessDeniedException("entity", entity.getName(), EntityOp.READ.getId());
            }
        }
    }

    /**
     * Names the parameters the query matches with {@code IN}, which is what makes an empty value unusable for
     * them. Read off the text because nothing else can say it: a collection parameter left empty reaches a run
     * as {@code null}, indistinguishable from an empty scalar, and the stored document declares no cardinality.
     * String literals are blanked first, so an {@code in :name} spelled inside one is text.
     */
    protected Set<String> parametersMatchedWithIn(String jpql) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = IN_PARAMETER_PATTERN.matcher(LlmQueryParameterNames.stripStringLiterals(jpql));
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    protected boolean isEmptyCollection(@Nullable Object value) {
        return value instanceof Collection<?> values && values.stream().noneMatch(Objects::nonNull);
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

            if (isEmptyCollection(value)) {
                continue;
            }

            if (availableParameters.containsKey(name)) {
                // The band is loaded once per parent row, and the collision is the same every time.
                scope.warnOnce(reportQuery, "shadowed-band-field:" + name,
                        () -> log.warn("Parameter [{}] is already available, so the field [{}] of band [{}] is not "
                                + "offered to the query; rename one of them to make both usable",
                                name, field.getKey(), band.getName()));
            } else {
                availableParameters.put(name, value);
            }
        }
    }

    /**
     * Collects the value to bind for every parameter the query references. The type the query declares for a
     * parameter is not consulted: it was written to tell a model what the value would be, and a value is bound
     * as the run holds it.
     */
    protected Map<String, @Nullable Object> resolveArguments(ReportQuery reportQuery, LlmDataQuery query,
                                                             Map<String, Object> availableParameters,
                                                             Map<String, Object> params, RunScope scope) {
        Map<String, @Nullable Object> arguments = new LinkedHashMap<>();
        Set<String> matchedWithIn =
                scope.parametersMatchedWithIn(query.getJpql(), () -> parametersMatchedWithIn(query.getJpql()));

        for (LlmQueryParameter parameter : query.getParameters()) {
            String name = parameter.getName();
            if (!availableParameters.containsKey(name)) {
                throw new DataLoadingException(describeMissingArgument(reportQuery, name, params));
            }

            Object value = availableParameters.get(name);
            if (value == null && matchedWithIn.contains(name)) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] matches parameter [%s] with IN, and this run left it empty: "
                                + "an IN condition cannot match an empty value and no guard switches it off. "
                                + "Fill the parameter in or regenerate the query without it",
                        reportQuery.getName(), name));
            }

            arguments.put(name, value);
        }

        return arguments;
    }

    /**
     * Says why a parameter the query references cannot be bound, telling two cases apart. A name the run has
     * never heard of means a query that does not match its report. A name the run knows reaches here only when
     * its value cannot be bound as it stands — a collection with nothing to match, above all — since an empty
     * value the report knows is otherwise bound as {@code null} for a guarded condition to switch off.
     */
    protected String describeMissingArgument(ReportQuery reportQuery, String name, Map<String, Object> params) {
        if (params.containsKey(name)) {
            return String.format("The query of data set [%s] references parameter [%s], whose value this run "
                    + "cannot bind as it stands — a collection with nothing to match, for instance. Fill the "
                    + "parameter in or regenerate the query without it", reportQuery.getName(), name);
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
        protected Map<String, Set<String>> inParameters = new LinkedHashMap<>();
        protected Map<String, String> rowLevelJpql = new LinkedHashMap<>();
        protected Set<String> entityReadChecked = new LinkedHashSet<>();
        protected Set<String> queryChecked = new LinkedHashSet<>();
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
            inParameters = new LinkedHashMap<>();
            rowLevelJpql = new LinkedHashMap<>();
            entityReadChecked = new LinkedHashSet<>();
            queryChecked = new LinkedHashSet<>();
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
         * Returns the parameters a query text matches with {@code IN}, reading each text once: telling them
         * apart means scanning the query, which no row of a parent band changes.
         */
        protected Set<String> parametersMatchedWithIn(String jpql, Supplier<Set<String>> read) {
            return inParameters.computeIfAbsent(jpql, key -> read.get());
        }

        /**
         * Returns the text to execute for a stored query text, weaving the row-level policies into it once: the
         * policies of a run do not change between the rows of a parent band, and weaving them parses the query.
         */
        protected String rowLevelJpql(String jpql, Supplier<String> weave) {
            return rowLevelJpql.computeIfAbsent(jpql, key -> weave.get());
        }

        /**
         * Runs the entity-read check once per query text: the permissions of a run do not change between the
         * rows of a parent band, and the check parses the query to find the entities it reads.
         */
        protected void entityReadChecked(String jpql, Runnable check) {
            checkedOnce(entityReadChecked, jpql, check);
        }

        /**
         * Runs the barrier once per query text: what makes a text unfit to execute does not change between the
         * rows of a parent band, and establishing it parses and scans the query.
         */
        protected void queryChecked(String jpql, Runnable check) {
            checkedOnce(queryChecked, jpql, check);
        }

        /**
         * Runs a check the first time a query text reaches it, remembering it only <em>once it has passed</em>: a
         * refusal must be raised again for every data set and every parent row that reaches this query, not
         * swallowed because the first attempt already ran.
         */
        protected void checkedOnce(Set<String> passed, String jpql, Runnable check) {
            if (passed.contains(jpql)) {
                return;
            }

            check.run();
            passed.add(jpql);
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
