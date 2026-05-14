/*
 * Copyright 2026 Haulmont.
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

package io.jmix.texttodata.validation.validator;

import io.jmix.core.JmixOrder;
import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.validation.JpqlResultValidator;
import io.jmix.texttodata.validation.JpqlValidationIssue;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("textdt_ParametersValidator")
public class ParametersValidator implements JpqlResultValidator, Ordered {

    protected static final Pattern PARAMETER_PATTERN = Pattern.compile(":([A-Za-z_][A-Za-z0-9_]*)");

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();

        Set<String> jpqlParameters = extractParameterNames(jpql);
        Set<String> dtoParameters = new LinkedHashSet<>();
        for (GeneratedJpqlParameter parameter : result.getParameters()) {
            dtoParameters.add(parameter.getName());
        }

        for (String parameterName : jpqlParameters) {
            if (!dtoParameters.contains(parameterName)) {
                issues.add(new JpqlValidationIssue("parameter.missingInDto",
                        "JPQL parameter is missing in DTO parameters: " + parameterName));
            }
        }

        for (String parameterName : dtoParameters) {
            if (!jpqlParameters.contains(parameterName)) {
                issues.add(new JpqlValidationIssue("parameter.unusedInJpql",
                        "DTO parameter is not used in JPQL: " + parameterName));
            }
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 1100;
    }

    protected Set<String> extractParameterNames(String jpql) {
        Set<String> parameterNames = new LinkedHashSet<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(jpql);
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
        }
        return parameterNames;
    }
}
