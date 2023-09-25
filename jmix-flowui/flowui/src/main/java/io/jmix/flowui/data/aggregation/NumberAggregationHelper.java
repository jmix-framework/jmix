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

public class NumberAggregationHelper {

    protected final List<Double> items = new ArrayList<>();

    public NumberAggregationHelper() {
    }

    public void addItem(Double newItem) {
        items.add(newItem);
    }

    public Double sum() {
        double sum = 0d;
        for (final Double item : items) {
            if (item != null) {
                sum += item;
            }
        }

        return sum;
    }

    @Nullable
    public Double avg() {
        return items.isEmpty()
                ? null
                : sum() / items.size();
    }

    @Nullable
    public Double min() {
        return items.isEmpty()
                ? null
                : NumberUtils.min(ArrayUtils.toPrimitive(items.toArray(new Double[0])));
    }

    @Nullable
    public Double max() {
        return items.isEmpty()
                ? null
                : NumberUtils.max(ArrayUtils.toPrimitive(items.toArray(new Double[0])));
    }
}
