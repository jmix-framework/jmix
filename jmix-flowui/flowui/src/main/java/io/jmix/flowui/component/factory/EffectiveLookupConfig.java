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
package io.jmix.flowui.component.factory;

import io.jmix.core.entity.annotation.LookupType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The effective, precedence-resolved {@code @LookupField} configuration for one entity reference.
 * Produced by {@link LookupFieldSupport}; consumed by {@code EntityFieldCreationSupport} (live
 * component) and {@code ComponentXmlFactory} (XML descriptor).
 *
 * @param componentType      resolved component intent, or {@code null} if no {@code @LookupField} applies
 * @param fieldLevel         {@code true} if the component config came from a field-level annotation
 * @param actions            resolved action ids (field annotation, then property, then class annotation)
 * @param itemsMode          items behavior for a {@code DROPDOWN}; ignored for {@code VIEW}
 * @param query              explicit JPQL for {@link ItemsMode#QUERY}
 * @param searchStringFormat search-string format for {@link ItemsMode#QUERY}
 * @param escapeValueForLike escape flag for {@link ItemsMode#QUERY}
 * @param fetchPlanName       optional fetch plan name for lazy items
 */
public record EffectiveLookupConfig(@Nullable LookupType componentType,
                                    boolean fieldLevel,
                                    List<String> actions,
                                    ItemsMode itemsMode,
                                    @Nullable String query,
                                    @Nullable String searchStringFormat,
                                    boolean escapeValueForLike,
                                    @Nullable String fetchPlanName) {

    /**
     * How the items of a {@code DROPDOWN} are loaded.
     */
    public enum ItemsMode {
        /** Load all items (no lazy query configured). */
        EAGER,
        /** Lazy load via an explicit JPQL query with {@code :searchString}. */
        QUERY,
        /** Lazy load via instance-name query conditions. */
        BY_INSTANCE_NAME
    }
}
