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

import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.*;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@Service(ReportService.NAME)
public class ReportServiceBean implements ReportService {

    @Autowired
    protected io.jmix.reports.ReportingApi reportingApi;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    @Override
    public Report storeReportEntity(Report report) {
        return reportingApi.storeReportEntity(report);
    }

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) {
        return reportingApi.createReport(report, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params, ReportOutputType outputType) {
        return reportingApi.createReport(report, params, outputType);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) {
        return reportingApi.createReport(report, templateCode, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params, ReportOutputType outputType) {
        return reportingApi.createReport(report, templateCode, params, outputType);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) {
        return reportingApi.createReport(report, template, params);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, templateCode, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) {
        return reportingApi.exportReports(reports);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) {
        return reportingApi.importReports(zipBytes);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions) {
        return reportingApi.importReports(zipBytes, importOptions);
    }

    @Override
    public ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions) {
        return reportingApi.importReportsWithResult(zipBytes, importOptions);
    }

    @Override
    public String convertToString(Report report) {
        return reportingApi.convertToString(report);
    }

    @Override
    public Report convertToReport(String xml) {
        return reportingApi.convertToReport(xml);
    }

    @Override
    public Report copyReport(Report source) {
        return reportingApi.copyReport(source);
    }

    @Override
    public ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList) {
        return reportingApi.bulkPrint(report, paramsList);
    }

    @Override
    public ReportOutputDocument bulkPrint(Report report, String templateCode, ReportOutputType outputType, List<Map<String, Object>> paramsList) {
        return reportingApi.bulkPrint(report, templateCode, outputType, paramsList);
    }

    @Override
    public MetaClass findMetaClassByDataSetEntityAlias(final String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters) {
        return reportingApi.findMetaClassByDataSetEntityAlias(alias, dataSetType, reportInputParameters);
    }

    @Override
    public List loadDataForParameterPrototype(ParameterPrototype prototype) {
        return reportingApi.loadDataForParameterPrototype(prototype);
    }

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        return objectToStringConverter.convertToString(parameterClass, paramValue);
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        return objectToStringConverter.convertFromString(parameterClass, paramValueStr);
    }

    @Override
    public void cancelReportExecution(UUID userSessionId, UUID reportId) {
        reportingApi.cancelReportExecution(userSessionId, reportId);
    }

    @Override
    public Date currentDateOrTime(ParameterType parameterType) {
        return reportingApi.currentDateOrTime(parameterType);
    }
}