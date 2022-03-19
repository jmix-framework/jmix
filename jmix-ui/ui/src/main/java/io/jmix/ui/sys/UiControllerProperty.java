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

package io.jmix.ui.sys;

import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Immutable POJO that stores name, value and type of property that will be injected
 * into UI controller.
 *
 * @see UiControllerPropertyInjector
 */
@StudioElement(caption = "Controller Property",
        xmlElement = "property",
        icon = "io/jmix/ui/icon/element/property.svg")
@StudioProperties(
        properties = {
                @StudioProperty(name = "ref", type = PropertyType.STRING)
        },
        groups = {
                @PropertiesGroup(properties = {"value", "ref"}, constraint = PropertiesConstraint.ONE_OF)
        }
)
public class UiControllerProperty {

    protected final String name;
    protected final Object value;
    protected final Type type;

    public UiControllerProperty(String name, Object value, Type type) {
        checkNotEmptyString(name, "Controller property name cannot be empty");
        checkNotNullArgument(value, "Controller property value cannot be null");
        checkNotNullArgument(type, "Controller property type cannot be null");

        this.name = name;
        this.value = value;
        this.type = type;
    }

    @StudioProperty(required = true)
    public String getName() {
        return name;
    }

    @StudioProperty(type = PropertyType.STRING)
    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    /**
     * Type defines what should be passed to controller property:
     * some value (String, boolean, etc) or a reference to an object.
     */
    public enum Type {
        VALUE,
        REFERENCE
    }
}
