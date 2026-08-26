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
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reportsflowui.support.LlmDataSetGenerationSupport;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import llm_designer.test_support.TestLlmDataQueryService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the designer offers to query generation, and how a generated query is stored back into the data set.
 */
@SpringBootTest(classes = {LlmDesignerTestConfiguration.class})
public class LlmDataSetGenerationSupportTest {

    protected static final String PROMPT = "Orders of the given customer";
    protected static final String AXIS_NAME = "Revenue_dynamic_header";

    @Autowired
    protected LlmDataSetGenerationSupport generationSupport;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected LlmDataQueryService queryService;

    @Autowired
    protected Metadata metadata;

    @Test
    void testRequestCarriesThePrompt() {
        DataSet dataSet = llmDataSet(reportWithParameters());

        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(dataSet);

        assertThat(request.getPrompt()).isEqualTo(PROMPT);
    }

    @Test
    void testReportInputParametersAreOfferedWithTheirTypes() {
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
    void testParentBandColumnsAreOfferedWhenTheParentIsAnLlmDataSet() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Orders", rootBand(report));
        DataSet parentDataSet = llmDataSet(parentBand);
        parentDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o", List.of("orderNumber"), List.of(),
                null, List.of())));

        BandDefinition linesBand = band(report, "Lines", parentBand);
        DataSet linesDataSet = llmDataSet(linesBand);

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(linesDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName).contains("Orders_orderNumber");
    }

    @Test
    void testRootBandColumnsAreNotOffered() {
        // A run offers no Root_<field> name — the loader stops the walk short of the root band — so offering one
        // here would have generation reference a parameter nothing could bind, failing every run of the report.
        Report report = reportWithParameters();
        BandDefinition rootBand = rootBand(report);
        DataSet rootDataSet = llmDataSet(rootBand);
        rootDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select max(o.date) as reportedUntil from sales_Order o", List.of("reportedUntil"), List.of(),
                null, List.of())));

        DataSet ordersDataSet = llmDataSet(band(report, "Orders", rootBand));

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(ordersDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName).doesNotContain("Root_reportedUntil");
    }

    @Test
    void testUndeclaredParentBandIsNamedSoTheAuthorHearsAboutIt() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Orders", rootBand(report));
        DataSet parentDataSet = metadata.create(DataSet.class);
        parentDataSet.setName("Orders");
        parentDataSet.setBandDefinition(parentBand);
        parentDataSet.setType(DataSetType.JPQL);
        parentDataSet.setText("select o.number from sales_Order o");
        parentBand.getDataSets().add(parentDataSet);

        BandDefinition linesBand = band(report, "Lines", parentBand);
        DataSet linesDataSet = llmDataSet(linesBand);

        // A JPQL master states its columns inside its own text, so generation is offered no Orders_<field> name
        // and the query it produces cannot filter the detail band by its master row.
        assertThat(generationSupport.createGenerationRequest(linesDataSet).getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .noneMatch(name -> name.startsWith("Orders_"));
        assertThat(generationSupport.sourcesWithUndeclaredColumns(linesDataSet)).containsExactly("Orders");
    }

    @Test
    void testParentBandDeclaringItsColumnsIsNotNamed() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Orders", rootBand(report));
        DataSet parentDataSet = llmDataSet(parentBand);
        parentDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o", List.of("orderNumber"), List.of(),
                null, List.of())));

        DataSet linesDataSet = llmDataSet(band(report, "Lines", parentBand));

        assertThat(generationSupport.sourcesWithUndeclaredColumns(linesDataSet)).isEmpty();
    }

    @Test
    void testUndeclaredCrossTabAxisIsNamed() {
        Report report = reportWithParameters();
        BandDefinition crossBand = band(report, "Revenue", rootBand(report));
        crossBand.setOrientation(Orientation.CROSS);

        DataSet axis = metadata.create(DataSet.class);
        axis.setName(AXIS_NAME);
        axis.setBandDefinition(crossBand);
        axis.setType(DataSetType.JPQL);
        axis.setText("select o.date as period from sales_Order o");
        crossBand.getDataSets().add(axis);

        DataSet cellDataSet = llmDataSet(crossBand);
        cellDataSet.setName("Revenue");

        assertThat(generationSupport.sourcesWithUndeclaredColumns(cellDataSet))
                .containsExactly(AXIS_NAME);
    }


    @Test
    void testGeneratingForAnAxisIsOfferedNoColumnsOfTheOtherAxis() {
        Report report = reportWithParameters();
        BandDefinition crossBand = band(report, "Revenue", rootBand(report));
        crossBand.setOrientation(Orientation.CROSS);
        axisDataSet(crossBand, serializer.toJson(new LlmDataQuery(
                "select o.date as period from sales_Order o", List.of("period"), List.of(), null, List.of())));

        DataSet otherAxis = llmDataSet(crossBand);
        otherAxis.setName("Revenue_master_data");

        // Only a cell query is linked to the axes. A run of an axis data set receives no axis rows, so a query
        // generated for it against another axis would reference a parameter with nothing to bind.
        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(otherAxis);

        assertThat(request.getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .noneMatch(name -> name.startsWith(AXIS_NAME));
        assertThat(request.getRequiredResultProperties()).isEmpty();
        assertThat(generationSupport.sourcesWithUndeclaredColumns(otherAxis)).isEmpty();
    }

    @Test
    void testCrossTabAxisColumnsAreOfferedAsMultiValuedParameters() {
        Report report = reportWithParameters();
        BandDefinition crossBand = band(report, "Revenue", null);
        crossBand.setOrientation(Orientation.CROSS);
        axisDataSet(crossBand, serializer.toJson(new LlmDataQuery(
                "select year(o.date) as year from sales_Order o", List.of("year"), List.of(),
                null, List.of())));
        DataSet cellDataSet = llmDataSet(crossBand);

        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(cellDataSet);
        List<LlmQueryParameter> parameters = request.getAvailableParameters();

        assertThat(parameters)
                .filteredOn(parameter -> "Revenue_dynamic_header_year".equals(parameter.getName()))
                .singleElement()
                .extracting(LlmQueryParameter::isMultiValued)
                .isEqualTo(true);
        assertThat(request.getRequiredResultProperties()).containsExactly("Revenue_dynamic_header_year");
    }

    @Test
    void testOnlyTheFirstColumnOfACrossTabAxisIsRequiredBack() {
        // A caption column belongs to the axis but holds text no cell row has, and a cross-tab links a cell by
        // the first column named after the axis.
        Report report = reportWithParameters();
        BandDefinition crossBand = band(report, "Revenue", null);
        crossBand.setOrientation(Orientation.CROSS);
        axisDataSet(crossBand, serializer.toJson(new LlmDataQuery(
                "select year(o.date) as year, cast(year(o.date) as string) as year_caption from sales_Order o",
                List.of("year", "year_caption"), List.of(), null, List.of())));
        DataSet cellDataSet = llmDataSet(crossBand);

        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(cellDataSet);

        assertThat(request.getRequiredResultProperties()).containsExactly("Revenue_dynamic_header_year");
        assertThat(request.getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .contains("Revenue_dynamic_header_year", "Revenue_dynamic_header_year_caption");
    }


    @Test
    void testColumnsOfABandWhoseNameIsNotAnIdentifierAreNotOffered() {
        Report report = reportWithParameters();
        BandDefinition parentBand = band(report, "Order Details", rootBand(report));
        DataSet parentDataSet = llmDataSet(parentBand);
        parentDataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o", List.of("orderNumber"), List.of(),
                null, List.of())));

        DataSet linesDataSet = llmDataSet(band(report, "Lines", parentBand));

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(linesDataSet)
                .getAvailableParameters();

        assertThat(parameters).extracting(LlmQueryParameter::getName)
                .noneMatch(name -> name.contains("orderNumber"));
    }

    @Test
    void testGeneratedQueryIsStoredAsAReadableDocument() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        LlmDataQuery query = new LlmDataQuery("select o.number as orderNumber from sales_Order o",
                List.of("orderNumber"), List.of(), "All order numbers", List.of());

        generationSupport.storeGeneratedQuery(dataSet, query);

        assertThat(dataSet.getLlmGeneratedQuery()).isNotBlank();
        LlmDataQuery stored = serializer.fromJson(dataSet.getLlmGeneratedQuery());
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(query.getJpql());
        assertThat(stored.getResultProperties()).containsExactly("orderNumber");
    }

    @Test
    void testEditedQueryIsStoredAsAReadableDocument() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        generationSupport.storeGeneratedQuery(dataSet,
                new LlmDataQuery("select o.number as orderNumber from sales_Order o", List.of("orderNumber"),
                        List.of(), "All order numbers", List.of("Amounts are not converted")));

        generationSupport.storeEditedQuery(dataSet,
                "select o.number as num from sales_Order o where o.customer = :customerName", List.of("num"));

        LlmDataQuery stored = serializer.fromJson(dataSet.getLlmGeneratedQuery());
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).contains(":customerName");
        assertThat(stored.getResultProperties()).containsExactly("num");
        assertThat(stored.getParameters()).extracting(LlmQueryParameter::getName).containsExactly("customerName");
        assertThat(stored.getExplanation()).isEqualTo("All order numbers");
        assertThat(stored.getWarnings()).containsExactly("Amounts are not converted");
    }



    @Test
    void testBlankQueryTextLeavesTheDataSetWithoutAStoredQueryOnceEditingEnds() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        generationSupport.storeGeneratedQuery(dataSet, storedQueryWithNotes());

        generationSupport.finishEditedQuery(dataSet, "   ", List.of("orderNumber"));

        assertThat(dataSet.getLlmGeneratedQuery()).isNull();
    }


    @Test
    void testUnreadableStoredDocumentIsReportedRatherThanHidden() {
        DataSet dataSet = llmDataSet(reportWithParameters());
        dataSet.setLlmGeneratedQuery("{\"jpql\": ");

        assertThat(generationSupport.readStoredQuery(dataSet)).isNull();
        assertThatThrownBy(() -> generationSupport.readStoredQueryOrFail(dataSet))
                .isInstanceOf(LlmDataQueryException.class);
    }

    @Test
    void testOptionalReportParameterIsOfferedAsOptional() {
        // It is offered — a query can filter by it — but marked, so generation guards its condition and the
        // loader may bind null for it.
        Report report = reportWithParameters();
        report.getInputParameters().add(inputParameter("optionalCity", ParameterType.TEXT, false));
        DataSet dataSet = llmDataSet(report);

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(dataSet)
                .getAvailableParameters();

        assertThat(parameters)
                .filteredOn(parameter -> "optionalCity".equals(parameter.getName()))
                .singleElement()
                .extracting(LlmQueryParameter::isOptional)
                .isEqualTo(true);
        assertThat(parameters)
                .filteredOn(parameter -> "orderDate".equals(parameter.getName()))
                .singleElement()
                .extracting(LlmQueryParameter::isOptional)
                .isEqualTo(false);
    }

    @Test
    void testUnguardedOptionalParameterIsNamed() {
        // The one way this type prints wrong data instead of failing: an empty value is bound as null and a
        // plain comparison then matches nothing, emptying the band with no error.
        Report report = reportWithParameters();
        report.getInputParameters().add(inputParameter("optionalCity", ParameterType.TEXT, false));
        DataSet dataSet = llmDataSet(report);
        LlmDataQuery unguarded = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.city = :optionalCity",
                List.of("orderNumber"), List.of(new LlmQueryParameter("optionalCity", "java.lang.String")),
                null, List.of());

        assertThat(generationSupport.unguardedOptionalParameters(dataSet, unguarded))
                .containsExactly("optionalCity");
    }

    @Test
    void testGuardedOptionalParameterIsNotNamed() {
        Report report = reportWithParameters();
        report.getInputParameters().add(inputParameter("optionalCity", ParameterType.TEXT, false));
        DataSet dataSet = llmDataSet(report);
        LlmDataQuery guarded = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o "
                        + "where (:optionalCity is null or o.city = :optionalCity)",
                List.of("orderNumber"), List.of(new LlmQueryParameter("optionalCity", "java.lang.String")),
                null, List.of());

        assertThat(generationSupport.unguardedOptionalParameters(dataSet, guarded)).isEmpty();
    }




    @Test
    void testOptionalCollectionParameterIsNotMarkedOptional() {
        // A guard cannot rescue an IN over an empty collection, so such a parameter is not described as one a
        // query may survive without: an empty one fails the run instead, saying which parameter it was.
        Report report = reportWithParameters();
        report.getInputParameters().add(inputParameter("customers", ParameterType.ENTITY_LIST, false));
        DataSet dataSet = llmDataSet(report);

        List<LlmQueryParameter> parameters = generationSupport.createGenerationRequest(dataSet)
                .getAvailableParameters();

        assertThat(parameters)
                .filteredOn(parameter -> "customers".equals(parameter.getName()))
                .singleElement()
                .satisfies(parameter -> {
                    assertThat(parameter.isMultiValued()).isTrue();
                    assertThat(parameter.isOptional()).isFalse();
                });
    }

    @Test
    void testListOfEntitiesParameterIsOfferedAsMultiValued() {
        Report report = reportWithParameters();
        report.getInputParameters().add(inputParameter("customers", ParameterType.ENTITY_LIST));
        DataSet dataSet = llmDataSet(report);

        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(dataSet);
        List<LlmQueryParameter> parameters = request.getAvailableParameters();

        assertThat(parameters)
                .filteredOn(parameter -> "customers".equals(parameter.getName()))
                .singleElement()
                .extracting(LlmQueryParameter::isMultiValued)
                .isEqualTo(true);
        assertThat(request.getRequiredResultProperties()).isEmpty();
    }

    @Test
    void testGenerationAvailabilityFollowsWhatTheServiceSaysAboutIt() {
        TestLlmDataQueryService service = (TestLlmDataQueryService) queryService;
        assertThat(generationSupport.isGenerationAvailable()).isTrue();

        service.setGenerationAvailable(false);
        try {
            assertThat(generationSupport.isGenerationAvailable()).isFalse();
        } finally {
            service.setGenerationAvailable(true);
        }
    }

    @Test
    void testTypeStaysSupportedWhileGenerationIsNot() {
        // A data set whose query is already stored is edited, checked and run without a model behind it, so the
        // type does not stop being supported when the service says it cannot generate.
        TestLlmDataQueryService service = (TestLlmDataQueryService) queryService;
        assertThat(generationSupport.isTypeSupported()).isTrue();

        service.setGenerationAvailable(false);
        try {
            assertThat(generationSupport.isTypeSupported()).isTrue();
            assertThat(generationSupport.isGenerationAvailable()).isFalse();
        } finally {
            service.setGenerationAvailable(true);
        }
    }

    protected LlmDataQuery storedQueryWithNotes() {
        return new LlmDataQuery("select o.number as orderNumber from sales_Order o", List.of("orderNumber"),
                List.of(), "All order numbers", List.of("Time zone ignored"));
    }

    @Test
    void testColumnsAreNoChangeUnlessTheSetOfNamesDiffers() {
        // A template refers to a column by name, so a different order breaks nothing, and a first generation
        // has nothing to be compared against.
        assertThat(generationSupport.compareColumns(List.of("orderNumber", "total"),
                List.of("orderNumber", "total")).isEmpty()).isTrue();
        assertThat(generationSupport.compareColumns(List.of("orderNumber", "total"),
                List.of("total", "orderNumber")).isEmpty()).isTrue();
        assertThat(generationSupport.compareColumns(List.of(), List.of("orderNumber")).isEmpty()).isTrue();
    }

    @Test
    void testRegeneratedColumnsThatDifferAreReportedAsAddedAndDisappeared() {
        LlmDataSetGenerationSupport.ColumnsChange change = generationSupport.compareColumns(
                List.of("orderNumber", "total"), List.of("orderNumber", "amount"));

        assertThat(change.isEmpty()).isFalse();
        assertThat(change.added()).containsExactly("amount");
        assertThat(change.disappeared()).containsExactly("total");
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

    protected ReportInputParameter parameterByAlias(Report report, String alias) {
        return report.getInputParameters().stream()
                .filter(parameter -> alias.equals(parameter.getAlias()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No input parameter [" + alias + "]"));
    }

    protected ReportInputParameter inputParameter(String alias, ParameterType type) {
        // Required by default: only a required parameter is offered to generation, and most fixtures assert what
        // is offered.
        return inputParameter(alias, type, true);
    }

    protected ReportInputParameter inputParameter(String alias, ParameterType type, boolean required) {
        ReportInputParameter parameter = metadata.create(ReportInputParameter.class);
        parameter.setAlias(alias);
        parameter.setName(alias);
        parameter.setType(type);
        parameter.setRequired(required);
        return parameter;
    }

    /**
     * The band every other band of a report descends from, as the designer creates it.
     */
    protected BandDefinition rootBand(Report report) {
        return band(report, "Root", null);
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

    protected void axisDataSet(BandDefinition band, @Nullable String storedQuery) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName(AXIS_NAME);
        dataSet.setBandDefinition(band);
        dataSet.setType(DataSetType.LLM);
        dataSet.setLlmGeneratedQuery(storedQuery);
        band.getDataSets().add(dataSet);
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
