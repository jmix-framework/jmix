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
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.LlmQueryParameterNames;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    protected List<String> storedColumnsOf(BandDefinition band) {
        if (band.getDataSets() == null) {
            return List.of();
        }

        List<String> columns = new ArrayList<>();
        for (DataSet dataSet : band.getDataSets()) {
            if (dataSet.getType() != DataSetType.LLM || StringUtils.isBlank(dataSet.getLlmGeneratedQuery())) {
                continue;
            }
            LlmDataQuery storedQuery = readStoredQuery(dataSet);
            if (storedQuery != null) {
                columns.addAll(storedQuery.getResultProperties());
            }
        }
        return columns;
    }

    @Nullable
    protected LlmDataQuery readStoredQuery(DataSet dataSet) {
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
}
