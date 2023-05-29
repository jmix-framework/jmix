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

import com.haulmont.cuba.settings.component.CubaGroupTableSettings;
import io.jmix.ui.settings.component.GroupTableSettings;
import io.jmix.ui.settings.component.TableSettings;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class LegacyGroupTableSettingsConverter extends LegacyTableSettingsConverter {

    @Override
    public GroupTableSettings convertToComponentSettings(Element settings) {
        GroupTableSettings groupTableSettings = (GroupTableSettings) super.convertToComponentSettings(settings);

        Element groupPropertiesElem = settings.element("groupProperties");
        if (groupPropertiesElem != null) {
            List<Element> properties = groupPropertiesElem.elements("property");
            if (properties.isEmpty()) {
                return groupTableSettings;
            }

            List<String> groupProperties = new ArrayList<>(properties.size());
            for (Element property : properties) {
                groupProperties.add(property.attributeValue("id"));
            }

            groupTableSettings.setGroupProperties(groupProperties);
        }

        return groupTableSettings;
    }

    @Override
    protected void copySettingsToElement(TableSettings tableSettings, Element element) {
        super.copySettingsToElement(tableSettings, element);

        GroupTableSettings settings = (GroupTableSettings) tableSettings;

        List<String> groupProperties = settings.getGroupProperties();
        if (groupProperties != null) {
            Element groupPropertiesElem = element.addElement("groupProperties");

            for (String property : groupProperties) {
                Element propertyElem = groupPropertiesElem.addElement("property");
                propertyElem.addAttribute("id", property);
            }
        }
    }

    @Override
    protected TableSettings createSettings() {
        return new CubaGroupTableSettings();
    }
}
