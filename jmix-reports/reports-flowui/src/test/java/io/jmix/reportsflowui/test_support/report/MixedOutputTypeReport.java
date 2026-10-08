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
 * Report with three templates: two of them allow the user to alter the output type, the third one does not.
 */
@ReportDef(
        name = "Report with alterable and fixed templates",
        code = MixedOutputTypeReport.CODE
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = MixedOutputTypeReport.ALTERABLE_TEMPLATE,
        outputType = ReportOutputType.HTML,
        alterableOutput = true,
        isDefault = true,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
@TemplateDef(
        code = MixedOutputTypeReport.SECOND_ALTERABLE_TEMPLATE,
        outputType = ReportOutputType.HTML,
        alterableOutput = true,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
@TemplateDef(
        code = MixedOutputTypeReport.FIXED_TEMPLATE,
        outputType = ReportOutputType.HTML,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
public class MixedOutputTypeReport {

    public static final String CODE = "MIXED_OUTPUT_TYPE";
    public static final String ALTERABLE_TEMPLATE = "alterable";
    public static final String SECOND_ALTERABLE_TEMPLATE = "second-alterable";
    public static final String FIXED_TEMPLATE = "fixed";
}
