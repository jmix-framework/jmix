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
import io.jmix.flowui.kit.action.Action;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class ActionsHolderBindingImpl<H extends Component, A extends Action, C extends Component>
        extends AbstractActionBindingImpl<H, A, C> implements ActionsHolderBinding<H, A, C> {

    protected final H holder;

    public ActionsHolderBindingImpl(ActionBinder<H> binder, H holder, A action, C component,
                                    BiFunction<C, ComponentEventListener, Registration> actionHandler,
                                    @Nullable List<Registration> registrations) {
        super(binder, action, component, registrations);
        this.holder = holder;
        this.registrations.add(actionHandler.apply(component, __ -> action.actionPerform(component)));
    }

    @Override
    public H getHolder() {
        return holder;
    }
}
