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

import io.jmix.core.*;
import io.jmix.reports.ReportExecutionHistoryRecorder;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Nullable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@TestPropertySource(properties = {
        "jmix.reports.history-recording-enabled=true",
        "jmix.reports.save-output-documents-to-history=true",
        "jmix.reports.history-cleanup-max-days=2",
        "jmix.reports.history-cleanup-max-items-per-report=0"
})
public class ExecutionHistoryCleanByDaysTest {
    @Autowired
    FetchPlans fetchPlans;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    UnconstrainedDataManager unconstrainedDataManager;
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

    protected FetchPlan fetchPlan;

    @BeforeEach
    public void setup() {
        fetchPlan = fetchPlans.builder(ReportExecution.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("outputDocument", FetchPlan.LOCAL)
                .build();

        if (annotatedReportHolder.getAllReports().isEmpty()) {
            annotatedReportScanner.importGroupDefinitions();
            annotatedReportScanner.importReportDefinitions();
        }
    }

    @AfterEach
    public void cleanup() {
        jdbcTemplate.update("delete from REPORT_EXECUTION");
    }


    @Test
    public void testCleanByDays() {
        // given
        String reportCode = ReportForHistory.CODE;

        for (int i = 0; i < 2; i++) {
            reportRunner.byReportCode(reportCode)
                    .run();
        }

        List<ReportExecution> executionList = loadExecutions(ReportForHistory.CODE);
        assertThat(executionList).hasSize(2);

        ReportExecution item1 = executionList.get(0);
        FileStorage fileStorage = fileStorageLocator.getDefault();
        assertThat(fileStorage.fileExists(item1.getOutputDocument())).isTrue();

        // when
        // make first item too old
        item1.setStartTime(DateUtils.addDays(item1.getStartTime(), -2));
        unconstrainedDataManager.save(item1);

        String deleted = executionHistoryRecorder.cleanupHistory();

        // then
        assertThat(deleted).isEqualTo("1");
        assertThat(reload(item1)).isNull();
        assertThat(fileStorage.fileExists(item1.getOutputDocument())).isFalse();

        ReportExecution item2 = executionList.get(1);
        assertThat(reload(item2)).isNotNull();
        assertThat(fileStorage.fileExists(item2.getOutputDocument())).isTrue();
    }

    private List<ReportExecution> loadExecutions(String reportCode) {
        return unconstrainedDataManager.load(ReportExecution.class)
                .query("select e from report_ReportExecution e" +
                       " where e.reportCode = :code order by e.startTime asc")
                .parameter("code", reportCode)
                .fetchPlan(fetchPlan)
                .list();
    }

    @Nullable
    private ReportExecution reload(ReportExecution execution) {
        return unconstrainedDataManager.load(Id.of(execution))
                .optional()
                .orElse(null);
    }
}
