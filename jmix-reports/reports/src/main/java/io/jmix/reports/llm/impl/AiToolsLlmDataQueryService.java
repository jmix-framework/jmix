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

package io.jmix.reports.llm.impl;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implements the Reports-side seam on top of the AI Tools data-load subsystem: generation produces the
 * query, and execution runs it through {@code DataManager}, which is what applies the current user's data
 * access constraints.
 * <p>
 * The only class in Reports that depends on the AI Tools add-on, and therefore the only one that must not
 * be loaded when the add-on is absent — its bean is declared by a conditional auto-configuration.
 */
public class AiToolsLlmDataQueryService implements LlmDataQueryService {

    @Autowired
    protected EntityDataLoadGenerationService entityDataLoadGenerationService;

    @Autowired
    protected JpqlExecutionService jpqlExecutionService;

    @Autowired
    protected ChatClientFactory chatClientFactory;

    /**
     * The add-on's data-load beans are declared on properties alone, so they are there even when no model is
     * configured for the application. Generation then fails on the first call, which the designer has no reason
     * to find out by trying.
     */
    @Override
    public boolean isGenerationAvailable() {
        return chatClientFactory.isConfigured();
    }

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        EntityDataLoadQuery generatedQuery;
        try {
            generatedQuery = entityDataLoadGenerationService.generate(composeUserText(request));
        } catch (RuntimeException e) {
            throw new LlmDataQueryException("Cannot generate a query for the data set prompt", e);
        }

        if (StringUtils.isBlank(generatedQuery.getJpql())) {
            throw new LlmDataQueryException("Query generation produced no query text");
        }

        // A model answers with the lists it likes, and nothing between here and the model rejects a null
        // element in them, so they are cleaned before the query is built out of them.
        return new LlmDataQuery(generatedQuery.getJpql(), retainNonNull(generatedQuery.getResultProperties()),
                toQueryParameters(generatedQuery.getParameters()), generatedQuery.getExplanation(),
                retainNonNull(generatedQuery.getWarnings()), generatedQuery.getMaxResults());
    }

    @Override
    public List<Map<String, @Nullable Object>> execute(LlmQueryExecutionRequest request) {
        LlmDataQuery query = request.getQuery();
        Integer maxResults = request.getMaxResults() != null ? request.getMaxResults() : query.getMaxResults();

        JpqlExecutionResult result;
        try {
            result = jpqlExecutionService.execute(new JpqlExecutionRequest(
                    request.getPrompt(),
                    query.getJpql(),
                    toExecutionParameters(request.getArguments()),
                    query.getResultProperties(),
                    maxResults,
                    null));
        } catch (AccessDeniedException e) {
            // Being refused the data is not a failure of this seam: the caller reports it as what it is.
            throw e;
        } catch (RuntimeException e) {
            // The add-on validates, repairs and converts before its own error handling starts, so a failure
            // there would otherwise leave the seam as an exception of an unrelated kind.
            throw new LlmDataQueryException("Cannot execute the query of the data set", e);
        }

        if (!result.isExecuted()) {
            throw new LlmDataQueryException(describeFailure(result));
        }

        return result.getRows();
    }

    protected <T> List<T> retainNonNull(@Nullable List<T> values) {
        //noinspection ConstantValue
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Appends the parameter contract to the prompt. It goes into the user text rather than into a prompt
     * provider, because the data-load system prompt is a single bean shared by every consumer of generation.
     * <p>
     * Only the referenced parameters may be declared: the add-on's {@code ParametersValidator} reports a
     * declared parameter the query never uses as an issue.
     */
    protected String composeUserText(LlmQueryGenerationRequest request) {
        StringBuilder userText = new StringBuilder(request.getPrompt());

        if (!request.getAvailableParameters().isEmpty()) {
            userText.append("\n\nAVAILABLE REPORT PARAMETERS:");
            for (LlmQueryParameter parameter : request.getAvailableParameters()) {
                userText.append("\n- :").append(parameter.getName())
                        .append(" (").append(parameter.getJavaType());
                // A collection parameter is bound as a whole, so the query has to match it with IN.
                if (parameter.isMultiValued()) {
                    userText.append(", several values of this type, matched with IN and no parentheses "
                            + "around the parameter name");
                }
                userText.append(')');
            }
            userText.append("\n\nPARAMETER RULES:")
                    .append("\n- Reference these as JPQL named parameters, never inline their values.")
                    .append("\n- Declare in \"parameters\" only the ones the query actually references.")
                    .append("\n- Use a parameter only where the request calls for it; ignore the rest.");
        }

        appendCrossTabRules(userText, request);

        if (request.getMaxResults() != null) {
            userText.append("\n\nROW LIMIT: return at most ").append(request.getMaxResults()).append(" rows.");
        }

        return userText.toString();
    }

    /**
     * States what a cross-tab cell query must return. Every required axis value has to come back as a result
     * column of the same name — that is how the extraction controller places a row in the matrix. Stated as an
     * explicit list of required aliases rather than inferred from multi-valued parameters: an ordinary report
     * parameter may be a collection too, while told only a generic rule, models alias axis columns after their
     * attributes instead (`username`, `active`) and the report then renders an empty matrix.
     */
    protected void appendCrossTabRules(StringBuilder userText, LlmQueryGenerationRequest request) {
        List<String> axisNames = request.getRequiredResultProperties();
        if (axisNames.isEmpty()) {
            return;
        }

        userText.append("\n\nREQUIRED RESULT COLUMNS: this band is a cross-tab, so the query MUST select and "
                + "alias one column per name below, holding the value of that row for it:");
        for (String axisName : axisNames) {
            userText.append("\n- ").append(axisName);
        }
        userText.append("\nUse these exact aliases in addition to the value columns the request asks for.");

        boolean hasMultiValuedAxisParameter = request.getAvailableParameters().stream()
                .filter(LlmQueryParameter::isMultiValued)
                .map(LlmQueryParameter::getName)
                .anyMatch(axisNames::contains);
        if (hasMultiValuedAxisParameter) {
            userText.append(" When a required column is also listed as a multi-valued available parameter, narrow "
                    + "the query to the matrix by matching that parameter with IN, and write no parentheses around "
                    + "the parameter name, because parentheses make JPQL expect a single value.");
        }
    }

    /**
     * Keeps the names and types of the generated parameters and drops their values: the values a model
     * invents belong to the request it was generated for, while the query is stored and reused.
     */
    protected List<LlmQueryParameter> toQueryParameters(List<GeneratedJpqlParameter> generatedParameters) {
        List<LlmQueryParameter> parameters = new ArrayList<>(generatedParameters.size());
        for (GeneratedJpqlParameter generatedParameter : generatedParameters) {
            parameters.add(new LlmQueryParameter(generatedParameter.getName(),
                    StringUtils.defaultString(generatedParameter.getType()), null));
        }
        return parameters;
    }

    protected List<JpqlExecutionParameter> toExecutionParameters(List<LlmQueryParameter> arguments) {
        List<JpqlExecutionParameter> parameters = new ArrayList<>(arguments.size());
        for (LlmQueryParameter argument : arguments) {
            parameters.add(new JpqlExecutionParameter(argument.getName(), argument.getJavaType(),
                    argument.getValue()));
        }
        return parameters;
    }

    protected String describeFailure(JpqlExecutionResult result) {
        String executionError = result.getExecutionError();
        if (StringUtils.isNotBlank(executionError)) {
            return "Cannot execute the query: " + executionError;
        }

        JpqlValidationResult validationResult = result.getValidationResult();
        if (!validationResult.isValid()) {
            return "The query was rejected as invalid: " + validationResult.getIssues().stream()
                    .map(JpqlValidationIssue::getMessage)
                    .collect(Collectors.joining("; "));
        }

        return "The query was not executed";
    }
}
