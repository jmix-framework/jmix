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

package io.jmix.reportsui.gui.actions;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.run.InputParametersFrame;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractPrintFormAction extends AbstractAction implements Action.HasBeforeActionPerformedHandler {

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Messages messages;

    @Autowired
    protected ScreenBuilders screenBuilder;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    protected AbstractPrintFormAction(String id) {
        super(id);
    }

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass) {
        openRunReportScreen(screen, selectedValue, inputValueMetaClass, null);
    }

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass,
                                       @Nullable String outputFileName) {
        BaseUser user = (BaseUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Report> reports = reportGuiManager.getAvailableReports(screen.getId(), user, inputValueMetaClass);

        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        if (reports.size() > 1) {
            if (CollectionUtils.isNotEmpty(reports)) {
                Report report = reports.iterator().next();
                Report reloadedReport = reloadReport(report);
                ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);

                Screen reportPrintScreen = screenBuilder.screen(screen).withScreenId("report$Report.run")
                        .withOpenMode(OpenMode.DIALOG)
                        .withOptions(new MapScreenOptions(ParamsMap.of(
                                InputParametersFrame.REPORT_PARAMETER, reports,
                                InputParametersFrame.PARAMETERS_PARAMETER, parameter
                        )))
                        .build();

                reportPrintScreen.addAfterCloseListener(afterCloseEvent -> reportGuiManager.runReport(reloadedReport, screen, parameter, selectedValue, null, outputFileName));

                reportPrintScreen.show();
            }


        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            Report reloadedReport = reloadReport(report);
            ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);
            if (selectedValue instanceof ParameterPrototype) {
                ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
            }
            reportGuiManager.runReport(reloadedReport, screen, parameter, selectedValue, null, outputFileName);
        } else {
            Notifications notifications = screenContext.getNotifications();

            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage(ReportGuiManager.class, "report.notFoundReports"))
                    .show();
        }
    }

    protected ReportInputParameter getParameterAlias(Report report, MetaClass inputValueMetaClass) {
        for (ReportInputParameter parameter : report.getInputParameters()) {
            if (reportGuiManager.parameterMatchesMetaClass(parameter, inputValueMetaClass)) {
                return parameter;
            }
        }

        throw new ReportingException(String.format("Selected report [%s] doesn't have parameter with class [%s].",
                report.getName(), inputValueMetaClass));
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }

    protected Report reloadReport(Report report) {
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.print");
        return dataManager.load(Id.of(report))
                .fetchPlan(fetchPlan)
                .one();
    }
}