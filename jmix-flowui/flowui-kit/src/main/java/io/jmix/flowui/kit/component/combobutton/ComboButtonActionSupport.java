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
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.Objects;

public class ComboButtonActionSupport {

    protected final ComboButton comboButton;

    protected Action action;

    protected Registration comboButtonClickRegistration;
    protected Registration actionPropertyChangeRegistration;

    public ComboButtonActionSupport(ComboButton comboButton) {
        this.comboButton = comboButton;
    }

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
            updateShortcutCombination(overrideComponentProperties);
            updateActionVariant(overrideComponentProperties);

            comboButtonClickRegistration =
                    comboButton.addClickListener(this::onButtonClick);
            actionPropertyChangeRegistration =
                    action.addPropertyChangeListener(this::onActionPropertyChange);
        }
    }

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
        if (StringUtils.isEmpty(comboButton.getText()) || overrideComponentProperties) {
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
        if (StringUtils.isEmpty(comboButton.getTitle()) || overrideComponentProperties) {
            String description = action.getDescription();
            if (StringUtils.isNotEmpty(description)) {
                comboButton.setTitle(description);
            } else {
                String text = action.getText();
                String shortcutCombination = action.getShortcutCombination() != null
                        ? action.getShortcutCombination().format()
                        : null;

                if (!Strings.isNullOrEmpty(text)
                        && !Strings.isNullOrEmpty(shortcutCombination)) {
                    comboButton.setTitle(String.format("%s (%s)", text, shortcutCombination));
                } else if (!Strings.isNullOrEmpty(text)) {
                    comboButton.setTitle(text);
                } else if (!Strings.isNullOrEmpty(shortcutCombination)) {
                    comboButton.setTitle(shortcutCombination);
                }
            }
        }
    }

    protected void updateShortcutCombination(boolean overrideComponentProperties) {
        if (comboButton.getShortcutCombination() == null || overrideComponentProperties) {
            comboButton.setShortcutCombination(action.getShortcutCombination());
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
            case Action.PROP_SHORTCUT_COMBINATION:
                comboButton.setShortcutCombination((KeyCombination) event.getNewValue());
                break;
        }
    }
}
