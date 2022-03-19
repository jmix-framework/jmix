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

package io.jmix.pivottable.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines derived properties
 * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Derived-Attributes">documentation</a>).
 */
public class DerivedProperties extends AbstractPivotObject {
    private static final long serialVersionUID = -2113616038227536186L;

    private Map<String, JsFunction> properties;

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
     * @return a reference to this object
     */
    public DerivedProperties setProperties(Map<String, JsFunction> derivedProperties) {
        this.properties = derivedProperties;
        return this;
    }

    /**
     * Adds a map of derived properties.
     *
     * @param derivedProperties a map of derived properties
     * @return a reference to this object
     */
    public DerivedProperties addAttributes(Map<String, JsFunction> derivedProperties) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.putAll(derivedProperties);
        return this;
    }

    /**
     * Adds a function that calculated a derived property to the given localized attribute name.
     *
     * @param caption  a localized attribute name
     * @param function a function that calculated a derived property
     * @return a reference to this object
     */
    public DerivedProperties addAttribute(String caption, JsFunction function) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(caption, function);
        return this;
    }
}
