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
import io.jmix.flowui.kit.action.Action;

/**
 * Attaches/detaches an {@link Action} to/from a keyed action container (e.g. {@code HasActions}).
 */
public interface StudioPreviewActionProcessor extends StudioPreviewComponentProcessor {

    /**
     * Attaches {@code action} to {@code parent}.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if handled
     */
    boolean addAction(Component parent, Action action, int index);

    /**
     * Detaches {@code action} from {@code parent}.
     *
     * @return {@code true} if handled
     */
    boolean removeAction(Component parent, Action action);
}
