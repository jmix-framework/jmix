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
class DataContextChangeTracker {

    // entity -> attribute -> baseline (scalar value, reference id, or membership bag for collections)
    private final Map<Object, Map<String, Object>> baselines = new IdentityHashMap<>();

    // entity -> set of currently-dirty attributes. An attribute may have a baseline stored
    // (e.g. a collection snapshot taken before mutation) without being dirty yet.
    private final Map<Object, Set<String>> dirtyAttrs = new IdentityHashMap<>();

    // entity -> attributes made loaded by a user set or a non-null merge-install (increment 04's markLoaded).
    // A fresh merge of a narrower copy replaces the loaded-state cache with the source's, whose negative for
    // such an attribute shadows the (correct) fetch-group state; this registry lets reapplySetLoaded restore it
    // afterwards (DataContextImpl.reapplySetLoaded). Dropped when an entity leaves the context (drop), NOT by
    // clear(): clear() resets dirty state (clearChanges, and inside save()) while the value stays present.
    private final Map<Object, Set<String>> setLoadedAttrs = new IdentityHashMap<>();

    private final Consumer<Object> onEntityDirty;
    private final Consumer<Object> onEntityClean;

    DataContextChangeTracker(Consumer<Object> onEntityDirty, Consumer<Object> onEntityClean) {
        this.onEntityDirty = onEntityDirty;
        this.onEntityClean = onEntityClean;
    }

    void trackChange(Object entity, String attribute, @Nullable Object prevValue,
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

    void snapshotCollectionBaseline(Object entity, String attribute, Collection<?> baselineContents) {
        if (!isAttributeDirty(entity, attribute)) {
            // A clean attribute's baseline must always track what merge just installed: merge
            // legitimately replaces a clean collection's contents (e.g. a fresh reload bringing in
            // DB-side changes), so if the baseline were left stale, later mutations would be
            // compared against membership that no longer reflects reality (spurious dirty, or a
            // false clean when a removed-then-re-added item happens to restore the stale bag).
            // A dirty attribute keeps its existing baseline: mergeFromChild relies on the child's
            // own baseline surviving the merge (see isOverriding), and putDirty overwrites it
            // anyway whenever the tracked value actually changes.
            baselines.computeIfAbsent(entity, e -> new HashMap<>())
                    .put(attribute, membershipBag(baselineContents));
        }
    }

    void trackCollectionChange(Object entity, String attribute, Collection<?> current) {
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

    boolean isAttributeDirty(Object entity, String attribute) {
        return dirtyAttributes(entity).contains(attribute);
    }

    Set<String> getModifiedAttributes(Object entity) {
        Set<String> attrs = dirtyAttrs.get(entity);
        return attrs == null || attrs.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(attrs));
    }

    void rebaseline(Object entity, String attribute, @Nullable Object incomingValue,
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

    void rebaselineCollection(Object entity, String attribute, Collection<?> incoming, Collection<?> current) {
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

    Map<String, Object> dirtyBaselines(Object entity) {
        Map<String, Object> entityBaselines = baselines.get(entity);
        Set<String> dirty = dirtyAttrs.get(entity);
        if (entityBaselines == null || dirty == null || dirty.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (String attribute : dirty) {
            result.put(attribute, entityBaselines.get(attribute));
        }
        return result;
    }

    void markDirty(Object entity, String attribute, @Nullable Object baseline) {
        putDirty(entity, attribute, baseline);
    }

    void markSetLoaded(Object entity, String attribute) {
        setLoadedAttrs.computeIfAbsent(entity, e -> new HashSet<>()).add(attribute);
    }

    Set<String> setLoadedAttributes(Object entity) {
        return setLoadedAttrs.getOrDefault(entity, Collections.emptySet());
    }

    void drop(Object entity) {
        baselines.remove(entity);
        setLoadedAttrs.remove(entity);
        Set<String> dirty = dirtyAttrs.remove(entity);
        if (dirty != null && !dirty.isEmpty()) {
            onEntityClean.accept(entity);
        }
    }

    void clear() {
        baselines.clear();
        dirtyAttrs.clear();
    }

    private Set<String> dirtyAttributes(Object entity) {
        Set<String> attrs = dirtyAttrs.get(entity);
        return attrs == null ? Collections.emptySet() : attrs;
    }

    private void putDirty(Object entity, String attribute, @Nullable Object baseline) {
        baselines.computeIfAbsent(entity, e -> new HashMap<>()).put(attribute, baseline);
        markAttributeDirty(entity, attribute);
    }

    private void markAttributeDirty(Object entity, String attribute) {
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

    private void removeDirty(Object entity, String attribute) {
        Map<String, Object> entityBaselines = baselines.get(entity);
        if (entityBaselines != null) {
            entityBaselines.remove(attribute);
            if (entityBaselines.isEmpty()) {
                baselines.remove(entity);
            }
        }
        removeDirtyKeepBaseline(entity, attribute);
    }

    private void removeDirtyKeepBaseline(Object entity, String attribute) {
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
    private static Object refKey(@Nullable Object refOrId) {
        if (refOrId == null) return null;
        Object id = EntityValues.getId(refOrId);
        return id != null ? id : new IdentityKey(refOrId);
    }

    static Map<Object, Integer> membershipBag(Collection<?> collection) {
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
