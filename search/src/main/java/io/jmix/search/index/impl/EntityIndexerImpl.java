/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("search_EntityIndexer")
public class EntityIndexerImpl implements EntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(EntityIndexerImpl.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected IndexStateRegistry indexStateRegistry;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public IndexResult index(Object entityInstance) {
        return indexCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public IndexResult indexCollection(Collection<Object> entityInstances) {
        Map<IndexConfiguration, Collection<Object>> groupedInstances = prepareInstancesForIndexing(entityInstances);
        return indexGroupedInstances(groupedInstances);
    }

    @Override
    public IndexResult indexByEntityId(Id<?> entityId) {
        return indexCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public IndexResult indexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<Object>> groupedInstances = prepareInstancesForIndexingByIds(entityIds);
        return indexGroupedInstances(groupedInstances);
    }

    @Override
    public IndexResult delete(Object entityInstance) {
        return deleteCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public IndexResult deleteCollection(Collection<Object> entityInstances) {
        Map<IndexConfiguration, Collection<String>> groupedIndexIds = prepareIndexIdsByEntityInstances(entityInstances);
        return deleteByGroupedIndexIds(groupedIndexIds);
    }

    @Override
    public IndexResult deleteByEntityId(Id<?> entityId) {
        return deleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<String>> groupedIndexIds = prepareIndexIdsByEntityIds(entityIds);
        return deleteByGroupedIndexIds(groupedIndexIds);
    }

    protected IndexResult indexGroupedInstances(Map<IndexConfiguration, Collection<Object>> groupedInstancesForIndexing) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedInstancesForIndexing.values().stream()
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            log.debug("Prepared {} instances within {} entities", amountOfInstances, groupedInstancesForIndexing.keySet().size());
        }

        BulkRequest request = new BulkRequest();
        for (Map.Entry<IndexConfiguration, Collection<Object>> entry : groupedInstancesForIndexing.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();
            if (indexStateRegistry.isIndexAvailable(indexConfiguration.getEntityName())) {
                for (Object instance : entry.getValue()) {
                    addIndexActionToBulkRequest(request, indexConfiguration, instance);
                }
            }
        }

        try {
            BulkResponse bulkResponse = esClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("Bulk Response (Index): Took {}, Status = {}, With Failures = {}{}",
                    bulkResponse.getTook(), bulkResponse.status(), bulkResponse.hasFailures(),
                    bulkResponse.hasFailures() ? ": " + bulkResponse.buildFailureMessage() : "");
            return IndexResult.create(bulkResponse);
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected Map<IndexConfiguration, Collection<Object>> prepareInstancesForIndexing(Collection<Object> instances) {
        Map<MetaClass, List<Object>> idsGroupedByMetaClass = instances.stream().collect(
                Collectors.groupingBy(
                        instance -> metadata.getClass(instance),
                        Collectors.mapping(EntityValues::getId, Collectors.toList())
                )
        );

        return reloadEntityInstances(idsGroupedByMetaClass);
    }

    protected Map<IndexConfiguration, Collection<Object>> prepareInstancesForIndexingByIds(Collection<Id<?>> entityIds) {
        Map<MetaClass, List<Object>> idsGroupedByMetaClass = entityIds.stream().collect(
                Collectors.groupingBy(
                        id -> metadata.getClass(id.getEntityClass()),
                        Collectors.mapping(Id::getValue, Collectors.toList())
                )
        );

        return reloadEntityInstances(idsGroupedByMetaClass);
    }

    protected Map<IndexConfiguration, Collection<Object>> reloadEntityInstances(Map<MetaClass, List<Object>> idsGroupedByMetaClass) {
        Map<IndexConfiguration, FetchPlan> fetchPlanLocalCache = new HashMap<>();
        Map<IndexConfiguration, Collection<Object>> result = new HashMap<>();
        idsGroupedByMetaClass.forEach((metaClass, entityIds) -> {
            Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName());
            if (indexConfigurationOpt.isPresent()) {
                IndexConfiguration indexConfiguration = indexConfigurationOpt.get();
                FetchPlan fetchPlan = fetchPlanLocalCache.computeIfAbsent(indexConfiguration, this::createFetchPlan);
                List<Object> loaded = dataManager.load(metaClass.getJavaClass())
                        .ids(entityIds)
                        .fetchPlan(fetchPlan)
                        .list();
                result.put(indexConfiguration, loaded);
            }
        });
        return result;
    }

    protected FetchPlan createFetchPlan(IndexConfiguration indexConfiguration) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(indexConfiguration.getEntityClass());
        indexConfiguration.getMapping().getFields().values().forEach(field -> {
            log.trace("Add property to fetch plan: {}", field.getEntityPropertyFullName());
            fetchPlanBuilder.add(field.getEntityPropertyFullName());
            field.getInstanceNameRelatedProperties().forEach(instanceNameRelatedProperty -> {
                log.trace("Add instance name related property to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
            });
        });

        indexConfiguration.getMapping()
                .getDisplayedNameDescriptor()
                .getInstanceNameRelatedProperties()
                .forEach(instanceNameRelatedProperty -> {
                    log.trace("Add instance name related property (displayed name) to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                    fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
                });

        return fetchPlanBuilder.build();
    }

    protected void addIndexActionToBulkRequest(BulkRequest request,
                                               IndexConfiguration indexConfiguration,
                                               Object instance) {
        ObjectNode sourceObject = JsonNodeFactory.instance.objectNode();
        IndexMappingConfiguration indexMappingConfiguration = indexConfiguration.getMapping();
        indexMappingConfiguration.getFields()
                .values()
                .stream()
                .filter(field -> !field.isStandalone())
                .forEach(field -> addFieldValueToEntityIndexContent(sourceObject, field, instance));

        DisplayedNameDescriptor displayedNameDescriptor = indexMappingConfiguration.getDisplayedNameDescriptor();
        JsonNode displayedName = displayedNameDescriptor.getValue(instance);
        sourceObject.set(displayedNameDescriptor.getIndexPropertyFullName(), displayedName);

        log.debug("Source object: {}", sourceObject);
        try {
            String serializedEntityId = idSerialization.idToString(Id.of(instance));
            request.add(new IndexRequest()
                    .index(indexConfiguration.getIndexName())
                    .id(serializedEntityId)
                    .source(objectMapper.writeValueAsString(sourceObject), XContentType.JSON));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create index request: unable to parse source object", e);
        }
    }

    protected Map<IndexConfiguration, Collection<String>> prepareIndexIdsByEntityInstances(Collection<Object> instances) {
        Map<IndexConfiguration, Collection<String>> result = new HashMap<>();
        instances.forEach(instance -> {
            MetaClass metaClass = metadata.getClass(instance);
            Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName());
            if (indexConfigurationOpt.isPresent()) {
                IndexConfiguration indexConfiguration = indexConfigurationOpt.get();
                String indexId = idSerialization.idToString(Id.of(instance));
                Collection<String> idsForConfig = result.computeIfAbsent(indexConfiguration, k -> new HashSet<>());
                idsForConfig.add(indexId);
            }
        });
        return result;
    }

    protected Map<IndexConfiguration, Collection<String>> prepareIndexIdsByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<String>> result = new HashMap<>();
        entityIds.forEach(entityId -> {
            MetaClass metaClass = metadata.getClass(entityId.getEntityClass());
            Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName());
            if (indexConfigurationOpt.isPresent()) {
                IndexConfiguration indexConfiguration = indexConfigurationOpt.get();
                String indexId = idSerialization.idToString(entityId);
                Collection<String> idsForConfig = result.computeIfAbsent(indexConfiguration, k -> new HashSet<>());
                idsForConfig.add(indexId);
            }
        });
        return result;
    }

    protected IndexResult deleteByGroupedIndexIds(Map<IndexConfiguration, Collection<String>> groupedIndexIds) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedIndexIds.values().stream().map(Collection::size).reduce(Integer::sum).orElse(0);
            log.debug("Prepared {} instances within {} entities", amountOfInstances, groupedIndexIds.keySet().size());
        }

        BulkRequest request = new BulkRequest();
        for (Map.Entry<IndexConfiguration, Collection<String>> entry : groupedIndexIds.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();
            for (String indexId : entry.getValue()) {
                addDeleteActionToBulkRequest(request, indexConfiguration, indexId);
            }
        }

        try {
            BulkResponse bulkResponse = esClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("Bulk Response (Delete): Took {}, Status = {}, With Failures = {}{}",
                    bulkResponse.getTook(), bulkResponse.status(), bulkResponse.hasFailures(),
                    bulkResponse.hasFailures() ? ": " + bulkResponse.buildFailureMessage() : "");
            return IndexResult.create(bulkResponse);
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected void addDeleteActionToBulkRequest(BulkRequest request,
                                                IndexConfiguration indexConfiguration,
                                                String indexId) {
        request.add(new DeleteRequest(indexConfiguration.getIndexName(), indexId));
    }

    protected ObjectNode createObjectNodeForField(String key, JsonNode value) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        String[] fields = key.split("\\.");
        ObjectNode currentRoot = root;
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (i == fields.length - 1) {
                currentRoot.set(field, value);
            } else {
                currentRoot = currentRoot.putObject(field);
            }
        }
        return root;
    }

    protected void addFieldValueToEntityIndexContent(ObjectNode entityIndexContent, MappingFieldDescriptor field, Object entity) {
        log.trace("Extract value of property '{}' from entity {}", field.getMetaPropertyPath(), entity);
        JsonNode propertyValue = field.getValue(entity);
        if (!propertyValue.isNull()) {
            String indexPropertyFullName = field.getIndexPropertyFullName();
            ObjectNode objectNodeForField = createObjectNodeForField(indexPropertyFullName, propertyValue);
            log.trace("Field value tree: {}", objectNodeForField);
            merge(objectNodeForField, entityIndexContent);
        }
    }

    //todo move to tools?
    private void merge(JsonNode toBeMerged, JsonNode mergedInTo) {
        log.trace("Merge object {} into {}", toBeMerged, mergedInTo);
        Iterator<Map.Entry<String, JsonNode>> incomingFieldsIterator = toBeMerged.fields();
        Iterator<Map.Entry<String, JsonNode>> mergedIterator;

        while (incomingFieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> incomingEntry = incomingFieldsIterator.next();

            JsonNode subNode = incomingEntry.getValue();

            if (subNode.getNodeType().equals(JsonNodeType.OBJECT)) {
                boolean isNewBlock = true;
                mergedIterator = mergedInTo.fields();
                while (mergedIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = mergedIterator.next();
                    if (entry.getKey().equals(incomingEntry.getKey())) {
                        merge(incomingEntry.getValue(), entry.getValue());
                        isNewBlock = false;
                    }
                }
                if (isNewBlock) {
                    ((ObjectNode) mergedInTo).replace(incomingEntry.getKey(), incomingEntry.getValue());
                }
            } else if (subNode.getNodeType().equals(JsonNodeType.ARRAY)) {
                boolean newEntry = true;
                mergedIterator = mergedInTo.fields();
                while (mergedIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = mergedIterator.next();
                    if (entry.getKey().equals(incomingEntry.getKey())) {
                        updateArray(incomingEntry.getValue(), entry);
                        newEntry = false;
                    }
                }
                if (newEntry) {
                    ((ObjectNode) mergedInTo).replace(incomingEntry.getKey(), incomingEntry.getValue());
                }
            }
            ValueNode valueNode = null;
            JsonNode incomingValueNode = incomingEntry.getValue();
            switch (subNode.getNodeType()) {
                case STRING:
                    valueNode = new TextNode(incomingValueNode.textValue());
                    break;
                case NUMBER:
                    valueNode = new IntNode(incomingValueNode.intValue());
                    break;
                case BOOLEAN:
                    valueNode = BooleanNode.valueOf(incomingValueNode.booleanValue());
            }
            if (valueNode != null) {
                updateObject(mergedInTo, valueNode, incomingEntry);
            }
        }
    }

    private void updateArray(JsonNode valueToBePlaced, Map.Entry<String, JsonNode> toBeMerged) {
        toBeMerged.setValue(valueToBePlaced);
    }

    private void updateObject(JsonNode mergeInTo, ValueNode valueToBePlaced,
                              Map.Entry<String, JsonNode> toBeMerged) {
        boolean newEntry = true;
        Iterator<Map.Entry<String, JsonNode>> mergedIterator = mergeInTo.fields();
        while (mergedIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = mergedIterator.next();
            if (entry.getKey().equals(toBeMerged.getKey())) {
                newEntry = false;
                entry.setValue(valueToBePlaced);
            }
        }
        if (newEntry) {
            ((ObjectNode) mergeInTo).replace(toBeMerged.getKey(), toBeMerged.getValue());
        }
    }
}
