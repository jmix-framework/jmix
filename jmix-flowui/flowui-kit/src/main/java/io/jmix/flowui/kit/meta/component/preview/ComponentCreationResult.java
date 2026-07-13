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

package io.jmix.flowui.kit.meta.component.preview;

import com.vaadin.flow.component.Component;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Result of preview component creation: the component plus the XML aspects
 * the loader consumed itself (Studio skips its own handling of owned aspects).
 * Used in Studio via reflection. Do not rename accessors.
 */
public final class ComponentCreationResult {

    public static final String COLUMNS = "columns";
    public static final String ITEMS = "items";

    private final Component component;
    private final Set<String> ownedAspects;

    public ComponentCreationResult(@Nullable Component component, Set<String> ownedAspects) {
        this.component = component;
        this.ownedAspects = Set.copyOf(ownedAspects);
    }

    @Nullable
    public Component component() {
        return component;
    }

    public Set<String> ownedAspects() {
        return ownedAspects;
    }
}
