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

/**
 * SPI marker for Studio preview component processors; implement one of its role
 * sub-interfaces (e.g. {@link StudioPreviewChildProcessor}), not this type directly.
 * <p>
 * Register implementations in
 * {@code META-INF/services/io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentProcessor}.
 *
 * @see StudioPreviewComponentProvider
 */
public interface StudioPreviewComponentProcessor {
}
