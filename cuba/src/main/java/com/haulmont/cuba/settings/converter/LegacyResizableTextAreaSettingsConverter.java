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

package com.haulmont.cuba.settings.converter;

import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.ResizableTextAreaSettings;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LegacyResizableTextAreaSettingsConverter implements LegacySettingsConverter {

    @Override
    public Element convertToElement(ComponentSettings settings) {
        Element element = DocumentHelper.createElement("component");

        copySettingsToElement((ResizableTextAreaSettings) settings, element);

        return element;
    }

    @Override
    public void copyToElement(ComponentSettings settings, Element element) {
        element.attributes().clear();
        element.clearContent();

        copySettingsToElement((ResizableTextAreaSettings) settings, element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComponentSettings> T convertToComponentSettings(Element settings) {
        ResizableTextAreaSettings textAreaSettings = createSettings();
        textAreaSettings.setId(settings.attributeValue("name"));

        String width = settings.attributeValue("width");
        String height = settings.attributeValue("height");

        if (isNotBlank(width) && isNotBlank(height)) {
            textAreaSettings.setWidth(width);
            textAreaSettings.setHeight(height);
        }

        return (T) textAreaSettings;
    }

    protected void copySettingsToElement(ResizableTextAreaSettings settings, Element element) {
        element.addAttribute("name", settings.getId());

        if (isNotBlank(settings.getHeight())
                && isNotBlank(settings.getWidth())) {
            element.addAttribute("width", settings.getWidth());
            element.addAttribute("height", settings.getHeight());
        }
    }

    protected ResizableTextAreaSettings createSettings() {
        return new ResizableTextAreaSettings();
    }
}
