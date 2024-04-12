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

import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.entity.Report;

/**
 * Interface is used for running reports
 */
public interface ReportRunner {

    /**
     * Runs the report based on the information from the {@link ReportRunContext}. The run context may be created
     * manually using the constructor or using the {@link FluentReportRunner}.
     *
     * @param context the object that contains all information required to run the report
     * @return report execution result
     */
    ReportOutputDocument run(ReportRunContext context);

    /**
     * Creates an instance of {@link FluentReportRunner} for a report with specified code.
     * <br>
     * Usage examples:
     * <pre>
     * ReportRunContext context = reportRunner.byReportCode("orders-report")
     *                 .withParams(paramsMap)
     *                 .withOutputType(ReportOutputType.PDF)
     *                 .buildContext();
     *
     *  ReportOutputDocument document = reportRunner.byReportCode("orders-report")
     *                 .addParam("orders", ordersList)
     *                 .withTemplateCode("orders-template")
     *                 .run();
     * </pre>
     *
     * @param reportCode report code
     * @return instance of {@link FluentReportRunner}
     */
    FluentReportRunner byReportCode(String reportCode);

    /**
     * Creates an instance of {@link FluentReportRunner} for specified report.
     * <br>
     *
     * Usage examples:
     * <pre>
     * ReportRunContext context = reportRunner.byReportEntity(report)
     *                 .withParams(paramsMap)
     *                 .withTemplateCode("orders-template")
     *                 .buildContext();
     *
     * ReportOutputDocument document = reportRunner.byReportEntity(report)
     *                 .addParam("orders", orders)
     *                 .withOutputType(ReportOutputType.PDF)
     *                 .withOutputNamePattern("Orders")
     *                 .run();
     * </pre>
     *
     * @param report report entity
     * @return instance of {@link FluentReportRunner}
     */
    FluentReportRunner byReportEntity(Report report);
}
