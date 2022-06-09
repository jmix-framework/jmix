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

package io.jmix.flowui.kit.component.button;

import com.google.common.base.Strings;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public class JmixButtonActionSupport {

    protected final JmixButton button;

    protected Action action;

    protected Registration registration;
    protected Registration actionPropertyChangeRegistration;

    public JmixButtonActionSupport(JmixButton button) {
        this.button = button;
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
            updateShortcut(overrideComponentProperties);
            updateActionVariant(overrideComponentProperties);

            registration = button.addClickListener(event ->
                    action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
        }
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    protected void addActionVariant(JmixButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.addThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeActionVariant(JmixButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.removeThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeRegistrations() {
        if (this.action != null) {
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

    protected void updateText(boolean overrideComponentProperties) {
        String text = action.getText();
        if (StringUtils.isEmpty(button.getText()) || overrideComponentProperties) {
            button.setText(text);
        }
    }

    protected void updateEnabled() {
        button.setEnabled(action.isEnabled());
    }

    protected void updateVisible() {
        button.setVisible(action.isVisible());
    }

    protected void updateActionVariant(boolean overrideComponentProperties) {
        if (overrideComponentProperties) {
            addActionVariant(button, action.getVariant());
        }
    }

    protected void updateIcon(boolean overrideComponentProperties) {
        if (button.getIcon() == null || overrideComponentProperties) {
            String icon = action.getIcon();
            if (StringUtils.isEmpty(icon)) {
                button.setIcon(null);
            } else {
                button.setIcon(new Icon(icon));
            }
        }
    }

    protected void updateTitle(boolean overrideComponentProperties) {
        String description = action.getDescription();
        if (StringUtils.isEmpty(button.getTitle()) || overrideComponentProperties) {
            if (StringUtils.isNotEmpty(description)) {
                button.setTitle(description);
            } else {
                String text = action.getText();
                String shortcutCombination = action.getShortcutCombination() != null
                        ? action.getShortcutCombination().format()
                        : null;

                if (!Strings.isNullOrEmpty(text)
                        && !Strings.isNullOrEmpty(shortcutCombination)) {
                    button.setTitle(String.format("%s (%s)", text, shortcutCombination));
                } else if (!Strings.isNullOrEmpty(text)) {
                    button.setTitle(text);
                } else if (!Strings.isNullOrEmpty(shortcutCombination)) {
                    button.setTitle(shortcutCombination);
                }
            }
        }
    }

    protected void updateShortcut(boolean overrideComponentProperties) {
        KeyCombination shortcutCombination = action.getShortcutCombination();
        if (button.getShortcutCombination() == null || overrideComponentProperties) {
            button.setShortcutCombination(shortcutCombination);
        }
    }

    protected Registration addPropertyChangeListener() {
        return action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            if (Action.PROP_TEXT.equals(propertyName)) {
                button.setText((String) event.getNewValue());
            } else if (Action.PROP_ENABLED.equals(propertyName)) {
                button.setEnabled((Boolean) event.getNewValue());
            } else if (Action.PROP_VISIBLE.equals(propertyName)) {
                button.setVisible((Boolean) event.getNewValue());
            } else if (Action.PROP_ICON.equals(propertyName)) {
                String icon = (String) event.getNewValue();
                if (StringUtils.isEmpty(icon)) {
                    button.setIcon(null);
                } else {
                    button.setIcon(new Icon(icon));
                }
            } else if (Action.PROP_DESCRIPTION.equals(propertyName)) {
                button.setTitle((String) event.getNewValue());
            } else if (Action.PROP_VARIANT.equals(propertyName)) {
                removeActionVariant(button, (ActionVariant) event.getOldValue());
                addActionVariant(button, (ActionVariant) event.getNewValue());
            } else if (Action.PROP_SHORTCUT.equals(propertyName)) {
                button.setShortcutCombination((KeyCombination) event.getNewValue());
            }
        });
    }
}
