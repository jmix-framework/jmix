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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.login.LoginOverlay;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoginOverlayLoader extends AbstractLoginFormLoader<LoginOverlay> {

    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    @Override
    protected LoginOverlay createComponent() {
        return factory.create(LoginOverlay.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        Element customFormAreaElement = element.element("customFormArea");
        if (customFormAreaElement != null) {
            createSubComponents(resultComponent.getCustomFormArea()::add, customFormAreaElement);
        }

        Element footerElement = element.element("footer");
        if (footerElement != null) {
            createSubComponents(resultComponent.getFooter()::add, footerElement);
        }
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadBoolean(element, "opened", resultComponent::setOpened);

        loadSubComponents();
    }

    protected void createSubComponents(Consumer<Component> setter, Element containerElement) {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : containerElement.elements()) {
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
}
