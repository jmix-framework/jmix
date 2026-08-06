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
import java.util.regex.Pattern;

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

    /**
     * JPQL parameter names are identifiers, so a report parameter whose alias is not one cannot be offered
     * to query generation.
     */
    protected static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

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
        Map<String, LlmQueryParameter> availableParameters = collectAvailableParameters(params);

        try {
            LlmDataQuery query = resolveQuery(reportQuery, prompt, additionalParams, maxResults, availableParameters);

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
     * Collects the parameters the query may reference, keyed by name. A parameter with no value is left out:
     * its type is unknown, and the add-on binds no value for it, which would fail the query anyway.
     */
    protected Map<String, LlmQueryParameter> collectAvailableParameters(Map<String, Object> params) {
        Map<String, LlmQueryParameter> availableParameters = new LinkedHashMap<>();

        for (Map.Entry<String, Object> param : params.entrySet()) {
            Object value = param.getValue();
            // A report parameter left unfilled arrives as a null value, whatever the map's declared type says.
            //noinspection ConstantValue
            if (value == null || !PARAMETER_NAME_PATTERN.matcher(param.getKey()).matches()) {
                continue;
            }

            availableParameters.put(param.getKey(),
                    new LlmQueryParameter(param.getKey(), value.getClass().getName(), value));
        }

        return availableParameters;
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
