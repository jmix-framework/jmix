/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reportsflowui.view.report;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportImportExport;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.history.ReportExecutionListView;
import io.jmix.reportsflowui.view.importdialog.ReportImportDialogView;
import io.jmix.reportsflowui.view.reportwizard.ReportWizardCreatorView;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;

@Route(value = "reports", layout = DefaultMainViewParent.class)
@ViewController("report_Report.list")
@ViewDescriptor("report-list-view.xml")
@LookupComponent("reportsDataGrid")
@DialogMode(width = "60em")
public class ReportListView extends StandardListView<Report> {

    @ViewComponent
    protected DataGrid<Report> reportsDataGrid;
    @ViewComponent("reportsDataGrid.create")
    protected CreateAction<Report> reportsDataGridCreate;

    @ViewComponent
    protected CollectionLoader<Report> reportsDl;
    @ViewComponent
    protected CollectionContainer<Report> reportsDc;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ReportsUtils reportsUtils;
    @Autowired
    protected ReportsPersistence reports;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected ReportImportExport reportImportExport;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UiReportRunner uiReportRunner;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected ViewNavigators viewNavigators;

    @Subscribe
    protected void onInit(InitEvent event) {
        initColumnDataGrid();
        initReportsDataCreate();
    }

    private void initReportsDataCreate() {
        reportsDataGridCreate.setIcon(null);
    }

    @Subscribe("wizard")
    public void onWizardClick(final DropdownButtonItem.ClickEvent event) {
        DialogWindow<ReportWizardCreatorView> build = dialogWindows.view(this, ReportWizardCreatorView.class).build();
        build.addAfterCloseListener(e -> {
            if (e.closedWith(StandardOutcome.SAVE)) {
                Report item = build.getView().getItem().getGeneratedReport();
                reportsDc.getMutableItems().add(item);
                reportsDataGrid.select(item);
                viewNavigators.detailView(Report.class)
                        .editEntity(reportsDc.getItem())
                        .withViewClass(ReportDetailView.class)
                        .navigate();
            }
        });
        build.open();
    }


    private void initColumnDataGrid() {
        reportsDataGrid.addColumn(report -> metadataTools.getInstanceName(report))
                .setKey("name")
                .setHeader(messageBundle.getMessage("name"))
                .setResizable(true)
                .setSortable(true);

        List<Grid.Column<Report>> columnList = Arrays.asList(
                reportsDataGrid.getColumnByKey("name"),
                reportsDataGrid.getColumnByKey("group"),
                reportsDataGrid.getColumnByKey("description"),
                reportsDataGrid.getColumnByKey("code")
        );

        reportsDataGrid.setColumnOrder(columnList);
    }

    @Subscribe("reportsDataGrid.runReport")
    public void onReportsDataGridRunReport(ActionPerformedEvent event) {
        Report report = reportsDataGrid.getSingleSelectedItem();
        report = reloadReport(report, fetchPlanRepository.findFetchPlan(metadata.getClass(Report.class), "report.edit"));
        if (CollectionUtils.isNotEmpty(report.getInputParameters()) || inputParametersRequiredByTemplates(report)) {
            DialogWindow<InputParametersDialog> parametersDialogWindow = dialogWindows.view(this, InputParametersDialog.class)
                    .withAfterCloseListener(e -> reportsDataGrid.focus())
                    .build();

            InputParametersDialog inputParametersDialog = parametersDialogWindow.getView();
            inputParametersDialog.setReport(report);
            inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());
            parametersDialogWindow.open();
        } else {
            FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(report)
                    .withParams(Collections.emptyMap());
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(this);
            }
            fluentRunner.runAndShow();
        }
    }

    @Subscribe("reportsDataGrid.importAction")
    public void onReportsDataGridImportAction(ActionPerformedEvent event) {
        dialogWindows.view(this, ReportImportDialogView.class)
                .withAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.SAVE)) {
                        reportsDl.load();
                    }
                })
                .build()
                .open();
    }

    @Subscribe("reportsDataGrid.export")
    public void onReportsDataGridExport(ActionPerformedEvent event) {
        Set<Report> reportsTableSelected = reportsDataGrid.getSelectedItems();
        if (reportsTableSelected.isEmpty()) {
            return;
        }

        ByteArrayDownloadDataProvider provider = new ByteArrayDownloadDataProvider(
                reportImportExport.exportReports(reportsTableSelected),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir());
        if (reportsTableSelected.size() > 1) {
            downloader.download(provider, "Reports", DownloadFormat.ZIP);
        } else if (reportsTableSelected.size() == 1) {
            downloader.download(provider, reportsTableSelected.iterator().next().getName(), DownloadFormat.ZIP);
        }
    }

    @Subscribe("reportsDataGrid.executions")
    protected void onReportsDataGridExecutions(ActionPerformedEvent event) {
        Set<Report> selectedReports = reportsDataGrid.getSelectedItems();
        DialogWindow<ReportExecutionListView> dialog = dialogWindows.view(this, ReportExecutionListView.class)
                .build();

        ReportExecutionListView reportExecutionListView = dialog.getView();
        reportExecutionListView.setFilterByReports(new ArrayList<>(selectedReports));
        dialog.open();
    }

    @Subscribe("reportsDataGrid.copy")
    public void onReportsDataGridCopy(ActionPerformedEvent event) {
        Report report = reportsDataGrid.getSingleSelectedItem();
        if (report != null) {
            copyReport(report);
            reportsDl.load();
        } else {
            notifications.create(messageBundle.getMessage("action.copy.notification.title"))
                    .show();
        }
    }

    @Install(to = "reportsDataGrid.create", subject = "afterSaveHandler")
    private void reportsDataGridCreateAfterSaveHandler(Report report) {
        reportsDataGrid.select(report);
    }

    @Install(to = "reportsDataGrid.create", subject = "enabledRule")
    protected boolean reportsDataGridCreateEnabledRule() {
        return isPermissionsToCreateReports();
    }

    @Install(to = "reportsDataGrid.importAction", subject = "enabledRule")
    protected boolean reportsDataGridImportEnabledRule() {
        return isPermissionsToCreateReports();
    }

    @Install(to = "reportsDataGrid.copy", subject = "enabledRule")
    protected boolean reportsDataGridCopyEnabledRule() {
        Report report = reportsDataGrid.getSingleSelectedItem();
        return report != null && isPermissionsToCreateReports();
    }

    protected Report copyReport(Report source) {
        source = dataManager.load(Id.of(source))
                .fetchPlan("report.edit")
                .one();
        Report copiedReport = metadataTools.deepCopy(source);
        copiedReport.setId(UuidProvider.createUuid());
        copiedReport.setName(reportsUtils.generateReportName(source.getName()));
        copiedReport.setCode(null);
        for (ReportTemplate copiedTemplate : copiedReport.getTemplates()) {
            copiedTemplate.setId(UuidProvider.createUuid());
        }

        reports.save(copiedReport);
        return copiedReport;
    }

    private boolean isPermissionsToCreateReports() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isCreatePermitted();
    }

    private Report reloadReport(Report report, FetchPlan fetchPlan) {
        MetaClass metaClass = metadata.getClass(Report.class);
        LoadContext<Report> loadContext = new LoadContext<>(metaClass);
        loadContext.setId(report.getId());
        loadContext.setFetchPlan(fetchPlan);
        return dataManager.load(loadContext);
    }
}