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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CardLoader extends AbstractComponentLoader<Card> {

    protected static final Set<String> singleChildElements =
            Set.of("title", "subtitle", "media", "headerPrefix", "header", "headerSuffix");
    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    @Override
    protected Card createComponent() {
        return factory.create(Card.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createSubComponent("title", resultComponent::setTitle);
        createSubComponent("subtitle", resultComponent::setSubtitle);

        createSubComponent("media", resultComponent::setMedia);
        createSubComponent("content", resultComponent::add);

        createSubComponent("headerPrefix", resultComponent::setHeaderPrefix);
        createSubComponent("header", resultComponent::setHeader);
        createSubComponent("headerSuffix", resultComponent::setHeaderSuffix);

        createSubComponent("footer", resultComponent::addToFooter);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "title", context.getMessageGroup(), this::titleSetter);
        loadResourceString(element, "subtitle", context.getMessageGroup(), this::subTitleSetter);
        loadInteger(element, "titleHeadingLevel", resultComponent::setTitleHeadingLevel);

        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);

        loadSubComponents();
    }

    protected void createSubComponent(String elementName, Consumer<Component> setter) {
        Element containerElement = element.element(elementName);
        if (containerElement == null) {
            return;
        }

        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : containerElement.elements()) {
            validateChildElement(subElement);

            ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
            componentLoader.initComponent();
            pendingLoadComponents.add(componentLoader);

            setter.accept(componentLoader.getResultComponent());
        }
    }

    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
        }

        pendingLoadComponents.clear();
    }

    protected void titleSetter(String title) {
        if (resultComponent.getTitle() != null) {
            return;
        }

        resultComponent.setTitle(title);
    }

    protected void subTitleSetter(String subTitle) {
        if (resultComponent.getSubtitle() != null) {
            return;
        }

        Div subtitleComponent = factory.create(Div.class);
        subtitleComponent.setClassName("subtitle");
        subtitleComponent.setText(subTitle);

        resultComponent.setSubtitle(subtitleComponent);
    }

    protected void validateChildElement(Element element) {
        String elementName = element.getName();

        if (singleChildElements.contains(elementName) && element.elements().size() > 1) {
            throw new GuiDevelopmentException("%s can contain only one child element".formatted(elementName),
                    context, "Component ID", resultComponent.getId());
        }
    }
}
