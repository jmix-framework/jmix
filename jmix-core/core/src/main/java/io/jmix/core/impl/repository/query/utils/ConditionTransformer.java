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

package io.jmix.core.impl.repository.query.utils;

import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import jakarta.annotation.Nullable;
import java.util.List;

/**
 * Utility class to build {@link Condition} by Spring Data repositories {@link PartTree}
 */
public class ConditionTransformer {

    /**
     * Creates {@link Condition} tree for data repository method by it's spring {@link PartTree}.
     * Also fills specified {@code List} with parameter names required for query
     *
     * @param parts      PartTree to be converted to Condition
     * @param parameters collection to put parameter names for conditions
     */
    @Nullable
    public static Condition fromPartTree(PartTree parts, List<String> parameters) {
        Condition condition = null;

        if (parts.iterator().hasNext()) {
            LogicalCondition orCondition = new LogicalCondition(LogicalCondition.Type.OR);
            for (PartTree.OrPart orPart : parts) {
                LogicalCondition andCondition = new LogicalCondition(LogicalCondition.Type.AND);

                for (Part part : orPart) {
                    andCondition.add(handleQueryPart(part, parameters));
                }

                if (andCondition.getConditions().size() == 1) {
                    orCondition.add(andCondition.getConditions().get(0));
                } else if (andCondition.getConditions().size() > 0) {
                    orCondition.add(andCondition);
                }

            }

            if (orCondition.getConditions().size() == 1) {
                condition = orCondition.getConditions().get(0);
            } else if (orCondition.getConditions().size() > 0) {
                condition = orCondition;
            }
        }

        return condition;
    }

    private static Condition handleQueryPart(Part part, List<String> parameters) {
        Condition condition;
        String property = part.getProperty().toString();
        property = property.substring(property.indexOf('.') + 1);
        String paramName = part.getProperty().getLeafProperty().getSegment();

        if (parameters.contains(paramName)) {
            int i = 2;
            String corrected = paramName + "_2";
            while (parameters.contains(corrected)) {
                i++;
                corrected = paramName + "_" + i;
            }
            paramName = corrected;
        }

        switch (part.getType()) {
            case SIMPLE_PROPERTY:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.EQUAL, paramName);
                break;
            case NEGATING_SIMPLE_PROPERTY:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.NOT_EQUAL, paramName);
                break;
            case AFTER:
            case GREATER_THAN:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.GREATER, paramName);
                break;
            case GREATER_THAN_EQUAL:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.GREATER_OR_EQUAL, paramName);
                break;
            case BEFORE:
            case LESS_THAN:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.LESS, paramName);
                break;
            case LESS_THAN_EQUAL:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.LESS_OR_EQUAL, paramName);
                break;
            case IS_NOT_NULL:
                condition = PropertyCondition.isSet(property, true);
                break;
            case IS_NULL:
                condition = PropertyCondition.isSet(property, false);
                break;
            case FALSE:
                condition = PropertyCondition.equal(property, false);
                break;
            case TRUE:
                condition = PropertyCondition.equal(property, true);
                break;
            case CONTAINING:
            case LIKE:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.CONTAINS, paramName);
                break;
            case NOT_CONTAINING:
            case NOT_LIKE:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.NOT_CONTAINS, paramName);
                break;
            case STARTING_WITH:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.STARTS_WITH, paramName);
                break;
            case ENDING_WITH:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.ENDS_WITH, paramName);
                break;
            case IN:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.IN_LIST, paramName);
                break;
            case NOT_IN:
                parameters.add(paramName);
                condition = PropertyCondition.createWithParameterName(property, PropertyCondition.Operation.NOT_IN_LIST, paramName);
                break;
            case REGEX:
            case BETWEEN:
            case NEAR:
            case EXISTS:
            default:
                throw new UnsupportedOperationException(part.getType() + " is not supported!");
        }

        return condition;
    }


}
