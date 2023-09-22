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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.EnumSet;

public class LocalDateTimeAggregation extends CountAggregation<LocalDateTime> {

    public LocalDateTimeAggregation() {
        super(LocalDateTime.class);
    }

    @Nullable
    @Override
    public LocalDateTime min(Collection<LocalDateTime> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        ZoneId zoneId = ZoneId.systemDefault();

        for (final LocalDateTime item : items) {
            if (item != null) {
                long epochMilli = ZonedDateTime.of(item, zoneId).toInstant().toEpochMilli();
                helper.addItem(((double) epochMilli));
            }
        }
        Double result = helper.min();

        return result != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(result.longValue()), zoneId)
                : null;
    }

    @Nullable
    @Override
    public LocalDateTime max(Collection<LocalDateTime> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        ZoneId zoneId = ZoneId.systemDefault();

        for (final LocalDateTime item : items) {
            if (item != null) {
                long epochMilli = ZonedDateTime.of(item, zoneId).toInstant().toEpochMilli();
                helper.addItem(((double) epochMilli));
            }
        }
        Double result = helper.max();

        return result != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(result.longValue()), zoneId)
                : null;
    }

    @Override
    public EnumSet<AggregationInfo.Type> getSupportedAggregationTypes() {
        return EnumSet.of(AggregationInfo.Type.COUNT, AggregationInfo.Type.MIN, AggregationInfo.Type.MAX);
    }
}
