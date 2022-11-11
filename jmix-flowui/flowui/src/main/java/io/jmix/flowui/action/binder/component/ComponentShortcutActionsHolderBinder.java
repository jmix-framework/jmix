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

import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ShortcutActionHandler;
import io.jmix.flowui.action.binder.ShortcutActionsHolderBinding;
import io.jmix.flowui.kit.action.Action;

public interface ComponentShortcutActionsHolderBinder<C extends Component> {

    boolean supports(Component component);

    <H extends Component, A extends Action> ShortcutActionsHolderBinding<H, A, C> bindShortcut(ActionBinder<H> binder,
                                                                                               A action,
                                                                                               C component,
                                                                                               ShortcutActionHandler<C> actionHandler);
}
