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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerLoader<T extends Component> extends AbstractComponentLoader<T> {

    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
        }

        pendingLoadComponents.clear();
    }

    protected void createSubComponents(HasComponents container, Element containerElement) {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : containerElement.elements()) {
            if (!isChildElementIgnored(subElement)) {
                ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
                componentLoader.initComponent();
                pendingLoadComponents.add(componentLoader);

                container.add(componentLoader.getResultComponent());
            }
        }
    }

    /**
     * Checks if child element should have a separate loader or not.
     * For instance, if a child element doesn't represent a UI component.
     *
     * @param subElement the element to be checked
     * @return {@code true} if child element should have no separate loader, {@code false} otherwise
     */
    protected boolean isChildElementIgnored(Element subElement) {
        return false;
    }

    protected void loadSubComponentsAndExpand(HasComponents layout, Element element) {
        loadSubComponents();

        if (layout instanceof FlexComponent) {
            loadString(element, "expand").ifPresent(componentId -> {
                Component componentToExpand = UiComponentUtils.findComponent(((Component) layout), componentId)
                        .orElseThrow(() -> new GuiDevelopmentException(
                                String.format("There is no component with id '%s' to expand", componentId), context));
                ((FlexComponent) layout).expand(componentToExpand);
            });
        }
    }

    // TODO: what is it?
    /*protected void setComponentsRatio(ComponentContainer resultComponent, Element element) {
        if (!(resultComponent instanceof SupportsExpandRatio)) {
            return;
        }

        List<Element> elements = element.elements();
        if (elements.isEmpty()) {
            return;
        }

        SupportsExpandRatio supportsRatio = (SupportsExpandRatio) resultComponent;
        List<Component> ownComponents = resultComponent.getOwnComponentsStream().collect(Collectors.toList());
        if (ownComponents.size() != elements.size()) {
            return;
        }

        for (int i = 0; i < elements.size(); i++) {
            String stringRatio = elements.get(i).attributeValue("box.expandRatio");
            if (!Strings.isNullOrEmpty(stringRatio)) {

                Component subComponent = ownComponents.get(i);
                if (subComponent != null) {
                    float ratio = Float.parseFloat(stringRatio);
                    supportsRatio.setExpandRatio(subComponent, ratio);
                }
            }
        }
    }*/

    /*@Nullable
    protected String find(String[] parts, String name) {
        String prefix = name + "=";

        for (String part : parts) {
            if (part.trim().startsWith(prefix)) {
                return part.trim().substring((prefix).length()).trim();
            }
        }
        return null;
    }*/
}
