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
import io.jmix.flowui.data.aggregation.Aggregation;
import io.jmix.flowui.data.aggregation.NumberAggregationHelper;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Implementation of {@link Aggregation} intended for aggregating {@link LocalDate} values.
 */
public class LocalDateAggregation extends CountAggregation<LocalDate> {

    public LocalDateAggregation() {
        super(LocalDate.class);
    }

    @Nullable
    @Override
    public LocalDate min(Collection<LocalDate> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final LocalDate item : items) {
            if (item != null) {
                helper.addItem(((double) item.getLong(ChronoField.EPOCH_DAY)));
            }
        }
        Double result = helper.min();

        return result != null
                ? LocalDate.ofEpochDay(result.longValue())
                : null;
    }

    @Nullable
    @Override
    public LocalDate max(Collection<LocalDate> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final LocalDate item : items) {
            if (item != null) {
                helper.addItem(((double) item.getLong(ChronoField.EPOCH_DAY)));
            }
        }
        Double result = helper.max();

        return result != null
                ? LocalDate.ofEpochDay(result.longValue())
                : null;
    }

    @Override
    public EnumSet<AggregationInfo.Type> getSupportedAggregationTypes() {
        return EnumSet.of(AggregationInfo.Type.COUNT, AggregationInfo.Type.MIN, AggregationInfo.Type.MAX);
    }
}
