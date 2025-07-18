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

import io.jmix.flowui.data.aggregation.Aggregation;
import org.springframework.lang.Nullable;

/**
 * Implementation of {@link Aggregation} intended for aggregating {@link Long} values.
 */
public class LongAggregation extends AbstractNumberAggregation<Long> {

    public LongAggregation() {
        super(Long.class);
    }

    @Nullable
    @Override
    protected Long convert(@Nullable Double result) {
        return result != null
                ? result.longValue()
                : null;
    }
}
