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

package llm_designer;

import io.jmix.core.Metadata;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reportsflowui.support.LlmDataSetGenerationSupport;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * What the designer offers to query generation, and how a generated query is stored back into the data set.
 */
@SpringBootTest(classes = {LlmDesignerTestConfiguration.class})
public class LlmDataSetGenerationSupportTest {

    protected static final String PROMPT = "Orders of the given customer";

    @Autowired
    protected LlmDataSetGenerationSupport generationSupport;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected Metadata metadata;

    @Test
    public void testServiceIsAvailable() {
        assertThat(generationSupport.isAvailable()).isTrue();
    }

    @Test
    public void testRequestCarriesThePromptAndTheRowLimit() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        dataSet.setLlmMaxResults(300);

        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(dataSet);

        assertThat(request.getPrompt()).isEqualTo(PROMPT);
        assertThat(request.getMaxResults()).isEqualTo(300);
    }

    @Test
    public void testReportInputParametersAreOfferedWithTheirTypes() {
        DataSet dataSet = llmDataSet(reportWithParameters());

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(dataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName).contains("customerName", "orderDate");
        assertThat(parameters)
                .filteredOn(parameter -> "customerName".equals(parameter.getName()))
                .singleElement()
                .extracting(LlmQueryParameter::getJavaType)
                .isEqualTo(String.class.getName());
    }

    @Test
    public void testParentBandColumnsAreOfferedWhenTheParentIsAnLlmDataSet() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Orders", null);
        DataSet parentDataSet = llmDataSet(parentBand);
        parentDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o", List.of("orderNumber"), List.of(),
                null, List.of(), null)));

        BandDefinition linesBand = band(report, "Lines", parentBand);
        DataSet linesDataSet = llmDataSet(linesBand);

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(linesDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName).contains("Orders_orderNumber");
    }

    @Test
    public void testParentBandColumnsAreNotOfferedWithoutAStoredQuery() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Orders", null);
        llmDataSet(parentBand);

        BandDefinition linesBand = band(report, "Lines", parentBand);
        DataSet linesDataSet = llmDataSet(linesBand);

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(linesDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName).doesNotContain("Orders_orderNumber");
    }

    @Test
    public void testColumnsOfABandWhoseNameIsNotAnIdentifierAreNotOffered() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Order Details", null);
        DataSet parentDataSet = llmDataSet(parentBand);
        parentDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o", List.of("orderNumber"), List.of(),
                null, List.of(), null)));

        DataSet linesDataSet = llmDataSet(band(report, "Lines", parentBand));

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(linesDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName)
                .noneMatch(name -> name.contains("orderNumber"));
    }

    @Test
    public void testGeneratedQueryIsStoredAsAReadableDocument() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        LlmDataQuery query = new LlmDataQuery("select o.number as orderNumber from sales_Order o",
                List.of("orderNumber"), List.of(), "All order numbers", List.of(), 200);

        generationSupport.storeGeneratedQuery(dataSet, query);

        assertThat(dataSet.getLlmGeneratedQuery()).isNotBlank();
        LlmDataQuery stored = serializer.fromJson(dataSet.getLlmGeneratedQuery());
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(query.getJpql());
        assertThat(stored.getResultProperties()).containsExactly("orderNumber");
        assertThat(stored.getMaxResults()).isEqualTo(200);
    }

    @Test
    public void testGenerationDelegatesToTheService() {
        LlmDataQuery generated = generationSupport.generate(
                new LlmQueryGenerationRequest(PROMPT, List.of(), null));

        assertThat(generated.getJpql()).contains("select o.number");
    }

    protected Report reportWithParameters() {
        Report report = metadata.create(Report.class);
        report.setName("LLM designer report");
        report.setBands(new LinkedHashSet<>());
        report.setInputParameters(new ArrayList<>(List.of(
                inputParameter("customerName", ParameterType.TEXT),
                inputParameter("orderDate", ParameterType.DATE))));
        return report;
    }

    protected ReportInputParameter inputParameter(String alias, ParameterType type) {
        ReportInputParameter parameter = metadata.create(ReportInputParameter.class);
        parameter.setAlias(alias);
        parameter.setName(alias);
        parameter.setType(type);
        return parameter;
    }

    protected BandDefinition band(Report report, String name, @Nullable BandDefinition parent) {
        BandDefinition band = metadata.create(BandDefinition.class);
        band.setReport(report);
        band.setName(name);
        band.setParentBandDefinition(parent);
        band.setDataSets(new ArrayList<>());
        report.getBands().add(band);
        return band;
    }

    protected DataSet axisDataSet(BandDefinition band, String name, @Nullable String storedQuery) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName(name);
        dataSet.setBandDefinition(band);
        dataSet.setType(DataSetType.LLM);
        dataSet.setLlmGeneratedQuery(storedQuery);
        band.getDataSets().add(dataSet);
        return dataSet;
    }

    protected DataSet llmDataSet(Report report) {
        return llmDataSet(band(report, "Data", null));
    }

    protected DataSet llmDataSet(BandDefinition band) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName(band.getName());
        dataSet.setBandDefinition(band);
        dataSet.setType(DataSetType.LLM);
        dataSet.setText(PROMPT);
        band.getDataSets().add(dataSet);
        return dataSet;
    }
}
