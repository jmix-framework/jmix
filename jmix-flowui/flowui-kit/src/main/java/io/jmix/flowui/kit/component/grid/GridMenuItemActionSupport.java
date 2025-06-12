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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import jakarta.annotation.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.Objects;

/**
 * A helper class that enhances the behavior of a menu item in the context of a grid or data table
 * by associating it with an {@link Action}. It synchronizes the properties and behavior of the menu item
 * with the associated action and handles user interactions through action listeners.
 */
public class GridMenuItemActionSupport {

    protected final GridMenuItemActionWrapper<?> menuItem;

    protected Action action;
    protected boolean overrideComponentProperties;

    protected Registration registration;
    protected Registration actionPropertyChangeRegistration;

    public GridMenuItemActionSupport(GridMenuItemActionWrapper<?> menuItem) {
        this.menuItem = menuItem;
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    /**
     * Sets the {@link Action} for this menu item and optionally configures it to override
     * the component properties such as text, tooltip, enabled state, and icon.
     *
     * @param action the action to set, or {@code null} to remove the current action
     */
    public void setAction(@Nullable Action action) {
        setAction(action, true);
    }

    /**
     * Sets the {@link Action} for this menu item and optionally configures it to override
     * the component properties such as text, tooltip, enabled state, and icon.
     *
     * @param action                      the action to set, or {@code null} to remove the current action
     * @param overrideComponentProperties whether the action should override the component's properties
     */
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action) && this.overrideComponentProperties == overrideComponentProperties) {
            return;
        }

        removeRegistrations();

        this.action = action;
        this.overrideComponentProperties = overrideComponentProperties;

        if (action != null) {
            GridMenuItem<?> contextMenuItem = menuItem.getMenuItem();
            if (overrideComponentProperties) {
                menuItem.setText(action.getText());
                contextMenuItem.setEnabled(action.isEnabled());
                contextMenuItem.setVisible(action.isVisible());
                menuItem.setTooltipText(action.getDescription());
                if (isShowActionIconEnabled()) {
                    menuItem.setPrefixComponent(action.getIcon());
                }
                if (isShowActionShortcutEnabled()) {
                    menuItem.setSuffixComponent(createShortcutComponent(action.getShortcutCombination()));
                }
                actionPropertyChangeRegistration = action.addPropertyChangeListener(this::propertyChangeEventListener);
            }
            registration = contextMenuItem.addMenuItemClickListener(event ->
                    action.actionPerform(event.getSource()));
        }

        updateVisible();
    }

    protected boolean isShowActionIconEnabled() {
        return false;
    }

    protected boolean isShowActionShortcutEnabled() {
        return false;
    }

    @Nullable
    protected Component createShortcutComponent(@Nullable KeyCombination keyCombination) {
        return keyCombination == null ? null : new Html("<kbd>" + keyCombination.format() + "</kbd>");
    }

    protected void updateVisible() {
        GridMenuItem<?> contextMenuItem = menuItem.getMenuItem();
        if (contextMenuItem != null) {
            boolean visibleByAction = !overrideComponentProperties || (action != null && action.isVisible());
            contextMenuItem.setVisible(!menuItem.isEmpty() && visibleByAction);
        }
    }

    protected void removeRegistrations() {
        if (action != null) {
            if (registration != null) {
                registration.remove();
                registration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected void propertyChangeEventListener(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        GridMenuItem<?> contextMenuItem = menuItem.getMenuItem();

        switch (propertyName) {
            case Action.PROP_TEXT -> {
                menuItem.setText(action.getText());
                updateVisible();
            }
            case Action.PROP_ENABLED -> {
                if (contextMenuItem != null) {
                    contextMenuItem.setEnabled(action.isEnabled());
                }
            }
            case Action.PROP_VISIBLE -> updateVisible();
            case Action.PROP_DESCRIPTION -> menuItem.setTooltipText(action.getDescription());
            case Action.PROP_ICON -> {
                if (isShowActionIconEnabled()) {
                    menuItem.setPrefixComponent(action.getIcon());
                }
                updateVisible();
            }
            case Action.PROP_SHORTCUT_COMBINATION -> {
                if (isShowActionShortcutEnabled()) {
                    menuItem.setSuffixComponent(createShortcutComponent(action.getShortcutCombination()));
                }
                updateVisible();
            }
            default -> {/* do nothing */}
        }
    }
}
