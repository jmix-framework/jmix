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

package io.jmix.reports.runner;

import com.google.common.base.Strings;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Id;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class is used to run a report using various additional criteria:
 * <ul>
 *     <li>{@link Report} entity or report code</li>
 *     <li>{@link ReportTemplate} entity or template code: if none of these fields is set, the default template is used.</li>
 *     <li>Output type</li>
 *     <li>Output name pattern</li>
 *     <li>Input parameters</li>
 * </ul>
 * <br>
 * Use the {@link ReportRunner} bean to obtain an instance of the {@link FluentReportRunner}.
 */
@Component("report_FluentReportRunner")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentReportRunner {

    private static final String REPORT_RUN_FETCH_PLAN = "report.edit";

    private Report report;
    private String reportCode;
    private Map<String, Object> params = new HashMap<>();
    private String templateCode;
    private ReportTemplate template;
    private ReportOutputType outputType;
    private String outputNamePattern;

    private static final Logger log = LoggerFactory.getLogger(FluentReportRunner.class);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityStates entityStates;

    private ReportRunner reportRunner;

    public FluentReportRunner(Report report) {
        this.report = report;
    }

    public FluentReportRunner(String reportCode) {
        this.reportCode = reportCode;
    }

    @Autowired
    public void setReportRunner(ReportRunner reportRunner) {
        this.reportRunner = reportRunner;
    }

    /**
     * Sets a map with input parameters.
     *
     * @param params input parameters
     * @return current instance of fluent runner
     */
    public FluentReportRunner withParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * Adds an input parameter to the parameter map.
     *
     * @param alias parameter alias
     * @param value parameter value
     * @return current instance of fluent runner
     */
    public FluentReportRunner addParam(String alias, Object value) {
        params.put(alias, value);
        return this;
    }

    /**
     * Sets a code of template that will be used to run a report.
     *
     * @param templateCode template code
     * @return current instance of fluent runner
     */
    public FluentReportRunner withTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    /**
     * Sets a template that will be used to run a report.
     *
     * @param template report template
     * @return current instance of fluent runner
     */
    public FluentReportRunner withTemplate(@Nullable ReportTemplate template) {
        this.template = template;
        return this;
    }

    /**
     * Sets a type of output document.
     *
     * @param outputType type of output document.
     * @return current instance of fluent runner
     */
    public FluentReportRunner withOutputType(ReportOutputType outputType) {
        this.outputType = outputType;
        return this;
    }

    /**
     * Sets a name pattern of an output document.
     *
     * @param outputNamePattern name pattern of an output document
     * @return current instance of fluent runner
     */
    public FluentReportRunner withOutputNamePattern(@Nullable String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
        return this;
    }

    /**
     * Creates an instance of {@link ReportRunContext} based on the parameters specified for the runner.
     *
     * @return run context
     */
    public ReportRunContext buildContext() {
        Report report = getReportToUse();
        ReportTemplate reportTemplate = getReportTemplateToUse(report);
        return new ReportRunContext(report)
                .setReportTemplate(reportTemplate)
                .setOutputNamePattern(this.outputNamePattern)
                .setOutputType(this.outputType)
                .setParams(this.params);
    }

    /**
     * Builds a {@link ReportRunContext} instance and runs a report using this run context.
     *
     * @return report execution result
     */
    public ReportOutputDocument run() {
        return reportRunner.run(buildContext());
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
            if (report.getIsTmp()) {
                return this.report;
            }
            if (!entityStates.isLoadedWithFetchPlan(this.report, REPORT_RUN_FETCH_PLAN)) {
                return dataManager.load(Id.of(report))
                        .fetchPlan(REPORT_RUN_FETCH_PLAN)
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
            throw new ReportingException(String.format("Cannot find report with code %s", reportCode));
        }
        log.error("Cannot evaluate report to run. report param: {}, reportCode param: {}", report, reportCode);
        throw new IllegalStateException("Cannot evaluate a report to run");
    }

    private ReportTemplate getReportTemplateToUse(Report report) {
        if (this.template != null) {
            if (!entityStates.isLoadedWithFetchPlan(this.template, "template.edit")) {
                return dataManager.load(Id.of(template))
                        .fetchPlan("template.edit")
                        .one();
            }
            return this.template;
        }
        if (!Strings.isNullOrEmpty(templateCode)) {
            ReportTemplate templateByCode = report.getTemplateByCode(templateCode);
            if (templateByCode == null) {
                throw new ReportingException(String.format("Cannot find report template with code %s in report %s", templateCode, report.getCode()));
            }
            return templateByCode;
        }
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate == null)
            throw new ReportingException(String.format("No default template specified for report [%s]", report.getName()));
        return defaultTemplate;
    }


}
