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
import java.util.Date;
import java.util.EnumSet;

public class DateAggregation extends BasicAggregation<Date> {

    public DateAggregation() {
        super(Date.class);
    }

    @Nullable
    @Override
    public Date min(Collection<Date> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final Date item : items) {
            if (item != null) {
                helper.addItem(((double) item.getTime()));
            }
        }
        Double result = helper.min();

        return result != null
                ? new Date(result.longValue())
                : null;
    }

    @Nullable
    @Override
    public Date max(Collection<Date> items) {
        NumberAggregationHelper helper = new NumberAggregationHelper();
        for (final Date item : items) {
            if (item != null) {
                helper.addItem(((double) item.getTime()));
            }
        }
        Double result = helper.max();

        return result != null
                ? new Date(result.longValue())
                : null;
    }

    @Override
    public EnumSet<AggregationInfo.Type> getSupportedAggregationTypes() {
        return EnumSet.of(AggregationInfo.Type.COUNT, AggregationInfo.Type.MIN, AggregationInfo.Type.MAX);
    }
}
