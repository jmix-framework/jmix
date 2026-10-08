/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.test_support.report;

import io.jmix.reports.annotation.BandDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.ReportOutputType;

/**
 * Report with two alterable templates offering different sets of output types, so that an output type selected
 * for one of them cannot be selected for the other one. The templates are used for output type selection only,
 * their content is never rendered.
 */
@ReportDef(
        name = "Report with templates of different output types",
        code = DifferentOutputTypesReport.CODE
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = DifferentOutputTypesReport.WIDE_TEMPLATE,
        outputType = ReportOutputType.PDF,
        alterableOutput = true,
        isDefault = true,
        filePath = "io/jmix/reportsflowui/test_support/report/WideOutputTemplate.jrxml"
)
@TemplateDef(
        code = DifferentOutputTypesReport.NARROW_TEMPLATE,
        outputType = ReportOutputType.HTML,
        alterableOutput = true,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
public class DifferentOutputTypesReport {

    public static final String CODE = "DIFFERENT_OUTPUT_TYPES";
    public static final String WIDE_TEMPLATE = "wide";
    public static final String NARROW_TEMPLATE = "narrow";
}
