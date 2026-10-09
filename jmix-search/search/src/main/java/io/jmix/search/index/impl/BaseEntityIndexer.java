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
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityDeletionTarget;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.impl.dynattr.DynamicAttributesSupport;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides non-platform-specific functionality.
 * Interaction with indexes is performed in platform-specific implementations.
 */
@NullMarked
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
    protected final DynamicAttributesSupport dynamicAttributesSupport;
    protected final ObjectMapper objectMapper;
    protected final MultitenancyAdapter multitenancyAdapter;

    @Autowired
    protected IndexLayout indexLayout;

    public BaseEntityIndexer(UnconstrainedDataManager dataManager,
                             FetchPlans fetchPlans,
                             IndexConfigurationManager indexConfigurationManager,
                             Metadata metadata,
                             IdSerialization idSerialization,
                             IndexStateRegistry indexStateRegistry,
                             MetadataTools metadataTools,
                             SearchProperties searchProperties,
                             DynamicAttributesSupport dynamicAttributesSupport,
                             MultitenancyAdapter multitenancyAdapter) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.indexConfigurationManager = indexConfigurationManager;
        this.metadata = metadata;
        this.idSerialization = idSerialization;
        this.indexStateRegistry = indexStateRegistry;
        this.metadataTools = metadataTools;
        this.searchProperties = searchProperties;
        this.dynamicAttributesSupport = dynamicAttributesSupport;
        this.multitenancyAdapter = multitenancyAdapter;
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
        List<EntityDeletionTarget> targets = entityInstances.stream()
                .map(instance -> new EntityDeletionTarget(Id.of(instance), tenantOfInstance(instance)))
                .toList();
        return deleteCollectionByTargets(targets);
    }

    @Override
    public IndexResult deleteByEntityId(Id<?> entityId) {
        return deleteCollectionByTargets(List.of(EntityDeletionTarget.tenantUnknown(entityId)));
    }

    @Deprecated(since = "3.1", forRemoval = true)
    @Override
    public IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return deleteCollectionByTargets(entityIds.stream().map(EntityDeletionTarget::tenantUnknown).toList());
    }

    @Override
    public IndexResult deleteCollectionByTargets(Collection<EntityDeletionTarget> targets) {
        return deleteByGroupedIndexIdsInternal(prepareIndexIdsByTargets(targets));
    }

    /**
     * Reads the tenant off an instance the caller holds.
     * <p>
     * The instance comes from application code and may be detached with the tenant attribute left unfetched. The
     * tenant is then unknown and the document is deleted from every index of the entity: slower, but still
     * correct, and better than losing the whole batch of deletions.
     */
    @Nullable
    protected String tenantOfInstance(Object instance) {
        if (!multitenancyAdapter.isTenantIdReadable(instance)) {
            log.debug("The tenant of an instance of entity '{}' cannot be read: its document is deleted from every"
                    + " index of the entity", metadata.getClass(instance).getName());
            return null;
        }
        return multitenancyAdapter.getTenantIdForInstance(instance);
    }

    protected abstract IndexResult indexDocuments(List<IndexDocumentData> documents);

    protected abstract IndexResult deleteByGroupedDocIds(List<DocumentToDelete> documents);

    protected IndexResult indexGroupedInstances(Map<IndexConfiguration, Collection<Object>> groupedInstances) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedInstances.values().stream()
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            log.debug("[INDEX] Prepared {} instances within {} entities", amountOfInstances, groupedInstances.keySet().size());
        }

        List<IndexDocumentData> documents = new ArrayList<>();
        List<IndexResult.Failure> postponed = new ArrayList<>();
        for (Map.Entry<IndexConfiguration, Collection<Object>> entry : groupedInstances.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();

            addDocuments(entry, indexConfiguration, documents, postponed);
        }
        IndexResult result = documents.isEmpty() ? nothingToSend() : indexDocuments(documents);
        return withPostponed(result, postponed);
    }

    /**
     * Reports instances that were not sent to the search engine as failures, so that the caller keeps them for
     * the next attempt instead of considering them processed.
     */
    protected IndexResult withPostponed(IndexResult indexResult, List<IndexResult.Failure> postponed) {
        if (postponed.isEmpty()) {
            return indexResult;
        }
        List<IndexResult.Failure> failures = new ArrayList<>(indexResult.getFailures());
        failures.addAll(postponed);
        return new IndexResult(indexResult.getTotalSize() + postponed.size(), failures);
    }

    protected void addDocuments(Map.Entry<IndexConfiguration, Collection<Object>> entry,
                                IndexConfiguration indexConfiguration,
                                List<IndexDocumentData> documents,
                                List<IndexResult.Failure> postponed) {
        Predicate<Object> indexablePredicate = indexConfiguration.getIndexablePredicate();

        if (!indexLayout.isSplitByTenants(indexConfiguration)) {
            String indexName = indexLayout.indexName(indexConfiguration, null);
            for (Object instance : entry.getValue()) {
                if (indexName != null && indexStateRegistry.isIndexAvailable(indexName)) {
                    addSingleDocumentSafely(indexConfiguration, documents, instance, indexName,
                            indexablePredicate, postponed);
                } else {
                    postponed.add(indexUnavailableFailure(instance, indexConfiguration, indexName));
                }
            }
            return;
        }

        // The index name depends on the tenant only, so it is computed once per tenant of the batch.
        Map<String, String> indexNamesByTenant = new HashMap<>();
        for (Object instance : entry.getValue()) {
            // No guard: these instances come from reloadEntityInstances, whose fetch plan carries the tenant.
            String tenantId = multitenancyAdapter.getTenantIdForInstance(instance);
            if (tenantId == null) {
                // The instance will not get a tenant later, so returning it to the queue would only repeat forever.
                log.warn("Instance {} of entity '{}' is not indexed: it belongs to no tenant, and the data of the"
                                + " entity is stored per tenant",
                        idSerialization.idToString(Id.of(instance)), indexConfiguration.getEntityName());
            } else {
                String indexName = indexNamesByTenant.computeIfAbsent(tenantId,
                        id -> indexLayout.indexName(indexConfiguration, id));
                if (indexName != null && indexStateRegistry.isIndexAvailable(indexName)) {
                    addSingleDocumentSafely(indexConfiguration, documents, instance, indexName,
                            indexablePredicate, postponed);
                } else {
                    postponed.add(indexUnavailableFailure(instance, indexConfiguration, indexName));
                }
            }
        }
    }

    /**
     * Builds the document of a single instance, keeping a failure of one instance from aborting the whole batch.
     * <p>
     * Everything here runs application code — the indexable predicate, the value extractors of the mapping — so an
     * instance with unexpected data can throw. Without this the exception would leave the queue processing
     * altogether: nothing gets removed from the queue, and the next run takes the same batch and throws again.
     */
    protected void addSingleDocumentSafely(IndexConfiguration indexConfiguration,
                                           List<IndexDocumentData> documents,
                                           Object instance,
                                           String indexName,
                                           Predicate<Object> indexablePredicate,
                                           List<IndexResult.Failure> postponed) {
        try {
            addSingleDocument(indexConfiguration, documents, instance, indexName, indexablePredicate);
        } catch (RuntimeException e) {
            postponed.add(documentBuildingFailure(instance, indexConfiguration, indexName, e));
        }
    }

    protected IndexResult.Failure documentBuildingFailure(Object instance,
                                                          IndexConfiguration indexConfiguration,
                                                          String indexName,
                                                          RuntimeException cause) {
        String instanceId = idSerialization.idToString(Id.of(instance));
        log.error("Unable to build the document of instance {} of entity '{}' for index '{}'."
                        + " The rest of the batch is indexed without it, and the instance stays in the queue",
                instanceId, indexConfiguration.getEntityName(), indexName, cause);
        return new IndexResult.Failure(instanceId, indexName, "Failed to build the document: " + cause.getMessage());
    }

    protected IndexResult.Failure indexUnavailableFailure(Object instance,
                                                          IndexConfiguration indexConfiguration,
                                                          @Nullable String indexName) {
        log.debug("Indexing of an instance of entity '{}' is postponed: index '{}' is not available",
                indexConfiguration.getEntityName(), indexName);
        return new IndexResult.Failure(
                idSerialization.idToString(Id.of(instance)),
                indexName == null ? "" : indexName,
                "Index is not available");
    }

    protected void addSingleDocument(IndexConfiguration indexConfiguration, List<IndexDocumentData> documents, Object entity, String indexName, Predicate<Object> indexablePredicate) {
        if (indexablePredicate.test(entity)) {
            documents.add(generateIndexDocument(
                    indexConfiguration.getMapping(),
                    indexName,
                    entity));
        }
    }

    protected IndexResult deleteByGroupedIndexIdsInternal(Map<IndexConfiguration, Collection<DocumentToDelete>> groupedIndexIds) {
        if (log.isDebugEnabled()) {
            Integer amountOfInstances = groupedIndexIds.values().stream()
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            log.debug("[DELETE] Prepared {} documents across {} indexed entities", amountOfInstances,
                    groupedIndexIds.size());
        }
        List<DocumentToDelete> documents = groupedIndexIds.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return documents.isEmpty() ? nothingToSend() : deleteByGroupedDocIds(documents);
    }

    /**
     * The result of an operation that reaches no index at all - a deletion of a record of a tenant-aware entity in an
     * application that has no tenants yet, or a batch whose every instance was postponed.
     * <p>
     * It must not reach the engine: a bulk request without operations cannot even be built, the client rejects it
     * before it is sent.
     */
    protected IndexResult nothingToSend() {
        log.debug("Nothing to send to the engine");
        return new IndexResult(0, List.of());
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

    /**
     * Maps records to delete onto the physical indexes their documents live in.
     * <p>
     * A record whose tenant is known goes to that tenant's index alone. A record whose tenant is unknown goes to
     * every index of the entity: the document lives in exactly one of them, and the engine reports the others as
     * missing, which is not a failure. For an entity that is not split by tenants both branches yield its single
     * index.
     */
    protected Map<IndexConfiguration, Collection<DocumentToDelete>> prepareIndexIdsByTargets(
            Collection<EntityDeletionTarget> targets) {
        Map<IndexConfiguration, Collection<DocumentToDelete>> result = new HashMap<>();
        targets.forEach(target -> {
            MetaClass metaClass = metadata.getClass(target.entityId().getEntityClass());
            indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName())
                    .ifPresent(indexConfiguration -> {
                        String documentId = idSerialization.idToString(target.entityId());
                        Collection<DocumentToDelete> documentsForConfig =
                                result.computeIfAbsent(indexConfiguration, k -> new HashSet<>());
                        indexNamesToDeleteFrom(indexConfiguration, target.tenantId()).forEach(indexName ->
                                documentsForConfig.add(new DocumentToDelete(documentId, indexName)));
                    });
        });
        return result;
    }

    protected List<String> indexNamesToDeleteFrom(IndexConfiguration indexConfiguration, @Nullable String tenantId) {
        if (tenantId != null) {
            String indexName = indexLayout.indexName(indexConfiguration, tenantId);
            return indexName == null ? List.of() : List.of(indexName);
        }
        List<String> indexNames = indexLayout.allIndexes(indexConfiguration).stream()
                .map(IndexLayout.TenantIndex::indexName)
                .toList();
        if (indexLayout.isSplitByTenants(indexConfiguration)) {
            log.debug("Tenant of a deleted record of entity '{}' is unknown: its document is deleted from all {}"
                    + " indexes of the entity", indexConfiguration.getEntityName(), indexNames.size());
        }
        return indexNames;
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
                                    .id(id)
                                    .fetchPlan(fetchPlan)
                                    .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                                    .optional())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                } else if (!metadataTools.isJpaEntity(metaClass)) {
                    // A non-JPA store cannot run the JPQL below; its own query path supports an IN condition.
                    String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
                    loaded = dataManager
                            .load(metaClass.getJavaClass())
                            .condition(PropertyCondition.inList(primaryKeyName, entityIds))
                            .fetchPlan(fetchPlan)
                            .list();
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
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(indexConfiguration.getEntityClass());
        indexConfiguration.getMapping().getFields().values().forEach(field -> {
            String entityPropertyFullName = field.getEntityPropertyFullName();
            if (!dynamicAttributesSupport.isDynamicAttributeName(entityPropertyFullName)) {
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

        addTenantAttribute(indexConfiguration, fetchPlanBuilder);

        return fetchPlanBuilder.build();
    }

    /**
     * The tenant of an instance decides which index it goes to, so the tenant attribute has to be loaded even
     * though it is not mapped into the document. Without it the attribute stays unfetched and reading it throws
     * while the instance is already detached.
     */
    protected void addTenantAttribute(IndexConfiguration indexConfiguration, FetchPlanBuilder fetchPlanBuilder) {
        MetaClass metaClass = metadata.getClass(indexConfiguration.getEntityClass());
        MetaProperty tenantProperty = metadataTools.findTenantIdProperty(metaClass);
        if (tenantProperty != null) {
            log.trace("Add tenant property to fetch plan: {}", tenantProperty.getName());
            fetchPlanBuilder.add(tenantProperty.getName());
        }
    }

    // document generation
    protected IndexDocumentData generateIndexDocument(IndexMappingConfiguration indexMappingConfiguration,
                                                      String indexName,
                                                      Object instance) {
        ObjectNode sourceObject = JsonNodeFactory.instance.objectNode();
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
        return new IndexDocumentData(indexName, serializedEntityId, sourceObject);
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

    protected record DocumentToDelete(String entityId, String indexName) {
    }
}
