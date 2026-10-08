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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Builds diagnostic log messages for attribute-level dirty tracking and merge decisions in
 * {@link DataContextImpl} and {@link DataContextChangeTracker}. All output goes through the
 * {@code io.jmix.flowui.datacontext.diagnostics} logger category at DEBUG level; call sites are
 * expected to guard with {@code log.isDebugEnabled()} before building a message.
 * <p>
 * Messages never call {@code toString()} on an entity (which could trigger lazy loading or
 * instance-name resolution over unfetched attributes); entities are rendered as
 * {@code ClassName-id} via {@link #formatEntity(Object)} instead.
 */
public class DataContextDiagnostics {

    protected static final Logger log = LoggerFactory.getLogger("io.jmix.flowui.datacontext.diagnostics");

    /**
     * Not meant to be instantiated directly; {@code protected} only so the class can be subclassed to add
     * or reuse message formats.
     */
    protected DataContextDiagnostics() {
    }

    /**
     * Message for an attribute that just became dirty, showing the baseline its future changes are
     * measured against.
     */
    public static String attributeDirtied(Object entity, String attribute, @Nullable Object baseline) {
        return "Attribute dirtied: " + formatEntity(entity) + "." + attribute
                + " (baseline '" + formatEntity(baseline) + "')";
    }

    /**
     * Message for an attribute whose value returned to its baseline and is therefore no longer dirty.
     */
    public static String attributeReverted(Object entity, String attribute) {
        return "Attribute reverted to baseline: " + formatEntity(entity) + "." + attribute;
    }

    /**
     * Message for a non-fresh merge that left a dirty attribute untouched, keeping the user's unsaved value.
     */
    public static String mergeSkippedDirty(Object entity, String attribute) {
        return "Merge skipped dirty attribute (kept user value): " + formatEntity(entity) + "." + attribute;
    }

    /**
     * Message for a dirty attribute whose baseline was moved to an incoming (merged) value by a fresh merge.
     */
    public static String baselineRebased(Object entity, String attribute, @Nullable Object newBaseline) {
        return "Baseline rebased for dirty attribute: " + formatEntity(entity) + "." + attribute
                + " -> '" + formatEntity(newBaseline) + "'";
    }

    /**
     * Message warning that {@code merge()} was called on a read-only {@code NoopDataContext}, so the change
     * is neither tracked nor saved.
     */
    public static String readOnlyContextMerge() {
        return "merge() called on a read-only DataContext (NoopDataContext); changes are not tracked or saved";
    }

    /**
     * Renders a value for a diagnostic message: {@code ClassName-id} for entities, {@code
     * "<collection>"} for a membership bag (used as a collection baseline), {@code "null"} for
     * null, and {@code String.valueOf} otherwise. Never calls {@code toString()} on an entity.
     */
    public static String formatEntity(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map) {
            // a collection baseline is stored as a membership bag (Map, see DataContextChangeTracker.membershipBag)
            return "<collection>";
        }
        if (EntityValues.isEntity(value)) {
            return value.getClass().getSimpleName() + "-" + EntityValues.getId(value);
        }
        return String.valueOf(value);
    }
}
