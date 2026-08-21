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
 * Loads band data for the {@link DataSetType#LLM} data set type: executes the query stored in the data set.
 * <p>
 * A run never generates a query. The query is generated in the report designer of a running application and
 * stored with the report, which is what makes a run reproducible, free of model calls and independent of
 * whether a model is reachable at all.
 */
@NullMarked
public class LlmDataLoader implements ReportDataLoader {

    private static final Logger log = LoggerFactory.getLogger(LlmDataLoader.class);

    /**
     * A single-quoted JPQL string literal, doubled quotes included.
     */
    protected static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");

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

        String emptyAxis = firstEmptyCrossTabAxis(params);
        if (emptyAxis != null) {
            // A cross-tab has no cells along an axis that produced no values, so there is nothing for this
            // query to return — and its parameters, which name that axis, have no values to bind either.
            scope.warnOnce(reportQuery, "empty-axis:" + emptyAxis,
                    () -> log.debug("The cross-tab axis [{}] produced no values, so data set [{}] is not executed",
                            emptyAxis, reportQuery.getName()));
            return List.of();
        }

        CollectedParameters collectedParameters =
                collectAvailableParameters(reportQuery, params, parentBand, scope);
        Map<String, LlmQueryParameter> availableParameters = collectedParameters.availableParameters();

        LlmDataQuery query = resolveQuery(reportQuery, additionalParams, scope);
        checkCrossTabAxesAreLinkable(reportQuery, query, params, availableParameters,
                collectedParameters.requiredResultProperties());

        List<LlmQueryParameter> arguments = resolveArguments(reportQuery, query, availableParameters, params);
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

        return toBandRows(rows, query.getResultProperties());
    }

    /**
     * Executes the stored query through {@code DataManager}, so that the entity and attribute permissions of the
     * current user and the row-level policies of the query's root entity apply as they do to any other data set.
     * An attribute the user may not read comes back as {@code null}.
     * <p>
     * Values are bound as named JPQL parameters, never inlined into the text: the query is written once and run
     * with whatever the report parameters and the parent band hold this time.
     *
     * @param query     stored query to execute
     * @param arguments values to bind, one per parameter the query references
     * @return the rows as the query returned them, keyed by the query's result properties
     */
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               List<LlmQueryParameter> arguments) {
        FluentValuesLoader valuesLoader = dataManager.loadValues(query.getJpql())
                .properties(query.getResultProperties());
        for (LlmQueryParameter argument : arguments) {
            valuesLoader.parameter(argument.getName(), argument.getValue());
        }

        List<Map<String, @Nullable Object>> rows = new ArrayList<>();
        for (KeyValueEntity row : valuesLoader.list()) {
            Map<String, @Nullable Object> values = new LinkedHashMap<>();
            for (String property : query.getResultProperties()) {
                values.put(property, row.getValue(property));
            }
            rows.add(values);
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

        // Emptied of literals, so that a call spelled inside one is read as the text it is.
        Matcher nativeEscape = NATIVE_ESCAPE_PATTERN.matcher(STRING_LITERAL_PATTERN.matcher(jpql).replaceAll("''"));
        if (nativeEscape.find()) {
            throw new DataLoadingException(String.format(
                    "The stored query of data set [%s] calls [%s], which reaches the database directly and would "
                            + "leave the data access constraints of the current user behind, so it is not executed",
                    reportQuery.getName(), nativeEscape.group(1)));
        }
    }

    /**
     * Repackages the rows into what the report engine expects. The add-on returns immutable maps in an
     * unspecified order; a band row must be mutable ({@link ReportDataLoader} states so, and merging several
     * data sets of one band writes into it) and must follow the select clause (a cross-tab links its cells by
     * the first matching column). Values, including null and an empty string, are preserved as distinct values.
     */
    protected List<Map<String, Object>> toBandRows(List<Map<String, @Nullable Object>> rows,
                                                   List<String> resultProperties) {
        List<Map<String, @Nullable Object>> bandRows = new ArrayList<>(rows.size());

        for (Map<String, @Nullable Object> row : rows) {
            Map<String, @Nullable Object> bandRow = new LinkedHashMap<>();
            for (String property : resultProperties) {
                if (row.containsKey(property)) {
                    bandRow.put(property, row.get(property));
                }
            }

            // A column the query returned but the stored document does not name still belongs to the row.
            for (Map.Entry<String, @Nullable Object> column : row.entrySet()) {
                bandRow.putIfAbsent(column.getKey(), column.getValue());
            }
            bandRows.add(bandRow);
        }

        //noinspection NullableProblems
        return bandRows;
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
     * Collects the parameters the query may reference, keyed by name: the run parameters first, then the fields
     * of every parent row up the band hierarchy, then the values of the cross-tab axes this band is built from.
     * A parameter with no value is left out — its type is unknown, and the add-on binds no value for it, which
     * would fail the query anyway.
     */
    protected CollectedParameters collectAvailableParameters(ReportQuery reportQuery,
                                                             Map<String, Object> params,
                                                             @Nullable BandData parentBand,
                                                             RunScope scope) {
        Map<String, LlmQueryParameter> availableParameters = new LinkedHashMap<>();
        Map<String, List<?>> crossTabAxes = new LinkedHashMap<>();
        List<String> requiredResultProperties = new ArrayList<>();

        for (Map.Entry<String, Object> param : params.entrySet()) {
            Object value = param.getValue();
            // A report parameter left unfilled arrives as a null value, whatever the map's declared type says.
            //noinspection ConstantValue
            if (value == null || !LlmQueryParameterNames.isValid(param.getKey())) {
                continue;
            }

            // An axis holds the rows of another data set, so it is offered field by field rather than as it is.
            if (LlmQueryParameterNames.isCrossTabAxis(param.getKey()) && isAxisRows(value)) {
                crossTabAxes.put(param.getKey(), (List<?>) value);
                continue;
            }

            LlmQueryParameter parameter = toParameter(param.getKey(), value);
            if (parameter != null) {
                availableParameters.put(param.getKey(), parameter);
            }
        }

        // The walk stops short of the root band: its data is the run parameters, which are already offered
        // under their own names, and offering them a second time as Root_<name> would double every parameter
        // and describe a dictionary the designer never shows.
        for (BandData band = parentBand; band != null && !BandData.ROOT_BAND_NAME.equals(band.getName());
             band = band.getParentBand()) {
            addParentBandFields(reportQuery, band, availableParameters, scope);
        }

        crossTabAxes.forEach((dataSetName, rows) -> addCrossTabAxisValues(reportQuery, dataSetName, rows,
                availableParameters, requiredResultProperties, scope));

        return new CollectedParameters(availableParameters, List.copyOf(requiredResultProperties));
    }

    /**
     * Describes one run parameter. A parameter holding several values — a "list of entities" parameter, for
     * instance — is offered as multi-valued and typed by its elements, so that a query matches it with
     * {@code IN} instead of comparing a collection for equality. A collection with nothing in it is left out:
     * there is no value to match and no type to state.
     */
    @Nullable
    protected LlmQueryParameter toParameter(String name, Object value) {
        if (!(value instanceof Collection<?> values)) {
            return new LlmQueryParameter(name, value.getClass().getName(), value);
        }

        Object element = values.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (element == null) {
            return null;
        }

        return new LlmQueryParameter(name, element.getClass().getName(), value, true);
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
                                       Map<String, LlmQueryParameter> availableParameters, RunScope scope) {
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

            LlmQueryParameter parameter = toParameter(name, value);
            if (parameter == null) {
                continue;
            }

            LlmQueryParameter present = availableParameters.putIfAbsent(name, parameter);
            if (present != null) {
                // The band is loaded once per parent row, and the collision is the same every time.
                scope.warnOnce(reportQuery, "shadowed-band-field:" + name,
                        () -> log.warn("Parameter [{}] is already available, so the field [{}] of band [{}] is not "
                                + "offered to the query; rename one of them to make both usable",
                                name, field.getKey(), band.getName()));
            }
        }
    }

    /**
     * Fails a query that cannot be placed into the matrix of a cross-tab band.
     * <p>
     * {@code CrossTabExtractionController} links a cell to its column and to its row by looking for a result
     * column whose name starts with the axis data set's name; without one, every cell is dropped and the band
     * renders empty with no error at all. Failing here turns that silence into a message.
     */
    protected void checkCrossTabAxesAreLinkable(ReportQuery reportQuery, LlmDataQuery query,
                                                Map<String, Object> params,
                                                Map<String, LlmQueryParameter> availableParameters,
                                                List<String> requiredResultProperties) {
        for (String name : params.keySet()) {
            if (!LlmQueryParameterNames.isCrossTabAxis(name) || !isAxisRows(params.get(name))) {
                continue;
            }

            String prefix = LlmQueryParameterNames.ofCrossTabAxisPrefix(name);
            // An axis that produced no value has no columns either, so there is nothing to link a cell to.
            if (availableParameters.keySet().stream().noneMatch(parameter -> parameter.startsWith(prefix))) {
                continue;
            }

            // The controller links by the first column starting with the axis name — not with the axis name and
            // an underscore — and then cuts one character more, so a column named after the axis without the
            // separator would be linked by a truncated field. Reading the query the same way here catches it.
            String returned = firstWithPrefix(query.getResultProperties(), name);
            if (returned == null) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns no column named [%s<field>], so its rows cannot be "
                                + "linked to the cross-tab axis [%s]; it returns %s",
                        reportQuery.getName(), prefix, name, query.getResultProperties()));
            }

            // A cross-tab links a cell by the first column of the axis prefix, so a query that puts another
            // field of the axis first would link the matrix by that field — a caption, for instance — and lose
            // the cells whose value differs from it.
            String required = firstWithPrefix(requiredResultProperties, prefix);
            if (required != null && !required.equals(returned)) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns [%s] before [%s], so the cross-tab axis [%s] would be "
                                + "linked by the wrong field; a cross-tab links a cell by the first column named "
                                + "after the axis. The query returns %s",
                        reportQuery.getName(), returned, required, name, query.getResultProperties()));
            }
        }
    }

    @Nullable
    protected String firstWithPrefix(List<String> names, String prefix) {
        return names.stream().filter(name -> name.startsWith(prefix)).findFirst().orElse(null);
    }

    /**
     * Returns the name of the first cross-tab axis of this band that produced no values, or {@code null} when
     * every axis has some. An axis is put into the params by the controller whether it produced rows or not.
     */
    @Nullable
    protected String firstEmptyCrossTabAxis(Map<String, Object> params) {
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (LlmQueryParameterNames.isCrossTabAxis(param.getKey())
                    && param.getValue() instanceof List<?> rows && rows.isEmpty()) {
                return param.getKey();
            }
        }

        return null;
    }

    /**
     * Tells the rows of a cross-tab axis from an ordinary parameter that merely happens to be named like one:
     * an axis holds rows, so it is a list of maps, and an empty list is the axis that produced nothing. A list
     * of anything else belongs to the report run and stays a parameter of its own.
     *
     * @param value value of a run parameter, which is {@code null} for a parameter left unfilled
     */
    protected boolean isAxisRows(@Nullable Object value) {
        return value instanceof List<?> rows && (rows.isEmpty() || rows.get(0) instanceof Map);
    }

    /**
     * Offers the values of one cross-tab axis, one parameter per field of its rows, so that a cell query can
     * narrow itself to the columns and rows the matrix actually has. The value is the whole list — the add-on
     * converts a collection element by element — and the type is the element's, not the list's.
     * <p>
     * Exactly one of those names is required back as a result column: a cross-tab links a cell to its axis by
     * the first returned column whose name starts with the axis prefix, so requiring every field would let a
     * caption column come first and the matrix link by the caption text. The required one is the axis's first
     * referenceable field, which is the order the axis itself describes — the same field on every run, so that
     * a query generated once keeps answering the requirement.
     * <p>
     * A field with no values in it is required back all the same, but offered no parameter: there is nothing
     * to match and no type to state. The same naming rules as for band fields apply: a name that is not an
     * identifier is skipped, and a name already taken is kept.
     */
    protected void addCrossTabAxisValues(ReportQuery reportQuery, String dataSetName, List<?> rows,
                                         Map<String, LlmQueryParameter> availableParameters,
                                         List<String> requiredResultProperties, RunScope scope) {
        for (Map.Entry<String, List<Object>> field : axisValuesByField(rows).entrySet()) {
            String name = LlmQueryParameterNames.ofCrossTabValue(dataSetName, field.getKey());
            if (!LlmQueryParameterNames.isValid(name)) {
                continue;
            }

            String axisPrefix = LlmQueryParameterNames.ofCrossTabAxisPrefix(dataSetName);
            if (requiredResultProperties.stream().noneMatch(required -> required.startsWith(axisPrefix))) {
                requiredResultProperties.add(name);
            }

            List<Object> values = field.getValue();
            if (values.isEmpty()) {
                // The column is still required — the axis has this field, and which field links the matrix is
                // decided by the axis, not by this run — but there is no value to offer and no type to state.
                continue;
            }

            LlmQueryParameter present = availableParameters.putIfAbsent(name,
                    new LlmQueryParameter(name, values.get(0).getClass().getName(), values, true));
            if (present != null) {
                scope.warnOnce(reportQuery, "shadowed-axis-field:" + name,
                        () -> log.warn("Parameter [{}] is already available, so the values of the field [{}] of "
                                + "the cross-tab axis [{}] are not offered to the query, while the column of that "
                                + "name may still be required back; rename one of them to make both usable",
                                name, field.getKey(), dataSetName));
            }
        }
    }

    /**
     * Groups the values of one cross-tab axis by the field they belong to, keyed by every field the axis has, in
     * the order its rows describe them: a field is a field of the axis whether this run left it empty or not, so
     * which one comes first does not change with the data — the required column would otherwise move between
     * runs and stop matching the stored query. A row that is not a row and a field without a value contribute
     * nothing.
     */
    protected Map<String, List<Object>> axisValuesByField(List<?> rows) {
        Map<String, List<Object>> valuesByField = new LinkedHashMap<>();

        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> fields)) {
                continue;
            }

            for (Map.Entry<?, ?> field : fields.entrySet()) {
                List<Object> values = valuesByField.computeIfAbsent(String.valueOf(field.getKey()),
                        name -> new ArrayList<>());
                Object value = field.getValue();
                if (value != null) {
                    values.add(value);
                }
            }
        }

        return valuesByField;
    }

    /**
     * Builds one argument per parameter the query references. The type comes from the report parameter, not
     * from the query's own declaration: the add-on coerces the value to the declared type, so a type the
     * model guessed wrong would corrupt the value.
     */
    protected List<LlmQueryParameter> resolveArguments(ReportQuery reportQuery, LlmDataQuery query,
                                                       Map<String, LlmQueryParameter> availableParameters,
                                                       Map<String, Object> params) {
        List<LlmQueryParameter> arguments = new ArrayList<>(query.getParameters().size());

        for (LlmQueryParameter parameter : query.getParameters()) {
            LlmQueryParameter available = availableParameters.get(parameter.getName());
            if (available == null) {
                throw new DataLoadingException(describeMissingArgument(reportQuery, parameter.getName(), params));
            }

            arguments.add(available);
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

    protected record CollectedParameters(Map<String, LlmQueryParameter> availableParameters,
                                         List<String> requiredResultProperties) {
    }

    /**
     * What one report run has already done, so that loading a band once per parent row does not repeat it: the
     * queries generated within the run and the warnings already written for it.
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
        protected LlmDataQuery storedQuery(String document, Supplier<@Nullable LlmDataQuery> reading) {
            return storedQueries.computeIfAbsent(document, key -> Optional.ofNullable(reading.get())).orElse(null);
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
