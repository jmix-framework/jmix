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
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorUtils.containsFunctionCall;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorUtils.containsWord;

@Component("aitols_CommonNonJpqlConstructsJpqlValidator")
public class CommonNonJpqlConstructsValidator implements JpqlResultValidator, Ordered {

    protected static final Pattern CURRENT_DATE_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_date\\s*\\(", Pattern.CASE_INSENSITIVE);
    protected static final Pattern CURRENT_TIME_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_time\\s*\\(", Pattern.CASE_INSENSITIVE);
    protected static final Pattern CURRENT_TIMESTAMP_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_timestamp\\s*\\(", Pattern.CASE_INSENSITIVE);

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();

        String normalizedJpql = jpql.trim().toLowerCase(Locale.ROOT);

        if (containsWord(normalizedJpql, "limit") || containsWord(normalizedJpql, "offset")) {
            issues.add(new JpqlValidationIssue("jpql.sqlPagination",
                    "JPQL must not contain SQL pagination keywords such as LIMIT or OFFSET"));
        }

        if (containsWord(normalizedJpql, "date_sub")
                || containsWord(normalizedJpql, "date_add")
                || containsWord(normalizedJpql, "interval")
                || containsWord(normalizedJpql, "curdate")
                || containsFunctionCall(normalizedJpql, "now")) {
            issues.add(new JpqlValidationIssue("jpql.sqlDateFunction",
                    "JPQL must not contain SQL-specific date functions or interval expressions"));
        }

        if (CURRENT_DATE_WITH_PARENTHESES_PATTERN.matcher(jpql).find()
                || CURRENT_TIME_WITH_PARENTHESES_PATTERN.matcher(jpql).find()
                || CURRENT_TIMESTAMP_WITH_PARENTHESES_PATTERN.matcher(jpql).find()) {
            issues.add(new JpqlValidationIssue("jpql.currentFunctionParentheses",
                    "JPQL CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP must be used without parentheses"));
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 300;
    }
}
