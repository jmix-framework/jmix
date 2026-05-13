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

package io.jmix.texttodata.postprocess;

import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("textdt_PaginationJpqlResultPostProcessor")
public class PaginationJpqlResultPostProcessor implements JpqlResultPostProcessor {

    // TODO: pinyazhin, used as fallback. Probably, will be removed in future
    protected static final Pattern LIMIT_PATTERN =
            Pattern.compile("(?i)\\s+limit\\s+(:[A-Za-z_][A-Za-z0-9_]*|\\d+)");
    protected static final Pattern OFFSET_PATTERN =
            Pattern.compile("(?i)\\s+offset\\s+(:[A-Za-z_][A-Za-z0-9_]*|\\d+)");
    protected static final Pattern PARAMETER_PATTERN =
            Pattern.compile(":([A-Za-z_][A-Za-z0-9_]*)");

    @Override
    public PostProcessedResult process(PostProcessedResult postProcessedResult) {
        GeneratedJpqlResult generatedJpqlResult = postProcessedResult.getGeneratedJpqlResult();
        String jpql = generatedJpqlResult.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return postProcessedResult;
        }

        Matcher limitMatcher = LIMIT_PATTERN.matcher(jpql);
        Matcher offsetMatcher = OFFSET_PATTERN.matcher(jpql);

        String limitValueRef = limitMatcher.find() ? limitMatcher.group(1) : null;
        String offsetValueRef = offsetMatcher.find() ? offsetMatcher.group(1) : null;

        Integer maxResults = postProcessedResult.getMaxResults();
        Integer firstResult = postProcessedResult.getFirstResult();

        Integer resolvedMaxResults = maxResults != null
                ? maxResults
                : resolveExecutionValue(limitValueRef, generatedJpqlResult.getParameters());
        Integer resolvedFirstResult = firstResult != null
                ? firstResult
                : resolveExecutionValue(offsetValueRef, generatedJpqlResult.getParameters());

        String normalizedJpql = jpql;
        Set<String> paginationParameterNames = new LinkedHashSet<>();

        if (limitValueRef != null && resolvedMaxResults != null) {
            normalizedJpql = LIMIT_PATTERN.matcher(normalizedJpql).replaceFirst(" ");
            collectParameterName(limitValueRef, paginationParameterNames);
        }

        if (offsetValueRef != null && resolvedFirstResult != null) {
            normalizedJpql = OFFSET_PATTERN.matcher(normalizedJpql).replaceFirst(" ");
            collectParameterName(offsetValueRef, paginationParameterNames);
        }

        if (normalizedJpql.equals(jpql) && resolvedMaxResults == maxResults && resolvedFirstResult == firstResult) {
            return postProcessedResult;
        }

        normalizedJpql = normalizeWhitespace(normalizedJpql);
        Set<String> remainingParameters = extractParameterNames(normalizedJpql);

        List<GeneratedJpqlParameter> parameters = new ArrayList<>();
        for (GeneratedJpqlParameter parameter : generatedJpqlResult.getParameters()) {
            if (paginationParameterNames.contains(parameter.getName()) && !remainingParameters.contains(parameter.getName())) {
                continue;
            }
            parameters.add(parameter);
        }

        List<String> warnings = new ArrayList<>(generatedJpqlResult.getWarnings());
        if ((limitValueRef != null && resolvedMaxResults != null) || (offsetValueRef != null && resolvedFirstResult != null)) {
            warnings.add("Pagination clauses were normalized into execution options");
        }

        GeneratedJpqlResult normalizedResult = new GeneratedJpqlResult(
                normalizedJpql,
                generatedJpqlResult.getRootEntityName(),
                List.copyOf(parameters),
                generatedJpqlResult.getUsedEntities(),
                generatedJpqlResult.getUsedPropertyPaths(),
                generatedJpqlResult.getExplanation(),
                List.copyOf(warnings),
                resolvedMaxResults,
                resolvedFirstResult
        );

        return new PostProcessedResult(normalizedResult, resolvedMaxResults, resolvedFirstResult);
    }

    @Nullable
    protected Integer resolveExecutionValue(String valueRef, List<GeneratedJpqlParameter> parameters) {
        if (valueRef == null || valueRef.isBlank()) {
            return null;
        }

        if (valueRef.startsWith(":")) {
            String parameterName = valueRef.substring(1);
            for (GeneratedJpqlParameter parameter : parameters) {
                if (parameterName.equals(parameter.getName())) {
                    return toInteger(parameter.getValue());
                }
            }
            return null;
        }

        return toInteger(valueRef);
    }

    @Nullable
    protected Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    protected void collectParameterName(String valueRef, Set<String> parameterNames) {
        if (valueRef != null && valueRef.startsWith(":")) {
            parameterNames.add(valueRef.substring(1));
        }
    }

    protected String normalizeWhitespace(String jpql) {
        return jpql.replaceAll("\\s+", " ").trim();
    }

    protected Set<String> extractParameterNames(String jpql) {
        Set<String> parameterNames = new LinkedHashSet<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(jpql.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
        }
        return parameterNames;
    }
}
