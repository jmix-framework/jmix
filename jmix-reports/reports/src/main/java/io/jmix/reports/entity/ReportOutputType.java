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

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public enum ReportOutputType implements EnumClass<Integer> {
    XLS(0, JmixReportOutputType.xls),
    DOC(10, JmixReportOutputType.doc),
    PDF(20, JmixReportOutputType.pdf),
    HTML(30, JmixReportOutputType.html),
    DOCX(40, JmixReportOutputType.docx),
    XLSX(50, JmixReportOutputType.xlsx),
    CUSTOM(60, JmixReportOutputType.custom),
    CHART(70, JmixReportOutputType.chart),
    CSV(80, JmixReportOutputType.csv),
    TABLE(90, JmixReportOutputType.table),
    PIVOT_TABLE(100, JmixReportOutputType.pivot);

    private Integer id;

    private com.haulmont.yarg.structure.ReportOutputType outputType;

    @Override
    public Integer getId() {
        return id;
    }

    public com.haulmont.yarg.structure.ReportOutputType getOutputType() {
        return outputType;
    }

    ReportOutputType(Integer id, com.haulmont.yarg.structure.ReportOutputType outputType) {
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