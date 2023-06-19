/*
 * Copyright 2014 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.reporting;

import io.jmix.reports.yarg.structure.Report;
import io.jmix.reports.yarg.structure.ReportOutputType;

import java.io.Serializable;

/**
 * This interface describes reporting result object.
 * Generally returned by io.jmix.reports.yarg.reporting.ReportingAPI
 */
public interface ReportOutputDocument extends Serializable {

    Report getReport();

    byte[] getContent();

    String getDocumentName();

    ReportOutputType getReportOutputType();

    void setReport(Report report);

    void setContent(byte[] content);

    void setDocumentName(String documentName);

    void setReportOutputType(ReportOutputType reportOutputType);
}