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

package io.jmix.flowui.component.genericfilter.registration;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.view.ViewRegistry;

/**
 * Builds registration object that is used for adding or overriding UI filter components in the framework
 * <p>
 * For instance:
 * <pre>
 * &#64;Configuration
 * public class FilterComponentConfiguration {
 *
 *      &#64;Bean
 *      public FilterComponentRegistration registerPropertyFilterComponent() {
 *          return FilterComponentRegistrationBuilder.create(PropertyFilter.class,
 *          PropertyFilterCondition.class,
 *          PropertyFilterConverter.class)
 *          .build();
 *      }
 * }
 * </pre>
 *
 * @param <C> UI filter component type
 * @param <M> model type
 * @see FilterComponentRegistration
 * @see FilterComponents
 */
public class FilterComponentRegistrationBuilder<C extends FilterComponent, M extends FilterCondition> {

    protected Class<C> componentClass;
    protected Class<M> modelClass;
    protected Class<? extends FilterConverter<C, M>> converterClass;
    protected String detailViewId;

    /**
     * @param componentClass a UI filter component class
     * @param modelClass     a model class
     * @param converterClass a converter class
     */
    public FilterComponentRegistrationBuilder(Class<C> componentClass,
                                              Class<M> modelClass,
                                              Class<? extends FilterConverter<C, M>> converterClass) {
        this.componentClass = componentClass;
        this.modelClass = modelClass;
        this.converterClass = converterClass;
    }

    /**
     * @param componentClass a UI filter component class
     * @param modelClass     a model class
     * @param converterClass a converter class
     * @param <C>            UI filter component type
     * @param <M>            model type
     * @return builder instance
     */
    public static <C extends FilterComponent, M extends FilterCondition> FilterComponentRegistrationBuilder<C, M> create(
            Class<C> componentClass,
            Class<M> modelClass,
            Class<? extends FilterConverter<C, M>> converterClass) {
        return new FilterComponentRegistrationBuilder<>(componentClass, modelClass, converterClass);
    }

    /**
     * Sets an id of the model detail view. If no identifier is specified then the identifier will be equal
     * to the model name obtained from the {@link JmixEntity}, with {@link ViewRegistry#DETAIL_VIEW_SUFFIX}.
     *
     * @param detailViewId id of the model detail view
     * @return builder instance
     */
    public FilterComponentRegistrationBuilder<C, M> withDetailViewId(String detailViewId) {
        this.detailViewId = detailViewId;
        return this;
    }

    /**
     * @return instance of registration object
     */
    public FilterComponentRegistration build() {
        return new FilterComponentRegistrationImpl(componentClass, modelClass, converterClass, detailViewId);
    }
}
