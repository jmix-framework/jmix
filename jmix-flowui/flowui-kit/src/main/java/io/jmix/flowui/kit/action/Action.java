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
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.function.Consumer;

public interface Action {
    String PROP_TEXT = "text";
    String PROP_ENABLED = "enabled";
    String PROP_VISIBLE = "visible";
    String PROP_ICON = "icon";
    String PROP_TITLE = "title";
    String PROP_VARIANT = "variant";
    String PROP_SHORTCUT = "shortcut";

    String getId();

    @Nullable
    String getText();

    void setText(@Nullable String text);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isVisible();

    void setVisible(boolean visible);

    @Nullable
    String getIcon();

    void setIcon(@Nullable String icon);

    @Nullable
    String getTitle();

    void setTitle(@Nullable String title);

    ActionVariant getVariant();

    void setVariant(ActionVariant variant);

    @Nullable
    KeyCombination getShortcutCombination();

    void setShortcutCombination(@Nullable KeyCombination shortcutCombination);

    void refreshState();

    void actionPerform(Component component);

    Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener);
}
