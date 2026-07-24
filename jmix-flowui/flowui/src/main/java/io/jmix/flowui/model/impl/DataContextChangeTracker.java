/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.entity.EntityValues;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Per-attribute dirty state of a {@link DataContextImpl}: which attributes of which managed
 * instances the user changed, and the baseline value each change is measured against.
 * Baselines exist only for dirty attributes; an attribute whose current value returns to its
 * baseline is removed. Entity-level dirty membership is signalled through the two callbacks.
 */
public class DataContextChangeTracker {

    // entity -> attribute -> baseline (scalar value, reference id, or membership bag for collections)
    private final Map<Object, Map<String, Object>> baselines = new IdentityHashMap<>();

    // entity -> set of currently-dirty attributes. An attribute may have a baseline stored
    // (e.g. a collection snapshot taken before mutation) without being dirty yet.
    private final Map<Object, Set<String>> dirtyAttrs = new IdentityHashMap<>();

    // entity -> attributes made loaded by a user set or a non-null merge-install. Lets
    // DataContextImpl.reapplySetLoaded restore the loaded flag after a fresh merge installs a narrower
    // loaded-state cache. Dropped when an entity leaves the context (drop), but NOT by clear().
    private final Map<Object, Set<String>> setLoadedAttrs = new IdentityHashMap<>();

    private final Consumer<Object> onEntityDirty;
    private final Consumer<Object> onEntityClean;

    /**
     * @param onEntityDirty callback invoked when an entity becomes dirty (gains its first dirty attribute)
     * @param onEntityClean callback invoked when an entity becomes clean again (loses its last dirty attribute)
     */
    public DataContextChangeTracker(Consumer<Object> onEntityDirty, Consumer<Object> onEntityClean) {
        this.onEntityDirty = onEntityDirty;
        this.onEntityClean = onEntityClean;
    }

    /**
     * Records a scalar or single-reference attribute change against its baseline. If a baseline already
     * exists for the attribute, the change is measured against it: the attribute is un-dirtied when the
     * current value returns to the baseline, and nothing else happens otherwise. With no baseline yet,
     * the attribute becomes dirty (with {@code prevValue} as baseline) only when the value actually changed.
     *
     * @param reference {@code true} to compare by entity id / identity rather than by value
     */
    public void trackChange(Object entity, String attribute, @Nullable Object prevValue,
                      @Nullable Object newValue, boolean reference) {
        Object prev = reference ? refKey(prevValue) : prevValue;
        Object curr = reference ? refKey(newValue) : newValue;
        Map<String, Object> entityBaselines = baselines.get(entity);
        if (entityBaselines != null && entityBaselines.containsKey(attribute)) {
            if (Objects.equals(entityBaselines.get(attribute), curr)) {
                removeDirty(entity, attribute);
            }
            return;
        }
        if (Objects.equals(prev, curr)) {
            return;
        }
        putDirty(entity, attribute, prev);
    }

    /**
     * Snapshots the membership of a to-many attribute as its baseline, so a later mutation can be
     * compared against it (see {@link #trackCollectionChange}). Called by the merge when it installs a
     * collection. Only a clean attribute's baseline is (re)snapshotted; a dirty attribute keeps its
     * existing baseline so an unsaved user edit is not measured against freshly merged contents.
     */
    public void snapshotCollectionBaseline(Object entity, String attribute, Collection<?> baselineContents) {
        if (!isAttributeDirty(entity, attribute)) {
            // A clean attribute's baseline must track what merge just installed (merge legitimately
            // replaces a clean collection's contents), otherwise later mutations would be compared
            // against stale membership. A dirty attribute keeps its baseline: mergeFromChild relies on
            // the child's baseline surviving, and putDirty overwrites it when the value actually changes.
            baselines.computeIfAbsent(entity, e -> new HashMap<>())
                    .put(attribute, membershipBag(baselineContents));
        }
    }

    /**
     * Records the current membership of a to-many attribute against its snapshotted baseline: the
     * attribute is un-dirtied when the membership matches the baseline again, and marked dirty when it
     * differs. When no baseline was snapshotted the attribute is marked dirty conservatively (the change
     * cannot be reconstructed). Called from the observable-collection mutation callback.
     */
    public void trackCollectionChange(Object entity, String attribute, Collection<?> current) {
        Map<String, Object> entityBaselines = baselines.get(entity);
        Object baseline = entityBaselines == null ? null : entityBaselines.get(attribute);
        if (baseline == null) {
            // no pre-mutation snapshot: cannot reconstruct, treat conservatively as dirty
            // with the current membership as baseline-of-record minus nothing (coarse dirty)
            putDirty(entity, attribute, DIRTY_WITHOUT_BASELINE);
            return;
        }
        if (baseline.equals(membershipBag(current))) {
            if (dirtyAttributes(entity).contains(attribute)) {
                removeDirtyKeepBaseline(entity, attribute);
            }
        } else {
            markAttributeDirty(entity, attribute);
        }
    }

    /**
     * Whether the given attribute of the entity currently carries an unsaved change.
     */
    public boolean isAttributeDirty(Object entity, String attribute) {
        return dirtyAttributes(entity).contains(attribute);
    }

    /**
     * The names of the entity's currently-dirty attributes (embedded sub-attributes as dotted paths),
     * as an unmodifiable snapshot; empty if the entity is clean.
     */
    public Set<String> getModifiedAttributes(Object entity) {
        Set<String> attrs = dirtyAttrs.get(entity);
        return attrs == null || attrs.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(attrs));
    }

    /**
     * Moves a dirty scalar or single-reference attribute's baseline to an incoming (merged) value, so the
     * user's edit is henceforth measured against it; the attribute is un-dirtied if it now equals that
     * value. No-op when the attribute is not dirty. Used by a fresh merge, which rebaselines protected
     * user edits rather than overwriting them.
     *
     * @param reference {@code true} to compare by entity id / identity rather than by value
     */
    public void rebaseline(Object entity, String attribute, @Nullable Object incomingValue,
                     @Nullable Object currentValue, boolean reference) {
        if (!isAttributeDirty(entity, attribute)) return;
        Object incoming = reference ? refKey(incomingValue) : incomingValue;
        Object current = reference ? refKey(currentValue) : currentValue;
        baselines.get(entity).put(attribute, incoming);
        if (Objects.equals(incoming, current)) {
            removeDirty(entity, attribute);
        } else if (DataContextDiagnostics.log.isDebugEnabled()) {
            DataContextDiagnostics.log.debug(DataContextDiagnostics.baselineRebased(entity, attribute, incoming));
        }
    }

    /**
     * The to-many counterpart of {@link #rebaseline}: moves a dirty collection attribute's baseline to the
     * membership of an incoming (merged) collection, un-dirtying it if the current membership now matches.
     * No-op when the attribute is not dirty.
     */
    public void rebaselineCollection(Object entity, String attribute, Collection<?> incoming, Collection<?> current) {
        if (!isAttributeDirty(entity, attribute)) return;
        Map<Object, Integer> bag = membershipBag(incoming);
        baselines.get(entity).put(attribute, bag);
        if (bag.equals(membershipBag(current))) {
            // keep the new baseline: unlike scalars, a collection mutation callback
            // cannot reconstruct a lost snapshot from the change event alone
            removeDirtyKeepBaseline(entity, attribute);
        } else if (DataContextDiagnostics.log.isDebugEnabled()) {
            DataContextDiagnostics.log.debug(DataContextDiagnostics.baselineRebased(entity, attribute, bag));
        }
    }

    /**
     * Marks an attribute dirty with an explicit baseline, without a value comparison. Used to import
     * dirty state from a child context (see {@code DataContextImpl.mergeFromChild}).
     */
    public void markDirty(Object entity, String attribute, @Nullable Object baseline) {
        putDirty(entity, attribute, baseline);
    }

    /**
     * Remembers that an attribute was made loaded by a user set or a non-null merge-install, so the loaded
     * flag can be re-asserted after a fresh merge installs a narrower loaded-state cache (see
     * {@link #setLoadedAttributes} and {@code DataContextImpl.reapplySetLoaded}).
     */
    public void markSetLoaded(Object entity, String attribute) {
        setLoadedAttrs.computeIfAbsent(entity, e -> new HashSet<>()).add(attribute);
    }

    /**
     * The attributes previously recorded by {@link #markSetLoaded} for the entity; empty if none. Survives
     * {@link #clear}, and is dropped only when the entity leaves the context (see {@link #drop}).
     */
    public Set<String> setLoadedAttributes(Object entity) {
        return setLoadedAttrs.getOrDefault(entity, Collections.emptySet());
    }

    /**
     * Forgets all tracked state (baselines, dirty attributes and set-loaded markers) for an entity that is
     * leaving the context, firing the clean callback if it was dirty.
     */
    public void drop(Object entity) {
        baselines.remove(entity);
        setLoadedAttrs.remove(entity);
        Set<String> dirty = dirtyAttrs.remove(entity);
        if (dirty != null && !dirty.isEmpty()) {
            onEntityClean.accept(entity);
        }
    }

    /**
     * Drops all baselines and dirty attributes for every entity (e.g. on save or clear-changes). Set-loaded
     * markers are intentionally kept, unlike {@link #drop}.
     */
    public void clear() {
        baselines.clear();
        dirtyAttrs.clear();
    }

    protected Set<String> dirtyAttributes(Object entity) {
        Set<String> attrs = dirtyAttrs.get(entity);
        return attrs == null ? Collections.emptySet() : attrs;
    }

    protected void putDirty(Object entity, String attribute, @Nullable Object baseline) {
        baselines.computeIfAbsent(entity, e -> new HashMap<>()).put(attribute, baseline);
        markAttributeDirty(entity, attribute);
    }

    protected void markAttributeDirty(Object entity, String attribute) {
        Set<String> attrs = dirtyAttrs.computeIfAbsent(entity, e -> new HashSet<>());
        boolean wasEmpty = attrs.isEmpty();
        boolean newlyDirtied = attrs.add(attribute);
        if (newlyDirtied && DataContextDiagnostics.log.isDebugEnabled()) {
            Map<String, Object> entityBaselines = baselines.get(entity);
            Object baseline = entityBaselines == null ? null : entityBaselines.get(attribute);
            if (baseline == DIRTY_WITHOUT_BASELINE) {
                baseline = "<unknown>";
            }
            DataContextDiagnostics.log.debug(DataContextDiagnostics.attributeDirtied(entity, attribute, baseline));
        }
        if (wasEmpty) {
            onEntityDirty.accept(entity);
        }
    }

    protected void removeDirty(Object entity, String attribute) {
        Map<String, Object> entityBaselines = baselines.get(entity);
        if (entityBaselines != null) {
            entityBaselines.remove(attribute);
            if (entityBaselines.isEmpty()) {
                baselines.remove(entity);
            }
        }
        removeDirtyKeepBaseline(entity, attribute);
    }

    protected void removeDirtyKeepBaseline(Object entity, String attribute) {
        Set<String> attrs = dirtyAttrs.get(entity);
        if (attrs == null) {
            return;
        }
        boolean wasDirty = attrs.remove(attribute);
        if (wasDirty && DataContextDiagnostics.log.isDebugEnabled()) {
            DataContextDiagnostics.log.debug(DataContextDiagnostics.attributeReverted(entity, attribute));
        }
        if (attrs.isEmpty()) {
            dirtyAttrs.remove(entity);
            onEntityClean.accept(entity);
        }
    }

    @Nullable
    protected static Object refKey(@Nullable Object refOrId) {
        if (refOrId == null) return null;
        Object id = EntityValues.getId(refOrId);
        return id != null ? id : new IdentityKey(refOrId);
    }

    /**
     * The membership bag (element key {@code ->} count) of a collection, used as a to-many attribute's
     * baseline. Each element is keyed by its entity id, or by identity when it has none.
     */
    public static Map<Object, Integer> membershipBag(Collection<?> collection) {
        Map<Object, Integer> bag = new HashMap<>();
        for (Object e : collection) {
            bag.merge(refKey(e), 1, Integer::sum);
        }
        return bag;
    }

    private record IdentityKey(Object instance) {
        @Override
        public boolean equals(Object o) {
            return o instanceof IdentityKey k && k.instance == instance;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(instance);
        }
    }

    private static final Object DIRTY_WITHOUT_BASELINE = new Object();
}
