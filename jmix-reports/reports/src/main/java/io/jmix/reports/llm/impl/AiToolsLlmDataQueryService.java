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
import io.jmix.aitools.dataload.execution.*;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.reports.llm.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    protected JpqlValidationService jpqlValidationService;

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
                toQueryParameters(generatedQuery.getJpql(), generatedQuery.getParameters()),
                generatedQuery.getExplanation(), retainNonNull(generatedQuery.getWarnings()));
    }

    @Override
    public List<String> validate(LlmDataQuery query) {
        JpqlValidationResult validationResult = jpqlValidationService.validate(toGeneratedResult(query));
        if (validationResult.isValid()) {
            return List.of();
        }

        return validationResult.getIssues().stream()
                .map(JpqlValidationIssue::getMessage)
                .toList();
    }

    @Override
    public LlmQueryExecutionResult execute(LlmQueryExecutionRequest request) {
        LlmDataQuery query = request.getQuery();

        JpqlExecutionResult result;
        try {
            result = jpqlExecutionService.execute(new JpqlExecutionRequest(
                    request.getPrompt(),
                    query.getJpql(),
                    toExecutionParameters(request.getArguments()),
                    query.getResultProperties(),
                    null,
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

        return new LlmQueryExecutionResult(result.getRows(), result.isHasMore());
    }

    protected <T> List<T> retainNonNull(@Nullable List<T> values) {
        if (values == null) {
            return List.of();
        }

        //noinspection ConstantValue
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
                    userText.append(", several values of this type, matched with IN and no parentheses ")
                            .append("around the parameter name");
                }
                userText.append(')');
            }
            userText.append("\n\nPARAMETER RULES:")
                    .append("\n- Reference these as JPQL named parameters, never inline their values.")
                    .append("\n- Declare in \"parameters\" only the ones the query actually references.")
                    .append("\n- Use a parameter only where the request calls for it; ignore the rest.");
        }

        appendCrossTabRules(userText, request);

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

        userText.append("\n\nREQUIRED RESULT COLUMNS: this band is a cross-tab, so the query MUST select and ")
                .append("alias one column per name below, holding the value of that row for it:");
        for (String axisName : axisNames) {
            userText.append("\n- ").append(axisName);
        }
        userText.append("\nUse these exact aliases in addition to the value columns the request asks for.");

        boolean hasMultiValuedAxisParameter = request.getAvailableParameters().stream()
                .filter(LlmQueryParameter::isMultiValued)
                .map(LlmQueryParameter::getName)
                .anyMatch(axisNames::contains);
        if (hasMultiValuedAxisParameter) {
            userText.append(" When a required column is also listed as a multi-valued available parameter, narrow ")
                    .append("the query to the matrix by matching that parameter with IN, and write no parentheses ")
                    .append("around the parameter name, because parentheses make JPQL expect a single value.");
        }
    }

    /**
     * Declares the parameters the generated text references, and nothing else. The model's own list only
     * supplies the Java types: a list that says anything else than the text does is what the add-on rejects a
     * query for, and a query stored with such a list fails on every run instead of once here.
     * <p>
     * The values are dropped along the way: the values a model invents belong to the request it was generated
     * for, while the query is stored and reused.
     */
    protected List<LlmQueryParameter> toQueryParameters(String jpql,
                                                        List<GeneratedJpqlParameter> generatedParameters) {
        Map<String, String> declaredTypes = new LinkedHashMap<>();
        for (GeneratedJpqlParameter generatedParameter : generatedParameters) {
            if (StringUtils.isNotBlank(generatedParameter.getName())) {
                declaredTypes.put(generatedParameter.getName(),
                        StringUtils.defaultString(generatedParameter.getType()));
            }
        }

        List<LlmQueryParameter> parameters = new ArrayList<>();
        for (String name : LlmQueryParameterNames.referencedIn(jpql)) {
            parameters.add(new LlmQueryParameter(name, declaredTypes.getOrDefault(name, ""), null));
        }
        return parameters;
    }

    /**
     * Describes a query the way the add-on's validation reads one. The arguments are not part of it: a check
     * is about the query alone, while the values it is run with are decided per run.
     */
    protected GeneratedJpqlResult toGeneratedResult(LlmDataQuery query) {
        List<GeneratedJpqlParameter> parameters = new ArrayList<>(query.getParameters().size());
        for (LlmQueryParameter parameter : query.getParameters()) {
            parameters.add(new GeneratedJpqlParameter(parameter.getName(), parameter.getJavaType(), null));
        }

        return new GeneratedJpqlResult(query.getJpql(), parameters, StringUtils.defaultString(query.getExplanation()),
                query.getWarnings());
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

        return "The query was not executed: the current user may read none of the columns it selects";
    }
}
