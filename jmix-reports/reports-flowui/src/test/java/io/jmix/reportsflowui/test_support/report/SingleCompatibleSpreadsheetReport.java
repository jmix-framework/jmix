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
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.annotation.TemplateDelegate;
import io.jmix.reports.entity.CustomTemplateDefinedBy;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = "Single compatible spreadsheet report",
        code = SingleCompatibleSpreadsheetReport.CODE
)
@BandDef(
        name = "Root",
        root = true,
        dataSets = @DataSetDef(
                name = "Root",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = SingleCompatibleSpreadsheetReport.DOWNLOAD_TEMPLATE,
        outputType = ReportOutputType.CSV,
        isDefault = true,
        custom = @CustomTemplateParameters(
                enabled = true,
                definedBy = CustomTemplateDefinedBy.DELEGATE
        )
)
@TemplateDef(
        code = SingleCompatibleSpreadsheetReport.SPREADSHEET_TEMPLATE,
        outputType = ReportOutputType.XLSX,
        custom = @CustomTemplateParameters(
                enabled = true,
                definedBy = CustomTemplateDefinedBy.DELEGATE
        )
)
public class SingleCompatibleSpreadsheetReport {

    public static final String CODE = "SINGLE_COMPATIBLE_SPREADSHEET";
    public static final String DOWNLOAD_TEMPLATE = "download";
    public static final String SPREADSHEET_TEMPLATE = "spreadsheet";

    @DataSetDelegate(name = "Root")
    public ReportDataLoader rootDataLoader() {
        return (reportQuery, parentBand, parameters) -> List.of(new HashMap<>(Map.of(
                "value", "Spreadsheet"
        )));
    }

    @TemplateDelegate(code = DOWNLOAD_TEMPLATE)
    public CustomReport downloadTemplate() {
        return (report, rootBand, params) -> "value\nSpreadsheet".getBytes(StandardCharsets.UTF_8);
    }

    @TemplateDelegate(code = SPREADSHEET_TEMPLATE)
    public CustomReport spreadsheetTemplate() {
        return (report, rootBand, params) -> createWorkbook();
    }

    protected byte[] createWorkbook() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.createSheet("Data")
                    .createRow(0)
                    .createCell(0)
                    .setCellValue("Spreadsheet");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
