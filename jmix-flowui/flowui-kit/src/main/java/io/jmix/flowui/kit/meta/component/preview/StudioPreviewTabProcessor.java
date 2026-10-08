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
 * Attaches/detaches a tab (paired with its content) to/from a {@code TabSheet}-like container.
 */
public interface StudioPreviewTabProcessor extends StudioPreviewComponentProcessor {

    /**
     * Attaches {@code tab} (paired with {@code content}) to {@code parent}.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if handled
     */
    boolean addTab(Component parent, Component tab, Component content, int index);

    /**
     * Detaches {@code tab} (and its paired content) from {@code parent}.
     *
     * @return {@code true} if handled
     */
    boolean removeTab(Component parent, Component tab);
}
