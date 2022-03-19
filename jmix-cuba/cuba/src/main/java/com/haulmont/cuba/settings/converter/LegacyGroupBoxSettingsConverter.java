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
import io.jmix.ui.settings.component.GroupBoxSettings;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LegacyGroupBoxSettingsConverter implements LegacySettingsConverter {

    @Override
    public Element convertToElement(ComponentSettings settings) {
        Element element = DocumentHelper.createElement("component");

        copySettingsToElement((GroupBoxSettings) settings, element);

        return element;
    }

    @Override
    public void copyToElement(ComponentSettings settings, Element element) {
        element.attributes().clear();
        element.clearContent();

        copySettingsToElement((GroupBoxSettings) settings, element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComponentSettings> T convertToComponentSettings(Element settings) {
        GroupBoxSettings groupBoxSettings = createSettings();
        groupBoxSettings.setId(settings.attributeValue("name"));

        Element groupBoxElem = settings.element("groupBox");
        if (groupBoxElem != null) {
            String expanded = groupBoxElem.attributeValue("expanded");
            if (expanded != null) {
                groupBoxSettings.setExpanded(Boolean.parseBoolean(expanded));
            }
        }

        return (T) groupBoxSettings;
    }

    protected void copySettingsToElement(GroupBoxSettings settings, Element element) {
        element.addAttribute("name", settings.getId());

        if (settings.getExpanded() != null) {
            Element groupBoxElem = element.addElement("groupBox");
            groupBoxElem.addAttribute("expanded", settings.getExpanded().toString());
        }
    }

    protected GroupBoxSettings createSettings() {
        return new GroupBoxSettings();
    }
}
