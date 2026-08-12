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

package io.jmix.reportsflowui.support;

import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.*;
import io.jmix.reports.llm.*;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves the designer's side of the {@link DataSetType#LLM} data set type: whether the type is usable at all,
 * what a query may be generated from, and how a generated query is stored.
 * <p>
 * The UI module needs no dependency on the AI Tools add-on for this: the type works exactly when the
 * {@link LlmDataQueryService} bean exists, which happens when the add-on is present.
 */
@NullMarked
@Component("report_LlmDataSetGenerationSupport")
public class LlmDataSetGenerationSupport {

    @Autowired
    protected ObjectProvider<LlmDataQueryService> llmDataQueryServiceProvider;

    @Autowired
    protected LlmDataQuerySerializer llmDataQuerySerializer;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    /**
     * @return {@code true} if queries can be generated, that is if the data set type is usable in this
     * application
     */
    public boolean isAvailable() {
        return llmDataQueryServiceProvider.getIfAvailable() != null;
    }

    /**
     * Describes what a query for this data set may be generated from: its prompt, the row limit, and the
     * parameters the query is allowed to reference.
     *
     * @param dataSet data set whose prompt is being turned into a query
     * @return the request to pass to {@link #generate(LlmQueryGenerationRequest)}
     */
    public LlmQueryGenerationRequest createGenerationRequest(DataSet dataSet) {
        Map<String, LlmQueryParameter> parameters = new LinkedHashMap<>();
        collectReportParameters(dataSet, parameters);
        collectParentBandColumns(dataSet, parameters);
        collectCrossTabAxisColumns(dataSet, parameters);

        return new LlmQueryGenerationRequest(StringUtils.defaultString(dataSet.getText()),
                List.copyOf(parameters.values()), dataSet.getLlmMaxResults());
    }

    /**
     * Generates a query. Runs the LLM, so callers are expected to do this in a background task.
     *
     * @param request prompt together with the parameters the query may reference
     * @return the generated query
     * @throws LlmDataQueryException if the query cannot be generated
     */
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        LlmDataQueryService service = llmDataQueryServiceProvider.getIfAvailable();
        if (service == null) {
            throw new LlmDataQueryException("Queries cannot be generated: the AI Tools add-on is not available");
        }

        return service.generate(request);
    }

    /**
     * Stores a generated query in the data set, so that report runs execute it instead of generating anew.
     *
     * @param dataSet data set to store the query in
     * @param query   query to store
     */
    public void storeGeneratedQuery(DataSet dataSet, LlmDataQuery query) {
        dataSet.setLlmGeneratedQuery(llmDataQuerySerializer.toJson(query));
    }

    /**
     * Stores a query edited by hand, assembling the document out of the text and the column names as the author
     * left them. The parameters are re-derived from the text and the previous document's explanation and
     * warnings are carried over (see {@link LlmDataQuerySerializer#assemble}).
     * <p>
     * An empty query text means the data set has no stored query and runs by generating one, so it clears the
     * document instead of storing an empty one.
     *
     * @param dataSet          data set to store the query in
     * @param jpql             query text as edited
     * @param resultProperties column names in select-clause order
     */
    public void storeEditedQuery(DataSet dataSet, String jpql, List<String> resultProperties) {
        if (StringUtils.isBlank(jpql)) {
            dataSet.setLlmGeneratedQuery(null);
            return;
        }

        List<String> columns = resultProperties.stream()
                .map(StringUtils::trimToEmpty)
                .toList();

        LlmDataQuery edited = llmDataQuerySerializer.assemble(jpql, columns, readStoredQuery(dataSet));
        dataSet.setLlmGeneratedQuery(llmDataQuerySerializer.toJson(edited));
    }

    /**
     * Offers the report's input parameters under their aliases, typed as the report itself types them.
     */
    protected void collectReportParameters(DataSet dataSet, Map<String, LlmQueryParameter> parameters) {
        Report report = dataSet.getBandDefinition() != null ? dataSet.getBandDefinition().getReport() : null;
        if (report == null || report.getInputParameters() == null) {
            return;
        }

        for (ReportInputParameter inputParameter : report.getInputParameters()) {
            String alias = inputParameter.getAlias();
            // A query can only reference a name that is a JPQL identifier, so an alias that is not one is left
            // out here as well as in the loader.
            if (StringUtils.isBlank(alias) || !LlmQueryParameterNames.isValid(alias)) {
                continue;
            }
            parameters.put(alias, new LlmQueryParameter(alias, resolveJavaType(inputParameter), null));
        }
    }

    /**
     * Offers the columns of the parent bands under the flattened names the loader binds them by. Only an LLM
     * parent with a stored query is known here: a JPQL or SQL parent states its columns as aliases inside the
     * query text, and reading them out of there is not worth it — such a parameter is written by hand.
     */
    protected void collectParentBandColumns(DataSet dataSet, Map<String, LlmQueryParameter> parameters) {
        BandDefinition band = dataSet.getBandDefinition();
        for (BandDefinition parentBand = band != null ? band.getParentBandDefinition() : null;
             parentBand != null;
             parentBand = parentBand.getParentBandDefinition()) {

            for (String column : storedColumnsOf(parentBand)) {
                String name = LlmQueryParameterNames.ofBandField(parentBand.getName(), column);
                if (!LlmQueryParameterNames.isValid(name)) {
                    continue;
                }
                // A run parameter of the same name outranks a band column, as it does in the loader.
                parameters.putIfAbsent(name, new LlmQueryParameter(name, Object.class.getName(), null));
            }
        }
    }

    /**
     * Offers the columns of the axes of a cross-tab band, so that a cell query can filter itself by them and
     * alias its own columns accordingly. Only an axis that is an LLM data set with a stored query states its
     * columns; against a JPQL or SQL axis they exist only once it has run.
     */
    protected void collectCrossTabAxisColumns(DataSet dataSet, Map<String, LlmQueryParameter> parameters) {
        BandDefinition band = dataSet.getBandDefinition();
        if (band == null || band.getOrientation() != Orientation.CROSS || band.getDataSets() == null) {
            return;
        }

        for (DataSet axis : band.getDataSets()) {
            String axisName = axis.getName();
            if (axis == dataSet || axisName == null || !LlmQueryParameterNames.isCrossTabAxis(axisName)) {
                continue;
            }

            for (String column : storedColumnsOf(axis)) {
                String name = LlmQueryParameterNames.ofCrossTabValue(axisName, column);
                if (!LlmQueryParameterNames.isValid(name)) {
                    continue;
                }
                // The type is unknown until the axis runs, as for a parent band column.
                parameters.putIfAbsent(name, new LlmQueryParameter(name, Object.class.getName(), null, true));
            }
        }
    }

    protected List<String> storedColumnsOf(BandDefinition band) {
        if (band.getDataSets() == null) {
            return Collections.emptyList();
        }

        List<String> columns = new ArrayList<>();
        for (DataSet dataSet : band.getDataSets()) {
            columns.addAll(storedColumnsOf(dataSet));
        }
        return columns;
    }

    protected List<String> storedColumnsOf(DataSet dataSet) {
        if (dataSet.getType() != DataSetType.LLM || StringUtils.isBlank(dataSet.getLlmGeneratedQuery())) {
            return Collections.emptyList();
        }

        LlmDataQuery storedQuery = readStoredQuery(dataSet);
        return storedQuery != null ? storedQuery.getResultProperties() : Collections.emptyList();
    }

    /**
     * Reads the query stored in a data set, or returns {@code null} when there is none or the stored document
     * cannot be read — a panel showing it has nothing better to do than show nothing.
     *
     * @param dataSet data set to read the stored query of
     * @return the stored query, or {@code null}
     */
    @Nullable
    public LlmDataQuery readStoredQuery(DataSet dataSet) {
        try {
            return llmDataQuerySerializer.fromJson(dataSet.getLlmGeneratedQuery());
        } catch (LlmDataQueryException e) {
            return null;
        }
    }

    protected String resolveJavaType(ReportInputParameter inputParameter) {
        Class<?> parameterClass = parameterClassResolver.resolveClass(inputParameter);
        return parameterClass != null ? parameterClass.getName() : Object.class.getName();
    }

    /**
     * Tells what a regenerated query changed about the columns of the one it replaced, so that a report whose
     * template prints a column that is no longer produced can be noticed while the author is still there.
     * <p>
     * Compared as sets: a template refers to a column by name, so the same names in another order break
     * nothing. A first generation reports no change, having nothing to compare against.
     *
     * @param previous columns of the query being replaced
     * @param current  columns of the generated query
     * @return what was added and what disappeared
     */
    public ColumnsChange compareColumns(List<String> previous, List<String> current) {
        if (previous.isEmpty()) {
            return new ColumnsChange(Collections.emptyList(), Collections.emptyList());
        }

        return new ColumnsChange(
                current.stream().filter(column -> !previous.contains(column)).toList(),
                previous.stream().filter(column -> !current.contains(column)).toList());
    }

    /**
     * What a regeneration did to the columns of the query it replaced.
     *
     * @param added       columns the new query returns and the previous one did not
     * @param disappeared columns the previous query returned and the new one does not
     */
    public record ColumnsChange(List<String> added, List<String> disappeared) {

        public boolean isEmpty() {
            return added.isEmpty() && disappeared.isEmpty();
        }
    }
}
