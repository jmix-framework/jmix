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

/**
 * Creates/removes a keyed column on a {@code Grid}-like container.
 */
public interface StudioPreviewColumnProcessor extends StudioPreviewComponentProcessor {

    /**
     * Creates (or reuses, if already materialized) the column identified by {@code key} on
     * {@code parent}.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if handled
     */
    boolean addColumn(Component parent, String key, int index);

    /**
     * Removes the column identified by {@code key} from {@code parent}.
     *
     * @return {@code true} if handled
     */
    boolean removeColumn(Component parent, String key);
}
