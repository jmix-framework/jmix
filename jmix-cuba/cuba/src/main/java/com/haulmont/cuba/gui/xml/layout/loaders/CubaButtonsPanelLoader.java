/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.ButtonsPanel;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.component.Component;
import io.jmix.ui.xml.layout.loader.ButtonsPanelLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Supplier;

public class CubaButtonsPanelLoader extends ButtonsPanelLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(ButtonsPanel.NAME);
        loadId(resultComponent, element);
        createSubComponents(resultComponent, element);
    }

    @Override
    protected void loadProviderClass(io.jmix.ui.component.ButtonsPanel resultComponent, Element element) {
        String className = element.attributeValue("providerClass");
        if (StringUtils.isNotEmpty(className)) {
            Class<Supplier<Collection<Component>>> clazz = ReflectionHelper.getClass(className);

            Supplier<Collection<Component>> instance;
            try {
                Constructor<Supplier<Collection<Component>>> constructor = clazz.getConstructor();
                instance = constructor.newInstance();
            } catch (NoSuchMethodException | InstantiationException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Unable to apply buttons provider", e);
            }

            applyComponentsProvider(resultComponent, instance);
        }
    }

    protected void applyComponentsProvider(io.jmix.ui.component.ButtonsPanel panel,
                                           Supplier<Collection<Component>> buttonsProvider) {
        Collection<Component> buttons = buttonsProvider.get();
        for (Component button : buttons) {
            panel.add(button);
        }
    }
}
