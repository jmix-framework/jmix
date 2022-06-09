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
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;

/**
 * EntitySuggestionField adds to EntityPicker the ability to search an entity by user input.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "EntitySuggestionField",
        category = "Components",
        xmlElement = "entitySuggestionField",
        icon = "io/jmix/ui/icon/component/entitySuggestionField.svg",
        canvasBehaviour = CanvasBehaviour.VALUE_PICKER,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/entity-suggestion-field.html",
        unsupportedProperties = {"buffered", "fieldEditable"},
        unsupportedHandlers = {"addFieldValueChangeListener"}
)
public interface EntitySuggestionField<V> extends SuggestionField<V>, EntityPicker<V> {

    String NAME = "entitySuggestionField";

    static <T> ParameterizedTypeReference<EntitySuggestionField<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<EntitySuggestionField<T>>() {
        };
    }

    @StudioElement
    @Override
    void setSearchExecutor(@Nullable SearchExecutor<V> searchExecutor);
}
