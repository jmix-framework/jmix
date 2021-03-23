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

package io.jmix.search.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.search.index.mapping.IndexConfigurationProvider;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.index.queue.entity.QueueItem;
import io.jmix.search.utils.PropertyTools;
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

@Component(EntityIndexer.NAME)
public class EntityIndexerImpl implements EntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(EntityIndexerImpl.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected IndexConfigurationProvider indexDefinitionsProvider;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PropertyTools propertyTools;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void indexQueueItems(Collection<QueueItem> queueItems) {
        log.debug("Index Queue Items: {}", queueItems);
        Map<MetaClass, Map<EntityOp, Set<String>>> indexScope = new HashMap<>();
        queueItems.forEach(queueItem -> {
            MetaClass metaClass = metadata.getClass(queueItem.getEntityName());
            Map<EntityOp, Set<String>> changesByClass = indexScope.computeIfAbsent(metaClass, k -> new HashMap<>());
            Set<String> changesByChangeType = changesByClass.computeIfAbsent(EntityOp.fromId(queueItem.getChangeType()), k -> new HashSet<>());
            changesByChangeType.add(queueItem.getEntityId());
        });

        indexScope.forEach(
                (metaClass, changes) -> changes.forEach(
                        (entityChangeType, pks) -> indexEntitiesByPks(metaClass, pks, entityChangeType)
                )
        );
    }

    public void indexEntityByPk(MetaClass metaClass, String entityPk, EntityOp changeType) {
        indexEntitiesByPks(metaClass, Collections.singletonList(entityPk), changeType);
    }

    public void indexEntitiesByPks(MetaClass metaClass, Collection<String> entityPks, EntityOp changeType) {
        log.debug("Index entities: Class={}, Change Type={}, Pks={}", metaClass, changeType, entityPks);
        IndexConfiguration indexConfiguration = indexDefinitionsProvider.getIndexDefinitionByEntityName(metaClass.getName());
        if (indexConfiguration == null) {
            log.error("Index Definition not found for entity '{}'", metaClass);
            return;
        }
        log.debug("Mapping Fields for entity '{}': {}", metaClass, indexConfiguration.getMapping().getFields());

        if (EntityOp.UPDATE.equals(changeType) || EntityOp.CREATE.equals(changeType)) {
            indexDocuments(indexConfiguration, metaClass, entityPks);
        } else if (EntityOp.DELETE.equals(changeType)) {
            deleteDocuments(indexConfiguration, entityPks);
        } else {
            throw new UnsupportedOperationException("Entity Change Type '" + changeType + "' is not supported");
        }
    }

    protected void indexDocuments(IndexConfiguration indexConfiguration, MetaClass metaClass, Collection<String> entityPks) {
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
        List<Object> loaded = reloadEntities(indexConfiguration, metaClass, entityPks, primaryKeyPropertyName);
        log.debug("Loaded {} entities", loaded.size());

        BulkRequest request = new BulkRequest(indexConfiguration.getIndexName());
        loaded.forEach(entity -> addIndexActionToBulkRequest(request, indexConfiguration, metaClass, entity, primaryKeyPropertyName));

        try {
            BulkResponse bulkResponse = esClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("Bulk Response (Index): Took {}, Status = {}, With Failures = {}",
                    bulkResponse.getTook(), bulkResponse.status(), bulkResponse.hasFailures());
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected List<Object> reloadEntities(IndexConfiguration indexConfiguration,
                                          MetaClass metaClass,
                                          Collection<String> entityPks,
                                          String primaryKeyPropertyName) {
        FetchPlan fetchPlan = createFetchPlan(indexConfiguration);
        log.debug("Fetch plan for entity {}: {}", metaClass, fetchPlan.getProperties());
        return dataManager.load(metaClass.getJavaClass())
                .query(String.format("select e from %s e where e.%s in :ids", metaClass.getName(), primaryKeyPropertyName))
                .parameter("ids", entityPks)
                .fetchPlan(fetchPlan)
                .list();
    }

    protected void addIndexActionToBulkRequest(BulkRequest request,
                                               IndexConfiguration indexConfiguration,
                                               MetaClass metaClass,
                                               Object entity,
                                               String primaryKeyPropertyName) {
        ObjectNode entityIndexContent = JsonNodeFactory.instance.objectNode();
        indexConfiguration.getMapping().getFields().values().stream()
                .filter(field -> !field.isStandalone())
                .forEach(field -> addFieldValueToEntityIndexContent(entityIndexContent, field, entity));

        ObjectNode resultObject = createResultIndexDocument(metaClass, entityIndexContent);
        log.debug("Result object: {}", resultObject);
        try {
            Object primaryKey = EntityValues.getValue(entity, primaryKeyPropertyName);
            if (primaryKey == null) {
                log.error("Unable to create Index Request for '{}({})': Primary key not found", metaClass.getName(), EntityValues.getId(entity));
            } else {
                request.add(new IndexRequest()
                        .id(primaryKey.toString())
                        .source(objectMapper.writeValueAsString(resultObject), XContentType.JSON));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create index request: unable to parse source object", e);
        }
    }

    protected void deleteDocuments(IndexConfiguration indexConfiguration, Collection<String> entityPks) {
        BulkRequest request = new BulkRequest(indexConfiguration.getIndexName());
        entityPks.forEach(id -> request.add(new DeleteRequest().id(id)));

        try {
            BulkResponse bulkResponse = esClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("Bulk Response (Delete): Took {}, Status = {}, With Failures = {}",
                    bulkResponse.getTook(), bulkResponse.status(), bulkResponse.hasFailures());
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected FetchPlan createFetchPlan(IndexConfiguration indexConfiguration) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(indexConfiguration.getEntityClass());
        MetaClass metaClass = metadata.getClass(indexConfiguration.getEntityName());
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
        fetchPlanBuilder.add(primaryKeyPropertyName);
        indexConfiguration.getMapping().getFields().values().forEach(field -> {
            log.trace("Add property to fetch plan: {}", field.getEntityPropertyFullName());
            fetchPlanBuilder.add(field.getEntityPropertyFullName());
            field.getInstanceNameRelatedProperties().forEach(instanceNameRelatedProperty -> {
                log.trace("Add instance name related property to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
            });
        });
        return fetchPlanBuilder.build();
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

    protected ObjectNode createResultIndexDocument(MetaClass metaClass, ObjectNode entityIndexContent) {
        ObjectNode resultObject = JsonNodeFactory.instance.objectNode();
        resultObject.putObject("meta").put("entityClass", metaClass.getName());
        resultObject.set("content", entityIndexContent);
        return resultObject;
    }

    //todo move to tools?
    public void merge(JsonNode toBeMerged, JsonNode mergedInTo) {
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
