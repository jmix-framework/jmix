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

package io.jmix.reports;

import io.jmix.reports.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@ManagedResource(description = "Manages import/export of the reports", objectName = "jmix.reports:type=ReportImportExport")
@Component("report_ReportImportExportManagementFacade")
public class ReportImportExportManagementFacade {

    @Autowired
    private ReportImportExport reportImportExport;

    @ManagedOperation(description = "Import all reports from the specified folder")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "path", description = "Path to folder with reports")
    })
    public String importReportsFromPath(String path) throws IOException {
        Collection<Report> reports = reportImportExport.importReportsFromPath(path);
        return reports.isEmpty() ? "No reports imported." : String.format("%d reports imported", reports.size());
    }
}