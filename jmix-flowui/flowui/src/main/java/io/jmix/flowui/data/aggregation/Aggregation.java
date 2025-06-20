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

import io.jmix.flowui.component.AggregationInfo;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Defines methods for performing aggregation operations on a collection of items.
 *
 * @param <T> the type of the items to be aggregated
 */
public interface Aggregation<T> {

    /**
     * Calculates the sum of the provided collection of items.
     *
     * @param items the collection of items to be summed
     * @return the sum of the items, or {@code null} if the collection is empty
     */
    @Nullable
    T sum(Collection<T> items);

    /**
     * Computes the average value of the elements in the provided collection.
     *
     * @param items the collection of elements to be averaged
     * @return the average value of the elements, or {@code null} if the collection is empty
     */
    @Nullable
    T avg(Collection<T> items);

    /**
     * Determines the minimum value in the provided collection of items.
     *
     * @param items the collection of items to evaluate for the minimum value
     * @return the minimum value from the collection, or {@code null} if the collection is empty
     */
    @Nullable
    T min(Collection<T> items);

    /**
     * Determines the maximum value in the provided collection of items.
     *
     * @param items the collection of items to evaluate for the maximum value
     * @return the maximum value from the collection, or {@code null} if the collection is empty
     */
    @Nullable
    T max(Collection<T> items);

    /**
     * Counts the number of elements in the provided collection.
     *
     * @param items the collection of items to be counted
     * @return the total number of elements in the collection
     */
    int count(Collection<T> items);

    /**
     * Returns the class type of the result produced by this aggregation.
     *
     * @return the class representing the type of the result
     */
    Class<T> getResultClass();

    /**
     * Returns the set of aggregation types that are supported by this class.
     *
     * @return a set of supported aggregation types
     */
    EnumSet<AggregationInfo.Type> getSupportedAggregationTypes();
}
