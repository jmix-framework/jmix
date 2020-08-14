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


import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ComboBoxLoader extends AbstractFieldLoader<ComboBox> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(ComboBox.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        loadCaptionProperty(resultComponent, element);

        String nullName = element.attributeValue("nullName");
        if (StringUtils.isNotEmpty(nullName)) {
            resultComponent.setNullSelectionCaption(loadResourceString(nullName));
        }

        String pageLength = element.attributeValue("pageLength");
        if (StringUtils.isNotEmpty(pageLength)) {
            resultComponent.setPageLength(Integer.parseInt(pageLength));
        }

        loadBuffered(resultComponent, element);

        loadTextInputAllowed();
        loadInputPrompt(resultComponent, element);

        loadFilterMode(resultComponent, element);
        loadNewOptionHandler(resultComponent, element);

        loadNullOptionVisible(resultComponent, element);

        loadOptionsEnum(resultComponent, element);
    }


    @SuppressWarnings("rawtypes")
    protected void loadCaptionProperty(ComboBox resultComponent, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            resultComponent.setOptionCaptionProvider(
                    new CaptionAdapter(captionProperty, applicationContext.getBean(Metadata.class), applicationContext.getBean(MetadataTools.class)));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadOptionsEnum(ComboBox resultComponent, Element element) {
        String optionsEnumClass = element.attributeValue("optionsEnum");
        if (StringUtils.isNotEmpty(optionsEnumClass)) {
            resultComponent.setOptionsEnum(getClassManager().findClass(optionsEnumClass));
        }
    }

    protected void loadNullOptionVisible(ComboBox resultComponent, Element element) {
        String nullOptionVisible = element.attributeValue("nullOptionVisible");
        if (StringUtils.isNotEmpty(nullOptionVisible)) {
            resultComponent.setNullOptionVisible(Boolean.parseBoolean(nullOptionVisible));
        }
    }

    protected void loadTextInputAllowed() {
        String textInputAllowed = element.attributeValue("textInputAllowed");
        if (StringUtils.isNotEmpty(textInputAllowed)) {
            resultComponent.setTextInputAllowed(Boolean.parseBoolean(textInputAllowed));
        }
    }

    protected void loadNewOptionHandler(ComboBox component, Element element) {
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

    protected void loadFilterMode(ComboBox component, Element element) {
        String filterMode = element.attributeValue("filterMode");
        if (!StringUtils.isEmpty(filterMode)) {
            component.setFilterMode(ComboBox.FilterMode.valueOf(filterMode));
        }
    }
}
