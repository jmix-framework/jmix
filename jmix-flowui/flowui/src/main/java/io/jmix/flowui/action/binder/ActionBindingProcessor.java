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
import io.jmix.flowui.action.binder.component.ComponentActionBinder;
import io.jmix.flowui.action.binder.component.ComponentActionUnbinder;
import io.jmix.flowui.action.binder.component.ComponentShortcutActionBinder;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@org.springframework.stereotype.Component("flowui_ActionBindingProcessor")
public class ActionBindingProcessor {

    protected List<ComponentActionBinder> actionBinders = new ArrayList<>();
    protected List<ComponentShortcutActionBinder> shortcutActionBinders = new ArrayList<>();
    protected List<ComponentActionUnbinder> actionUnbinders = new ArrayList<>();

    @Autowired(required = false)
    public void setActionBinders(List<ComponentActionBinder> actionBinders) {
        this.actionBinders = actionBinders;
    }

    @Autowired(required = false)
    public void setShortcutActionBinders(List<ComponentShortcutActionBinder> shortcutActionBinders) {
        this.shortcutActionBinders = shortcutActionBinders;
    }

    @Autowired(required = false)
    public void setActionUnbinders(List<ComponentActionUnbinder> actionUnbinders) {
        this.actionUnbinders = actionUnbinders;
    }

    public <C extends Component, A extends Action> ActionBinding<C, A> bind(A action,
                                                                            ActionBinder<C> binder,
                                                                            BiFunction<C, ComponentEventListener, Registration> handler,
                                                                            boolean overrideComponentProperties) {
        ActionBinding<C, A> binding;
        ComponentActionBinder actionBinder = getComponentActionBinder(binder.getHolder());
        if (actionBinder != null) {
            binding = actionBinder.bind(binder, action, handler, overrideComponentProperties);
        } else {
            binding = new ActionBindingImpl<>(binder, action, binder.getHolder(), handler, null);
        }
        return binding;
    }

    public <C extends Component, A extends Action> ShortcutActionBinding<C, A> bindShortcut(A action,
                                                                                            ActionBinder<C> binder,
                                                                                            ShortcutActionHandler<C> handler,
                                                                                            boolean overrideComponentProperties) {
        ShortcutActionBinding<C, A> binding;
        ComponentShortcutActionBinder shortcutActionBinder = getComponentShortcutActionBinder(binder.getHolder());
        if (shortcutActionBinder != null) {
            binding = shortcutActionBinder.bindShortcut(binder, action, handler, overrideComponentProperties);
        } else {
            binding = new ShortcutActionBindingImpl<>(binder, action, binder.getHolder(), handler, null);
        }
        return binding;
    }

    public void unbind(ActionBinding binding) {
        ComponentActionUnbinder actionUnbinder = getComponentActionUnbinder(binding.getComponent());
        if (actionUnbinder != null) {
            actionUnbinder.unbind(binding.getComponent(), binding.getAction());
        }
    }

    @Nullable
    protected ComponentActionBinder getComponentActionBinder(Component component) {
        for (ComponentActionBinder actionBinder : actionBinders) {
            if (actionBinder.supports(component)) {
                return actionBinder;
            }
        }

        return null;
    }

    @Nullable
    protected ComponentShortcutActionBinder getComponentShortcutActionBinder(Component component) {
        for (ComponentShortcutActionBinder shortcutActionBinder : shortcutActionBinders) {
            if (shortcutActionBinder.supports(component)) {
                return shortcutActionBinder;
            }
        }

        return null;
    }

    @Nullable
    protected ComponentActionUnbinder getComponentActionUnbinder(Component component) {
        for (ComponentActionUnbinder actionUnbinder : actionUnbinders) {
            if (actionUnbinder.supports(component)) {
                return actionUnbinder;
            }
        }

        return null;
    }
}
