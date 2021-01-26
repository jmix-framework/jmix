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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.FetchPlans;
import io.jmix.core.LoadContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.EntityChangeType;
import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(EntityIndexer.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected RestHighLevelClient esClient;

    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;

    public void indexEntityById(MetaClass metaClass, Object entityId, EntityChangeType changeType) {
        indexEntitiesByIds(metaClass, Collections.singletonList(entityId), changeType);
    }

    public void indexEntitiesByIds(MetaClass metaClass, Collection<Object> entityIds, EntityChangeType changeType) {
        log.info("[IVGA] Index entity: Class={}, changeType={}, ids={}", metaClass, changeType, entityIds);
        IndexDefinition indexDefinition = indexDefinitionsProvider.getIndexDefinitionForEntityClass(metaClass.getJavaClass());
        if(indexDefinition == null) {
            log.warn("[IVGA] Index Definition not found for entity {}", metaClass);
            return;
        }
        log.info("[IVGA] Mapping Fields for entity '{}': {}", metaClass, indexDefinition.getMapping().getFields());

        if(EntityChangeType.UPDATE.equals(changeType) || EntityChangeType.CREATE.equals(changeType)) {
            LoadContext<Object> loadContext = new LoadContext<>(metaClass);
            FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
            indexDefinition.getMapping().getFields().values().forEach((field) -> fetchPlanBuilder.add(field.getEntityPropertyFullName()));

            loadContext.setIds(entityIds).setFetchPlan(fetchPlanBuilder.build());
            List<Object> loaded = dataManager.loadList(loadContext);
            log.info("[IVGA] Loaded entities: {}", loaded);

            loaded.forEach(object -> {
                ObjectNode indexObject = JsonNodeFactory.instance.objectNode();
                List<ObjectNode> fieldObjects = indexDefinition.getMapping().getFields().values().stream()
                        .filter(field -> !field.isStandalone())
                        .map(field -> {
                            log.info("[IVGA] Extract value of property {}", field.getMetaPropertyPath());
                            JsonNode propertyValue = field.getValueMapper().getValue(object, field.getMetaPropertyPath(), Collections.emptyMap());
                            String indexPropertyFullName = field.getIndexPropertyFullName();

                            ObjectNode objectNodeForField = createObjectNodeForField(indexPropertyFullName, propertyValue);
                            log.info("[IVGA] objectNodeForField = {}", objectNodeForField);

                            return objectNodeForField;
                        })
                        .collect(Collectors.toList());

                fieldObjects.forEach(fieldObject ->  merge(fieldObject, indexObject));
                log.info("[IVGA] INDEX OBJECT {}: Result Json = {}", object, indexObject);
            });
        } else if(EntityChangeType.DELETE.equals(changeType)) {
            //todo
        } else {
            throw new UnsupportedOperationException("Entity Change Type '" + changeType + "' is not supported");
        }
    }

    protected ObjectNode createObjectNodeForField(String key, JsonNode value) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        String[] fields = key.split("\\.");
        ObjectNode currentRoot = root;
        for(int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if(i == fields.length - 1) {
                currentRoot.set(field, value);
            } else {
                currentRoot = currentRoot.putObject(field);
            }
        }
        return root;
    }

    //todo move to tools?
    public void merge(JsonNode toBeMerged, JsonNode mergedInTo) {
        log.info("[IVGA] Merge object {} into {}", toBeMerged, mergedInTo);
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
