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
 * A filtering dropdown single-select. Items are filtered based on user input using asynchronous data loading.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "SuggestionField",
        category = "Components",
        xmlElement = "suggestionField",
        icon = "io/jmix/ui/icon/component/suggestionField.svg",
        canvasBehaviour = CanvasBehaviour.INPUT_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/suggestion-field.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface SuggestionField<V> extends SuggestionFieldComponent<V, V> {

    String NAME = "suggestionField";

    static <T> ParameterizedTypeReference<SuggestionField<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<SuggestionField<T>>() {};
    }
}
