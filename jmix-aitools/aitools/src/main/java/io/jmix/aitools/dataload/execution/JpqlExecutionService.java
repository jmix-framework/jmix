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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.aitools.dataload.generation.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.FluentLoader;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("aitols_JpqlExecutionService")
public class JpqlExecutionService {

    // TODO: pinyazhin, app property?
    protected static final int DEFAULT_MAX_RESULTS = 50;

    protected static final TypeReference<List<Map<String, Object>>> LIST_OF_MAPS_TYPE = new TypeReference<>() {
    };

    @Autowired
    protected JpqlValidationService jpqlValidationService;
    @Autowired
    protected JpqlRepairService jpqlRepairService;
    @Autowired
    protected JpqlParameterConversionService jpqlParameterConversionService;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntitySerialization entitySerialization;

    protected ObjectMapper objectMapper;

    public JpqlExecutionResult execute(JpqlExecutionRequest request) {
        Preconditions.checkNotNullArgument(request, "request is null");

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
            List<Map<String, Object>> rows = executeQuery(generatedResult, executionParameters,
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

    protected List<Map<String, Object>> executeQuery(GeneratedJpqlResult generatedJpqlResult,
                                                     Map<String, Object> executionParameters,
                                                     @Nullable Integer maxResults,
                                                     @Nullable Integer firstResult) {
        MetaClass metaClass = metadata.getClass(generatedJpqlResult.getRootEntityName());

        Class<Object> entityClass = metaClass.getJavaClass();

        FluentLoader.ByQuery<Object> loader = dataManager.load(entityClass).query(generatedJpqlResult.getJpql());
        executionParameters.forEach(loader::parameter);

        if (firstResult != null) {
            loader.firstResult(firstResult);
        }
        if (maxResults != null) {
            loader.maxResults(maxResults);
        }

        List<Object> entities = loader.list();

        return deserializeRows(entitySerialization.toJson(entities));
    }

    protected List<Map<String, Object>> deserializeRows(String rowsJson) {
        try {
            return getObjectMapper().readValue(rowsJson, LIST_OF_MAPS_TYPE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize query result rows", e);
        }
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : new ObjectMapper();
    }
}
