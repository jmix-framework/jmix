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

package io.jmix.flowui.action.binder.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionBinderUtils;
import io.jmix.flowui.action.binder.ActionsHolderBinding;
import io.jmix.flowui.action.binder.ActionsHolderBindingImpl;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerButton;
import org.springframework.core.annotation.Order;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component("flowui_ValuePickerButtonActionBinder")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class ValuePickerButtonActionBinder implements ComponentActionsHolderBinder<ValuePickerButton> {

    @Override
    public boolean supports(Component component) {
        return ValuePickerButton.class.isAssignableFrom(component.getClass());
    }

    @Override
    public <H extends Component, A extends Action> ActionsHolderBinding<H, A, ValuePickerButton> bind(ActionBinder<H> binder,
                                                                                                      A action,
                                                                                                      ValuePickerButton component,
                                                                                                      BiFunction<ValuePickerButton, ComponentEventListener, Registration> actionHandler) {
        List<Registration> registrations = new ArrayList<>();
        ValuePickerButton button = initComponent(action, component, registrations);
        action = initAction(action, button, registrations);

        return new ActionsHolderBindingImpl<>(binder, binder.getHolder(), action, button, actionHandler, registrations);
    }

    protected <A extends Action> ValuePickerButton initComponent(A action, ValuePickerButton component, List<Registration> registrations) {
        component.setEnabled(action.isEnabled());
        component.setVisible(action.isVisible());

        if (action.getIcon() != null) {
            component.setIcon(action.getIcon());
        }

        KeyCombination shortcutCombination = action.getShortcutCombination();
        if (shortcutCombination != null) {
            registrations.add(
                    component.addClickShortcut(shortcutCombination.getKey(), shortcutCombination.getKeyModifiers()));
        }

        component.setTitle(generateTitle(action));

        return component;
    }

    protected <A extends Action> A initAction(A action, ValuePickerButton component, List<Registration> registrations) {
        registrations.add(action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            switch (propertyName) {
                case Action.PROP_TEXT:
                    component.setTitle(generateTitle(action));
                    break;
                case Action.PROP_ENABLED:
                    component.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    component.setVisible(action.isVisible());
                    break;
                case Action.PROP_ICON:
                    component.setIcon(action.getIcon());
                    break;
                case Action.PROP_SHORTCUT_COMBINATION:
                    ActionBinderUtils.refreshShortcutCombinationProperty(component, (KeyCombination) event.getNewValue(), (KeyCombination) event.getOldValue(), registrations);
                    component.setTitle(generateTitle(action));
                    break;
            }
        }));

        return action;
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
