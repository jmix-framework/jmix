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

package io.jmix.reportsui.runner.impl;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.util.ReportZipUtils;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reportsui.runner.*;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.reportsui.screen.report.run.ShowChartScreen;
import io.jmix.reportsui.screen.report.run.ShowPivotTableScreen;
import io.jmix.reportsui.screen.report.run.ShowReportTableScreen;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UIScope
@Component("report_UiReportRunner")
public class UiReportRunnerImpl implements UiReportRunner {
    @Autowired
    protected ReportRunner reportRunner;
    @Autowired
    protected Screens screens;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected ReportZipUtils reportZipUtils;
    @Autowired
    protected ReportsUtils reportsUtils;
    @Autowired
    protected ObjectProvider<FluentUiReportRunner> fluentUiReportRunners;

    @Override
    public void runAndShow(UiReportRunContext context) {
        prepareContext(context);
        if (needToShowParamsDialog(context)) {
            openReportParamsDialog(context, null, false);
            return;
        }

        FrameOwner originFrameOwner = context.getOriginFrameOwner();
        if (originFrameOwner!= null && context.getInBackground()) {
            Screen hostScreen = UiControllerUtils.getScreen(originFrameOwner);
            runInBackground(context, hostScreen);
        } else {
            ReportOutputDocument reportOutputDocument = reportRunner.run(context.getReportRunContext());
            showResult(reportOutputDocument, context);
        }
    }

    @Override
    public void runMultipleReports(UiReportRunContext context, String multiParamAlias, Collection multiParamValue) {
        prepareContext(context);
        if (needToShowParamsDialog(context)) {
            context.addParam(multiParamAlias, multiParamValue);
            openReportParamsDialog(context, getInputParameter(context, multiParamAlias), true);
            return;
        }

        FrameOwner originFrameOwner = context.getOriginFrameOwner();
        if (originFrameOwner!= null && context.getInBackground()) {
            Report targetReport = getReportForPrinting(context.getReport());
            long timeout = reportsClientProperties.getBackgroundReportProcessingTimeoutMs();
            Screen hostScreen = UiControllerUtils.getScreen(originFrameOwner);
            BackgroundTask<Integer, List<ReportOutputDocument>> task =
                    new BackgroundTask<Integer, List<ReportOutputDocument>>(timeout, TimeUnit.MILLISECONDS, hostScreen) {
                        @SuppressWarnings("UnnecessaryLocalVariable")
                        @Override
                        public List<ReportOutputDocument> run(TaskLifeCycle<Integer> taskLifeCycle) {
                            context.setReport(targetReport);
                            return multiRunSync(context, multiParamAlias, multiParamValue);
                        }

                        @Override
                        public void done(List<ReportOutputDocument> result) {
                            downloadZipArchive(result);
                        }
                    };
            showDialog(task);
        } else {
            List<ReportOutputDocument> outputDocuments = multiRunSync(context, multiParamAlias, multiParamValue);
            downloadZipArchive(outputDocuments);
        }
    }

    protected void downloadZipArchive(List<ReportOutputDocument> outputDocuments) {
        byte[] zipArchiveContent = reportZipUtils.createZipArchive(outputDocuments);
        downloader.download(zipArchiveContent, "Reports.zip", DownloadFormat.ZIP);
    }

    protected List<ReportOutputDocument> multiRunSync(UiReportRunContext uiReportRunContext, String multiParamName, Collection<Object> multiParamValues) {
        List<ReportOutputDocument> outputDocuments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(multiParamValues)) {
            multiParamValues.forEach(paramValue -> {
                Map<String, Object> map = new HashMap<>();
                map.put(multiParamName, paramValue);
                if (CollectionUtils.isNotEmpty(multiParamValues)) {
                    map.putAll(uiReportRunContext.getParams());
                }

                ReportRunContext reportRunContext = new ReportRunContext(uiReportRunContext.getReport())
                        .setReportTemplate(uiReportRunContext.getReportTemplate())
                        .setParams(map)
                        .setOutputType(uiReportRunContext.getOutputType())
                        .setOutputNamePattern(uiReportRunContext.getOutputNamePattern());

                ReportOutputDocument reportOutputDocument = reportRunner.run(reportRunContext);
                outputDocuments.add(reportOutputDocument);
            });
        }
        return outputDocuments;
    }

    @Override
    public FluentUiReportRunner byReportCode(String reportCode) {
        FluentUiReportRunner fluentRunner = fluentUiReportRunners.getObject();
        return fluentRunner.init(reportCode);
    }

    @Override
    public FluentUiReportRunner byReportEntity(Report report) {
        FluentUiReportRunner fluentRunner = fluentUiReportRunners.getObject();
        return fluentRunner.init(report);
    }

    protected void runInBackground(UiReportRunContext context, Screen hostScreen) {
        Report targetReport = getReportForPrinting(context.getReport());
        long timeout = reportsClientProperties.getBackgroundReportProcessingTimeoutMs();
        BackgroundTask<Integer, ReportOutputDocument> task =
                new BackgroundTask<Integer, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, hostScreen) {

                    @SuppressWarnings("UnnecessaryLocalVariable")
                    @Override
                    public ReportOutputDocument run(TaskLifeCycle<Integer> taskLifeCycle) {
                        context.setReport(targetReport);
                        ReportOutputDocument result = reportRunner.run(context.getReportRunContext());
                        return result;
                    }

                    @Override
                    public boolean handleException(Exception ex) {
                        if (ex instanceof ReportingException) {
                            if (ex instanceof FailedToConnectToOpenOfficeException) {
                                String caption = messages.getMessage("io.jmix.reportsui.exception", "reportException.failedConnectToOffice");
                                return showErrorNotification(caption);
                            } else if (ex instanceof NoOpenOfficeFreePortsException) {
                                String caption = messages.getMessage("io.jmix.reportsui.exception", "reportException.noOpenOfficeFreePorts");
                                return showErrorNotification(caption);
                            }
                        }
                        return super.handleException(ex);
                    }

                    protected boolean showErrorNotification(String text) {
                        Screen ownerScreen = this.getOwnerScreen();
                        if (ownerScreen != null) {
                            ScreenContext screenContext = ComponentsHelper.getScreenContext(ownerScreen.getWindow());
                            Notifications notifications = screenContext.getNotifications();
                            notifications.create()
                                    .withCaption(text)
                                    .withType(Notifications.NotificationType.ERROR)
                                    .show();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void done(ReportOutputDocument document) {
                        showResult(document, context);
                    }

                    @Override
                    public void canceled() {
                        super.canceled();
                        //todo https://github.com/Haulmont/jmix-reports/issues/22
                        //reportService.cancelReportExecution(userSessionId, report.getId());
                    }
                };


        showDialog(task);
    }

    protected <T> void showDialog(BackgroundTask<Integer, T> task) {
        String caption = messages.getMessage(UiReportRunner.class, "runReportBackgroundTitle");
        String description = messages.getMessage(UiReportRunner.class, "runReportBackgroundMessage");

        dialogs.createBackgroundWorkDialog(task.getOwnerScreen(), task)
                .withCancelAllowed(true)
                .withCaption(caption)
                .withMessage(description)
                .show();
    }


    protected void openReportParamsDialog(UiReportRunContext context, @Nullable ReportInputParameter inputParameter,
                                          boolean bulkPrint) {
        InputParametersDialog inputParametersDialog = screens.create(InputParametersDialog.class, OpenMode.DIALOG);
        inputParametersDialog.setReport(context.getReport());
        inputParametersDialog.setInputParameter(inputParameter);
        inputParametersDialog.setParameters(context.getParams());
        inputParametersDialog.setTemplateCode(getTemplateCode(context));
        inputParametersDialog.setOutputFileName(context.getOutputNamePattern());
        inputParametersDialog.setBulkPrint(bulkPrint);
        inputParametersDialog.setInBackground(context.getInBackground());
        inputParametersDialog.show();
    }


    protected Report getReportForPrinting(Report report) {
        Report copy = metadataTools.copy(report);
        copy.setIsTmp(report.getIsTmp());
        return copy;
    }

    protected void showResult(ReportOutputDocument document, UiReportRunContext context) {
        String templateCode = getTemplateCode(context);
        Map<String, Object> params = context.getParams();
        ReportOutputType outputType = context.getOutputType();
        if (document.getReportOutputType().getId().equals(JmixReportOutputType.chart.getId())) {
            ShowChartScreen showChartScreen = (ShowChartScreen) screens.create("report_ShowChart.screen", OpenMode.DIALOG);
            showChartScreen.setChartJson(new String(document.getContent(), StandardCharsets.UTF_8));
            showChartScreen.setReport((Report) document.getReport());
            showChartScreen.setTemplateCode(templateCode);
            showChartScreen.setReportParameters(params);
            showChartScreen.show();
        } else if (document.getReportOutputType().getId().equals(JmixReportOutputType.pivot.getId())) {
            ShowPivotTableScreen showPivotTableScreen = (ShowPivotTableScreen) screens.create("report_ShowPivotTable.screen", OpenMode.DIALOG);
            showPivotTableScreen.setPivotTableData(document.getContent());
            showPivotTableScreen.setReport((Report) document.getReport());
            showPivotTableScreen.setTemplateCode(templateCode);
            showPivotTableScreen.setParams(params);
            showPivotTableScreen.show();
        } else if (document.getReportOutputType().getId().equals(JmixReportOutputType.table.getId())) {
            ShowReportTableScreen reportTable = (ShowReportTableScreen) screens.create("report_ShowReportTable.screen", OpenMode.DIALOG);
            reportTable.setTableData(document.getContent());
            reportTable.setReport((Report) document.getReport());
            reportTable.setTemplateCode(templateCode);
            reportTable.setReportParameters(params);
            reportTable.show();
        } else {
            byte[] byteArr = document.getContent();
            com.haulmont.yarg.structure.ReportOutputType finalOutputType =
                    (outputType != null) ? outputType.getOutputType() : document.getReportOutputType();

            DownloadFormat exportFormat = DownloadFormat.getByExtension(finalOutputType.getId());
            String outputFileName = context.getOutputNamePattern();
            String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();

            downloader.download(byteArr, documentName, exportFormat);
        }
    }

    @Nullable
    protected String getTemplateCode(UiReportRunContext context) {
        ReportTemplate reportTemplate = context.getReportTemplate();
        return reportTemplate != null ? reportTemplate.getCode() : null;
    }

    protected void prepareContext(UiReportRunContext context) {
        Report report = context.getReport();
        context.setReport(reportsUtils.reloadReportIfNeeded(report, "report.edit"));

        ReportTemplate template = context.getReportTemplate();
        if (template == null) {
            template = getDefaultTemplate(report);
            context.setReportTemplate(template);
        }
    }

    protected ReportTemplate getDefaultTemplate(Report report) {
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate == null)
            throw new ReportingException(String.format("No default template specified for report [%s]", report.getName()));
        return defaultTemplate;
    }

    protected boolean needToShowParamsDialog(UiReportRunContext uiReportRunContext) {
        ParametersDialogShowMode mode = uiReportRunContext.getParametersDialogShowMode();
        Report report = uiReportRunContext.getReport();
        if (mode == ParametersDialogShowMode.YES) {
            return true;
        }
        if (mode == null || mode == ParametersDialogShowMode.IF_REQUIRED) {
            return CollectionUtils.isNotEmpty(report.getInputParameters()) || inputParametersRequiredByTemplates(report);
        }
        return false;
    }

    protected ReportInputParameter getInputParameter(UiReportRunContext uiReportRunContext, String multiParamAlias) {
        Report report = uiReportRunContext.getReport();
        ReportInputParameter multiParameter = report.getInputParameters().stream()
                .filter(reportParameter -> StringUtils.equals(reportParameter.getAlias(), multiParamAlias))
                .findFirst()
                .orElse(null);
        if (multiParameter == null) {
            throw new ReportingException(String.format("Unable to find parameter by alias [%s] for report [%s]", multiParamAlias, report.getName()));
        }
        return multiParameter;
    }
}
