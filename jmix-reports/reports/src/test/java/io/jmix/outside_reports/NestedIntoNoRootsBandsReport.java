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
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = "Nested into no roots bands",
        code = "no-roots-bands-code",
        uuid = "33eb147b-6580-404e-836c-51483509c41d"
)
@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "title",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "title",
                type = DataSetType.DELEGATE
        )
)
@BandDef(
        name = "description",
        parent = "title", // nested in a band that is not the root
        dataSets = @DataSetDef(
                name = "description",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reports/SomeTemplate.html"
)
public class NestedIntoNoRootsBandsReport {
    @DataSetDelegate(name = "title")
    public ReportDataLoader titleDataLoader() {
        return (reportQuery, parentBand, parameters) -> {
            return List.of(
                    new HashMap<>(Map.of(
                            "caption",
                            "Hello reports",
                            "date",
                            parameters.get("afterDate")
                    ))
            );
        };
    }

    @DataSetDelegate(name = "description")
    public ReportDataLoader descriptionDataLoader() {
        return (reportQuery, parentBand, parameters) -> {
            return List.of(
                    new HashMap<>(Map.of(
                            "caption",
                            "Hello reports",
                            "date",
                            parameters.get("afterDate")
                    ))
            );
        };
    }
}
