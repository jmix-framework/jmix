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
 * Attaches/detaches a generic child component to/from a parent in the live Vaadin preview tree.
 */
public interface StudioPreviewChildProcessor extends StudioPreviewComponentProcessor {

    /**
     * Whether this processor knows how to attach/detach children of {@code parent}.
     */
    boolean isSupported(Component parent);

    /**
     * Attaches {@code child} to {@code parent}.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if handled
     */
    boolean addChild(Component parent, Component child, int index);

    /**
     * Detaches {@code child} from {@code parent}.
     *
     * @return {@code true} if handled
     */
    boolean removeChild(Component parent, Component child);
}
