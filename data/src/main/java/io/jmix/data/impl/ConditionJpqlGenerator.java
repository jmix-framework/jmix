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

package io.jmix.data.impl;

import com.google.common.base.Strings;
import io.jmix.core.querycondition.*;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modifies JPQL query according to the tree of conditions. See {@link #processQuery(String, List, Condition)} method.
 */
@Component("data_ConditionJpqlGenerator")
public class ConditionJpqlGenerator {

    @Autowired
    private QueryTransformerFactory queryTransformerFactory;

    /**
     * Returns a JPQL query modified according to the given tree of conditions.
     *
     * @param query           JPQL query
     * @param valueProperties keys of key-value pairs. If null, the query does not contain key-value pairs.
     * @param condition       root condition. If null, the query is returned as is.
     */
    public String processQuery(String query, @Nullable List<String> valueProperties, @Nullable Condition condition) {
        if (condition == null) {
            return query;
        }
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        QueryParser parser = queryTransformerFactory.parser(query);
        String entityAlias = parser.getEntityAlias();

        List<String> selectedExpressions = null;
        if (valueProperties != null) {
            selectedExpressions = parser.getSelectedExpressionsList();
        }

        String joins = generateJoins(condition);
        String where = generateWhere(condition, entityAlias, valueProperties, selectedExpressions);
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
            String join = ((JpqlCondition) condition).getJoin();
            return join != null ? join : "";
        } else if (condition instanceof PropertyCondition) {
            return "";
        }
        throw new UnsupportedOperationException("Condition is not supported: " + condition);
    }

    protected String generateWhere(Condition condition,
                                   String entityAlias,
                                   @Nullable List<String> valueProperties,
                                   @Nullable List<String> selectedExpressions) {
        if (condition instanceof LogicalCondition) {
            LogicalCondition logical = (LogicalCondition) condition;
            List<Condition> conditions = logical.getConditions();
            if (conditions.isEmpty())
                return "";
            else {
                StringBuilder sb = new StringBuilder();

                String op = logical.getType() == LogicalCondition.Type.AND ? " and " : " or ";

                String where = conditions.stream().map(c ->
                        this.generateWhere(c, entityAlias, valueProperties, selectedExpressions))
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
            return ((JpqlCondition) condition).getWhere();
        } else if (condition instanceof PropertyCondition) {
            PropertyCondition propertyCondition = (PropertyCondition) condition;
            if (valueProperties != null && selectedExpressions != null) {
                return generateKeyValueWhere(propertyCondition, valueProperties, selectedExpressions);
            } else {
                return generateWhere(propertyCondition, entityAlias, propertyCondition.getProperty());
            }
        }
        throw new UnsupportedOperationException("Condition is not supported: " + condition);
    }

    protected String generateWhere(PropertyCondition propertyCondition, String entityAlias, String property) {
        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            return String.format("%s.%s %s",
                    entityAlias,
                    property,
                    getJpqlOperation(propertyCondition));
        } else {
            return String.format("%s.%s %s :%s",
                    entityAlias,
                    property,
                    getJpqlOperation(propertyCondition),
                    propertyCondition.getParameterName());
        }
    }

    protected String generateKeyValueWhere(PropertyCondition propertyCondition,
                                           List<String> valueProperties,
                                           List<String> selectedExpressions) {
        String entityAlias = propertyCondition.getProperty();
        String property = null;
        if (entityAlias.contains(".")) {
            int indexOfDot = entityAlias.indexOf(".");
            property = entityAlias.substring(indexOfDot + 1);
            entityAlias = entityAlias.substring(0, indexOfDot);
        }

        int index = valueProperties.indexOf(entityAlias);
        if (index >= 0 && index < selectedExpressions.size()) {
            entityAlias = selectedExpressions.get(index);
        }

        if (property != null) {
            return generateWhere(propertyCondition, entityAlias, property);
        } else {
            return generateKeyValueWhere(propertyCondition, entityAlias);
        }
    }

    protected String generateKeyValueWhere(PropertyCondition propertyCondition, String entityAlias) {
        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            return String.format("%s %s",
                    entityAlias,
                    getJpqlOperation(propertyCondition));
        } else {
            return String.format("%s %s :%s",
                    entityAlias,
                    getJpqlOperation(propertyCondition),
                    propertyCondition.getParameterName());
        }
    }

    protected String getJpqlOperation(PropertyCondition condition) {
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
            case PropertyCondition.Operation.IN_LIST:
                return "in";
            case PropertyCondition.Operation.NOT_IN_LIST:
                return "not in";
            case PropertyCondition.Operation.IS_SET:
                return Boolean.TRUE.equals(condition.getParameterValue()) ? "is not null" : "is null";
        }
        throw new RuntimeException("Unknown PropertyCondition operation: " + condition.getOperation());
    }
}
