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
package io.jmix.reportsui.gui.report.browse;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.accesscontext.CrudEntityContext;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.edit.ReportEditor;
import io.jmix.reportsui.gui.report.history.ReportExecutionBrowser;
import io.jmix.reportsui.gui.report.importdialog.ReportImportDialog;
import io.jmix.reportsui.gui.report.run.InputParametersWindow;
import io.jmix.reportsui.gui.report.wizard.ReportWizardCreator;
import io.jmix.ui.*;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@UiController("report_Report.browse")
@UiDescriptor("report-browse.xml")
@LookupComponent("table")
@Route("reports")
public class ReportBrowser extends StandardLookup<Report> {

    @Autowired
    protected ReportGuiManager reportGuiManager;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    private CollectionLoader<Report> reportDl;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    private CollectionContainer<Report> reportDc;
    @Autowired
    protected Button runReport;
    @Named("import")
    protected Button importReport;
    @Named("export")
    protected Button exportReport;
    @Named("copy")
    protected Button copyReport;
    @Named("table")
    protected GroupTable<Report> reportsTable;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Screens screens;
    @Autowired
    protected Actions actions;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected PopupButton popupCreateBtn;
    @Autowired
    protected Button createBtn;

    @Subscribe
    public void onInit(InitEvent initEvent) {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        boolean hasPermissionsToCreateReports = showScreenContext.isCreatePermitted();

        Action copyAction = actions.create(ItemTrackingAction.class, "copy")
                .withCaption(messages.getMessage("copy"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        reportService.copyReport(report);
                        reportDl.load();
                    } else {
                        notifications.create(Notifications.NotificationType.HUMANIZED)
                                .withCaption(messages.getMessage("notification.selectReport"))
                                .show();
                    }
                });
        copyAction.setEnabled(hasPermissionsToCreateReports);
        copyReport.setAction(copyAction);

        runReport.setAction(new ItemTrackingAction("runReport")
                .withCaption(messages.getMessage("runReport"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        report = reloadReport(report, fetchPlanRepository.findFetchPlan(
                                metadata.getClass(Report.class), "report.edit"));
                        if (report.getInputParameters() != null && report.getInputParameters().size() > 0 ||
                                reportGuiManager.inputParametersRequiredByTemplates(report)) {
                            screens.create(InputParametersWindow.class, OpenMode.DIALOG,
                                    new MapScreenOptions(ParamsMap.of("report", report)))
                                    .show()
                                    .addAfterCloseListener(e -> {
                                        reportsTable.focus();
                                    });
                        } else {
                            reportGuiManager.printReport(report, Collections.emptyMap(), ReportBrowser.this);
                        }
                    }
                }));

        BaseAction importAction = new BaseAction("import")
                .withHandler(event -> {
                    screens.create(ReportImportDialog.class, OpenMode.DIALOG)
                            .show()
                            .addAfterCloseListener(e -> {
                                if (e.closedWith(StandardOutcome.COMMIT)) {
                                    reportDl.load();
                                }
                            });
                });

        importAction.setEnabled(hasPermissionsToCreateReports);
        importReport.setAction(importAction);

        Action exportAction = actions.create(ItemTrackingAction.class, "export")
                .withHandler(event -> {
                    Set<Report> reports = reportsTable.getSelected();
                    if ((reports != null) && (!reports.isEmpty())) {
                        ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports), uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());
                        if (reports.size() > 1) {
                            downloader.download(provider, "Reports", DownloadFormat.ZIP);
                        } else if (reports.size() == 1) {
                            downloader.download(provider, reports.iterator().next().getName(), DownloadFormat.ZIP);
                        }
                    }
                });

        exportReport.setAction(exportAction);

        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());
        reportsTable.addAction(new ShowExecutionsAction());

        CreateAction<Report> createReportAction = actions.create(CreateAction.class);
        createReportAction.setTarget(reportsTable);
        createReportAction.setAfterCommitHandler(e -> reportsTable.expandPath(e));

        reportsTable.addAction(createReportAction);
        subscribeCreateActionCloseHandler(createReportAction);

        reportsTable.getButtonsPanel().remove(createBtn);

        CreateAction<Report> popupCreateReportAction = actions.create(CreateAction.class);
        popupCreateReportAction.setTarget(reportsTable);
        popupCreateReportAction.withCaption(messages.getMessage("report.new"));
        popupCreateReportAction.setAfterCommitHandler(e -> reportsTable.expandPath(e));

        popupCreateBtn.addAction(popupCreateReportAction);
        subscribeCreateActionCloseHandler(popupCreateReportAction);

        popupCreateBtn.addAction(new AbstractAction("wizard") {
            @Override
            public void actionPerform(Component component) {
                ReportWizardCreator wizard = screens.create(ReportWizardCreator.class, OpenMode.DIALOG);
                wizard.addAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.COMMIT)) {
                        Report item = wizard.getItem().getGeneratedReport();
                        reportDc.getItems().add(item);
                        reportsTable.setSelected(item);
                        ReportEditor reportEditor = (ReportEditor) screenBuilders.editor(reportsTable)
                                .withOpenMode(OpenMode.THIS_TAB)
                                .build();
                        reportEditor.show()
                                .addAfterCloseListener(closeEvent -> {
                                    if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                                        Report item1 = reportEditor.getEditedEntity();
                                        if (item1 != null) {
                                            reportDc.replaceItem(item1);
                                        }
                                    }
                                    UUID newReportId = reportEditor.getEditedEntity().getId();
                                    reportsTable.expandPath(reportDc.getItem(newReportId));
                                });
                    }
                });
                wizard.show();
            }

            @Override
            public String getCaption() {
                return messages.getMessage("report.wizard");
            }
        });

        popupCreateBtn.setEnabled(hasPermissionsToCreateReports);
    }

    private Report reloadReport(Report report, FetchPlan fetchPlan) {
        MetaClass metaClass = metadata.findClass(Report.class);
        LoadContext<Report> lc = new LoadContext<>(metaClass);
        lc.setId(report.getId());
        lc.setFetchPlan(fetchPlan);
        report = dataManager.load(lc);
        return report;
    }

    @Install(to = "table.edit", subject = "afterCloseHandler")
    protected void tableEditAfterCloseHandler(AfterCloseEvent event) {
        if (event.closedWith(StandardOutcome.COMMIT)) {
            Report editedReport = (Report) ((EditorScreen) event.getScreen()).getEditedEntity();
            Report currentItem = reportDc.getItem(editedReport.getId());

            if (currentItem != null && !editedReport.getVersion().equals(currentItem.getVersion())) {
                Report reloadedReport = reloadReport(currentItem, reportDl.getFetchPlan());
                reportDc.replaceItem(reloadedReport);
            }
        }
    }

    protected void subscribeCreateActionCloseHandler(CreateAction<Report> createAction) {
        createAction.setAfterCloseHandler(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                Report newReport = (Report) ((EditorScreen) closeEvent.getScreen()).getEditedEntity();

                if (!entityStates.isNew(newReport)) {
                    Report reloadedReport = reloadReport(newReport, reportDl.getFetchPlan());
                    reportDc.getMutableItems().add(reloadedReport);
                }
            }
        });
    }

    public class ShowExecutionsAction extends BaseAction {

        public ShowExecutionsAction() {
            super("executions");
        }

        @Override
        public String getCaption() {
            return messages.getMessage("report.browser.showExecutions");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Report> selectedReports = reportsTable.getSelected();
            screenBuilders.screen(ReportBrowser.this)
                    .withScreenClass(ReportExecutionBrowser.class)
                    .withOptions(new MapScreenOptions(
                            ParamsMap.of(ReportExecutionBrowser.REPORTS_PARAMETER, new ArrayList<>(selectedReports))
                    ))
                    .show();
        }
    }
}