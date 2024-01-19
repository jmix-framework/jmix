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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import jakarta.annotation.Nullable;

import java.util.Objects;

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

    public void setAction(@Nullable Action action) {
        setAction(action, true);
    }

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
                    menuItem.setPrefixComponent(createIconComponent(action.getIcon()));
                }
                if (isShowActionShortcutEnabled()) {
                    menuItem.setSuffixComponent(createShortcutComponent(action.getShortcutCombination()));
                }
                actionPropertyChangeRegistration = addPropertyChangeListener();
            }
            registration = contextMenuItem.addMenuItemClickListener(event ->
                    action.actionPerform(event.getSource()));
        }

        updateVisible();
    }

    protected boolean isShowActionIconEnabled() {
        return false;
    }

    @Nullable
    protected Component createIconComponent(@Nullable Icon actionIcon) {
        if (actionIcon == null) {
            return null;
        }
        String iconAttribute = actionIcon.getElement().getAttribute("icon");
        if (iconAttribute == null) {
            return null;
        }
        return ComponentUtils.parseIcon(iconAttribute);
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

    protected Registration addPropertyChangeListener() {
        return action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            GridMenuItem<?> contextMenuItem = menuItem.getMenuItem();
            switch (propertyName) {
                case Action.PROP_TEXT:
                    menuItem.setText(action.getText());
                    updateVisible();
                    break;
                case Action.PROP_ENABLED:
                    if (contextMenuItem != null) {
                        contextMenuItem.setEnabled(action.isEnabled());
                    }
                    break;
                case Action.PROP_VISIBLE:
                    updateVisible();
                    break;
                case Action.PROP_DESCRIPTION:
                    menuItem.setTooltipText(action.getDescription());
                case Action.PROP_ICON:
                    menuItem.setPrefixComponent(action.getIcon());
                    updateVisible();
                case Action.PROP_SHORTCUT_COMBINATION:
                    menuItem.setSuffixComponent(createShortcutComponent(action.getShortcutCombination()));
                    updateVisible();
                default:
            }
        });
    }
}
