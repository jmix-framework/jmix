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

package com.haulmont.cuba.gui;

import com.google.common.reflect.TypeToken;
import io.jmix.ui.component.Component;

/**
 * @deprecated Use {@link io.jmix.ui.UiComponents} instead
 */
@Deprecated
public interface UiComponents extends io.jmix.ui.UiComponents {

    /**
     * Create a component instance by its type.
     *
     * @param type component type token
     * @return component instance for the current client type (web or desktop)
     * @see io.jmix.ui.component.Label#TYPE_DEFAULT
     * @see io.jmix.ui.component.TextField#TYPE_DEFAULT
     */
    <T extends Component> T create(TypeToken<T> type);
}
