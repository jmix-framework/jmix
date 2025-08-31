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

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.view.report.ReportListView;

public class BaseReportListUiTest extends BaseReportUiTest {

    protected ReportListView reportListView;

    protected DataGrid<Report> getMainDataGrid() {
        viewNavigators.view(UiTestUtils.getCurrentView(), ReportListView.class).navigate();
        reportListView = UiTestUtils.getCurrentView();

        return findComponent(reportListView, "reportsDataGrid");
    }

    protected DataGridItems<Report> getDataGridItems() {
        return getMainDataGrid().getItems();
    }
}
