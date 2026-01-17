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

import io.jmix.core.metamodel.datatype.EnumClass;
import org.apache.commons.lang3.StringUtils;

import org.springframework.lang.Nullable;

/**
 * Kind of output content produced by the reporting.
 * Generally, reporting produces either a document file or a set of data to be displayed in a UI component.
 */
public enum ReportOutputType implements EnumClass<Integer> {
    /**
     * Old-school binary Excel document.
     */
    XLS(0, JmixReportOutputType.xls),
    /**
     * Old-school binary Word document.
     */
    DOC(10, JmixReportOutputType.doc),
    /**
     * PDF document.
     */
    PDF(20, JmixReportOutputType.pdf),
    /**
     * HTML output.
     */
    HTML(30, JmixReportOutputType.html),
    /**
     * DOCX document.
     */
    DOCX(40, JmixReportOutputType.docx),
    /**
     * XLSX document.
     */
    XLSX(50, JmixReportOutputType.xlsx),
    /**
     * Any custom output, the format is determined by the template renderer.
     */
    CUSTOM(60, JmixReportOutputType.custom),
    /**
     * <b>Note: not supported at the moment.</b>
     * The produced output is displayed in the application UI in a Chart component.
     */
    CHART(70, JmixReportOutputType.chart),
    /**
     * CSV (comma-separated values) file.
     */
    CSV(80, JmixReportOutputType.csv),
    /**
     * The produced output is displayed in the application UI in a DataGrid component.
     */
    TABLE(90, JmixReportOutputType.table),
    /**
     * <b>Note: not supported at the moment.</b>
     * The produced output is displayed in the application UI in a Pivot Table component.
     */
    PIVOT_TABLE(100, JmixReportOutputType.pivot);

    private Integer id;

    private io.jmix.reports.yarg.structure.ReportOutputType outputType;

    @Override
    public Integer getId() {
        return id;
    }

    public io.jmix.reports.yarg.structure.ReportOutputType getOutputType() {
        return outputType;
    }

    ReportOutputType(Integer id, io.jmix.reports.yarg.structure.ReportOutputType outputType) {
        this.id = id;
        this.outputType = outputType;
    }

    public static ReportOutputType fromId(Integer id) {
        for (ReportOutputType type : ReportOutputType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    @Nullable
    public static ReportOutputType getTypeFromExtension(String extension) {
        for (ReportOutputType outputType : ReportOutputType.values()) {
            if (StringUtils.equals(outputType.toString(), extension)) {
                return outputType;
            }
        }
        return null;
    }
}