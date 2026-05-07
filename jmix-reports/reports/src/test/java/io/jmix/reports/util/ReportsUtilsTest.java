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

package io.jmix.reports.util;

import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.RuntimeReportUtil;
import io.jmix.reports.test_support.report.GameCriticScoresReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
class ReportsUtilsTest {

    @Autowired
    ReportsUtils reportsUtils;
    @Autowired
    ReportsPersistence reportsPersistence;
    @Autowired
    RuntimeReportUtil runtimeReportUtil;
    @Autowired
    AnnotatedReportScanner annotatedReportScanner;

    @AfterEach
    void tearDown() {
        runtimeReportUtil.cleanupDatabaseReports();
    }

    @Test
    void testGenerateReportCodeForCopiedReport() {
        Report report = runtimeReportUtil.constructSimpleRuntimeReport();
        report.setCode("simple-runtime-report");
        reportsPersistence.save(report);

        assertThat(reportsUtils.generateReportCode("simple-runtime-report"))
                .isEqualTo("simple-runtime-report-1");
    }

    @Test
    void testGenerateReportCodeByNameReturnsSanitizedCode() {
        assertThat(reportsUtils.generateReportCodeByName(RuntimeReportUtil.SIMPLE_RUNTIME_REPORT_NAME))
                .isEqualTo("simple-runtime-report");
    }

    @Test
    void testGenerateReportCodeChecksDesignTimeReports() {
        annotatedReportScanner.importReportDefinitions();

        assertThat(reportsUtils.generateReportCode(GameCriticScoresReport.CODE))
                .isEqualTo(GameCriticScoresReport.CODE + "-1");
    }

    @Test
    void testGenerateReportCodeByNameUsesDefaultCode() {
        assertThat(reportsUtils.generateReportCodeByName("   "))
                .isEqualTo("report");
    }

    @Test
    void testGenerateReportCodeByNameUsesCustomDefaultCode() {
        assertThat(reportsUtils.generateReportCodeByName("   ", "imported-report"))
                .isEqualTo("imported-report");
    }
}
