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

import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modifies JPQL query parameters according to the tree of conditions.
 */
@Component("data_ParameterJpqlGenerator")
public class ParameterJpqlGenerator {

    protected ConditionGeneratorResolver resolver;

    @Autowired
    public ParameterJpqlGenerator(ConditionGeneratorResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Returns parameters for JPQL query modified according to the given tree of conditions.
     *
     * @param parameters      result parameters
     * @param queryParameters query parameters
     * @param actualized      an actualized condition
     * @return modified parameters
     */
    public Map<String, Object> processParameters(Map<String, Object> parameters, Map<String, Object> queryParameters,
                                                 Condition actualized, @Nullable String entityName) {
        List<PropertyCondition> propertyConditions = collectNestedPropertyConditions(actualized);
        for (PropertyCondition propertyCondition : propertyConditions) {
            String parameterName = propertyCondition.getParameterName();
            if (PropertyConditionUtils.isUnaryOperation(propertyCondition)
                    || PropertyConditionUtils.isInIntervalOperation(propertyCondition)) {
                //remove query parameter for unary operations (e.g. IS_NULL) and "in interval" operations
                parameters.remove(parameterName);
            } else {
                //PropertyCondition may take a value from queryParameters collection or from the
                //PropertyCondition.parameterValue attribute. queryParameters has higher priority.
                Object parameterValue;
                if (!queryParameters.containsKey(parameterName) || queryParameters.get(parameterName) == null) {
                    parameterValue = generateParameterValue(propertyCondition, propertyCondition.getParameterValue(), entityName);
                } else {
                    //modify the query parameter value (e.g. wrap value for "contains" jpql operation)
                    Object queryParameterValue = queryParameters.get(parameterName);
                    parameterValue = generateParameterValue(propertyCondition, queryParameterValue, entityName);
                }
                parameters.put(parameterName, parameterValue);
            }
        }

        List<JpqlCondition> jpqlConditions = collectNestedJpqlConditions(actualized);
        for (JpqlCondition jpqlCondition : jpqlConditions) {
            for (Map.Entry<String, Object> parameter : jpqlCondition.getParameterValuesMap().entrySet()) {
                // JpqlCondition may take a value from queryParameters collection or from the
                // JpqlCondition.parameterValuesMap attribute. queryParameters value has higher priority.
                Object parameterValue;
                String parameterName = parameter.getKey();
                if (!queryParameters.containsKey(parameterName) || queryParameters.get(parameterName) == null) {
                    // Modify the query parameter value (e.g. wrap value from JpqlFilter for "contains"
                    // jpql operation)
                    parameterValue = generateParameterValue(jpqlCondition, parameter.getValue(), entityName);
                } else {
                    // In other cases, it is assumed that the value has already been modified
                    // (e.g. wrapped value from DataLoadCoordinator)
                    parameterValue = queryParameters.get(parameter.getKey());
                }
                parameters.put(parameter.getKey(), parameterValue);
            }
        }

        return parameters;
    }

    protected List<PropertyCondition> collectNestedPropertyConditions(Condition rootCondition) {
        List<PropertyCondition> propertyConditions = new ArrayList<>();
        if (rootCondition instanceof LogicalCondition) {
            ((LogicalCondition) rootCondition).getConditions().forEach(c ->
                    propertyConditions.addAll(collectNestedPropertyConditions(c)));
        } else if (rootCondition instanceof PropertyCondition) {
            propertyConditions.add((PropertyCondition) rootCondition);
        }
        return propertyConditions;
    }

    protected List<JpqlCondition> collectNestedJpqlConditions(Condition rootCondition) {
        List<JpqlCondition> jpqlConditions = new ArrayList<>();
        if (rootCondition instanceof LogicalCondition) {
            ((LogicalCondition) rootCondition).getConditions().forEach(c ->
                    jpqlConditions.addAll(collectNestedJpqlConditions(c)));
        } else if (rootCondition instanceof JpqlCondition) {
            jpqlConditions.add((JpqlCondition) rootCondition);
        }
        return jpqlConditions;
    }

    @Nullable
    protected Object generateParameterValue(Condition condition, @Nullable Object parameterValue, @Nullable String entityName) {
        ConditionGenerator conditionGenerator = resolver.getConditionGenerator(new ConditionGenerationContext(condition));
        return conditionGenerator.generateParameterValue(condition, parameterValue, entityName);
    }
}
