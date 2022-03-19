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

import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.TableSettings;
import org.dom4j.Element;

/**
 * Base interface for converters.  As {@link HasSettings#saveSettings(Element)} and
 * {@link HasSettings#applySettings(Element)} are deprecated we need to support XML elements for legacy screens that do
 * not support new screen settings API.
 */
public interface LegacySettingsConverter {

    /**
     * @param settings component settings
     * @return new element with copied settings
     */
    Element convertToElement(ComponentSettings settings);

    /**
     * Will copy all settings from ComponentSettings to an element.
     *
     * @param settings component settings
     * @param element  element to modify
     */
    void copyToElement(ComponentSettings settings, Element element);

    /**
     * @param settings element that should be converted
     * @param <T>      type of component settings, e.g. {@link TableSettings}
     * @return object with copied settings
     */
    <T extends ComponentSettings> T convertToComponentSettings(Element settings);
}
