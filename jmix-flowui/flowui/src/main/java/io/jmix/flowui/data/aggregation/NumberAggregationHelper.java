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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for aggregating numerical values. This class provides methods
 * for adding numbers to a collection and performing basic aggregation
 * operations such as sum, average, minimum, and maximum.
 */
public class NumberAggregationHelper {

    protected final List<Double> items = new ArrayList<>();

    public NumberAggregationHelper() {
    }

    /**
     * Adds a new numerical item to the collection for aggregation operations.
     *
     * @param newItem the numerical value to be added to the collection
     */
    public void addItem(Double newItem) {
        items.add(newItem);
    }

    /**
     * Calculates the total sum of all numerical values in the collection.
     * Null values in the collection are ignored during the calculation.
     *
     * @return the sum of all non-null numerical values in the collection
     */
    public Double sum() {
        double sum = 0d;
        for (final Double item : items) {
            if (item != null) {
                sum += item;
            }
        }

        return sum;
    }

    /**
     * Computes the average value of all numerical items in the collection.
     * If the collection is empty, returns {@code null}.
     *
     * @return the average value of the items in the collection, or {@code null} if the collection is empty
     */
    @Nullable
    public Double avg() {
        return items.isEmpty()
                ? null
                : sum() / items.size();
    }

    /**
     * Determines the minimum value from the collection of numerical items.
     * If the collection is empty, returns {@code null}.
     *
     * @return the minimum value among the items in the collection, or {@code null} if the collection is empty
     */
    @Nullable
    public Double min() {
        return items.isEmpty()
                ? null
                : NumberUtils.min(ArrayUtils.toPrimitive(items.toArray(new Double[0])));
    }

    /**
     * Determines the maximum value among the numerical items in the collection.
     * If the collection is empty, returns {@code null}.
     *
     * @return the maximum value among the numerical items, or {@code null} if the collection is empty
     */
    @Nullable
    public Double max() {
        return items.isEmpty()
                ? null
                : NumberUtils.max(ArrayUtils.toPrimitive(items.toArray(new Double[0])));
    }
}
