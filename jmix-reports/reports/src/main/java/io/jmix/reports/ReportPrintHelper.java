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

import com.haulmont.yarg.structure.ReportOutputType;

import org.springframework.lang.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jmix.reports.entity.ReportOutputType.*;

public class ReportPrintHelper {

    private static HashMap<ReportOutputType, String> mimeTypes = new HashMap<>();

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

        mimeTypes.put(ReportOutputType.xls, "application/vnd.ms-excel");
        mimeTypes.put(ReportOutputType.xlsx, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.put(ReportOutputType.doc, "application/doc");
        mimeTypes.put(ReportOutputType.docx, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.put(ReportOutputType.pdf, "application/pdf");
        mimeTypes.put(ReportOutputType.html, "text/html");
        mimeTypes.put(ReportOutputType.csv, "application/csv");
    }

    @Nullable
    public static String getMimeType(ReportOutputType outputType) {
        return mimeTypes.get(outputType);
    }

    public static Map<String, List<io.jmix.reports.entity.ReportOutputType>> getInputOutputTypesMapping() {
        return inputOutputTypesMapping;
    }
}