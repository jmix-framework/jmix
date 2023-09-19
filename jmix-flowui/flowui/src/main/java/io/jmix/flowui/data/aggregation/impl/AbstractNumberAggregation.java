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

package io.jmix.flowui.data.aggregation.impl;

import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.data.aggregation.NumberAggregationHelper;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EnumSet;

public abstract class AbstractNumberAggregation<T extends Number> extends AbstractAggregation<T> {

    protected AbstractNumberAggregation(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T sum(Collection<T> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final T item : items) {
            if (item != null) {
                helper.addItem(item.doubleValue());
            }
        }

        return convert(helper.sum());
    }

    @Override
    public T avg(Collection<T> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final T item : items) {
            if (item != null) {
                helper.addItem(item.doubleValue());
            }
        }

        return convert(helper.avg());
    }

    @Nullable
    @Override
    public T min(Collection<T> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final T item : items) {
            if (item != null) {
                helper.addItem(item.doubleValue());
            }
        }

        return convert(helper.min());
    }

    @Override
    public T max(Collection<T> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final T item : items) {
            if (item != null) {
                helper.addItem(item.doubleValue());
            }
        }

        return convert(helper.max());
    }

    @Override
    public EnumSet<AggregationInfo.Type> getSupportedAggregationTypes() {
        return EnumSet.allOf(AggregationInfo.Type.class);
    }

    @Nullable
    protected abstract T convert(@Nullable Double result);
}
