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

package io.jmix.flowui.xml.layout.support;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;

import java.util.Optional;
import java.util.function.Consumer;

@org.springframework.stereotype.Component("flowui_IconLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IconLoaderSupport implements ApplicationContextAware {

    protected static final String DEFAULT_ICON_ELEMENT_NAME = "icon";

    protected ComponentLoader.Context context;
    protected LayoutLoader layoutLoader;
    protected LoaderSupport loaderSupport;

    protected ApplicationContext applicationContext;

    public IconLoaderSupport(ComponentLoader.Context context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void loadIcon(Element element,
                         Consumer<Component> setter) {
        loadIcon(element, DEFAULT_ICON_ELEMENT_NAME, setter);
    }

    public void loadIcon(Element element, String iconElementName, Consumer<Component> setter) {
        loadIcon(element, iconElementName)
                .ifPresent(setter);
    }

    public Optional<Component> loadIcon(Element element) {
        return loadIcon(element, DEFAULT_ICON_ELEMENT_NAME);
    }

    public Optional<Component> loadIcon(Element element, String iconElementName) {
        Element iconElement = element.element(iconElementName);
        if (iconElement != null) {
            return loadIconComponent(iconElement);
        } else {
            return loaderSupport().loadString(element, iconElementName)
                    .map(ComponentUtils::parseIcon);
        }
    }

    protected Optional<Component> loadIconComponent(Element iconElement) {
        if (iconElement.elements().isEmpty()) {
            throw new GuiDevelopmentException("Icon component cannot be empty",
                    context);
        } else if (iconElement.elements().size() > 1) {
            throw new GuiDevelopmentException("Only one component can be defined as an icon",
                    context);
        }

        Element componentElement = iconElement.elements().get(0);
        ComponentLoader<?> componentLoader = getLayoutLoader().createComponentLoader(componentElement);
        // TODO: gg, check allowed list or correct component loading?
        componentLoader.initComponent();
        componentLoader.loadComponent();

        return Optional.of(componentLoader.getResultComponent());
    }

    protected LoaderSupport loaderSupport() {
        if (loaderSupport == null) {
            loaderSupport = applicationContext.getBean(LoaderSupport.class, context);
        }

        return loaderSupport;
    }

    protected LayoutLoader getLayoutLoader() {
        if (layoutLoader == null) {
            layoutLoader = applicationContext.getBean(LayoutLoader.class, context);
        }

        return layoutLoader;
    }
}
