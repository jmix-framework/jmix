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

package io.jmix.reportsui.runner;

import io.jmix.reports.entity.Report;

import java.util.Collection;

/**
 * Interface is used for running reports from UI
 */
public interface UiReportRunner {
    /**
     * Runs the report based on the information from the {@link UiReportRunContext} and shows the result. The run context may be created
     * manually using the constructor or using the {@link FluentUiReportRunner}.
     *
     * @param context the object that contains all information required to run the report from UI
     */
    void runAndShow(UiReportRunContext context);

    /**
     * Runs a report for each object from the specified collection. Objects in the collection should have the same type as an input parameter with specified alias.
     * If the report has other parameters besides the specified one, values for these parameters are copied for each report run.
     * As result, the ZIP archive with executed reports is downloaded.
     * <br>
     * For example, a report has an input parameter with alias "name" and the String type:
     * <pre>
     *  UiReportRunContext context = new UiReportRunContext(report)
     *                              .setOutputType(ReportOutputType.PDF);
     *  uiReportRunner.runMultipleReports(context, "name", namesList);
     * </pre>
     *  The report will be executed for each string from the "namesList" collection.
     *
     * @param context         the object that contains all information required to run the report from UI
     * @param multiParamAlias alias of the parameter for which a value from the collection is used for report execution
     * @param multiParamValue collection of values
     */
    void runMultipleReports(UiReportRunContext context, String multiParamAlias, Collection multiParamValue);

    /**
     * Creates an instance of {@link FluentUiReportRunner} for a report with specified code.
     * <br>
     * Usage examples:
     * <pre>
     * UiReportRunContext context = uiReportRunner.byReportCode("orders-report")
     *                 .withTemplateCode("order-template")
     *                 .withOutputNamePattern("Orders")
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED)
     *                 .inBackground(screen)
     *                 .buildContext();
     *
     * uiReportRunner.byReportCode("orders-report")
     *                 .addParam("orders", ordersList)
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.NO)
     *                 .runAndShow();
     *
     * uiReportRunner.byReportCode("orders-report")
     *                 .addParam("orders", ordersList)
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.YES)
     *                 .runAndShow();
     *
     * uiReportRunner.byReportCode("customer-orders-report")
     *                 .addParam("minOrdersDate", date)
     *                 .withOutputType(ReportOutputType.PDF)
     *                 .withTemplateCode(""orders-template"")
     *                 .runMultipleReports("customer", customersList);
     * </pre>
     *
     * @param reportCode report code
     * @return instance of {@link FluentUiReportRunner}
     */
    FluentUiReportRunner byReportCode(String reportCode);

    /**
     * Creates an instance of {@link FluentUiReportRunner} for specified report.
     * <br>
     * Usage examples:
     * <pre>
     * UiReportRunContext context = uiReportRunner.byReportEntity(report)
     *                 .withTemplateCode("order-template")
     *                 .withOutputNamePattern("Orders")
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED)
     *                 .inBackground(screen)
     *                 .buildContext();
     *
     * uiReportRunner.byReportEntity(report)
     *                 .addParam("orders", ordersList)
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.NO)
     *                 .runAndShow();
     *
     * uiReportRunner.byReportEntity(report)
     *                 .withTemplate(template)
     *                 .addParam("orders", ordersList)
     *                 .withParametersDialogShowMode(ParametersDialogShowMode.YES)
     *                 .runAndShow();
     *
     * uiReportRunner.byReportEntity(report)
     *                 .addParam("minOrdersDate", date)
     *                 .withOutputType(ReportOutputType.PDF)
     *                 .withTemplateCode(""orders-template"")
     *                 .runMultipleReports("customer", customersList);
     * </pre>
     *
     * @param report report entity
     * @return instance of {@link FluentUiReportRunner}
     */
    FluentUiReportRunner byReportEntity(Report report);
}
