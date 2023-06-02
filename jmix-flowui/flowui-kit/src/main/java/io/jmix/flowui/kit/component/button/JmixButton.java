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

package io.jmix.flowui.kit.component.button;

import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.kit.component.HasShortcutCombination;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.KeyCombination;

import jakarta.annotation.Nullable;
import java.util.Objects;

public class JmixButton extends Button implements HasTitle, HasAction, HasShortcutCombination {

    protected JmixButtonActionSupport actionSupport;
    protected ShortcutRegistration shortcutRegistration;

    @Override
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        getActionSupport().setAction(action, overrideComponentProperties);
    }

    @Nullable
    @Override
    public Action getAction() {
        return getActionSupport().getAction();
    }

    protected JmixButtonActionSupport getActionSupport() {
        if (actionSupport == null) {
            actionSupport = new JmixButtonActionSupport(this);
        }

        return actionSupport;
    }

    @Nullable
    @Override
    public KeyCombination getShortcutCombination() {
        if (shortcutRegistration == null) {
            return null;
        }

        KeyModifier[] keyModifiers = shortcutRegistration.getModifiers().stream()
                .filter(key -> key instanceof KeyModifier)
                .map(key -> (KeyModifier) key)
                .toArray(KeyModifier[]::new);

        return KeyCombination.create(shortcutRegistration.getKey(), keyModifiers);
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        KeyCombination oldValue = getShortcutCombination();
        if (!Objects.equals(oldValue, shortcutCombination)) {
            if (shortcutRegistration != null) {
                shortcutRegistration.remove();
                shortcutRegistration = null;
            }

            if (shortcutCombination != null) {
                shortcutRegistration = addClickShortcut(shortcutCombination.getKey(),
                        shortcutCombination.getKeyModifiers());
            }
        }
    }
}
