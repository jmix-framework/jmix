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

package io.jmix.reports.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Contains import information about created reports or updated reports
 */
public class ReportImportResult implements Serializable {
    private static final long serialVersionUID = -1796078629837052922L;

    protected Collection<Report> importedReports;
    protected Collection<Report> updatedReports;
    protected Collection<Report> createdReports;

    protected Collection<Exception> innerExceptions;

    public Collection<Report> getImportedReports() {
        return importedReports == null ? Collections.emptySet() : importedReports;
    }

    public Collection<Report> getUpdatedReports() {
        return updatedReports == null ? Collections.emptySet() : updatedReports;
    }

    public Collection<Report> getCreatedReports() {
        return createdReports == null ? Collections.emptySet() : createdReports;
    }

    public Collection<Exception> getInnerExceptions() {
        return innerExceptions == null ? Collections.emptyList() : innerExceptions;
    }

    public boolean hasErrors(){
        return getInnerExceptions().size() != 0;
    }

    public void addException(Exception exception) {
        if (innerExceptions == null) {
            innerExceptions = new ArrayList<>();
        }
        innerExceptions.add(exception);
    }

    public void addImportedReport(Report importedReport) {
        if (importedReports == null) {
            importedReports = new HashSet<>();
        }
        importedReports.add(importedReport);
    }

    public void addUpdatedReport(Report updatedReport) {
        if (updatedReports == null) {
            updatedReports = new HashSet<>();
        }
        updatedReports.add(updatedReport);
    }

    public void addCreatedReport(Report newReport) {
        if (createdReports == null) {
            createdReports = new HashSet<>();
        }
        createdReports.add(newReport);
    }
}
