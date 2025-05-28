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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class HorizontalLayoutLoader extends AbstractLayoutLoader<HorizontalLayout> {

    @Override
    protected HorizontalLayout createComponent() {
        return factory.create(HorizontalLayout.class);
    }

    @Override
    public void loadComponent() {
        loadSlot("startSlot", resultComponent::addToStart);
        loadSlot("middleSlot", resultComponent::addToMiddle);
        loadSlot("endSlot", resultComponent::addToEnd);

        super.loadComponent();
    }

    protected void loadSlot(String slotName, Consumer<Collection<Component>> setter) {
        Element slotElement = element.element(slotName);

        if (slotElement == null || slotElement.elements().isEmpty()) {
            return;
        }

        List<Component> slotChildren = slotElement.elements()
                .stream()
                .map(this::createSlotChild)
                .toList();

        setter.accept(slotChildren);
    }

    protected Component createSlotChild(Element element) {
        ComponentLoader<?> componentLoader = getLayoutLoader().createComponentLoader(element);
        componentLoader.initComponent();

        pendingLoadComponents.add(componentLoader);
        return componentLoader.getResultComponent();
    }

    @Override
    protected boolean isChildElementIgnored(Element subElement) {
        return "startSlot".equals(subElement.getName())
                || "middleSlot".equals(subElement.getName())
                || "endSlot".equals(subElement.getName());
    }
}
