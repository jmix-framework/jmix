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

package com.haulmont.cuba.core.global.filter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class GroovyGenerator {
    protected final EnumSet METHOD_OPS = EnumSet.of(Op.CONTAINS, Op.STARTS_WITH, Op.ENDS_WITH, Op.DOES_NOT_CONTAIN);
    protected final EnumSet NEGATIVE_OPS = EnumSet.of(Op.DOES_NOT_CONTAIN, Op.NOT_IN);

    public String generateGroovy(Condition condition) {
        if (condition instanceof LogicalCondition) {
            LogicalOp operation = ((LogicalCondition) condition).getOperation();
            List<Condition> conditions = condition.getConditions();
            if (conditions.isEmpty()) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();

                if (conditions.size() > 1)
                    sb.append("(");

                for (Iterator<Condition> it = conditions.iterator(); it.hasNext(); ) {
                    Condition child = it.next();
                    sb.append(generateGroovy(child));
                    if (it.hasNext())
                        sb.append(" ").append(operation.forGroovy()).append(" ");
                }

                if (conditions.size() > 1)
                    sb.append(")");

                return sb.toString();
            }
        } else if (condition instanceof Clause) {
            ParameterInfo parameterInfo = condition.getCompiledParameters().iterator().next();
            Class javaClass = parameterInfo.getJavaClass();
            if (javaClass == null) {
                throw new UnsupportedOperationException();
            }

            Op operator = ((Clause) condition).getOperator();
            String groovyOperator = operator.forGroovy();
            String parameterInfoValue = parameterInfo.getValue();

            if (parameterInfoValue != null && (operator == Op.IN || operator == Op.NOT_IN)) {
                if (parameterInfoValue.startsWith("[") || parameterInfoValue.startsWith("(")) {
                    parameterInfoValue = parameterInfoValue.replaceAll("[\\[\\]()]", "");
                }

                String[] splittedValues = parameterInfoValue.split(",");
                String convertedValue = Arrays.stream(splittedValues)
                        .map(String::trim)
                        .map(v -> valueToString(javaClass, v, Op.EQUAL))
                        .collect(Collectors.joining(", ", "[", "]"));
                parameterInfoValue = convertedValue;
            }

            String valueToString = valueToString(javaClass, parameterInfoValue, operator);

            String resultingClause;
            if (operator.isUnary()) {
                resultingClause = format("{E}.%s %s", condition.getName(), groovyOperator);
            } else if (METHOD_OPS.contains(operator)) {
                resultingClause = format("{E}.%s.%s(%s)", condition.getName(), groovyOperator, valueToString);
            } else {
                resultingClause = format("{E}.%s %s %s", condition.getName(), groovyOperator, valueToString);
            }

            if (NEGATIVE_OPS.contains(operator)) {
                resultingClause = "!(" + resultingClause + ")";
            }

            return resultingClause;
        }

        throw new UnsupportedOperationException();
    }

    protected String valueToString(Class javaClass, @Nullable String value, Op operator) {
        if (value == null) {
            return "null";
        } else if (Number.class.isAssignableFrom(javaClass)
                || Boolean.class.isAssignableFrom(javaClass)
                || operator == Op.IN || operator == Op.NOT_IN) {
            return value;
        } else if (String.class.isAssignableFrom(javaClass)) {
            return "'" + value + "'";
        } else {
            //the following method should match com.haulmont.cuba.core.sys.SecurityImpl.parseValue()
            return format("parse(%s.class, '%s')", javaClass.getCanonicalName(), value);
        }
    }
}
