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

package io.jmix.search.index.mapping;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.metadata.GenerationStateStore;
import io.jmix.core.impl.metadata.MetadataGenerationManager;
import io.jmix.core.impl.metadata.MetadataGenerationRetiredEvent;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.exception.IndexDefinitionRejectedException;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor;
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The {@code IndexConfigurationManager} class provides functionality for managing
 * index configurations within an application. It allows for retrieving, creating,
 * and refreshing index definitions, as well as for determining the involvement of
 * entities in the indexing process.
 *
 * <p>This class is responsible for ensuring index configuration consistency and
 * providing metadata about entities and their relationships in the indexing context.
 *
 * <h2>Main Responsibilities</h2>
 * <ul>
 *   <li>Managing index definitions and their lifecycle</li>
 *   <li>Retrieving index configurations by entity or index name</li>
 *   <li>Determining the extent to which entities are indexed</li>
 *   <li>Providing metadata on dependencies between entities in the indexing process</li>
 * </ul>
 *
 * <p>Definitions contributed by {@link IndexDefinitionContributor} beans are merged with the annotated Java ones on
 * every (re)build: a contribution for an entity without a Java definition creates a configuration, one for an entity
 * that has a Java definition appends its fields.
 */
@Component("search_IndexConfigurationManager")
public class IndexConfigurationManager {

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationManager.class);

    protected static class State {
        /**
         * Replaced as a whole when the definitions are rebuilt, never refilled in place.
         * <p>
         * A reader walks what it was handed - the queue builds its query from it, the change listener asks on
         * every save, every search asks for the scope - and a walk takes long enough for a rebuild to land in the
         * middle of it. Refilling would show that reader a set half taken apart; replacing leaves the set it
         * holds exactly as it was and gives the next reader the new one.
         */
        protected volatile Registry registry;
        protected volatile boolean initialized;

        protected State(Registry registry) {
            this.registry = registry;
        }
    }

    protected final AnnotatedIndexDefinitionProcessor indexDefinitionProcessor;
    protected final Set<String> classNames;
    protected final InstanceNameProvider instanceNameProvider;
    protected final GenerationStateStore<State> stateStore = new GenerationStateStore<>();


    /**
     * Stands for any tenant while the index names of different entities are compared with each other. Spelled the
     * same as the sample the name generator validates its patterns with - both only have to be a usable tenant id.
     */
    protected static final String SAMPLE_TENANT_ID = "sampleTenant";

    @Autowired
    protected IndexLayout indexLayout;
    protected final IndexStateRegistry indexStateRegistry;

    @Autowired
    protected MetadataGenerationManager metadataGenerationManager;

    @Autowired(required = false)
    protected List<IndexDefinitionContributor> indexDefinitionContributors = Collections.emptyList();

    public IndexConfigurationManager(JmixModulesClasspathScanner classpathScanner,
                                     AnnotatedIndexDefinitionProcessor indexDefinitionProcessor,
                                     InstanceNameProvider instanceNameProvider,
                                     IndexDefinitionDetector indexDefinitionDetector,
                                     IndexStateRegistry indexStateRegistry) {
        this.indexDefinitionProcessor = indexDefinitionProcessor;
        this.instanceNameProvider = instanceNameProvider;
        this.indexStateRegistry = indexStateRegistry;
        Class<? extends IndexDefinitionDetector> detectorClass = indexDefinitionDetector.getClass();
        classNames = Collections.unmodifiableSet(classpathScanner.getClassNames(detectorClass));
    }

    /**
     * Refreshes and recreates the index definitions by creating and registering them.
     * <p>
     * This method ensures that the current index configurations are updated to reflect
     * any changes in the index definitions within the application.
     * <p>
     * This refreshing also takes into account any changes in the dynamic attributes metadata
     * if the Dynamic attributes add-on is used in the project.
     */
    public void refreshIndexDefinitions() {
        // Forget which indexes were known to be ready: the definitions are about to be recomputed, and an index
        // whose mapping changed must not be written to before it has been checked again. The caller of this
        // method synchronizes the schemas right after, which is what fills the knowledge back in - the lazy
        // rebuild on a new metadata generation has no such repair, so it leaves the markers alone.
        indexStateRegistry.clean();
        initializeIndexDefinitions(getState());
    }

    /**
     * Removes index-configuration state cached for a retired metadata generation.
     *
     * @param event retired-generation event
     */
    @EventListener
    public void onMetadataGenerationRetired(MetadataGenerationRetiredEvent event) {
        stateStore.remove(event.getGenerationId());
    }

    /**
     * Gets all {@link IndexConfiguration} registered in application
     *
     * @return all {@link IndexConfiguration}
     */
    public Collection<IndexConfiguration> getAllIndexConfigurations() {
        State state = getState();
        ensureInitialized(state);
        return state.registry.getIndexConfigurations();
    }

    /**
     * Gets {@link IndexConfiguration} registered for provided entity name.
     * Throws {@link IllegalArgumentException} if there is no configuration for provided entity name.
     *
     * @param entityName entity name.
     * @return {@link IndexConfiguration}
     */
    public IndexConfiguration getIndexConfigurationByEntityName(String entityName) {
        State state = getState();
        ensureInitialized(state);
        IndexConfiguration indexConfiguration = state.registry.getIndexConfigurationByEntityName(entityName);
        if (indexConfiguration == null) {
            throw new IllegalArgumentException("Entity '" + entityName + "' is not configured for indexing");
        }
        return indexConfiguration;
    }

    /**
     * Gets optional {@link IndexConfiguration} registered for provided entity name.
     *
     * @param entityName entity name
     * @return optional {@link IndexConfiguration}
     */
    public Optional<IndexConfiguration> getIndexConfigurationByEntityNameOpt(String entityName) {
        State state = getState();
        ensureInitialized(state);
        return Optional.ofNullable(state.registry.getIndexConfigurationByEntityName(entityName));
    }

    /**
     * Gets {@link IndexConfiguration} registered for provided index name.
     * Throws {@link IllegalArgumentException} if there is no configuration for provided index name.
     *
     * @param indexName index name
     * @return {@link IndexConfiguration}
     * @deprecated an index name no longer identifies one configuration on its own: an entity split by tenants has
     * one index per tenant, and the names are built from tenant ids while the application runs. Ask by entity name
     * instead - {@link #getIndexConfigurationByEntityName(String)}.
     */
    @Deprecated(since = "3.1", forRemoval = true)
    public IndexConfiguration getIndexConfigurationByIndexName(String indexName) {
        return getIndexConfigurationByIndexNameOpt(indexName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "There is no configuration for index name '" + indexName + "'"));
    }

    /**
     * Gets optional {@link IndexConfiguration} registered for provided index name.
     * <p>
     * Answering this reads the tenants of the application: the index names of a split entity are not stored
     * anywhere to be looked up.
     *
     * @param indexName index name
     * @return optional {@link IndexConfiguration}
     * @deprecated see {@link #getIndexConfigurationByIndexName(String)}
     */
    @Deprecated(since = "3.1", forRemoval = true)
    public Optional<IndexConfiguration> getIndexConfigurationByIndexNameOpt(String indexName) {
        return indexLayout.allIndexes(getAllIndexConfigurations()).entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(index -> index.indexName().equals(indexName)))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Collection<String> getAllIndexedEntities() {
        State state = getState();
        ensureInitialized(state);
        return state.registry.getAllIndexedEntities();
    }

    /**
     * Checks if the provided entity is declared to be indexed directly (not as a part of another entity).
     *
     * @param entityName entity name
     * @return true if the entity is indexed, false otherwise
     */
    public boolean isDirectlyIndexed(String entityName) {
        State state = getState();
        ensureInitialized(state);
        return state.registry.hasDefinitionForEntity(entityName);
    }

    /**
     * Checks if the provided entity is involved in the index process directly or as a part of another entity.
     *
     * @param entityClass entity java class
     * @return true if the entity is involved in the index process, false otherwise
     */
    public boolean isAffectedEntityClass(Class<?> entityClass) {
        State state = getState();
        ensureInitialized(state);
        return state.registry.isEntityClassRegistered(entityClass);
    }

    /**
     * Gets local property names of the provided entity involved into the index update process
     *
     * @param entityClass entity class
     * @return set of property names
     */
    public Set<String> getLocalPropertyNamesAffectedByUpdate(Class<?> entityClass) {
        State state = getState();
        ensureInitialized(state);
        return state.registry.getLocalPropertyNamesAffectedByUpdate(entityClass);
    }

    /**
     * Gets metadata of entities dependent on the updated main entity and its changed properties.
     *
     * @param entityClass       java class of the main entity
     * @param changedProperties changed property of the main entity
     * @return dependent entities grouped by their {@link MetaClass}.
     * For every meta class group there are set of properties representing dependency-to-main references
     */
    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForUpdate(Class<?> entityClass, Set<String> changedProperties) {
        log.debug("Get dependencies metadata for class {} with changed properties: {}", entityClass, changedProperties);
        State state = getState();
        ensureInitialized(state);
        Map<String, Set<MetaPropertyPath>> backRefProperties =
                state.registry.getBackRefPropertiesForUpdate(entityClass);
        if (MapUtils.isEmpty(backRefProperties)) {
            return Collections.emptyMap();
        }

        Map<MetaClass, Set<MetaPropertyPath>> result = new HashMap<>();

        changedProperties.stream()
                .flatMap(changedProperty -> {
                    Set<MetaPropertyPath> metaPropertyPaths = backRefProperties.get(changedProperty);
                    return metaPropertyPaths == null ? Stream.empty() : metaPropertyPaths.stream();
                })
                .forEach(property -> {
                    MetaClass metaClass = property.getMetaClass();
                    Set<MetaPropertyPath> metaPropertyPaths = result.computeIfAbsent(metaClass, k -> new HashSet<>());
                    metaPropertyPaths.add(property);
                });
        return result;
    }

    /**
     * Gets metadata of entities dependent on the deleted main entity.
     *
     * @param deletedEntityClass java class of the main entity
     * @return dependent entities grouped by their {@link MetaClass}.
     * For every meta class group there are set of properties representing dependency-to-main references
     */
    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForDelete(Class<?> deletedEntityClass) {
        log.debug("Get dependencies metadata for class {} deletion", deletedEntityClass);
        State state = getState();
        ensureInitialized(state);
        Set<MetaPropertyPath> backRefPropertiesDelete = state.registry.getBackRefPropertiesForDelete(deletedEntityClass);
        if (CollectionUtils.isEmpty(backRefPropertiesDelete)) {
            return Collections.emptyMap();
        }

        Map<MetaClass, Set<MetaPropertyPath>> result = new HashMap<>();

        backRefPropertiesDelete.forEach(property -> {
            MetaClass metaClass = property.getMetaClass();
            Set<MetaPropertyPath> metaPropertyPaths = result.computeIfAbsent(metaClass, k -> new HashSet<>());
            metaPropertyPaths.add(property);
        });

        return result;
    }

    /**
     * Initializes the index definitions by creating and registering them.
     * <p>
     * The method ensures that the current index configurations are updated to match
     * the definitions specified by the provided class names.
     */
    protected void initializeIndexDefinitions(State state) {
        Map<String, IndexConfiguration> configurations = new LinkedHashMap<>();
        for (String className : classNames) {
            try {
                IndexConfiguration configuration = indexDefinitionProcessor.createIndexConfiguration(className);
                if (configurations.putIfAbsent(configuration.getEntityName(), configuration) != null) {
                    log.warn("Multiple Index Definitions are detected for entity '{}'", configuration.getEntityName());
                }
            } catch (IndexDefinitionRejectedException e) {
                // The entity of this definition is left out; the entities of the other definitions are not.
                log.error("Index definition {} is not applied. {}", className, e.getMessage());
            }
        }
        for (IndexDefinitionContributor contributor : indexDefinitionContributors) {
            Collection<ContributedIndexDefinition> definitions = contributor.getIndexDefinitions();
            if (definitions == null) {
                throw new IllegalStateException(
                        "getIndexDefinitions() of " + contributor.getClass().getName() + " returned null " +
                                "instead of an empty collection");
            }
            for (ContributedIndexDefinition definition : definitions) {
                try {
                    configurations.compute(definition.getEntityName(), (entityName, existing) -> existing == null
                            ? indexDefinitionProcessor.createIndexConfiguration(definition)
                            : indexDefinitionProcessor.appendContributedFields(existing, definition));
                } catch (IndexDefinitionRejectedException e) {
                    // The same rule as for an annotated definition: this entity is left out, the rest keep working.
                    // A contribution rejected on top of an existing configuration leaves that configuration alone.
                    log.error("Contributed index definition of entity '{}' is not applied. {}",
                            definition.getEntityName(), e.getMessage());
                }
            }
        }
        replaceConfigurations(state, accepted(configurations.values()));
    }

    /**
     * Leaves out the configurations that cannot be applied, whichever way they were built.
     * <p>
     * The rule is attached to the assembled configuration rather than to the way of assembling one: a definition
     * comes from an annotation, from a contribution, or from a contribution on top of an annotation, and a new way
     * of building one must not need a new place to check it.
     */
    protected List<IndexConfiguration> accepted(Collection<IndexConfiguration> configurations) {
        List<IndexConfiguration> accepted = new ArrayList<>(configurations.size());
        for (IndexConfiguration configuration : configurations) {
            try {
                indexDefinitionProcessor.checkNoTenantDataInSharedIndex(
                        String.format("Index definition of entity '%s'", configuration.getEntityName()),
                        configuration.getMapping());
                accepted.add(configuration);
            } catch (IndexDefinitionRejectedException e) {
                log.error("Entity '{}' is not indexed. {}", configuration.getEntityName(), e.getMessage());
            }
        }
        return accepted;
    }

    /**
     * Replaces the current index configurations with the provided list of new configurations.
     *
     * @param configurations the list of {@link IndexConfiguration} objects to be set in the registry
     */
    protected void replaceConfigurations(State state, List<IndexConfiguration> configurations) {
        List<IndexConfiguration> accepted = dropIndexNameCollisions(configurations);
        Registry fresh = new Registry(instanceNameProvider);
        accepted.forEach(fresh::registerIndexConfiguration);
        state.registry = fresh;
    }

    /**
     * Leaves out the entities that claim an index already taken: their documents would be mixed in one index, and
     * the mapping of one entity would be applied to the documents of the other.
     * <p>
     * The first claim wins and the later ones are dropped, so one mistake costs the entity that made it and
     * nothing else. The module behaved this way before the check existed, except that the loss was silent.
     * <p>
     * Names are compared by the shape a configuration produces, not by the names of the indexes that exist right
     * now: the name of a configuration split by tenants is taken for one sample tenant. Two tenants of the same
     * entity can never collide, because the pattern of a split configuration is required to contain the tenant
     * placeholder, so comparing one sample tenant is enough and the check does not depend on which tenants the
     * application happens to have.
     *
     * @return the configurations that keep their index
     */
    protected List<IndexConfiguration> dropIndexNameCollisions(List<IndexConfiguration> configurations) {
        Map<String, String> entityNamesByIndexName = new HashMap<>();
        List<IndexConfiguration> accepted = new ArrayList<>(configurations.size());
        for (IndexConfiguration configuration : configurations) {
            String indexName = sampleIndexName(configuration);
            String claimedBy = entityNamesByIndexName.putIfAbsent(indexName, configuration.getEntityName());
            if (claimedBy != null) {
                log.error("Entity '{}' is not indexed: index '{}' is already taken by entity '{}'. Their documents"
                                + " would be mixed in one index. Give one of them an index name of its own in its"
                                + " index definition.",
                        configuration.getEntityName(), indexName, claimedBy);
                continue;
            }
            accepted.add(configuration);
        }
        return accepted;
    }

    protected String sampleIndexName(IndexConfiguration configuration) {
        String indexName = indexLayout.isSplitByTenants(configuration)
                ? indexLayout.indexName(configuration, SAMPLE_TENANT_ID)
                : indexLayout.indexName(configuration, null);
        return requireNonNull(indexName);
    }

    protected State getState() {
        return stateStore.getOrCreate(metadataGenerationManager.getPinnedOrCurrentGenerationId(),
                () -> new State(new Registry(instanceNameProvider)));
    }

    protected void ensureInitialized(State state) {
        if (!state.initialized) {
            synchronized (state) {
                if (!state.initialized) {
                    log.debug("Create Index Configurations");
                    initializeIndexDefinitions(state);
                    state.initialized = true;
                }
            }
        }
    }

    protected static class PropertyTrackingInfo {

        protected final Class<?> trackedClassUpdate; //todo change both tracked class to their entity names?
        protected final Class<?> trackedClassDelete;
        protected final String localPropertyName;
        protected final MetaPropertyPath backRefGlobalPropertyUpdate;
        protected final MetaPropertyPath backRefGlobalPropertyDelete;

        private PropertyTrackingInfo(Class<?> trackedClassUpdate,
                                     @Nullable Class<?> trackedClassDelete,
                                     String localPropertyName,
                                     @Nullable MetaPropertyPath backRefGlobalPropertyUpdate,
                                     @Nullable MetaPropertyPath backRefGlobalPropertyDelete) {
            this.trackedClassUpdate = trackedClassUpdate;
            this.trackedClassDelete = trackedClassDelete;
            this.localPropertyName = localPropertyName;
            this.backRefGlobalPropertyUpdate = backRefGlobalPropertyUpdate;
            this.backRefGlobalPropertyDelete = backRefGlobalPropertyDelete;
        }

        public Class<?> getTrackedClassUpdate() {
            return trackedClassUpdate;
        }

        public String getLocalPropertyName() {
            return localPropertyName;
        }

        @Nullable
        public MetaPropertyPath getBackRefGlobalPropertyUpdate() {
            return backRefGlobalPropertyUpdate;
        }

        @Nullable
        public Class<?> getTrackedClassDelete() {
            return trackedClassDelete;
        }

        @Nullable
        public MetaPropertyPath getBackRefGlobalPropertyDelete() {
            return backRefGlobalPropertyDelete;
        }

        @Override
        public String toString() {
            return "PropertyTrackingInfo{" +
                   "trackedClassUpdate=" + trackedClassUpdate +
                   ", trackedClassDelete=" + trackedClassDelete +
                   ", localPropertyName='" + localPropertyName + '\'' +
                   ", backRefGlobalPropertyUpdate=" + backRefGlobalPropertyUpdate +
                   ", backRefGlobalPropertyDelete=" + backRefGlobalPropertyDelete +
                   '}';
        }
    }

    /**
     * Holds the configurations of one metadata generation.
     * <p>
     * A registry is filled before it is published and is not written to afterwards, so the getters hand out the
     * collections themselves: whoever holds one holds a generation that no longer changes.
     */
    protected static class Registry {

        private final InstanceNameProvider instanceNameProvider;

        private final Map<String, IndexConfiguration> indexConfigurationsByEntityName = new HashMap<>();
        private final Map<Class<?>, Map<String, Set<MetaPropertyPath>>> referentiallyAffectedPropertiesForUpdate = new HashMap<>();
        private final Map<Class<?>, Set<MetaPropertyPath>> referentiallyAffectedPropertiesForDelete = new HashMap<>();
        private final Set<Class<?>> registeredEntityClasses = new HashSet<>();

        public Registry(InstanceNameProvider instanceNameProvider) {
            this.instanceNameProvider = instanceNameProvider;
        }

        /**
         * @deprecated Use {@link #Registry(InstanceNameProvider)} instead
         */
        @Deprecated(since = "3.0", forRemoval = true)
        public Registry(InstanceNameProvider instanceNameProvider, MetadataTools metadataTools) {
            this(instanceNameProvider);
        }

        void registerIndexConfiguration(IndexConfiguration indexConfiguration) {
            registerInMainRegistries(indexConfiguration);
            IndexMappingConfiguration mappingConfiguration = indexConfiguration.getMapping();

            mappingConfiguration
                    .getFields()
                    .values()
                    .stream()
                    .filter(f -> !f.isStandalone())
                    .map(MappingFieldDescriptor::getMetaPropertyPath)
                    .forEach(this::processProperty);

            mappingConfiguration
                    .getDisplayedNameDescriptor()
                    .getInstanceNameRelatedProperties()
                    .forEach(this::processProperty);
        }

        @Nullable
        IndexConfiguration getIndexConfigurationByEntityName(String entityName) {
            return indexConfigurationsByEntityName.get(entityName);
        }

        Collection<IndexConfiguration> getIndexConfigurations() {
            return indexConfigurationsByEntityName.values();
        }

        @Nullable
        Map<String, Set<MetaPropertyPath>> getBackRefPropertiesForUpdate(Class<?> entityClass) {
            Map<String, Set<MetaPropertyPath>> properties = referentiallyAffectedPropertiesForUpdate.get(entityClass);
            return properties;
        }

        @Nullable
        Set<MetaPropertyPath> getBackRefPropertiesForDelete(Class<?> entityClass) {
            Set<MetaPropertyPath> properties = referentiallyAffectedPropertiesForDelete.get(entityClass);
            return properties;
        }

        Set<String> getLocalPropertyNamesAffectedByUpdate(Class<?> entityClass) {
            Map<String, Set<MetaPropertyPath>> updateMetadata = referentiallyAffectedPropertiesForUpdate.get(entityClass);
            return updateMetadata == null ? Collections.emptySet() : updateMetadata.keySet();
        }

        Collection<String> getAllIndexedEntities() {
            return indexConfigurationsByEntityName.keySet();
        }

        boolean hasDefinitionForEntity(String entityName) {
            return indexConfigurationsByEntityName.containsKey(entityName);
        }

        boolean isEntityClassRegistered(Class<?> entityClass) {
            return registeredEntityClasses.contains(entityClass);
        }

        private void registerInMainRegistries(IndexConfiguration indexConfiguration) {
            String entityName = indexConfiguration.getEntityName();
            // Duplicates by entity name are already collapsed while the definitions are collected, and index name
            // collisions are dropped by replaceConfigurations, so a configuration reaching here keeps its place.
            indexConfigurationsByEntityName.put(entityName, indexConfiguration);
            registeredEntityClasses.addAll(indexConfiguration.getAffectedEntityClasses());
        }

        private void processProperty(MetaPropertyPath propertyPath) {
            List<MetaPropertyPath> effectiveProperties;
            if (propertyPath.getRange().isClass()) {
                // Extend properties with instance-name-affected properties for simple 'refEntity' field declaration case
                effectiveProperties = extendClassProperty(propertyPath);
            } else {
                effectiveProperties = Collections.singletonList(propertyPath);
            }
            log.debug("Effective properties = {}", effectiveProperties);

            List<PropertyTrackingInfo> propertyTrackingInfoList = effectiveProperties.stream()
                    .flatMap(p -> createPropertyTrackingInfoList(p.getMetaClass(), p).stream())
                    .collect(Collectors.toList());
            log.debug("Properties tracking info = {}", propertyTrackingInfoList);

            propertyTrackingInfoList.forEach(this::processPropertyTrackingInfo);
        }

        private List<MetaPropertyPath> extendClassProperty(MetaPropertyPath propertyPath) {
            Collection<MetaProperty> instanceNameRelatedProperties = instanceNameProvider.getInstanceNameRelatedProperties(
                    propertyPath.getRange().asClass(), true
            );
            log.debug("Instance Name related properties: {}", instanceNameRelatedProperties);
            MetaProperty[] metaProperties = propertyPath.getMetaProperties();

            return instanceNameRelatedProperties.stream()
                    .map(instanceNameRelatedProperty -> {
                        MetaProperty[] extendedPropertyArray = Arrays.copyOf(metaProperties, metaProperties.length + 1);
                        extendedPropertyArray[extendedPropertyArray.length - 1] = instanceNameRelatedProperty;
                        return new MetaPropertyPath(propertyPath.getMetaClass(), extendedPropertyArray);
                    })
                    .collect(Collectors.toList());
        }

        private void processPropertyTrackingInfo(PropertyTrackingInfo trackingInfo) {
            log.debug("Process Property Tracking Info: {}", trackingInfo);
            registerBackRefPropertyForUpdate(trackingInfo);
            registerBackRefPropertyForDelete(trackingInfo);
        }

        private void registerBackRefPropertyForUpdate(PropertyTrackingInfo trackingInfo) {
            Map<String, Set<MetaPropertyPath>> refTrackedProperties = referentiallyAffectedPropertiesForUpdate.computeIfAbsent(
                    trackingInfo.getTrackedClassUpdate(), k -> new HashMap<>()
            );
            Set<MetaPropertyPath> refPropertyPaths = refTrackedProperties.computeIfAbsent(
                    trackingInfo.getLocalPropertyName(), k -> new HashSet<>()
            );
            log.debug("Update info: Tracked Class = {}, Local Property = {}, Back Ref Global Property = {}",
                    trackingInfo.getTrackedClassUpdate(),
                    trackingInfo.getLocalPropertyName(),
                    trackingInfo.getBackRefGlobalPropertyUpdate());
            if (trackingInfo.getBackRefGlobalPropertyUpdate() != null) {
                log.debug("Add Update back-ref property");
                refPropertyPaths.add(trackingInfo.getBackRefGlobalPropertyUpdate());
            }
        }

        private void registerBackRefPropertyForDelete(PropertyTrackingInfo trackingInfo) {
            log.debug("Delete info: Tracked Class = {}, Back Ref Global Property = {}",
                    trackingInfo.getTrackedClassDelete(), trackingInfo.getBackRefGlobalPropertyDelete());
            if (trackingInfo.getTrackedClassDelete() != null && trackingInfo.getBackRefGlobalPropertyDelete() != null) {
                Set<MetaPropertyPath> refTrackedPropertiesDelete =
                        referentiallyAffectedPropertiesForDelete.computeIfAbsent(
                                trackingInfo.getTrackedClassDelete(), k -> new HashSet<>()
                        );
                log.debug("Add Delete back-ref property");
                refTrackedPropertiesDelete.add(trackingInfo.getBackRefGlobalPropertyDelete());
            }
        }

        private List<PropertyTrackingInfo> createPropertyTrackingInfoList(MetaClass rootClass, MetaPropertyPath propertyPath) {
            log.debug("Process property for MetaClass={}: {}", rootClass, propertyPath);
            List<PropertyTrackingInfo> result = new ArrayList<>();

            String trackedLocalPropertyName;
            MetaPropertyPath effectivePropertyPath;
            if (isBelongToEmbedded(propertyPath)) {
                /*
                Skip nested value-property and continue to work with top-level embedded property.
                Keep full name of nested value-property (started from the owner of top-level embedded property)
                to match the changed in Entity Changed Event
                */
                effectivePropertyPath = createShiftedPropertyPath(propertyPath, 1);
                trackedLocalPropertyName = createEmbeddedValuePropertyFullLocalName(propertyPath);
            } else {
                effectivePropertyPath = propertyPath;
                trackedLocalPropertyName = propertyPath.getMetaProperty().getName();
            }

            Class<?> trackedClassUpdate = resolveTrackedClassForUpdateCase(effectivePropertyPath);
            Class<?> trackedClassDelete = resolveTrackedClassForDeleteCase(effectivePropertyPath);
            MetaPropertyPath backRefGlobalPropertyDelete = resolveBackRefPropertyForDeleteCase(propertyPath);
            MetaPropertyPath backRefGlobalPropertyUpdate = resolveBackRefPropertyForUpdateCase(effectivePropertyPath);

            PropertyTrackingInfo propertyTrackingInfo = new PropertyTrackingInfo(
                    trackedClassUpdate,
                    trackedClassDelete,
                    trackedLocalPropertyName,
                    backRefGlobalPropertyUpdate,
                    backRefGlobalPropertyDelete
            );
            result.add(propertyTrackingInfo);
            if (backRefGlobalPropertyUpdate != null) {
                result.addAll(createPropertyTrackingInfoList(rootClass, backRefGlobalPropertyUpdate));
            }

            return result;
        }

        private boolean isBelongToEmbedded(MetaPropertyPath propertyPath) {
            MetaProperty[] metaProperties = propertyPath.getMetaProperties();
            boolean result = false;
            if (metaProperties.length > 1) {
                MetaProperty metaProperty = metaProperties[metaProperties.length - 2];
                result = metaProperty.getType() == MetaProperty.Type.EMBEDDED;
            }
            return result;
        }

        private MetaPropertyPath createShiftedPropertyPath(MetaPropertyPath sourcePropertyPath, int positions) {
            MetaProperty[] metaProperties = sourcePropertyPath.getMetaProperties();
            MetaProperty[] newProperties = Arrays.copyOf(metaProperties, metaProperties.length - positions);
            return new MetaPropertyPath(sourcePropertyPath.getMetaClass(), newProperties);
        }

        private String createEmbeddedValuePropertyFullLocalName(MetaPropertyPath propertyPath) {
            MetaProperty[] metaProperties = propertyPath.getMetaProperties();
            return metaProperties[metaProperties.length - 2].getName() + "." + metaProperties[metaProperties.length - 1].getName();
        }

        private Class<?> resolveTrackedClassForUpdateCase(MetaPropertyPath propertyPath) {
            return propertyPath.getMetaProperty().getDomain().getJavaClass();
        }

        @Nullable
        private Class<?> resolveTrackedClassForDeleteCase(MetaPropertyPath propertyPath) {
            return propertyPath.getRange().isClass() ? propertyPath.getRangeJavaClass() : null;
        }

        @Nullable
        private MetaPropertyPath resolveBackRefPropertyForDeleteCase(MetaPropertyPath propertyPath) {
            return propertyPath.getRange().isClass() ? propertyPath : null;
        }

        @Nullable
        private MetaPropertyPath resolveBackRefPropertyForUpdateCase(MetaPropertyPath propertyPath) {
            return propertyPath.getMetaProperties().length > 1
                    ? createShiftedPropertyPath(propertyPath, 1)
                    : null;
        }
    }
}
