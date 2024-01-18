/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import jakarta.annotation.Nullable;

/**
 * Interface for actions and UI components that can be performed using shortcuts.<br/>
 * Provides the same functionality as the {@link ClickNotifier#addClickShortcut(Key, KeyModifier...)}.
 */
public interface HasShortcutCombination {

    @Nullable
    KeyCombination getShortcutCombination();

    void setShortcutCombination(@Nullable KeyCombination shortcutCombination);
}
