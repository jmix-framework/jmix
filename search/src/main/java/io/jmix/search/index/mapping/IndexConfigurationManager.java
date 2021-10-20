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
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.processor.AnnotatedIndexDefinitionProcessor;
import io.jmix.search.index.mapping.processor.IndexDefinitionDetector;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("search_IndexConfigurationManager")
public class IndexConfigurationManager {

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationManager.class);

    protected final Registry registry;

    @Autowired
    public IndexConfigurationManager(JmixModulesClasspathScanner classpathScanner,
                                     AnnotatedIndexDefinitionProcessor indexDefinitionProcessor,
                                     InstanceNameProvider instanceNameProvider,
                                     IndexDefinitionDetector indexDefinitionDetector,
                                     MetadataTools metadataTools) {
        Class<? extends IndexDefinitionDetector> detectorClass = indexDefinitionDetector.getClass();
        Set<String> classNames = classpathScanner.getClassNames(detectorClass);
        log.debug("Create Index Configurations");

        Registry registry = new Registry(instanceNameProvider, metadataTools);
        classNames.stream()
                .map(indexDefinitionProcessor::createIndexConfiguration)
                .forEach(registry::registerIndexConfiguration);
        this.registry = registry;
    }

    /**
     * Gets all {@link IndexConfiguration} registered in application
     *
     * @return all {@link IndexConfiguration}
     */
    public Collection<IndexConfiguration> getAllIndexConfigurations() {
        return registry.getIndexConfigurations();
    }

    /**
     * Gets {@link IndexConfiguration} registered for provided entity name.
     * Throws {@link IllegalArgumentException} if there is no configuration for provided entity name.
     *
     * @param entityName entity name.
     * @return {@link IndexConfiguration}
     */
    public IndexConfiguration getIndexConfigurationByEntityName(String entityName) {
        IndexConfiguration indexConfiguration = registry.getIndexConfigurationByEntityName(entityName);
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
        return Optional.ofNullable(registry.getIndexConfigurationByEntityName(entityName));
    }

    /**
     * Gets {@link IndexConfiguration} registered for provided index name.
     * Throws {@link IllegalArgumentException} if there is no configuration for provided index name.
     *
     * @param indexName index name
     * @return {@link IndexConfiguration}
     */
    public IndexConfiguration getIndexConfigurationByIndexName(String indexName) {
        IndexConfiguration indexConfiguration = registry.getIndexConfigurationByIndexName(indexName);
        if (indexConfiguration == null) {
            throw new IllegalArgumentException("There is no configuration for index name '" + indexName + "'");
        }
        return indexConfiguration;
    }

    /**
     * Gets optional {@link IndexConfiguration} registered for provided index name.
     *
     * @param indexName index name
     * @return optional {@link IndexConfiguration}
     */
    public Optional<IndexConfiguration> getIndexConfigurationByIndexNameOpt(String indexName) {
        return Optional.ofNullable(registry.getIndexConfigurationByIndexName(indexName));
    }

    public Collection<String> getAllIndexedEntities() {
        return registry.getAllIndexedEntities();
    }

    /**
     * Checks if provided entity is declared to be indexed directly (not as a part of another entity).
     *
     * @param entityName entity name
     * @return true if entity is indexed, false otherwise
     */
    public boolean isDirectlyIndexed(String entityName) {
        return registry.hasDefinitionForEntity(entityName);
    }

    /**
     * Checks if provided entity is involved in index process directly or as a part of another entity.
     *
     * @param entityClass entity java class
     * @return true if entity is involved in index process, false otherwise
     */
    public boolean isAffectedEntityClass(Class<?> entityClass) {
        return registry.isEntityClassRegistered(entityClass);
    }

    /**
     * Gets local property names of provided entity involved into index update process
     *
     * @param entityClass entity class
     * @return set of property names
     */
    public Set<String> getLocalPropertyNamesAffectedByUpdate(Class<?> entityClass) {
        return registry.getLocalPropertyNamesAffectedByUpdate(entityClass);
    }

    /**
     * Gets metadata of entities dependent on updated main entity and its changed properties.
     *
     * @param entityClass       java class of main entity
     * @param changedProperties changed property of main entity
     * @return dependent entities grouped by their {@link MetaClass}.
     * For every meta class group there are set of properties representing dependency-to-main references
     */
    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForUpdate(Class<?> entityClass, Set<String> changedProperties) {
        log.debug("Get dependencies metadata for class {} with changed properties: {}", entityClass, changedProperties);
        Map<String, Set<MetaPropertyPath>> backRefProperties = registry.getBackRefPropertiesForUpdate(entityClass);
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
     * Gets metadata of entities dependent on deleted main entity.
     *
     * @param deletedEntityClass java class of main entity
     * @return dependent entities grouped by their {@link MetaClass}.
     * For every meta class group there are set of properties representing dependency-to-main references
     */
    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForDelete(Class<?> deletedEntityClass) {
        log.debug("Get dependencies metadata for class {} deletion", deletedEntityClass);
        Set<MetaPropertyPath> backRefPropertiesDelete = registry.getBackRefPropertiesForDelete(deletedEntityClass);
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

    private static class Registry {
        private final InstanceNameProvider instanceNameProvider;
        private final MetadataTools metadataTools;

        private final Map<String, IndexConfiguration> indexConfigurationsByEntityName = new HashMap<>();
        private final Map<String, IndexConfiguration> indexConfigurationsByIndexName = new HashMap<>();
        private final Map<Class<?>, Map<String, Set<MetaPropertyPath>>> referentiallyAffectedPropertiesForUpdate = new HashMap<>();
        private final Map<Class<?>, Set<MetaPropertyPath>> referentiallyAffectedPropertiesForDelete = new HashMap<>();
        private final Set<Class<?>> registeredEntityClasses = new HashSet<>();

        public Registry(InstanceNameProvider instanceNameProvider, MetadataTools metadataTools) {
            this.instanceNameProvider = instanceNameProvider;
            this.metadataTools = metadataTools;
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

        @Nullable
        IndexConfiguration getIndexConfigurationByIndexName(String indexName) {
            return indexConfigurationsByIndexName.get(indexName);
        }

        Collection<IndexConfiguration> getIndexConfigurations() {
            return indexConfigurationsByEntityName.values();
        }

        @Nullable
        Map<String, Set<MetaPropertyPath>> getBackRefPropertiesForUpdate(Class<?> entityClass) {
            return referentiallyAffectedPropertiesForUpdate.get(entityClass);
        }

        @Nullable
        Set<MetaPropertyPath> getBackRefPropertiesForDelete(Class<?> entityClass) {
            return referentiallyAffectedPropertiesForDelete.get(entityClass);
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
            if (indexConfigurationsByEntityName.containsKey(entityName)) {
                log.warn("Multiple Index Definitions are detected for entity '{}'", entityName);
            } else {
                indexConfigurationsByEntityName.put(entityName, indexConfiguration);
                indexConfigurationsByIndexName.put(indexConfiguration.getIndexName(), indexConfiguration);
                registeredEntityClasses.addAll(indexConfiguration.getAffectedEntityClasses());
            }
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
                result = metadataTools.isEmbedded(metaProperty);
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
