/*
 * Copyright 2024 Haulmont.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides non-platform-specific functionality.
 * Interaction with indexes is performed in platform-specific implementations.
 */
public abstract class BaseEntityIndexer implements EntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(BaseEntityIndexer.class);

    protected final UnconstrainedDataManager dataManager;
    protected final FetchPlans fetchPlans;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final Metadata metadata;
    protected final IdSerialization idSerialization;
    protected final IndexStateRegistry indexStateRegistry;
    protected final MetadataTools metadataTools;
    protected final SearchProperties searchProperties;

    protected final ObjectMapper objectMapper;

    public BaseEntityIndexer(UnconstrainedDataManager dataManager,
                             FetchPlans fetchPlans,
                             IndexConfigurationManager indexConfigurationManager,
                             Metadata metadata,
                             IdSerialization idSerialization,
                             IndexStateRegistry indexStateRegistry,
                             MetadataTools metadataTools,
                             SearchProperties searchProperties) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.indexConfigurationManager = indexConfigurationManager;
        this.metadata = metadata;
        this.idSerialization = idSerialization;
        this.indexStateRegistry = indexStateRegistry;
        this.metadataTools = metadataTools;
        this.searchProperties = searchProperties;
        this.objectMapper = new ObjectMapper();
    }

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
        return deleteByGroupedIndexIdsInternal(groupedIndexIds);
    }

    @Override
    public IndexResult deleteByEntityId(Id<?> entityId) {
        return deleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<String>> groupedIndexIds = prepareIndexIdsByEntityIds(entityIds);
        return deleteByGroupedIndexIdsInternal(groupedIndexIds);
    }

    protected abstract IndexResult indexDocuments(List<IndexDocumentData> documents);

    protected abstract IndexResult deleteByGroupedDocIds(Map<IndexConfiguration, Collection<String>> groupedDocIds);

    protected IndexResult indexGroupedInstances(Map<IndexConfiguration, Collection<Object>> groupedInstances) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedInstances.values().stream()
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            log.debug("[INDEX] Prepared {} instances within {} entities", amountOfInstances, groupedInstances.keySet().size());
        }

        List<IndexDocumentData> documents = new ArrayList<>();
        for (Map.Entry<IndexConfiguration, Collection<Object>> entry : groupedInstances.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();
            if (indexStateRegistry.isIndexAvailable(indexConfiguration.getEntityName())) {
                Predicate<Object> indexablePredicate = indexConfiguration.getIndexablePredicate();
                for (Object instance : entry.getValue()) {
                    if (indexablePredicate.test(instance)) {
                        documents.add(generateIndexDocument(indexConfiguration, instance));
                    }
                }
            }
        }

        return indexDocuments(documents);
    }

    protected IndexResult deleteByGroupedIndexIdsInternal(Map<IndexConfiguration, Collection<String>> groupedIndexIds) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedIndexIds.values().stream()
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            log.debug("[DELETE] Prepared {} instances within {} entities", amountOfInstances, groupedIndexIds.keySet().size());
        }
        return deleteByGroupedDocIds(groupedIndexIds);
    }

    protected Map<IndexConfiguration, Collection<Object>> prepareInstancesForIndexing(Collection<Object> instances) {
        Map<MetaClass, List<Object>> idsGroupedByMetaClass = instances.stream().collect(
                Collectors.groupingBy(
                        metadata::getClass,
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

    protected Map<IndexConfiguration, Collection<Object>> reloadEntityInstances(Map<MetaClass, List<Object>> idsGroupedByMetaClass) {
        Map<IndexConfiguration, FetchPlan> fetchPlanLocalCache = new HashMap<>();
        Map<IndexConfiguration, Collection<Object>> result = new HashMap<>();
        idsGroupedByMetaClass.forEach((metaClass, entityIds) -> {
            Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName());
            if (indexConfigurationOpt.isPresent()) {
                IndexConfiguration indexConfiguration = indexConfigurationOpt.get();
                FetchPlan fetchPlan = fetchPlanLocalCache.computeIfAbsent(indexConfiguration, this::createFetchPlan);
                List<Object> loaded;
                if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                    loaded = entityIds.stream()
                            .map(id -> dataManager
                                    .load(metaClass.getJavaClass())
                                    //TODO почему грузится по одной записи?
                                    .id(id)
                                    .fetchPlan(fetchPlan)
                                    .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                                    .optional())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                } else {
                    String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
                    String discriminatorCondition = metaClass.getDescendants().isEmpty() ? "" : " and TYPE(e) = " + metaClass.getName();
                    String queryString = "select e from " + metaClass.getName() + " e where e." + primaryKeyName + " in :ids" + discriminatorCondition;
                    loaded = dataManager
                            .load(metaClass.getJavaClass())
                            .query(queryString)
                            .parameter("ids", entityIds)
                            .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                            .fetchPlan(fetchPlan)
                            .list();
                }
                result.put(indexConfiguration, loaded);
            }
        });
        return result;
    }

    protected FetchPlan createFetchPlan(IndexConfiguration indexConfiguration) {
        Class<?> entityClass = indexConfiguration.getEntityClass();
        MetaClass metaClass = metadata.getClass(entityClass);
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(entityClass);
        indexConfiguration.getMapping().getFields().values().forEach(field -> {
            String entityPropertyFullName = field.getEntityPropertyFullName();
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(entityPropertyFullName);
            if (propertyPath != null){
                log.trace("Add property to fetch plan: {}", entityPropertyFullName);
                fetchPlanBuilder.add(entityPropertyFullName);
                field.getInstanceNameRelatedProperties().forEach(instanceNameRelatedProperty -> {
                    log.trace("Add instance name related property to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                    if (instanceNameRelatedProperty.getRange().isClass()) {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString(), FetchPlan.INSTANCE_NAME);
                    } else {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
                    }
                });
            }
        });

        indexConfiguration.getMapping()
                .getDisplayedNameDescriptor()
                .getInstanceNameRelatedProperties()
                .forEach(instanceNameRelatedProperty -> {
                    log.trace("Add instance name related property (displayed name) to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                    if (instanceNameRelatedProperty.getRange().isClass()) {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString(), FetchPlan.INSTANCE_NAME);
                    } else {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
                    }
                });

        return fetchPlanBuilder.build();
    }

    // document generation
    protected IndexDocumentData generateIndexDocument(IndexConfiguration indexConfiguration,
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
        String serializedEntityId = idSerialization.idToString(Id.of(instance));
        return new IndexDocumentData(indexConfiguration.getIndexName(), serializedEntityId, sourceObject);
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

    protected void merge(JsonNode toBeMerged, JsonNode mergedInTo) {
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
                    break;
                default:
                    break;
            }
            if (valueNode != null) {
                updateObject(mergedInTo, valueNode, incomingEntry);
            }
        }
    }

    protected void updateArray(JsonNode valueToBePlaced, Map.Entry<String, JsonNode> toBeMerged) {
        toBeMerged.setValue(valueToBePlaced);
    }

    protected void updateObject(JsonNode mergeInTo, ValueNode valueToBePlaced,
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

    protected record IndexDocumentData(String indexName, String id, ObjectNode source) {
    }
}
