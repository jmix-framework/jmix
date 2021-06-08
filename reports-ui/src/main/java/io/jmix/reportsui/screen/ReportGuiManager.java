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
package io.jmix.reportsui.screen;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.Reports;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.reportsui.screen.report.run.ShowChartScreen;
import io.jmix.reportsui.screen.report.run.ShowPivotTableScreen;
import io.jmix.reportsui.screen.report.run.ShowReportTableScreen;
import io.jmix.ui.*;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Window;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UIScope
@Component("report_ReportGuiManager")
public class ReportGuiManager {

    @Autowired
    protected Reports reports;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ReportSecurityManager reportSecurityManager;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Downloader downloader;

    @Autowired
    protected ReportsClientProperties reportingClientConfig;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Screens screens;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * Open input parameters dialog if report has parameters otherwise print report
     *
     * @param report - target report
     * @param screen - caller window
     */
    public void runReport(Report report, FrameOwner screen) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }

        if (report.getInputParameters() != null && report.getInputParameters().size() > 0
                || inputParametersRequiredByTemplates(report)) {
            openReportParamsDialog(screen, report, null, null, null);
        } else {
            printReport(report, ParamsMap.empty(), screen);
        }
    }

    /**
     * Open input parameters dialog if report has parameters otherwise print report.
     * The method allows to select target template code, pass single parameter to report, and set output file name.
     *
     * @param report         target report
     * @param screen         caller window
     * @param parameter      input parameter linked with passed parameter value
     * @param parameterValue parameter value
     * @param templateCode   target template code
     * @param outputFileName name for output file
     */
    public void runReport(Report report, FrameOwner screen, final ReportInputParameter parameter, final Object parameterValue,
                          @Nullable String templateCode, @Nullable String outputFileName) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }
        List<ReportInputParameter> params = report.getInputParameters();

        boolean reportHasMoreThanOneParameter = params != null && params.size() > 1;
        boolean inputParametersRequiredByTemplates = inputParametersRequiredByTemplates(report);

        Object resultingParamValue = convertParameterIfNecessary(parameter, parameterValue,
                reportHasMoreThanOneParameter || inputParametersRequiredByTemplates);

        boolean reportTypeIsSingleEntity = ParameterType.ENTITY == parameter.getType() && resultingParamValue instanceof Collection;
        boolean moreThanOneEntitySelected = resultingParamValue instanceof Collection && ((Collection) resultingParamValue).size() > 1;

        if (reportHasMoreThanOneParameter || inputParametersRequiredByTemplates) {
            boolean bulkPrint = reportTypeIsSingleEntity && moreThanOneEntitySelected;
            openReportParamsDialog(screen, report,
                    ParamsMap.of(parameter.getAlias(), resultingParamValue),
                    parameter, templateCode, outputFileName, bulkPrint);
        } else {
            if (reportTypeIsSingleEntity) {
                Collection selectedEntities = (Collection) resultingParamValue;
                if (moreThanOneEntitySelected) {
                    bulkPrint(report, parameter.getAlias(), selectedEntities, screen);
                } else if (selectedEntities.size() == 1) {
                    printReport(report, ParamsMap.of(parameter.getAlias(), selectedEntities.iterator().next()), templateCode, outputFileName, screen);
                }
            } else {
                printReport(report, ParamsMap.of(parameter.getAlias(), resultingParamValue), templateCode, outputFileName, screen);
            }
        }
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName) {
        printReportSync(report, params, templateCode, outputFileName, null);
    }

    /**
     * Print report synchronously
     *
     * @param report - target report
     * @param params - report parameters (map keys should match with parameter aliases)
     */
    public void printReport(Report report, Map<String, Object> params) {
        printReportSync(report, params, null, null, null);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report - target report
     * @param params - report parameters (map keys should match with parameter aliases)
     * @param screen - caller window
     */
    public void printReport(Report report, Map<String, Object> params, FrameOwner screen) {
        printReport(report, params, null, null, screen);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param screen         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode,
                            @Nullable String outputFileName, @Nullable FrameOwner screen) {
        printReport(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode,
                            @Nullable String outputFileName, @Nullable ReportOutputType outputType, @Nullable FrameOwner screen) {

        if (screen != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            printReportBackground(report, params, templateCode, outputFileName, outputType, screen);
        } else {
            printReportSync(report, params, templateCode, outputFileName, outputType, screen);
        }
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param screen         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode,
                                @Nullable String outputFileName, @Nullable FrameOwner screen) {
        printReportSync(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode,
                                @Nullable String outputFileName, @Nullable ReportOutputType outputType,
                                @Nullable FrameOwner screen) {
        ReportOutputDocument document = getReportResult(report, params, templateCode, outputType);

        showReportResult(document, params, templateCode, outputFileName, screen);
    }

    /**
     * Generate ReportOutputDocument
     *
     * @param report       - target report
     * @param params       - report parameters (map keys should match with parameter aliases)
     * @param templateCode - target template code
     * @return resulting ReportOutputDocument
     */
    public ReportOutputDocument getReportResult(Report report, Map<String, Object> params, @Nullable String templateCode) {
        return getReportResult(report, params, templateCode, null);
    }

    /**
     * Generate ReportOutputDocument
     *
     * @param report       - target report
     * @param params       - report parameters (map keys should match with parameter aliases)
     * @param templateCode - target template code
     * @param outputType   - target output type
     * @return resulting ReportOutputDocument
     */
    public ReportOutputDocument getReportResult(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable ReportOutputType outputType) {
        ReportOutputDocument document;
        if (StringUtils.isBlank(templateCode) && outputType == null) {
            document = reports.createReport(report, params);
        } else if (!StringUtils.isBlank(templateCode) && outputType == null) {
            document = reports.createReport(report, templateCode, params);
        } else if (!StringUtils.isBlank(templateCode) && outputType != null) {
            document = reports.createReport(report, templateCode, params, outputType);
        } else {
            document = reports.createReport(report, params, outputType);
        }
        return document;
    }

    protected void showReportResult(ReportOutputDocument document, Map<String, Object> params,
                                    @Nullable String templateCode, @Nullable String outputFileName, @Nullable FrameOwner screen) {
        showReportResult(document, params, templateCode, outputFileName, null, screen);
    }

    protected void showReportResult(ReportOutputDocument document, Map<String, Object> params,
                                    @Nullable String templateCode, @Nullable String outputFileName,
                                    @Nullable ReportOutputType outputType, @Nullable FrameOwner screen) {

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
            String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();

            downloader.download(new ByteArrayDataProvider(byteArr, uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()), documentName, exportFormat);
        }
    }

    /**
     * Print report in background task with window, supports cancel
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param screen         - caller window
     */
    public void printReportBackground(Report report, Map<String, Object> params,
                                      @Nullable String templateCode, @Nullable String outputFileName, FrameOwner screen) {
        printReportBackground(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report in background task with window, supports cancel
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReportBackground(Report report, final Map<String, Object> params, @Nullable String templateCode,
                                      @Nullable String outputFileName, @Nullable ReportOutputType outputType, FrameOwner screen) {
        Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();
        //todo https://github.com/Haulmont/jmix-reports/issues/22
        //UUID userSessionId = currentAuthentication.getUser().getId();

        Screen hostScreen = UiControllerUtils.getScreen(screen);

        BackgroundTask<Integer, ReportOutputDocument> task =
                new BackgroundTask<Integer, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, hostScreen) {

                    @SuppressWarnings("UnnecessaryLocalVariable")
                    @Override
                    public ReportOutputDocument run(TaskLifeCycle<Integer> taskLifeCycle) {
                        ReportOutputDocument result = getReportResult(targetReport, params, templateCode, outputType);
                        return result;
                    }

                    @Override
                    public boolean handleException(Exception ex) {
                        if (ex instanceof ReportingException) {
                            if (ex instanceof FailedToConnectToOpenOfficeException) {
                                String caption = messages.getMessage(ReportGuiManager.class, "reportException.failedConnectToOffice");
                                return showErrorNotification(caption);
                            } else if (ex instanceof NoOpenOfficeFreePortsException) {
                                String caption = messages.getMessage(ReportGuiManager.class, "reportException.noOpenOfficeFreePorts");
                                return showErrorNotification(caption);
                            }
                        }
                        return super.handleException(ex);
                    }

                    protected boolean showErrorNotification(String text) {
                        Screen ownerScreen = this.getOwnerScreen();
                        if (ownerScreen != null) {
                            Window window = ownerScreen.getWindow();
                            if (window != null) {
                                ScreenContext screenContext = ComponentsHelper.getScreenContext(window);
                                if (screenContext != null) {
                                    Notifications notifications = screenContext.getNotifications();
                                    notifications.create()
                                            .withCaption(text)
                                            .withType(Notifications.NotificationType.ERROR)
                                            .show();
                                    return true;
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    public void done(ReportOutputDocument document) {
                        showReportResult(document, params, templateCode, outputFileName, outputType, screen);
                    }

                    @Override
                    public void canceled() {
                        super.canceled();
                        //todo https://github.com/Haulmont/jmix-reports/issues/22
                        //reportService.cancelReportExecution(userSessionId, report.getId());
                    }
                };

        String caption = messages.getMessage(ReportGuiManager.class, "runReportBackgroundTitle");
        String description = messages.getMessage(ReportGuiManager.class, "runReportBackgroundMessage");

        dialogs.createBackgroundWorkDialog(task.getOwnerScreen(), task)
                .withCancelAllowed(true)
                .withCaption(caption)
                .withMessage(description)
                .show();
    }

    /**
     * Return list of reports, available for certain screen, user and input parameter
     *
     * @param screenId            - id of the screen
     * @param user                - caller user
     * @param inputValueMetaClass - meta class of report input parameter
     */
    public List<Report> getAvailableReports(@Nullable String screenId, @Nullable UserDetails user, @Nullable MetaClass inputValueMetaClass) {
        MetaClass metaClass = metadata.getClass(Report.class);
        LoadContext<Report> lc = new LoadContext<>(metaClass);
        lc.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
        FetchPlan fetchPlan = fetchPlans.builder(Report.class)
                .add("name")
                .add("localeNames")
                .add("description")
                .add("code")
                .add("group", FetchPlan.LOCAL)
                .add("updateTs")
                .build();

        lc.setFetchPlan(fetchPlan);
        lc.setQueryString("select r from report_Report r where r.system <> true");
        reportSecurityManager.applySecurityPolicies(lc, screenId, user);
        reportSecurityManager.applyPoliciesByEntityParameters(lc, inputValueMetaClass);
        return dataManager.loadList(lc);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrint(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType, String alias,
                          Collection selectedEntities, @Nullable FrameOwner screen, @Nullable Map<String, Object> additionalParameters) {
        if (screen != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            bulkPrintBackground(report, templateCode, outputType, alias, selectedEntities, screen, additionalParameters);
        } else {
            bulkPrintSync(report, templateCode, outputType, alias, selectedEntities, screen, additionalParameters);
        }
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report               target report
     * @param alias                parameter alias
     * @param selectedEntities     list of selected entities
     * @param screen               caller window
     * @param additionalParameters user-defined parameters
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen,
                          @Nullable Map<String, Object> additionalParameters) {
        bulkPrint(report, null, null, alias, selectedEntities, screen, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as inputParameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report           - target report
     * @param alias            - inputParameter alias
     * @param selectedEntities - list of selected entities
     * @param screen           - caller window
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen) {
        bulkPrint(report, alias, selectedEntities, screen, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities) {
        bulkPrintSync(report, alias, selectedEntities, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report               - target report
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen,
                              Map<String, Object> additionalParameters) {
        bulkPrintSync(report, null, null, alias, selectedEntities, screen, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType,
                              String alias, Collection selectedEntities, @Nullable FrameOwner screen,
                              Map<String, Object> additionalParameters) {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            Map<String, Object> map = new HashMap<>();
            map.put(alias, selectedEntity);
            if (additionalParameters != null) {
                map.putAll(additionalParameters);
            }
            paramsList.add(map);
        }

        ReportOutputDocument reportOutputDocument = reports.bulkPrint(report, templateCode, outputType, paramsList);
        String documentName = reportOutputDocument.getDocumentName();

        downloader.download(new ByteArrayDataProvider(reportOutputDocument.getContent(), uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()), documentName, DownloadFormat.ZIP);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as inputParameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param alias            - inputParameter alias
     * @param selectedEntities - list of selected entities
     * @param screen           - caller window
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen) {
        bulkPrintSync(report, alias, selectedEntities, screen, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report               - target report
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param window               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, FrameOwner window,
                                    Map<String, Object> additionalParameters) {
        bulkPrintBackground(report, null, null, alias, selectedEntities, window, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType,
                                    String alias, Collection selectedEntities, FrameOwner screen,
                                    Map<String, Object> additionalParameters) {
        Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();

        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            Map<String, Object> map = new HashMap<>();
            map.put(alias, selectedEntity);
            if (additionalParameters != null) {
                map.putAll(additionalParameters);
            }
            paramsList.add(map);
        }

        Screen hostScreen = UiControllerUtils.getScreen(screen);

        BackgroundTask<Integer, ReportOutputDocument> task =
                new BackgroundTask<Integer, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, hostScreen) {
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    @Override
                    public ReportOutputDocument run(TaskLifeCycle<Integer> taskLifeCycle) {
                        ReportOutputDocument result = reports.bulkPrint(targetReport, templateCode, outputType, paramsList);
                        return result;
                    }

                    @Override
                    public void done(ReportOutputDocument result) {
                        String documentName = result.getDocumentName();
                        downloader.download(new ByteArrayDataProvider(result.getContent(), uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()), documentName, DownloadFormat.ZIP);
                    }
                };

        String caption = messages.getMessage(getClass(), "runReportBackgroundTitle");
        String description = messages.getMessage(getClass(), "runReportBackgroundMessage");

        dialogs.createBackgroundWorkDialog(task.getOwnerScreen(), task)
                .withCancelAllowed(true)
                .withCaption(caption)
                .withMessage(description)
                .show();
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param screen           - caller window
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, FrameOwner screen) {
        bulkPrintBackground(report, alias, selectedEntities, screen, null);
    }

    /**
     * Check if the meta class is applicable for the input parameter
     */
    public boolean parameterMatchesMetaClass(ReportInputParameter parameter, MetaClass metaClass) {
        if (isNotBlank(parameter.getEntityMetaClass())) {
            MetaClass parameterMetaClass = metadata.getClass(parameter.getEntityMetaClass());
            return (metaClass.equals(parameterMetaClass) || metaClass.getAncestors().contains(parameterMetaClass));
        } else {
            return false;
        }
    }

    /**
     * Defensive copy
     */
    protected Report getReportForPrinting(Report report) {
        Report copy = metadataTools.copy(report);
        copy.setIsTmp(report.getIsTmp());
        return copy;
    }

    protected void openReportParamsDialog(FrameOwner screen, Report report, @Nullable Map<String, Object> parameters,
                                          @Nullable ReportInputParameter inputParameter, @Nullable String templateCode,
                                          @Nullable String outputFileName,
                                          boolean bulkPrint) {
        InputParametersDialog inputParametersDialog = screens.create(InputParametersDialog.class, OpenMode.DIALOG);
        inputParametersDialog.setReport(report);
        inputParametersDialog.setInputParameter(inputParameter);
        inputParametersDialog.setParameters(parameters);
        inputParametersDialog.setTemplateCode(templateCode);
        inputParametersDialog.setOutputFileName(outputFileName);
        inputParametersDialog.setBulkPrint(bulkPrint);
        inputParametersDialog.show();
    }

    protected void openReportParamsDialog(FrameOwner screen, Report report, @Nullable Map<String, Object> parameters,
                                          @Nullable String templateCode, @Nullable String outputFileName) {
        openReportParamsDialog(screen, report, parameters, null, templateCode, outputFileName, false);
    }

    @Nullable
    protected Object convertParameterIfNecessary(ReportInputParameter parameter, @Nullable Object paramValue,
                                                 boolean useForInputParametersForm) {
        Object resultingParamValue = paramValue;
        if (ParameterType.ENTITY == parameter.getType()) {
            if (paramValue instanceof Collection || paramValue instanceof ParameterPrototype) {
                resultingParamValue = handleCollectionParameter(paramValue,
                        useForInputParametersForm);
            }
        } else if (ParameterType.ENTITY_LIST == parameter.getType()) {
            if (!(paramValue instanceof Collection) && !(paramValue instanceof ParameterPrototype)) {
                resultingParamValue = Collections.singletonList(paramValue);
            } else if (paramValue instanceof ParameterPrototype && useForInputParametersForm) {
                resultingParamValue = handleCollectionParameter(paramValue, false);
            }
        }

        return resultingParamValue;
    }

    @Nullable
    protected Object handleCollectionParameter(@Nullable Object paramValue, boolean convertToSingleItem) {
        Collection paramValueWithCollection = null;
        if (paramValue instanceof Collection) {
            paramValueWithCollection = (Collection) paramValue;
        } else if (paramValue instanceof ParameterPrototype) {
            ParameterPrototype prototype = (ParameterPrototype) paramValue;
            paramValueWithCollection = reports.loadDataForParameterPrototype(prototype);
        }

        if (CollectionUtils.isEmpty(paramValueWithCollection)) {
            return null;
        }

        if (convertToSingleItem && paramValueWithCollection.size() == 1) {
            //if the case of several params we can not do bulk print, because the params should be filled, so we get only first object from the list
            return paramValueWithCollection.iterator().next();
        }

        return paramValueWithCollection;
    }

    public boolean inputParametersRequiredByTemplates(Report report) {
        return report.getTemplates() != null && report.getTemplates().size() > 1 || containsAlterableTemplate(report);
    }

    public boolean containsAlterableTemplate(Report report) {
        if (report.getTemplates() == null)
            return false;
        for (ReportTemplate template : report.getTemplates()) {
            if (supportAlterableForTemplate(template)) {
                return true;
            }
        }
        return false;
    }

    public boolean supportAlterableForTemplate(ReportTemplate template) {
        if (BooleanUtils.isTrue(template.getCustom())) {
            return false;
        }
        if (template.getReportOutputType() == ReportOutputType.CHART || template.getReportOutputType() == ReportOutputType.TABLE) {
            return false;
        }
        return BooleanUtils.isTrue(template.getAlterable());
    }
}