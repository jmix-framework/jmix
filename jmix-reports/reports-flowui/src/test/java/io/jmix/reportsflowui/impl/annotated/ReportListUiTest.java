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

import com.vaadin.flow.data.provider.Query;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reportsflowui.test_support.RuntimeReportUtil;
import io.jmix.reportsflowui.test_support.report.ReportWithRoles;
import io.jmix.reportsflowui.test_support.report.SimpleReportGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportListUiTest extends BaseReportListUiTest {

    @Autowired
    protected AnnotatedReportGroupHolder annotatedGroupHolder;
    @Autowired
    protected RuntimeReportUtil runtimeReportUtil;
    @Autowired
    protected ReportRepository reportRepository;

    private DataGridItems<Report> dataGridItems;

    @BeforeEach
    public void setUp() {
        dataGridItems = getDataGridItems();
    }

    @Test
    public void testNameFilter() {
        runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        TypedTextField<String> nameFilter = findComponent(reportListView, "nameFilter");
        nameFilter.setValue("roles");

        assertThat(dataGridItems.getItems()).size().isEqualTo(1);

        runtimeReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testGroupFilter() {
        EntityComboBox<ReportGroup> groupFilter = findComponent(reportListView, "groupFilter");

        List<ReportGroup> reportGroups = groupFilter.getDataProvider().fetch(new Query<>()).toList();
        assertThat(reportGroups).size().isEqualTo(2);

        groupFilter.setValue(annotatedGroupHolder.getGroupByCode(SimpleReportGroup.CODE));
        assertThat(groupFilter.getSelectedItems()).anyMatch(r -> r.getCode().equals(SimpleReportGroup.CODE));

        assertThat(dataGridItems.getItems().size()).isEqualTo(1);
        assertThat(dataGridItems.getItems()).anyMatch(r -> r.getCode().equals(ReportWithRoles.CODE));
    }

    @Test
    public void testSystemCodeFilter() {
        TypedTextField<String> codeFilterField = findComponent(reportListView, "codeFilter");
        codeFilterField.setValue("roles");

        assertThat(dataGridItems.getItems().size()).isEqualTo(1);
    }

    @Test
    public void testUpdatedAfter() {
        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        TypedDatePicker<Date> updatedDateFilter = findComponent(reportListView, "updatedDateFilter");

        runtimeReport.setUpdateTs(new Date());
        reportRepository.save(runtimeReport);

        updatedDateFilter.setValue(LocalDate.of(2025, 8, 11));

        assertThat(dataGridItems.getItems()).size().isEqualTo(1);
    }

    @Test
    public void testOutputType() {
        JmixSelect<ReportOutputType> outputTypeFilter = findComponent(reportListView, "outputTypeFilter");
        List<ReportOutputType> reportGroups = outputTypeFilter.getDataProvider().fetch(new Query<>()).toList();

        assertThat(reportGroups).size().isEqualTo(9);

        outputTypeFilter.setValue(ReportOutputType.TABLE);

        assertThat(dataGridItems.getItems()).size().isEqualTo(1);
    }
}
