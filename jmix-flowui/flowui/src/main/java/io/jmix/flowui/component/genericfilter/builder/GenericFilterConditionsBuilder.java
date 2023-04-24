/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.genericfilter.builder;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.entity.filter.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Internal
@Component("flowui_GenericFilterConditionsBuilder")
public class GenericFilterConditionsBuilder {

    protected List<ConditionBuilder> conditionBuilders;

    @Autowired(required = false)
    public void setConditionBuilders(List<ConditionBuilder> conditionBuilders) {
        this.conditionBuilders = conditionBuilders;
    }

    public List<FilterCondition> buildConditions(GenericFilter filter) {
        List<FilterCondition> conditions = new ArrayList<>();

        if (conditionBuilders != null) {
            for (ConditionBuilder conditionBuilder : conditionBuilders) {
                conditions.addAll(conditionBuilder.build(filter));
            }
        }

        return conditions;
    }
}
