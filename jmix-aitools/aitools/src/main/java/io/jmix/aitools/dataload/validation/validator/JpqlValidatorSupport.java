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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helpers for JPQL validators.
 */
public final class JpqlValidatorSupport {

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");

    private static final Pattern AS_ALIAS_PATTERN = Pattern.compile("\\bas\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PLAIN_PATH_PATTERN =
            Pattern.compile("[A-Za-z_$][\\w$]*(\\.[A-Za-z_$][\\w$]*)*");
    private static final Pattern DISTINCT_PREFIX_PATTERN = Pattern.compile("(?i)^distinct\\s+");
    private static final Pattern RESULT_ALIAS_SUFFIX_PATTERN = Pattern.compile("(?i)\\s+(as\\s+)?[A-Za-z_$][\\w$]*$");
    private static final Set<String> LITERAL_WORDS = Set.of("true", "false", "null");
    private static final Pattern OBJECT_PATTERN = Pattern.compile("(?i)^object\\s*\\(\\s*([A-Za-z_$][\\w$]*)\\s*\\)$");
    private static final Pattern SELECT_PREFIX_PATTERN = Pattern.compile("(?i)^\\s*select\\s+");
    private static final Pattern SELECT_ITEM_ALIAS_PATTERN = Pattern.compile("(?i)\\s+as\\s+([A-Za-z_$][\\w$]*)\\s*$");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

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
     * Returns the select-clause expressions whose result column is an entity or embeddable instance
     * rather than a value: a bare identification variable ({@code select c}) or a path ending in a
     * reference or embedded attribute ({@code select o.customer}). An aggregate over such an expression
     * ({@code count(o)}) yields a value and is not returned; neither is a path the data model does not know.
     *
     * @param parser   parser of the query
     * @param metadata metadata used to resolve the attributes a path ends in
     * @return the offending expressions as the parser reports them, in select-clause order; empty if none
     */
    public static List<String> selectedEntityExpressions(QueryParser parser, Metadata metadata) {
        Map<String, String> variableEntities = new HashMap<>();
        for (QueryParser.QueryPath path : parser.getQueryPaths()) {
            variableEntities.putIfAbsent(path.getVariableName().toLowerCase(Locale.ROOT), path.getEntityName());
        }

        List<String> entityExpressions = new ArrayList<>();
        for (String expression : parser.getSelectedExpressionsList()) {
            String path = selectedPathOf(expression);
            if (path == null) {
                continue;
            }

            int dot = path.indexOf('.');
            if (dot < 0) {
                // A bare identifier in the select clause is an identification variable, unless it is a literal.
                if (!LITERAL_WORDS.contains(path.toLowerCase(Locale.ROOT))) {
                    entityExpressions.add(expression);
                }
                continue;
            }

            String entityName = variableEntities.get(path.substring(0, dot).toLowerCase(Locale.ROOT));
            MetaClass metaClass = entityName != null ? metadata.findClass(entityName) : null;
            MetaPropertyPath propertyPath = metaClass != null ? metaClass.getPropertyPath(path.substring(dot + 1)) : null;
            if (propertyPath != null && propertyPath.getRange().isClass()) {
                entityExpressions.add(expression);
            }
        }
        return entityExpressions;
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
     * Counts the values the select clause of a query selects. A comma inside a function call, a subquery or a
     * string literal does not separate selected values.
     *
     * @param jpql JPQL select query
     * @return the number of selected values
     */
    public static int selectedValueCount(String jpql) {
        return splitSelectedItems(selectClauseOf(jpql)).size();
    }

    /**
     * Returns the {@code AS} alias of each value the select clause selects, in select-clause order, so that a
     * value can be told from the name it is bound to by position. An {@code as} inside a value, such as the target
     * type of {@code cast(... as string)}, is not an alias.
     *
     * @param jpql JPQL select query
     * @return one entry per selected value: its alias, or {@code null} when the value has none
     */
    public static List<@Nullable String> selectedAliases(String jpql) {
        List<String> items = splitSelectedItems(selectClauseOf(jpql));
        List<@Nullable String> aliases = new ArrayList<>(items.size());
        for (String item : items) {
            Matcher matcher = SELECT_ITEM_ALIAS_PATTERN.matcher(item);
            aliases.add(matcher.find() ? matcher.group(1) : null);
        }
        return aliases;
    }

    /**
     * Returns the expression of each value the select clause selects, in select-clause order, without its
     * {@code AS} alias and with whitespace collapsed, so that two queries can be compared value by value.
     *
     * @param jpql JPQL select query
     * @return one expression per selected value
     */
    public static List<String> selectedExpressions(String jpql) {
        List<String> items = splitSelectedItems(selectClauseOf(jpql));
        List<String> expressions = new ArrayList<>(items.size());
        for (String item : items) {
            String expression = SELECT_ITEM_ALIAS_PATTERN.matcher(item).replaceFirst("");
            expressions.add(WHITESPACE_PATTERN.matcher(expression.trim()).replaceAll(" "));
        }
        return expressions;
    }

    /**
     * Returns the names of the columns of a query: the {@code AS} aliases of its select clause, in select-clause
     * order. Columns are bound to values by position, so the aliases name the columns only when every selected
     * value has one; a partial list is not returned.
     *
     * @param jpql JPQL select query
     * @return the aliases, or an empty list when not every selected value is aliased
     */
    public static List<String> resultAliases(String jpql) {
        List<@Nullable String> aliases = selectedAliases(jpql);
        List<String> names = new ArrayList<>(aliases.size());
        for (String alias : aliases) {
            if (alias == null) {
                return List.of();
            }
            names.add(alias);
        }
        return names;
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
     * Returns the plain path a select item consists of, without a leading {@code distinct} and a trailing
     * result alias, and with {@code OBJECT(…)} unwrapped.
     *
     * @param expression select item as the parser reports it, e.g. {@code o.customer as customer}
     * @return the path, or {@code null} when the item is anything else (a function call, arithmetic, a literal)
     */
    @Nullable
    private static String selectedPathOf(String expression) {
        String item = DISTINCT_PREFIX_PATTERN.matcher(expression.trim()).replaceFirst("");
        if (!PLAIN_PATH_PATTERN.matcher(item).matches()) {
            item = RESULT_ALIAS_SUFFIX_PATTERN.matcher(item).replaceFirst("");
        }
        Matcher objectMatcher = OBJECT_PATTERN.matcher(item);
        if (objectMatcher.matches()) {
            // OBJECT(c) is the JPQL spelling of the variable itself.
            item = objectMatcher.group(1);
        }
        return PLAIN_PATH_PATTERN.matcher(item).matches() ? item : null;
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

    /**
     * Returns the part of the query before its own {@code from}, with string literals blanked. A {@code from} of
     * a subquery is inside parentheses, so only the depth-zero one ends the select clause.
     */
    private static String selectClauseOf(String jpql) {
        String text = stripStringLiterals(jpql);
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            } else if (depth == 0 && text.regionMatches(true, i, "from", 0, 4)
                    && isWordBoundary(text, i - 1) && isWordBoundary(text, i + 4)) {
                return text.substring(0, i);
            }
        }
        return text;
    }

    /**
     * Splits a select clause into its selected values at the depth-zero commas: a comma inside a function call or
     * a subquery separates arguments, not values. The leading {@code select} keyword is dropped.
     */
    private static List<String> splitSelectedItems(String selectClause) {
        List<String> items = new ArrayList<>();
        int depth = 0;
        int itemStart = 0;
        for (int i = 0; i < selectClause.length(); i++) {
            char character = selectClause.charAt(i);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            } else if (character == ',' && depth == 0) {
                items.add(selectClause.substring(itemStart, i));
                itemStart = i + 1;
            }
        }
        items.add(selectClause.substring(itemStart));
        items.set(0, SELECT_PREFIX_PATTERN.matcher(items.get(0)).replaceFirst(""));
        return items;
    }

    /**
     * Whether the character at this index ends a word. An underscore or a dot does not: {@code valid_from} and
     * {@code e.from} carry the letters {@code from} without being the query's own {@code from}.
     */
    private static boolean isWordBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return true;
        }
        char character = text.charAt(index);
        return !Character.isLetterOrDigit(character) && character != '_' && character != '.';
    }
}
