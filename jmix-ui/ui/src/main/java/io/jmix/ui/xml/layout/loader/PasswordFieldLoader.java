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

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.CapsLockIndicator;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.TextInputField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class PasswordFieldLoader extends AbstractTextFieldLoader<PasswordField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(PasswordField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadMaxLength(resultComponent, element);
        loadInputPrompt(resultComponent, element);

        loadAutoComplete(resultComponent, element);
        loadCapsLockIndicator(resultComponent, element);
        loadTextChangeEventProperties(resultComponent, element);
        loadHtmlName(resultComponent, element);
    }

    protected void loadCapsLockIndicator(PasswordField component, Element element) {
        String capsLockIndicator = element.attributeValue("capsLockIndicator");
        if (StringUtils.isNotEmpty(capsLockIndicator)) {
            if (component.getCapsLockIndicator() == null) {
                Component bindComponent = findComponent(capsLockIndicator);
                if (bindComponent instanceof CapsLockIndicator) {
                    component.setCapsLockIndicator((CapsLockIndicator) bindComponent);
                } else if (bindComponent != null) {
                    throw new GuiDevelopmentException("Unsupported 'capsLockIndicator' component class",
                            context, "componentId", component.getId());
                } else {
                    throw new GuiDevelopmentException("Unable to find capsLockIndicator component with id: " +
                            capsLockIndicator, context);
                }
            }
        }
    }

    protected void loadAutoComplete(PasswordField component, Element element) {
        String autocomplete = element.attributeValue("autocomplete");
        if (StringUtils.isNotEmpty(autocomplete)) {
            component.setAutocomplete(Boolean.parseBoolean(autocomplete));
        }
    }

    protected void loadHtmlName(TextInputField.HtmlNameSupported component, Element element) {
        String htmlName = element.attributeValue("htmlName");
        if (htmlName != null && !htmlName.isEmpty()) {
            component.setHtmlName(htmlName);
        }
    }
}