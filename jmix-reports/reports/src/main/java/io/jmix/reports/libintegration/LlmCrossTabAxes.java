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

package io.jmix.reports.libintegration;

import io.jmix.core.annotation.Internal;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryParameterNames;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.structure.BandOrientation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * What a cell of a cross-tab band has to know about the axes it sits between: which values the axes hold, which
 * of them a query must return so that its rows find their place in the matrix, and when the matrix has no cells
 * at all.
 * <p>
 * A cross-tab is assembled by {@code CrossTabExtractionController}, which links a cell to its column and to its
 * row by the name of a returned column alone. The rules of that linking are what this class holds, so that the
 * loader of the {@link io.jmix.reports.entity.DataSetType#LLM} data set type is left with reading a query,
 * binding values and executing it.
 */
@Internal
@NullMarked
public final class LlmCrossTabAxes {

    private static final Logger log = LoggerFactory.getLogger(LlmCrossTabAxes.class);

    private LlmCrossTabAxes() {
    }

    /**
     * Returns the name of the first cross-tab axis of this band that produced no values, or {@code null} when
     * every axis has some. An axis is put into the params by the controller whether it produced rows or not.
     *
     * @param bandName name of the band the data set belongs to, or {@code null} when it is unknown
     */
    @Nullable
    public static String firstEmptyAxis(Map<String, Object> params, @Nullable String bandName) {
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (isAxisOf(param.getKey(), bandName)
                    && param.getValue() instanceof List<?> rows && rows.isEmpty()) {
                return param.getKey();
            }
        }

        return null;
    }

    /**
     * Tells whether a params entry is an axis of this band rather than of another one.
     * <p>
     * The params of a run are one mutable map shared by every band of it: {@code ExtractionContextImpl} hands
     * the same map to every band and {@code CrossTabExtractionController} adds its axes to it, so the axes of a
     * cross-tab band stay in the params of every band extracted afterwards. An axis is named
     * {@code <band>_master_data} or {@code <band>_dynamic_header}, which is what tells whose it is.
     * <p>
     * A data set whose band is unknown claims every axis, as it did before the band name was published: without
     * a name there is nothing to compare, and a cross-tab cell that stopped seeing its own axes would be worse
     * than one seeing a foreign one.
     *
     * @param name     params entry name
     * @param bandName name of the band the data set belongs to, or {@code null} when it is unknown
     */
    public static boolean isAxisOf(String name, @Nullable String bandName) {
        if (!LlmQueryParameterNames.isCrossTabAxis(name)) {
            return false;
        }

        return bandName == null || LlmQueryParameterNames.isCrossTabAxisOf(name, bandName);
    }

    /**
     * Tells whether the axes a run put into its params are this data set's to read. Only a cell of a cross-tab
     * band sits between axes: a band of another orientation is never handed any, and a data set named after an
     * axis builds one instead of filling the matrix.
     * <p>
     * Both halves matter because the params of a run are one map shared by every band of it. A band that is not
     * a cross-tab would otherwise read a report parameter that happens to be named like an axis as one, and an
     * axis data set of a cross-tab band under a multi-row parent would find the axes of the row extracted before
     * it — its own among them — and be taken for a cell of the matrix it builds.
     *
     * @param dataSetName     name of the data set being loaded, which is what tells an axis from a cell:
     *                        {@code CrossTabExtractionController} recognises an axis by its data set's name
     * @param bandOrientation orientation of the band the data set belongs to, or {@code null} when it is
     *                        unknown, which claims the axes as before the orientation was published
     */
    public static boolean areReadBy(String dataSetName, @Nullable BandOrientation bandOrientation) {
        return !LlmQueryParameterNames.isCrossTabAxis(dataSetName)
                && (bandOrientation == null || bandOrientation == BandOrientation.CROSS);
    }

    /**
     * Tells the rows of a cross-tab axis from an ordinary parameter that merely happens to be named like one:
     * an axis holds rows, so it is a list of maps, and an empty list is the axis that produced nothing. A list
     * of anything else belongs to the report run and stays a parameter of its own.
     *
     * @param value value of a run parameter, which is {@code null} for a parameter left unfilled
     */
    public static boolean isAxisRows(@Nullable Object value) {
        return value instanceof List<?> rows && (rows.isEmpty() || rows.get(0) instanceof Map);
    }

    /**
     * Offers the values of one cross-tab axis, one name per field of its rows, so that a cell query can narrow
     * itself to the columns and rows the matrix actually has. The value offered is the whole list of what the
     * axis holds for that field, which a query matches with {@code IN}.
     * <p>
     * Exactly one of those names is required back as a result column: a cross-tab links a cell to its axis by
     * the first returned column whose name starts with the axis prefix, so requiring every field would let a
     * caption column come first and the matrix link by the caption text. The required one is the axis's first
     * referenceable field, which is the order the axis itself describes — the same field on every run, so that
     * a query generated once keeps answering the requirement.
     * <p>
     * A field with no values in it is required back all the same, but offers nothing to bind. The same naming
     * rules as for band fields apply: a name that is not an identifier is skipped, and a name already taken is
     * kept.
     *
     * @param axisName                 name of the axis data set, which its fields are named after
     * @param rows                     rows the axis produced
     * @param availableValues          values a query may bind, added to under the axis field names
     * @param requiredResultProperties columns a query must return, added to for this axis
     * @param warnOnce                 says a thing once per report run, given a reason to tell repetitions by
     */
    public static void addAxisValues(String axisName, List<?> rows, Map<String, Object> availableValues,
                                     List<String> requiredResultProperties,
                                     BiConsumer<String, Runnable> warnOnce) {
        for (Map.Entry<String, List<Object>> field : valuesByField(rows).entrySet()) {
            String name = LlmQueryParameterNames.ofCrossTabValue(axisName, field.getKey());
            if (!LlmQueryParameterNames.isValid(name)) {
                continue;
            }

            String axisPrefix = LlmQueryParameterNames.ofCrossTabAxisPrefix(axisName);
            if (fieldsAreOrdered(rows)
                    && requiredResultProperties.stream().noneMatch(required -> required.startsWith(axisPrefix))) {
                requiredResultProperties.add(name);
            }

            List<Object> values = field.getValue();
            if (values.isEmpty()) {
                // The column is still required — the axis has this field, and which field links the matrix is
                // decided by the axis, not by this run — but there is no value to offer.
                continue;
            }

            if (availableValues.containsKey(name)) {
                warnOnce.accept("shadowed-axis-field:" + name,
                        () -> log.warn("Parameter [{}] is already available, so the values of the field [{}] of "
                                + "the cross-tab axis [{}] are not offered to the query, while the column of that "
                                + "name may still be required back; rename one of them to make both usable",
                                name, field.getKey(), axisName));
            } else {
                availableValues.put(name, values);
            }
        }
    }

    /**
     * Fails a query that cannot be placed into the matrix of a cross-tab band, because the controller drops
     * every cell it cannot link and the band then renders empty with no error at all. Failing here turns that
     * silence into a message.
     * <p>
     * An axis holding rows is checked whether or not it holds any value: a field that is empty in every row
     * offers nothing to bind, but the matrix still has that column, and a cell query that cannot be linked would
     * otherwise render an empty matrix with nothing said.
     *
     * @param dataSetName              data set the query belongs to, named in a failure
     * @param query                    query whose columns are read
     * @param params                   parameters of the run, which is where the axes arrive
     * @param requiredResultProperties columns the axes require the query to return
     * @param bandName                 name of the band the data set belongs to, or {@code null} when unknown
     */
    public static void checkAxesAreLinkable(String dataSetName, LlmDataQuery query,
                                            Map<String, Object> params, List<String> requiredResultProperties,
                                            @Nullable String bandName) {
        for (Map.Entry<String, Object> param : params.entrySet()) {
            String name = param.getKey();
            // Entries of other bands are not this one's business; an axis that produced no rows renders no
            // columns, so there is nothing to link a cell to, and such a band is not executed at all.
            if (!isAxisOf(name, bandName) || !isAxisRows(param.getValue())
                    || ((List<?>) param.getValue()).isEmpty()) {
                continue;
            }

            String prefix = LlmQueryParameterNames.ofCrossTabAxisPrefix(name);

            // The controller links by the first column starting with the axis name — not with the axis name and
            // an underscore — and then cuts one character more. Reading the query the same way here is what
            // makes a column it would stumble over this loader's failure instead of the engine's.
            String returned = firstWithPrefix(query.getResultProperties(), name);
            if (returned == null) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns no column named [%s<field>], so its rows cannot be "
                                + "linked to the cross-tab axis [%s]; it returns %s",
                        dataSetName, prefix, name, query.getResultProperties()));
            }

            // A column named after the axis with no field name after it. The controller cuts one character past
            // the axis name — which such a column has not got, so the run dies inside the engine with nothing
            // said about which query is at fault — or, with the separator alone, links the matrix by a field
            // named by the empty string, which no row has, and the matrix comes out empty in silence.
            if (returned.length() <= prefix.length() || !returned.startsWith(prefix)) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns the column [%s], which is named after the cross-tab "
                                + "axis [%s] without a field name after it, so its rows cannot be linked to that "
                                + "axis; return a column named [%s<field>] instead",
                        dataSetName, returned, name, prefix));
            }

            // A cross-tab links a cell by the first column of the axis prefix, so a query that puts another
            // field of the axis first would link the matrix by that field — a caption, for instance — and lose
            // the cells whose value differs from it.
            String required = firstWithPrefix(requiredResultProperties, prefix);
            if (required != null && !required.equals(returned)) {
                throw new DataLoadingException(String.format(
                        "The query of data set [%s] returns [%s] before [%s], so the cross-tab axis [%s] would be "
                                + "linked by the wrong field; a cross-tab links a cell by the first column named "
                                + "after the axis. The query returns %s",
                        dataSetName, returned, required, name, query.getResultProperties()));
            }
        }
    }

    /**
     * Tells whether the fields of an axis row come in an order that means anything. A data set that names its
     * columns keeps them in a {@code LinkedHashMap} — an LLM data set builds its rows in select-clause order —
     * while {@code AbstractDbDataLoader} builds the rows of a JPQL or SQL data set as a plain {@code HashMap},
     * where "the first field" is a hash order that changes between runs of the JVM. A map of any other kind is
     * taken for unordered: requiring a column on the strength of an order that may not be one is the mistake
     * this guards against.
     * <p>
     * Nothing may be required of a query on the strength of such an order: the same stored query would be
     * accepted on one run and refused on the next.
     */
    private static boolean fieldsAreOrdered(List<?> rows) {
        return rows.stream()
                .filter(Map.class::isInstance)
                .findFirst()
                .map(LinkedHashMap.class::isInstance)
                .orElse(false);
    }

    /**
     * Groups the values of one cross-tab axis by the field they belong to, keyed by every field the axis has, in
     * the order its rows describe them: a field is a field of the axis whether this run left it empty or not, so
     * which one comes first does not change with the data — the required column would otherwise move between
     * runs and stop matching the stored query. A row that is not a row and a field without a value contribute
     * nothing.
     */
    private static Map<String, List<Object>> valuesByField(List<?> rows) {
        Map<String, List<Object>> valuesByField = new LinkedHashMap<>();

        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> fields)) {
                continue;
            }

            for (Map.Entry<?, ?> field : fields.entrySet()) {
                List<Object> values = valuesByField.computeIfAbsent(String.valueOf(field.getKey()),
                        name -> new ArrayList<>());
                Object value = field.getValue();
                if (value != null) {
                    values.add(value);
                }
            }
        }

        return valuesByField;
    }

    @Nullable
    private static String firstWithPrefix(List<String> names, String prefix) {
        return names.stream().filter(name -> name.startsWith(prefix)).findFirst().orElse(null);
    }
}
