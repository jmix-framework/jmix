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
import org.jspecify.annotations.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.function.Consumer;

/**
 * The {@code Action} interface abstracts away a function from a visual component.
 * <p>
 * The action is executed by invoking its {@link #actionPerform(Component)} method.
 * <p>
 * The action itself has no visual representations, but visual components may use its
 * properties to initialize their appearance.
 */
public interface Action extends HasShortcutCombination {

    String PROP_TEXT = "text";
    String PROP_ENABLED = "enabled";
    String PROP_VISIBLE = "visible";
    String PROP_ICON = "icon";
    String PROP_DESCRIPTION = "description";
    String PROP_VARIANT = "variant";
    String PROP_SHORTCUT_COMBINATION = "shortcutCombination";

    /**
     * @return action's identifier
     */
    String getId();

    /**
     * Returns the text property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return action's text or {@code null} if not set
     */
    @Nullable
    String getText();

    /**
     * Sets the text property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param text text to set or {@code null} to remove
     */
    void setText(@Nullable String text);

    /**
     * Returns the enabled property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return whether the action is currently enabled
     */
    boolean isEnabled();

    /**
     * Sets the enabled property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param enabled whether the action is currently enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the visible property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return whether the action is currently visible
     */
    boolean isVisible();

    /**
     * Sets the visible property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param visible whether the action is currently visible
     */
    void setVisible(boolean visible);

    /**
     * Returns the icon property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return action's icon or {@code null} if not set
     * @deprecated use {@link #getIconComponent()} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    @Nullable
    Icon getIcon();

    /**
     * Sets the icon property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param icon icon to set or {@code null} to remove
     * @deprecated use {@link #setIconComponent(Component)} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    void setIcon(@Nullable Icon icon);

    /**
     * Returns the component that represents an icon associated with this action.
     * May be used by components to initialize their appearance.
     *
     * @return action's icon or {@code null} if not set
     */
    @Nullable
    Component getIconComponent();

    /**
     * Sets the component that represents an icon associated with this action.
     * May be used by components to initialize their appearance.
     *
     * @param icon the icon component to set or {@code null} to remove
     */
    void setIconComponent(@Nullable Component icon);

    /**
     * Returns the description property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return action's description or {@code null} if not set
     */
    @Nullable
    String getDescription();

    /**
     * Sets the description property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param description description to set or {@code null} to remove
     */
    void setDescription(@Nullable String description);

    /**
     * Returns the variant property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @return action's variant
     */
    ActionVariant getVariant();

    /**
     * Sets the variant property value of an action. May be used by components
     * to initialize their appearance.
     *
     * @param variant variant to set
     */
    void setVariant(ActionVariant variant);

    /**
     * Refreshes internal state of the action to initialize enabled, visible, text, icon, etc. properties depending
     * on programmatically set values and user permissions set at runtime.
     * <p>
     * For example, this method is called by visual components holding actions when they are bound to
     * data. At this moment the action can find out what entity it is connected to and change its state
     * according to the user permissions.
     */
    void refreshState();

    /**
     * Executes action logic.
     *
     * @param component {@link Component} that triggered this action
     */
    void actionPerform(Component component);

    /**
     * Adds a listener to be notified about changes in the properties of the action.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener);
}
