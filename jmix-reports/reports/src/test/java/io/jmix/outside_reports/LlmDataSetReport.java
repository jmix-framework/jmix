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
import io.jmix.reports.annotation.LlmDataSetParameters;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;

/**
 * Two LLM data sets: one stating everything the annotation carries, one leaving the optional attributes out.
 */
@ReportDef(
        name = "LLM data set report",
        code = "llm-data-set-report",
        uuid = "019fe0a1-3b1c-7a10-8f21-5c0d2a7c9e01",
        group = CorrectReportGroup.class
)
@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "orders",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "orders",
                type = DataSetType.LLM,
                llm = @LlmDataSetParameters(
                        prompt = "Order numbers of this month",
                        maxResults = 50
                )
        )
)
@BandDef(
        name = "customers",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "customers",
                type = DataSetType.LLM,
                llm = @LlmDataSetParameters(prompt = "Customers of this month")
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/outside_reports/CorrectReport.csv"
)
public class LlmDataSetReport {
}
