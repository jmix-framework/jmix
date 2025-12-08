/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowuidata.dateinterval;

import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.impl.jpql.generator.InIntervalParametersResolver;
import io.jmix.flowuidata.dateinterval.model.CustomDateInterval;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("flowui_InIntervalParametersResolver")
public class InIntervalParametersResolverImpl implements InIntervalParametersResolver {

    @Override
    public List<Pair<String, Object>> resolveParameters(PropertyCondition condition) {
        if (condition.getParameterValue() instanceof CustomDateInterval customDateInterval) {
            return List.of(
                    new Pair<>(customDateInterval.getStartParameterName(), customDateInterval.getStart()),
                    new Pair<>(customDateInterval.getEndParameterName(), customDateInterval.getEnd())
            );
        }

        return Collections.emptyList();
    }
}
