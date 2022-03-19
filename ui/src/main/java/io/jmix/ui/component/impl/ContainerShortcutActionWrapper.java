/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.*;

public class ContainerShortcutActionWrapper extends com.vaadin.event.ShortcutListener {

    protected ShortcutAction action;
    protected ComponentContainer container;
    protected KeyCombination keyCombination;

    public ContainerShortcutActionWrapper(ShortcutAction action,
                                          ComponentContainer container, KeyCombination keyCombination) {
        super(null, keyCombination.getKey().getCode(), KeyCombination.Modifier.codes(keyCombination.getModifiers()));
        this.action = action;
        this.keyCombination = keyCombination;
        this.container = container;
    }

    @Override
    public void handleAction(Object sender, Object target) {
        ShortcutTriggeredEvent event = ComponentsHelper.getShortcutEvent(container,
                (com.vaadin.ui.Component) target);
        action.getHandler().accept(event);
    }

    public ShortcutAction getAction() {
        return action;
    }

    public ComponentContainer getContainer() {
        return container;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }
}
