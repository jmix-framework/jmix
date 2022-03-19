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
package io.jmix.ui.xml.layout.loader;

import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.Component;
import org.dom4j.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Function;

public class ButtonsPanelLoader extends ContainerLoader<ButtonsPanel> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(ButtonsPanel.NAME);
        loadId(resultComponent, element);
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadId(resultComponent, element);
        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);

        loadSpacing(resultComponent, element);
        loadMargin(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        if (!element.elements().isEmpty()) {
            loadSubComponents();
        } else {
            loadProviderClass(resultComponent, element);
        }
    }

    protected void loadProviderClass(ButtonsPanel resultComponent, Element element) {
        loadString(element, "providerClass")
                .ifPresent(className -> {
                    Class<Function<UiComponents, Collection<Component>>> clazz = ReflectionHelper.getClass(className);

                    Function<UiComponents, Collection<Component>> instance;
                    try {
                        Constructor<Function<UiComponents, Collection<Component>>> constructor = clazz.getConstructor();
                        instance = constructor.newInstance();
                    } catch (NoSuchMethodException | InstantiationException
                            | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException("Unable to apply buttons provider", e);
                    }

                    applyComponentsProvider(resultComponent, instance);
                });
    }

    protected void applyComponentsProvider(ButtonsPanel panel,
                                           Function<UiComponents, Collection<Component>> componentsProvider) {
        Collection<Component> components = componentsProvider.apply(getFactory());
        for (Component button : components) {
            panel.add(button);
        }
    }
}
