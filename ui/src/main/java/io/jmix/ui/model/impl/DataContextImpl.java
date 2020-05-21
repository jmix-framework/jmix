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
import org.springframework.context.ApplicationContext;

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

    protected ApplicationContext applicationContext;

    protected EventHub events = new EventHub();

    protected Map<Class<?>, Map<Object, Entity>> content = new HashMap<>();

    protected Set<Entity> modifiedInstances = new HashSet<>();

    protected Set<Entity> removedInstances = new HashSet<>();

    protected PropertyChangeListener propertyChangeListener = new PropertyChangeListener();

    protected boolean disableListeners;

    protected DataContextImpl parentContext;

    protected Function<SaveContext, Set<Entity>> commitDelegate;

    protected Map<Entity, Map<String, EmbeddedPropertyChangeListener>> embeddedPropertyListeners = new WeakHashMap<>();

    protected Map<Entity, Entity> nullIdEntitiesMap = new IdentityHashMap<>();

    public DataContextImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected Metadata getMetadata() {
        return applicationContext.getBean(Metadata.NAME, Metadata.class);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.NAME, MetadataTools.class);
    }

    protected EntityStates getEntityStates() {
        return applicationContext.getBean(EntityStates.NAME, EntityStates.class);
    }

    protected DataManager getDataManager() {
        return applicationContext.getBean(DataManager.NAME, DataManager.class);
    }

    protected EntitySystemStateSupport getEntitySystemStateSupport() {
        return applicationContext.getBean(EntitySystemStateSupport.NAME, EntitySystemStateSupport.class);
    }

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

    protected void fireChangeListener(Entity entity) {
        events.publish(ChangeEvent.class, new ChangeEvent(this, entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T extends Entity> T find(Class<T> entityClass, Object entityId) {
        Map<Object, Entity> entityMap = content.get(entityClass);
        if (entityMap != null) {
            return (T) entityMap.get(entityId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends Entity> T find(T entity) {
        checkNotNullArgument(entity, "entity is null");
        return (T) find(entity.getClass(), makeKey(entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Entity entity) {
        checkNotNullArgument(entity, "entity is null");
        return find(entity.getClass(), makeKey(entity)) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> T merge(T entity) {
        checkNotNullArgument(entity, "entity is null");

        disableListeners = true;
        T result;
        try {
            Map<Entity, Entity> merged = new IdentityHashMap<>();
            result = (T) internalMerge(entity, merged, true);
        } finally {
            disableListeners = false;
        }
        return result;
    }

    @Override
    public EntitySet merge(Collection<? extends Entity> entities) {
        checkNotNullArgument(entities, "entity collection is null");

        List<Entity> managedList = new ArrayList<>(entities.size());
        disableListeners = true;
        try {
            Map<Entity, Entity> merged = new IdentityHashMap<>();

            for (Entity entity : entities) {
                Entity managed = internalMerge(entity, merged, true);
                managedList.add(managed);
            }
        } finally {
            disableListeners = false;
        }
        return EntitySet.of(managedList);
    }

    protected Entity internalMerge(Entity entity, Map<Entity, Entity> mergedMap, boolean isRoot) {
        Map<Object, Entity> entityMap = content.computeIfAbsent(entity.getClass(), aClass -> new HashMap<>());

        Entity nullIdEntity = nullIdEntitiesMap.get(entity);
        if (nullIdEntity != null) {
            Entity managed = entityMap.get(makeKey(nullIdEntity));
            if (managed != null) {
                mergedMap.put(entity, managed);
                mergeState(entity, managed, mergedMap, isRoot);
                return managed;
            } else {
                throw new IllegalStateException("No managed instance for " + nullIdEntity);
            }
        }

        Entity managed = entityMap.get(makeKey(entity));

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

            if (getEntityStates().isNew(managed)) {
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

    protected Object makeKey(Entity entity) {
        Object id = EntityValues.getId(entity);
        if (id != null) {
            return EntityValues.getId(entity);
        } else {
            return entity;
        }
    }

    protected Entity copyEntity(Entity srcEntity) {
        Entity dstEntity;
        try {
            dstEntity = srcEntity.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + srcEntity.getClass(), e);
        }
        copySystemState(srcEntity, dstEntity);
        return dstEntity;
    }

    protected void mergeState(Entity srcEntity, Entity dstEntity, Map<Entity, Entity> mergedMap, boolean isRoot) {
        EntityStates entityStates = getEntityStates();

        boolean srcNew = entityStates.isNew(srcEntity);
        boolean dstNew = entityStates.isNew(dstEntity);

        mergeSystemState(srcEntity, dstEntity, isRoot);

        MetaClass metaClass = getMetadata().getClass(srcEntity.getClass());

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (!property.getRange().isClass()                                             // local
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))          // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {       // loaded dst

                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities and do not try to assign IdProxy
                if ((!isRoot && value == null)
                        || (value instanceof IdProxy)) {
                    continue;
                }

                setPropertyValue(dstEntity, property, value);
            }
        }

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass()                                              // refs and collections
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))          // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {       // loaded dst

                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities and do not try to assign IdProxy
                if ((!isRoot && value == null)
                        || (value instanceof IdProxy)) {
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
                    Entity srcRef = (Entity) value;
                    if (!mergedMap.containsKey(srcRef)) {
                        Entity managedRef = internalMerge(srcRef, mergedMap, false);
                        setPropertyValue(dstEntity, property, managedRef, false);
                        if (getMetadataTools().isEmbedded(property)) {
                            EmbeddedPropertyChangeListener listener = new EmbeddedPropertyChangeListener(dstEntity);
                            managedRef.__getEntityEntry().addPropertyChangeListener(listener);
                            embeddedPropertyListeners.computeIfAbsent(dstEntity, e -> new HashMap<>()).put(propertyName, listener);
                        }
                    } else {
                        Entity managedRef = mergedMap.get(srcRef);
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

    protected void setPropertyValue(Entity entity, MetaProperty property, @Nullable Object value) {
        setPropertyValue(entity, property, value, true);
    }

    protected void setPropertyValue(Entity entity, MetaProperty property, @Nullable Object value, boolean checkEquals) {
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
    protected void copySystemState(Entity srcEntity, Entity dstEntity) {
        EntityValues.setId(dstEntity, EntityValues.getId(srcEntity));
        getEntitySystemStateSupport().copySystemState(srcEntity, dstEntity);

        if (dstEntity instanceof Versioned) {
            ((Versioned) dstEntity).setVersion(((Versioned) srcEntity).getVersion());
        }
    }

    protected void mergeSystemState(Entity srcEntity, Entity dstEntity, boolean isRoot) {
        if (isRoot) {
            getEntitySystemStateSupport().mergeSystemState(srcEntity, dstEntity);
        }
    }

    protected void mergeList(List<Entity> list, Entity managedEntity, MetaProperty property, boolean replace,
                             Map<Entity, Entity> mergedMap) {
        if (replace) {
            List<Entity> managedRefs = new ArrayList<>(list.size());
            for (Entity entity : list) {
                Entity managedRef = internalMerge(entity, mergedMap, false);
                managedRefs.add(managedRef);
            }
            List<Entity> dstList = createObservableList(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstList);

        } else {
            List<Entity> dstList = EntityValues.getValue(managedEntity, property.getName());
            if (dstList == null) {
                dstList = createObservableList(managedEntity);
                setPropertyValue(managedEntity, property, dstList);
            }
            if (dstList.size() == 0) {
                for (Entity srcRef : list) {
                    dstList.add(internalMerge(srcRef, mergedMap, false));
                }
            } else {
                for (Entity srcRef : list) {
                    Entity managedRef = internalMerge(srcRef, mergedMap, false);
                    if (!dstList.contains(managedRef)) {
                        dstList.add(managedRef);
                    }
                }
            }
        }
    }

    protected void mergeSet(Set<Entity> set, Entity managedEntity, MetaProperty property, boolean replace,
                            Map<Entity, Entity> mergedMap) {
        if (replace) {
            Set<Entity> managedRefs = new LinkedHashSet<>(set.size());
            for (Entity entity : set) {
                Entity managedRef = internalMerge(entity, mergedMap, false);
                managedRefs.add(managedRef);
            }
            Set<Entity> dstSet = createObservableSet(managedRefs, managedEntity);
            setPropertyValue(managedEntity, property, dstSet);

        } else {
            Set<Entity> dstSet = EntityValues.getValue(managedEntity, property.getName());
            if (dstSet == null) {
                dstSet = createObservableSet(managedEntity);
                setPropertyValue(managedEntity, property, dstSet);
            }
            if (dstSet.size() == 0) {
                for (Entity srcRef : set) {
                    dstSet.add(internalMerge(srcRef, mergedMap, false));
                }
            } else {
                for (Entity srcRef : set) {
                    Entity managedRef = internalMerge(srcRef, mergedMap, false);
                    dstSet.add(managedRef);
                }
            }
        }
    }

    protected List<Entity> createObservableList(Entity notifiedEntity) {
        return createObservableList(new ArrayList<>(), notifiedEntity);
    }

    protected List<Entity> createObservableList(List<Entity> list, Entity notifiedEntity) {
        return new ObservableList<>(list, (changeType, changes) -> modified(notifiedEntity));
    }

    protected Set<Entity> createObservableSet(Entity notifiedEntity) {
        return createObservableSet(new LinkedHashSet<>(), notifiedEntity);
    }

    protected ObservableSet<Entity> createObservableSet(Set<Entity> set, Entity notifiedEntity) {
        return new ObservableSet<>(set, (changeType, changes) -> modified(notifiedEntity));
    }

    @Override
    public void remove(Entity entity) {
        checkNotNullArgument(entity, "entity is null");

        modifiedInstances.remove(entity);
        if (!getEntityStates().isNew(entity) || parentContext != null) {
            removedInstances.add(entity);
        }
        removeListeners(entity);
        fireChangeListener(entity);

        Map<Object, Entity> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            Entity mergedEntity = entityMap.get(makeKey(entity));
            if (mergedEntity != null) {
                entityMap.remove(makeKey(entity));
                removeFromCollections(mergedEntity);
            }
        }

        cleanupContextAfterRemoveEntity(this, entity);
    }

    protected void removeFromCollections(Entity entityToRemove) {
        for (Map.Entry<Class<?>, Map<Object, Entity>> entry : content.entrySet()) {
            Class<?> entityClass = entry.getKey();

            MetaClass metaClass = getMetadata().getClass(entityClass);
            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (metaProperty.getRange().isClass()
                        && metaProperty.getRange().getCardinality().isMany()
                        && metaProperty.getRange().asClass().getJavaClass().isAssignableFrom(entityToRemove.getClass())) {

                    Map<Object, Entity> entityMap = entry.getValue();
                    for (Entity entity : entityMap.values()) {
                        if (getEntityStates().isLoaded(entity, metaProperty.getName())) {
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
    public void evict(Entity entity) {
        checkNotNullArgument(entity, "entity is null");

        Map<Object, Entity> entityMap = content.get(entity.getClass());
        if (entityMap != null) {
            Entity mergedEntity = entityMap.get(makeKey(entity));
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
        Set<Entity> tmpModifiedInstances = new HashSet<>(modifiedInstances);
        Set<Entity> tmpRemovedInstances = new HashSet<>(removedInstances);

        for (Entity entity : tmpModifiedInstances) {
            evict(entity);
        }
        for (Entity entity : tmpRemovedInstances) {
            evict(entity);
        }
    }

    @Override
    public void clear() {
        for (Entity entity : getAll()) {
            evict(entity);
        }
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        T entity = getMetadata().create(entityClass);
        return merge(entity);
    }

    protected void removeListeners(Entity entity) {
        entity.__getEntityEntry().removePropertyChangeListener(propertyChangeListener);
        Map<String, EmbeddedPropertyChangeListener> listenerMap = embeddedPropertyListeners.get(entity);
        if (listenerMap != null) {
            for (Map.Entry<String, EmbeddedPropertyChangeListener> entry : listenerMap.entrySet()) {
                Entity embedded = EntityValues.getValue(entity, entry.getKey());
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
    public boolean isModified(Entity entity) {
        return modifiedInstances.contains(entity);
    }

    @Override
    public void setModified(Entity entity, boolean modified) {
        Entity merged = find(entity);
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
    public Set<Entity> getModified() {
        return Collections.unmodifiableSet(modifiedInstances);
    }

    @Override
    public boolean isRemoved(Entity entity) {
        return removedInstances.contains(entity);
    }

    @Override
    public Set<Entity> getRemoved() {
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
            Set<Entity> committed = performCommit();
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
    public Function<SaveContext, Set<Entity>> getCommitDelegate() {
        return commitDelegate;
    }

    @Override
    public void setCommitDelegate(Function<SaveContext, Set<Entity>> delegate) {
        this.commitDelegate = delegate;
    }

    protected Set<Entity> performCommit() {
        if (!hasChanges())
            return Collections.emptySet();

        if (parentContext == null) {
            return commitToDataManager();
        } else {
            return commitToParentContext();
        }
    }

    protected Set<Entity> commitToDataManager() {
        SaveContext saveContext = new SaveContext()
                .saving(isolate(filterCommittedInstances(modifiedInstances)))
                .removing(isolate(filterCommittedInstances(removedInstances)));

        for (Entity entity : saveContext.getEntitiesToSave()) {
            saveContext.getFetchPlans().put(entity, getEntityStates().getCurrentFetchPlan(entity));
        }

        if (commitDelegate == null) {
            return getDataManager().save(saveContext);
        } else {
            return commitDelegate.apply(saveContext);
        }
    }

    protected List<Entity> filterCommittedInstances(Set<Entity> instances) {
        return instances.stream()
                .filter(entity -> !getMetadataTools().isEmbeddable(entity.getClass()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Collection<Entity> isolate(List<Entity> entities) {
        // re-serialize the whole collection to preserve links between objects
        List isolatedEntities = (List) StandardSerialization.deserialize(StandardSerialization.serialize(entities));
        for (int i = 0; i < isolatedEntities.size(); i++) {
            Entity isolatedEntity = (Entity) isolatedEntities.get(i);
            Entity entity = entities.get(i);
            if (EntityValues.getId(entity) == null) {
                nullIdEntitiesMap.put(isolatedEntity, entity);
            }
        }
        return isolatedEntities;
    }

    protected Set<Entity> commitToParentContext() {
        HashSet<Entity> committedEntities = new HashSet<>();
        for (Entity entity : modifiedInstances) {
            Entity merged = parentContext.merge(entity);
            parentContext.modifiedInstances.add(merged);
            committedEntities.add(merged);
        }
        for (Entity entity : removedInstances) {
            parentContext.remove(entity);
            cleanupContextAfterRemoveEntity(parentContext, entity);
        }
        return committedEntities;
    }

    protected void cleanupContextAfterRemoveEntity(DataContextImpl context, Entity removedEntity) {
        EntityStates entityStates = getEntityStates();
        if (entityStates.isNew(removedEntity)) {
            for (Entity modifiedInstance : new ArrayList<>(context.modifiedInstances)) {
                if (entityStates.isNew(modifiedInstance) && entityHasReference(modifiedInstance, removedEntity)) {
                    context.modifiedInstances.remove(modifiedInstance);
                }
            }
        }
    }

    protected boolean entityHasReference(Entity entity, Entity refEntity) {
        MetaClass metaClass = getMetadata().getClass(entity.getClass());
        MetaClass refMetaClass = getMetadata().getClass(refEntity.getClass());

        return metaClass.getProperties().stream()
                .anyMatch(metaProperty -> metaProperty.getRange().isClass()
                        && metaProperty.getRange().asClass().equals(refMetaClass)
                        && Objects.equals(EntityValues.getValue(entity, metaProperty.getName()), refEntity));
    }

    protected EntitySet mergeCommitted(Set<Entity> committed) {
        // transform into sorted collection to have reproducible behavior
        List<Entity> entitiesToMerge = new ArrayList<>();
        for (Entity entity : committed) {
            Entity e = nullIdEntitiesMap.getOrDefault(entity, entity);
            if (contains(e)) {
                entitiesToMerge.add(entity);
            }
        }
        entitiesToMerge.sort(Comparator.comparing(Object::hashCode));

        return merge(entitiesToMerge);
    }

    public Collection<Entity> getAll() {
        List<Entity> resultList = new ArrayList<>();
        for (Map<Object, Entity> entityMap : content.values()) {
            resultList.addAll(entityMap.values());
        }
        return resultList;
    }

    protected void modified(Entity entity) {
        if (!disableListeners) {
            modifiedInstances.add(entity);
            fireChangeListener(entity);
        }
    }

    public String printContent() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Class<?>, Map<Object, Entity>> entry : content.entrySet()) {
            sb.append("=== ").append(entry.getKey().getSimpleName()).append(" ===\n");
            for (Entity entity : entry.getValue().values()) {
                sb.append(printEntity(entity, 1, Sets.newIdentityHashSet())).append('\n');
            }
        }
        return sb.toString();
    }

    protected String printEntity(Entity entity, int level, Set<Entity> visited) {
        StringBuilder sb = new StringBuilder();
        sb.append(printObject(entity)).append(" ").append(entity.toString()).append("\n");

        if (visited.contains(entity)) {
            return sb.toString();
        }
        visited.add(entity);

        for (MetaProperty property : getMetadata().getClass(entity.getClass()).getProperties()) {
            if (!property.getRange().isClass() || !getEntityStates().isLoaded(entity, property.getName()))
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

    protected class PropertyChangeListener implements EntityPropertyChangeListener {
        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            // if id has been changed, put the entity to the content with the new id
            MetaProperty primaryKeyProperty = getMetadataTools().getPrimaryKeyProperty(e.getItem().getClass());
            if (primaryKeyProperty != null && e.getProperty().equals(primaryKeyProperty.getName())) {
                Map<Object, Entity> entityMap = content.get(e.getItem().getClass());
                if (entityMap != null) {
                    entityMap.remove(e.getPrevValue());
                    entityMap.put(e.getValue(), (Entity) e.getItem());
                }
            }

            if (!disableListeners) {
                modifiedInstances.add((Entity) e.getItem());
                fireChangeListener((Entity) e.getItem());
            }
        }
    }

    protected class EmbeddedPropertyChangeListener implements EntityPropertyChangeListener {

        private final Entity entity;

        public EmbeddedPropertyChangeListener(Entity entity) {
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
