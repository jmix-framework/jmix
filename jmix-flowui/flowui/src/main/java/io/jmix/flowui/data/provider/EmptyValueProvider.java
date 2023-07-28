/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.data.provider;

import com.vaadin.flow.function.ValueProvider;

/**
 * Value provider that returns an empty string for passed item. Can be used as a placeholder
 * before an actual value provider is set.
 *
 * @param <T> item type
 */
public class EmptyValueProvider<T> implements ValueProvider<T, String> {

    @Override
    public String apply(T t) {
        return "";
    }
}
