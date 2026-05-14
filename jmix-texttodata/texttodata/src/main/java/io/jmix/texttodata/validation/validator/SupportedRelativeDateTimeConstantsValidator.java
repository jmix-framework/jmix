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
import io.jmix.data.impl.queryconstant.RelativeDateTimeMoment;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.validation.JpqlValidationIssue;
import io.jmix.texttodata.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("textdt_SupportedRelativeDateTimeConstantsValidator")
public class SupportedRelativeDateTimeConstantsValidator implements JpqlResultValidator, Ordered {

    protected static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");

    protected static final Pattern RELATIVE_DATE_TIME_CONSTANT_PATTERN =
            Pattern.compile("\\b[A-Z][A-Z0-9]*_[A-Z0-9_]*\\b");

    protected static final Set<String> SUPPORTED_RELATIVE_DATE_TIME_CONSTANTS = Arrays.stream(RelativeDateTimeMoment.values())
            .map(RelativeDateTimeMoment::name)
            .collect(Collectors.toUnmodifiableSet());

    protected static final Set<String> JPQL_TEMPORAL_KEYWORDS = Set.of(
            "CURRENT_DATE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP"
    );

    protected static final Set<String> NON_OPERATION_KEYWORDS = Set.of(
            "select",
            "from",
            "join",
            "left",
            "right",
            "inner",
            "outer",
            "fetch",
            "update",
            "delete",
            "insert",
            "into",
            "new",
            "as",
            "order",
            "group",
            "by"
    );
    protected static final Set<String> OPERATION_KEYWORDS = Set.of(
            "where",
            "and",
            "or",
            "on",
            "having",
            "when",
            "then",
            "else",
            "between",
            "in",
            "not"
    );

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();

        String jpqlWithoutStringLiterals = stripStringLiterals(jpql);
        Matcher matcher = RELATIVE_DATE_TIME_CONSTANT_PATTERN.matcher(jpqlWithoutStringLiterals);
        while (matcher.find()) {
            String constantName = matcher.group();
            if (!isRelativeDateTimeConstantUsage(jpqlWithoutStringLiterals, matcher.start(), matcher.end())) {
                continue;
            }
            if (JPQL_TEMPORAL_KEYWORDS.contains(constantName)) {
                continue;
            }
            if (!SUPPORTED_RELATIVE_DATE_TIME_CONSTANTS.contains(constantName)) {
                issues.add(new JpqlValidationIssue("jpql.unsupportedRelativeDateTimeConstant",
                        "Unsupported relative date time constant: " + constantName));
            }
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 500;
    }

    protected boolean isRelativeDateTimeConstantUsage(String jpql, int start, int end) {
        int previousIndex = previousNonWhitespaceIndex(jpql, start - 1);
        int nextIndex = nextNonWhitespaceIndex(jpql, end);

        if (previousIndex >= 0 && jpql.charAt(previousIndex) == '.') {
            return false;
        }
        if (nextIndex >= 0 && nextIndex < jpql.length() && jpql.charAt(nextIndex) == '.') {
            return false;
        }

        String previousWord = previousWord(jpql, start);
        if (NON_OPERATION_KEYWORDS.contains(previousWord)) {
            return false;
        }

        if (previousIndex < 0) {
            return true;
        }

        char previousChar = jpql.charAt(previousIndex);
        if ("=<>!,(+-*/".indexOf(previousChar) >= 0) {
            return true;
        }

        return OPERATION_KEYWORDS.contains(previousWord);
    }

    protected int previousNonWhitespaceIndex(String text, int fromIndex) {
        for (int index = fromIndex; index >= 0; index--) {
            if (!Character.isWhitespace(text.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    protected int nextNonWhitespaceIndex(String text, int fromIndex) {
        for (int index = fromIndex; index < text.length(); index++) {
            if (!Character.isWhitespace(text.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    protected String previousWord(String text, int tokenStart) {
        int index = previousNonWhitespaceIndex(text, tokenStart - 1);
        if (index < 0 || !Character.isLetter(text.charAt(index))) {
            return "";
        }

        int end = index + 1;
        while (index >= 0 && Character.isLetter(text.charAt(index))) {
            index--;
        }

        return text.substring(index + 1, end).toLowerCase(Locale.ROOT);
    }

    protected String stripStringLiterals(String jpql) {
        return STRING_LITERAL_PATTERN.matcher(jpql).replaceAll("''");
    }
}
