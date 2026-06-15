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

import io.jmix.aitools.AiToolsDataLoadProperties;
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService.OperationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.accesscontext.LoadValuesAccessContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validates, repairs and executes a generated JPQL query, returning the fetched rows.
 */
@Component("aitls_JpqlExecutionService")
public class JpqlExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JpqlExecutionService.class);

    @Autowired
    protected JpqlValidationAndRepairService validateAndRepair;
    @Autowired
    protected JpqlParameterConversionService jpqlParameterConversionService;
    @Autowired
    protected AiToolsDataLoadProperties dataLoadProperties;
    @Autowired
    protected DataManager dataManager;
    @Autowired(required = false)
    protected AccessManager accessManager;
    @Autowired(required = false)
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired(required = false)
    protected Metadata metadata;

    /**
     * Validates, repairs if needed and executes the query described by the request.
     * <p>
     * This method runs the full pipeline: it validates and (if needed) repairs the
     * query, enforces data-access constraints, converts the parameters to their Java types and runs
     * the query through {@link DataManager#loadValues}. One extra row is fetched to detect whether
     * more results are available. Validation or execution failures are reported as a non-successful
     * {@link JpqlExecutionResult} rather than thrown.
     *
     * @param request query to execute together with its parameters and paging hints
     * @return result with the fetched rows on success, or with validation/execution failure details
     */
    public JpqlExecutionResult execute(JpqlExecutionRequest request) {
        Preconditions.checkNotNullArgument(request, "request is null");

        OperationResult vrResult = validateAndRepair.validateAndRepair(request);
        GeneratedJpqlResult generatedResult = vrResult.getGeneratedResult();
        JpqlValidationResult validationResult = vrResult.getValidationResult();
        if (vrResult.isFailed()) {
            return JpqlExecutionResult.failed(generatedResult, validationResult, false);
        }

        ensureQueryIsPermitted(generatedResult.getJpql());

        Map<String, Object> executionParameters =
                jpqlParameterConversionService.convert(toExecutionParameters(generatedResult));
        Integer effectiveMaxResults = getEffectiveMaxResult(generatedResult.getMaxResults());

        try {
            ExecutionRows executionRows = executeQuery(request, generatedResult, executionParameters,
                    effectiveMaxResults, generatedResult.getFirstResult());

            return new JpqlExecutionResult(generatedResult, validationResult, executionRows.rows(),
                    effectiveMaxResults, generatedResult.getFirstResult(),
                    executionRows.hasMore(),
                    vrResult.isRepaired(), true, null
            );
        } catch (RuntimeException e) {
            log.error("Cannot execute query", e);

            return JpqlExecutionResult.failed(generatedResult, validationResult, effectiveMaxResults,
                    vrResult.isRepaired(), e.getMessage());
        }
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

    protected void ensureQueryIsPermitted(String jpqlQuery) {
        if (accessManager == null || queryTransformerFactory == null || metadata == null) {
            return;
        }

        LoadValuesAccessContext queryContext = new LoadValuesAccessContext(jpqlQuery, queryTransformerFactory, metadata);
        accessManager.applyRegisteredConstraints(queryContext);

        if (!queryContext.isPermitted()) {
            String entityNames = queryContext.getEntityClasses().stream()
                    .map(MetadataObject::getName)
                    .sorted()
                    .collect(Collectors.joining(","));
            String deniedResource = entityNames.isBlank() ? jpqlQuery : entityNames;
            throw new AccessDeniedException("entity", deniedResource, EntityOp.READ.getId());
        }

        if (!queryContext.getDeniedSelectedIndexes().isEmpty()) {
            List<MetaPropertyPath> selectedPropertyPaths = new ArrayList<>(queryContext.getSelectedPropertyPaths());
            String deniedAttributes = queryContext.getDeniedSelectedIndexes().stream()
                    .distinct()
                    .sorted()
                    .map(index -> getSelectedPropertyPath(selectedPropertyPaths, index))
                    .map(this::toAttributeResource)
                    .collect(Collectors.joining(","));
            throw new AccessDeniedException("attribute", deniedAttributes);
        }
    }

    protected MetaPropertyPath getSelectedPropertyPath(List<MetaPropertyPath> selectedPropertyPaths, Integer index) {
        if (index < 0 || index >= selectedPropertyPaths.size()) {
            throw new IllegalStateException("Denied selected property index is out of bounds: " + index);
        }
        return selectedPropertyPaths.get(index);
    }

    protected String toAttributeResource(@Nullable MetaPropertyPath propertyPath) {
        if (propertyPath == null) {
            return "<unknown>";
        }
        return propertyPath.getMetaClass().getName() + "." + propertyPath.toPathString();
    }

    protected ExecutionRows executeQuery(JpqlExecutionRequest request,
                                         GeneratedJpqlResult generatedJpqlResult,
                                         Map<String, Object> executionParameters,
                                         Integer maxResults,
                                         @Nullable Integer firstResult) {
        FluentValuesLoader loader = dataManager
                .loadValues(generatedJpqlResult.getJpql())
                .properties(request.getResultProperties());
        executionParameters.forEach(loader::parameter);

        if (firstResult != null) {
            loader.firstResult(firstResult);
        }
        loader.maxResults(maxResults + 1);

        List<KeyValueEntity> loadedRows = loader.list();
        boolean hasMore = loadedRows.size() > maxResults;
        int rowCount = hasMore ? maxResults : loadedRows.size();

        List<Map<String, Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            KeyValueEntity entity = loadedRows.get(i);
            Map<String, Object> valueRow = toValueRow(entity, request.getResultProperties());
            rows.add(valueRow);
        }

        return createExecutionRows(List.copyOf(rows), hasMore);
    }

    protected Map<String, Object> toValueRow(KeyValueEntity keyValueEntity, List<String> resultProperties) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String property : resultProperties) {
            Object value = keyValueEntity.getValue(property);
            row.put(property, value == null ? "" : value);
        }
        return Map.copyOf(row);
    }

    protected Integer getEffectiveMaxResult(@Nullable Integer maxResults) {
        if (maxResults == null) {
            return dataLoadProperties.getJpqlExecutionMaxResult();
        }
        return maxResults;
    }

    protected ExecutionRows createExecutionRows(List<Map<String, Object>> rows, boolean hasMore) {
        return new ExecutionRows(rows, hasMore);
    }

    /**
     * Rows fetched for a query plus a flag telling whether more results are available beyond them.
     */
    protected record ExecutionRows(List<Map<String, Object>> rows, boolean hasMore) {
    }
}
