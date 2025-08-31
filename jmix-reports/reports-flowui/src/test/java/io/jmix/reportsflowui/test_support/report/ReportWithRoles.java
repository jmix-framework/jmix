/*
 * Copyright 2025 Haulmont.
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

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reportsflowui.view.TestReportView2;
import io.jmix.reportsflowui.view.run.ReportRunView;

@ReportDef(
        name = ReportWithRoles.NAME,
        code = ReportWithRoles.CODE,
        group = SimpleReportGroup.class,
        system = true,
        description = """
                Report description
                """
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reportsflowui/SomeTemplate.html"
)
@AvailableInViews(
        viewIds = TestReportView2.ID,
        viewClasses = ReportRunView.class
)
@AvailableForRoles()
public class ReportWithRoles {
    public static final String NAME = "Report with roles";
    public static final String CODE = "REPORT_WITH_VIEW_ROLES";
    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_END_DATE = "endDate";


}
