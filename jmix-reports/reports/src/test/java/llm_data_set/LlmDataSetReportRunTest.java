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

package llm_data_set;

import io.jmix.core.Metadata;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import llm_data_set.test_support.TestLlmDataQueryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
class LlmDataSetReportRunTest {

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected TestLlmDataQueryService queryService;

    @Autowired
    protected Metadata metadata;

    @BeforeEach
    void setUp() {
        queryService.reset();
    }

    @Test
    void testReportWithLlmBandRendersRowsOfTheStoredQuery() {
        queryService.setRows(List.of(
                Map.of("orderNumber", "A-1", "amount", "120"),
                Map.of("orderNumber", "A-2", "amount", "80")));

        ReportOutputDocument document = reportRunner.byReportEntity(createReport()).run();

        String content = new String(document.getContent(), StandardCharsets.UTF_8);
        assertThat(content)
                .contains("\"A-1\",\"120\"")
                .contains("\"A-2\",\"80\"");
    }

    @Test
    void testNestedLlmBandIsExecutedPerParentRowWithItsParentField() {
        ReportOutputDocument document = reportRunner.byReportEntity(createReportWithNestedLlmBand()).run();

        assertThat(new String(document.getContent(), StandardCharsets.UTF_8)).contains("\"A-1\"");
        assertThat(queryService.getExecutionRequests()).hasSize(2);
        assertThat(queryService.getExecutionRequests())
                .flatExtracting(LlmQueryExecutionRequest::getArguments)
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("Orders_number", "A-1"), tuple("Orders_number", "A-2"));
    }

    @Test
    void testLlmRowsAreMergedWithTheOtherDataSetOfTheBandByTheLinkParameter() {
        queryService.setRows(List.of(
                Map.of("orderNumber", "A-2", "amount", "80"),
                Map.of("orderNumber", "A-1", "amount", "120")));

        ReportOutputDocument document = reportRunner.byReportEntity(createReportWithLinkedDataSets()).run();

        // The Groovy rows carry the customer, the LLM rows the amount; the link field pairs them up, and the
        // order of the LLM rows does not matter.
        String content = new String(document.getContent(), StandardCharsets.UTF_8);
        assertThat(content)
                .contains("\"A-1\",\"Acme\",\"120\"")
                .contains("\"A-2\",\"Globex\",\"80\"");
    }

    @Test
    void testCrossTabBandBuildsItsMatrixFromAnLlmCellDataSet() {
        queryService.setRows(List.of(
                Map.of("revenue_dynamic_header_month", 3, "revenue_master_data_publisherId", 1, "amount", 10.0),
                Map.of("revenue_dynamic_header_month", 4, "revenue_master_data_publisherId", 2, "amount", 20.0)));

        ReportOutputDocument document = reportRunner.byReportEntity(createCrossTabReport()).run();

        Sheet sheet = readFirstSheet(document.getContent());
        assertThat(cellValue(sheet, 0, 1)).isEqualTo("March");
        assertThat(cellValue(sheet, 0, 2)).isEqualTo("April");
        assertThat(cellValue(sheet, 1, 0)).isEqualTo("Nintendo");
        assertThat(cellValue(sheet, 1, 1)).isEqualTo(10.0);
        assertThat(cellValue(sheet, 2, 0)).isEqualTo("Ubisoft");
        assertThat(cellValue(sheet, 2, 2)).isEqualTo(20.0);
    }

    @Test
    void testCrossTabAxisValuesReachTheCellDataSetAsLists() {
        queryService.setRows(List.of());
        queryService.setQueryToGenerate(new LlmDataQuery("select 1 as amount from sales_Order o",
                List.of("revenue_dynamic_header_month", "revenue_master_data_publisherId", "amount"),
                List.of(), "Revenue matrix", List.of(), null));

        reportRunner.byReportEntity(createCrossTabReport(true)).run();

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .contains(tuple("revenue_dynamic_header_month", List.of(3, 4)),
                        tuple("revenue_master_data_publisherId", List.of(1, 2)));
    }

    /**
     * Root → a cross-tab band whose axes are Groovy data sets and whose cells come from an LLM one. Reuses the
     * template of the annotated cross-tab report, whose named ranges are what the controller renders into.
     */
    protected Report createCrossTabReport() {
        return createCrossTabReport(false);
    }

    protected Report createCrossTabReport(boolean regenerateOnRun) {
        Report report = metadata.create(Report.class);
        report.setName("Cross-tab LLM report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setMultiDataSet(false);
        rootBand.setPosition(0);

        BandDefinition revenueBand = metadata.create(BandDefinition.class);
        revenueBand.setReport(report);
        revenueBand.setName("revenue");
        revenueBand.setOrientation(Orientation.CROSS);
        revenueBand.setMultiDataSet(false);
        revenueBand.setPosition(0);
        revenueBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(revenueBand);

        DataSet header = metadata.create(DataSet.class);
        header.setName("revenue_dynamic_header");
        header.setBandDefinition(revenueBand);
        header.setType(DataSetType.GROOVY);
        header.setText("""
                return [["month": 3, "month_caption": "March"], ["month": 4, "month_caption": "April"]]""");

        DataSet masterData = metadata.create(DataSet.class);
        masterData.setName("revenue_master_data");
        masterData.setBandDefinition(revenueBand);
        masterData.setType(DataSetType.GROOVY);
        masterData.setText("""
                return [["publisherId": 1, "publisher_name": "Nintendo"],\
                 ["publisherId": 2, "publisher_name": "Ubisoft"]]""");

        DataSet cells = metadata.create(DataSet.class);
        cells.setName("revenue");
        cells.setBandDefinition(revenueBand);
        cells.setType(DataSetType.LLM);
        cells.setText("Revenue per publisher and month");
        cells.setLlmGeneratedQuery("""
                {"jpql":"select 1 as amount from sales_Order o",\
                "resultProperties":["revenue_dynamic_header_month","revenue_master_data_publisherId","amount"]}""");
        cells.setLlmRegenerateOnRun(regenerateOnRun);

        revenueBand.setDataSets(List.of(header, masterData, cells));
        report.setBands(Set.of(rootBand, revenueBand));

        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.XLSX);
        template.setName("RevenueByPublisher.xlsx");
        template.setContent(readCrossTabTemplate());
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        report.setXml(reportsSerialization.convertToString(report));
        return report;
    }

    protected byte[] readCrossTabTemplate() {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("io/jmix/reports/test_support/report/RevenueByPublisher.xlsx")) {
            return Objects.requireNonNull(stream, "The cross-tab template is missing").readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected Sheet readFirstSheet(byte[] content) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            return workbook.getSheetAt(0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected Object cellValue(Sheet sheet, int row, int column) {
        Cell cell = sheet.getRow(row).getCell(column);
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : cell.getStringCellValue();
    }

    protected Report createReportWithNestedLlmBand() {
        Report report = metadata.create(Report.class);
        report.setName("Nested LLM band report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setMultiDataSet(false);
        rootBand.setPosition(0);

        BandDefinition ordersBand = metadata.create(BandDefinition.class);
        ordersBand.setReport(report);
        ordersBand.setName("Orders");
        ordersBand.setOrientation(Orientation.HORIZONTAL);
        ordersBand.setMultiDataSet(false);
        ordersBand.setPosition(0);
        ordersBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(ordersBand);

        DataSet ordersDataSet = metadata.create(DataSet.class);
        ordersDataSet.setName("Orders");
        ordersDataSet.setBandDefinition(ordersBand);
        ordersDataSet.setType(DataSetType.GROOVY);
        ordersDataSet.setText("return [[\"number\": \"A-1\"], [\"number\": \"A-2\"]]");
        ordersBand.setDataSets(List.of(ordersDataSet));

        BandDefinition linesBand = metadata.create(BandDefinition.class);
        linesBand.setReport(report);
        linesBand.setName("Lines");
        linesBand.setOrientation(Orientation.HORIZONTAL);
        linesBand.setMultiDataSet(false);
        linesBand.setPosition(0);
        linesBand.setParentBandDefinition(ordersBand);
        ordersBand.getChildrenBandDefinitions().add(linesBand);

        DataSet linesDataSet = metadata.create(DataSet.class);
        linesDataSet.setName("Lines");
        linesDataSet.setBandDefinition(linesBand);
        linesDataSet.setType(DataSetType.LLM);
        linesDataSet.setText("Lines of the order");
        linesDataSet.setLlmGeneratedQuery("""
                {"jpql":"select l.product as product from sales_OrderLine l where l.order.number = :Orders_number",\
                "resultProperties":["product"],\
                "parameters":[{"name":"Orders_number","javaType":"java.lang.String"}]}""");
        linesBand.setDataSets(List.of(linesDataSet));

        report.setBands(Set.of(rootBand, ordersBand, linesBand));
        report.setTemplates(List.of(csvTemplate(report, "Number\n${number}\n")));
        report.setDefaultTemplate(report.getTemplates().get(0));

        report.setXml(reportsSerialization.convertToString(report));
        return report;
    }

    /**
     * Root → a band fed by two data sets: a Groovy one and an LLM one that links to it by the order number.
     */
    protected Report createReportWithLinkedDataSets() {
        Report report = metadata.create(Report.class);
        report.setName("Linked data sets report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setMultiDataSet(false);
        rootBand.setPosition(0);

        BandDefinition ordersBand = metadata.create(BandDefinition.class);
        ordersBand.setReport(report);
        ordersBand.setName("Orders");
        ordersBand.setOrientation(Orientation.HORIZONTAL);
        ordersBand.setMultiDataSet(true);
        ordersBand.setPosition(0);
        ordersBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(ordersBand);

        DataSet orders = metadata.create(DataSet.class);
        orders.setName("Orders");
        orders.setBandDefinition(ordersBand);
        orders.setType(DataSetType.GROOVY);
        orders.setText("""
                return [["orderNumber": "A-1", "customer": "Acme"], ["orderNumber": "A-2", "customer": "Globex"]]""");

        DataSet amounts = metadata.create(DataSet.class);
        amounts.setName("Amounts");
        amounts.setBandDefinition(ordersBand);
        amounts.setType(DataSetType.LLM);
        amounts.setText("Amount of every order");
        amounts.setLinkParameterName("orderNumber");
        amounts.setLlmGeneratedQuery("""
                {"jpql":"select o.number as orderNumber, o.amount as amount from sales_Order o",\
                "resultProperties":["orderNumber","amount"]}""");

        ordersBand.setDataSets(List.of(orders, amounts));
        report.setBands(Set.of(rootBand, ordersBand));

        ReportTemplate template = csvTemplate(report, "Number,Customer,Amount\n${orderNumber},${customer},${amount}\n");
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        report.setXml(reportsSerialization.convertToString(report));
        return report;
    }

    protected ReportTemplate csvTemplate(Report report, String content) {
        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.CSV);
        template.setName("LlmReport.csv");
        template.setContent(content.getBytes(StandardCharsets.UTF_8));
        return template;
    }

    protected Report createReport() {
        Report report = metadata.create(Report.class);
        report.setName("LLM band report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setMultiDataSet(false);
        rootBand.setPosition(0);

        // The CSV formatter takes its rows from the root band's children, so the data set lives on a child.
        BandDefinition ordersBand = metadata.create(BandDefinition.class);
        ordersBand.setReport(report);
        ordersBand.setName("Orders");
        ordersBand.setOrientation(Orientation.HORIZONTAL);
        ordersBand.setMultiDataSet(false);
        ordersBand.setPosition(0);
        ordersBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(ordersBand);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Orders");
        dataSet.setBandDefinition(ordersBand);
        dataSet.setType(DataSetType.LLM);
        dataSet.setText("Order numbers with their amounts");
        dataSet.setLlmGeneratedQuery("""
                {"jpql":"select o.number as orderNumber, o.amount as amount from sales_Order o",\
                "resultProperties":["orderNumber","amount"]}""");
        ordersBand.setDataSets(List.of(dataSet));
        report.setBands(Set.of(rootBand, ordersBand));

        ReportTemplate template = csvTemplate(report, "Number,Amount\n${orderNumber},${amount}\n");
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        report.setXml(reportsSerialization.convertToString(report));
        return report;
    }
}
