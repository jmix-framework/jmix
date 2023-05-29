/*
 * Copyright 2021 Haulmont.
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
package io.jmix.audit.snapshot.impl;

import io.jmix.audit.snapshot.EntityDifferenceManager;
import io.jmix.audit.snapshot.EntitySnapshotManager;
import io.jmix.audit.snapshot.model.*;
import io.jmix.core.*;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Component("audit_EntityDifferenceManager")
public class EntityDifferenceManagerImpl implements EntityDifferenceManager {

    private static final Logger log = LoggerFactory.getLogger(EntityDifferenceManagerImpl.class);

    private final EntitySnapshotManager entitySnapshotManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;
    private final ExtendedEntities extendedEntities;
    private final MetadataTools metadataTools;
    private final InstanceNameProvider instanceNameProvider;

    public EntityDifferenceManagerImpl(EntitySnapshotManager entitySnapshotManager,
                                 FetchPlans fetchPlans,
                                 Metadata metadata,
                                 ExtendedEntities extendedEntities, MetadataTools metadataTools, InstanceNameProvider instanceNameProvider) {
        this.entitySnapshotManager = entitySnapshotManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.metadataTools = metadataTools;
        this.instanceNameProvider = instanceNameProvider;
    }

    @Override
    public EntityDifferenceModel getDifference(@Nullable EntitySnapshotModel first, @Nullable EntitySnapshotModel second) {
        // Sort snapshots by date, first - old, second - new
        long firstTime = 0;
        if (first != null && first.getSnapshotDate() != null)
            firstTime = first.getSnapshotDate().getTime();

        long secondTime = 0;
        if (second != null && second.getSnapshotDate() != null)
            secondTime = second.getSnapshotDate().getTime();

        if (secondTime < firstTime) {
            EntitySnapshotModel snapshot = first;
            first = second;
            second = snapshot;
        }

        checkNotNull(second, "Diff could not be create for null snapshot");

        // Extract fetchPlans
        FetchPlan firstFetchPlan = first != null ? entitySnapshotManager.extractFetchPlan(first) : null;
        FetchPlan secondFetchPlan = entitySnapshotManager.extractFetchPlan(second);

        // Get fetchPlan for diff
        FetchPlan diffFetchPlan;
        if (firstFetchPlan != null) {
            diffFetchPlan = intersectFetchPlans(firstFetchPlan, secondFetchPlan);
        } else {
            diffFetchPlan = secondFetchPlan;
        }

        // Diff
        return getDifferenceByFetchPlan(first, second, diffFetchPlan);
    }

    private EntityDifferenceModel getDifferenceByFetchPlan(
            @Nullable EntitySnapshotModel first,
            @Nullable EntitySnapshotModel second, FetchPlan diffFetchPlan) {
        EntityDifferenceModel result = metadata.create(EntityDifferenceModel.class);
        result.setDiffFetchPlan(diffFetchPlan);
        result.setBeforeSnapshot(first);
        result.setAfterSnapshot(second);

        if (!diffFetchPlan.getProperties().isEmpty()) {
            Object firstEntity = first != null ? entitySnapshotManager.extractEntity(first) : null;
            Object secondEntity = entitySnapshotManager.extractEntity(second);

            result.setBeforeEntity(firstEntity);
            result.setAfterEntity(secondEntity);

            Stack<Object> diffBranch = new Stack<>();
            if (secondEntity != null) {
                diffBranch.push(secondEntity);
            }

            List<EntityPropertyDifferenceModel> propertyDiffs = getPropertyDiffs(diffFetchPlan, firstEntity, secondEntity, diffBranch);
            result.setPropertyDiffs(propertyDiffs);
        }
        return result;
    }

    /**
     * Get diffs for entity properties
     *
     * @param diffFetchPlan FetchPlan
     * @param firstEntity   First entity
     * @param secondEntity  Second entity
     * @param diffBranch    Diff branch
     * @return Diff list
     */
    private List<EntityPropertyDifferenceModel> getPropertyDiffs(FetchPlan diffFetchPlan,
                                                                 @Nullable Object firstEntity,
                                                                 @Nullable Object secondEntity,
                                                                 Stack<Object> diffBranch) {
        List<EntityPropertyDifferenceModel> propertyDiffs = new LinkedList<>();

        MetaClass fetchPlanMetaClass = metadata.getSession().getClass(diffFetchPlan.getEntityClass());
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(fetchPlanMetaClass);

        Collection<MetaPropertyPath> metaProperties = metadataTools.getFetchPlanPropertyPaths(diffFetchPlan, metaClass);

        for (MetaPropertyPath metaPropertyPath : metaProperties) {
            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

            if (metadataTools.isJpa(metaProperty) && !metadataTools.isSystem(metaProperty)) {
                FetchPlanProperty fetchPlanProperty = diffFetchPlan.getProperty(metaProperty.getName());

                Object firstValue = firstEntity != null ? EntityValues.getValue(firstEntity,metaPropertyPath.toString()) : null;
                Object secondValue = secondEntity != null ? EntityValues.getValue(secondEntity,metaPropertyPath.toString()) : null;

                if (fetchPlanProperty == null) {
                    throw new RuntimeException("Fetch plan property must not be null");
                }
                EntityPropertyDifferenceModel diff = getPropertyDifference(firstValue, secondValue, metaProperty, fetchPlanProperty, diffBranch);
                if (diff != null)
                    propertyDiffs.add(diff);
            }
        }

        Comparator<EntityPropertyDifferenceModel> comparator = Comparator.comparing(EntityPropertyDifferenceModel::getName);
        Collections.sort(propertyDiffs, comparator);

        return propertyDiffs;
    }

    /**
     * Return difference between property values
     *
     * @param firstValue        First value
     * @param secondValue       Second value
     * @param metaProperty      Meta Property
     * @param fetchPlanProperty FetchPlan property
     * @param diffBranch        Branch with passed diffs
     * @return Diff
     */
    @Nullable
    private EntityPropertyDifferenceModel getPropertyDifference(@Nullable Object firstValue, @Nullable Object secondValue,
                                                                MetaProperty metaProperty, FetchPlanProperty fetchPlanProperty,
                                                                Stack<Object> diffBranch) {
        EntityPropertyDifferenceModel propertyDiff = null;

        Range range = metaProperty.getRange();
        if (range.isDatatype() || range.isEnum()) {
            if (!Objects.equals(firstValue, secondValue)) {
                EntityBasicPropertyDifferenceModel basicPropertyDiff = metadata.create(EntityBasicPropertyDifferenceModel.class);
                basicPropertyDiff.setBeforeValue(firstValue);
                basicPropertyDiff.setAfterValue(secondValue);
                basicPropertyDiff.setMetaProperty(metaProperty);
                propertyDiff = basicPropertyDiff;
            }
        } else if (range.getCardinality().isMany()) {
            propertyDiff = getCollectionDiff(firstValue, secondValue, fetchPlanProperty, metaProperty, diffBranch);
        } else if (range.isClass()) {
            propertyDiff = getClassDiff(firstValue, secondValue, fetchPlanProperty, metaProperty, diffBranch);
        }

        return propertyDiff;
    }

    @Nullable
    private EntityPropertyDifferenceModel getCollectionDiff(@Nullable Object firstValue, @Nullable Object secondValue,
                                                            FetchPlanProperty fetchPlanProperty, MetaProperty metaProperty,
                                                            Stack<Object> diffBranch) {
        EntityPropertyDifferenceModel propertyDiff = null;

        Collection<Entity> addedEntities = new LinkedList<>();
        Collection<Entity> removedEntities = new LinkedList<>();
        Collection<Pair<Entity, Entity>> modifiedEntities = new LinkedList<>();

        // collection
        Collection firstCollection = firstValue == null ? Collections.emptyList() : (Collection) firstValue;
        Collection secondCollection = secondValue == null ? Collections.emptyList() : (Collection) secondValue;

        // added or modified
        for (Object item : secondCollection) {
            Entity secondEntity = (Entity) item;
            Entity firstEntity = getRelatedItem(firstCollection, secondEntity);
            if (firstEntity == null)
                addedEntities.add(secondEntity);
            else
                modifiedEntities.add(new Pair<>(firstEntity, secondEntity));
        }

        // removed
        for (Object item : firstCollection) {
            Entity firstEntity = (Entity) item;
            Entity secondEntity = getRelatedItem(secondCollection, firstEntity);
            if (secondEntity == null)
                removedEntities.add(firstEntity);
        }

        boolean changed = !(addedEntities.isEmpty() && removedEntities.isEmpty() && modifiedEntities.isEmpty());
        if (changed) {
            EntityCollectionPropertyDifferenceModel diff = metadata.create(EntityCollectionPropertyDifferenceModel.class);
            diff.setMetaProperty(metaProperty);

            for (Entity entity : addedEntities) {
                EntityPropertyDifferenceModel addedDiff = getClassDiff(null, entity, fetchPlanProperty, metaProperty, diffBranch);
                if (addedDiff != null) {
                    addedDiff.setName(instanceNameProvider.getInstanceName(entity));
                    addedDiff.setItemState(EntityPropertyDifferenceModel.ItemState.Added);
                    diff.getAddedEntities().add(addedDiff);
                }
            }
            // check modified
            for (Pair<Entity, Entity> entityPair : modifiedEntities) {
                EntityPropertyDifferenceModel modifiedDiff = getClassDiff(entityPair.getFirst(), entityPair.getSecond(),
                        fetchPlanProperty, metaProperty, diffBranch);
                if (modifiedDiff != null) {
                    modifiedDiff.setName(instanceNameProvider.getInstanceName(entityPair.getSecond()));
                    modifiedDiff.setItemState(EntityPropertyDifferenceModel.ItemState.Modified);
                    diff.getModifiedEntities().add(modifiedDiff);
                }
            }
            // check removed
            for (Entity entity : removedEntities) {
                EntityPropertyDifferenceModel removedDiff = getClassDiff(entity, null, fetchPlanProperty, metaProperty, diffBranch);
                if (removedDiff != null) {
                    removedDiff.setName(instanceNameProvider.getInstanceName(entity));
                    removedDiff.setItemState(EntityPropertyDifferenceModel.ItemState.Removed);
                    diff.getRemovedEntities().add(removedDiff);
                }
            }

            boolean empty = diff.getAddedEntities().isEmpty()
                    && diff.getModifiedEntities().isEmpty()
                    && diff.getRemovedEntities().isEmpty();
            if (!empty)
                propertyDiff = diff;
        }
        return propertyDiff;
    }

    @Nullable
    private EntityPropertyDifferenceModel getClassDiff(@Nullable Object firstValue, @Nullable Object secondValue,
                                              FetchPlanProperty fetchPlanProperty, MetaProperty metaProperty,
                                              Stack<Object> diffBranch) {
        EntityPropertyDifferenceModel propertyDiff = null;
        if (fetchPlanProperty.getFetchPlan() != null) {
            // check exist value in diff branch
            if (!diffBranch.contains(secondValue)) {

                if (secondValue != null) {
                    // added or modified
                    propertyDiff = generateClassDiffFor(secondValue, firstValue, secondValue,
                            fetchPlanProperty, metaProperty, diffBranch);
                } else {
                    if (firstValue != null) {
                        // removed or set null
                        propertyDiff = generateClassDiffFor(firstValue, firstValue, null /*secondValue*/,
                                fetchPlanProperty, metaProperty, diffBranch);
                    }
                }
            }
        } else {
            if ((firstValue != null) || (secondValue != null))
                log.debug("Not null values for (null) fetchPlan ignored, property: " + metaProperty.getName() +
                        "in class " + (metaProperty.getDeclaringClass() != null ?
                        metaProperty.getDeclaringClass().getCanonicalName() : ""));
        }
        return propertyDiff;
    }

    @Nullable
    private Entity getRelatedItem(Collection collection, Entity entity) {
        for (Object item : collection) {
            Entity itemEntity = (Entity) item;

            Object entityId = EntityValues.getId(entity);
            if (entityId != null && entityId.equals(EntityValues.getId(itemEntity)))
                return itemEntity;
        }
        return null;
    }

    /**
     * Generate class difference for selected not null object
     *
     * @param diffObject   Object
     * @param firstValue   First value
     * @param secondValue  Second value
     * @param fetchPlanProperty FetchPlan property
     * @param metaProperty Meta property
     * @param diffBranch   Diff branch
     * @return Property difference
     */
    @Nullable
    private EntityPropertyDifferenceModel generateClassDiffFor(Object diffObject,
                                                               @Nullable Object firstValue, @Nullable Object secondValue,
                                                               FetchPlanProperty fetchPlanProperty, MetaProperty metaProperty,
                                                               Stack<Object> diffBranch) {
        // link
        boolean isLinkChange = !Objects.equals(firstValue, secondValue);
        isLinkChange = !(EntitySystemAccess.isEmbeddable(diffObject)) && isLinkChange;

        EntityClassPropertyDifferenceModel classPropertyDiff = metadata.create(EntityClassPropertyDifferenceModel.class);
        classPropertyDiff.setAfterValue(firstValue);
        classPropertyDiff.setBeforeValue(secondValue);
        classPropertyDiff.setMetaProperty(metaProperty);
        classPropertyDiff.setLinkChange(isLinkChange);

        boolean isInternalChange = false;
        diffBranch.push(diffObject);

        FetchPlan fetchPlanPropertyFetchPlan = fetchPlanProperty.getFetchPlan();
        if (fetchPlanPropertyFetchPlan == null) {
            throw new RuntimeException("Fetch plan property doesn't have a fetch plan");
        }
        List<EntityPropertyDifferenceModel> propertyDiffs =
                getPropertyDiffs(fetchPlanPropertyFetchPlan, firstValue, secondValue, diffBranch);

        diffBranch.pop();

        if (!propertyDiffs.isEmpty()) {
            isInternalChange = true;
            classPropertyDiff.setPropertyDiffs(propertyDiffs);
        }

        if (isInternalChange || isLinkChange)
            return classPropertyDiff;
        else
            return null;
    }

    private FetchPlan intersectFetchPlans(FetchPlan first, FetchPlan second) {
        if (first == null)
            throw new IllegalArgumentException("FetchPlan is null");
        if (second == null)
            throw new IllegalArgumentException("FetchPlan is null");

        FetchPlanBuilder builder = fetchPlans.builder(first.getEntityClass());

        Collection<FetchPlanProperty> firstProps = first.getProperties();

        for (FetchPlanProperty firstProperty : firstProps) {
            if (second.containsProperty(firstProperty.getName())) {
                FetchPlanProperty secondProperty = second.getProperty(firstProperty.getName());
                if ((firstProperty.getFetchPlan() != null) && (secondProperty != null && secondProperty.getFetchPlan() != null)) {
                    FetchPlan fetchPlan = intersectFetchPlans(firstProperty.getFetchPlan(), secondProperty.getFetchPlan());
                    builder.mergeProperty(firstProperty.getName(), fetchPlan, firstProperty.getFetchMode());
                }
                else {
                    builder.add(firstProperty.getName());
                }
            }
        }

        return builder.build();
    }
}
