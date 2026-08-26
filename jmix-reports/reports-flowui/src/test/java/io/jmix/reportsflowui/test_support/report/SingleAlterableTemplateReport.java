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
 * Report with a single template that allows the user to alter the output type and without input parameters,
 * so the output type is the only thing the input parameters dialog can ask for.
 */
@ReportDef(
        name = "Report with a single alterable template",
        code = SingleAlterableTemplateReport.CODE
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        alterableOutput = true,
        isDefault = true,
        filePath = "io/jmix/reportsflowui/test_support/report/FixedOutputTemplate.html"
)
public class SingleAlterableTemplateReport {

    public static final String CODE = "SINGLE_ALTERABLE_TEMPLATE";
}
