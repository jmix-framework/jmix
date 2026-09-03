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

package io.jmix.reports.llm;

import io.jmix.core.annotation.Internal;
import io.jmix.reports.yarg.reporting.extraction.controller.CrossTabExtractionController;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Names under which an LLM data query may reference the values around it.
 * <p>
 * The rules live here because two sides must agree on them: the report designer tells query generation which
 * names exist, and the loader binds values under the very same names at run time. A name invented on one side
 * and not recognised on the other would fail the report instead of filtering a band.
 */
@Internal
public final class LlmQueryParameterNames {

    /**
     * A JPQL parameter name is an identifier, so a report parameter or a band field whose name is not one cannot
     * be referenced by a query at all.
     */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * How a query refers to a parameter. This is a copy of the reading the add-on judges a query by
     * ({@code JpqlValidatorSupport.referencedParameters}), kept because the parameters are also derived
     * here when the add-on is absent — a hand-written or hand-edited query is stored and run without it.
     * The two must agree, or a query the add-on considers to declare the wrong parameters is stored; the
     * agreement is pinned by a parity test rather than held by this comment.
     */
    private static final Pattern REFERENCE = Pattern.compile(":([A-Za-z_][A-Za-z0-9_]*)");

    /**
     * A string literal, whose content is text rather than query syntax.
     */
    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^']|'')*'");

    private LlmQueryParameterNames() {
    }

    /**
     * Returns the name a field of a parent band row is referenced by. SQL and JPQL data sets use
     * {@code ${Band.field}}, but a JPQL parameter name cannot contain a dot, so the name is flattened.
     *
     * @param bandName  name of the band the field belongs to
     * @param fieldName name of the field within that band's row
     * @return the parameter name, which still has to be checked with {@link #isValid(String)}
     */
    public static String ofBandField(String bandName, String fieldName) {
        return bandName + "_" + fieldName;
    }

    /**
     * Returns the name the values of one cross-tab axis field are referenced by. The form matches what
     * {@code SqlCrosstabPreprocessor} produces out of the {@code <dataSet>@<field>} reference SQL and JPQL data
     * sets use, so the same query reads the same for either type.
     *
     * @param dataSetName name of the axis data set, i.e. {@code <band>_dynamic_header} or {@code <band>_master_data}
     * @param fieldName   name of the field within that axis' rows
     * @return the parameter name, which still has to be checked with {@link #isValid(String)}
     */
    public static String ofCrossTabValue(String dataSetName, String fieldName) {
        return dataSetName + "_" + fieldName;
    }

    /**
     * Returns what every name of one cross-tab axis starts with. {@code CrossTabExtractionController} links a
     * cell to its axis by the first result column starting with the axis data set's name, so both sides of the
     * seam — the loader that builds the dictionary from real rows and the designer that builds one from stored
     * queries — decide by this prefix which axis a name belongs to.
     *
     * @param dataSetName name of the axis data set
     * @return the prefix the axis's names share
     */
    public static String ofCrossTabAxisPrefix(String dataSetName) {
        return dataSetName + "_";
    }

    /**
     * Tells an axis of a cross-tab band from an ordinary run parameter by the suffix
     * {@link CrossTabExtractionController} recognises its data sets by.
     *
     * @param name name of a run parameter, which for an axis is the axis data set's own name
     * @return {@code true} if the parameter holds the rows of a cross-tab axis
     */
    public static boolean isCrossTabAxis(String name) {
        return name.endsWith(CrossTabExtractionController.HORIZONTAL_BAND)
                || name.endsWith(CrossTabExtractionController.VERTICAL_BAND);
    }

    /**
     * Returns whether this is the name of an axis of that very band. Compared whole: a band named
     * {@code revenue} would otherwise recognise {@code revenue_extra_master_data} — the axis of a band named
     * {@code revenue_extra} — as its own.
     *
     * @param name     candidate axis name
     * @param bandName name of the band the axis would belong to
     */
    public static boolean isCrossTabAxisOf(String name, String bandName) {
        return name.equals(bandName + "_" + CrossTabExtractionController.HORIZONTAL_BAND)
                || name.equals(bandName + "_" + CrossTabExtractionController.VERTICAL_BAND);
    }

    /**
     * Returns the parameters a query text references, in the order it references them. This is what a query
     * declares by being written, and what the parameters of a stored document are derived from every time a
     * query is stored. It must read a query the same way the add-on's validator does
     * ({@code JpqlValidatorSupport.referencedParameters}), so a document the designer wrote and a document
     * the designer validated agree; a parity test enforces that. It lives here, rather than delegating to the
     * add-on, so the derivation still works when the add-on is absent.
     *
     * @param jpql query text
     * @return names of the referenced parameters
     */
    public static Set<String> referencedIn(String jpql) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = REFERENCE.matcher(stripStringLiterals(jpql));

        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    /**
     * Blanks out the string literals of a query, so that {@code like 'urn:isbn%'} references no parameter named
     * {@code isbn} — one nothing could ever bind. Every literal is replaced by spaces of the same length to
     * leave the rest of the text where it was.
     *
     * @param jpql query text
     * @return the same text with the content of its literals blanked out
     */
    public static String stripStringLiterals(String jpql) {
        return STRING_LITERAL.matcher(jpql).replaceAll(literal -> " ".repeat(literal.group().length()));
    }

    /**
     * @param name candidate parameter name
     * @return {@code true} if a generated query can reference this name
     */
    public static boolean isValid(String name) {
        return VALID_NAME.matcher(name).matches();
    }
}
