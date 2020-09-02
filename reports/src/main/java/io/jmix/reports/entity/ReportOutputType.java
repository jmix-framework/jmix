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
package io.jmix.reports.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public enum ReportOutputType implements EnumClass<Integer> {
    XLS(0, CubaReportOutputType.xls),
    DOC(10, CubaReportOutputType.doc),
    PDF(20, CubaReportOutputType.pdf),
    HTML(30, CubaReportOutputType.html),
    DOCX(40, CubaReportOutputType.docx),
    XLSX(50, CubaReportOutputType.xlsx),
    CUSTOM(60, CubaReportOutputType.custom),
    CHART(70, CubaReportOutputType.chart),
    CSV(80, CubaReportOutputType.csv),
    TABLE(90, CubaReportOutputType.table),
    PIVOT_TABLE(100, CubaReportOutputType.pivot);

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