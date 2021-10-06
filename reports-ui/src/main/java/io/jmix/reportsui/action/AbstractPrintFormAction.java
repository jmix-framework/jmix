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

package io.jmix.reportsui.action;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.reports.PrototypesLoader;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reportsui.runner.FluentUiReportRunner;
import io.jmix.reportsui.runner.ParametersDialogShowMode;
import io.jmix.reportsui.runner.UiReportRunner;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.reportsui.screen.report.run.ReportRun;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractPrintFormAction extends AbstractAction {

    @Autowired
    protected ReportSecurityManager reportSecurityManager;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Messages messages;

    @Autowired
    protected ScreenBuilders screenBuilder;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    protected UiReportRunner uiReportRunner;

    @Autowired
    protected Screens screens;

    @Autowired
    protected PrototypesLoader prototypesLoader;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    protected AbstractPrintFormAction(String id) {
        super(id);
    }

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass) {
        openRunReportScreen(screen, selectedValue, inputValueMetaClass, null);
    }

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass,
                                       @Nullable String outputFileName) {
        List<Report> reports = reportSecurityManager.getAvailableReports(
                screen.getId(),
                currentUserSubstitution.getEffectiveUser(),
                inputValueMetaClass);

        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        if (reports.size() > 1) {
            if (CollectionUtils.isNotEmpty(reports)) {
                ReportRun reportRunScreen = screenBuilder.lookup(Report.class, screen)
                        .withScreenClass(ReportRun.class)
                        .withOpenMode(OpenMode.DIALOG)
                        .withSelectHandler(selectedReports -> {
                            if (CollectionUtils.isNotEmpty(selectedReports)) {
                                Report report = selectedReports.iterator().next();
                                runReport(report, screen, selectedValue, inputValueMetaClass, outputFileName);
                            }
                        })
                        .build();
                reportRunScreen.setReports(reports);
                reportRunScreen.show();
            }
        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            runReport(report, screen, selectedValue, inputValueMetaClass, outputFileName);
        } else {
            Notifications notifications = screenContext.getNotifications();

            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage(AbstractPrintFormAction.class, "report.notFoundReports"))
                    .show();
        }
    }

    protected void runReport(Report report, Screen screen, Object selectedValue, MetaClass inputValueMetaClass, @Nullable String outputFileName) {
        Report reloadedReport = reloadReport(report);
        ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);
        if (selectedValue instanceof ParameterPrototype) {
            ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
        }
        runAndShow(reloadedReport, screen, parameter, selectedValue, outputFileName);
    }

    public void runAndShow(Report report, FrameOwner screen, ReportInputParameter reportInputParameter, Object parameterValue, @Nullable String outputFileName) {
        List<ReportInputParameter> params = report.getInputParameters();

        boolean reportHasMoreThanOneParameter = params != null && params.size() > 1;
        boolean inputParametersRequiredByTemplates = inputParametersRequiredByTemplates(report);

        Object resultingParamValue = convertParameterIfNecessary(reportInputParameter, parameterValue,
                reportHasMoreThanOneParameter || inputParametersRequiredByTemplates);

        boolean reportTypeIsSingleEntity = ParameterType.ENTITY == reportInputParameter.getType() && resultingParamValue instanceof Collection;
        boolean moreThanOneEntitySelected = resultingParamValue instanceof Collection && ((Collection) resultingParamValue).size() > 1;

        if (reportHasMoreThanOneParameter || inputParametersRequiredByTemplates) {
            boolean bulkPrint = reportTypeIsSingleEntity && moreThanOneEntitySelected;
            openReportParamsDialog(report, ParamsMap.of(reportInputParameter.getAlias(), resultingParamValue), outputFileName, reportInputParameter, bulkPrint);
        } else {
            FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(report)
                    .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                    .withOutputNamePattern(outputFileName);
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(screen);
            }
            if (reportTypeIsSingleEntity) {
                Collection selectedEntities = (Collection) resultingParamValue;
                if (moreThanOneEntitySelected) {
                    fluentRunner.runMultipleReports(reportInputParameter.getAlias(), selectedEntities);
                } else if (selectedEntities.size() == 1) {
                    fluentRunner.addParam(reportInputParameter.getAlias(), selectedEntities.iterator().next())
                            .runAndShow();
                }
            } else {
                fluentRunner.addParam(reportInputParameter.getAlias(), resultingParamValue)
                        .runAndShow();
            }
        }
    }

    protected void openReportParamsDialog(Report report, Map<String, Object> params,
                                          @Nullable String outputNamePattern, ReportInputParameter inputParameter,
                                          boolean bulkPrint) {
        InputParametersDialog inputParametersDialog = screens.create(InputParametersDialog.class, OpenMode.DIALOG);
        inputParametersDialog.setReport(report);
        inputParametersDialog.setInputParameter(inputParameter);
        inputParametersDialog.setParameters(params);
        inputParametersDialog.setOutputFileName(outputNamePattern);
        inputParametersDialog.setBulkPrint(bulkPrint);
        inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());
        inputParametersDialog.show();
    }

    @Nullable
    protected Object convertParameterIfNecessary(ReportInputParameter parameter, Object paramValue,
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
            paramValueWithCollection = prototypesLoader.loadData(prototype);
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

    protected ReportInputParameter getParameterAlias(Report report, MetaClass inputValueMetaClass) {
        for (ReportInputParameter parameter : report.getInputParameters()) {
            if (parameterMatchesMetaClass(parameter, inputValueMetaClass)) {
                return parameter;
            }
        }

        throw new ReportingException(String.format("Selected report [%s] doesn't have parameter with class [%s].",
                report.getName(), inputValueMetaClass));
    }

    protected boolean parameterMatchesMetaClass(ReportInputParameter parameter, MetaClass metaClass) {
        if (isNotBlank(parameter.getEntityMetaClass())) {
            MetaClass parameterMetaClass = metadata.getClass(parameter.getEntityMetaClass());
            return (metaClass.equals(parameterMetaClass) || metaClass.getAncestors().contains(parameterMetaClass));
        } else {
            return false;
        }
    }

    protected Report reloadReport(Report report) {
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.print");
        return dataManager.load(Id.of(report))
                .fetchPlan(fetchPlan)
                .one();
    }
}