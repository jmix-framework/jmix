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

package io.jmix.ui.component.filter.converter;

import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.filter.registration.FilterComponentRegistration;
import io.jmix.ui.component.filter.registration.FilterComponentRegistrationBuilder;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;

/**
 * Converts between a UI filter component and model classes.
 *
 * @param <C> UI filter component type
 * @param <M> model type
 * @see FilterComponentRegistration
 * @see FilterComponentRegistrationBuilder
 * @see FilterComponents
 */
public interface FilterConverter<C extends FilterComponent, M extends FilterCondition> {

    /**
     * Returns an instance of the UI filter component whose state was retrieved from the model.
     *
     * @param model a model instance
     * @return an instance of the UI filter component
     */
    C convertToComponent(M model);

    /**
     * Returns an instance of model whose state was retrieved from the UI filter component.
     *
     * @param component a filter component instance
     * @return a model instance
     */
    M convertToModel(C component);
}
