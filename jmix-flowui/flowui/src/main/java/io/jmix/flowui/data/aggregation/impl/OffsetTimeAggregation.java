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

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.EnumSet;

public class OffsetTimeAggregation extends CountAggregation<OffsetTime> {

    protected ZoneOffset currentOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());

    public OffsetTimeAggregation() {
        super(OffsetTime.class);
    }

    @Nullable
    @Override
    public OffsetTime min(Collection<OffsetTime> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final OffsetTime item : items) {
            if (item != null) {
                helper.addItem(((double) item.getLong(ChronoField.NANO_OF_DAY)));
            }
        }
        Double result = helper.min();

        return result != null
                ? LocalTime.ofNanoOfDay(result.longValue()).atOffset(currentOffset)
                : null;
    }

    @Nullable
    @Override
    public OffsetTime max(Collection<OffsetTime> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final OffsetTime item : items) {
            if (item != null) {
                helper.addItem(((double) item.getLong(ChronoField.NANO_OF_DAY)));
            }
        }
        Double result = helper.max();

        return result != null
                ? LocalTime.ofNanoOfDay(result.longValue()).atOffset(currentOffset)
                : null;
    }

    @Override
    public EnumSet<AggregationInfo.Type> getSupportedAggregationTypes() {
        return EnumSet.of(AggregationInfo.Type.COUNT, AggregationInfo.Type.MIN, AggregationInfo.Type.MAX);
    }
}
