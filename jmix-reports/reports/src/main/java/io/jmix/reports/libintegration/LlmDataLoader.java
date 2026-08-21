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
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.security.AccessDeniedException;
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

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected LlmDataQuerySerializer llmDataQuerySerializer;

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
        // them for every band extracted afterwards; what belongs to this band is told by its name.
        String bandName = (String) additionalParams.get(DataSet.BAND_NAME);

        String emptyAxis = LlmCrossTabAxes.firstEmptyAxis(params, bandName);
        if (emptyAxis != null) {
            // A cross-tab has no cells along an axis that produced no values, so there is nothing for this
            // query to return — and its parameters, which name that axis, have no values to bind either.
            scope.warnOnce(reportQuery, "empty-axis:" + emptyAxis,
                    () -> log.debug("The cross-tab axis [{}] produced no values, so data set [{}] is not executed",
                            emptyAxis, reportQuery.getName()));
            return List.of();
        }

        CollectedParameters collectedParameters =
                collectAvailableParameters(reportQuery, params, parentBand, bandName, scope);
        Map<String, Object> availableParameters = collectedParameters.availableParameters();

        LlmDataQuery query = resolveQuery(reportQuery, additionalParams, scope);
        LlmCrossTabAxes.checkAxesAreLinkable(reportQuery.getName(), query, params,
                collectedParameters.requiredResultProperties(), bandName);

        Map<String, Object> arguments = resolveArguments(reportQuery, query, availableParameters, params);
        log.debug("Executing the query of data set [{}]: {}", reportQuery.getName(), query.getJpql());

        List<Map<String, @Nullable Object>> rows;
        try {
            rows = executeQuery(query, arguments);
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
     * @return one row per row the query returned
     */
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               Map<String, Object> arguments) {
        FluentValuesLoader valuesLoader = dataManager.loadValues(query.getJpql())
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
     * Refuses a stored query that does more than read the entity model through {@code DataManager}: one that is
     * not a select, and one that reaches past JPQL into the database itself, whose text this loader would
     * otherwise hand over as it stands. See {@link #NATIVE_ESCAPE_PATTERN} for what counts as reaching past.
     * <p>
     * The designer checks a query the add-on generated or the author edited, and refuses some of this. A report
     * also arrives by import, though, bringing whatever text the file holds and no add-on to check it with — so
     * what a run promises about a query, a run has to establish itself.
     * <p>
     * Write keywords are not looked for: a JPQL query is a single statement, so {@code update} or {@code delete}
     * inside a select is a word rather than an operation, and refusing a query for the name of an attribute
     * would cost more than it saves.
     */
    protected void checkQueryOnlyReads(ReportQuery reportQuery, LlmDataQuery query) {
        String jpql = query.getJpql();
        if (!Strings.CI.startsWith(jpql.stripLeading(), "select")) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] is not a select, so it is not executed", reportQuery.getName()));
        }

        // Blanked of literals, so that a call spelled inside one is read as the text it is.
        Matcher nativeEscape =
                NATIVE_ESCAPE_PATTERN.matcher(LlmQueryParameterNames.stripStringLiterals(jpql));
        if (nativeEscape.find()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] calls [%s], which reaches the database directly and would "
                            + "leave the data access constraints of the current user behind, so it is not executed",
                    reportQuery.getName(), nativeEscape.group(1)));
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
     * found fit to execute — {@link #checkQueryOnlyReads} judges the text, which no row of a parent band
     * changes, so it is judged where the text is read.
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
                    checkQueryOnlyReads(reportQuery, read);
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

            // An axis of this band holds the rows of another data set, so it is offered field by field rather
            // than as it is. An axis of another band is not this band's business at all.
            if (LlmQueryParameterNames.isCrossTabAxis(param.getKey()) && LlmCrossTabAxes.isAxisRows(value)) {
                if (LlmCrossTabAxes.isAxisOf(param.getKey(), bandName)) {
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
