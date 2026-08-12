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

package llm_designer.test_support;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Builds a saved report whose data band is fed by an LLM data set with a stored query, so the designer can be
 * opened on it.
 */
@Component
public class LlmReportUtil {

    public static final String REPORT_NAME = "LLM data set report";
    public static final String REPORT_CODE = "llm-data-set-report";
    public static final String DATA_BAND_NAME = "Orders";
    public static final String PROMPT = "Order numbers of this month";

    public static final String STORED_QUERY = """
            {"jpql":"select o.number as orderNumber, o.customer as customerName from sales_Order o",\
            "resultProperties":["orderNumber","customerName"],\
            "explanation":"All order numbers"}""";

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    protected ReportsSerialization reportsSerialization;
    @Autowired
    protected ReportsPersistence reportsPersistence;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * The same report, but with nothing generated yet — a data set a report author has only written a prompt for.
     */
    public Report createAndSaveReportWithoutStoredQuery() {
        return createAndSaveReportWithLlmDataSet(null);
    }

    /**
     * Reads the prompt back from the database, to tell a saved report from one validation refused to save.
     */
    public String loadStoredPrompt() {
        return loadDataSet().getText();
    }

    /**
     * Reads the stored query document back from the database, to tell what the designer wrote into the data set.
     */
    @Nullable
    public String loadStoredQuery() {
        return loadDataSet().getLlmGeneratedQuery();
    }

    protected DataSet loadDataSet() {
        Report saved = unconstrainedDataManager.load(Report.class)
                .query("select r from report_Report r where r.name = :name")
                .parameter("name", REPORT_NAME)
                .one();

        Report structure = reportsSerialization.convertToReport(saved.getXml());
        return structure.getBands().stream()
                .filter(band -> DATA_BAND_NAME.equals(band.getName()))
                .findFirst()
                .orElseThrow()
                .getDataSets()
                .get(0);
    }

    public Report createAndSaveReportWithLlmDataSet() {
        return createAndSaveReportWithLlmDataSet(STORED_QUERY);
    }

    protected Report createAndSaveReportWithLlmDataSet(@Nullable String storedQuery) {
        Report report = unconstrainedDataManager.create(Report.class);
        report.setName(REPORT_NAME);
        // The designer refuses to save a report without a code, so the fixture has one.
        report.setCode(REPORT_CODE);

        BandDefinition rootBand = unconstrainedDataManager.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setMultiDataSet(false);
        rootBand.setPosition(0);

        BandDefinition dataBand = unconstrainedDataManager.create(BandDefinition.class);
        dataBand.setReport(report);
        dataBand.setName(DATA_BAND_NAME);
        dataBand.setOrientation(Orientation.HORIZONTAL);
        dataBand.setMultiDataSet(false);
        dataBand.setPosition(0);
        dataBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(dataBand);

        DataSet dataSet = unconstrainedDataManager.create(DataSet.class);
        dataSet.setName(DATA_BAND_NAME);
        dataSet.setBandDefinition(dataBand);
        dataSet.setType(DataSetType.LLM);
        dataSet.setText(PROMPT);
        dataSet.setLlmGeneratedQuery(storedQuery);
        dataBand.setDataSets(List.of(dataSet));

        report.setBands(Set.of(rootBand, dataBand));

        ReportTemplate template = unconstrainedDataManager.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.CSV);
        template.setName("LlmReport.csv");
        template.setContent("Number\n${orderNumber}\n".getBytes(StandardCharsets.UTF_8));
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        report.setXml(reportsSerialization.convertToString(report));

        return reportsPersistence.save(report);
    }

    public void cleanupDatabaseReports() {
        jdbcTemplate.update("update REPORT_REPORT set DEFAULT_TEMPLATE_ID = null");
        jdbcTemplate.update("delete from REPORT_TEMPLATE");
        jdbcTemplate.update("delete from REPORT_REPORT");
    }
}
