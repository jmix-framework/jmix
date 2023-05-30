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

import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Component("data_LogicalConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class LogicalConditionGenerator implements ConditionGenerator {

    @Autowired
    protected ConditionGeneratorResolver resolver;

    @Override
    public boolean supports(ConditionGenerationContext context) {
        return context.getCondition() instanceof LogicalCondition;
    }

    @Override
    public String generateJoin(ConditionGenerationContext context) {
        LogicalCondition logical = (LogicalCondition) context.getCondition();
        if (logical == null || logical.getConditions().isEmpty()) {
            return "";
        }

        return logical.getConditions().stream()
                .map(childCondition -> {
                    ConditionGenerationContext childContext = context.getChildContexts().get(childCondition);
                    ConditionGenerator generator = resolver.getConditionGenerator(childContext);
                    return generator.generateJoin(childContext);
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        LogicalCondition logical = (LogicalCondition) context.getCondition();
        if (logical == null || logical.getConditions().isEmpty()) {
            return "";
        }

        List<Condition> conditions = logical.getConditions();
        StringBuilder sb = new StringBuilder();

        String op = logical.getType() == LogicalCondition.Type.AND ? " and " : " or ";

        String where = conditions.stream()
                .map(childCondition -> {
                    ConditionGenerationContext childContext = context.getChildContexts().get(childCondition);
                    ConditionGenerator generator = resolver.getConditionGenerator(childContext);
                    return generator.generateWhere(childContext);
                })
                .filter(whereClause -> !Strings.isNullOrEmpty(whereClause))
                .collect(Collectors.joining(op));

        if (!Strings.isNullOrEmpty(where)) {
            sb.append("(")
                    .append(where)
                    .append(")");
        }

        return sb.toString();
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue,
                                         @Nullable String entityName) {
        return null;
    }
}
