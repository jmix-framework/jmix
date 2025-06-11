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

package io.jmix.reports.execution;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = ReportForHistory.NAME,
        code = ReportForHistory.CODE
)
@InputParameterDef(
        alias = ReportForHistory.PARAM_INPUT,
        name = "Input",
        type = ParameterType.NUMERIC,
        required = true,
        defaultValue = "10"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "Data",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                name = "Data",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/reports/execution/ReportForHistory.csv"
)
public class ReportForHistory {
    public static final String CODE = "REPORT_FOR_HISTORY";
    public static final String PARAM_INPUT = "input";
    public static final String NAME = "Report for history";

    @DataSetDelegate(name = "Data")
    public ReportDataLoader dataDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Double input = (Double) params.get(PARAM_INPUT);
            return List.of(
                    new HashMap<>(Map.of(
                            "output",
                            100 / input.intValue()       // throws error if input == 0
                    ))
            );
        };
    }
}