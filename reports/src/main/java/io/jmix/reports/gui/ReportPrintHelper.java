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

package io.jmix.reports.gui;

import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.yarg.structure.ReportOutputType;

import java.util.*;

import static io.jmix.reports.entity.ReportOutputType.*;

public class ReportPrintHelper {

    private static HashMap<ReportOutputType, ExportFormat> exportFormats = new HashMap<>();

    private static Map<String, List<io.jmix.reports.entity.ReportOutputType>> inputOutputTypesMapping = new HashMap<>();

    static {
        inputOutputTypesMapping.put("docx", Arrays.asList(DOCX, HTML, PDF));
        inputOutputTypesMapping.put("doc", Arrays.asList(DOC, PDF));
        inputOutputTypesMapping.put("odt", Arrays.asList(DOC, PDF));
        inputOutputTypesMapping.put("xlsx", Arrays.asList(XLSX, HTML, PDF, CSV));
        inputOutputTypesMapping.put("xlsm", Arrays.asList(XLSX, PDF));
        inputOutputTypesMapping.put("xls", Arrays.asList(XLS, PDF));
        inputOutputTypesMapping.put("html", Arrays.asList(HTML, PDF));
        inputOutputTypesMapping.put("ftl", Arrays.asList(HTML, PDF));
        inputOutputTypesMapping.put("csv", Arrays.asList(CSV));
        inputOutputTypesMapping.put("jrxml", Arrays.asList(XLS, DOC, PDF, HTML, DOCX, XLSX, CSV));
        inputOutputTypesMapping.put("jasper", Arrays.asList(XLS, DOC, PDF, HTML, DOCX, XLSX, CSV));
        exportFormats.put(ReportOutputType.xls, ExportFormat.XLS);
        exportFormats.put(ReportOutputType.xlsx, ExportFormat.XLSX);
        exportFormats.put(ReportOutputType.doc, ExportFormat.DOC);
        exportFormats.put(ReportOutputType.docx, ExportFormat.DOCX);
        exportFormats.put(ReportOutputType.pdf, ExportFormat.PDF);
        exportFormats.put(ReportOutputType.html, ExportFormat.HTML);
        exportFormats.put(ReportOutputType.csv, ExportFormat.CSV);
    }

    public static ExportFormat getExportFormat(ReportOutputType outputType) {
        return exportFormats.get(outputType);
    }

    public static Map<String, List<io.jmix.reports.entity.ReportOutputType>> getInputOutputTypesMapping() {
        return inputOutputTypesMapping;
    }
}