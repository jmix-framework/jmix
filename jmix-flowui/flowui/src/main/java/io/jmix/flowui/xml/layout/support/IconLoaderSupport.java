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

/**
 * Utility class for loading component icons.
 */
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

    /**
     * Loads an icon component from the given XML {@code element} and applies it
     * using the provided {@code setter}. The method first attempts to find the nested icon
     * element with the default name within the given element. If the icon element
     * is found, an icon component is loaded and passed to the {@code setter}.
     * Otherwise, it attempts to load an icon from the fallback attribute with the
     * default name.
     *
     * @param element the XML element to load the icon from
     * @param setter  the setter used to process the loaded icon
     */
    public void loadIcon(Element element,
                         Consumer<Component> setter) {
        loadIcon(element, DEFAULT_ICON_ELEMENT_NAME, DEFAULT_ICON_ELEMENT_NAME, setter);
    }

    /**
     * Loads an icon component from the given XML {@code element} and applies it
     * using the provided {@code setter}. The method first attempts to find the nested icon
     * element with the given name within the given element. If the icon element
     * is found, an icon component is loaded and passed to the {@code setter}.
     * Otherwise, it attempts to load an icon from the fallback attribute the same
     * name as the given icon element name.
     *
     * @param element         the XML element to load the icon from
     * @param iconElementName the name of the child element that stores the icon
     * @param setter          the setter used to process the loaded icon
     */
    public void loadIcon(Element element, String iconElementName, Consumer<Component> setter) {
        loadIcon(element, iconElementName, iconElementName)
                .ifPresent(setter);
    }

    /**
     * Loads an icon component from the given XML {@code element} and applies it
     * using the provided {@code setter}. The method first attempts to find the nested icon
     * element with the given name within the given element. If the icon element
     * is found, an icon component is loaded and passed to the {@code setter}.
     * Otherwise, it attempts to load an icon from the specified fallback attribute of
     * the given element.
     *
     * @param element           the XML element to load the icon from
     * @param iconElementName   the name of the child element that stores the icon
     * @param iconAttributeName the name of the fallback attribute that represents
     *                          the icon if no icon element is found
     * @param setter            the setter used to process the loaded icon
     */
    public void loadIcon(Element element,
                         String iconElementName, String iconAttributeName,
                         Consumer<Component> setter) {
        loadIcon(element, iconElementName, iconAttributeName)
                .ifPresent(setter);
    }

    /**
     * Loads an icon component from the given XML {@code element}. The method first
     * attempts to find the nested icon element with the default name within the given
     * element. If the icon element is found, the icon component is loaded from it.
     * Otherwise, it attempts to load an icon from the fallback attribute with the
     * default name.
     *
     * @param element the XML element to load the icon from
     * @return an {@link Optional} containing the loaded icon {@link Component},
     * or an empty {@link Optional}
     * if no icon could be loaded
     */
    public Optional<Component> loadIcon(Element element) {
        return loadIcon(element, DEFAULT_ICON_ELEMENT_NAME, DEFAULT_ICON_ELEMENT_NAME);
    }

    /**
     * Loads an icon component from the given XML {@code element}. The method first
     * attempts to find the nested icon element with the given name within the given
     * element. If the icon element is found, the icon component is loaded from it.
     * Otherwise, it attempts to load an icon from the fallback attribute with the same
     * name as the given icon element name. If no icon is found, an empty {@link Optional}
     * is returned.
     *
     * @param element         the XML element to load the icon from
     * @param iconElementName the name of the child element that stores the icon
     * @return an {@link Optional} containing the loaded icon {@link Component},
     * or an empty {@link Optional} if no icon could be loaded
     */
    public Optional<Component> loadIcon(Element element, String iconElementName) {
        return loadIcon(element, iconElementName, iconElementName);
    }

    /**
     * Loads an icon component from the given XML {@code element}. The method first
     * attempts to find the nested icon element with the given name within the given
     * element. If the icon element is found, the icon component is loaded from it.
     * Otherwise, it attempts to load an icon from the given fallback attribute.
     * If no icon is found, an empty {@link Optional} is returned.
     *
     * @param element           the XML element to load the icon from
     * @param iconElementName   the name of the child element that stores the icon
     * @param iconAttributeName the name of the fallback attribute that represents
     *                          the icon if no icon element is found
     * @return an {@link Optional} containing the loaded icon {@link Component}, or
     * an empty {@link Optional} if no icon could be loaded
     */
    public Optional<Component> loadIcon(Element element,
                                        String iconElementName, String iconAttributeName) {
        Element iconElement = element.element(iconElementName);
        if (iconElement != null) {
            return loadIconComponent(iconElement);
        } else {
            return loaderSupport().loadString(element, iconAttributeName)
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
