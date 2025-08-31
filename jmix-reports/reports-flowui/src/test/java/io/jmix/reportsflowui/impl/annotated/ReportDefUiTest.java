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

package io.jmix.reportsflowui.impl.annotated;

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reportsflowui.test_support.RuntimeReportUtil;
import io.jmix.reportsflowui.test_support.report.ReportWithRoles;
import io.jmix.reportsflowui.test_support.report.SimpleReportGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportDefUiTest extends BaseReportListUiTest {
    @Autowired
    protected AnnotatedReportHolder reportHolder;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;
    @Autowired
    protected RuntimeReportUtil runtimeReportUtil;

    private DataGridItems<Report> dataGridItems;

    @BeforeEach
    public void setUp() {
        dataGridItems = getDataGridItems();
    }

    @Test
    public void testDataGridFilling() {
        Report report = reportHolder.getByCode(ReportWithRoles.CODE);

        assertThat(dataGridItems).isNotNull();
        assertThat(report).isNotNull();

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.containsItem(report))
                .isInstanceOf(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    public void testName() {
        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems()).anyMatch(r -> r.getName().equals(ReportWithRoles.NAME));
    }

    @Test
    public void testCode() {
        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems()).anyMatch(r -> r.getCode().equals(ReportWithRoles.CODE));
    }

    @Test
    public void testDescription() {
        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems())
                .filteredOn(r -> r.getDescription() != null)
                .anyMatch(r -> r.getDescription().contains("Report description"));
    }

    @Test
    public void testGroup() {
        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems())
                .filteredOn(r -> Objects.nonNull(r.getGroup()))
                .anyMatch(r -> r.getGroup().getCode().equals(SimpleReportGroup.CODE));
    }

    @Test
    public void testSource() {
        Report report = runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        assertThat(report.getName()).isEqualTo(RuntimeReportUtil.SIMPLE_RUNTIME_REPORT_NAME);

        DataGridItems<Report> dataGridItems = getDataGridItems();

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems())
                .anyMatch(r -> r.getSource().equals(ReportSource.DATABASE));
        assertThat(dataGridItems.getItems())
                .anyMatch(r -> r.getSource().equals(ReportSource.ANNOTATED_CLASS));

        runtimeReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testSystem() {
        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems())
                .anyMatch(r -> r.getSystem().equals(true));
        assertThat(dataGridItems.getItems())
                .anyMatch(r -> r.getSystem().equals(false));
    }

    @Test
    public void testViewRole() {
        DataGridItems<Report> dataGridItems = systemAuthenticator.withUser(
                "with-no-access-user",
                this::getDataGridItems);

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems()).size().isEqualTo(0);
    }
}
