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
package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

@StudioComponent(
        caption = "CheckBox",
        category = "Components",
        xmlElement = "checkBox",
        icon = "io/jmix/ui/icon/component/checkBox.svg",
        unsupportedProperties = {"required", "requiredMessage"},
        canvasBehaviour = CanvasBehaviour.CHECK_BOX,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/check-box.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "boolean")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface CheckBox extends Field<Boolean>, Buffered, Component.Focusable {
    String NAME = "checkBox";

    /**
     * @return true if value is equal to {@link Boolean#TRUE}
     */
    boolean isChecked();
}