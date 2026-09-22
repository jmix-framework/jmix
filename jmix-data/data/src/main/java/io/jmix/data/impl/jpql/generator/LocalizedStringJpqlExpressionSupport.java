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
     * @param path      the localized string property; its store decides how the line break is produced
     * @param column    JPQL path of the column, e.g. {@code e.name} or {@code {E}.name}
     * @param locale    the locale whose text is extracted
     * @param lowerCase whether the result must be lower-cased for a case-insensitive comparison. The column is
     *                  lower-cased before the concatenation and the locale keys of the markers are lower-cased
     *                  too, because on MySQL {@code CHAR()} yields a binary string and {@code LOWER} over the
     *                  concatenated result would not change case.
     * @return {@code function('coalesce', entry(key1), entry(key2), ..., firstLine)}. The {@code FUNCTION} form
     * is used instead of a plain {@code coalesce(...)} because the Jmix JPQL grammar accepts a function
     * invocation, but not an arbitrary scalar expression, as the left operand of {@code IN}; EclipseLink prints
     * both forms as the same SQL.
     */
    public String buildResolvedValueExpression(MetaPropertyPath path, String column, Locale locale, boolean lowerCase) {
        String lineBreak = lineBreakExpression(metadataTools.getPropertyEnclosingMetaClass(path).getStore().getName());
        String source = lowerCase ? "lower(" + column + ")" : column;

        String defaultValue = buildDefaultValueExpression(source, lineBreak, lowerCase);
        StringBuilder sb = new StringBuilder("function('coalesce', ");

        int entries = 0;
        for (String key : entryKeys(localizedStringSupport.resolutionKeys(locale))) {
            String markerKey = lowerCase ? key.toLowerCase(Locale.ROOT) : key;
            sb.append(buildEntryValueExpression(source, markerKey, lineBreak)).append(", ");
            entries++;
        }

        if (entries == 0) {
            // Reached only when no resolution key can open an entry, which takes an application whose default
            // locale has an unusual key, because that key is always one of them. COALESCE needs two arguments
            // and the FUNCTION form is what an IN operand accepts, so the default value is repeated.
            sb.append(defaultValue).append(", ");
        }
        sb.append(defaultValue).append(")");

        return sb.toString();
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
        String padded = "concat(" + lineBreak + ", " + column + ", " + lineBreak + ")";
        String marker = "concat(" + lineBreak + ", '" + key + "=')";
        String position = "locate(" + marker + ", " + padded + ")";
        String start = "(" + position + " + " + (key.length() + 2) + ")";
        String end = "locate(" + lineBreak + ", " + padded + ", " + start + ")";

        return "nullif(case when " + position + " > 0"
                + " then substring(" + padded + ", " + start + ", " + end + " - " + start + ")"
                + " else '' end, '')";
    }

    /**
     * The first line of the column, which is the default value in the canonical form, or the whole column when
     * it has no line break.
     * <p>
     * A value that carries no default value may be stored without the leading line break, which is how a value
     * imported from another format looks. Its first line is an entry, not a default value, so it is reported as
     * an empty text, the way {@link LocalizedStringSupport#parse(String)} reads it.
     */
    protected String buildDefaultValueExpression(String column, String lineBreak, boolean lowerCase) {
        String position = "locate(" + lineBreak + ", " + column + ")";
        // One flat case: EclipseLink prints a case nested in the else branch of another case as invalid SQL.
        StringBuilder expression = new StringBuilder("case");
        String entryOnFirstLine = buildEntryOnFirstLineCondition(column, lowerCase);

        if (!entryOnFirstLine.isEmpty()) {
            expression.append(" when ").append(entryOnFirstLine).append(" then ''");
        }

        return expression
                .append(" when ").append(position).append(" > 0")
                .append(" then substring(").append(column).append(", 1, ").append(position).append(" - 1)")
                .append(" else ").append(column).append(" end")
                .toString();
    }

    /**
     * @return the condition that the first line of the column opens an entry of an available locale, or an
     * empty string when no locale is configured. Only the available locales can be tested: the keys a value
     * actually carries are unknown when the query is built.
     */
    protected String buildEntryOnFirstLineCondition(String column, boolean lowerCase) {
        StringJoiner condition = new StringJoiner(" or ");
        for (String key : entryKeys(keysOfAvailableLocales())) {
            String marker = lowerCase ? key.toLowerCase(Locale.ROOT) : key;
            condition.add("locate('" + marker + "=', " + column + ") = 1");
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
