/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.querycondition;

import com.google.common.base.Strings;
import io.jmix.core.QueryParser;
import io.jmix.core.QueryTransformer;
import io.jmix.core.QueryTransformerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modifies JPQL query according to the tree of conditions. See {@link #processQuery(String, Condition)} method.
 */
@Component("core_ConditionJpqlGenerator")
public class ConditionJpqlGenerator {

    @Autowired
    private QueryTransformerFactory queryTransformerFactory;

    /**
     * Returns a JPQL query modified according to the given tree of conditions.
     * @param query JPQL query
     * @param condition root condition. If null, the query is returned as is.
     */
    public String processQuery(String query, @Nullable Condition condition) {
        if (condition == null) {
            return query;
        }
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        QueryParser parser = queryTransformerFactory.parser(query);
        String entityAlias = parser.getEntityAlias();
        String joins = generateJoins(condition);
        String where = generateWhere(condition, entityAlias);
        if (!Strings.isNullOrEmpty(joins)) {
            transformer.addJoinAndWhere(joins, where);
        } else {
            transformer.addWhere(where);
        }
        return transformer.getResult();
    }

    protected String generateJoins(Condition condition) {
        if (condition instanceof LogicalCondition) {
            LogicalCondition logical = (LogicalCondition) condition;
            List<Condition> conditions = logical.getConditions();
            if (conditions.isEmpty())
                return "";
            else {
                return conditions.stream()
                        .map(this::generateJoins)
                        .collect(Collectors.joining(" "));
            }
        } else if (condition instanceof JpqlCondition) {
            String join = ((JpqlCondition) condition).getValue("join");
            return join != null ? join : "";
        } else if (condition instanceof PropertyCondition) {
            return "";
        }
        throw new UnsupportedOperationException("Condition is not supported: " + condition);
    }

    protected String generateWhere(Condition condition, String entityAlias) {
        if (condition instanceof LogicalCondition) {
            LogicalCondition logical = (LogicalCondition) condition;
            List<Condition> conditions = logical.getConditions();
            if (conditions.isEmpty())
                return "";
            else {
                StringBuilder sb = new StringBuilder();

                String op = logical.getType() == LogicalCondition.Type.AND ? " and " : " or ";

                String where = conditions.stream().map(c -> this.generateWhere(c, entityAlias))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(op));

                if (StringUtils.isNotBlank(where)) {
                    sb.append("(")
                            .append(where)
                            .append(")");
                }

                return sb.toString();
            }
        } else if (condition instanceof JpqlCondition) {
            return ((JpqlCondition) condition).getValue("where");
        } else if (condition instanceof PropertyCondition) {
            PropertyCondition propertyCondition = (PropertyCondition) condition;
            if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
                return String.format("%s.%s %s",
                        entityAlias,
                        propertyCondition.getProperty(),
                        getJpqlOperation(propertyCondition));
            } else {
                return String.format("%s.%s %s :%s",
                        entityAlias,
                        propertyCondition.getProperty(),
                        getJpqlOperation(propertyCondition),
                        propertyCondition.getParameterName());
            }
        }
        throw new UnsupportedOperationException("Condition is not supported: " + condition);
    }

    private String getJpqlOperation(PropertyCondition condition) {
        if (PropertyCondition.Operation.IS_NULL.equals(condition.getOperation())) {
            return Boolean.TRUE.equals(condition.getParameterValue()) ? "is null" : "is not null";
        } else if (PropertyCondition.Operation.IS_NOT_NULL.equals(condition.getOperation())) {
            return Boolean.TRUE.equals(condition.getParameterValue()) ? "is not null" : "is null";
        } else {
            switch (condition.getOperation()) {
                case PropertyCondition.Operation.EQUAL:
                    return "=";
                case PropertyCondition.Operation.NOT_EQUAL:
                    return "<>";
                case PropertyCondition.Operation.GREATER:
                    return ">";
                case PropertyCondition.Operation.GREATER_OR_EQUAL:
                    return ">=";
                case PropertyCondition.Operation.LESS:
                    return "<";
                case PropertyCondition.Operation.LESS_OR_EQUAL:
                    return "<=";
                case PropertyCondition.Operation.CONTAINS:
                case PropertyCondition.Operation.STARTS_WITH:
                case PropertyCondition.Operation.ENDS_WITH:
                    return "like";
                case PropertyCondition.Operation.NOT_CONTAINS:
                    return "not like";
                case PropertyCondition.Operation.IS_NULL:
                    return "is null";
                case PropertyCondition.Operation.IS_NOT_NULL:
                    return "is not null";
            }
        }
        throw new RuntimeException("Unknown PropertyCondition operation: " + condition.getOperation());
    }
}
