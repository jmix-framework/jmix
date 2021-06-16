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

import com.google.common.base.Strings;
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
import io.jmix.reports.runner.ReportRunner;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("report_ReportRunner")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportRunnerImpl implements ReportRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportRunnerImpl.class);

    private static final String REPORT_RUN_FETCH_PLAN = "report.run";

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private ReportExecutionHistoryRecorder executionHistoryRecorder;

    @Autowired
    private ReportsProperties reportsProperties;

    @Autowired
    protected PrototypesLoader prototypesLoader;

    @Autowired
    protected ReportingAPI reportingApi;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ApplicationContext applicationContext;

    private Report report;
    private String reportCode;
    private Map<String, Object> params = new HashMap<>();
    private String templateCode;
    private ReportTemplate template;
    private ReportOutputType outputType;
    private String outputNamePattern;

    public ReportRunnerImpl(Report report) {
        this.report = report;
    }

    public ReportRunnerImpl(String reportCode) {
        this.reportCode = reportCode;
    }

    @Override
    public ReportRunnerImpl withParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    @Override
    public ReportRunnerImpl addParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    @Override
    public ReportRunnerImpl withTemplateCode(String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    @Override
    public ReportRunnerImpl withTemplate(ReportTemplate template) {
        this.template = template;
        return this;
    }

    @Override
    public ReportRunnerImpl withOutputType(ReportOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    @Override
    public ReportRunnerImpl withOutputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
        return this;
    }

    private Optional<Report> loadReportByCode(String reportCode) {
        return dataManager.load(Report.class)
                .query("e.code = :code")
                .parameter("code", reportCode)
                .fetchPlan(REPORT_RUN_FETCH_PLAN)
                .optional();
    }

    private Report getReportToUse() {
        if (this.report != null) {
            if (!entityStates.isLoadedWithFetchPlan(this.report, "report.run")) {
                return dataManager.load(Id.of(report))
                        .fetchPlan("report.run")
                        .one();
            } else {
                return this.report;
            }
        }
        if (!Strings.isNullOrEmpty(reportCode)) {
            Optional<Report> reportOpt = loadReportByCode(this.reportCode);
            if (reportOpt.isPresent()) {
                return reportOpt.get();
            }
        }
        log.error("Cannot evaluate report to run. report param: {}, reportCode param: {}", report, reportCode);
        throw new IllegalStateException("Cannot evaluate a report to run");
    }

    private ReportTemplate getReportTemplateToUse() {
        if (this.template != null) {
            return template;
        }
        if (!Strings.isNullOrEmpty(templateCode)) {
            ReportTemplate templateByCode = report.getTemplateByCode(templateCode);
            if (templateByCode == null) {
                throw new RuntimeException(String.format("Cannot find report template with code %s in report %s", templateCode, report.getCode()));
            }
            return templateByCode;
        }
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate == null)
            throw new ReportingException(String.format("No default template specified for report [%s]", report.getName()));
        return defaultTemplate;
    }

    @Override
    public ReportOutputDocument run() {
        report = getReportToUse();
        template = getReportTemplateToUse();

        if (!reportsProperties.isHistoryRecordingEnabled()) {
            return runReport();
        }

        ReportExecution reportExecution =
                executionHistoryRecorder.startExecution(report, params);
        try {
            ReportOutputDocument document = runReport();
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


    private ReportOutputDocument runReport() {
//            Report report = reportRunParams.getReport();
//            ReportTemplate template = reportRunParams.getReportTemplate();
//            io.jmix.reports.entity.ReportOutputType outputType = reportRunParams.getOutputType();
//            Map<String, Object> params = reportRunParams.getParams();
//            String outputNamePattern = reportRunParams.getOutputNamePattern();


        StopWatch stopWatch = null;
        MDC.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
        //TODO web context name
//        MDC.put("webContextName", globalConfig.getWebContextName());
        //todo https://github.com/Haulmont/jmix-reports/issues/22
//        executions.startExecution(report.getId().toString(), "Reporting");
        try {
            //TODO Slf4JStopWatch
//            stopWatch = new Slf4JStopWatch("Reporting#" + report.getName());
            List<String> prototypes = new LinkedList<>();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (param.getValue() instanceof ParameterPrototype)
                    prototypes.add(param.getKey());
            }
            Map<String, Object> resultParams = new HashMap<>(params);

            for (String paramName : prototypes) {
                ParameterPrototype prototype = (ParameterPrototype) params.get(paramName);
                List data = loadDataForParameterPrototype(prototype);
                resultParams.put(paramName, data);
            }

            if (template.isCustom()) {
                CustomFormatter customFormatter = applicationContext.getBean(CustomFormatter.class, report, template);
                template.setCustomReport(customFormatter);
            }

            com.haulmont.yarg.structure.ReportOutputType resultOutputType = (outputType != null) ? outputType.getOutputType() : template.getOutputType();

            return reportingApi.runReport(new RunParams(report).template(template).params(resultParams).output(resultOutputType).outputNamePattern(outputNamePattern));
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
            //todo https://github.com/Haulmont/jmix-reports/issues/22
//            executions.endExecution();
            MDC.remove("user");
            MDC.remove("webContextName");
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
    }

    public List loadDataForParameterPrototype(ParameterPrototype prototype) {
        return prototypesLoader.loadData(prototype);
    }
}
