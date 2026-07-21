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

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.*;

/**
 * Rejects SQL-only constructs that are not valid JPQL: SQL pagination (LIMIT/OFFSET), SQL-specific
 * date functions and interval expressions, and CURRENT_DATE/CURRENT_TIME/CURRENT_TIMESTAMP used
 * with parentheses.
 */
@Component("aitls_CommonNonJpqlConstructsJpqlValidator")
public class CommonNonJpqlConstructsValidator implements JpqlResultValidator, Ordered {

    public static final String SQL_PAGINATION_CODE = "jpql.sqlPagination";
    public static final String SQL_PAGINATION_GUIDANCE = "Remove LIMIT and OFFSET from JPQL and move them into" +
            " maxResults and firstResult when the intent requires pagination.";

    public static final String SQL_DATE_FUNCTION_CODE = "jpql.sqlDateFunction";
    public static final String SQL_DATE_FUNCTION_GUIDANCE = "Remove SQL-specific date arithmetic and vendor functions." +
            " Prefer supported Jmix date macros or relative date time constants, and use named parameters only when the" +
            " date range cannot be expressed through supported constructs.";

    public static final String CURRENT_FUNCTION_PARENTHESES_CODE = "jpql.currentFunctionParentheses";
    public static final String CURRENT_FUNCTION_PARENTHESES_GUIDANCE = "Use CURRENT_DATE, CURRENT_TIME, and" +
            " CURRENT_TIMESTAMP without parentheses.";

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

        // Empty string literals so SQL keywords inside them (e.g. 'no limit applies') are not
        // mistaken for SQL pagination, date functions or CURRENT_* calls.
        String scannableJpql = stripStringLiterals(jpql);
        String normalizedJpql = scannableJpql.trim().toLowerCase(Locale.ROOT);

        if (containsWord(normalizedJpql, "limit") || containsWord(normalizedJpql, "offset")) {
            issues.add(new JpqlValidationIssue(SQL_PAGINATION_CODE,
                    "JPQL must not contain SQL pagination keywords such as LIMIT or OFFSET",
                    SQL_PAGINATION_GUIDANCE));
        }

        if (containsWord(normalizedJpql, "date_sub")
                || containsWord(normalizedJpql, "date_add")
                || containsWord(normalizedJpql, "interval")
                || containsWord(normalizedJpql, "curdate")
                || containsFunctionCall(normalizedJpql, "now")) {
            issues.add(new JpqlValidationIssue(SQL_DATE_FUNCTION_CODE,
                    "JPQL must not contain SQL-specific date functions or interval expressions",
                    SQL_DATE_FUNCTION_GUIDANCE));
        }

        if (CURRENT_DATE_WITH_PARENTHESES_PATTERN.matcher(scannableJpql).find()
                || CURRENT_TIME_WITH_PARENTHESES_PATTERN.matcher(scannableJpql).find()
                || CURRENT_TIMESTAMP_WITH_PARENTHESES_PATTERN.matcher(scannableJpql).find()) {
            issues.add(new JpqlValidationIssue(CURRENT_FUNCTION_PARENTHESES_CODE,
                    "JPQL CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP must be used without parentheses",
                    CURRENT_FUNCTION_PARENTHESES_GUIDANCE));
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 300;
    }
}
