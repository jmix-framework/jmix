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

package io.jmix.texttodata.validation;

import io.jmix.data.QueryTransformerFactory;
import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("textdt_JpqlValidationService")
public class JpqlValidationService {

    protected static final Pattern PARAMETER_PATTERN = Pattern.compile(":([A-Za-z_][A-Za-z0-9_]*)");
    protected static final Pattern CURRENT_DATE_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_date\\s*\\(", Pattern.CASE_INSENSITIVE);
    protected static final Pattern CURRENT_TIME_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_time\\s*\\(", Pattern.CASE_INSENSITIVE);
    protected static final Pattern CURRENT_TIMESTAMP_WITH_PARENTHESES_PATTERN =
            Pattern.compile("\\bcurrent_timestamp\\s*\\(", Pattern.CASE_INSENSITIVE);

    @Autowired
    protected DomainModelRegistry domainModelRegistry;

    @Autowired(required = false)
    protected QueryTransformerFactory queryTransformerFactory;

    public JpqlValidationResult validate(GeneratedJpqlResult generatedJpqlResult) {
        List<JpqlValidationIssue> issues = new ArrayList<>();

        if (generatedJpqlResult == null) {
            issues.add(new JpqlValidationIssue("result.missing", "Generated JPQL result is null"));
            return invalid(issues);
        }

        validateJpqlPresence(generatedJpqlResult, issues);
        validateReadOnlyQuery(generatedJpqlResult, issues);
        validateCommonNonJpqlConstructs(generatedJpqlResult, issues);
        validateJpqlSyntax(generatedJpqlResult, issues);
        validateRootEntity(generatedJpqlResult, issues);
        validateUsedEntities(generatedJpqlResult, issues);
        validateUsedPropertyPaths(generatedJpqlResult, issues);
        validateParameters(generatedJpqlResult, issues);

        return issues.isEmpty() ? new JpqlValidationResult(true, List.of())
                : invalid(issues);
    }

    protected void validateJpqlPresence(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        if (generatedJpqlResult.getJpql() == null || generatedJpqlResult.getJpql().isBlank()) {
            issues.add(new JpqlValidationIssue("jpql.blank", "JPQL is blank"));
        }
    }

    protected void validateReadOnlyQuery(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        String jpql = generatedJpqlResult.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return;
        }

        String normalizedJpql = jpql.trim().toLowerCase(Locale.ROOT);
        if (!normalizedJpql.startsWith("select ")) {
            issues.add(new JpqlValidationIssue("jpql.notSelect", "Only read-only select JPQL is supported"));
        }

        if (containsWord(normalizedJpql, "update")
                || containsWord(normalizedJpql, "delete")
                || containsWord(normalizedJpql, "insert")) {
            issues.add(new JpqlValidationIssue("jpql.writeOperation", "Write JPQL operations are not allowed"));
        }
    }

    protected void validateCommonNonJpqlConstructs(GeneratedJpqlResult generatedJpqlResult,
                                                   List<JpqlValidationIssue> issues) {
        String jpql = generatedJpqlResult.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return;
        }

        String normalizedJpql = jpql.trim().toLowerCase(Locale.ROOT);

        if (containsWord(normalizedJpql, "limit") || containsWord(normalizedJpql, "offset")) {
            issues.add(new JpqlValidationIssue("jpql.sqlPagination",
                    "JPQL must not contain SQL pagination keywords such as LIMIT or OFFSET"));
        }

        if (containsWord(normalizedJpql, "date_sub")
                || containsWord(normalizedJpql, "date_add")
                || containsWord(normalizedJpql, "interval")
                || containsWord(normalizedJpql, "curdate")
                || containsWord(normalizedJpql, "now")) {
            issues.add(new JpqlValidationIssue("jpql.sqlDateFunction",
                    "JPQL must not contain SQL-specific date functions or interval expressions"));
        }

        if (CURRENT_DATE_WITH_PARENTHESES_PATTERN.matcher(jpql).find()
                || CURRENT_TIME_WITH_PARENTHESES_PATTERN.matcher(jpql).find()
                || CURRENT_TIMESTAMP_WITH_PARENTHESES_PATTERN.matcher(jpql).find()) {
            issues.add(new JpqlValidationIssue("jpql.currentFunctionParentheses",
                    "JPQL CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP must be used without parentheses"));
        }
    }

    protected void validateJpqlSyntax(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        String jpql = generatedJpqlResult.getJpql();
        if (jpql == null || jpql.isBlank() || !isQueryParserAvailable()) {
            return;
        }

        try {
            queryTransformerFactory.parser(jpql).getEntityName();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            issues.add(new JpqlValidationIssue("jpql.syntax.invalid",
                    "Invalid JPQL syntax: " + cause.getMessage()));
        }
    }

    protected void validateRootEntity(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        String rootEntityName = generatedJpqlResult.getRootEntityName();
        if (rootEntityName == null || rootEntityName.isBlank()) {
            issues.add(new JpqlValidationIssue("rootEntity.blank", "Root entity name is blank"));
            return;
        }

        if (!domainModelRegistry.containsEntity(rootEntityName)) {
            issues.add(new JpqlValidationIssue("rootEntity.unknown",
                    "Unknown root entity: " + rootEntityName));
        }
    }

    protected void validateUsedEntities(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        for (String usedEntity : generatedJpqlResult.getUsedEntities()) {
            if (!domainModelRegistry.containsEntity(usedEntity)) {
                issues.add(new JpqlValidationIssue("usedEntity.unknown",
                        "Unknown used entity: " + usedEntity));
            }
        }
    }

    protected void validateUsedPropertyPaths(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        String rootEntityName = generatedJpqlResult.getRootEntityName();
        if (rootEntityName == null || rootEntityName.isBlank() || !domainModelRegistry.containsEntity(rootEntityName)) {
            return;
        }

        for (String propertyPath : generatedJpqlResult.getUsedPropertyPaths()) {
            if (!domainModelRegistry.containsPropertyPath(rootEntityName, propertyPath)) {
                issues.add(new JpqlValidationIssue("propertyPath.invalid",
                        "Invalid property path for root entity " + rootEntityName + ": " + propertyPath));
            }
        }
    }

    protected void validateParameters(GeneratedJpqlResult generatedJpqlResult, List<JpqlValidationIssue> issues) {
        String jpql = generatedJpqlResult.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return;
        }

        Set<String> jpqlParameters = extractParameterNames(jpql);
        Set<String> dtoParameters = new LinkedHashSet<>();
        for (GeneratedJpqlParameter parameter : generatedJpqlResult.getParameters()) {
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
    }

    protected Set<String> extractParameterNames(String jpql) {
        Set<String> parameterNames = new LinkedHashSet<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(jpql);
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
        }
        return parameterNames;
    }

    protected boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    protected boolean isQueryParserAvailable() {
        return queryTransformerFactory != null;
    }

    protected JpqlValidationResult invalid(List<JpqlValidationIssue> issues) {
        return new JpqlValidationResult(false, List.copyOf(issues));
    }
}
