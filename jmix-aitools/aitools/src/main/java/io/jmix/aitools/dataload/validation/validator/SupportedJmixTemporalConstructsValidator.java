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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.stripStringLiterals;


/**
 * Checks that only supported Jmix date macros are used: {@code @between}, {@code @today},
 * {@code @dateEquals}, {@code @dateBefore}, {@code @dateAfter}.
 */
@Component("aitls_SupportedJmixTemporalConstructsValidator")
public class SupportedJmixTemporalConstructsValidator implements JpqlResultValidator, Ordered {

    public static final String UNSUPPORTED_MACRO_CODE = "jpql.unsupportedMacro";
    public static final String UNSUPPORTED_MACRO_GUIDANCE = "Use only supported Jmix date macros: @between, @today," +
            " @dateEquals, @dateBefore, @dateAfter.";

    protected static final Pattern MACRO_PATTERN = Pattern.compile("@([A-Za-z][A-Za-z0-9]*)\\s*\\(");
    protected static final Set<String> SUPPORTED_MACROS = Set.of(
            "between",
            "today",
            "dateequals",
            "datebefore",
            "dateafter"
    );

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();

        // Empty string literals so a macro-like token inside them (e.g. '@unknownMacro(x)') is not
        // mistaken for an actual Jmix query macro.
        Matcher macroMatcher = MACRO_PATTERN.matcher(stripStringLiterals(jpql));
        while (macroMatcher.find()) {
            String macroName = macroMatcher.group(1).toLowerCase(Locale.ROOT);
            if (!SUPPORTED_MACROS.contains(macroName)) {
                issues.add(new JpqlValidationIssue(UNSUPPORTED_MACRO_CODE,
                        "Unsupported Jmix query macro: @" + macroMatcher.group(1), UNSUPPORTED_MACRO_GUIDANCE));
            }
        }

        return  issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 400;
    }
}
