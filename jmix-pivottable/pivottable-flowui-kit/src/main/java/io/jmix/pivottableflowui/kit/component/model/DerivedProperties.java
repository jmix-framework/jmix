/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines derived properties
 * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Derived-Attributes">documentation</a>).
 */
public class DerivedProperties extends PivotTableOptionsObservable {

    protected Map<String, JsFunction> properties;

    /**
     * @return a map of derived properties
     */
    public Map<String, JsFunction> getProperties() {
        return properties;
    }

    /**
     * Sets a map of derived properties.
     *
     * @param derivedProperties a map of derived properties
     */
    public void setProperties(Map<String, JsFunction> derivedProperties) {
        this.properties = derivedProperties;
        markAsChanged();
    }

    /**
     * Adds a map of derived properties.
     *
     * @param derivedProperties a map of derived properties
     */
    public void addAttributes(Map<String, JsFunction> derivedProperties) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.putAll(derivedProperties);
        markAsChanged();
    }

    /**
     * Adds a function that calculated a derived property to the given localized attribute name.
     *
     * @param caption  a localized attribute name
     * @param function a function that calculated a derived property
     */
    public void addAttribute(String caption, JsFunction function) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(caption, function);
        markAsChanged();
    }
}
