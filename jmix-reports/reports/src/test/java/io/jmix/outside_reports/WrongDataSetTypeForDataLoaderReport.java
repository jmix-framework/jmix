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

package io.jmix.outside_reports;

import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.JsonInputProvider;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.List;

@ReportDef(
        name = "Some name",
        code = "some-code"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                name = "title",
                type = DataSetType.SQL, // <!-- not DELEGATE
                query = "select ID, NAME from USER_"
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reports/SomeTemplate.html"
)
public class WrongDataSetTypeForDataLoaderReport {

    @DataSetDelegate(name = "title")
    public ReportDataLoader titleDataLoader() {
        return (reportQuery, parentBand, params) -> {
            return List.of();
        };
    }
}
