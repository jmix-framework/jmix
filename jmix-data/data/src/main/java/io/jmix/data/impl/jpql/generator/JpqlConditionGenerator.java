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
import io.jmix.core.QueryUtils;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("data_JpqlConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class JpqlConditionGenerator implements ConditionGenerator<JpqlCondition> {

    public static final Pattern LIKE_PATTERN = Pattern.compile(QueryUtils.LIKE_REGEXP, Pattern.CASE_INSENSITIVE);

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

    @Override
    public Map<String, Object> processParameters(Map<String, Object> parameters,
                                                 Map<String, Object> queryParameters,
                                                 JpqlCondition condition,
                                                 @Nullable String entityName) {
        for (Map.Entry<String, Object> parameter : condition.getParameterValuesMap().entrySet()) {
            // JpqlCondition may take a value from queryParameters collection or from the
            // JpqlCondition.parameterValuesMap attribute. queryParameters value has higher priority.
            Object parameterValue;
            String parameterName = parameter.getKey();
            if (!queryParameters.containsKey(parameterName) || queryParameters.get(parameterName) == null) {
                // Modify the query parameter value (e.g. wrap value from JpqlFilter for "contains"
                // jpql operation)
                parameterValue = generateParameterValue(condition, parameter.getValue(), entityName);
            } else {
                // In other cases, it is assumed that the value has already been modified
                // (e.g. wrapped value from DataLoadCoordinator)
                parameterValue = queryParameters.get(parameter.getKey());
            }

            parameters.put(parameter.getKey(), parameterValue);
        }

        return parameters;
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue,
                                         @Nullable String entityName) {
        JpqlCondition jpqlCondition = (JpqlCondition) condition;
        if (jpqlCondition == null || parameterValue == null) {
            return null;
        }

        if (parameterValue instanceof String) {
            String where = jpqlCondition.getWhere();
            Matcher matcher = LIKE_PATTERN.matcher(where);
            if (matcher.find()) {
                String parameterName = matcher.group(2);
                if (Objects.equals(jpqlCondition.getParameterValuesMap().get(parameterName), parameterValue)) {
                    boolean escape = !Strings.isNullOrEmpty(matcher.group(3));
                    if (escape) {
                        String escapeChar = matcher.group(4);
                        if (!Strings.isNullOrEmpty(escapeChar)) {
                            parameterValue = QueryUtils.escapeForLike((String) parameterValue, escapeChar);
                        }
                    }

                    return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + parameterValue + "%";
                }
            }
        }

        return parameterValue;
    }
}
