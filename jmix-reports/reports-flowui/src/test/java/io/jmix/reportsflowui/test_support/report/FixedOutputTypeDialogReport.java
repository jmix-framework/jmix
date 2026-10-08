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
import io.jmix.reports.annotation.InputParameterDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;

/**
 * Report with a single template that does not allow the user to alter the output type.
 * The visible parameter makes every run open the input parameters dialog.
 */
@ReportDef(
        name = "Report with a fixed output type and a visible parameter",
        code = FixedOutputTypeDialogReport.CODE
)
@InputParameterDef(
        alias = FixedOutputTypeDialogReport.PARAM_TITLE,
        name = "Title",
        type = ParameterType.TEXT
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
public class FixedOutputTypeDialogReport {

    public static final String CODE = "FIXED_OUTPUT_TYPE_DIALOG";
    public static final String PARAM_TITLE = "title";
}
