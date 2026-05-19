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

package io.jmix.aitools.dataload.execution;

import io.jmix.aitools.dataload.generation.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("aitols_JpqlExecutionService")
public class JpqlExecutionService {

    // TODO: pinyazhin, app property?
    protected static final int DEFAULT_MAX_RESULTS = 50;

    @Autowired
    protected JpqlValidationService jpqlValidationService;
    @Autowired
    protected JpqlRepairService jpqlRepairService;
    @Autowired
    protected JpqlParameterConversionService jpqlParameterConversionService;
    @Autowired
    protected DataManager dataManager;

    public JpqlExecutionResult execute(JpqlExecutionRequest request) {
        Preconditions.checkNotNullArgument(request, "request is null");

        // TODO: pinyazhin rework?
        if (request.getResultProperties().isEmpty()) {
            JpqlValidationResult validationResult = new JpqlValidationResult(false, List.of(
                    new JpqlValidationIssue("resultProperties.empty",
                            "resultProperties must be specified for loadValues execution")
            ));
            return JpqlExecutionResult.failed(toGeneratedJpqlResult(request), validationResult, false);
        }

        GeneratedJpqlResult initialGeneratedResult = toGeneratedJpqlResult(request);

        // Validate LLM generated JPQL
        JpqlValidationResult initialValidationResult = jpqlValidationService.validate(initialGeneratedResult);

        // Repair it if needed
        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(request, initialGeneratedResult, initialValidationResult);
        GeneratedJpqlResult generatedResult = repairResult.getGeneratedJpqlResult();

        // Final validation of repaired result
        JpqlValidationResult validationResult = jpqlValidationService.validate(generatedResult);

        if (!validationResult.isValid()) {
            return JpqlExecutionResult.failed(generatedResult, validationResult, repairResult.isRepaired());
        }

        Map<String, Object> executionParameters =
                jpqlParameterConversionService.convert(toExecutionParameters(generatedResult));

        Integer effectiveMaxResults = generatedResult.getMaxResults();
        if (effectiveMaxResults == null) {
            effectiveMaxResults = DEFAULT_MAX_RESULTS;
        }

        try {
            List<Map<String, Object>> rows = executeQuery(request, generatedResult, executionParameters,
                    effectiveMaxResults, generatedResult.getFirstResult());

            return new JpqlExecutionResult(generatedResult, validationResult, rows,
                    effectiveMaxResults, generatedResult.getFirstResult(),
                    repairResult.isRepaired(), true, null
            );
        } catch (RuntimeException e) {
            return JpqlExecutionResult.failed(generatedResult, validationResult, effectiveMaxResults,
                    repairResult.isRepaired(), e.getMessage());
        }
    }

    protected GeneratedJpqlResult toGeneratedJpqlResult(JpqlExecutionRequest request) {
        return new GeneratedJpqlResult(
                request.getJpql(),
                request.getRootEntityName(),
                toGeneratedParameters(request.getParameters()),
                request.getUsedEntities(),
                request.getUsedPropertyPaths(),
                "",
                List.of(),
                request.getMaxResults(),
                request.getFirstResult()
        );
    }

    protected List<GeneratedJpqlParameter> toGeneratedParameters(List<JpqlExecutionParameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return List.of();
        }

        List<GeneratedJpqlParameter> generatedParameters = new ArrayList<>(parameters.size());
        for (JpqlExecutionParameter parameter : parameters) {
            generatedParameters.add(
                    new GeneratedJpqlParameter(parameter.getName(), parameter.getType(), parameter.getValue()));
        }
        return List.copyOf(generatedParameters);
    }

    protected List<JpqlExecutionParameter> toExecutionParameters(GeneratedJpqlResult generatedJpqlResult) {
        if (generatedJpqlResult.getParameters().isEmpty()) {
            return List.of();
        }

        List<JpqlExecutionParameter> executionParameters = new ArrayList<>(generatedJpqlResult.getParameters().size());
        for (GeneratedJpqlParameter parameter : generatedJpqlResult.getParameters()) {
            executionParameters.add(
                    new JpqlExecutionParameter(parameter.getName(), parameter.getType(), parameter.getValue()));
        }
        return List.copyOf(executionParameters);
    }

    protected List<Map<String, Object>> executeQuery(JpqlExecutionRequest request,
                                                     GeneratedJpqlResult generatedJpqlResult,
                                                     Map<String, Object> executionParameters,
                                                     @Nullable Integer maxResults,
                                                     @Nullable Integer firstResult) {
        FluentValuesLoader loader = dataManager
                .loadValues(generatedJpqlResult.getJpql())
                .properties(request.getResultProperties());
        executionParameters.forEach(loader::parameter);

        if (firstResult != null) {
            loader.firstResult(firstResult);
        }
        if (maxResults != null) {
            loader.maxResults(maxResults);
        }

        return loader.list().stream()
                .map(keyValueEntity -> toValueRow(keyValueEntity, request.getResultProperties()))
                .toList();
    }

    protected Map<String, Object> toValueRow(KeyValueEntity keyValueEntity, List<String> resultProperties) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String property : resultProperties) {
            row.put(property, keyValueEntity.getValue(property));
        }
        return Map.copyOf(row);
    }
}
