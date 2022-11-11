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

package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.action.binder.component.ComponentActionsHolderBinder;
import io.jmix.flowui.action.binder.component.ComponentActionsHolderUnbinder;
import io.jmix.flowui.action.binder.component.ComponentShortcutActionsHolderBinder;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@org.springframework.stereotype.Component("flowui_ActionsHolderBindingProcessor")
public class ActionsHolderBindingProcessor {

    protected List<ComponentActionsHolderBinder> actionsHolderBinders = new ArrayList<>();
    protected List<ComponentShortcutActionsHolderBinder> shortcutActionsHolderBinders = new ArrayList<>();
    protected List<ComponentActionsHolderUnbinder> actionsHolderUnbinders = new ArrayList<>();

    @Autowired(required = false)
    public void setActionsHolderBinders(List<ComponentActionsHolderBinder> actionsHolderBinders) {
        this.actionsHolderBinders = actionsHolderBinders;
    }

    @Autowired(required = false)
    public void setShortcutActionsHolderBinders(List<ComponentShortcutActionsHolderBinder> shortcutActionsHolderBinders) {
        this.shortcutActionsHolderBinders = shortcutActionsHolderBinders;
    }

    @Autowired(required = false)
    public void setActionsHolderUnbinders(List<ComponentActionsHolderUnbinder> actionsHolderUnbinders) {
        this.actionsHolderUnbinders = actionsHolderUnbinders;
    }

    public <H extends Component, A extends Action, C extends Component> ActionsHolderBinding<H, A, C> bind(ActionBinder<H> binder,
                                                                                                           A action,
                                                                                                           C component,
                                                                                                           BiFunction<C, ComponentEventListener, Registration> handler) {
        ActionsHolderBinding<H, A, C> binding;
        ComponentActionsHolderBinder actionsHolderBinder = getActionsHolderBinder(component);
        if (actionsHolderBinder != null) {
            binding = actionsHolderBinder.bind(binder, action, component, handler);
        } else {
            binding = new ActionsHolderBindingImpl<>(binder, binder.getHolder(), action, component, handler, null);
        }
        return binding;
    }

    public <H extends Component, A extends Action, C extends Component> ShortcutActionsHolderBinding<H, A, C> bindShortcut(ActionBinder<H> binder,
                                                                                                                           A action,
                                                                                                                           C component,
                                                                                                                           ShortcutActionHandler<C> handler) {
        ShortcutActionsHolderBinding<H, A, C> binding;
        ComponentShortcutActionsHolderBinder shortcutActionsHolderBinder = getShortcutActionsHolderBinder(component);
        if (shortcutActionsHolderBinder != null) {
            binding = shortcutActionsHolderBinder.bindShortcut(binder, action, component, handler);
        } else {
            binding = new ShortcutActionsHolderBindingImpl<>(binder, binder.getHolder(), action, component, handler, null);
        }
        return binding;
    }

    public void unbind(ActionsHolderBinding binding) {
        ComponentActionsHolderUnbinder actionsHolderUnbinder = getActionsHolderUnbinder(binding.getComponent());
        if (actionsHolderUnbinder != null) {
            actionsHolderUnbinder.unbind(binding.getHolder(), binding.getAction(), binding.getComponent());
        }
    }

    @Nullable
    protected ComponentActionsHolderBinder getActionsHolderBinder(Component component) {
        for (ComponentActionsHolderBinder actionsHolderBinder : actionsHolderBinders) {
            if (actionsHolderBinder.supports(component)) {
                return actionsHolderBinder;
            }
        }

        return null;
    }

    @Nullable
    protected ComponentShortcutActionsHolderBinder getShortcutActionsHolderBinder(Component component) {
        for (ComponentShortcutActionsHolderBinder shortcutActionsHolderBinder : shortcutActionsHolderBinders) {
            if (shortcutActionsHolderBinder.supports(component)) {
                return shortcutActionsHolderBinder;
            }
        }

        return null;
    }

    @Nullable
    protected ComponentActionsHolderUnbinder getActionsHolderUnbinder(Component component) {
        for (ComponentActionsHolderUnbinder actionsHolderUnbinder : actionsHolderUnbinders) {
            if (actionsHolderUnbinder.supports(component)) {
                return actionsHolderUnbinder;
            }
        }

        return null;
    }
}
