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
import io.jmix.reports.annotation.CustomTemplateParameters;
import io.jmix.reports.annotation.DataSetDef;
import io.jmix.reports.annotation.DataSetDelegate;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TableBandDef;
import io.jmix.reports.annotation.TableColumnDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.annotation.TemplateDelegate;
import io.jmix.reports.annotation.TemplateTableDef;
import io.jmix.reports.entity.CustomTemplateDefinedBy;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = "Single compatible table report",
        code = SingleCompatibleTableReport.CODE
)
@BandDef(
        name = "Root",
        root = true,
        dataSets = @DataSetDef(
                name = "Root",
                type = DataSetType.DELEGATE
        )
)
@BandDef(
        name = "Data",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "Data",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = SingleCompatibleTableReport.DOWNLOAD_TEMPLATE,
        outputType = ReportOutputType.CSV,
        isDefault = true,
        custom = @CustomTemplateParameters(
                enabled = true,
                definedBy = CustomTemplateDefinedBy.DELEGATE
        )
)
@TemplateDef(
        code = SingleCompatibleTableReport.TABLE_TEMPLATE,
        outputType = ReportOutputType.TABLE,
        table = @TemplateTableDef(
                bands = @TableBandDef(
                        bandName = "Data",
                        columns = {
                                @TableColumnDef(key = "name", caption = "Name"),
                                @TableColumnDef(key = "count", caption = "Count")
                        }
                )
        )
)
public class SingleCompatibleTableReport {

    public static final String CODE = "SINGLE_COMPATIBLE_TABLE";
    public static final String DOWNLOAD_TEMPLATE = "download";
    public static final String TABLE_TEMPLATE = "table";

    @DataSetDelegate(name = "Root")
    public ReportDataLoader rootDataLoader() {
        return (reportQuery, parentBand, parameters) -> List.of(new HashMap<>(Map.of(
                "marker", "root"
        )));
    }

    @DataSetDelegate(name = "Data")
    public ReportDataLoader dataDataLoader() {
        return (reportQuery, parentBand, parameters) -> List.of(
                new HashMap<>(Map.of("name", "Alpha", "count", 1L)),
                new HashMap<>(Map.of("name", "Beta", "count", 2L))
        );
    }

    @TemplateDelegate(code = DOWNLOAD_TEMPLATE)
    public CustomReport downloadTemplate() {
        return (report, rootBand, params) -> "name,count\nAlpha,1\nBeta,2".getBytes(StandardCharsets.UTF_8);
    }
}
