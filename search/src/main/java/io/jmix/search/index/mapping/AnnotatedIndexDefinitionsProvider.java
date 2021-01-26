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
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexDefinition;
import io.jmix.search.index.IndexDefinitionDetector;
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

@Component("search_AnnotatedIndexDefinitionsProvider")
public class AnnotatedIndexDefinitionsProvider {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionsProvider.class);

    protected InstanceNameProvider instanceNameProvider;

    protected final Map<Class<?>, IndexDefinition> indexDefinitions;

    protected final Map<Class<?>, Map<String, Set<MetaPropertyPath>>> referentiallyAffectedPropertiesForUpdate;
    protected final Map<Class<?>, Set<MetaPropertyPath>> referentiallyAffectedPropertiesForDelete;

    @Autowired
    public AnnotatedIndexDefinitionsProvider(JmixModulesClasspathScanner classpathScanner,
                                             AnnotatedIndexDefinitionBuilder builder,
                                             InstanceNameProvider instanceNameProvider) {
        this.instanceNameProvider = instanceNameProvider;

        Set<String> classNames = classpathScanner.getClassNames(IndexDefinitionDetector.class);
        log.info("[IVGA] Create Index Definitions");
        Map<Class<?>, IndexDefinition> tmpIndexDefinitions = new HashMap<>();
        Map<Class<?>, Map<String, Set<MetaPropertyPath>>> tmpReferentiallyAffectedPropertiesForUpdate = new HashMap<>();
        Map<Class<?>, Set<MetaPropertyPath>> tmpReferentiallyAffectedPropertiesForDelete = new HashMap<>();

        //todo refactor
        classNames.stream().map(builder::createIndexDefinition).forEach(indexDefinition -> {
            Class<?> entityClass = indexDefinition.getEntityClass();

            if(tmpIndexDefinitions.containsKey(entityClass)) {
                log.warn("[IVGA] Multiple Index Definitions are detected for entity '{}'", entityClass);
            } else {
                tmpIndexDefinitions.put(entityClass, indexDefinition);
            }

            Collection<MappingFieldDescriptor> fields = indexDefinition.getMapping().getFields().values();
            fields.stream()
                    .filter(f -> {
                        return !f.isStandalone();//todo
                    })
                    .forEach(f -> {
                        List<MetaPropertyPath> extendedProperties;
                        // Extend properties with instance-name-affected properties for simple 'refEntity' field declaration case
                        if(f.getMetaPropertyPath().getRange().isClass()) {
                            Collection<MetaProperty> instanceNameRelatedProperties = instanceNameProvider.getInstanceNameRelatedProperties(
                                    f.getMetaPropertyPath().getRange().asClass(), true
                            );
                            log.info("[IVGA] instanceNameRelatedProperties={}", instanceNameRelatedProperties);
                            MetaProperty[] metaProperties = f.getMetaPropertyPath().getMetaProperties();

                            extendedProperties = instanceNameRelatedProperties.stream()
                                    .map(instanceNameRelatedProperty -> {
                                        MetaProperty[] extendedPropertyArray = Arrays.copyOf(metaProperties, metaProperties.length + 1);
                                        extendedPropertyArray[extendedPropertyArray.length - 1] = instanceNameRelatedProperty;
                                        return new MetaPropertyPath(f.getMetaPropertyPath().getMetaClass(), extendedPropertyArray);
                                    })
                                    .collect(Collectors.toList());

                            log.info("[IVGA] Extended Properties = {}", extendedProperties);
                        } else {
                            extendedProperties = Collections.singletonList(f.getMetaPropertyPath());
                        }

                        List<PropertyTrackingDetail> propertyTrackingDetails = extendedProperties.stream()
                                .flatMap(p -> createPropertyTrackingDetails(p.getMetaClass(), p).stream())
                                .collect(Collectors.toList());
                        log.info("[IVGA] Property Tracking Details = {}", propertyTrackingDetails);

                        propertyTrackingDetails.forEach(trackingDetail -> {
                            log.info("[IVGA] Process Property Tracking Detail: {}", trackingDetail);
                            Map<String, Set<MetaPropertyPath>> refTrackedProperties = tmpReferentiallyAffectedPropertiesForUpdate.computeIfAbsent(
                                    trackingDetail.getTrackedClassUpdate(), k -> new HashMap<>()
                            );
                            Set<MetaPropertyPath> refPropertyPaths = refTrackedProperties.computeIfAbsent(
                                    trackingDetail.getLocalPropertyName(), k -> new HashSet<>()
                            );
                            log.info("[IVGA] Update: Tracked Class = {}, Local Property = {}, Back Ref Global Property = {}",
                                    trackingDetail.getTrackedClassUpdate(),
                                    trackingDetail.getLocalPropertyName(),
                                    trackingDetail.getBackRefGlobalPropertyUpdate());
                            if(trackingDetail.getBackRefGlobalPropertyUpdate() != null) {
                                log.info("[IVGA] Add update back-ref property");
                                refPropertyPaths.add(trackingDetail.getBackRefGlobalPropertyUpdate());
                            }

                            log.info("[IVGA] Delete: Tracked Class = {}, Back Ref Global Property = {}",
                                    trackingDetail.getTrackedClassDelete(), trackingDetail.getBackRefGlobalPropertyDelete());
                            if(trackingDetail.getTrackedClassDelete() != null && trackingDetail.getBackRefGlobalPropertyDelete() != null) {
                                Set<MetaPropertyPath> refTrackedPropertiesDelete =
                                        tmpReferentiallyAffectedPropertiesForDelete.computeIfAbsent(
                                                trackingDetail.getTrackedClassDelete(), k -> new HashSet<>()
                                        );
                                log.info("[IVGA] Add delete back-ref property");
                                refTrackedPropertiesDelete.add(trackingDetail.getBackRefGlobalPropertyDelete());
                            }
                        });

                    });
        });

        indexDefinitions = tmpIndexDefinitions;
        referentiallyAffectedPropertiesForDelete = tmpReferentiallyAffectedPropertiesForDelete;
        referentiallyAffectedPropertiesForUpdate = tmpReferentiallyAffectedPropertiesForUpdate;

        log.info("[IVGA] Initialized indexDefinitions = {}", indexDefinitions);
        log.info("[IVGA] Initialized referentiallyAffectedPropertiesForUpdate = {}", referentiallyAffectedPropertiesForUpdate);
        log.info("[IVGA] Initialized referentiallyAffectedPropertiesForDelete = {}", referentiallyAffectedPropertiesForDelete);
    }

    public Collection<IndexDefinition> getIndexDefinitions() {
        return indexDefinitions.values();
    }

    @Nullable
    public IndexDefinition getIndexDefinitionForEntityClass(Class<?> entityClass) {
        return indexDefinitions.get(entityClass);
    }

    public boolean isDirectlyIndexed(Class<?> entityClass) {
        return indexDefinitions.containsKey(entityClass);
    }

    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForUpdate(Class<?> entityClass, Set<String> changedProperties) {
        log.info("[IVGA] Get dependencies for class {} with changed properties: {}", entityClass, changedProperties);
        Map<String, Set<MetaPropertyPath>> backRefProperties = referentiallyAffectedPropertiesForUpdate.get(entityClass);
        if(MapUtils.isEmpty(backRefProperties)) {
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

    public Map<MetaClass, Set<MetaPropertyPath>> getDependenciesMetaDataForDelete(Class<?> deletedEntityClass) {
        log.info("[IVGA] Get delete dependencies for class {}", deletedEntityClass);
        Set<MetaPropertyPath> backRefPropertiesDelete = referentiallyAffectedPropertiesForDelete.get(deletedEntityClass);
        if(CollectionUtils.isEmpty(backRefPropertiesDelete)) {
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

    protected List<PropertyTrackingDetail> createPropertyTrackingDetails(MetaClass rootClass, MetaPropertyPath propertyPath) {
        log.info("[IVGA] Process property for MetaClass={}: {}", rootClass, propertyPath);
        List<PropertyTrackingDetail> result = new ArrayList<>();
        String localPropertyName = propertyPath.getMetaProperty().getName();
        MetaClass domain = propertyPath.getMetaProperty().getDomain();
        log.info("[IVGA] Create Property Tracking Detail: Class={}, LocalProperty={}, GlobalProperty={}",
                domain.getJavaClass(), localPropertyName, propertyPath);
        if(propertyPath.getMetaProperties().length == 1) {
            result.add(new PropertyTrackingDetail( //todo refactor constructor
                    domain.getJavaClass(),
                    propertyPath.getRange().isClass() ? propertyPath.getRangeJavaClass() : null,
                    localPropertyName,
                    null,
                    propertyPath.getRange().isClass() ? propertyPath : null));
        } else {
            MetaProperty[] metaProperties = propertyPath.getMetaProperties();
            MetaProperty[] newProperties = Arrays.copyOf(metaProperties, metaProperties.length - 1);
            MetaPropertyPath refPropertyPath = new MetaPropertyPath(rootClass, newProperties);
            result.add(new PropertyTrackingDetail(
                    domain.getJavaClass(),
                    propertyPath.getRange().isClass() ? propertyPath.getRangeJavaClass() : null,
                    localPropertyName,
                    refPropertyPath,
                    propertyPath.getRange().isClass() ? propertyPath : null));
            result.addAll(createPropertyTrackingDetails(rootClass, refPropertyPath));
        }

        log.info("[IVGA] Input: RootClass={}, PropertyPath={}. Result={}", rootClass, propertyPath, result);

        return result;
    }

    protected static class PropertyTrackingDetail {

        protected final Class<?> trackedClassUpdate;
        protected final Class<?> trackedClassDelete;
        protected final String localPropertyName;
        protected final MetaPropertyPath backRefGlobalPropertyUpdate;
        protected final MetaPropertyPath backRefGlobalPropertyDelete;

        public PropertyTrackingDetail(Class<?> trackedClassUpdate,
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
            return "PropertyGraphItem{" +
                    "trackedClassUpdate=" + trackedClassUpdate +
                    ", trackedClassDelete=" + trackedClassDelete +
                    ", localPropertyName='" + localPropertyName + '\'' +
                    ", backRefGlobalPropertyUpdate=" + backRefGlobalPropertyUpdate +
                    ", backRefGlobalPropertyDelete=" + backRefGlobalPropertyDelete +
                    '}';
        }
    }
}
