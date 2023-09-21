/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.data.aggregation;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.grid.DataGrid;

import java.util.Collection;

/**
 * Custom aggregation strategy for {@link DataGrid} component.
 *
 * @param <T> type of aggregation result
 * @param <V> type of property values
 *            or type of data container items if {@link AggregationInfo.Type#CUSTOM} and
 *            {@link MetaPropertyPath} of aggregation info is null
 */
public interface AggregationStrategy<V, T> {

    /**
     * Performs aggregation of <code>propertyValues</code>.
     *
     * @param propertyValues values of the associated {@link MetaPropertyPath} or data container items if
     *                       {@link MetaPropertyPath} of aggregation info is null
     * @return aggregation result
     */
    T aggregate(Collection<V> propertyValues);

    /**
     * @return type of result value
     */
    Class<T> getResultClass();
}
