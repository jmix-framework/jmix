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

import org.jspecify.annotations.Nullable;

/**
 * Studio-side services available to {@link StudioPreviewComponentLoader preview loaders}.
 * Implemented by Studio via {@link java.lang.reflect.Proxy} over the preview classloader,
 * therefore: JDK-only signatures, every method may return {@code null}, implementations never throw.
 */
public interface StudioPreviewEnvironment {

    StudioPreviewEnvironment NOOP = new StudioPreviewEnvironment() {
        @Nullable
        @Override
        public String resolveMessage(String messageKey) {
            return null;
        }

        @Nullable
        @Override
        public String propertyCaption(@Nullable String dataContainerId, @Nullable String metaClass,
                                      String propertyPath) {
            return null;
        }
    };

    /**
     * Resolves a localized message reference (e.g. {@code msg://key}) using the IDE project messages.
     */
    @Nullable
    String resolveMessage(String messageKey);

    /**
     * Resolves the localized caption of an entity property, e.g. for a dataGrid column header.
     */
    @Nullable
    String propertyCaption(@Nullable String dataContainerId, @Nullable String metaClass, String propertyPath);
}
