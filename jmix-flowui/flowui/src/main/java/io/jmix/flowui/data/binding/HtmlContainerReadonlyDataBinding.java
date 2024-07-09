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

package io.jmix.flowui.data.binding;

import com.vaadin.flow.component.HtmlContainer;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;

/**
 * Binds {@link HtmlContainer} with {@link ValueSource}, {@link InstanceContainer} or {@link CollectionContainer}
 * to show entity property value.
 */
public interface HtmlContainerReadonlyDataBinding {

    /**
     * Binds {@link HtmlContainer} with {@link ValueSource} to show {@link ValueSource#getValue()}
     * as a text value in html container
     * @param htmlContainer html container
     * @param valueSource data component holding a typed value
     */
    void bind(HtmlContainer htmlContainer, ValueSource<?> valueSource);

    /**
     * Binds {@link HtmlContainer} with {@link InstanceContainer} to show {@link InstanceContainer#getItem()} property
     * as a text value in html container
     * @param htmlContainer html container
     * @param dataContainer instance container
     * @param property name of an entity property whose value is showed in html container
     */
    void bind(HtmlContainer htmlContainer, InstanceContainer<?> dataContainer, String property);

    /**
     * Binds {@link HtmlContainer} with {@link CollectionContainer} to show {@link CollectionContainer#getItem()} property
     * as a text value in html container
     * @param htmlContainer html container
     * @param dataContainer collection container
     * @param property name of an entity property whose value is showed in html container
     */
    void bind(HtmlContainer htmlContainer, CollectionContainer<?> dataContainer, String property);

    /**
     * Remove data binding fom html container
     * @param htmlContainer html container
     */
    void unbind(HtmlContainer htmlContainer);
}
