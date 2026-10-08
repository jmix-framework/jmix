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

package test_support.report;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.List;
import java.util.Map;

// This report is intentionally NOT available through the REST API (restAccessible defaults to false),
// so it must not be runnable via the REST endpoints even though it exists and has a valid code.
@ReportDef(
        code = "dt-non-rest-1",
        name = "Non REST Accessible Test Report"
)
@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "/test_support/dt-test-1.csv",
        outputType = ReportOutputType.CSV,
        outputNamePattern = "dt-non-rest-1.csv"
)

@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "Data",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "data",
                type = DataSetType.DELEGATE
        )
)
public class NonRestAccessibleTestReport {

    @DataSetDelegate(name = "data")
        public ReportDataLoader dataLoader() {
           return (reportQuery, parentBand, params) ->
                List.of(Map.of("field1", "value1", "field2", "value2"));
    }
}
