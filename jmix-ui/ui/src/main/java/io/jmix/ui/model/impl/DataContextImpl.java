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

package io.jmix.ui.model.impl;

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.*;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.MergeOptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Standard implementation of {@link DataContext} which commits data to {@link DataManager}.
 */
@SuppressWarnings("rawtypes")
public class DataContextImpl implements DataContextInternal {

    private static final Logger log = LoggerFactory.getLogger(DataContextImpl.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected EntitySystemStateSupport entitySystemStateSupport;

    @Autowired
    protected EntityReferencesNormalizer entityReferencesNormalizer;

    @Autowired
    protected StandardSerialization standardSerialization;

    protected EventHub events = new EventHub();

    protected Map<Class<?>, Map<Object, Object>> content = new HashMap<>();

    protected Set<Object> modifiedInstances = new HashSet<>();

    protected Set<Object> removedInstances = new HashSet<>();

    protected PropertyChangeListener propertyChangeListener = new PropertyChangeListener();

    protected boolean disableListeners;

    protected DataContextInternal parentContext;

    protected Function<SaveContext, Set<Object>> commitDelegate;

    protected Map<Object, Map<String, EmbeddedPropertyChangeListener>> embeddedPropertyListeners = new WeakHashMap<>();

    protected Map<Object, Object> nullIdEntitiesMap = new /*Identity*/HashMap<>();

    @Nullable
    @Override
    public DataContext getParent() {
        return parentContext;
    }

    @Override
    public void setParent(DataContext parentContext) {
        checkNotNullArgument(parentContext, "parentContext is null");
        if (!(parentContext instanceof DataContextInternal)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported DataContext type: %s. Parent DataContext must implement DataContextInternal",
                    parentContext.getClass().getName()));
        }
        this.parentContext = (DataContextInternal) parentContext;
    }

    @Override
    public Subscription addChangeListener(Consumer<ChangeEvent> listener) {
        return events.subscribe(ChangeEvent.class, listener);
    }

    protected void fireChangeListener(Object entity) {
        events.publish(ChangeEvent.class, new ChangeEvent(this, entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object entityId) {
        Map<Object, Object> entityMap = content.get(entityClass);
        if (entityMap != null) {
            return (T) entityMap.get(entityId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T find(T entity) {
        checkNotNullArgument(entity, "entity is null");
        return (T) find(entity.getClass(), makeKey(entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        return find(entity.getClass(), makeKey(entity)) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T merge(T entity, MergeOptions options) {
        checkNotNullArgument(entity, "entity is null");
        checkNotNullArgument(entity, "options object is null");

        disableListeners = true;
        T result;
        try {
            Map<Object, Object> merged = new IdentityHashMap<>();
            result = (T) internalMerge(entity, merged, true, options);
        } finally {
            disableListeners = false;
        }
        return result;
    }

    @Override
    public <T> T merge(T entity) {
        return merge(entity, new MergeOptions());
    }

    @Override
    public EntitySet merge(Collection entities, MergeOptions options) {
        checkNotNullArgument(entities, "entity collection is null");
        checkNotNullArgument(entities, "options object is null");

        List managedList = new ArrayList<>(entities.size());
        disableListeners = true;
        try {
            Map<Object, Object> merged = new IdentityHashMap<>();

            for (Object entity : entities) {
                Object managed = internalMerge(entity, merged, true, options);
                managedList.add(managed);
            }
        } finally {
            disableListeners = false;
        }
        return EntitySet.of(managedList);
    }

    @Override
    public EntitySet merge(Collection entities) {
        return merge(entities, new MergeOptions());
    }

    protected Object internalMerge(Object entity, Map<Object, Object> mergedMap, boolean isRoot, MergeOptions options) {
        Map<Object, Object> entityMap = content.computeIfAbsent(entity.getClass(), aClass -> new HashMap<>());

        Object nullIdEntity = nullIdEntitiesMap.get(entity);
        if (nullIdEntity != null) {
            Object managed = entityMap.get(makeKey(nullIdEntity));
            if (managed != null) {
                mergedMap.put(entity, managed);
                mergeState(entity, managed, mergedMap, isRoot, options);
                return managed;
            } else {
                throw new IllegalStateException("No managed instance for " + nullIdEntity);
            }
        }

        Object managed = entityMap.get(makeKey(entity));

        if (!isRoot && mergedMap.containsKey(entity)) {
            if (managed != null) {
                return managed;
            } else {
                // should never happen
                log.debug("Instance was merged but managed instance is null: {}", entity);
            }
        }

        if (managed == null) {
            managed = copyEntity(entity);
            entityMap.put(makeKey(managed), managed);
            mergedMap.put(entity, managed);

            mergeState(entity, managed, mergedMap, isRoot, options);

            EntitySystemAccess.addPropertyChangeListener(managed, propertyChangeListener);

            if (entityStates.isNew(managed)) {
                modifiedInstances.add(managed);
                fireChangeListener(managed);
            }
        } else {
            mergedMap.put(entity, managed);
            if (managed != entity) {
                mergeState(entity, managed, mergedMap, isRoot, options);
            }
        }
        return managed;
    }

    protected Object makeKey(Object entity) {
        Object id = EntityValues.getId(entity);
        if (id != null) {
            return id;
        } else {
            return entity;
        }
    }

    protected Object copyEntity(Object srcEntity) {
        Object dstEntity;
        try {
            dstEntity = srcEntity.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + srcEntity.getClass(), e);
        }
        copySystemState(srcEntity, dstEntity);
        return dstEntity;
    }

    protected void mergeState(Object srcEntity, Object dstEntity, Map<Object, Object> mergedMap,
                              boolean isRoot, MergeOptions options) {
        boolean srcNew = entityStates.isNew(srcEntity);
        boolean dstNew = entityStates.isNew(dstEntity);

        mergeSystemState(srcEntity, dstEntity, isRoot, options);

        MetaClass metaClass = getEntityMetaClass(srcEntity);

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (!property.getRange().isClass()                                   // local
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))// loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {// loaded dst - have to check to avoid unfetched for local properties

                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && !options.isFresh() && value == null) {
                    continue;
                }

                setPropertyValue(dstEntity, property, value);
            }
        }

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass()                                               // refs and collections
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))) {        // loaded src
                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && !options.isFresh() && value == null) {
                    continue;
                }

                if (value == null || !entityStates.isLoaded(dstEntity, propertyName)) {
                    if (!metadataTools.isEmbedded(property)) {//dstEntity property value will be lazy loaded and replaced by srcEntity property value
                        setPropertyValue(dstEntity, property, value);
                    }
                    continue;
                }

                if (value instanceof Collection) {
                    if (value instanceof List) {
                        mergeList((List) value, dstEntity, property, isRoot, options, mergedMap);
                    } else if (value instanceof Set) {
                        mergeSet((Set) value, dstEntity, property, isRoot, options, mergedMap);
                    } else {
                        throw new UnsupportedOperationException("Unsupported collection type: " + value.getClass().getName());
                    }
                } else {
                    if (!mergedMap.containsKey(value)) {
                        Object managedRef = internalMerge(value, mergedMap, false, options);
                        setPropertyValue(dstEntity, property, managedRef, false);
                        if (metadataTools.isEmbedded(property)) {
                            EmbeddedPropertyChangeListener listener = new EmbeddedPropertyChangeListener(dstEntity);
                            EntitySystemAccess.addPropertyChangeListener(managedRef, listener);
                            embeddedPropertyListeners.computeIfAbsent(dstEntity, e -> new HashMap<>()).put(propertyName, listener);
                        }
                    } else {
                        Object managedRef = mergedMap.get(value);
                        if (managedRef != null) {
                            setPropertyValue(dstEntity, property, managedRef, false);
                        } else {
                            // should never happen
                            log.debug("Instance was merged but managed instance is null: {}", value);
                        }
                    }
                }
            }
        }

        mergeLazyLoadingState(srcEntity, dstEntity);
    }

    protected void setPropertyValue(Object entity, MetaProperty property, @Nullable Object value) {
        setPropertyValue(entity, property, value, true);
    }

    protected void setPropertyValue(Object entity, MetaProperty property, @Nullable Object value, boolean checkEquals) {
        EntityPreconditions.checkEntityType(entity);
        if (!property.isReadOnly()) {
            EntityValues.setValue(entity, property.getName(), value, checkEquals);
        } else {
            AnnotatedElement annotatedElement = property.getAnnotatedElement();
            if (annotatedElement instanceof Field) {
                Field field = (Field) annotatedElement;
                field.setAccessible(true);
                if (value instanceof EnumClass) {
                    value = ((EnumClass<?>) value).getId();
                }
                try {
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to set property value", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void copySystemState(Object srcEntity, Object dstEntity) {
        EntityPreconditions.checkEntityType(srcEntity);
        EntityPreconditions.checkEntityType(dstEntity);

        entitySystemStateSupport.copySystemState((Entity) srcEntity, (Entity) dstEntity);
        EntityValues.setId(dstEntity, EntityValues.getId(srcEntity));
        EntityValues.setGeneratedId(dstEntity, EntityValues.getGeneratedId(srcEntity));

        if (EntityValues.isVersionSupported(dstEntity)) {
            EntityValues.setVersion(dstEntity, EntityValues.getVersion(srcEntity));
        }
    }

    protected void mergeSystemState(Object srcEntity, Object dstEntity, boolean isRoot, MergeOptions options) {
        if (isRoot || options.isFresh()) {
            entitySystemStateSupport.mergeSystemState((Entity) srcEntity, (Entity) dstEntity);
        }
    }

    protected void mergeLazyLoadingState(Object srcEntity, Object dstEntity) {
        boolean srcNew = entityStates.isNew(srcEntity);

        MetaClass metaClass = getEntityMetaClass(srcEntity);
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass()) {
                if (!srcNew && !entityStates.isLoaded(srcEntity, propertyName)) {
                    entitySystemStateSupport.mergeLazyLoadingState((Entity) srcEntity, (Entity) dstEntity, property,
                            collection -> wrapLazyValueIntoObservableCollection(collection, dstEntity));
                }
            }
        }
    }

    protected void mergeList(List<Object> list, Object managedEntity, MetaProperty property, boolean replace,
                             MergeOptions options, Map<Object, Object> mergedMap) {
        if (replace) {
            List<Object> managedRefs = new ArrayList<>(list.size());
            for (Object entity : list) {
                Object managedRef = internalMerge(entity, mergedMap, false, options);
                managedRefs.add(managedRef);
            }
            List<Object> dstList = createObservableList(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstList);

        } else {
            Object managedValue = EntityValues.getValue(managedEntity, property.getName());

            List<Object> dstList = null;
            if (managedValue instanceof List) {
                dstList = (List<Object>) managedValue;
            } else if (managedValue != null) {//any proxy Collection can be returned in case of Collection entity attribute (see Haulmont/jmix-ui#243)
                dstList = new ArrayList<Object>((Collection) managedValue);
            }

            if (dstList == null) {
                dstList = createObservableList(managedEntity);
                setPropertyValue(managedEntity, property, dstList);
            }
            if (dstList.size() == 0) {
                for (Object srcRef : list) {
                    dstList.add(internalMerge(srcRef, mergedMap, false, options));
                }
            } else {
                for (Object srcRef : list) {
                    Object managedRef = internalMerge(srcRef, mergedMap, false, options);
                    if (!dstList.contains(managedRef)) {
                        dstList.add(managedRef);
                    }
                }
            }
        }
    }

    protected void mergeSet(Set<Object> set, Object managedEntity, MetaProperty property, boolean replace,
                            MergeOptions options, Map<Object, Object> mergedMap) {
        if (replace) {
            Set<Object> managedRefs = new LinkedHashSet<>(set.size());
            for (Object entity : set) {
                Object managedRef = internalMerge(entity, mergedMap, false, options);
                managedRefs.add(managedRef);
            }
            Set<Object> dstSet = createObservableSet(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstSet);

        } else {
            Object managedValue = EntityValues.getValue(managedEntity, property.getName());

            Set<Object> dstSet = null;
            if (managedValue instanceof Set) {
                dstSet = (Set<Object>) managedValue;
            } else if (managedValue != null) {//any proxy Collection can be returned in case of Collection entity attribute (see Haulmont/jmix-ui#243)
                dstSet = new LinkedHashSet<Object>((Collection) managedValue);
            }


            if (dstSet == null) {
                dstSet = createObservableSet(managedEntity);
                setPropertyValue(managedEntity, property, dstSet);
            }
            if (dstSet.size() == 0) {
                for (Object srcRef : set) {
                    dstSet.add(internalMerge(srcRef, mergedMap, false, options));
                }
            } else {
                for (Object srcRef : set) {
                    Object managedRef = internalMerge(srcRef, mergedMap, false, options);
                    dstSet.add(managedRef);
                }
            }
        }
    }

    protected Collection<Object> wrapLazyValueIntoObservableCollection(Collection<Object> collection, Object notifiedEntity) {
        if (collection instanceof List) {
            return createObservableList((List<Object>) collection, notifiedEntity);
        } else if (collection instanceof Set) {
            return createObservableSet((Set<Object>) collection, notifiedEntity);
        }
        return collection;
    }

    protected List<Object> createObservableList(Object notifiedEntity) {
        return createObservableList(new ArrayList<>(), notifiedEntity);
    }

    protected List<Object> createObservableList(List<Object> list, Object notifiedEntity) {
        return new ObservableList<>(list, (changeType, changes) -> modified(notifiedEntity));
    }

    protected Set<Object> createObservableSet(Object notifiedEntity) {
        return createObservableSet(new LinkedHashSet<>(), notifiedEntity);
    }

    protected ObservableSet<Object> createObservableSet(Set<Object> set, Object notifiedEntity) {
        return new ObservableSet<>(set, (changeType, changes) -> modified(notifiedEntity));
    }

    @Override
    public void remove(Object entity) {
        checkNotNullArgument(entity, "entity is null");

        modifiedInstances.remove(entity);
        if (!entityStates.isNew(entity) || parentContext != null) {
            removedInstances.add(entity);
        }
        removeListeners(entity);
        fireChangeListener(entity);

        Map<Object, Object> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            Object mergedEntity = entityMap.get(makeKey(entity));
            if (mergedEntity != null) {
                entityMap.remove(makeKey(entity));
                removeFromCollections(mergedEntity);
            }
        }

        cleanupContextAfterRemoveEntity(this, entity);
    }

    protected void removeFromCollections(Object entityToRemove) {
        for (Map.Entry<Class<?>, Map<Object, Object>> entry : content.entrySet()) {
            Class<?> entityClass = entry.getKey();

            MetaClass metaClass = metadata.getClass(entityClass);
            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (metaProperty.getRange().isClass()
                        && metaProperty.getRange().getCardinality().isMany()
                        && metaProperty.getRange().asClass().getJavaClass().isAssignableFrom(entityToRemove.getClass())) {

                    Map<Object, Object> entityMap = entry.getValue();
                    for (Object entity : entityMap.values()) {
                        if (entityStates.isLoaded(entity, metaProperty.getName())) {
                            Collection collection = EntityValues.getValue(entity, metaProperty.getName());
                            if (collection != null) {
                                collection.remove(entityToRemove);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void evict(Object entity) {
        checkNotNullArgument(entity, "entity is null");

        Map<Object, Object> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            Object mergedEntity = entityMap.get(makeKey(entity));
            if (mergedEntity != null) {
                entityMap.remove(makeKey(entity));
                removeListeners(entity);
            }
            modifiedInstances.remove(entity);
            removedInstances.remove(entity);
        }
    }

    @Override
    public void evictModified() {
        Set<Object> tmpModifiedInstances = new HashSet<>(modifiedInstances);
        Set<Object> tmpRemovedInstances = new HashSet<>(removedInstances);

        for (Object entity : tmpModifiedInstances) {
            evict(entity);
        }
        for (Object entity : tmpRemovedInstances) {
            evict(entity);
        }
    }

    @Override
    public void clear() {
        for (Object entity : getAll()) {
            evict(entity);
        }
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        T entity = metadata.create(entityClass);
        return merge(entity);
    }

    protected void removeListeners(Object entity) {
        EntitySystemAccess.removePropertyChangeListener(entity, propertyChangeListener);
        Map<String, EmbeddedPropertyChangeListener> listenerMap = embeddedPropertyListeners.get(entity);
        if (listenerMap != null) {
            for (Map.Entry<String, EmbeddedPropertyChangeListener> entry : listenerMap.entrySet()) {
                Object embedded = EntityValues.getValue(entity, entry.getKey());
                if (embedded != null) {
                    EntitySystemAccess.removePropertyChangeListener(embedded, entry.getValue());
                    EntitySystemAccess.removePropertyChangeListener(embedded, propertyChangeListener);
                }
            }
            embeddedPropertyListeners.remove(entity);
        }
    }

    @Override
    public boolean hasChanges() {
        return !(modifiedInstances.isEmpty() && removedInstances.isEmpty());
    }

    @Override
    public boolean isModified(Object entity) {
        return modifiedInstances.contains(entity);
    }

    @Override
    public void setModified(Object entity, boolean modified) {
        Object merged = find(entity);
        if (merged == null) {
            return;
        }
        if (modified) {
            modifiedInstances.add(merged);
        } else {
            modifiedInstances.remove(merged);
        }
    }

    @Override
    public Set getModified() {
        return Collections.unmodifiableSet(modifiedInstances);
    }

    @Override
    public boolean isRemoved(Object entity) {
        return removedInstances.contains(entity);
    }

    @Override
    public Set getRemoved() {
        return Collections.unmodifiableSet(removedInstances);
    }

    @Override
    public EntitySet commit() {
        PreCommitEvent preCommitEvent = new PreCommitEvent(this, modifiedInstances, removedInstances);
        events.publish(PreCommitEvent.class, preCommitEvent);
        if (preCommitEvent.isCommitPrevented())
            return EntitySet.of(Collections.emptySet());

        EntitySet committedAndMerged;
        try {
            Set<Object> committed = performCommit();
            committedAndMerged = mergeCommitted(committed);
        } finally {
            nullIdEntitiesMap.clear();
        }

        events.publish(PostCommitEvent.class, new PostCommitEvent(this, committedAndMerged));

        modifiedInstances.clear();
        removedInstances.clear();

        return committedAndMerged;
    }

    @Override
    public Subscription addPreCommitListener(Consumer<PreCommitEvent> listener) {
        return events.subscribe(PreCommitEvent.class, listener);
    }

    @Override
    public Subscription addPostCommitListener(Consumer<PostCommitEvent> listener) {
        return events.subscribe(PostCommitEvent.class, listener);
    }

    @Override
    public Function<SaveContext, Set<Object>> getCommitDelegate() {
        return commitDelegate;
    }

    @Override
    public void setCommitDelegate(Function<SaveContext, Set<Object>> delegate) {
        this.commitDelegate = delegate;
    }

    protected Set<Object> performCommit() {
        if (!hasChanges())
            return Collections.emptySet();

        if (parentContext == null) {
            return commitToDataManager();
        } else {
            return commitToParentContext();
        }
    }

    protected Set<Object> commitToDataManager() {
        SaveContext saveContext = new SaveContext()
                .saving(isolate(filterCommittedInstances(modifiedInstances)))
                .removing(isolate(filterCommittedInstances(removedInstances)));

        entityReferencesNormalizer.updateReferences(saveContext.getEntitiesToSave());
        updateFetchPlans(saveContext);

        if (commitDelegate == null) {
            return dataManager.save(saveContext);
        } else {
            return commitDelegate.apply(saveContext);
        }
    }

    protected List filterCommittedInstances(Set<Object> instances) {
        return instances.stream()
                .filter(entity -> !metadataTools.isJpaEmbeddable(entity.getClass()))
                .collect(Collectors.toList());
    }

    protected void updateFetchPlans(SaveContext saveContext) {
        for (Object entity : saveContext.getEntitiesToSave()) {
            saveContext.getFetchPlans().put(entity, entityStates.getCurrentFetchPlan(entity));
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Object> isolate(List entities) {
        // re-serialize the whole collection to preserve links between objects
        List isolatedEntities = (List) standardSerialization.deserialize(standardSerialization.serialize(entities));
        for (int i = 0; i < isolatedEntities.size(); i++) {
            Object isolatedEntity = isolatedEntities.get(i);
            Object entity = entities.get(i);
            if (EntityValues.getId(entity) == null) {
                nullIdEntitiesMap.put(isolatedEntity, entity);
            }
        }
        return isolatedEntities;
    }

    protected Set<Object> commitToParentContext() {
        Set<Object> committedEntities = new HashSet<>();
        for (Object entity : modifiedInstances) {
            Object merged = parentContext.merge(entity);
            parentContext.getModifiedInstances().add(merged);
            committedEntities.add(merged);
        }
        for (Object entity : removedInstances) {
            parentContext.remove(entity);
            cleanupContextAfterRemoveEntity(parentContext, entity);
        }
        return committedEntities;
    }

    protected void cleanupContextAfterRemoveEntity(DataContextInternal context, Object removedEntity) {
        if (entityStates.isNew(removedEntity)) {
            context.getModifiedInstances().removeIf(modifiedInstance ->
                    entityStates.isNew(modifiedInstance) && entityHasReference(modifiedInstance, removedEntity));
        }
    }

    protected boolean entityHasReference(Object entity, Object refEntity) {
        MetaClass metaClass = metadata.getClass(entity);
        MetaClass refMetaClass = metadata.getClass(refEntity);

        return metaClass.getProperties().stream()
                .anyMatch(metaProperty -> metaProperty.getRange().isClass()
                        && metaProperty.getRange().asClass().equals(refMetaClass)
                        && Objects.equals(EntityValues.getValue(entity, metaProperty.getName()), refEntity));
    }

    protected EntitySet mergeCommitted(Set<Object> committed) {
        // transform into sorted collection to have reproducible behavior
        List<Object> entitiesToMerge = new ArrayList<>();
        for (Object entity : committed) {
            Object e = nullIdEntitiesMap.getOrDefault(entity, entity);
            if (contains(e)) {
                entitiesToMerge.add(entity);
            }
        }
        entitiesToMerge.sort(Comparator.comparing(Object::hashCode));

        return merge(entitiesToMerge);
    }

    public Collection getAll() {
        List<Object> resultList = new ArrayList<>();
        for (Map<Object, Object> entityMap : content.values()) {
            resultList.addAll(entityMap.values());
        }
        return resultList;
    }

    protected void modified(Object entity) {
        if (!disableListeners) {
            modifiedInstances.add(entity);
            fireChangeListener(entity);
        }
    }

    public String printContent() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Class<?>, Map<Object, Object>> entry : content.entrySet()) {
            sb.append("=== ").append(entry.getKey().getSimpleName()).append(" ===\n");
            for (Object entity : entry.getValue().values()) {
                sb.append(printEntity(entity, 1, Sets.newIdentityHashSet())).append('\n');
            }
        }
        return sb.toString();
    }

    protected String printEntity(Object entity, int level, Set<Object> visited) {
        StringBuilder sb = new StringBuilder();
        sb.append(printObject(entity)).append(" ").append(entity.toString()).append("\n");

        if (visited.contains(entity)) {
            return sb.toString();
        }
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (!property.getRange().isClass() || !entityStates.isLoaded(entity, property.getName()))
                continue;
            Object value = EntityValues.getValue(entity, property.getName());
            String prefix = StringUtils.repeat("  ", level);
            if (value instanceof Entity) {
                String str = printEntity((Entity) value, level + 1, visited);
                if (!str.equals(""))
                    sb.append(prefix).append(str);
            } else if (value instanceof Collection) {
                sb.append(prefix).append(value.getClass().getSimpleName()).append("[\n");
                for (Object item : (Collection) value) {
                    String str = printEntity((Entity) item, level + 1, visited);
                    if (!str.equals(""))
                        sb.append(prefix).append(str);
                }
                sb.append(prefix).append("]\n");
            }
        }
        return sb.toString();
    }

    protected String printObject(Object object) {
        return "{" + object.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(object)) + "}";
    }

    @Override
    public Set<Object> getModifiedInstances() {
        return modifiedInstances;
    }

    protected MetaClass getEntityMetaClass(Object entity) {
        return metadata.getClass(entity);
    }

    @Nullable
    protected String getPrimaryKeyPropertyName(Object entity) {
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(entity.getClass());
        if (primaryKeyProperty != null) {
            return primaryKeyProperty.getName();
        }
        return null;
    }

    protected class PropertyChangeListener implements EntityPropertyChangeListener {
        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            // if id has been changed, put the entity to the content with the new id
            if (e.getProperty().equals(getPrimaryKeyPropertyName(e.getItem()))) {
                Map<Object, Object> entityMap = content.get(e.getItem().getClass());
                if (entityMap != null) {
                    if (e.getPrevValue() == null) {
                        entityMap.remove(e.getItem());
                    } else {
                        entityMap.remove(e.getPrevValue());
                    }
                    entityMap.put(e.getValue(), e.getItem());
                }
            }

            if (!disableListeners) {
                modifiedInstances.add(e.getItem());
                fireChangeListener(e.getItem());
            }
        }
    }

    protected class EmbeddedPropertyChangeListener implements EntityPropertyChangeListener {

        private final Object entity;

        public EmbeddedPropertyChangeListener(Object entity) {
            this.entity = entity;
        }

        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            if (!disableListeners) {
                modifiedInstances.add(entity);
                fireChangeListener(entity);
            }
        }
    }
}
