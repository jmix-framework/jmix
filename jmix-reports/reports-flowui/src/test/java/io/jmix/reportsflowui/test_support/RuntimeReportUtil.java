/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reportsflowui.test_support;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class RuntimeReportUtil {

    public static final String SIMPLE_RUNTIME_REPORT_NAME = "Simple runtime report";
    public static final String SIMPLE_SPREADSHEET_REPORT_CODE = "SIMPLE_SPREADSHEET_RUNTIME_REPORT";
    public static final String ALTERABLE_SPREADSHEET_REPORT_CODE = "ALTERABLE_SPREADSHEET_RUNTIME_REPORT";
    public static final String MULTI_TEMPLATE_SPREADSHEET_REPORT_CODE = "MULTI_TEMPLATE_SPREADSHEET_RUNTIME_REPORT";

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected ReportsSerialization reportsSerialization;
    @Autowired
    protected ReportsPersistence reportsPersistence;

    /*
     * Create simple report, not persisted yet, that executes successfully.
     */
    public Report constructSimpleRuntimeReport() {
        Report report = unconstrainedDataManager.create(Report.class);
        report.setName(SIMPLE_RUNTIME_REPORT_NAME);
        // no code

        BandDefinition band = unconstrainedDataManager.create(BandDefinition.class);
        band.setReport(report);
        band.setOrientation(Orientation.HORIZONTAL);
        band.setName("Root");
        band.setMultiDataSet(false);
        band.setPosition(0);

        DataSet dataSet = unconstrainedDataManager.create(DataSet.class);
        dataSet.setName("Root");
        dataSet.setType(DataSetType.GROOVY);
        dataSet.setText("""
                def user = currentAuthentication.getUser()
                def currentDate = timeSource.currentTimestamp()
                return [["username": user.username, "today": currentDate]]
                """);
        band.setDataSets(List.of(dataSet));
        report.setBands(Set.of(band));

        ReportTemplate template = unconstrainedDataManager.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.CSV);
        template.setName("SimpleReport.csv");
        template.setContent("""
                Username, Today
                ${username},${today}
                """
                .getBytes(StandardCharsets.UTF_8)
        );
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        String xml = reportsSerialization.convertToString(report);
        report.setXml(xml);

        return report;
    }

    /*
     * Create and save to database simple report that executes successfully.
     */
    public Report createAndSaveSimpleRuntimeReport() {
        Report report = constructSimpleRuntimeReport();

        return reportsPersistence.save(report);
    }

    public Report createAndSaveSimpleSpreadsheetRuntimeReport() {
        Report existingReport = unconstrainedDataManager.load(Report.class)
                .condition(PropertyCondition.equal("code", SIMPLE_SPREADSHEET_REPORT_CODE))
                .optional()
                .orElse(null);
        if (existingReport != null) {
            return existingReport;
        }

        Report report = unconstrainedDataManager.create(Report.class);
        report.setName("Simple spreadsheet runtime report");
        report.setCode(SIMPLE_SPREADSHEET_REPORT_CODE);

        BandDefinition band = unconstrainedDataManager.create(BandDefinition.class);
        band.setReport(report);
        band.setOrientation(Orientation.HORIZONTAL);
        band.setName("Root");
        band.setMultiDataSet(false);
        band.setPosition(0);
        report.setBands(Set.of(band));

        ReportInputParameter inputParameter = unconstrainedDataManager.create(ReportInputParameter.class);
        inputParameter.setReport(report);
        inputParameter.setAlias("input");
        inputParameter.setName("Input");
        inputParameter.setPosition(0);
        inputParameter.setType(ParameterType.TEXT);
        inputParameter.setRequired(true);
        report.setInputParameters(List.of(inputParameter));

        ReportTemplate template = unconstrainedDataManager.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.XLSX);
        template.setName("SimpleSpreadsheet.xlsx");
        template.setContent("spreadsheet-template".getBytes(StandardCharsets.UTF_8));
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);

        String xml = reportsSerialization.convertToString(report);
        report.setXml(xml);

        return reportsPersistence.save(report);
    }

    public Report createAndSaveAlterableSpreadsheetRuntimeReport() {
        Report existingReport = unconstrainedDataManager.load(Report.class)
                .condition(PropertyCondition.equal("code", ALTERABLE_SPREADSHEET_REPORT_CODE))
                .optional()
                .orElse(null);
        if (existingReport != null) {
            return existingReport;
        }

        Report report = createSpreadsheetReportBase("Alterable spreadsheet runtime report",
                ALTERABLE_SPREADSHEET_REPORT_CODE);

        ReportTemplate defaultTemplate = unconstrainedDataManager.create(ReportTemplate.class);
        defaultTemplate.setReport(report);
        defaultTemplate.setCode("default");
        defaultTemplate.setReportOutputType(ReportOutputType.CSV);
        defaultTemplate.setName("Default.csv");
        defaultTemplate.setContent("default".getBytes(StandardCharsets.UTF_8));

        ReportTemplate spreadsheetTemplate = unconstrainedDataManager.create(ReportTemplate.class);
        spreadsheetTemplate.setReport(report);
        spreadsheetTemplate.setCode("spreadsheet");
        spreadsheetTemplate.setAlterable(true);
        spreadsheetTemplate.setReportOutputType(ReportOutputType.PDF);
        spreadsheetTemplate.setName("Spreadsheet.jrxml");
        spreadsheetTemplate.setContent("spreadsheet".getBytes(StandardCharsets.UTF_8));

        report.setTemplates(List.of(defaultTemplate, spreadsheetTemplate));
        report.setDefaultTemplate(defaultTemplate);

        report.setXml(reportsSerialization.convertToString(report));
        return reportsPersistence.save(report);
    }

    public Report createAndSaveMultiTemplateSpreadsheetRuntimeReport() {
        Report existingReport = unconstrainedDataManager.load(Report.class)
                .condition(PropertyCondition.equal("code", MULTI_TEMPLATE_SPREADSHEET_REPORT_CODE))
                .optional()
                .orElse(null);
        if (existingReport != null) {
            return existingReport;
        }

        Report report = createSpreadsheetReportBase("Multi-template spreadsheet runtime report",
                MULTI_TEMPLATE_SPREADSHEET_REPORT_CODE);

        ReportTemplate defaultTemplate = unconstrainedDataManager.create(ReportTemplate.class);
        defaultTemplate.setReport(report);
        defaultTemplate.setCode("default");
        defaultTemplate.setReportOutputType(ReportOutputType.CSV);
        defaultTemplate.setName("Default.csv");
        defaultTemplate.setContent("default".getBytes(StandardCharsets.UTF_8));

        ReportTemplate xlsTemplate = unconstrainedDataManager.create(ReportTemplate.class);
        xlsTemplate.setReport(report);
        xlsTemplate.setCode("xlsTemplate");
        xlsTemplate.setReportOutputType(ReportOutputType.XLS);
        xlsTemplate.setName("Spreadsheet.xls");
        xlsTemplate.setContent("xls".getBytes(StandardCharsets.UTF_8));

        ReportTemplate xlsxTemplate = unconstrainedDataManager.create(ReportTemplate.class);
        xlsxTemplate.setReport(report);
        xlsxTemplate.setCode("xlsxTemplate");
        xlsxTemplate.setReportOutputType(ReportOutputType.XLSX);
        xlsxTemplate.setName("Spreadsheet.xlsx");
        xlsxTemplate.setContent("xlsx".getBytes(StandardCharsets.UTF_8));

        report.setTemplates(List.of(defaultTemplate, xlsTemplate, xlsxTemplate));
        report.setDefaultTemplate(defaultTemplate);

        report.setXml(reportsSerialization.convertToString(report));
        return reportsPersistence.save(report);
    }

    protected Report createSpreadsheetReportBase(String name, String code) {
        Report report = unconstrainedDataManager.create(Report.class);
        report.setName(name);
        report.setCode(code);

        BandDefinition band = unconstrainedDataManager.create(BandDefinition.class);
        band.setReport(report);
        band.setOrientation(Orientation.HORIZONTAL);
        band.setName("Root");
        band.setMultiDataSet(false);
        band.setPosition(0);
        report.setBands(Set.of(band));

        ReportInputParameter inputParameter = unconstrainedDataManager.create(ReportInputParameter.class);
        inputParameter.setReport(report);
        inputParameter.setAlias("input");
        inputParameter.setName("Input");
        inputParameter.setPosition(0);
        inputParameter.setType(ParameterType.TEXT);
        inputParameter.setRequired(true);
        report.setInputParameters(List.of(inputParameter));

        return report;
    }

    public void cleanupDatabaseReports() {
        jdbcTemplate.update("update REPORT_REPORT set DEFAULT_TEMPLATE_ID = null");
        jdbcTemplate.update("delete from REPORT_TEMPLATE");
        jdbcTemplate.update("delete from REPORT_REPORT");
    }

}
