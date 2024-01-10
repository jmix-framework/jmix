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
import com.vaadin.flow.component.html.Span;
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
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null) {
            menuItem.setText(action.getText());
            menuItem.setEnabled(action.isEnabled());
            menuItem.setVisible(action.isVisible());
            menuItem.setTooltipText(action.getDescription());
            menuItem.setPrefixComponent(createIconComponent(action.getIcon()));
            //todo app property
            if (true) {
                menuItem.setSuffixComponent(createShortcutComponent(action.getShortcutCombination()));
            }

            registration = menuItem.addMenuItemClickListener(event ->
                    action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
        }

        updateVisible();
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

    @Nullable
    protected Component createShortcutComponent(@Nullable KeyCombination keyCombination) {
        return keyCombination == null ? null : new Span(keyCombination.format());
    }

    protected void updateVisible() {
        menuItem.setVisible(
                !menuItem.isEmpty()
                        && action != null
                        && action.isVisible()
        );
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
            switch (propertyName) {
                case Action.PROP_TEXT:
                    menuItem.setText(action.getText());
                    updateVisible();
                    break;
                case Action.PROP_ENABLED:
                    menuItem.setEnabled(action.isEnabled());
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
