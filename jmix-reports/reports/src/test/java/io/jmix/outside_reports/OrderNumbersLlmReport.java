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
 * A report defined in code whose band is described in plain language instead of a query. Its query is
 * generated on every run, since an annotated report keeps none.
 * <p>
 * Lives outside the scanned package so that it is built only by the test that loads it: the scanned set is
 * what every annotated-report test sees, and two of them count the reports in it.
 */
@ReportDef(
        name = "Order numbers",
        code = OrderNumbersLlmReport.CODE,
        description = "An LLM data set in a report defined in code"
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
                llm = @LlmDataSetParameters(prompt = "Order numbers of this month", maxResults = 50)
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/outside_reports/OrderNumbers.csv"
)
public class OrderNumbersLlmReport {

    public static final String CODE = "order-numbers-llm";
}
