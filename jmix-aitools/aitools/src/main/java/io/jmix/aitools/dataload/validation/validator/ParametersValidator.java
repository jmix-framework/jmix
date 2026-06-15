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

package io.jmix.aitools.dataload.validation.validator;

import io.jmix.core.JmixOrder;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that the JPQL named parameters and the declared parameters match — flagging both
 * parameters used in the query but missing from the declaration, and declared parameters that the
 * query never uses.
 */
@Component("aitls_ParametersValidator")
public class ParametersValidator implements JpqlResultValidator, Ordered {

    public static final String PARAMETER_MISSING_CODE = "parameter.missingInDto";
    public static final String PARAMETER_MISSING_GUIDANCE = "Ensure every named JPQL parameter is declared in the" +
            " parameters array.";

    public static final String PARAMETER_UNUSED_CODE = "parameter.unusedInJpql";
    public static final String PARAMETER_UNUSED_GUIDANCE = "Remove parameters that are not used in the JPQL text.";

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
                issues.add(new JpqlValidationIssue(PARAMETER_MISSING_CODE,
                        "JPQL parameter is missing in DTO parameters: " + parameterName,
                        PARAMETER_MISSING_GUIDANCE));
            }
        }

        for (String parameterName : dtoParameters) {
            if (!jpqlParameters.contains(parameterName)) {
                issues.add(new JpqlValidationIssue(PARAMETER_UNUSED_CODE,
                        "DTO parameter is not used in JPQL: " + parameterName, PARAMETER_UNUSED_GUIDANCE));
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
