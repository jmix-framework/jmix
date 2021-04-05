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

import javax.annotation.Nullable;
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

        List<String> joinClauses = logical.getConditions().stream()
                .map(childCondition -> {
                    ConditionGenerationContext childContext = createChildContext(context, childCondition);
                    ConditionGenerator generator = resolver.getConditionGenerator(childContext);
                    return generator.generateJoin(childContext);
                })
                .collect(Collectors.toList());
        return generateJoin(joinClauses);
    }

    protected String generateJoin(List<String> joinClauses) {
        return joinClauses.stream()
                .filter(joinClause -> !Strings.isNullOrEmpty(joinClause))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        LogicalCondition logical = (LogicalCondition) context.getCondition();
        if (logical == null || logical.getConditions().isEmpty()) {
            return "";
        }

        List<String> whereClauses = logical.getConditions().stream()
                .map(childCondition -> {
                    ConditionGenerationContext childContext = createChildContext(context, childCondition);
                    ConditionGenerator generator = resolver.getConditionGenerator(childContext);
                    return generator.generateWhere(childContext);
                })
                .collect(Collectors.toList());

        return generateWhere(whereClauses, logical.getType());
    }

    protected String generateWhere(List<String> whereClauses, LogicalCondition.Type type) {
        StringBuilder sb = new StringBuilder();

        String op = type == LogicalCondition.Type.AND ? " and " : " or ";

        String where = whereClauses.stream()
                .filter(whereClause -> !Strings.isNullOrEmpty(whereClause))
                .collect(Collectors.joining(op));

        if (!Strings.isNullOrEmpty(where)) {
            sb.append("(")
                    .append(where)
                    .append(")");
        }

        return sb.toString();
    }

    @Override
    public ConditionJpqlClause generateJoinAndWhere(ConditionGenerationContext context) {
        LogicalCondition logical = (LogicalCondition) context.getCondition();
        if (logical == null || logical.getConditions().isEmpty()) {
            return new ConditionJpqlClause("", "");
        }

        List<ConditionJpqlClause> clauses = logical.getConditions().stream()
                .map(childCondition -> {
                    ConditionGenerationContext childContext = createChildContext(context, childCondition);
                    ConditionGenerator generator = resolver.getConditionGenerator(childContext);
                    return generator.generateJoinAndWhere(childContext);
                })
                .collect(Collectors.toList());

        List<String> joinClauses = clauses.stream()
                .map(ConditionJpqlClause::getJoin)
                .collect(Collectors.toList());
        String join = generateJoin(joinClauses);

        List<String> whereClauses = clauses.stream()
                .map(ConditionJpqlClause::getWhere)
                .collect(Collectors.toList());
        String where = generateWhere(whereClauses, logical.getType());

        return new ConditionJpqlClause(join, where);
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue) {
        return null;
    }

    protected ConditionGenerationContext createChildContext(ConditionGenerationContext context, Condition condition) {
        ConditionGenerationContext childContext = new ConditionGenerationContext(condition);
        childContext.copy(context);
        return childContext;
    }
}
