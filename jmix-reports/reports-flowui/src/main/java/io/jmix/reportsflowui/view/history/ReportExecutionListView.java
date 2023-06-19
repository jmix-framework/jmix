/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsflowui.view.history;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "reports/executions", layout = DefaultMainViewParent.class)
@ViewController("report_ReportExecution.list")
@ViewDescriptor("report-execution-list-view.xml")
@LookupComponent("executionsDataGrid")
@DialogMode(width = "80em", height = "65em", resizable = true)
public class ReportExecutionListView extends StandardListView<ReportExecution> {

    @ViewComponent
    protected CollectionLoader<ReportExecution> executionsDl;
    @ViewComponent
    protected DataGrid<ReportExecution> executionsDataGrid;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected SecondsToTextFormatter durationFormatter;

    protected List<Report> filterByReports;

    @Subscribe
    protected void onInit(InitEvent event) {
        executionsDataGrid.addColumn(reportExecution -> durationFormatter.apply(reportExecution.getExecutionTimeSec()))
                .setHeader(messageBundle.getMessage("history.executionTimeSec.header"))
                .setKey("executionTimeSec")
                .setSortable(true)
                .setResizable(true);

        executionsDataGrid.addColumn(reportExecution ->
                        reportExecution.getOutputDocument() != null
                                ? reportExecution.getOutputDocument().getFileName()
                                : ""
                )
                .setHeader(messageBundle.getMessage("history.outputDocument.header"))
                .setKey("outputDocument")
                .setSortable(true)
                .setResizable(true);
    }


    @Subscribe("executionsDataGrid.download")
    public void onDownloadClick(final ActionPerformedEvent event) {
        ReportExecution execution = executionsDataGrid.getSingleSelectedItem();
        if (execution != null && execution.getOutputDocument() != null) {
            FileRef fileRef = execution.getOutputDocument();
            downloader.download(fileRef);
        }
    }

    @Install(to = "executionsDataGrid.download", subject = "enabledRule")
    protected boolean reportsDataGridImportEnabledRule() {
        return downloadEnabledRule();
    }

    @Override
    public String getPageTitle() {
        if (CollectionUtils.isNotEmpty(filterByReports)) {
            return messageBundle.formatMessage("history.format.title", getReportsNames());
        }

        return super.getPageTitle();
    }

    @Subscribe
    public void onQueryParametersChange(final QueryParametersChangeEvent event) {
        if (CollectionUtils.isNotEmpty(filterByReports)) {
            executionsDl.setParameter("reportIds", filterByReports);
        }
    }

    protected String getReportsNames() {
        if (CollectionUtils.isEmpty(filterByReports)) {
            return "";
        }

        return filterByReports.stream()
                .map(Report::getName)
                .collect(Collectors.joining(", "));
    }

    protected boolean downloadEnabledRule() {
        if (executionsDataGrid.getSelectedItems().size() != 1) {
            return false;
        }
        ReportExecution execution = executionsDataGrid.getSingleSelectedItem();
        return execution != null && execution.getOutputDocument() != null;
    }

    public void setFilterByReports(List<Report> filterByReports) {
        this.filterByReports = filterByReports;
    }
}
