/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.DevelopmentException;
import io.jmix.core.impl.repository.query.JmixAbstractQuery;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal helper with query processing logic common for both {@link io.jmix.core.impl.repository.query.JmixCustomLoadQuery}
 * and {@link io.jmix.core.impl.repository.query.JmixScalarQuery}.
 */
public class QueryParameterUtils {

    protected static final String PARAMETER_TEMPLATE = "([:?][a-zA-Z0-9_$]+)";
    protected static final String PARAMETER_PREFIX = "p_";

    /**
     * Binds parameter names in the query with positions of method arguments.
     * Replaces positional parameters with named ones if needed.
     *
     * @param queryMethod Spring query method metadata
     * @param method java method definition
     * @param query to process
     * @param namedParametersBindings map to store binding
     * @return modified query
     */
    public static String replaceQueryParameters(QueryMethod queryMethod,
                                                Method method,
                                                String query,
                                                Map<String, Integer> namedParametersBindings) {
        String resultQuery = query;
        Set<String> parameterNames = new HashSet<>();
        Matcher m = Pattern.compile(PARAMETER_TEMPLATE).matcher(query);
        Boolean positionParametersFound = null;
        while (m.find()) {
            String occurrence = m.group();
            String paramName;
            if (occurrence.startsWith("?")) {
                if (!Boolean.FALSE.equals(positionParametersFound)) {
                    positionParametersFound = true;
                    paramName = occurrence.replace("?", PARAMETER_PREFIX);
                    resultQuery = resultQuery.replace(occurrence, ":" + paramName);
                } else {
                    throw new DevelopmentException(String.format("There are mixed parameter types in query '%s' for %s",
                            query,
                            JmixAbstractQuery.formatMethod(method)));
                }
            } else if (occurrence.startsWith(":")) {
                if (!Boolean.TRUE.equals(positionParametersFound)) {
                    positionParametersFound = false;
                    paramName = occurrence.replace(":", "");
                } else {
                    throw new DevelopmentException(String.format("There are mixed parameter types in query '%s' for %s",
                            query,
                            JmixAbstractQuery.formatMethod(method)));
                }
            } else {
                throw new RuntimeException("Cannot happen");
            }
            parameterNames.add(paramName);
        }
        if (!parameterNames.isEmpty())
            matchQueryParameters(resultQuery, parameterNames, queryMethod.getParameters().getBindableParameters(),
                    namedParametersBindings, positionParametersFound, method);

        return resultQuery;
    }

    protected static void matchQueryParameters(String query,
                                               Set<String> parameterNames,
                                               Parameters<? extends Parameters, ? extends Parameter> bindableParameters,
                                               Map<String, Integer> namedParametersBindings,
                                               boolean fromPositionParameters, Method method) {

        if (fromPositionParameters) {
            for (String parameterName : parameterNames) {
                int position = Integer.parseInt(parameterName.substring(PARAMETER_PREFIX.length())) - 1;
                namedParametersBindings.put(parameterName, bindableParameters.getParameter(position).getIndex());
            }
        } else {
            for (Parameter bindableParameter : bindableParameters.toList()) {
                //noinspection OptionalGetWithoutIsPresent
                String name = bindableParameter.getName().get();//existence checked by spring
                if (parameterNames.contains(name)) {
                    namedParametersBindings.put(name, bindableParameter.getIndex());
                } else {
                    throw new DevelopmentException(String.format("Parameter %s of method %s does not included to query \"%s\"",
                            name,
                            JmixAbstractQuery.formatMethod(method),
                            query
                    ));
                }
            }
        }
    }
}
