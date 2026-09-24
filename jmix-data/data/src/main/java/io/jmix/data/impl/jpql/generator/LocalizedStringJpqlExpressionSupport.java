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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.CoreProperties;
import io.jmix.core.LocalizedStringSupport;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Builds the JPQL expression that yields the text of a locale from a localized string column, following the
 * resolution chain of {@link LocalizedStringSupport}: the entries of the resolution keys, then the first line.
 * Uses only functions of the Jmix JPQL grammar. The line break that separates the entries cannot be written
 * as a literal ({@code QueryTree} replaces line breaks with spaces before parsing), so the database produces
 * it through the function named by {@link io.jmix.data.persistence.DbmsFeatures#getCharFunctionName()}.
 */
@Internal
@Component("data_LocalizedStringJpqlExpressionSupport")
public class LocalizedStringJpqlExpressionSupport {

    /**
     * The separator the database produces with {@link #lineBreakExpression(String)}.
     */
    protected static final String LINE_BREAK = "\n";

    @Autowired
    protected LocalizedStringSupport localizedStringSupport;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DbmsSpecifics dbmsSpecifics;
    @Autowired
    protected CoreProperties coreProperties;



    /**
     * @return true if the path ends in a JPA, non-collection, non-LOB property of the localized string datatype.
     * LOB properties are excluded because string functions over them are not supported by every database, the
     * same reason the sort expression leaves them to {@code getLobSortExpression}.
     */
    public boolean isLocalizedString(MetaPropertyPath path) {
        Range range = path.getRange();
        if (!range.isDatatype()) {
            return false;
        }

        Datatype<?> datatype = range.asDatatype();
        return datatype instanceof LocalizedStringDatatype
                && metadataTools.isJpa(path)
                && !metadataTools.isElementCollection(path.getMetaProperty())
                && !metadataTools.isLob(path.getMetaProperty());
    }

    /**
     * Builds the expression for an {@code order by} item and for a comparison operand.
     * <p>
     * The expression is a built-in {@code coalesce(...)} wrapped in {@code concat(..., '')}, or in
     * {@code lower(...)} for a case-insensitive comparison. The wrapper does not change the value, but it lets
     * the Jmix JPQL parser commit to a string expression at the first token: a bare {@code coalesce(...)} or
     * {@code function('coalesce', ...)} of this size makes it backtrack for seconds on every query.
     *
     * @param path      the localized string property; the store of its entity decides how the line break is
     *                  produced. The store of the entity rather than of the enclosing class, because an
     *                  embeddable belongs to the main store however many stores embed it, while the query runs
     *                  against the store of the entity.
     * @param column    JPQL path of the column, e.g. {@code e.name} or {@code {E}.name}
     * @param locale    the locale whose text is extracted
     * @param lowerCase whether the result must be lower-cased for a case-insensitive comparison
     */
    public String buildResolvedValueExpression(MetaPropertyPath path, String column, Locale locale, boolean lowerCase) {
        String storeName = path.getMetaClass().getStore().getName();

        // Where the line-break function yields a binary string, LOWER over the concatenation would not change
        // case, so the column is lower-cased before it and the markers are lower-cased with it. Everywhere else
        // only the result is lower-cased: the entry keys of the stored value then keep the case the parser
        // reads them with, and the expression carries one LOWER instead of one per column reference.
        boolean lowerCaseSource = lowerCase && dbmsSpecifics.getDbmsFeatures(storeName).isCharFunctionBinary();
        String coalesce = "coalesce(" + buildResolutionChain(storeName, column, locale, lowerCaseSource) + ")";

        return lowerCase && !lowerCaseSource
                ? "lower(" + coalesce + ")"
                : "concat(" + coalesce + ", '')";
    }

    /**
     * Builds the resolved text framed by line breaks, to be searched for in the list that
     * {@link #buildSearchList(Collection)} produces. This is how {@code IN} is expressed: the Jmix JPQL grammar
     * accepts only a path or a function invocation as its left operand, and a function invocation of this size
     * takes the parser seconds, while a search reads as fast as any comparison.
     */
    public String buildResolvedValueSearchExpression(MetaPropertyPath path, String column, Locale locale) {
        String storeName = path.getMetaClass().getStore().getName();
        String lineBreak = lineBreakExpression(storeName);
        return "concat(" + lineBreak + ", coalesce(" + buildResolutionChain(storeName, column, locale, false) + "), "
                + lineBreak + ")";
    }

    /**
     * Joins the values of an {@code IN} list into one string for {@link #buildResolvedValueSearchExpression}:
     * every value framed by line breaks, so that only a whole resolved text matches one of them.
     * <p>
     * A null value and a value holding a line break are left out: a resolved text is never null when it is
     * compared and never holds a line break, so neither can match. An empty list becomes a single line break,
     * which no framed text is found in, so that {@code IN} matches nothing and {@code NOT IN} everything.
     */
    public String buildSearchList(Collection<?> values) {
        StringBuilder list = new StringBuilder(LINE_BREAK);
        for (Object value : values) {
            if (value != null && !value.toString().contains(LINE_BREAK)) {
                list.append(value).append(LINE_BREAK);
            }
        }
        return list.toString();
    }

    /**
     * @return the arguments of the coalesce: the entries of the resolution keys, then the first line
     */
    protected String buildResolutionChain(String storeName, String column, Locale locale, boolean lowerCaseSource) {
        String lineBreak = lineBreakExpression(storeName);
        String source = lowerCaseSource ? "lower(" + column + ")" : column;
        String defaultValue = buildDefaultValueExpression(source, lineBreak, lowerCaseSource);

        StringJoiner chain = new StringJoiner(", ");
        List<String> keys = entryKeys(localizedStringSupport.resolutionKeys(locale));
        for (String key : keys) {
            String markerKey = lowerCaseSource ? key.toLowerCase(Locale.ROOT) : key;
            chain.add(buildEntryValueExpression(source, markerKey, lineBreak));
        }

        if (keys.isEmpty()) {
            // Reached only when no resolution key can open an entry, which takes an application whose default
            // locale has an unusual key, because that key is always one of them. COALESCE needs two arguments,
            // so the default value is repeated.
            chain.add(defaultValue);
        }
        chain.add(defaultValue);

        return chain.toString();
    }

    public Locale getCurrentLocale() {
        return localizedStringSupport.getCurrentLocale();
    }

    /**
     * @return the JPQL function call that yields a line break in the given store's database
     */
    protected String lineBreakExpression(String storeName) {
        String function = dbmsSpecifics.getDbmsFeatures(storeName).getCharFunctionName();
        return "function('" + function + "', 10)";
    }

    /**
     * The text of one entry, or null when the entry is absent or empty. The column is padded with line breaks
     * so that the first line and the last line need no special handling.
     */
    protected String buildEntryValueExpression(String column, String key, String lineBreak) {
        // The padded column ends with an empty entry of the key, so the marker is always found: an absent entry is
        // found there and yields an empty text, which needs no case to guard the arithmetic below.
        String padded = "concat(" + lineBreak + ", " + column + ", " + lineBreak + ", '" + key + "=', " + lineBreak + ")";
        String marker = "concat(" + lineBreak + ", '" + key + "=')";
        String position = "locate(" + marker + ", " + padded + ")";
        String start = "(" + position + " + " + (key.length() + 2) + ")";
        String end = "locate(" + lineBreak + ", " + padded + ", " + start + ")";

        // The parser strips the whitespace that follows the sign, so the database does too. TRIM strips spaces
        // only, which leaves a tab after the sign as a divergence of the kind whitespace before the sign is.
        // It runs before the emptiness test, so that an entry of nothing but spaces answers null on both sides.
        String text = "trim(leading from substring(" + padded + ", " + start + ", " + end + " - " + start + "))";

        return "nullif(" + text + ", '')";
    }

    /**
     * The first line of the column, which is the default value in the canonical form, or the whole column when
     * it has no line break.
     * <p>
     * A value that carries no default value may be stored without the leading line break, which is how a value
     * imported from another format looks. Its first line is an entry, not a default value, so it is reported as
     * an empty text, the way {@link LocalizedStringSupport#parse(String)} reads it.
     */
    protected String buildDefaultValueExpression(String column, String lineBreak, boolean lowerCaseSource) {
        // The column is followed by a line break, so the first line is found without a case for a value that has
        // no line break at all.
        String terminated = "concat(" + column + ", " + lineBreak + ")";
        String firstLine = "substring(" + terminated + ", 1, locate(" + lineBreak + ", " + terminated + ") - 1)";

        String entryOnFirstLine = buildEntryOnFirstLineCondition(column, lowerCaseSource);
        return entryOnFirstLine.isEmpty()
                ? firstLine
                : "case when " + entryOnFirstLine + " then '' else " + firstLine + " end";
    }

    /**
     * @return the condition that the first line of the column opens an entry of an available locale, or an
     * empty string when no key can open one. Only the available locales can be tested: the keys a value actually
     * carries are unknown when the query is built. The key is compared in its canonical case, the only one the
     * parser reads an entry in; a column lower-cased for a case-insensitive search is compared with the key
     * lower-cased along with it.
     */
    protected String buildEntryOnFirstLineCondition(String column, boolean lowerCaseSource) {
        StringJoiner condition = new StringJoiner(" or ");
        for (String key : entryKeys(keysOfAvailableLocales())) {
            String markerKey = lowerCaseSource ? key.toLowerCase(Locale.ROOT) : key;
            condition.add("locate('" + markerKey + "=', " + column + ") = 1");
        }
        return condition.toString();
    }

    /**
     * @return the keys an entry of an available locale can be written with: the locale key itself and the
     * language alone, which is the second step of the resolution chain
     */
    protected Set<String> keysOfAvailableLocales() {
        Set<String> keys = new LinkedHashSet<>();
        for (Locale locale : coreProperties.getAvailableLocales()) {
            keys.add(localizedStringSupport.localeKey(locale));

            if (!locale.getLanguage().isEmpty()) {
                keys.add(locale.getLanguage());
            }
        }
        return keys;
    }

    /**
     * Keys are written into JPQL string literals, and the locale of the session is not a validated input. A
     * key that could not open an entry anyway is therefore dropped rather than escaped: the Jmix JPQL grammar
     * does not accept the doubled quote that escaping a quote would produce.
     *
     * @return the keys a stored value can carry as an entry
     */
    protected List<String> entryKeys(Collection<String> keys) {
        return keys.stream().filter(localizedStringSupport::isEntryKey).toList();
    }
}
