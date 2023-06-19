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

package io.jmix.reportsflowui.runner.impl;

import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.runner.UiReportRunContext;

import java.util.concurrent.TimeUnit;

public class RunSingleReportBackgroundTask extends BackgroundTask<Integer, ReportOutputDocument> {

    protected ReportRunner reportRunner;
    protected Messages messages;
    protected Notifications notifications;

    protected UiReportRunnerSupport uiReportRunnerSupport;
    protected Report targetReport;
    protected UiReportRunContext context;

    public RunSingleReportBackgroundTask(long timeout, TimeUnit timeUnit, View<?> view, Report targetReport,
                                         ReportRunner runner, UiReportRunContext context) {
        super(timeout, timeUnit, view);
    }

    @Override
    public ReportOutputDocument run(TaskLifeCycle<Integer> taskLifeCycle) {
        context.setReport(targetReport);
        return reportRunner.run(context.getReportRunContext());
    }

    @Override
    public boolean handleException(Exception ex) {
        if (ex instanceof ReportingException) {
            if (ex instanceof FailedToConnectToOpenOfficeException) {
                String caption = messages.getMessage("io.jmix.reportsflowui.exception",
                        "reportException.failedConnectToOffice");
                return showErrorNotification(caption);
            } else if (ex instanceof NoOpenOfficeFreePortsException) {
                String caption = messages.getMessage("io.jmix.reportsflowui.exception",
                        "reportException.noOpenOfficeFreePorts");
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
        uiReportRunnerSupport.showResult(document, context);
    }

    @Override
    public void canceled() {
        super.canceled();
        //todo https://github.com/Haulmont/jmix-reports/issues/22
        //reportService.cancelReportExecution(userSessionId, report.getId());
    }
}
