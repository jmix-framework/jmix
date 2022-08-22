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

package io.jmix.flowui.component.accordion;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.dom.Element;

import java.util.stream.Stream;

public class JmixAccordion extends Accordion implements HasOrderedComponents {

    /*@Override
    public Stream<Component> getChildren() {
        return super.getContent();
    }

    @Override
    public void add(Component... components) {
        super.addContent(components);
    }


    @Override
    public void remove(Component... components) {
        getContentContainer().remove(components);
    }

    @Override
    public void removeAll() {
        getContentContainer().removeAll();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        getContentContainer().addComponentAtIndex(index, component);
    }

    @Override
    public void addComponentAsFirst(Component component) {
        getContentContainer().addComponentAsFirst(component);
    }

    protected HasOrderedComponents getContentContainer() {
        Element firstChild = getElement().getChild(0);
        return firstChild.getComponent()
                .filter(component -> component instanceof HasOrderedComponents)
                .map(component -> ((HasOrderedComponents) component))
                .orElseThrow(() ->
                        new IllegalStateException(Details.class.getSimpleName() +
                                " content doesn't contain components"));
    }*/
}
