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

package io.jmix.flowui.kit.component.valuepicker;

import com.google.common.base.Strings;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * Provides support for associating and managing an {@link Action} with a {@link ValuePickerButton}.
 * This class handles updating the button's properties (such as title, icon, and visibility) based
 * on the associated action, and facilitates event registration and cleanup.
 */
public class ValuePickerButtonActionSupport {

    protected final ValuePickerButton button;

    protected Action action;

    protected Registration registration;
    protected Registration actionPropertyChangeRegistration;

    public ValuePickerButtonActionSupport(ValuePickerButton button) {
        this.button = button;
    }

    /**
     * Sets the {@link Action} for the associated component and updates the component's
     * properties based on the action's state if specified.
     *
     * @param action                      the action to set; may be null
     * @param overrideComponentProperties a flag indicating whether to update the component's
     *                                    properties based on the action's state
     */
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null && overrideComponentProperties) {
            button.setId(action.getId());
            button.setTitle(generateTitle(action));
            button.setEnabled(action.isEnabled());
            button.setVisible(action.isVisible());
            button.setIcon(action.getIcon());

            registration = button.addClickListener(event -> action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
        }
    }

    /**
     * Returns the current {@link Action} associated with the component.
     *
     * @return the current action, or {@code null} if no action is set
     */
    @Nullable
    public Action getAction() {
        return action;
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
                case Action.PROP_TEXT, Action.PROP_SHORTCUT_COMBINATION:
                    button.setTitle(generateTitle(action));
                    break;
                case Action.PROP_ENABLED:
                    button.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    button.setVisible(action.isVisible());
                    break;
                case Action.PROP_ICON:
                    button.setIcon(action.getIcon());
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
