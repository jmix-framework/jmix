/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.execution;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.reports.ReportExecutionHistoryRecorder;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.RuntimeReportUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "jmix.reports.history-recording-enabled=true",
        "jmix.reports.save-output-documents-to-history=true",
        "jmix.reports.history-cleanup-max-days=0",
        "jmix.reports.history-cleanup-max-items-per-report=2"
})
public class ExecutionHistoryCleanPerReportTest extends BaseExecutionHistoryTest {
    @Autowired
    FileStorageLocator fileStorageLocator;
    @Autowired
    ReportExecutionHistoryRecorder executionHistoryRecorder;
    @Autowired
    AnnotatedReportHolder annotatedReportHolder;
    @Autowired
    AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    ReportRunner reportRunner;
    @Autowired
    RuntimeReportUtil runtimeReportUtil;

    @BeforeEach
    public void setup() {
        if (annotatedReportHolder.getAllReports().isEmpty()) {
            annotatedReportScanner.importGroupDefinitions();
            annotatedReportScanner.importReportDefinitions();
        }
    }

    @AfterEach
    public void cleanupDatabaseReports() {
        cleanup();
        runtimeReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testCleanItemsPerAnnotatedReport() {
        // given
        String reportCode = ReportForHistory.CODE;

        for (int i = 0; i < 4; i++) {
            reportRunner.byReportCode(reportCode)
                    .run();
        }

        List<ReportExecution> executions = loadExecutions(ReportForHistory.CODE);
        assertThat(executions).hasSize(4);
        FileStorage fileStorage = fileStorageLocator.getDefault();

        // when
        String deleted = executionHistoryRecorder.cleanupHistory();

        // then
        assertThat(deleted).isEqualTo("2");

        assertThat(reload(executions.get(0))).isNull();
        assertThat(fileStorage.fileExists(executions.get(0).getOutputDocument())).isFalse();

        assertThat(reload(executions.get(1))).isNull();
        assertThat(fileStorage.fileExists(executions.get(1).getOutputDocument())).isFalse();

        assertThat(reload(executions.get(2))).isNotNull();
        assertThat(fileStorage.fileExists(executions.get(2).getOutputDocument())).isTrue();
    }

    @Test
    public void testCleanItemsPerDatabaseReport() {
        // given
        Report report = runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        for (int i = 0; i < 3; i++) {
            reportRunner.byReportEntity(report)
                    .run();
        }

        List<ReportExecution> executions = loadExecutionsByName(RuntimeReportUtil.SIMPLE_RUNTIME_REPORT_NAME);
        assertThat(executions).hasSize(3);
        FileStorage fileStorage = fileStorageLocator.getDefault();

        // when
        String deleted = executionHistoryRecorder.cleanupHistory();

        // then
        assertThat(deleted).isEqualTo("1");

        assertThat(reload(executions.get(0))).isNull();
        assertThat(fileStorage.fileExists(executions.get(0).getOutputDocument())).isFalse();

        assertThat(reload(executions.get(1))).isNotNull();
        assertThat(fileStorage.fileExists(executions.get(1).getOutputDocument())).isTrue();

        assertThat(reload(executions.get(2))).isNotNull();
        assertThat(fileStorage.fileExists(executions.get(2).getOutputDocument())).isTrue();
    }
}
