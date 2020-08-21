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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.model.DataContext;
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
public class DataContextImpl implements DataContext {

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

    protected Map<Class<?>, Map<Object, JmixEntity>> content = new HashMap<>();

    protected Set<JmixEntity> modifiedInstances = new HashSet<>();

    protected Set<JmixEntity> removedInstances = new HashSet<>();

    protected PropertyChangeListener propertyChangeListener = new PropertyChangeListener();

    protected boolean disableListeners;

    protected DataContextImpl parentContext;

    protected Function<SaveContext, Set<JmixEntity>> commitDelegate;

    protected Map<JmixEntity, Map<String, EmbeddedPropertyChangeListener>> embeddedPropertyListeners = new WeakHashMap<>();

    protected Map<JmixEntity, JmixEntity> nullIdEntitiesMap = new /*Identity*/HashMap<>();

    @Nullable
    @Override
    public DataContext getParent() {
        return parentContext;
    }

    @Override
    public void setParent(DataContext parentContext) {
        checkNotNullArgument(parentContext, "parentContext is null");
        if (!(parentContext instanceof DataContextImpl)) {
            throw new IllegalArgumentException("Unsupported DataContext type: " + parentContext.getClass().getName());
        }
        this.parentContext = (DataContextImpl) parentContext;
    }

    @Override
    public Subscription addChangeListener(Consumer<ChangeEvent> listener) {
        return events.subscribe(ChangeEvent.class, listener);
    }

    protected void fireChangeListener(JmixEntity entity) {
        events.publish(ChangeEvent.class, new ChangeEvent(this, entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T extends JmixEntity> T find(Class<T> entityClass, Object entityId) {
        Map<Object, JmixEntity> entityMap = content.get(entityClass);
        if (entityMap != null) {
            return (T) entityMap.get(entityId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends JmixEntity> T find(T entity) {
        checkNotNullArgument(entity, "entity is null");
        return (T) find(entity.getClass(), makeKey(entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(JmixEntity entity) {
        checkNotNullArgument(entity, "entity is null");
        return find(entity.getClass(), makeKey(entity)) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends JmixEntity> T merge(T entity) {
        checkNotNullArgument(entity, "entity is null");

        disableListeners = true;
        T result;
        try {
            Map<JmixEntity, JmixEntity> merged = new IdentityHashMap<>();
            result = (T) internalMerge(entity, merged, true);
        } finally {
            disableListeners = false;
        }
        return result;
    }

    @Override
    public EntitySet merge(Collection<? extends JmixEntity> entities) {
        checkNotNullArgument(entities, "entity collection is null");

        List<JmixEntity> managedList = new ArrayList<>(entities.size());
        disableListeners = true;
        try {
            Map<JmixEntity, JmixEntity> merged = new IdentityHashMap<>();

            for (JmixEntity entity : entities) {
                JmixEntity managed = internalMerge(entity, merged, true);
                managedList.add(managed);
            }
        } finally {
            disableListeners = false;
        }
        return EntitySet.of(managedList);
    }

    protected JmixEntity internalMerge(JmixEntity entity, Map<JmixEntity, JmixEntity> mergedMap, boolean isRoot) {
        Map<Object, JmixEntity> entityMap = content.computeIfAbsent(entity.getClass(), aClass -> new HashMap<>());

        JmixEntity nullIdEntity = nullIdEntitiesMap.get(entity);
        if (nullIdEntity != null) {
            JmixEntity managed = entityMap.get(makeKey(nullIdEntity));
            if (managed != null) {
                mergedMap.put(entity, managed);
                mergeState(entity, managed, mergedMap, isRoot);
                return managed;
            } else {
                throw new IllegalStateException("No managed instance for " + nullIdEntity);
            }
        }

        JmixEntity managed = entityMap.get(makeKey(entity));

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

            mergeState(entity, managed, mergedMap, isRoot);

            managed.__getEntityEntry().addPropertyChangeListener(propertyChangeListener);

            if (entityStates.isNew(managed)) {
                modifiedInstances.add(managed);
                fireChangeListener(managed);
            }
        } else {
            mergedMap.put(entity, managed);
            if (managed != entity) {
                mergeState(entity, managed, mergedMap, isRoot);
            }
        }
        return managed;
    }

    protected Object makeKey(JmixEntity entity) {
        Object id = EntityValues.getId(entity);
        if (id != null) {
            return id;
        } else {
            return entity;
        }
    }

    protected JmixEntity copyEntity(JmixEntity srcEntity) {
        JmixEntity dstEntity;
        try {
            dstEntity = srcEntity.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + srcEntity.getClass(), e);
        }
        copySystemState(srcEntity, dstEntity);
        return dstEntity;
    }

    protected void mergeState(JmixEntity srcEntity, JmixEntity dstEntity, Map<JmixEntity, JmixEntity> mergedMap, boolean isRoot) {
        boolean srcNew = entityStates.isNew(srcEntity);
        boolean dstNew = entityStates.isNew(dstEntity);

        mergeSystemState(srcEntity, dstEntity, isRoot);

        MetaClass metaClass = metadata.getClass(srcEntity.getClass());

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (!property.getRange().isClass()                                             // local
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))          // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {       // loaded dst

                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && value == null) {
                    continue;
                }

                setPropertyValue(dstEntity, property, value);
            }
        }

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass()                                               // refs and collections
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))           // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {        // loaded dst
                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && value == null) {
                    continue;
                }

                if (value == null) {
                    setPropertyValue(dstEntity, property, null);
                    continue;
                }

                if (value instanceof Collection) {
                    if (value instanceof List) {
                        mergeList((List) value, dstEntity, property, isRoot, mergedMap);
                    } else if (value instanceof Set) {
                        mergeSet((Set) value, dstEntity, property, isRoot, mergedMap);
                    } else {
                        throw new UnsupportedOperationException("Unsupported collection type: " + value.getClass().getName());
                    }
                } else {
                    JmixEntity srcRef = (JmixEntity) value;
                    if (!mergedMap.containsKey(srcRef)) {
                        JmixEntity managedRef = internalMerge(srcRef, mergedMap, false);
                        setPropertyValue(dstEntity, property, managedRef, false);
                        if (metadataTools.isEmbedded(property)) {
                            EmbeddedPropertyChangeListener listener = new EmbeddedPropertyChangeListener(dstEntity);
                            managedRef.__getEntityEntry().addPropertyChangeListener(listener);
                            embeddedPropertyListeners.computeIfAbsent(dstEntity, e -> new HashMap<>()).put(propertyName, listener);
                        }
                    } else {
                        JmixEntity managedRef = mergedMap.get(srcRef);
                        if (managedRef != null) {
                            setPropertyValue(dstEntity, property, managedRef, false);
                        } else {
                            // should never happen
                            log.debug("Instance was merged but managed instance is null: {}", srcRef);
                        }
                    }
                }
            }
        }
    }

    protected void setPropertyValue(JmixEntity entity, MetaProperty property, @Nullable Object value) {
        setPropertyValue(entity, property, value, true);
    }

    protected void setPropertyValue(JmixEntity entity, MetaProperty property, @Nullable Object value, boolean checkEquals) {
        if (!property.isReadOnly()) {
            EntityValues.setValue(entity, property.getName(), value, checkEquals);
        } else {
            AnnotatedElement annotatedElement = property.getAnnotatedElement();
            if (annotatedElement instanceof Field) {
                Field field = (Field) annotatedElement;
                field.setAccessible(true);
                try {
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to set property value", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void copySystemState(JmixEntity srcEntity, JmixEntity dstEntity) {
        EntityValues.setId(dstEntity, EntityValues.getId(srcEntity));
        entitySystemStateSupport.copySystemState(srcEntity, dstEntity);

        if (EntitySystemValues.isVersionedSupported(dstEntity)) {
            EntitySystemValues.setVersion(dstEntity, EntitySystemValues.getVersion(srcEntity));
        }
    }

    protected void mergeSystemState(JmixEntity srcEntity, JmixEntity dstEntity, boolean isRoot) {
        if (isRoot) {
            entitySystemStateSupport.mergeSystemState(srcEntity, dstEntity);
        }
    }

    protected void mergeList(List<JmixEntity> list, JmixEntity managedEntity, MetaProperty property, boolean replace,
                             Map<JmixEntity, JmixEntity> mergedMap) {
        if (replace) {
            List<JmixEntity> managedRefs = new ArrayList<>(list.size());
            for (JmixEntity entity : list) {
                JmixEntity managedRef = internalMerge(entity, mergedMap, false);
                managedRefs.add(managedRef);
            }
            List<JmixEntity> dstList = createObservableList(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstList);

        } else {
            List<JmixEntity> dstList = EntityValues.getValue(managedEntity, property.getName());
            if (dstList == null) {
                dstList = createObservableList(managedEntity);
                setPropertyValue(managedEntity, property, dstList);
            }
            if (dstList.size() == 0) {
                for (JmixEntity srcRef : list) {
                    dstList.add(internalMerge(srcRef, mergedMap, false));
                }
            } else {
                for (JmixEntity srcRef : list) {
                    JmixEntity managedRef = internalMerge(srcRef, mergedMap, false);
                    if (!dstList.contains(managedRef)) {
                        dstList.add(managedRef);
                    }
                }
            }
        }
    }

    protected void mergeSet(Set<JmixEntity> set, JmixEntity managedEntity, MetaProperty property, boolean replace,
                            Map<JmixEntity, JmixEntity> mergedMap) {
        if (replace) {
            Set<JmixEntity> managedRefs = new LinkedHashSet<>(set.size());
            for (JmixEntity entity : set) {
                JmixEntity managedRef = internalMerge(entity, mergedMap, false);
                managedRefs.add(managedRef);
            }
            Set<JmixEntity> dstSet = createObservableSet(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstSet);

        } else {
            Set<JmixEntity> dstSet = EntityValues.getValue(managedEntity, property.getName());
            if (dstSet == null) {
                dstSet = createObservableSet(managedEntity);
                setPropertyValue(managedEntity, property, dstSet);
            }
            if (dstSet.size() == 0) {
                for (JmixEntity srcRef : set) {
                    dstSet.add(internalMerge(srcRef, mergedMap, false));
                }
            } else {
                for (JmixEntity srcRef : set) {
                    JmixEntity managedRef = internalMerge(srcRef, mergedMap, false);
                    dstSet.add(managedRef);
                }
            }
        }
    }

    protected List<JmixEntity> createObservableList(JmixEntity notifiedEntity) {
        return createObservableList(new ArrayList<>(), notifiedEntity);
    }

    protected List<JmixEntity> createObservableList(List<JmixEntity> list, JmixEntity notifiedEntity) {
        return new ObservableList<>(list, (changeType, changes) -> modified(notifiedEntity));
    }

    protected Set<JmixEntity> createObservableSet(JmixEntity notifiedEntity) {
        return createObservableSet(new LinkedHashSet<>(), notifiedEntity);
    }

    protected ObservableSet<JmixEntity> createObservableSet(Set<JmixEntity> set, JmixEntity notifiedEntity) {
        return new ObservableSet<>(set, (changeType, changes) -> modified(notifiedEntity));
    }

    @Override
    public void remove(JmixEntity entity) {
        checkNotNullArgument(entity, "entity is null");

        modifiedInstances.remove(entity);
        if (!entityStates.isNew(entity) || parentContext != null) {
            removedInstances.add(entity);
        }
        removeListeners(entity);
        fireChangeListener(entity);

        Map<Object, JmixEntity> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            JmixEntity mergedEntity = entityMap.get(makeKey(entity));
            if (mergedEntity != null) {
                entityMap.remove(makeKey(entity));
                removeFromCollections(mergedEntity);
            }
        }

        cleanupContextAfterRemoveEntity(this, entity);
    }

    protected void removeFromCollections(JmixEntity entityToRemove) {
        for (Map.Entry<Class<?>, Map<Object, JmixEntity>> entry : content.entrySet()) {
            Class<?> entityClass = entry.getKey();

            MetaClass metaClass = metadata.getClass(entityClass);
            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (metaProperty.getRange().isClass()
                        && metaProperty.getRange().getCardinality().isMany()
                        && metaProperty.getRange().asClass().getJavaClass().isAssignableFrom(entityToRemove.getClass())) {

                    Map<Object, JmixEntity> entityMap = entry.getValue();
                    for (JmixEntity entity : entityMap.values()) {
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
    public void evict(JmixEntity entity) {
        checkNotNullArgument(entity, "entity is null");

        Map<Object, JmixEntity> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            JmixEntity mergedEntity = entityMap.get(makeKey(entity));
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
        Set<JmixEntity> tmpModifiedInstances = new HashSet<>(modifiedInstances);
        Set<JmixEntity> tmpRemovedInstances = new HashSet<>(removedInstances);

        for (JmixEntity entity : tmpModifiedInstances) {
            evict(entity);
        }
        for (JmixEntity entity : tmpRemovedInstances) {
            evict(entity);
        }
    }

    @Override
    public void clear() {
        for (JmixEntity entity : getAll()) {
            evict(entity);
        }
    }

    @Override
    public <T extends JmixEntity> T create(Class<T> entityClass) {
        T entity = metadata.create(entityClass);
        return merge(entity);
    }

    protected void removeListeners(JmixEntity entity) {
        entity.__getEntityEntry().removePropertyChangeListener(propertyChangeListener);
        Map<String, EmbeddedPropertyChangeListener> listenerMap = embeddedPropertyListeners.get(entity);
        if (listenerMap != null) {
            for (Map.Entry<String, EmbeddedPropertyChangeListener> entry : listenerMap.entrySet()) {
                JmixEntity embedded = EntityValues.getValue(entity, entry.getKey());
                if (embedded != null) {
                    embedded.__getEntityEntry().removePropertyChangeListener(entry.getValue());
                    embedded.__getEntityEntry().removePropertyChangeListener(propertyChangeListener);
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
    public boolean isModified(JmixEntity entity) {
        return modifiedInstances.contains(entity);
    }

    @Override
    public void setModified(JmixEntity entity, boolean modified) {
        JmixEntity merged = find(entity);
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
    public Set<JmixEntity> getModified() {
        return Collections.unmodifiableSet(modifiedInstances);
    }

    @Override
    public boolean isRemoved(JmixEntity entity) {
        return removedInstances.contains(entity);
    }

    @Override
    public Set<JmixEntity> getRemoved() {
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
            Set<JmixEntity> committed = performCommit();
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
    public Function<SaveContext, Set<JmixEntity>> getCommitDelegate() {
        return commitDelegate;
    }

    @Override
    public void setCommitDelegate(Function<SaveContext, Set<JmixEntity>> delegate) {
        this.commitDelegate = delegate;
    }

    protected Set<JmixEntity> performCommit() {
        if (!hasChanges())
            return Collections.emptySet();

        if (parentContext == null) {
            return commitToDataManager();
        } else {
            return commitToParentContext();
        }
    }

    protected Set<JmixEntity> commitToDataManager() {
        SaveContext saveContext = new SaveContext()
                .saving(isolate(filterCommittedInstances(modifiedInstances)))
                .removing(isolate(filterCommittedInstances(removedInstances)));

        entityReferencesNormalizer.updateReferences(saveContext.getEntitiesToSave());

        for (JmixEntity entity : saveContext.getEntitiesToSave()) {
            saveContext.getFetchPlans().put(entity, entityStates.getCurrentFetchPlan(entity));
        }

        if (commitDelegate == null) {
            return dataManager.save(saveContext);
        } else {
            return commitDelegate.apply(saveContext);
        }
    }

    protected List<JmixEntity> filterCommittedInstances(Set<JmixEntity> instances) {
        return instances.stream()
                .filter(entity -> !metadataTools.isEmbeddable(entity.getClass()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Collection<JmixEntity> isolate(List<JmixEntity> entities) {
        // re-serialize the whole collection to preserve links between objects
        List isolatedEntities = (List) standardSerialization.deserialize(standardSerialization.serialize(entities));
        for (int i = 0; i < isolatedEntities.size(); i++) {
            JmixEntity isolatedEntity = (JmixEntity) isolatedEntities.get(i);
            JmixEntity entity = entities.get(i);
            if (EntityValues.getId(entity) == null) {
                nullIdEntitiesMap.put(isolatedEntity, entity);
            }
        }
        return isolatedEntities;
    }

    protected Set<JmixEntity> commitToParentContext() {
        HashSet<JmixEntity> committedEntities = new HashSet<>();
        for (JmixEntity entity : modifiedInstances) {
            JmixEntity merged = parentContext.merge(entity);
            parentContext.modifiedInstances.add(merged);
            committedEntities.add(merged);
        }
        for (JmixEntity entity : removedInstances) {
            parentContext.remove(entity);
            cleanupContextAfterRemoveEntity(parentContext, entity);
        }
        return committedEntities;
    }

    protected void cleanupContextAfterRemoveEntity(DataContextImpl context, JmixEntity removedEntity) {
        if (entityStates.isNew(removedEntity)) {
            for (JmixEntity modifiedInstance : new ArrayList<>(context.modifiedInstances)) {
                if (entityStates.isNew(modifiedInstance) && entityHasReference(modifiedInstance, removedEntity)) {
                    context.modifiedInstances.remove(modifiedInstance);
                }
            }
        }
    }

    protected boolean entityHasReference(JmixEntity entity, JmixEntity refEntity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        MetaClass refMetaClass = metadata.getClass(refEntity.getClass());

        return metaClass.getProperties().stream()
                .anyMatch(metaProperty -> metaProperty.getRange().isClass()
                        && metaProperty.getRange().asClass().equals(refMetaClass)
                        && Objects.equals(EntityValues.getValue(entity, metaProperty.getName()), refEntity));
    }

    protected EntitySet mergeCommitted(Set<JmixEntity> committed) {
        // transform into sorted collection to have reproducible behavior
        List<JmixEntity> entitiesToMerge = new ArrayList<>();
        for (JmixEntity entity : committed) {
            JmixEntity e = nullIdEntitiesMap.getOrDefault(entity, entity);
            if (contains(e)) {
                entitiesToMerge.add(entity);
            }
        }
        entitiesToMerge.sort(Comparator.comparing(Object::hashCode));

        return merge(entitiesToMerge);
    }

    public Collection<JmixEntity> getAll() {
        List<JmixEntity> resultList = new ArrayList<>();
        for (Map<Object, JmixEntity> entityMap : content.values()) {
            resultList.addAll(entityMap.values());
        }
        return resultList;
    }

    protected void modified(JmixEntity entity) {
        if (!disableListeners) {
            modifiedInstances.add(entity);
            fireChangeListener(entity);
        }
    }

    public String printContent() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Class<?>, Map<Object, JmixEntity>> entry : content.entrySet()) {
            sb.append("=== ").append(entry.getKey().getSimpleName()).append(" ===\n");
            for (JmixEntity entity : entry.getValue().values()) {
                sb.append(printEntity(entity, 1, Sets.newIdentityHashSet())).append('\n');
            }
        }
        return sb.toString();
    }

    protected String printEntity(JmixEntity entity, int level, Set<JmixEntity> visited) {
        StringBuilder sb = new StringBuilder();
        sb.append(printObject(entity)).append(" ").append(entity.toString()).append("\n");

        if (visited.contains(entity)) {
            return sb.toString();
        }
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (!property.getRange().isClass() || !entityStates.isLoaded(entity, property.getName()))
                continue;
            Object value = EntityValues.getValue(entity, property.getName());
            String prefix = StringUtils.repeat("  ", level);
            if (value instanceof JmixEntity) {
                String str = printEntity((JmixEntity) value, level + 1, visited);
                if (!str.equals(""))
                    sb.append(prefix).append(str);
            } else if (value instanceof Collection) {
                sb.append(prefix).append(value.getClass().getSimpleName()).append("[\n");
                for (Object item : (Collection) value) {
                    String str = printEntity((JmixEntity) item, level + 1, visited);
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

    protected class PropertyChangeListener implements EntityPropertyChangeListener {
        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            // if id has been changed, put the entity to the content with the new id
            MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(e.getItem().getClass());
            if (primaryKeyProperty != null
                    && e.getProperty().equals(primaryKeyProperty.getName())) {
                Map<Object, JmixEntity> entityMap = content.get(e.getItem().getClass());
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

        private final JmixEntity entity;

        public EmbeddedPropertyChangeListener(JmixEntity entity) {
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
