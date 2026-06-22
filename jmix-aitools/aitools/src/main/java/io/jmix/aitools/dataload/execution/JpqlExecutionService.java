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
     * more results are available.
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

        List<Integer> deniedSelectedIndexes = resolveDeniedSelectedIndexes(generatedResult.getJpql());
        List<String> retainedProperties = retainPermittedProperties(request.getResultProperties(), deniedSelectedIndexes);
        Integer effectiveMaxResults = getEffectiveMaxResult(generatedResult.getMaxResults());

        if (!request.getResultProperties().isEmpty() && retainedProperties.isEmpty()) {
            // Every selected column is inaccessible to the current user: there is nothing to return,
            // so skip execution and report an empty, non-executed result instead of failing.
            return new JpqlExecutionResult(generatedResult, validationResult, List.of(),
                    effectiveMaxResults, generatedResult.getFirstResult(), false,
                    vrResult.isRepaired(), false, null);
        }

        Map<String, Object> executionParameters =
                jpqlParameterConversionService.convert(toExecutionParameters(generatedResult));

        try {
            ExecutionRows executionRows = executeQuery(request, generatedResult, executionParameters,
                    effectiveMaxResults, generatedResult.getFirstResult());

            List<Map<String, Object>> rows = retainProperties(executionRows.rows(),
                    request.getResultProperties(), retainedProperties);

            return new JpqlExecutionResult(generatedResult, validationResult, rows,
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

    /**
     * Resolves the positions of the selected columns the current user is not allowed to read.
     *
     * @param jpqlQuery query whose data access is being checked
     * @return positions (in select-clause order) of the denied columns, or an empty list if access
     * checking is unavailable or all selected columns are readable
     * @throws AccessDeniedException if the current user cannot read the queried entity
     */
    protected List<Integer> resolveDeniedSelectedIndexes(String jpqlQuery) {
        if (accessManager == null || queryTransformerFactory == null || metadata == null) {
            return List.of();
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

        return queryContext.getDeniedSelectedIndexes().stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns the result properties that stay readable, dropping the ones at the denied select
     * positions so the inaccessible columns are omitted from the result.
     *
     * @param resultProperties      result property names in select-clause order
     * @param deniedSelectedIndexes positions of the denied columns
     * @return the retained property names, in their original order
     */
    protected List<String> retainPermittedProperties(List<String> resultProperties,
                                                     List<Integer> deniedSelectedIndexes) {
        if (deniedSelectedIndexes.isEmpty()) {
            return resultProperties;
        }

        List<String> retained = new ArrayList<>(resultProperties.size());
        for (int i = 0; i < resultProperties.size(); i++) {
            if (!deniedSelectedIndexes.contains(i)) {
                retained.add(resultProperties.get(i));
            }
        }
        return List.copyOf(retained);
    }

    /**
     * Rebuilds each row keeping only the retained properties, in their original order.
     *
     * @param rows               fetched rows keyed by all result properties
     * @param resultProperties   all result property names the rows are keyed by
     * @param retainedProperties property names to keep in the output rows
     * @return rows containing only the retained properties, or the original rows if nothing is dropped
     */
    protected List<Map<String, Object>> retainProperties(List<Map<String, Object>> rows,
                                                         List<String> resultProperties,
                                                         List<String> retainedProperties) {
        if (retainedProperties.size() == resultProperties.size()) {
            return rows;
        }

        List<Map<String, Object>> retainedRows = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> retainedRow = new LinkedHashMap<>();
            for (String property : retainedProperties) {
                retainedRow.put(property, row.getOrDefault(property, ""));
            }
            retainedRows.add(Map.copyOf(retainedRow));
        }
        return List.copyOf(retainedRows);
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
        int limit = dataLoadProperties.getJpqlExecutionMaxResultLimit();
        int requested = maxResults != null ? maxResults : dataLoadProperties.getJpqlExecutionMaxResult();
        if (requested > limit) {
            log.debug("Requested maxResults {} exceeds the configured limit {}; capping to the limit",
                    requested, limit);
            return limit;
        }
        return requested;
    }

    protected ExecutionRows createExecutionRows(List<Map<String, Object>> rows, boolean hasMore) {
        return new ExecutionRows(rows, hasMore);
    }

    /**
     * Rows fetched for a query plus a flag telling whether more results are available beyond them.
     *
     * @param rows    the fetched rows
     * @param hasMore {@code true} if more rows are available beyond {@code rows}
     */
    protected record ExecutionRows(List<Map<String, Object>> rows, boolean hasMore) {
    }
}
