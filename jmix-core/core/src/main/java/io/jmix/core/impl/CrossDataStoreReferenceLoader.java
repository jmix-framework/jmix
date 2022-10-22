/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * INTERNAL.
 * Populates references to entities from different data stores.
 */
@Component("core_CrossDataStoreReferenceLoader")
@Scope("prototype")
public class CrossDataStoreReferenceLoader {

    private static final Logger log = LoggerFactory.getLogger(CrossDataStoreReferenceLoader.class);

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private CoreProperties properties;

    private MetaClass metaClass;

    private FetchPlan fetchPlan;
    private boolean joinTransaction;

    public CrossDataStoreReferenceLoader(MetaClass metaClass, FetchPlan fetchPlan, boolean joinTransaction) {
        Preconditions.checkNotNullArgument(metaClass, "metaClass is null");
        Preconditions.checkNotNullArgument(fetchPlan, "fetchPlan is null");
        this.metaClass = metaClass;
        this.fetchPlan = fetchPlan;
        this.joinTransaction = joinTransaction;
    }

    public Map<Class<?>, List<CrossDataStoreProperty>> getCrossPropertiesMap() {
        Map<Class<?>, List<CrossDataStoreProperty>> crossPropertiesMap = new HashMap<>();
        traverseFetchPlan(fetchPlan, crossPropertiesMap, Sets.newIdentityHashSet());
        return crossPropertiesMap;
    }

    private void traverseFetchPlan(FetchPlan fetchPlan, Map<Class<?>, List<CrossDataStoreProperty>> crossPropertiesMap, Set<FetchPlan> visited) {
        if (visited.contains(fetchPlan))
            return;
        visited.add(fetchPlan);

        String storeName = metaClass.getStore().getName();

        Class<?> entityClass = fetchPlan.getEntityClass();
        for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metadata.getClass(entityClass).getProperty(fetchPlanProperty.getName());
            if (metaProperty.getRange().isClass()) {
                MetaClass propertyMetaClass = metaProperty.getRange().asClass();
                if (!Objects.equals(propertyMetaClass.getStore().getName(), storeName)) {
                    List<String> dependsOnProperties = metadataTools.getDependsOnProperties(metaProperty);
                    if (dependsOnProperties.size() == 0) {
                        continue;
                    }
                    if (dependsOnProperties.size() > 1) {
                        log.warn("More than 1 property is defined for attribute {} in DependsOnProperty annotation, skip handling cross-datastore reference", metaProperty);
                        continue;
                    }
                    List<CrossDataStoreProperty> crossProperties = crossPropertiesMap.computeIfAbsent(entityClass, k -> new ArrayList<>());
                    if (crossProperties.stream().noneMatch(aProp -> aProp.property == metaProperty))
                        crossProperties.add(new CrossDataStoreProperty(metaProperty, fetchPlanProperty));
                }
                FetchPlan propertyFetchPlan = fetchPlanProperty.getFetchPlan();
                if (propertyFetchPlan != null) {
                    traverseFetchPlan(propertyFetchPlan, crossPropertiesMap, visited);
                }
            }
        }
    }

    public void processEntities(Collection entities) {
        Map<Class<?>, List<CrossDataStoreProperty>> crossPropertiesMap = getCrossPropertiesMap();
        if (crossPropertiesMap.isEmpty())
            return;

        Set<Object> affectedEntities = getAffectedEntities(entities, crossPropertiesMap);
        if (affectedEntities.isEmpty())
            return;

        List<EntityCrossDataStoreProperty> entityCrossDataStorePropertyList = new ArrayList<>();
        for (Object affectedEntity : affectedEntities) {
            for (CrossDataStoreProperty crossDataStoreProperty : crossPropertiesMap.get(affectedEntity.getClass())) {
                entityCrossDataStorePropertyList.add(new EntityCrossDataStoreProperty(affectedEntity, crossDataStoreProperty));
            }
        }
        if (entityCrossDataStorePropertyList.size() == 1) {
            loadOne(entityCrossDataStorePropertyList.get(0));
        } else {
            entityCrossDataStorePropertyList.stream()
                    .collect(Collectors.groupingBy(EntityCrossDataStoreProperty::getCrossProp))
                    .forEach((ap, eapList) ->
                            loadMany(ap, eapList.stream().map(eap -> eap.entity).collect(Collectors.toList()))
                    );
        }
    }

    private Set<Object> getAffectedEntities(Collection entities,
                                            Map<Class<?>, List<CrossDataStoreProperty>> crossPropertiesMap) {
        Set<Object> resultSet = new HashSet<>();
        for (Object entity : entities) {
            metadataTools.traverseAttributesByFetchPlan(fetchPlan, entity, new EntityAttributeVisitor() {
                @Override
                public void visit(Object entity, MetaProperty property) {
                    List<CrossDataStoreProperty> crossProperties = crossPropertiesMap.get(entity.getClass());
                    if (crossProperties != null) {
                        crossProperties.stream()
                                .filter(ap -> ap.property == property)
                                .forEach(ap -> {
                                    if (EntityValues.getValue(entity, ap.relatedPropertyName) != null) {
                                        resultSet.add(entity);
                                    }
                                });
                    }
                }

                @Override
                public boolean skip(MetaProperty property) {
                    return !property.getRange().isClass();
                }
            });
        }
        return resultSet;
    }

    private void loadOne(EntityCrossDataStoreProperty entityCrossDataStoreProperty) {
        Object entity = entityCrossDataStoreProperty.entity;
        CrossDataStoreProperty aProp = entityCrossDataStoreProperty.crossProp;
        Object id = EntityValues.getValue(entity, aProp.relatedPropertyName);

        LoadContext<?> loadContext = new LoadContext<>(aProp.property.getRange().asClass())
                .setId(id);
        if (aProp.fetchPlanProperty.getFetchPlan() != null)
            loadContext.setFetchPlan(aProp.fetchPlanProperty.getFetchPlan());
        loadContext.setJoinTransaction(joinTransaction);
        Object relatedEntity = dataManager.load(loadContext);
        EntityValues.setValue(entity, aProp.property.getName(), relatedEntity);
    }

    private void loadMany(CrossDataStoreProperty crossDataStoreProperty, List<Object> entities) {
        int offset = 0, limit = properties.getCrossDataStoreReferenceLoadingBatchSize();
        while (true) {
            int end = offset + limit;
            List<Object> batch = entities.subList(offset, Math.min(end, entities.size()));
            loadBatch(crossDataStoreProperty, batch);
            if (end >= entities.size())
                break;
            else
                offset += limit;
        }
    }

    private void loadBatch(CrossDataStoreProperty crossDataStoreProperty, List<Object> entities) {
        List<Object> idList = entities.stream()
                .map(e -> EntityValues.getValue(e, crossDataStoreProperty.relatedPropertyName))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (idList.isEmpty())
            return;

        MetaClass cdsrMetaClass = crossDataStoreProperty.property.getRange().asClass();
        LoadContext<?> loadContext = new LoadContext<>(cdsrMetaClass);

        if (metadataTools.isJpa(crossDataStoreProperty.property)) {
            // Don't use standard loading by ids for JPA entities because AbstractDataStore throws exception
            // if not all requested entities are loaded, see checkAndReorderLoadedEntities()
            MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(cdsrMetaClass);
            if (primaryKeyProperty == null || !primaryKeyProperty.getRange().isClass()) {
                String queryString = String.format(
                        "select e from %s e where e.%s in :idList", cdsrMetaClass, crossDataStoreProperty.primaryKeyName);
                loadContext.setQuery(new LoadContext.Query(queryString).setParameter("idList", idList));
            } else {
                // composite key entity
                StringBuilder sb = new StringBuilder("select e from ");
                sb.append(cdsrMetaClass).append(" e where ");

                MetaClass idMetaClass = primaryKeyProperty.getRange().asClass();
                for (Iterator<MetaProperty> it = idMetaClass.getProperties().iterator(); it.hasNext(); ) {
                    MetaProperty property = it.next();
                    sb.append("e.").append(crossDataStoreProperty.primaryKeyName).append(".").append(property.getName());
                    sb.append(" in :list_").append(property.getName());
                    if (it.hasNext())
                        sb.append(" and ");
                }
                LoadContext.Query query = new LoadContext.Query(sb.toString());
                for (MetaProperty property : idMetaClass.getProperties()) {
                    List<Object> propList = idList.stream()
                            .map(o -> EntityValues.getValue(o, property.getName()))
                            .collect(Collectors.toList());
                    query.setParameter("list_" + property.getName(), propList);
                }
                loadContext.setQuery(query);
            }
        } else {
            // A custom datastore based on AbstractDataStore can override checkAndReorderLoadedEntities() if needed
            loadContext.setIds(idList);
        }

        loadContext.setFetchPlan(crossDataStoreProperty.fetchPlanProperty.getFetchPlan());
        loadContext.setJoinTransaction(joinTransaction);

        List<?> loadedEntities = dataManager.loadList(loadContext);

        for (Object entity : entities) {
            Object relatedPropertyValue = EntityValues.getValue(entity, crossDataStoreProperty.relatedPropertyName);
            loadedEntities.stream()
                    .filter(e -> {
                        Object id = EntityValues.getId(e);
                        assert id != null;
                        return id.equals(relatedPropertyValue);
                    })
                    .findAny()
                    .ifPresent(e -> EntityValues.setValue(entity, crossDataStoreProperty.property.getName(), e)
                    );
        }
    }

    private static class EntityCrossDataStoreProperty {

        private final Object entity;
        public final CrossDataStoreProperty crossProp;

        public EntityCrossDataStoreProperty(Object entity, CrossDataStoreProperty crossDataStoreProperty) {
            this.entity = entity;
            this.crossProp = crossDataStoreProperty;
        }

        public CrossDataStoreProperty getCrossProp() {
            return crossProp;
        }

        @Override
        public String toString() {
            return entity + " -> " + crossProp;
        }
    }

    public class CrossDataStoreProperty {

        public final MetaProperty property;
        public final FetchPlanProperty fetchPlanProperty;
        public final String relatedPropertyName;
        public final String primaryKeyName;

        public CrossDataStoreProperty(MetaProperty metaProperty, FetchPlanProperty fetchPlanProperty) {
            this.property = metaProperty;
            this.fetchPlanProperty = fetchPlanProperty;

            List<String> dependsOnProperties = metadataTools.getDependsOnProperties(property);
            relatedPropertyName = dependsOnProperties.get(0);

            String pkName = metadataTools.getPrimaryKeyName(property.getRange().asClass());
            primaryKeyName = pkName != null
                    ? pkName
                    : "id"; // sensible default for non-persistent entities
        }

        @Override
        public String toString() {
            return "CrossDataStoreProperty{" + property + "}";
        }
    }
}
