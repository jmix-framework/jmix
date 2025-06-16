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

package io.jmix.reports.execution;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Id;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Nullable;
import java.util.List;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public abstract class BaseExecutionHistoryTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    protected FetchPlans fetchPlans;

    protected FetchPlan fetchPlan;

    @BeforeEach
    public void setupFetchPlan() {
        fetchPlan = fetchPlans.builder(ReportExecution.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("outputDocument", FetchPlan.LOCAL)
                .build();
    }

    @AfterEach
    public void cleanup() {
        jdbcTemplate.update("delete from REPORT_EXECUTION");
    }

    protected List<ReportExecution> loadExecutions(String reportCode) {
        return unconstrainedDataManager.load(ReportExecution.class)
                .query("select e from report_ReportExecution e" +
                       " where e.reportCode = :code order by e.startTime asc")
                .parameter("code", reportCode)
                .fetchPlan(fetchPlan)
                .list();
    }

    @Nullable
    protected ReportExecution loadExecution(UnconstrainedDataManager unconstrainedDataManager, String reportCode) {
        return unconstrainedDataManager.load(ReportExecution.class)
                .query("select e from report_ReportExecution e where e.reportCode = :code")
                .parameter("code", reportCode)
                .fetchPlan(fetchPlan)
                .optional()
                .orElse(null);
    }

    @Nullable
    protected ReportExecution reload(ReportExecution execution) {
        return unconstrainedDataManager.load(Id.of(execution))
                .optional()
                .orElse(null);
    }

}
