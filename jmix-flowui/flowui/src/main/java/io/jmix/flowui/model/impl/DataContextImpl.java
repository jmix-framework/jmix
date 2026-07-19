/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.model.impl;

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.*;
import io.jmix.core.impl.CachingLoadedPropertiesInfo;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.MergeOptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Standard implementation of {@link DataContext} which saves data to {@link DataManager}.
 */
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
    protected Copier copier;

    protected EventHub events = new EventHub();

    protected Map<Class<?>, Map<Object, Object>> content = new HashMap<>();

    protected Set<Object> modifiedInstances = new HashSet<>();

    protected Set<Object> removedInstances = new HashSet<>();

    protected DataContextChangeTracker changeTracker =
            new DataContextChangeTracker(this::entityBecameDirty, this::entityBecameClean);

    protected Set<Object> manuallyModified = new HashSet<>();

    // Composition owners marked modified only to protect an intermediate editor from a stale reload:
    // the reopen gate reads isModified, but these owners have no change of their own
    // and must NOT be persisted, so they are kept out of modifiedInstances and the save set.
    protected Set<Object> compositionModifiedOwners = new HashSet<>();

    // Non-empty only inside mergeFromChild: attributes of the merge root whose incoming (child)
    // values must overwrite this context's own unsaved edits. Scoped to the single merge call
    // via try/finally; safe as a plain field because a context never re-enters its own merge
    // from within mergeFromChild (a parent's save into a grandparent runs on another instance).
    protected Set<String> overridingAttributes = Set.of();

    // (managedEntity identity -> property names) whose collection is being merged within the active
    // merge() call. Guards against re-entering mergeList/mergeSet for the same owner collection when
    // the source graph holds multiple java instances of the same id joined by a bidirectional reference.
    protected Map<Object, Set<String>> mergeCollectionInProgress;

    protected PropertyChangeListener propertyChangeListener = new PropertyChangeListener();

    protected boolean disableListeners;

    protected DataContextInternal parentContext;

    protected Function<SaveContext, Set<Object>> saveDelegate;

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
        mergeCollectionInProgress = new IdentityHashMap<>();
        T result;
        try {
            Map<Object, Object> merged = new IdentityHashMap<>();
            result = (T) internalMerge(entity, merged, true, options);
        } finally {
            disableListeners = false;
            mergeCollectionInProgress = null;
        }
        return result;
    }

    @Override
    public <T> T merge(T entity) {
        return merge(entity, new MergeOptions());
    }

    @Override
    public EntitySet merge(Collection<?> entities, MergeOptions options) {
        checkNotNullArgument(entities, "entity collection is null");
        checkNotNullArgument(entities, "options object is null");

        List<Object> managedList = new ArrayList<>(entities.size());
        disableListeners = true;
        mergeCollectionInProgress = new IdentityHashMap<>();
        try {
            Map<Object, Object> merged = new IdentityHashMap<>();

            for (Object entity : entities) {
                Object managed = internalMerge(entity, merged, true, options);
                managedList.add(managed);
            }
        } finally {
            disableListeners = false;
            mergeCollectionInProgress = null;
        }
        return EntitySet.of(managedList);
    }

    @Override
    public EntitySet merge(Collection<?> entities) {
        return merge(entities, new MergeOptions());
    }

    protected Object internalMerge(Object entity, Map<Object, Object> mergedMap, boolean isRoot, MergeOptions options) {
        Map<Object, Object> entityMap = content.computeIfAbsent(entity.getClass(), aClass -> new HashMap<>());

        Object nullIdEntity = nullIdEntitiesMap.get(entity);
        if (nullIdEntity != null) {
            Object managed = entityMap.get(makeKey(nullIdEntity));
            if (managed != null) {
                mergedMap.put(entity, managed);
                mergeState(entity, managed, mergedMap, isRoot, options, true);
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

            mergeState(entity, managed, mergedMap, isRoot, options, false);

            EntitySystemAccess.addPropertyChangeListener(managed, propertyChangeListener);

            if (entityStates.isNew(managed)) {
                modifiedInstances.add(managed);
                fireChangeListener(managed);
            }
        } else {
            mergedMap.put(entity, managed);
            if (managed != entity) {
                mergeState(entity, managed, mergedMap, isRoot, options, true);
            }
        }
        return managed;
    }

    protected Object makeKey(Object entity) {
        Object id = EntityValues.getId(entity);
        return Objects.requireNonNullElse(id, entity);
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
                              boolean isRoot, MergeOptions options, boolean dstExisted) {
        boolean srcNew = entityStates.isNew(srcEntity);
        boolean dstNew = entityStates.isNew(dstEntity);

        mergeSystemState(srcEntity, dstEntity, isRoot, options);

        boolean coldReset = isColdResetTarget(dstEntity, isRoot, options, dstExisted);

        resetLoadedInfoBeforeCopy(dstEntity, coldReset);

        MetaClass metaClass = getEntityMetaClass(srcEntity);

        mergeDatatypeProperties(srcEntity, dstEntity, metaClass, srcNew, dstNew, isRoot, options);

        mergeReferenceProperties(srcEntity, dstEntity, metaClass, srcNew, isRoot, options, mergedMap);

        mergeLoadedPropertiesInfo(srcEntity, dstEntity, isRoot, options, coldReset);

        reapplySetLoaded(dstEntity);

        mergeLazyLoadingState(srcEntity, dstEntity);
    }

    protected void mergeDatatypeProperties(Object srcEntity, Object dstEntity, MetaClass metaClass,
                                           boolean srcNew, boolean dstNew, boolean isRoot, MergeOptions options) {
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (!property.getRange().isClass()                                       // datatype or element collection
                    && !(metadataTools.isMethodBased(property) && property.isReadOnly())
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))    // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) { // loaded dst - have to check to avoid unfetched for local properties

                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && !options.isFresh() && value == null) {
                    continue;
                }

                if (changeTracker.isAttributeDirty(dstEntity, propertyName)
                        && !(isRoot && isOverriding(propertyName))) {
                    // dirty-aware merge rule: the destination attribute carries an unsaved user
                    // edit and is never overwritten; a fresh merge rebaselines the edit against
                    // the incoming value instead (un-dirtying it if now equal). Reading the
                    // current value is safe: a dirty attribute was written by the user through
                    // the managed instance, so it is loaded on the destination.
                    // Exception: during mergeFromChild the child's dirty attributes of the merge
                    // root override this context's own edits (see isOverriding).
                    if (options.isFresh()) {
                        changeTracker.rebaseline(dstEntity, propertyName, value,
                                EntityValues.getValue(dstEntity, propertyName), false);
                    } else if (DataContextDiagnostics.log.isDebugEnabled()) {
                        DataContextDiagnostics.log.debug(DataContextDiagnostics.mergeSkippedDirty(dstEntity, propertyName));
                    }
                    continue; // the user's value stays in place in both cases
                }

                if (value instanceof Collection<?> srcCollection) {
                    // properties reaching this branch never have a to-many reference range (that is
                    // handled below via mergeList/mergeSet), so these are always element collections of
                    // datatype values; do not snapshot a collection baseline for them - their elements
                    // are not entities and cannot be used as a tracker refKey
                    if (value instanceof List) {
                        value = createObservableList(new ArrayList<>((Collection<Object>) srcCollection), dstEntity, propertyName);
                    } else if (value instanceof Set) {
                        value = createObservableSet(new HashSet<>((Collection<Object>) srcCollection), dstEntity, propertyName);
                    } else {
                        throw new UnsupportedOperationException("Unsupported collection type: " + value.getClass().getName());
                    }
                }

                setPropertyValue(dstEntity, property, value);
            }
        }
    }

    protected void mergeReferenceProperties(Object srcEntity, Object dstEntity, MetaClass metaClass,
                                            boolean srcNew, boolean isRoot, MergeOptions options,
                                            Map<Object, Object> mergedMap) {
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass()                                               // refs and collections
                    && !(metadataTools.isMethodBased(property) && property.isReadOnly())
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))) {        // loaded src
                Object value = EntityValues.getValue(srcEntity, propertyName);

                // ignore null values in non-root source entities
                if (!isRoot && !options.isFresh() && value == null) {
                    continue;
                }

                if (skipOrRebaselineDirtyReference(dstEntity, property, value, isRoot, options, mergedMap)) {
                    continue;
                }

                if (value == null || !entityStates.isLoaded(dstEntity, propertyName)) {
                    mergeUnloadedOrNullReference(srcEntity, dstEntity, property, value);
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
                        if (property.getType() == MetaProperty.Type.EMBEDDED) {
                            EmbeddedPropertyChangeListener listener = new EmbeddedPropertyChangeListener(dstEntity, propertyName);
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
    }

    protected boolean skipOrRebaselineDirtyReference(Object dstEntity, MetaProperty property, @Nullable Object value,
                                                     boolean isRoot, MergeOptions options,
                                                     Map<Object, Object> mergedMap) {
        String propertyName = property.getName();

        // dirty-aware merge rule: a destination attribute carrying an unsaved user edit
        // is never overwritten (this must also shield it from the lazy-state transplant
        // in mergeUnloadedOrNullReference); a fresh merge rebaselines the edit against the
        // incoming value instead. For an embedded reference the user's edits are recorded
        // on the owner under dotted paths ('address.city'), so any dirty dotted path
        // protects the whole embedded value.
        boolean embedded = property.getType() == MetaProperty.Type.EMBEDDED;
        Set<String> dirtyEmbeddedPaths = embedded
                ? getDirtyEmbeddedPaths(dstEntity, propertyName)
                : Collections.emptySet();
        if ((changeTracker.isAttributeDirty(dstEntity, propertyName) || !dirtyEmbeddedPaths.isEmpty())
                && !(isRoot && isOverriding(propertyName))) {
            if (!embedded && value != null && !(value instanceof Collection) && !mergedMap.containsKey(value)) {
                // the skipped incoming node must still enter the context graph;
                // only the reassignment of dst's reference is suppressed
                internalMerge(value, mergedMap, false, options);
            }
            if (options.isFresh()) {
                // reads of current values below are safe: a dirty attribute was written
                // by the user through the managed instance, so it is loaded on the
                // destination (references hold a managed instance or null; collections
                // hold the observable wrapper)
                if (value instanceof Collection<?> incoming) {
                    Collection<?> current = (Collection<?>) EntityValues.getValue(dstEntity, propertyName);
                    changeTracker.rebaselineCollection(dstEntity, propertyName, incoming, current);
                } else if (embedded) {
                    rebaselineEmbedded(dstEntity, propertyName, value, dirtyEmbeddedPaths);
                } else {
                    changeTracker.rebaseline(dstEntity, propertyName, value,
                            EntityValues.getValue(dstEntity, propertyName), true);
                }
            } else if (DataContextDiagnostics.log.isDebugEnabled()) {
                DataContextDiagnostics.log.debug(DataContextDiagnostics.mergeSkippedDirty(dstEntity, propertyName));
            }
            return true;
        }
        return false;
    }

    protected void mergeUnloadedOrNullReference(Object srcEntity, Object dstEntity, MetaProperty property,
                                                @Nullable Object value) {
        if (property.getType() != MetaProperty.Type.EMBEDDED) {//dstEntity property value will be lazy loaded and replaced by srcEntity property value
            String propertyName = property.getName();
            if (value instanceof Collection) {
                // a to-many value installed on an unloaded dst property must notify the
                // context on mutation like every other merge-installed collection
                boolean[] wrappedLazy = new boolean[1];
                entitySystemStateSupport.mergeLazyLoadingState((Entity) srcEntity, (Entity) dstEntity, property,
                        collection -> {
                            wrappedLazy[0] = true;
                            return wrapLazyValueIntoObservableCollection(collection, dstEntity, propertyName);
                        });
                if (!wrappedLazy[0]) {
                    Collection<Object> installed = wrapLazyValueIntoObservableCollection(
                            (Collection<Object>) value, dstEntity, propertyName);
                    setPropertyValue(dstEntity, property, installed);
                }
                // a non-null value is now present in memory; reflect it in the loaded-state info
                markLoaded(dstEntity, propertyName);
            } else {
                setPropertyValue(dstEntity, property, value);
                if (value != null) {
                    markLoaded(dstEntity, propertyName);
                }
            }
        }
    }

    /**
     * Marks an attribute as loaded on the entity's loaded-state info, so {@code isLoaded} reflects a value
     * that is present in memory because it was set (a user edit) or installed by a merge, rather than lazily
     * materialized. No-op for entities without a {@link LoadedPropertiesInfo} (e.g. DTOs).
     */
    protected void markLoaded(Object entity, String propertyName) {
        LoadedPropertiesInfo info = EntitySystemAccess.getEntityEntry(entity).getLoadedPropertiesInfo();
        if (info != null) {
            info.registerProperty(propertyName, true);
            changeTracker.markSetLoaded(entity, propertyName);
        }
    }

    /**
     * Re-asserts the set-loaded markers of an entity into its current {@link LoadedPropertiesInfo}, so a value
     * that is present in memory because it was set or merge-installed keeps reporting loaded after a fresh merge
     * replaces the loaded-state cache with a narrower source's (in {@link #mergeLoadedPropertiesInfo}), whose
     * negative for the attribute would otherwise shadow the fetch-group state. Writes only positive entries for
     * recorded attributes, so it can never turn a genuinely-unloaded attribute loaded. No-op for entities
     * without a cache. Runs after every merge, not only fresh ones; on the cold-reset path (which recomputes
     * loaded-state from the fetch group) it re-affirms the same markers, safe by that positive-only invariant
     * rather than by being inert.
     */
    protected void reapplySetLoaded(Object dstEntity) {
        LoadedPropertiesInfo info = EntitySystemAccess.getEntityEntry(dstEntity).getLoadedPropertiesInfo();
        if (info != null) {
            for (String attribute : changeTracker.setLoadedAttributes(dstEntity)) {
                info.registerProperty(attribute, true);
            }
        }
    }

    /**
     * Whether the dirty-protection of the given property of the merge root is suspended by the
     * current {@link #mergeFromChild(Object, Set)} call. A plain property is overridden when it
     * is among the child's dirty attributes; an embedded property is also overridden when any
     * child dirty attribute is a dotted path under it ({@code 'address.city'} overrides the
     * protection of {@code 'address'}).
     */
    protected boolean isOverriding(String propertyName) {
        if (overridingAttributes.isEmpty()) {
            return false;
        }
        if (overridingAttributes.contains(propertyName)) {
            return true;
        }
        String prefix = propertyName + ".";
        for (String attribute : overridingAttributes) {
            if (attribute.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dotted attribute paths ({@code 'address.city'}) of the given embedded property that are
     * dirty on the owning managed instance. User edits to embedded sub-attributes are tracked
     * on the owner under such paths (see {@link EmbeddedPropertyChangeListener}).
     */
    protected Set<String> getDirtyEmbeddedPaths(Object dstEntity, String propertyName) {
        Set<String> modified = changeTracker.getModifiedAttributes(dstEntity);
        if (modified.isEmpty()) {
            return Collections.emptySet();
        }
        String prefix = propertyName + ".";
        Set<String> result = null;
        for (String attribute : modified) {
            if (attribute.startsWith(prefix)) {
                if (result == null) {
                    result = new LinkedHashSet<>();
                }
                result.add(attribute);
            }
        }
        return result == null ? Collections.emptySet() : result;
    }

    /**
     * Fresh-merge rebaselining for an embedded property whose copy was skipped because the owner
     * carries dirty state for it: the embedded reference entry itself (if dirty) and each dirty
     * dotted sub-attribute path, using the sub-attribute values of the incoming (unmanaged)
     * embedded instance.
     */
    protected void rebaselineEmbedded(Object dstEntity, String propertyName, @Nullable Object incomingEmbedded,
                                      Set<String> dirtyEmbeddedPaths) {
        if (changeTracker.isAttributeDirty(dstEntity, propertyName)) {
            changeTracker.rebaseline(dstEntity, propertyName, incomingEmbedded,
                    EntityValues.getValue(dstEntity, propertyName), true);
        }
        if (incomingEmbedded == null) {
            // no incoming sub-values to rebaseline against; the user's edits stay dirty
            return;
        }
        Object currentEmbedded = EntityValues.getValue(dstEntity, propertyName);
        for (String path : dirtyEmbeddedPaths) {
            String subAttribute = path.substring(propertyName.length() + 1);
            if (!entityStates.isLoaded(incomingEmbedded, subAttribute)) {
                // cannot compare: keep the existing baseline and dirty state
                continue;
            }
            Object incomingValue = EntityValues.getValue(incomingEmbedded, subAttribute);
            Object currentValue = currentEmbedded == null ? null
                    : EntityValues.getValue(currentEmbedded, subAttribute);
            changeTracker.rebaseline(dstEntity, path, incomingValue, currentValue, false);
        }
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
            // Preserve the loaded state in the destination entity
            EntityEntry dstEntityEntry = EntitySystemAccess.getEntityEntry(dstEntity);
            LoadedPropertiesInfo prevLoadedPropertiesInfo = dstEntityEntry.getLoadedPropertiesInfo();

            entitySystemStateSupport.mergeSystemState((Entity) srcEntity, (Entity) dstEntity);

            dstEntityEntry.setLoadedPropertiesInfo(prevLoadedPropertiesInfo);
        }
    }

    /**
     * Whether this is a root non-fresh or a fresh merge onto a pre-existing managed instance whose
     * loaded-state info is the caching kind. Such an instance carries source-relative negatives cached
     * at earlier merges, and its fetch group was just unioned with the source's in {@link #mergeSystemState}.
     * In both cases the loaded-state cache is recomputed from that unioned fetch group (blanked before the
     * copy loops in {@link #resetLoadedInfoBeforeCopy}) instead of being replaced with the source's, so a
     * fetched attribute the narrower source omits is not reverted to unloaded by a copied source negative.
     */
    protected boolean isColdResetTarget(Object dstEntity, boolean isRoot, MergeOptions options, boolean dstExisted) {
        return (isRoot || options.isFresh()) && dstExisted
                && EntitySystemAccess.getEntityEntry(dstEntity).getLoadedPropertiesInfo() instanceof CachingLoadedPropertiesInfo;
    }

    protected void resetLoadedInfoBeforeCopy(Object dstEntity, boolean coldReset) {
        if (coldReset) {
            // Reset before the copy loops so their isLoaded(dstEntity, ...) checks recompute from the
            // fetch group just unioned in mergeSystemState. Otherwise a stale cached negative suppresses
            // the copy of a newly available value, and the end-of-merge reset then makes that attribute
            // report loaded while holding a default - a later save writes null over real data.
            // (see specs/limitations.md cluster 2)
            EntitySystemAccess.getEntityEntry(dstEntity).setLoadedPropertiesInfo(new CachingLoadedPropertiesInfo());
        }
    }

    protected void mergeLoadedPropertiesInfo(Object srcEntity, Object dstEntity, boolean isRoot, MergeOptions options,
                                             boolean coldReset) {
        if (isRoot || options.isFresh()) {
            if (coldReset) {
                // already reset before the copy loops (resetLoadedInfoBeforeCopy); the loops repopulated
                // correct answers into the fresh cache from the unioned fetch group, so leave it as is
                return;
            }
            EntityEntry srcEntityEntry = EntitySystemAccess.getEntityEntry(srcEntity);
            EntityEntry dstEntityEntry = EntitySystemAccess.getEntityEntry(dstEntity);
            if (srcEntityEntry.getLoadedPropertiesInfo() == null) {
                dstEntityEntry.setLoadedPropertiesInfo(null);
            } else {
                dstEntityEntry.setLoadedPropertiesInfo(srcEntityEntry.getLoadedPropertiesInfo().copy());
            }
        }
    }

    protected void mergeLazyLoadingState(Object srcEntity, Object dstEntity) {
        MetaClass metaClass = getEntityMetaClass(srcEntity);
        if (!metadataTools.isJpaEntity(metaClass))
            return;

        boolean srcNew = entityStates.isNew(srcEntity);

        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (property.getRange().isClass() && !metadataTools.isMethodBased(property)
                    && !srcNew && !entityStates.isLoaded(srcEntity, propertyName)
                    && !entityStates.isLoaded(dstEntity, propertyName)) {
                entitySystemStateSupport.mergeLazyLoadingState((Entity) srcEntity, (Entity) dstEntity, property,
                        collection -> wrapLazyValueIntoObservableCollection(collection, dstEntity, propertyName));
            }
        }

    }

    protected void mergeList(List<Object> list, Object managedEntity, MetaProperty property, boolean replace,
                             MergeOptions options, Map<Object, Object> mergedMap) {
        String propertyName = property.getName();
        if (!beginMergeCollection(managedEntity, propertyName)) {
            return;
        }
        try {
            if (replace) {
                List<Object> managedRefs = new ArrayList<>(list.size());
                for (Object entity : list) {
                    Object managedRef = internalMerge(entity, mergedMap, false, options);
                    managedRefs.add(managedRef);
                }
                changeTracker.snapshotCollectionBaseline(managedEntity, propertyName, managedRefs);
                List<Object> dstList = createObservableList(managedRefs, managedEntity, propertyName);
                setPropertyValue(managedEntity, property, dstList);

            } else {
                Object managedValue = EntityValues.getValue(managedEntity, propertyName);

                List<Object> dstList = null;
                if (managedValue instanceof List) {
                    dstList = (List<Object>) managedValue;
                } else if (managedValue != null) {//any proxy Collection can be returned in case of Collection entity attribute (see Haulmont/jmix-ui#243)
                    dstList = new ArrayList<>((Collection<?>) managedValue);
                }

                if (dstList == null) {
                    dstList = createObservableList(managedEntity, propertyName);
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
                changeTracker.snapshotCollectionBaseline(managedEntity, propertyName, dstList);
            }
        } finally {
            endMergeCollection(managedEntity, propertyName);
        }
    }

    protected void mergeSet(Set<Object> set, Object managedEntity, MetaProperty property, boolean replace,
                            MergeOptions options, Map<Object, Object> mergedMap) {
        String propertyName = property.getName();
        if (!beginMergeCollection(managedEntity, propertyName)) {
            return;
        }
        try {
            if (replace) {
                Set<Object> managedRefs = new LinkedHashSet<>(set.size());
                for (Object entity : set) {
                    Object managedRef = internalMerge(entity, mergedMap, false, options);
                    managedRefs.add(managedRef);
                }
                changeTracker.snapshotCollectionBaseline(managedEntity, propertyName, managedRefs);
                Set<Object> dstSet = createObservableSet(managedRefs, managedEntity, propertyName);
                setPropertyValue(managedEntity, property, dstSet);

            } else {
                Object managedValue = EntityValues.getValue(managedEntity, propertyName);

                Set<Object> dstSet = null;
                if (managedValue instanceof Set) {
                    dstSet = (Set<Object>) managedValue;
                } else if (managedValue != null) {//any proxy Collection can be returned in case of Collection entity attribute (see Haulmont/jmix-ui#243)
                    dstSet = new LinkedHashSet<>((Collection<?>) managedValue);
                }


                if (dstSet == null) {
                    dstSet = createObservableSet(managedEntity, propertyName);
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
                changeTracker.snapshotCollectionBaseline(managedEntity, propertyName, dstSet);
            }
        } finally {
            endMergeCollection(managedEntity, propertyName);
        }
    }

    /**
     * Marks the given owner entity's collection property as being merged within the active merge() call.
     * Returns {@code false} if it is already in progress (a re-entry, caused by a source graph holding
     * multiple java instances of the same id joined by a bidirectional reference), in which case the
     * caller must return early and let the outer mergeList/mergeSet finish populating the collection.
     */
    protected boolean beginMergeCollection(Object managedEntity, String propertyName) {
        return mergeCollectionInProgress.computeIfAbsent(managedEntity, k -> new HashSet<>()).add(propertyName);
    }

    protected void endMergeCollection(Object managedEntity, String propertyName) {
        Set<String> props = mergeCollectionInProgress.get(managedEntity);
        if (props != null) {
            props.remove(propertyName);
            if (props.isEmpty()) {
                mergeCollectionInProgress.remove(managedEntity);
            }
        }
    }

    protected Collection<Object> wrapLazyValueIntoObservableCollection(Collection<Object> collection, Object notifiedEntity,
                                                                        @Nullable String property) {
        if (collection instanceof List) {
            if (property != null) {
                changeTracker.snapshotCollectionBaseline(notifiedEntity, property, collection);
            }
            return createObservableList((List<Object>) collection, notifiedEntity, property);
        } else if (collection instanceof Set) {
            if (property != null) {
                changeTracker.snapshotCollectionBaseline(notifiedEntity, property, collection);
            }
            return createObservableSet((Set<Object>) collection, notifiedEntity, property);
        }
        return collection;
    }

    protected List<Object> createObservableList(Object notifiedEntity, @Nullable String property) {
        return createObservableList(new ArrayList<>(), notifiedEntity, property);
    }

    protected List<Object> createObservableList(List<Object> list, Object notifiedEntity, @Nullable String property) {
        return new ObservableList<>(list, (changeType, changes) -> collectionChanged(notifiedEntity, property));
    }

    protected Set<Object> createObservableSet(Object notifiedEntity, @Nullable String property) {
        return createObservableSet(new LinkedHashSet<>(), notifiedEntity, property);
    }

    protected ObservableSet<Object> createObservableSet(Set<Object> set, Object notifiedEntity, @Nullable String property) {
        return new ObservableSet<>(set, (changeType, changes) -> collectionChanged(notifiedEntity, property));
    }

    protected void collectionChanged(Object entity, @Nullable String property) {
        if (disableListeners) {
            return;
        }
        // Unconditional mark-and-notify first, matching the scalar PropertyChangeListener's semantics:
        // the tracker's transition below may remove the entity from modifiedInstances again once the
        // tracked attribute becomes clean (e.g. a collection mutation reverted to its baseline). Calling
        // the tracker before this unconditional marking would let this marking immediately undo a
        // just-computed clean transition.
        modified(entity);
        if (property != null) {
            MetaProperty metaProperty = metadata.getClass(entity).findProperty(property);
            if (metaProperty != null && metaProperty.getRange().isClass()) {
                Collection<?> current = entityStates.isLoaded(entity, property)
                        ? (Collection<?>) EntityValues.getValue(entity, property) : null;
                if (current != null) {
                    changeTracker.trackCollectionChange(entity, property, current);
                }
            }
        }
    }

    @Override
    public void remove(Object entity) {
        checkNotNullArgument(entity, "entity is null");

        modifiedInstances.remove(entity);
        compositionModifiedOwners.remove(entity);
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
                changeTracker.drop(mergedEntity);
                manuallyModified.remove(entity);
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
                            Collection<?> collection = EntityValues.getValue(entity, metaProperty.getName());
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
                changeTracker.drop(mergedEntity);
                manuallyModified.remove(entity);
            }
            modifiedInstances.remove(entity);
            removedInstances.remove(entity);
            compositionModifiedOwners.remove(entity);
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
    public void clearChanges() {
        modifiedInstances.clear();
        removedInstances.clear();
        changeTracker.clear();
        manuallyModified.clear();
        compositionModifiedOwners.clear();
    }

    @Override
    public boolean isModified(Object entity) {
        return modifiedInstances.contains(entity) || compositionModifiedOwners.contains(entity);
    }

    @Override
    public void setModified(Object entity, boolean modified) {
        Object merged = find(entity);
        if (merged == null) {
            return;
        }
        if (modified) {
            manuallyModified.add(merged);
            modifiedInstances.add(merged);
        } else {
            manuallyModified.remove(merged);
            modifiedInstances.remove(merged);
            compositionModifiedOwners.remove(merged);
            changeTracker.drop(merged);
        }
    }

    @Override
    public Set<Object> getModified() {
        return Collections.unmodifiableSet(modifiedInstances);
    }

    @Override
    public Set<String> getModifiedAttributes(Object entity) {
        Object managed = find(entity);
        if (managed == null) {
            return Collections.emptySet();
        }
        return changeTracker.getModifiedAttributes(managed);
    }

    protected void entityBecameDirty(Object entity) {
        modifiedInstances.add(entity);
    }

    protected void entityBecameClean(Object entity) {
        if (!manuallyModified.contains(entity)) {
            modifiedInstances.remove(entity);
        }
    }

    @Override
    public boolean isRemoved(Object entity) {
        return removedInstances.contains(entity);
    }

    @Override
    public Set<Object> getRemoved() {
        return Collections.unmodifiableSet(removedInstances);
    }

    @Override
    public EntitySet save() {
        return save(true);
    }

    @Override
    public EntitySet save(boolean reloadSaved) {
        PreSaveEvent preSaveEvent = new PreSaveEvent(this, modifiedInstances, removedInstances);
        events.publish(PreSaveEvent.class, preSaveEvent);
        if (preSaveEvent.isSavePrevented())
            return EntitySet.of(Collections.emptySet());

        EntitySet savedAndMerged;
        try {
            Set<Object> saved = performSave(reloadSaved);
            // the user's edits have been consumed by the save; drop attribute-level dirty state
            // now so the merge-back of saved results below is not mistaken for a conflicting
            // overwrite of unsaved edits and skipped by the dirty-aware merge rule
            changeTracker.clear();
            if (reloadSaved) {
                savedAndMerged = mergeSaved(saved);
            } else {
                savedAndMerged = EntitySet.of(Collections.emptySet());
            }
        } finally {
            nullIdEntitiesMap.clear();
        }

        events.publish(PostSaveEvent.class, new PostSaveEvent(this, savedAndMerged, reloadSaved));

        modifiedInstances.clear();
        removedInstances.clear();
        changeTracker.clear();
        manuallyModified.clear();
        compositionModifiedOwners.clear();

        return savedAndMerged;
    }

    @Override
    public Subscription addPreSaveListener(Consumer<PreSaveEvent> listener) {
        return events.subscribe(PreSaveEvent.class, listener);
    }

    @Override
    public Subscription addPostSaveListener(Consumer<PostSaveEvent> listener) {
        return events.subscribe(PostSaveEvent.class, listener);
    }

    @Override
    public Function<SaveContext, Set<Object>> getSaveDelegate() {
        return saveDelegate;
    }

    @Override
    public void setSaveDelegate(Function<SaveContext, Set<Object>> delegate) {
        this.saveDelegate = delegate;
    }

    protected Set<Object> performSave(boolean reloadSaved) {
        if (!hasChanges())
            return Collections.emptySet();

        if (parentContext == null) {
            return saveToDataManager(reloadSaved);
        } else {
            return saveToParentContext();
        }
    }

    protected Set<Object> saveToDataManager(boolean reloadSaved) {
        SaveContext saveContext = new SaveContext()
                .saving(isolate(filterSavedInstances(modifiedInstances)))
                .removing(isolate(filterSavedInstances(removedInstances)))
                .setDiscardSaved(!reloadSaved);

        entityReferencesNormalizer.updateReferences(saveContext.getEntitiesToSave());
        updateFetchPlans(saveContext);

        if (saveDelegate == null) {
            return dataManager.save(saveContext);
        } else {
            return saveDelegate.apply(saveContext);
        }
    }

    protected List<Object> filterSavedInstances(Set<Object> instances) {
        return instances.stream()
                .filter(entity -> !metadataTools.isJpaEmbeddable(entity.getClass()))
                .collect(Collectors.toList());
    }

    protected void updateFetchPlans(SaveContext saveContext) {
        for (Object entity : saveContext.getEntitiesToSave()) {
            saveContext.getFetchPlans().put(entity, entityStates.getCurrentFetchPlan(entity));
        }
    }

    public Collection<Object> isolate(List<Object> entities) {
        // re-serialize the whole collection to preserve links between objects
        List<Object> isolatedEntities = copier.copy(entities);
        for (int i = 0; i < isolatedEntities.size(); i++) {
            Object isolatedEntity = isolatedEntities.get(i);
            Object entity = entities.get(i);
            if (EntityValues.getId(entity) == null) {
                nullIdEntitiesMap.put(isolatedEntity, entity);
            }
        }
        return isolatedEntities;
    }

    @Override
    public Object mergeFromChild(Object entity, Set<String> childDirtyAttributes) {
        if (childDirtyAttributes.isEmpty()) {
            return merge(entity);
        }
        // capture this context's pre-overwrite values: they become the baselines of the
        // attributes the child's merge is about to overwrite (when not already dirty here)
        Object managed = find(entity);
        Map<String, Object> preMergeValues = new HashMap<>();
        if (managed != null) {
            // the merge below resets this instance's loaded-state cache before its copy loops
            // (resetLoadedInfoBeforeCopy, same condition as this managed root), so probing isLoaded
            // here cannot poison the merge's own checks - no disposable-copy guard is needed
            for (String attribute : childDirtyAttributes) {
                if (entityStates.isLoaded(managed, rootSegment(attribute))) {
                    preMergeValues.put(attribute, valueForBaseline(managed, attribute));
                }
            }
        }
        overridingAttributes = childDirtyAttributes; // consulted by the merge-rule checks
        Object result;
        try {
            result = merge(entity);
        } finally {
            overridingAttributes = Set.of();
        }
        for (String attribute : childDirtyAttributes) {
            if (changeTracker.isAttributeDirty(result, attribute)) {
                // already dirty here: keep the existing baseline, the child's value replaced
                // the current value only
                continue;
            }
            if (!entityStates.isLoaded(result, rootSegment(attribute))) {
                // edge guard for the dirty-implies-loaded invariant: after the merge the child's
                // dirty attributes are normally loaded here (the fetch groups were unioned and the
                // child's values copied), so this fires only in unusual states; never register
                // dirt for an unloaded attribute (rebaseline reads would hit unfetched state)
                log.debug("Skipping dirty union of '{}' from child context: not loaded on the parent instance {}",
                        attribute, result);
                continue;
            }
            changeTracker.markDirty(result, attribute, preMergeValues.get(attribute));
        }
        markCompositionOwnersModified(result);
        return result;
    }

    /**
     * Baseline-shaped current value of the given attribute of a managed instance, in the form
     * {@link DataContextChangeTracker} compares against: a scalar value as is, a reference as
     * its id, a to-many attribute as the membership bag of its current contents, a dotted path
     * via {@link EntityValues#getValueEx(Object, String)}. The root segment of the attribute
     * must be loaded on the instance.
     */
    @Nullable
    protected Object valueForBaseline(Object entity, String attribute) {
        if (attribute.indexOf('.') >= 0) {
            // dotted (embedded) paths are tracked with raw sub-attribute values
            return EntityValues.getValueEx(entity, attribute);
        }
        Object value = EntityValues.getValue(entity, attribute);
        if (value == null) {
            return null;
        }
        MetaProperty property = getEntityMetaClass(entity).findProperty(attribute);
        if (property != null && property.getRange().isClass()) {
            if (property.getRange().getCardinality().isMany()) {
                return DataContextChangeTracker.membershipBag((Collection<?>) value);
            }
            Object id = EntityValues.getId(value);
            return id != null ? id : value;
        }
        return value;
    }

    protected static String rootSegment(String attribute) {
        int dotIndex = attribute.indexOf('.');
        return dotIndex < 0 ? attribute : attribute.substring(0, dotIndex);
    }

    /**
     * Marks the composition owner chain of a just-merged composition child for reopen protection in
     * this (parent) context. A deep-composition edit propagated from a child context dirties only the
     * edited leaf; the intermediate owners the child never marked would otherwise stay clean, so
     * reopening an intermediate editor reloads a stale instance and loses the unsaved deeper edit.
     * Owners go into compositionModifiedOwners (consulted by isModified), NOT into
     * modifiedInstances, so they are never persisted - saving an aggregate must still persist only
     * the entities that actually changed.
     */
    protected void markCompositionOwnersModified(Object entity) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Object current = entity;
        while (current != null && visited.add(current)) {
            Object owner = findCompositionOwner(current);
            if (owner != null) {
                compositionModifiedOwners.add(owner);
            }
            current = owner;
        }
    }

    /**
     * Resolves the composition owner of a managed instance in this context. Inverse first: a
     * reference whose inverse is a composition points back to the owner. Scan fallback (when no
     * loaded inverse owner is found): a managed instance whose composition property references
     * {@code entity}. Returns the managed owner instance, or {@code null} if none is found.
     */
    @Nullable
    protected Object findCompositionOwner(Object entity) {
        MetaClass metaClass = getEntityMetaClass(entity);
        for (MetaProperty property : metaClass.getProperties()) {
            MetaProperty inverse = property.getInverse();
            if (property.getRange().isClass()
                    && inverse != null && inverse.getType() == MetaProperty.Type.COMPOSITION
                    && entityStates.isLoaded(entity, property.getName())) {
                Object owner = EntityValues.getValue(entity, property.getName());
                if (owner != null) {
                    Object managedOwner = find(owner);
                    if (managedOwner != null) {
                        return managedOwner;
                    }
                }
            }
        }
        for (Map<Object, Object> entityMap : content.values()) {
            for (Object candidate : entityMap.values()) {
                if (candidate == entity) {
                    continue;
                }
                for (MetaProperty property : getEntityMetaClass(candidate).getProperties()) {
                    if (property.getType() == MetaProperty.Type.COMPOSITION
                            && entityStates.isLoaded(candidate, property.getName())) {
                        Object value = EntityValues.getValue(candidate, property.getName());
                        if (value == null) {
                            continue;
                        }
                        if (property.getRange().getCardinality().isMany()) {
                            if (value instanceof Collection<?> collection
                                    && collection.stream().anyMatch(e -> e == entity)) {
                                return candidate;
                            }
                        } else if (value == entity) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Set<Object> saveToParentContext() {
        Set<Object> savedEntities = new HashSet<>();
        for (Object entity : modifiedInstances) {
            Object merged = parentContext.mergeFromChild(entity, changeTracker.getModifiedAttributes(entity));
            parentContext.getModifiedInstances().add(merged);
            savedEntities.add(merged);
        }
        for (Object entity : removedInstances) {
            parentContext.remove(entity);
            cleanupContextAfterRemoveEntity(parentContext, entity);
        }
        return savedEntities;
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

    protected EntitySet mergeSaved(Set<Object> saved) {
        // transform into sorted collection to have reproducible behavior
        List<Object> entitiesToMerge = new ArrayList<>();
        for (Object entity : saved) {
            Object e = nullIdEntitiesMap.getOrDefault(entity, entity);
            if (contains(e)) {
                entitiesToMerge.add(entity);
            }
        }
        entitiesToMerge.sort(Comparator.comparing(Object::hashCode));

        return merge(entitiesToMerge);
    }

    public Collection<?> getAll() {
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
        sb.append(printObject(entity)).append(" ").append(entity).append("\n");

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
                String str = printEntity(value, level + 1, visited);
                if (!str.equals(""))
                    sb.append(prefix).append(str);
            } else if (value instanceof Collection) {
                sb.append(prefix).append(value.getClass().getSimpleName()).append("[\n");
                for (Object item : (Collection) value) {
                    String str = printEntity(item, level + 1, visited);
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

    @Override
    public Set<Object> getRemovedInstances() {
        return removedInstances;
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
            String primaryKeyPropertyName = getPrimaryKeyPropertyName(e.getItem());
            // if id has been changed, put the entity to the content with the new id
            if (e.getProperty().equals(primaryKeyPropertyName)) {
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
                // Unconditional add preserves today's semantics for to-many and unclassifiable properties;
                // the tracker's transition callback below may remove the entity again once the tracked
                // attributes are all clean (e.g. a scalar/reference edit reverted to its baseline).
                modifiedInstances.add(e.getItem());

                MetaProperty changedProperty = metadata.getClass(e.getItem()).findProperty(e.getProperty());
                if (changedProperty != null && changedProperty.getRange().isClass()
                        && changedProperty.getRange().getCardinality().isMany()) {
                    // to-many properties are handled by the observable collection wrappers; skip the tracker here
                } else if (e.getProperty().equals(primaryKeyPropertyName)) {
                    // an id change is identity bookkeeping (e.g. setId() in a saveDelegate),
                    // not a user attribute edit; skip the tracker
                } else {
                    boolean reference = changedProperty != null && changedProperty.getRange().isClass()
                            && !changedProperty.getRange().getCardinality().isMany();
                    changeTracker.trackChange(e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue(), reference);
                    // a user set makes the attribute authoritative and present in memory (even a set to null),
                    // so it is loaded regardless of whether it was fetched
                    markLoaded(e.getItem(), e.getProperty());
                }
                fireChangeListener(e.getItem());
            }
        }
    }

    protected class EmbeddedPropertyChangeListener implements EntityPropertyChangeListener {

        private final Object entity;

        private final String embeddedPropertyName;

        public EmbeddedPropertyChangeListener(Object entity, String embeddedPropertyName) {
            this.entity = entity;
            this.embeddedPropertyName = embeddedPropertyName;
        }

        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            if (!disableListeners) {
                modifiedInstances.add(entity);

                MetaProperty changedProperty = metadata.getClass(e.getItem()).findProperty(e.getProperty());
                if (changedProperty != null && changedProperty.getRange().isClass()
                        && changedProperty.getRange().getCardinality().isMany()) {
                    // to-many properties are handled by the observable collection wrappers; skip the tracker here
                } else {
                    changeTracker.trackChange(entity, embeddedPropertyName + '.' + e.getProperty(),
                            e.getPrevValue(), e.getValue(), false);
                }
                fireChangeListener(entity);
            }
        }
    }
}
