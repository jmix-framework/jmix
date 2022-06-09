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

import com.google.common.base.Strings;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
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
            menuItem.setText(generateTitle(action));
            menuItem.setEnabled(action.isEnabled());
            menuItem.setVisible(action.isVisible());

            registration = menuItem.addMenuItemClickListener(event ->
                    action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
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
            switch (propertyName) {
                case Action.PROP_TEXT:
                case Action.PROP_SHORTCUT:
                    menuItem.setText(generateTitle(action));
                    break;
                case Action.PROP_ENABLED:
                    menuItem.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    menuItem.setVisible(action.isVisible());
                    break;
                default:
            }
        });
    }

    @Nullable
    protected String generateTitle(Action action) {
        String text = action.getText();
        String shortcutCombination = action.getShortcutCombination() != null
                ? action.getShortcutCombination().format()
                : null;

        if (!Strings.isNullOrEmpty(text)) {
            return Strings.isNullOrEmpty(shortcutCombination)
                    ? text
                    : String.format("%s (%s)", text, shortcutCombination);
        } else if (!Strings.isNullOrEmpty(shortcutCombination)) {
            return shortcutCombination;
        } else {
            return null;
        }
    }
}
