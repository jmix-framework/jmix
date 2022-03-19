/*
 * Copyright 2020 Haulmont.
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
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.HasCaptionMode;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.OptionsField;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.xml.layout.loader.ComboBoxLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class LookupFieldLoader extends ComboBoxLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(LookupField.NAME);
        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadComponent() {
        super.loadComponent();

        LookupField lookupField = (LookupField) resultComponent;

        loadNewOptionAllowed(lookupField, element);
        loadNewOptionHandler(lookupField, element);
        ComponentLoaderHelper.loadCaptionProperty((HasCaptionMode) resultComponent, element);
        ComponentLoaderHelper.loadValidators((Field) resultComponent, element, context, getClassManager(), getMessages());
    }

    protected void loadNewOptionAllowed(LookupField lookupField, Element element) {
        String newOptionAllowed = element.attributeValue("newOptionAllowed");
        if (StringUtils.isNotEmpty(newOptionAllowed)) {
            lookupField.setNewOptionAllowed(Boolean.parseBoolean(newOptionAllowed));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(ComboBox component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);

        DatasourceLoaderHelper
                .loadOptionsDatasourceIfOptionsNull((OptionsField) resultComponent, element,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setOptions);
    }

    protected void loadNewOptionHandler(LookupField component, Element element) {
        String newOptionHandlerMethod = element.attributeValue("newOptionHandler");
        if (StringUtils.isNotEmpty(newOptionHandlerMethod)) {
            FrameOwner controller = getComponentContext().getFrame().getFrameOwner();
            Class<? extends FrameOwner> windowClass = controller.getClass();

            Method newOptionHandler;
            try {
                newOptionHandler = windowClass.getMethod(newOptionHandlerMethod, ComboBox.class, String.class);
            } catch (NoSuchMethodException e) {
                Map<String, Object> params = ParamsMap.of(
                        "LookupField Id", component.getId(),
                        "Method name", newOptionHandlerMethod
                );

                throw new GuiDevelopmentException("Unable to find new option handler method for lookup field",
                        context, params);
            }

            component.setNewOptionHandler(caption -> {
                try {
                    newOptionHandler.invoke(controller, component, caption);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Unable to invoke new option handler", e);
                }
            });
        }
    }
}
