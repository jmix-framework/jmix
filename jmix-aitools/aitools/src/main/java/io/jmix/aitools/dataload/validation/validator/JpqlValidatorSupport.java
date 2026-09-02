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

import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helpers for JPQL validators.
 */
public final class JpqlValidatorSupport {

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");

    private static final Pattern AS_ALIAS_PATTERN = Pattern.compile("\\bas\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PARAMETER_PATTERN = Pattern.compile(":([A-Za-z_][A-Za-z0-9_]*)");

    private JpqlValidatorSupport() {
    }

    /**
     * Returns whether the text contains the given word as a whole word.
     *
     * @param text text to search
     * @param word word to look for
     * @return {@code true} if the word is present
     */
    public static boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    /**
     * Returns whether the text contains a call to the given function (its name followed by {@code '('}).
     *
     * @param text         text to search
     * @param functionName function name to look for
     * @return {@code true} if such a function call is present
     */
    public static boolean containsFunctionCall(String text, String functionName) {
        return Pattern.compile("\\b" + Pattern.quote(functionName) + "\\s*\\(").matcher(text).find();
    }

    /**
     * Replaces every single-quoted string literal with an empty literal ({@code ''}), so the
     * literal's content (e.g. a {@code :}-prefixed word or an uppercase token) is not mistaken for
     * a JPQL parameter, keyword or constant.
     *
     * @param jpql JPQL text
     * @return the text with all string literals emptied
     */
    public static String stripStringLiterals(String jpql) {
        return STRING_LITERAL_PATTERN.matcher(jpql).replaceAll("''");
    }

    /**
     * Returns the named parameters the query text references, in order of first appearance. This is
     * the reading the add-on applies when it accepts or rejects a query: a {@code :name} occurrence
     * counts as a parameter reference unless it sits inside a string literal (see
     * {@link #stripStringLiterals(String)}). Consumers that store a query together with a parameter
     * declaration should derive that declaration with this method, so a query the consumer writes and
     * a query the add-on validates read the same.
     *
     * @param jpql JPQL text
     * @return the referenced parameter names in order of appearance; empty if there are none
     */
    public static Set<String> referencedParameters(String jpql) {
        // Strip string literals first: a ':'-prefixed word inside a literal is not a JPQL parameter.
        Set<String> parameterNames = new LinkedHashSet<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(stripStringLiterals(jpql));
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
        }
        return parameterNames;
    }

    /**
     * Extracts the alias names declared with {@code AS} at the top level of the query — that is,
     * identification variables (entity and join aliases) and select result variables. An {@code AS}
     * inside parentheses (such as the target type of {@code CAST(... AS ...)} or
     * {@code TREAT(... AS ...)}) is not an alias declaration and is skipped. String literals are
     * ignored.
     *
     * @param jpql JPQL text
     * @return the declared alias names in order of appearance; empty if there are none
     */
    public static List<String> extractAliases(String jpql) {
        String text = stripStringLiterals(jpql);
        List<String> aliases = new ArrayList<>();
        Matcher matcher = AS_ALIAS_PATTERN.matcher(text);
        while (matcher.find()) {
            if (parenthesisDepthBefore(text, matcher.start()) == 0) {
                aliases.add(matcher.group(1));
            }
        }
        return aliases;
    }

    /**
     * Parses the given JPQL into a {@link QueryParser}.
     *
     * @param queryTransformerFactory factory used to create the parser, may be {@code null} if unavailable
     * @param jpql                    JPQL text to parse
     * @return the parser, or {@code null} if the factory is missing, the text is blank, or parsing fails
     */
    @Nullable
    public static QueryParser getQueryParser(@Nullable QueryTransformerFactory queryTransformerFactory,
                                             @Nullable String jpql) {
        if (queryTransformerFactory == null || jpql == null || jpql.isBlank()) {
            return null;
        }

        try {
            return queryTransformerFactory.parser(jpql);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Returns the net parenthesis nesting depth of the text before the given index.
     *
     * @param text  text to scan
     * @param index exclusive end index up to which parentheses are counted
     * @return the number of unclosed {@code '('} before {@code index}
     */
    private static int parenthesisDepthBefore(String text, int index) {
        int depth = 0;
        for (int i = 0; i < index; i++) {
            char c = text.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
        }
        return depth;
    }
}
