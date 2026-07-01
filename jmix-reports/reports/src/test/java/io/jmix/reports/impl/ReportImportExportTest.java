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

package io.jmix.reports.impl;

import io.jmix.core.DataManager;
import io.jmix.core.Resources;
import io.jmix.reports.ReportImportExport;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.Report;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@DisabledIf(expression = "#{systemEnvironment['JMIX_ECLIPSELINK_DISABLELAZYLOADING'] == 'true'}", reason = "Test disabled by 'jmix.eclipselink.disable-lazy-loading'")
public class ReportImportExportTest {

    static final String JMIX_FIXTURE_PATH = "classpath:test_support/test-report.zip";
    static final String JMIX_REPORT_CODE = "rt-test-1";

    static final String CUBA_FIXTURE_PATH = "classpath:test_support/test-cuba-report.zip";
    static final String CUBA_REPORT_NAME = "Report for entity \"Time Entry\"";

    static final String CUBA_FIXTURE_PATH_2 = "classpath:test_support/test-cuba-report-2.zip";
    static final String CUBA_REPORT_CODE_2 = "test";

    static final String CONFLICTING_CODE_REPORT_NAME = "Report with conflicting code";
    static final String EXISTING_REPORT_CODE = "code_1";
    static final String GENERATED_JMIX_REPORT_CODE = "rt-test-1-1";

    @Autowired
    ReportImportExport reportImportExport;
    @Autowired
    DataManager dataManager;
    @Autowired
    Resources resources;
    @Autowired
    ReportsPersistence reportsPersistence;

    @AfterEach
    void tearDown() {
        dataManager.remove(loadReportsByCode());
        dataManager.remove(loadReportsByCode(CUBA_REPORT_CODE_2));
        dataManager.remove(loadReportsByCode(EXISTING_REPORT_CODE));
        dataManager.remove(loadReportsByCode(GENERATED_JMIX_REPORT_CODE));
        dataManager.remove(loadReportsByName());
    }

    @Test
    void testImportReportsFromZipFile() throws IOException {
        assertThat(loadReportsByCode()).isEmpty();

        String zipPath = resources.getResource(JMIX_FIXTURE_PATH).getFile().getAbsolutePath();
        Collection<Report> importedReports = reportImportExport.importReports(zipPath);

        assertThat(importedReports).isNotEmpty();
        assertThat(importedReports)
                .anyMatch(report -> JMIX_REPORT_CODE.equals(report.getCode()));

        List<Report> dbReports = loadReportsByCode();
        assertThat(dbReports).hasSize(1);
        assertThat(dbReports.get(0).getCode()).isEqualTo(JMIX_REPORT_CODE);
        assertThat(dbReports.get(0).getTemplates()).isNotEmpty();
    }

    @Test
    void testImportCubaReportFromZipFile() throws IOException {
        assertThat(loadReportsByName()).isEmpty();

        String zipPath = resources.getResource(CUBA_FIXTURE_PATH).getFile().getAbsolutePath();
        Collection<Report> importedReports = reportImportExport.importReports(zipPath);

        assertThat(importedReports).isNotEmpty();
        assertThat(importedReports)
                .anyMatch(report -> CUBA_REPORT_NAME.equals(report.getName()));

        List<Report> dbReports = loadReportsByName();
        assertThat(dbReports).hasSize(1);

        Report importedReport = dbReports.get(0);
        assertThat(importedReport.getTemplates()).isNotEmpty();
        assertThat(importedReport.getDefaultTemplate()).isNotNull();

        UUID importedReportId = importedReport.getId();
        Report reloadedReport = dataManager.load(Report.class)
                .id(importedReportId)
                .one();
        assertThat(reloadedReport).isNotNull();
    }

    @Test
    void testImportCubaReportWithSpaceSeparatedTimestamp() throws IOException {
        assertThat(loadReportsByCode(CUBA_REPORT_CODE_2)).isEmpty();

        String zipPath = resources.getResource(CUBA_FIXTURE_PATH_2).getFile().getAbsolutePath();
        Collection<Report> importedReports = reportImportExport.importReports(zipPath);

        assertThat(importedReports).isNotEmpty();
        assertThat(importedReports)
                .anyMatch(report -> CUBA_REPORT_CODE_2.equals(report.getCode()));

        List<Report> dbReports = loadReportsByCode(CUBA_REPORT_CODE_2);
        assertThat(dbReports).hasSize(1);

        Report importedReport = dbReports.get(0);
        assertThat(importedReport.getCreateTs()).isNotNull();
        assertThat(importedReport.getUpdateTs()).isNotNull();
        assertThat(importedReport.getDefaultTemplate()).isNotNull();
        assertThat(importedReport.getDefaultTemplate().getCreateTs()).isNotNull();
        assertThat(importedReport.getDefaultTemplate().getUpdateTs()).isNotNull();
        assertThat(importedReport.getGroup()).isNotNull();
        assertThat(importedReport.getGroup().getCreateTs()).isNotNull();
    }

    @Test
    void testImportExistingReportWithCodeUsedByAnotherReport() throws IOException {
        String zipPath = resources.getResource(JMIX_FIXTURE_PATH).getFile().getAbsolutePath();
        Report existingReport = importSingleReport(zipPath);
        existingReport.setCode(EXISTING_REPORT_CODE);
        reportsPersistence.save(existingReport);
        Report conflictingReport = dataManager.create(Report.class);
        conflictingReport.setName(CONFLICTING_CODE_REPORT_NAME);
        conflictingReport.setCode(JMIX_REPORT_CODE);
        reportsPersistence.save(conflictingReport);

        Collection<Report> importedReports = reportImportExport.importReports(zipPath);

        assertThat(importedReports)
                .singleElement()
                .extracting(Report::getCode)
                .isEqualTo(GENERATED_JMIX_REPORT_CODE);

        Report updatedReport = dataManager.load(Report.class)
                .id(existingReport.getId())
                .one();
        assertThat(updatedReport.getCode()).isEqualTo(GENERATED_JMIX_REPORT_CODE);
        assertThat(loadReportsByCode(JMIX_REPORT_CODE)).hasSize(1);
        assertThat(loadReportsByCode(GENERATED_JMIX_REPORT_CODE)).hasSize(1);
    }

    List<Report> loadReportsByCode() {
        return loadReportsByCode(JMIX_REPORT_CODE);
    }

    List<Report> loadReportsByCode(String code) {
        return dataManager.load(Report.class)
                .query("select r from report_Report r where r.code = :code")
                .parameter("code", code)
                .list();
    }

    List<Report> loadReportsByName() {
        return dataManager.load(Report.class)
                .query("select r from report_Report r where r.name = :name")
                .parameter("name", CUBA_REPORT_NAME)
                .list();
    }

    Report importSingleReport(String zipPath) {
        Collection<Report> importedReports = reportImportExport.importReports(zipPath);
        assertThat(importedReports).hasSize(1);
        return importedReports.iterator().next();
    }
}
