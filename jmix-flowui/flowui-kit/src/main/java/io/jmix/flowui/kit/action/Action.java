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

package io.jmix.flowui.kit.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasShortcutCombination;

import jakarta.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.function.Consumer;

public interface Action extends HasShortcutCombination {
    String PROP_TEXT = "text";
    String PROP_ENABLED = "enabled";
    String PROP_VISIBLE = "visible";
    String PROP_ICON = "icon";
    String PROP_DESCRIPTION = "description";
    String PROP_VARIANT = "variant";
    String PROP_SHORTCUT_COMBINATION = "shortcutCombination";

    String getId();

    @Nullable
    String getText();

    void setText(@Nullable String text);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isVisible();

    void setVisible(boolean visible);

    @Nullable
    Icon getIcon();

    void setIcon(@Nullable Icon icon);

    @Nullable
    String getDescription();

    void setDescription(@Nullable String description);

    ActionVariant getVariant();

    void setVariant(ActionVariant variant);

    void refreshState();

    void actionPerform(Component component);

    Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener);
}
