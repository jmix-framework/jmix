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

package io.jmix.reports;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.entity.*;

import java.net.URI;
import java.util.*;

/**
 * API for reporting
 */
public interface Reports {

    String NAME = "report_Reports";

    String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    /**
     * Saves Report entity to the database.
     *
     * @param report report entity instance
     * @return saved instance
     */
    Report storeReportEntity(Report report);

    /**
     * Generates a report.
     *
     * @param report entity instance defining the report
     * @param params report parameters
     * @return report output
     */
    ReportOutputDocument createReport(Report report, Map<String, Object> params);

    /**
     * Generates a report.
     *
     * @param report     entity instance defining the report
     * @param params     report parameters
     * @param outputType desired report output type
     * @return report output
     */
    ReportOutputDocument createReport(Report report, Map<String, Object> params, ReportOutputType outputType);

    /**
     * Generates a report.
     *
     * @param report       entity instance defining the report
     * @param templateCode code of a template to use
     * @param params       report parameters
     * @return report output
     */
    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params);

    /**
     * Generates a report.
     *
     * @param report       entity instance defining the report
     * @param templateCode code of a template to use
     * @param params       report parameters
     * @param outputType   desired report output type
     * @return report output
     */
    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params, ReportOutputType outputType);

    /**
     * Generates a report.
     *
     * @param report   entity instance defining the report
     * @param template template to use
     * @param params   report parameters
     * @return report output
     */
    ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params);

    /**
     * Generates a report.
     *
     * @param reportRunParams all report parameters in a single POJO
     * @return report output
     */
    ReportOutputDocument createReport(ReportRunParams reportRunParams);

    /**
     * Generates a report and saves its output to the file storage.
     *
     * @param report   entity instance defining the report
     * @param params   report parameters
     * @param fileName output file name
     * @return FileDescriptor instance pointing to the report output
     */
    URI createAndSaveReport(Report report,
                                       Map<String, Object> params, String fileName);

    /**
     * Generates a report and saves its output to the file storage.
     *
     * @param report       entity instance defining the report
     * @param templateCode code of a template to use
     * @param params       report parameters
     * @param fileName     output file name
     * @return FileDescriptor instance pointing to the report output
     */
    URI createAndSaveReport(Report report, String templateCode,
                            Map<String, Object> params, String fileName);

    /**
     * Generates a report and saves its output to the file storage.
     *
     * @param report   entity instance defining the report
     * @param template template to use
     * @param params   report parameters
     * @param fileName output file name
     * @return FileDescriptor instance pointing to the report output
     */
    URI createAndSaveReport(Report report, ReportTemplate template,
                            Map<String, Object> params, String fileName);

    /**
     * Generates a report and saves its output to the file storage.
     *
     * @param reportRunParams all report parameters in a single POJO
     * @return FileDescriptor instance pointing to the report output
     */
    URI createAndSaveReport(ReportRunParams reportRunParams);

    /**
     * Exports all reports and their templates into one zip archive. Each report is exported into a separate zip
     * archive with 2 files (report.xml and a template file (for example MyReport.doc)).
     * For example:
     * return byte[] (bytes of zip arhive)
     * -- MegaReport.zip
     * ---- report.xml
     * ---- Mega report.xls
     * -- Other report.zip
     * ---- report.xml
     * ---- other report.odt
     *
     * @param reports Collection of Report objects to be exported.
     * @return ZIP byte array with zip archives inside.
     */
    byte[] exportReports(Collection<Report> reports);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes      ZIP archive as a byte array.
     * @param importOptions - report import options
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes      ZIP archive as a byte array.
     * @param importOptions report - import options
     * @return import result - collection of updated, created reports
     */
    ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    String convertToString(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    /**
     * Prints the report several times for each parameter map in the paramsList. Put the result files to zip archive.
     */
    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    /**
     * Prints the report several times for each parameter map in the paramsList. Put the result files to zip archive.
     */
    ReportOutputDocument bulkPrint(Report report, String templateCode, ReportOutputType outputType, List<Map<String, Object>> paramsList);

    <T extends Entity> T reloadEntity(T entity, FetchPlan view);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    String generateReportName(String sourceName);

    List loadDataForParameterPrototype(ParameterPrototype prototype);

    /**
     * Cancel report execution
     *
     * @param userSessionId - user session that started report execution
     * @param reportId      - identifier of executed report
     */
    void cancelReportExecution(UUID userSessionId, UUID reportId);

    /**
     * Get current date {@link Date} according to {@link ParameterType} value
     *
     * @param parameterType - ParameterType value.
     * @return adjusted Date
     */
    Date currentDateOrTime(ParameterType parameterType);

    String convertToString(Class parameterClass, Object paramValue);

    Object convertFromString(Class parameterClass, String paramValueStr);
}