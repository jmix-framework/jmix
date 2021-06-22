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

package io.jmix.reports.runner.impl;

import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.exception.ReportingInterruptedException;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.ReportingAPI;
import com.haulmont.yarg.reporting.RunParams;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Id;
import io.jmix.reports.PrototypesLoader;
import io.jmix.reports.ReportExecutionHistoryRecorder;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.*;
import io.jmix.reports.libintegration.CustomFormatter;
import io.jmix.reports.runner.FluentReportRunner;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.util.ReportsUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("report_ReportRunner")
public class ReportRunnerImpl implements ReportRunner {

    @Autowired
    protected PrototypesLoader prototypesLoader;

    @Autowired
    protected ReportingAPI reportingAPI;

    @Autowired
    protected ObjectProvider<FluentReportRunner> fluentReportRunners;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected ReportExecutionHistoryRecorder executionHistoryRecorder;

    @Autowired
    protected ReportsUtils reportsUtils;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public ReportOutputDocument run(ReportRunContext context) {
        prepareContext(context);
        if (!reportsProperties.isHistoryRecordingEnabled()) {
            return createReportDocumentInternal(context);
        }

        ReportExecution reportExecution =
                executionHistoryRecorder.startExecution(context.getReport(), context.getParams());
        try {
            ReportOutputDocument document = createReportDocumentInternal(context);
            executionHistoryRecorder.markAsSuccess(reportExecution, document);
            return document;
        } catch (ReportCanceledException e) {
            executionHistoryRecorder.markAsCancelled(reportExecution);
            throw e;
        } catch (Exception e) {
            executionHistoryRecorder.markAsError(reportExecution, e);
            throw e;
        }
    }

    protected ReportOutputDocument createReportDocumentInternal(ReportRunContext context) {
        Report report = context.getReport();
        ReportTemplate template = context.getReportTemplate();
        ReportOutputType outputType = context.getOutputType();
        Map<String, Object> params = context.getParams();
        String outputNamePattern = context.getOutputNamePattern();

        StopWatch stopWatch = null;
        MDC.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
        //TODO web context name
//        MDC.put("webContextName", globalConfig.getWebContextName());
//        todo https://github.com/Haulmont/jmix-reports/issues/22
//        executions.startExecution(report.getId().toString(), "Reporting");
        try {
            //TODO Slf4JStopWatch
//            stopWatch = new Slf4JStopWatch("Reporting#" + report.getName());
            Map<String, Object> resultParams = new HashMap<>(params);

            params.entrySet()
                    .stream()
                    .filter(param -> param.getValue() instanceof ParameterPrototype)
                    .forEach(param -> {
                        ParameterPrototype prototype = (ParameterPrototype) param.getValue();
                        List data = prototypesLoader.loadData(prototype);
                        resultParams.put(param.getKey(), data);
                    });

            if (template.isCustom()) {
                CustomFormatter customFormatter = applicationContext.getBean(CustomFormatter.class, report, template);
                template.setCustomReport(customFormatter);
            }

            com.haulmont.yarg.structure.ReportOutputType resultOutputType = (outputType != null) ? outputType.getOutputType() : template.getOutputType();

            return reportingAPI.runReport(new RunParams(report).template(template).params(resultParams).output(resultOutputType).outputNamePattern(outputNamePattern));
        } catch (NoFreePortsException nfe) {
            throw new NoOpenOfficeFreePortsException(nfe.getMessage());
        } catch (OpenOfficeException ooe) {
            throw new FailedToConnectToOpenOfficeException(ooe.getMessage());
        } catch (com.haulmont.yarg.exception.UnsupportedFormatException fe) {
            throw new UnsupportedFormatException(fe.getMessage());
        } catch (com.haulmont.yarg.exception.ValidationException ve) {
            throw new ValidationException(ve.getMessage());
        } catch (ReportingInterruptedException ie) {
            throw new ReportCanceledException(String.format("Report is canceled. %s", ie.getMessage()));
        } catch (com.haulmont.yarg.exception.ReportingException re) {
//            todo https://github.com/Haulmont/jmix-reports/issues/22
//            Throwable rootCause = ExceptionUtils.getRootCause(re);
//            if (rootCause instanceof ResourceCanceledException) {
//                throw new ReportCanceledException(String.format("Report is canceled. %s", rootCause.getMessage()));
//            }
            //noinspection unchecked
            List<Throwable> list = ExceptionUtils.getThrowableList(re);
            StringBuilder sb = new StringBuilder();
            for (Iterator<Throwable> it = list.iterator(); it.hasNext(); ) {
                //noinspection ThrowableResultOfMethodCallIgnored
                sb.append(it.next().getMessage());
                if (it.hasNext())
                    sb.append("\n");
            }

            throw new ReportingException(sb.toString());
        } finally {
//            todo https://github.com/Haulmont/jmix-reports/issues/22
//            executions.endExecution();
            MDC.remove("user");
            MDC.remove("webContextName");
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
    }

    protected void prepareContext(ReportRunContext context) {
        Report report = context.getReport();
        context.setReport(reportsUtils.reloadReportIfNeeded(report, "report.edit"));

        ReportTemplate template = context.getReportTemplate();
        if (template == null) {
            template = getDefaultTemplate(report);
            context.setReportTemplate(template);
        }

        if (!entityStates.isLoadedWithFetchPlan(template, "template.edit")) {
            template = dataManager.load(Id.of(template))
                    .fetchPlan("template.edit")
                    .one();
            context.setReportTemplate(template);
        }
    }

    protected ReportTemplate getDefaultTemplate(Report report) {
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate == null)
            throw new ReportingException(String.format("No default template specified for report [%s]", report.getName()));
        return defaultTemplate;
    }

    @Override
    public FluentReportRunner byReportCode(String reportCode) {
        return fluentReportRunners.getObject(reportCode);
    }

    @Override
    public FluentReportRunner byReportEntity(Report report) {
        return fluentReportRunners.getObject(report);
    }
}
