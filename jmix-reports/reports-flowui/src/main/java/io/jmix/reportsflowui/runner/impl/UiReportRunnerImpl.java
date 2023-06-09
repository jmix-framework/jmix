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

package io.jmix.reportsflowui.runner.impl;

import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportTableView;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.util.ReportZipUtils;
import io.jmix.reports.util.ReportsUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component("report_UiReportRunner")
public class UiReportRunnerImpl implements UiReportRunner {

    @Autowired
    protected ReportRunner reportRunner;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ReportZipUtils reportZipUtils;
    @Autowired
    protected ReportsUtils reportsUtils;
    @Autowired
    protected ObjectProvider<FluentUiReportRunner> fluentUiReportRunners;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    @Override
    public void runAndShow(UiReportRunContext context) {
        prepareContext(context);
        if (needToShowParamsDialog(context)) {
            openReportParamsDialog(context, null, false);
            return;
        }

        View<?> originFrameOwner = context.getOriginFrameOwner();
        if (originFrameOwner != null && context.getInBackground()) {
            runInBackground(context, originFrameOwner);
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

        View<?> originFrameOwner = context.getOriginFrameOwner();
        if (originFrameOwner != null && context.getInBackground()) {
            Report targetReport = getReportForPrinting(context.getReport());
            long timeout = reportsClientProperties.getBackgroundReportProcessingTimeoutMs();
            BackgroundTask<Integer, List<ReportOutputDocument>> task =
                    new BackgroundTask<>(timeout, TimeUnit.MILLISECONDS, originFrameOwner) {
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

    protected void runInBackground(UiReportRunContext context, View hostScreen) {
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
                                String caption = messages.getMessage("io.jmix.reportsflowui.exception", "reportException.failedConnectToOffice");
                                return showErrorNotification(caption);
                            } else if (ex instanceof NoOpenOfficeFreePortsException) {
                                String caption = messages.getMessage("io.jmix.reportsflowui.exception", "reportException.noOpenOfficeFreePorts");
                                return showErrorNotification(caption);
                            }
                        }
                        return super.handleException(ex);
                    }

                    protected boolean showErrorNotification(String text) {
                        View<?> ownerScreen = this.getOwnerView();
                        if (ownerScreen != null) {
                            notifications.create(text)
                                    .withType(Notifications.Type.ERROR)
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
        String header = messages.getMessage(UiReportRunner.class, "runReportBackground.header");
        String text = messages.getMessage(UiReportRunner.class, "runReportBackground.text");

        dialogs.createBackgroundTaskDialog(task)
                .withHeader(header)
                .withText(text)
                .withCancelAllowed(true)
                .open();
    }


    protected void openReportParamsDialog(UiReportRunContext context, @Nullable ReportInputParameter inputParameter,
                                          boolean bulkPrint) {
        DialogWindow<InputParametersDialog> inputParametersDialogWindow = dialogWindows.view(context.getOriginFrameOwner(),
                        InputParametersDialog.class)
                .build();

        InputParametersDialog inputParametersDialog = inputParametersDialogWindow.getView();
        inputParametersDialog.setReport(context.getReport());
        inputParametersDialog.setInputParameter(inputParameter);
        inputParametersDialog.setParameters(context.getParams());
        inputParametersDialog.setTemplateCode(getTemplateCode(context));
        inputParametersDialog.setOutputFileName(context.getOutputNamePattern());
        inputParametersDialog.setBulkPrint(bulkPrint);
        inputParametersDialog.setInBackground(context.getInBackground());

        inputParametersDialogWindow.open();
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

        if (document.getReportOutputType().getId().equals(JmixReportOutputType.table.getId())) {
            DialogWindow<ReportTableView> showReportTableViewDialogWindow = dialogWindows.view(context.getOriginFrameOwner(), ReportTableView.class)
                    .build();

            ReportTableView reportTableView = showReportTableViewDialogWindow.getView();
            reportTableView.setTableData(document.getContent());
            reportTableView.setReport((Report) document.getReport());
            reportTableView.setTemplateCode(templateCode);
            reportTableView.setReportParameters(params);
            showReportTableViewDialogWindow.open();
        } else {
            byte[] byteArr = document.getContent();
            io.jmix.reports.yarg.structure.ReportOutputType finalOutputType =
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
            return containsVisibleInputParameters(report) || inputParametersRequiredByTemplates(report);
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

    protected boolean containsVisibleInputParameters(Report report) {
        List<ReportInputParameter> inputParameters = report.getInputParameters();
        return CollectionUtils.isNotEmpty(inputParameters)
                && inputParameters.stream().anyMatch(inputParameter -> BooleanUtils.isNotTrue(inputParameter.getHidden()));
    }
}
