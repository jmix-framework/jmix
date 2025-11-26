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

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.*;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.MissingDefaultTemplateException;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.download.ReportDownloader;
import io.jmix.reportsflowui.helper.GridSortHelper;
import io.jmix.reportsflowui.helper.OutputTypeHelper;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.history.ReportExecutionListView;
import io.jmix.reportsflowui.view.importdialog.ReportImportDialogView;
import io.jmix.reportsflowui.view.reportwizard.ReportWizardCreatorView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
    protected TypedTextField<String> codeFilter;
    @ViewComponent
    protected TypedTextField<String> nameFilter;
    @ViewComponent
    protected EntityComboBox<ReportGroup> groupFilter;
    @ViewComponent
    protected TypedDatePicker<Date> updatedDateFilter;
    @ViewComponent
    protected JmixSelect<ReportOutputType> outputTypeFilter;
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
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ReportDownloader downloader;
    @Autowired
    protected ReportImportExport reportImportExport;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UiReportRunner uiReportRunner;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    private Messages messages;
    @Autowired
    private EntityUuidGenerator entityUuidGenerator;
    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected GridSortHelper gridSortHelper;
    @Autowired
    protected OutputTypeHelper outputTypeHelper;
    @Autowired
    protected ReportGroupRepository reportGroupRepository;

    @Subscribe
    protected void onInit(InitEvent event) {
        initReportsDataGridCreate();
        initOutputTypeList();

        codeFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        nameFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        groupFilter.addValueChangeListener(e -> onFilterFieldValueChange());
        updatedDateFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        outputTypeFilter.addValueChangeListener(e -> onFilterFieldValueChange());
    }

    protected void initOutputTypeList() {
        List<ReportOutputType> supportedOutputTypes = outputTypeHelper.getSupportedOutputTypes();
        outputTypeFilter.setItems(supportedOutputTypes);
    }

    private void initReportsDataGridCreate() {
        reportsDataGridCreate.setIconComponent(null);
    }

    @Supply(to = "reportsDataGrid.name", subject = "renderer")
    private Renderer<Report> nameCellRenderer() {
        return new TextRenderer<>(metadataTools::getInstanceName);
    }

    @Subscribe("reportsDataGrid.runReport")
    public void onReportsDataGridRunReport(ActionPerformedEvent event) {
        Report report = reportsDataGrid.getSingleSelectedItem();
        if (report == null) {
            return;
        }

        FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(report)
                .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
        try {
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(this);
            }
            fluentRunner.runAndShow();
        } catch (MissingDefaultTemplateException e) {
            notifications.create(
                            messages.getMessage("runningReportError.title"),
                            messages.getMessage("missingDefaultTemplateError.description"))
                    .withType(Notifications.Type.ERROR)
                    .show();
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
        return isPermissionsToCreateReports() && isDatabaseReportSelected();
    }

    @Install(to = "reportsDataGrid.edit", subject = "enabledRule")
    protected boolean reportsDataGridEditEnabledRule() {
        return isDatabaseReportSelected();
    }

    @Install(to = "reportsDataGrid.remove", subject = "enabledRule")
    protected boolean reportsDataGridRemoveEnabledRule() {
        return isDatabaseReportSelected();
    }

    @Install(to = "reportsDataGrid.export", subject = "enabledRule")
    protected boolean reportsDataGridExportEnabledRule() {
        return isDatabaseReportSelected();
    }

    protected boolean isDatabaseReportSelected() {
        Report report = reportsDataGrid.getSingleSelectedItem();
        return report != null && report.getSource() == ReportSource.DATABASE;
    }

    @Subscribe("reportsDataGrid.wizard")
    public void onWizardClick(final ActionPerformedEvent event) {
        DialogWindow<ReportWizardCreatorView> build = dialogWindows.view(this, ReportWizardCreatorView.class).build();
        build.addAfterCloseListener(e -> {
            if (e.closedWith(StandardOutcome.SAVE)) {
                Report item = build.getView().getItem().getGeneratedReport();
                reportsDc.getMutableItems().add(item);
                reportsDataGrid.select(item);
                viewNavigators.detailView(this, Report.class)
                        .editEntity(reportsDc.getItem())
                        .withViewClass(ReportDetailView.class)
                        .navigate();
            }
        });
        build.open();
    }

    @Install(to = "reportsDataGrid.wizard", subject = "enabledRule")
    protected boolean reportsDataGridWizardEnabledRule() {
        return isPermissionsToCreateReports();
    }

    protected Report copyReport(Report source) {
        source = dataManager.load(Id.of(source))
                .fetchPlan("report.edit")
                .one();
        Report copiedReport = metadataTools.deepCopy(source);
        copiedReport.setId(entityUuidGenerator.generate());
        copiedReport.setName(reportsUtils.generateReportName(source.getName()));
        copiedReport.setCode(null);
        for (ReportTemplate copiedTemplate : copiedReport.getTemplates()) {
            copiedTemplate.setId(entityUuidGenerator.generate());
        }

        reportRepository.save(copiedReport);
        return copiedReport;
    }

    private boolean isPermissionsToCreateReports() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isCreatePermitted();
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    private List<Report> reportsDlLoadDelegate(final LoadContext<Report> loadContext) {
        ReportFilter filter = createFilter();

        Sort sort = gridSortHelper.convertSortOrders(
                reportsDataGrid.getSortOrder(),
                Map.of("name", ReportLoadContext.LOCALIZED_NAME_SORT_KEY) // custom cell renderer
        );
        ReportLoadContext context = new ReportLoadContext(filter, sort, loadContext.getQuery().getFirstResult(),
                loadContext.getQuery().getMaxResults());
        List<Report> items = reportRepository.loadList(context);
        return items;
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    private Integer paginationTotalCountDelegate(final DataLoadContext ignored) {
        ReportFilter filter = createFilter();
        return reportRepository.getTotalCount(filter);
    }

    protected ReportFilter createFilter() {
        ReportFilter filter = new ReportFilter();
        // ui filters
        filter.setNameContains(nameFilter.getTypedValue());
        filter.setCodeContains(codeFilter.getTypedValue());
        filter.setGroup(groupFilter.getValue());
        filter.setUpdatedAfter(updatedDateFilter.getTypedValue());
        filter.setOutputType(outputTypeFilter.getValue());
        return filter;
    }

    protected void onFilterFieldValueChange() {
        reportsDl.load();
    }

    @Install(to = "reportGroupsDl", target = Target.DATA_LOADER)
    protected List<ReportGroup> reportGroupsDlLoadDelegate(final LoadContext<ReportGroup> ignored) {
        return reportGroupRepository.loadAll();
    }
}