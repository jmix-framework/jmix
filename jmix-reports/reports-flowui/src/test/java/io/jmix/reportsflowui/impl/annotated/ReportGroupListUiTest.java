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
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.reports.ReportGroupRepository;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reportsflowui.test_support.RuntimeReportGroupUtil;
import io.jmix.reportsflowui.test_support.report.SampleReportGroup;
import io.jmix.reportsflowui.view.group.ReportGroupListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportGroupListUiTest extends BaseReportListUiTest<ReportGroup, ReportGroupListView> {

    @Autowired
    protected ReportGroupRepository groupRepository;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;
    @Autowired
    protected RuntimeReportGroupUtil runtimeReportGroupUtil;
    @Autowired
    protected AnnotatedReportScanner reportScanner;
    @Autowired
    protected AnnotatedReportGroupHolder reportGroupHolder;

    protected ReportGroupListUiTest() {
        super("reportGroupsDataGrid", ReportGroupListView.class);
    }

    @BeforeEach
    public void setUp() {
        if (reportGroupHolder.getAllGroups().isEmpty()) {
            reportScanner.importGroupDefinitions();
        }

        dataGridItems = getDataGridItems();
    }

    @AfterEach
    public void tearDown() {
        runtimeReportGroupUtil.cleanupDatabaseReportGroups();
    }

    @Test
    public void testDataGridFilling() {
        List<ReportGroup> reportGroups = groupRepository.loadAll();

        assertThat(reportGroups).isNotNull();
        assertThat(reportGroups).size().isEqualTo(1);
        assertThat(reportGroups).anyMatch(rg -> rg.getCode().equals(SampleReportGroup.CODE));
        assertThat(reportGroups).allMatch(rg -> groupRepository.existsGroupByCode(rg.getCode()));

        runtimeReportGroupUtil.createAndSaveSimpleReportGroup();

        dataGridItems = getDataGridItems();
        reportGroups = dataGridItems.getItems().stream().toList();

        assertThat(reportGroups).size().isEqualTo(2);
        assertThat(reportGroups).anyMatch(rg -> rg.getCode().equals(RuntimeReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_CODE));
        assertThat(reportGroups).allMatch(rg -> groupRepository.existsGroupByCode(rg.getCode()));
    }

    @Test
    public void testDataGridColumns() {
        assertThat(dataGridItems).isNotNull();
        List<ReportGroup> reportGroupList = (List<ReportGroup>) dataGridItems.getItems();

        DataGrid<ReportGroup> dataGrid = getMainDataGrid();

        // title column
        DataGridColumn<ReportGroup> column = dataGrid.getColumnByKey("title");
        assertThat(column).isNotNull();

        assertThat(reportGroupList).isNotEmpty();
        assertThat(reportGroupList)
                .anyMatch(rg -> rg.getTitle().equals(SampleReportGroup.TITLE));

        // code column
        column = dataGrid.getColumnByKey("code");
        assertThat(column).isNotNull();
        assertThat(reportGroupList).anyMatch(rg -> rg.getCode().equals(SampleReportGroup.CODE));

        // system column
        column = dataGrid.getColumnByKey("systemFlag");
        assertThat(column).isNotNull();
        assertThat(reportGroupList)
                .anyMatch(rg -> rg.getSystemFlag().equals(true));
        // ?
//        assertThat(reportGroupList)
//                .anyMatch(rg -> rg.getSystemFlag().equals(false));

        // source column
        column = dataGrid.getColumnByKey("source");
        assertThat(column).isNotNull();
    }

    @Test
    public void testDataGridSourceColumn() {
        ReportGroup reportGroup = runtimeReportGroupUtil.createAndSaveSimpleReportGroup();

        assertThat(reportGroup.getTitle()).isEqualTo(RuntimeReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_TITLE);

        dataGridItems = getDataGridItems();

        assertThat(dataGridItems).isNotNull();

        List<ReportGroup> reportGroupList = (List<ReportGroup>) dataGridItems.getItems();

        assertThat(reportGroupList).isNotEmpty();
        assertThat(reportGroupList)
                .anyMatch(r -> r.getSource().equals(ReportSource.DATABASE));
        assertThat(reportGroupList)
                .anyMatch(r -> r.getSource().equals(ReportSource.ANNOTATED_CLASS));
    }

    @Test
    public void testTitleFilter() {
        // because the reports group table was cleared
        // and this test was run the calling reportGroupUtil.cleanupDatabaseReportGroups() method
//        reportScanner.importGroupDefinitions();
        runtimeReportGroupUtil.createAndSaveSimpleReportGroup();

        dataGridItems = getDataGridItems();

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems()).size().isEqualTo(2);

        TypedTextField<String> titleFilterField = findComponent(listView, "titleFilter");
        titleFilterField.setValue("sample");


        List<ReportGroup> reportList = (List<ReportGroup>) dataGridItems.getItems();
        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);

        //reportGroupUtil.cleanupDatabaseReportGroups();
    }

    @Test
    public void testSystemCodeFilter() {
        runtimeReportGroupUtil.createAndSaveSimpleReportGroup();

        dataGridItems = getDataGridItems();

        assertThat(dataGridItems).isNotNull();
        assertThat(dataGridItems.getItems()).size().isEqualTo(3);

        TypedTextField<String> codeFilterField = findComponent(listView, "codeFilter");
        codeFilterField.setValue("simple");

        List<ReportGroup> reportList = (List<ReportGroup>) dataGridItems.getItems();

        assertThat(reportList).isNotEmpty();
        assertThat(reportList).size().isEqualTo(1);
    }
}
