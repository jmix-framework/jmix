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

package io.jmix.flowui.kit.component.combobutton;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import jakarta.annotation.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.Objects;

/**
 * Provides support for associating an {@link Action} with a {@link ComboButton}.
 * Enables the synchronization of the button's state, appearance, and behavior based on the properties
 * of the associated action and manages event handling for the action and button interaction.
 */
public class ComboButtonActionSupport {

    protected final ComboButton comboButton;

    protected Action action;

    protected Registration comboButtonClickRegistration;
    protected Registration actionPropertyChangeRegistration;

    public ComboButtonActionSupport(ComboButton comboButton) {
        this.comboButton = comboButton;
    }

    /**
     * Sets the {@link Action} to be associated with the combo button and optionally overrides certain
     * component properties based on the provided action.
     * <p>
     * If the action is already set to the same value, the method performs no operations.
     * Otherwise, it updates the combo button's state (e.g., text, icon, visibility) and registers
     * necessary listeners to synchronize the combo button with the action.
     *
     * @param action                      the action to associate with the combo button; can be null to remove the current association
     * @param overrideComponentProperties if true, the combo button's properties (e.g., text, icon,
     *                                    descriptive text) will be overridden by the action's corresponding properties
     */
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null) {
            updateEnabled();
            updateVisible();
            updateText(overrideComponentProperties);
            updateTitle(overrideComponentProperties);
            updateIcon(overrideComponentProperties);
            updateActionVariant(overrideComponentProperties);

            comboButtonClickRegistration =
                    comboButton.addClickListener(this::onButtonClick);
            actionPropertyChangeRegistration =
                    action.addPropertyChangeListener(this::onActionPropertyChange);
        }
    }

    /**
     * Returns the current {@link Action} instance associated with this object.
     *
     * @return the currently associated {@code Action}, or {@code null} if no action is set
     */
    @Nullable
    public Action getAction() {
        return action;
    }

    protected void addActionVariant(ComboButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.addThemeVariants(ComboButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.addThemeVariants(ComboButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.addThemeVariants(ComboButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeActionVariant(ComboButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.removeThemeVariants(ComboButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.removeThemeVariants(ComboButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.removeThemeVariants(ComboButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeRegistrations() {
        if (this.action != null) {
            if (comboButtonClickRegistration != null) {
                comboButtonClickRegistration.remove();
                comboButtonClickRegistration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected void updateText(boolean overrideComponentProperties) {
        if (Strings.isNullOrEmpty(comboButton.getText()) || overrideComponentProperties) {
            comboButton.setText(action.getText());
        }
    }

    protected void updateEnabled() {
        comboButton.setEnabled(action.isEnabled());
    }

    protected void updateVisible() {
        comboButton.setVisible(action.isVisible());
    }

    protected void updateActionVariant(boolean overrideComponentProperties) {
        if (overrideComponentProperties) {
            addActionVariant(comboButton, action.getVariant());
        }
    }

    protected void updateIcon(boolean overrideComponentProperties) {
        if (comboButton.getIcon() == null || overrideComponentProperties) {
            comboButton.setIcon(action.getIcon());
        }
    }

    protected void updateTitle(boolean overrideComponentProperties) {
        if (Strings.isNullOrEmpty(comboButton.getTitle()) || overrideComponentProperties) {
            comboButton.setTitle(action.getDescription());
        }
    }

    protected void onButtonClick(ClickEvent<MenuItem> event) {
        this.action.actionPerform(event.getSource());
    }

    protected void onActionPropertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Action.PROP_TEXT:
                comboButton.setText((String) event.getNewValue());
                break;
            case Action.PROP_ENABLED:
                comboButton.setEnabled((Boolean) event.getNewValue());
                break;
            case Action.PROP_VISIBLE:
                comboButton.setVisible((Boolean) event.getNewValue());
                break;
            case Action.PROP_ICON:
                comboButton.setIcon((Icon) event.getNewValue());
                break;
            case Action.PROP_DESCRIPTION:
                comboButton.setTitle((String) event.getNewValue());
                break;
            case Action.PROP_VARIANT:
                removeActionVariant(comboButton, (ActionVariant) event.getOldValue());
                addActionVariant(comboButton, (ActionVariant) event.getNewValue());
                break;
            default:
        }
    }
}
