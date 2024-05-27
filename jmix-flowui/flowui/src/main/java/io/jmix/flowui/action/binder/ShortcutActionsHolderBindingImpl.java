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
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import org.springframework.lang.Nullable;
import java.util.List;

@Deprecated(since = "2.2", forRemoval = true)
public class ShortcutActionsHolderBindingImpl<H extends Component, A extends Action, C extends Component>
        extends AbstractShortcutActionBindingImpl<H, A, C> implements ShortcutActionsHolderBinding<H, A, C> {

    protected final H holder;

    public ShortcutActionsHolderBindingImpl(ActionBinder<H> binder, H holder, A action, C component,
                                            ShortcutActionHandler<C> actionHandler,
                                            @Nullable List<Registration> registrations) {
        super(binder, action, component, actionHandler, registrations);
        this.holder = holder;
    }

    @Override
    public H getHolder() {
        return holder;
    }
}
