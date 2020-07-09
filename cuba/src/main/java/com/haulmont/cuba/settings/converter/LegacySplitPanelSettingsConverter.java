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
import io.jmix.ui.settings.component.SplitPanelSettings;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LegacySplitPanelSettingsConverter implements LegacySettingsConverter {

    @Override
    public Element convertToElement(ComponentSettings settings) {
        Element element = DocumentHelper.createElement("component");

        copySettingsToElement((SplitPanelSettings) settings, element);

        return element;
    }

    @Override
    public void copyToElement(ComponentSettings settings, Element element) {
        element.attributes().clear();
        element.clearContent();

        copySettingsToElement((SplitPanelSettings) settings, element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComponentSettings> T convertToComponentSettings(Element settings) {
        SplitPanelSettings splitPanelSettings = createSettings();
        splitPanelSettings.setId(settings.attributeValue("name"));

        Element positionElem = settings.element("position");
        if (positionElem != null) {
            String value = positionElem.attributeValue("value");
            String unit = positionElem.attributeValue("unit");

            if (StringUtils.isNotBlank(value)
                    && StringUtils.isNotBlank(unit)) {
                splitPanelSettings.setPositionValue(Float.parseFloat(value));
                splitPanelSettings.setPositionUnit(unit);
            }
        }

        return (T) splitPanelSettings;
    }

    protected void copySettingsToElement(SplitPanelSettings settings, Element element) {
        element.addAttribute("name", settings.getId());

        Float value = settings.getPositionValue();
        String positionUnit = settings.getPositionUnit();

        if (value == null || StringUtils.isBlank(positionUnit)) {
            return;
        }

        Element positionElem = element.addElement("position");

        positionElem.addAttribute("value", String.valueOf(value));
        positionElem.addAttribute("unit", positionUnit);
    }

    protected SplitPanelSettings createSettings() {
        return new SplitPanelSettings();
    }
}
