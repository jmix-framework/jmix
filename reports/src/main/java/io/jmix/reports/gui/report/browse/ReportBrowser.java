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
package io.jmix.reports.gui.report.browse;


import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import io.jmix.core.CoreProperties;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.components.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;

import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.gui.ReportGuiManager;
import io.jmix.reports.gui.report.edit.ReportEditor;
import io.jmix.reports.gui.report.history.ReportExecutionBrowser;
import io.jmix.reports.gui.report.wizard.ReportWizardCreator;

import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.Window;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.MapScreenOptions;
import org.springframework.beans.factory.annotation.Autowired;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReportBrowser extends AbstractLookup {

    @Autowired
    protected ReportGuiManager reportGuiManager;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
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
    @Named("table.edit")
    protected EditAction tableEdit;
    @Autowired
    protected Security security;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected PopupButton popupCreateBtn;
    @Autowired
    protected Button createBtn;
    @Autowired
    protected CollectionDatasource<Report, UUID> reportDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        boolean hasPermissionsToCreateReports = security.isEntityOpPermitted(
                metadata.getClassNN(Report.class), EntityOp.CREATE);

        Action copyAction = new ItemTrackingAction("copy")
                .withCaption(getMessage("copy"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        reportService.copyReport(report);
                        reportsTable.getDatasource().refresh();
                    } else {
                        showNotification(getMessage("notification.selectReport"), NotificationType.HUMANIZED);
                    }
                });
        copyAction.setEnabled(hasPermissionsToCreateReports);
        copyReport.setAction(copyAction);

        runReport.setAction(new ItemTrackingAction("runReport")
                .withCaption(getMessage("runReport"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        report = getDsContext().getDataSupplier().reload(report, "report.edit");
                        if (report.getInputParameters() != null && report.getInputParameters().size() > 0 ||
                                reportGuiManager.inputParametersRequiredByTemplates(report)) {
                            Window paramsWindow = openWindow("report$inputParameters", OpenType.DIALOG,
                                    ParamsMap.of("report", report));
                            paramsWindow.addCloseListener(actionId ->
                                    reportsTable.focus()
                            );
                        } else {
                            reportGuiManager.printReport(report, Collections.emptyMap(), ReportBrowser.this);
                        }
                    }
                }));

        BaseAction importAction = new BaseAction("import")
                .withHandler(event -> {
                    openWindow("report$Report.importDialog", OpenType.DIALOG)
                            .addCloseListener(actionId -> {
                                if (COMMIT_ACTION_ID.equals(actionId)) {
                                    reportsTable.getDatasource().refresh();
                                }
                                //TODO request focus
//                                reportsTable.requestFocus();
                            });
                });

        importAction.setEnabled(hasPermissionsToCreateReports);
        importReport.setAction(importAction);

        Action exportAction = new ItemTrackingAction("export")
                .withHandler(event -> {
                    Set<Report> reports = reportsTable.getSelected();
                    if ((reports != null) && (!reports.isEmpty())) {
                        ExportDisplay exportDisplay = AppBeans.getPrototype(ExportDisplay.class);
                        ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports), uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());
                        if (reports.size() > 1) {
                            exportDisplay.show(provider, "Reports", ExportFormat.ZIP);
                        } else if (reports.size() == 1) {
                            exportDisplay.show(provider, reports.iterator().next().getName(), ExportFormat.ZIP);
                        }
                    }
                });

        exportReport.setAction(exportAction);

        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());
        reportsTable.addAction(new ShowExecutionsAction());

        CreateAction createReportAction = new CreateAction(reportsTable) {
            @Override
            protected void afterCommit(JmixEntity entity) {
                reportsTable.expandPath(entity);
            }
        };

        reportsTable.addAction(createReportAction);
        subscribeCreateActionCloseHandler(createReportAction);

            reportsTable.getButtonsPanel().remove(createBtn);

            CreateAction popupCreateReportAction = new CreateAction(reportsTable) {
                @Override
                public String getCaption() {
                    return getMessage("report.new");
                }

                @Override
                protected void afterCommit(JmixEntity entity) {
                    reportsTable.expandPath(entity);
                }
            };
            popupCreateBtn.addAction(popupCreateReportAction);
            subscribeCreateActionCloseHandler(popupCreateReportAction);

            popupCreateBtn.addAction(new AbstractAction("wizard") {
                @Override
                public void actionPerform(Component component) {
                    ReportWizardCreator wizard = (ReportWizardCreator) openWindow("report$Report.wizard",
                            OpenType.DIALOG);
                    wizard.addCloseListener(actionId -> {
                        if (COMMIT_ACTION_ID.equals(actionId)) {
                            if (wizard.getItem() != null && wizard.getItem().getGeneratedReport() != null) {
                                Report item = wizard.getItem().getGeneratedReport();
                                reportDs.includeItem(item);
                                reportsTable.setSelected(item);
                                ReportEditor reportEditor = (ReportEditor) openEditor("report$Report.edit",
                                        reportDs.getItem(), OpenType.THIS_TAB);

                                reportEditor.addCloseListener(reportEditorActionId -> {
                                    if (COMMIT_ACTION_ID.equals(reportEditorActionId)) {
                                        Report item1 = reportEditor.getItem();
                                        if (item1 != null) {
                                            reportDs.updateItem(item1);
                                        }
                                    }
                                    UUID newReportId = reportEditor.getItem().getId();
                                    reportsTable.expandPath(reportDs.getItem(newReportId));
                                    //TODO request focus
//                                    reportsTable.requestFocus();
                                });
                            }
                        }
                    });
                }

                @Override
                public String getCaption() {
                    return getMessage("report.wizard");
                }
            });

            popupCreateBtn.setEnabled(hasPermissionsToCreateReports);

        tableEdit.setAfterWindowClosedHandler((window, closeActionId) -> {
            if (!COMMIT_ACTION_ID.equals(closeActionId)) {
                Report editedReport = (Report) ((Editor) window).getItem();
                Report currentItem = reportDs.getItem(editedReport.getId());

                if (currentItem != null && !editedReport.getVersion().equals(currentItem.getVersion())) {
                    DataSupplier dataSupplier = getDsContext().getDataSupplier();
                    Report reloadedReport = dataSupplier.reload(currentItem, reportDs.getView());
                    reportDs.updateItem(reloadedReport);
                }
            }
        });
    }

    protected void subscribeCreateActionCloseHandler(CreateAction createAction) {
        createAction.setAfterWindowClosedHandler(((window, closeActionId) -> {
            if (!COMMIT_ACTION_ID.equals(closeActionId)) {
                Report newReport = (Report) ((Editor) window).getItem();

                if (!PersistenceHelper.isNew(newReport)) {
                    DataSupplier dataSupplier = getDsContext().getDataSupplier();
                    Report reloadedReport = dataSupplier.reload(newReport, reportDs.getView());

                    boolean modified = reportDs.isModified();
                    reportDs.addItem(reloadedReport);
                    ((DatasourceImplementation) reportDs).setModified(modified);
                }
            }
        }));
    }

    public class ShowExecutionsAction extends BaseAction {

        public ShowExecutionsAction() {
            super("executions");
        }

        @Override
        public String getCaption() {
            return getMessage("report.browser.showExecutions");
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