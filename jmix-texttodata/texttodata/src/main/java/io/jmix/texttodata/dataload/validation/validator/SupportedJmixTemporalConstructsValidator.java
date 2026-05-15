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

package io.jmix.texttodata.dataload.validation.validator;

import io.jmix.core.JmixOrder;
import io.jmix.texttodata.dataload.generation.GeneratedJpqlResult;
import io.jmix.texttodata.dataload.validation.JpqlValidationIssue;
import io.jmix.texttodata.dataload.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("textdt_SupportedJmixTemporalConstructsValidator")
public class SupportedJmixTemporalConstructsValidator implements JpqlResultValidator, Ordered {

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

        Matcher macroMatcher = MACRO_PATTERN.matcher(jpql);
        while (macroMatcher.find()) {
            String macroName = macroMatcher.group(1).toLowerCase(Locale.ROOT);
            if (!SUPPORTED_MACROS.contains(macroName)) {
                issues.add(new JpqlValidationIssue("jpql.unsupportedMacro",
                        "Unsupported Jmix query macro: @" + macroMatcher.group(1)));
            }
        }

        return  issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 400;
    }
}
