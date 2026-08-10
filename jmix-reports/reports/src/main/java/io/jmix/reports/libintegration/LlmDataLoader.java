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

import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.LlmQueryParameterNames;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads band data for the {@link DataSetType#LLM} data set type: executes the query
 * stored in the data set, or generates one first when the data set says so.
 * <p>
 * A generation performed here is not stored back into the data set — a report run must not modify the
 * report it runs. Storing a generated query is the report designer's job.
 */
@NullMarked
public class LlmDataLoader implements ReportDataLoader {

    private static final Logger log = LoggerFactory.getLogger(LlmDataLoader.class);

    @Autowired
    protected LlmDataQueryService llmDataQueryService;

    @Autowired
    protected LlmDataQuerySerializer llmDataQuerySerializer;

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, @Nullable BandData parentBand,
                                              Map<String, Object> params) {
        String prompt = reportQuery.getScript();
        if (StringUtils.isBlank(prompt)) {
            throw new DataLoadingException(
                    String.format("A prompt is required for data set [%s]", reportQuery.getName()));
        }

        Map<String, Object> additionalParams = reportQuery.getAdditionalParams();
        Integer maxResults = (Integer) additionalParams.get(DataSet.LLM_MAX_RESULTS);
        Map<String, LlmQueryParameter> availableParameters = collectAvailableParameters(params, parentBand);

        try {
            LlmDataQuery query = resolveQuery(reportQuery, prompt, additionalParams, maxResults, availableParameters);
            checkCrossTabAxesAreLinkable(reportQuery, query, params, availableParameters);

            log.debug("Executing the query of data set [{}]: {}", reportQuery.getName(), query.getJpql());
            return llmDataQueryService.execute(new LlmQueryExecutionRequest(prompt, query,
                    resolveArguments(reportQuery, query, availableParameters), maxResults));
        } catch (LlmDataQueryException e) {
            throw new DataLoadingException(
                    String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }

    protected LlmDataQuery resolveQuery(ReportQuery reportQuery, String prompt,
                                        Map<String, Object> additionalParams, @Nullable Integer maxResults,
                                        Map<String, LlmQueryParameter> availableParameters) {
        boolean regenerateOnRun = Boolean.TRUE.equals(additionalParams.get(DataSet.LLM_REGENERATE_ON_RUN));
        LlmDataQuery storedQuery = regenerateOnRun
                ? null
                : llmDataQuerySerializer.fromJson((String) additionalParams.get(DataSet.LLM_GENERATED_QUERY));
        if (storedQuery != null) {
            return storedQuery;
        }

        if (!regenerateOnRun) {
            log.warn("Data set [{}] has no generated query stored, so it is generated for this run; "
                    + "generate and review it in the report designer to make runs reproducible",
                    reportQuery.getName());
        }

        LlmDataQuery generatedQuery = llmDataQueryService.generate(new LlmQueryGenerationRequest(prompt,
                List.copyOf(availableParameters.values()), maxResults));
        if (!generatedQuery.getWarnings().isEmpty()) {
            log.warn("The query generated for data set [{}] comes with warnings: {}",
                    reportQuery.getName(), generatedQuery.getWarnings());
        }

        return generatedQuery;
    }

    /**
     * Collects the parameters the query may reference, keyed by name: the run parameters first, then the fields
     * of every parent row up the band hierarchy, then the values of the cross-tab axes this band is built from.
     * A parameter with no value is left out — its type is unknown, and the add-on binds no value for it, which
     * would fail the query anyway.
     */
    protected Map<String, LlmQueryParameter> collectAvailableParameters(Map<String, Object> params,
                                                                       @Nullable BandData parentBand) {
        Map<String, LlmQueryParameter> availableParameters = new LinkedHashMap<>();
        Map<String, List<?>> crossTabAxes = new LinkedHashMap<>();

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

            availableParameters.put(param.getKey(),
                    new LlmQueryParameter(param.getKey(), value.getClass().getName(), value));
        }

        for (BandData band = parentBand; band != null; band = band.getParentBand()) {
            addParentBandFields(band, availableParameters);
        }

        crossTabAxes.forEach((dataSetName, rows) -> addCrossTabAxisValues(dataSetName, rows, availableParameters));

        return availableParameters;
    }

    /**
     * Offers the fields of one parent row under names flattened to {@code <band>_<field>}, because a JPQL
     * parameter name cannot contain the dot that SQL and JPQL data sets use in {@code ${Band.field}}.
     * <p>
     * A name already taken is kept as it is: a run parameter outranks a band field, and a nearer parent
     * outranks a more distant one. Skipping a field whose flattened name is not an identifier is deliberate —
     * sanitizing the name would let two different bands collapse onto one parameter.
     */
    protected void addParentBandFields(BandData band, Map<String, LlmQueryParameter> availableParameters) {
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

            LlmQueryParameter present = availableParameters.putIfAbsent(name,
                    new LlmQueryParameter(name, value.getClass().getName(), value));
            if (present != null) {
                log.warn("Parameter [{}] is already available, so the field [{}] of band [{}] is not offered "
                        + "to the query; rename one of them to make both usable",
                        name, field.getKey(), band.getName());
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
                                                Map<String, LlmQueryParameter> availableParameters) {
        for (String name : params.keySet()) {
            if (!LlmQueryParameterNames.isCrossTabAxis(name) || !isAxisRows(params.get(name))) {
                continue;
            }

            String prefix = name + "_";
            // An axis that produced no value has no columns either, so there is nothing to link a cell to.
            if (availableParameters.keySet().stream().noneMatch(parameter -> parameter.startsWith(prefix))) {
                continue;
            }

            boolean linkable = query.getResultProperties().stream().anyMatch(property -> property.startsWith(prefix));
            if (!linkable) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns no column named [%s<field>], so its rows cannot be "
                                + "linked to the cross-tab axis [%s]; it returns %s",
                        reportQuery.getName(), prefix, name, query.getResultProperties()));
            }
        }
    }

    /**
     * Tells the rows of a cross-tab axis from an ordinary parameter that merely happens to be named like one:
     * an axis holds rows, so it is a list of maps, and an empty list is the axis that produced nothing. A list
     * of anything else belongs to the report run and stays a parameter of its own.
     */
    protected boolean isAxisRows(Object value) {
        return value instanceof List<?> rows && (rows.isEmpty() || rows.get(0) instanceof Map);
    }

    /**
     * Offers the values of one cross-tab axis, one parameter per field of its rows, so that a cell query can
     * narrow itself to the columns and rows the matrix actually has. The value is the whole list — the add-on
     * converts a collection element by element — and the type is the element's, not the list's.
     * <p>
     * A field contributes nothing when every row leaves it empty, and the same naming rules as for band fields
     * apply: a name that is not an identifier is skipped, and a name already taken is kept.
     */
    protected void addCrossTabAxisValues(String dataSetName, List<?> rows,
                                         Map<String, LlmQueryParameter> availableParameters) {
        Map<String, List<Object>> valuesByField = new LinkedHashMap<>();
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> fields)) {
                continue;
            }

            for (Map.Entry<?, ?> field : fields.entrySet()) {
                Object value = field.getValue();
                if (value == null) {
                    continue;
                }

                valuesByField.computeIfAbsent(String.valueOf(field.getKey()), name -> new ArrayList<>())
                        .add(value);
            }
        }

        for (Map.Entry<String, List<Object>> field : valuesByField.entrySet()) {
            String name = LlmQueryParameterNames.ofCrossTabValue(dataSetName, field.getKey());
            if (!LlmQueryParameterNames.isValid(name)) {
                continue;
            }

            List<Object> values = field.getValue();
            LlmQueryParameter present = availableParameters.putIfAbsent(name,
                    new LlmQueryParameter(name, values.get(0).getClass().getName(), values, true));
            if (present != null) {
                log.warn("Parameter [{}] is already available, so the field [{}] of the cross-tab axis [{}] is "
                        + "not offered to the query; rename one of them to make both usable",
                        name, field.getKey(), dataSetName);
            }
        }
    }

    /**
     * Builds one argument per parameter the query references. The type comes from the report parameter, not
     * from the query's own declaration: the add-on coerces the value to the declared type, so a type the
     * model guessed wrong would corrupt the value.
     */
    protected List<LlmQueryParameter> resolveArguments(ReportQuery reportQuery, LlmDataQuery query,
                                                       Map<String, LlmQueryParameter> availableParameters) {
        List<LlmQueryParameter> arguments = new ArrayList<>(query.getParameters().size());

        for (LlmQueryParameter parameter : query.getParameters()) {
            LlmQueryParameter available = availableParameters.get(parameter.getName());
            if (available == null) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] references parameter [%s], but the report run provides no "
                                + "value for it", reportQuery.getName(), parameter.getName()));
            }

            arguments.add(available);
        }

        return arguments;
    }
}
