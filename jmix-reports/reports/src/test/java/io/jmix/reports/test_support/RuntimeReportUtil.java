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

package io.jmix.reports.test_support;

import io.jmix.core.UnconstrainedDataManager;
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

    public void cleanupDatabaseReports() {
        jdbcTemplate.update("update REPORT_REPORT set DEFAULT_TEMPLATE_ID = null");
        jdbcTemplate.update("delete from REPORT_TEMPLATE");
        jdbcTemplate.update("delete from REPORT_REPORT");
    }

}
