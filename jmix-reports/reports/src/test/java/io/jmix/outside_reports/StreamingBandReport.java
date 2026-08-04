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

package io.jmix.outside_reports;

import io.jmix.reports.annotation.BandDef;
import io.jmix.reports.annotation.DataSetDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;

@ReportDef(
        name = "Streaming band report",
        code = "streaming-band-report",
        uuid = "01973162-6761-71a1-9d6b-b4ef0beac001",
        group = CorrectReportGroup.class
)
@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "Data",
        parent = "Root",
        streaming = true,
        dataSets = @DataSetDef(
                name = "Data",
                type = DataSetType.SQL,
                query = "select id from REPORT_REPORT"
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/outside_reports/CorrectReport.csv"
)
public class StreamingBandReport {
}
