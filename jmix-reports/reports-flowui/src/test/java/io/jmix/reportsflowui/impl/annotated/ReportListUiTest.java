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
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reportsflowui.test_support.RuntimeReportUtil;
import io.jmix.reportsflowui.test_support.report.ReportWithRoles;
import io.jmix.reportsflowui.test_support.report.SampleReportGroup;
import io.jmix.reportsflowui.view.report.ReportListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportListUiTest extends BaseReportListUiTest<Report, ReportListView> {

    @Autowired
    protected AnnotatedReportGroupHolder annotatedGroupHolder;
    @Autowired
    protected AnnotatedReportHolder annotatedReportHolder;
    @Autowired
    protected AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    protected RuntimeReportUtil runtimeReportUtil;
    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;

    protected ReportListUiTest() {
        super("reportsDataGrid", ReportListView.class);
    }

    @BeforeEach
    public void setUp() {
        if (annotatedReportHolder.getAllReports().isEmpty()) {
            annotatedReportScanner.importReportDefinitions();
        }

        dataGridItems = getDataGridItems();
    }

    @AfterEach
    public void tearDown() {
        runtimeReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testDataGridFilling() {
        Report report = annotatedReportHolder.getByCode(ReportWithRoles.CODE);

        assertThat(dataGridItems).isNotNull();
        assertThat(report).isNotNull();

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.containsItem(report))
                .isInstanceOf(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    public void testDataGridColumns() {
        assertThat(dataGridItems).isNotNull();

        DataGrid<Report> dataGrid = getMainDataGrid();

        // name column
        DataGridColumn<Report> column = dataGrid.getColumnByKey("name");
        assertThat(column).isNotNull();

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).anyMatch(r -> r.getName().equals(ReportWithRoles.NAME));

        // code column
        column = dataGrid.getColumnByKey("name");
        assertThat(column).isNotNull();

        assertThat(reportList).anyMatch(r -> r.getCode().equals(ReportWithRoles.CODE));

        // description column
        column = dataGrid.getColumnByKey("description");
        assertThat(column).isNotNull();

        assertThat(reportList)
                .filteredOn(r -> r.getDescription() != null)
                .anyMatch(r -> r.getDescription().contains("Report description"));

        // group column
        column = dataGrid.getColumnByKey("group");
        assertThat(column).isNotNull();

        assertThat(reportList)
                .filteredOn(r -> Objects.nonNull(r.getGroup()))
                .anyMatch(r -> r.getGroup().getCode().equals(SampleReportGroup.CODE));

        // system column
        column = dataGrid.getColumnByKey("system");
        assertThat(column).isNotNull();

        assertThat(reportList)
                .anyMatch(r -> r.getSystem().equals(true));
        assertThat(reportList)
                .anyMatch(r -> r.getSystem().equals(false));

        // updatedAt column
        column = dataGrid.getColumnByKey("updateTs");
        assertThat(column).isNotNull();
    }

    @Test
    public void testDataGridSourceColumn() {
        Report report = runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        assertThat(report.getName()).isEqualTo(RuntimeReportUtil.SIMPLE_RUNTIME_REPORT_NAME);

        dataGridItems = getDataGridItems();

        assertThat(dataGridItems).isNotNull();

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList)
                .anyMatch(r -> r.getSource().equals(ReportSource.DATABASE));
        assertThat(reportList)
                .anyMatch(r -> r.getSource().equals(ReportSource.ANNOTATED_CLASS));
    }

    @Test
    public void testDataGridViewRole() {
        DataGridItems<Report> dataGridItems = systemAuthenticator.withUser(
                "with-no-access-user",
                this::getDataGridItems);

        assertThat(dataGridItems).isNotNull();

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isEmpty();
    }

    @Test
    public void testNameFilter() {
        runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        dataGridItems = getDataGridItems();

        TypedTextField<String> nameFilter = findComponent(listView, "nameFilter");
        nameFilter.setValue("roles");

        assertThat(dataGridItems).isNotNull();

        List<Report> reportList = (List<Report>) dataGridItems.getItems();
        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
    }

    @Test
    public void testGroupFilter() {
        EntityComboBox<ReportGroup> groupFilter = findComponent(listView, "groupFilter");

        List<ReportGroup> reportGroups = groupFilter.getDataProvider().fetch(new Query<>()).toList();
        assertThat(reportGroups).size().isEqualTo(1);

        groupFilter.setValue(annotatedGroupHolder.getGroupByCode(SampleReportGroup.CODE));
        assertThat(groupFilter.getSelectedItems()).anyMatch(r -> r.getCode().equals(SampleReportGroup.CODE));

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
        assertThat(reportList).anyMatch(r -> r.getCode().equals(ReportWithRoles.CODE));
    }

    @Test
    public void testSystemCodeFilter() {
        TypedTextField<String> codeFilterField = findComponent(listView, "codeFilter");
        codeFilterField.setValue("roles");

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
    }

    @Test
    public void testUpdatedAfterFilter() {
        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        TypedDatePicker<Date> updatedDateFilter = findComponent(listView, "updatedDateFilter");

        runtimeReport.setUpdateTs(new Date());
        reportRepository.save(runtimeReport);

        updatedDateFilter.setValue(LocalDate.of(2025, 8, 11));

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
    }

    @Test
    public void testOutputType() {
        JmixSelect<ReportOutputType> outputTypeFilter = findComponent(listView, "outputTypeFilter");
        List<ReportOutputType> reportGroups = outputTypeFilter.getDataProvider().fetch(new Query<>()).toList();

        assertThat(reportGroups).size().isEqualTo(9);

        outputTypeFilter.setValue(ReportOutputType.TABLE);

        List<Report> reportList = (List<Report>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
    }
}
