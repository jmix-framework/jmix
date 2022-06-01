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

package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Collection;

@StudioComponent(
        caption = "ValuesPicker",
        category = "Components",
        xmlElement = "valuesPicker",
        icon = "io/jmix/ui/icon/component/valuesPicker.svg",
        canvasBehaviour = CanvasBehaviour.VALUE_PICKER,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/values-picker.html"
)
public interface ValuesPicker<V> extends ValuePicker<Collection<V>> {

    String NAME = "valuesPicker";

    ParameterizedTypeReference<ValuesPicker<String>> TYPE_STRING =
            new ParameterizedTypeReference<ValuesPicker<String>>() {
            };

    static <T> ParameterizedTypeReference<ValuesPicker<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<ValuesPicker<T>>() {
        };
    }
}
