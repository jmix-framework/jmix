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
import io.jmix.core.QueryUtils;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("data_PropertyConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class PropertyConditionGenerator implements ConditionGenerator {

    @Override
    public boolean supports(ConditionGenerationContext context) {
        return context.getCondition() instanceof PropertyCondition;
    }

    @Override
    public String generateJoin(ConditionGenerationContext context) {
        return "";
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        if (propertyCondition == null) {
            return "";
        }

        String entityAlias = context.getEntityAlias();
        String property = propertyCondition.getProperty();

        return generateWhere(propertyCondition, entityAlias, property);
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue) {
        PropertyCondition propertyCondition = (PropertyCondition) condition;
        if (propertyCondition == null || parameterValue == null) {
            return null;
        }

        if (parameterValue instanceof String) {
            switch (propertyCondition.getOperation()) {
                case PropertyCondition.Operation.CONTAINS:
                case PropertyCondition.Operation.NOT_CONTAINS:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + parameterValue + "%";
                case PropertyCondition.Operation.STARTS_WITH:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + parameterValue + "%";
                case PropertyCondition.Operation.ENDS_WITH:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + parameterValue;
            }
        }
        return parameterValue;
    }

    protected String generateWhere(PropertyCondition propertyCondition, String entityAlias, String property) {
        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            return String.format("%s.%s %s",
                    entityAlias,
                    property,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition));
        } else if (PropertyConditionUtils.isInIntervalOperation(propertyCondition)) {
            return PropertyConditionUtils.getJpqlOperation(propertyCondition);
        } else {
            return String.format("%s.%s %s :%s",
                    entityAlias,
                    property,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition),
                    propertyCondition.getParameterName());
        }
    }
}
