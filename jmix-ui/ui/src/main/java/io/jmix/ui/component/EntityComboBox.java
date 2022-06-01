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
import org.springframework.core.ParameterizedTypeReference;

/**
 * A components that adds to {@link EntityPicker} the ability to select an entity from drop-down list.
 */
@StudioComponent(
        caption = "EntityComboBox",
        category = "Components",
        xmlElement = "entityComboBox",
        icon = "io/jmix/ui/icon/component/entityComboBox.svg",
        canvasBehaviour = CanvasBehaviour.COMBO_BOX,
        unsupportedProperties = {"fieldEditable", "optionsEnum", "textInputAllowed"},
        unsupportedHandlers = {"addFieldValueChangeListener", "setFormatter", "setFieldIconProvider"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/entity-combo-box.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "optionsContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                        typeParameter = "V"),
                @StudioProperty(name = "captionProperty", type = PropertyType.PROPERTY_PATH_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"optionsContainer", "captionProperty"}),
        }
)
public interface EntityComboBox<V> extends ComboBox<V>, EntityPicker<V>, SupportsOptionsContainer<V> {

    String NAME = "entityComboBox";

    static <T> ParameterizedTypeReference<EntityComboBox<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<EntityComboBox<T>>() {
        };
    }
}
