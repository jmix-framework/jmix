/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.Element;

import java.util.Collection;

/**
 * Abstract wrapper class for {@link Dialog.DialogHeader} and {@link Dialog.DialogFooter} that is used for
 * {@link DialogWindow} configuration.
 */
public abstract class AbstractDialogWindowHeaderFooter implements HasComponents {

    protected HasComponents rootComponent;

    protected AbstractDialogWindowHeaderFooter(HasComponents rootComponent) {
        this.rootComponent = rootComponent;
    }

    @Override
    public void add(Component... components) {
        rootComponent.add(components);
    }

    @Override
    public void add(Collection<Component> components) {
        rootComponent.add(components);
    }

    @Override
    public void add(String text) {
        rootComponent.add(text);
    }

    @Override
    public void remove(Component... components) {
        rootComponent.remove(components);
    }

    @Override
    public void remove(Collection<Component> components) {
        rootComponent.remove(components);
    }

    @Override
    public void removeAll() {
        rootComponent.removeAll();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        rootComponent.addComponentAtIndex(index, component);
    }

    @Override
    public void addComponentAsFirst(Component component) {
        rootComponent.addComponentAsFirst(component);
    }

    @Override
    public Element getElement() {
        return rootComponent.getElement();
    }
}
