/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.JmixOrder;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("data_JpqlConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class JpqlConditionGenerator implements ConditionGenerator {

    @Override
    public boolean supports(ConditionGenerationContext context) {
        return context.getCondition() instanceof JpqlCondition;
    }

    @Override
    public String generateJoin(ConditionGenerationContext context) {
        JpqlCondition jpqlCondition = (JpqlCondition) context.getCondition();
        if (jpqlCondition == null) {
            return "";
        }

        String join = jpqlCondition.getJoin();
        return join != null ? join : "";
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        JpqlCondition jpqlCondition = (JpqlCondition) context.getCondition();

        return jpqlCondition != null
                ? jpqlCondition.getWhere()
                : "";
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue) {
        JpqlCondition jpqlCondition = (JpqlCondition) condition;
        if (jpqlCondition == null || parameterValue == null) {
            return null;
        }

        if (parameterValue instanceof String) {
            return "(?i)%" + parameterValue + "%";
        }
        return parameterValue;
    }
}
