/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.export;

import com.google.common.collect.Sets;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.download.Downloader;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Set;

public class PivotTableExporterImpl implements PivotTableExporter {

    public static final Set<Renderer> supportedRenderers = Collections.unmodifiableSet(Sets.newHashSet(
            Renderer.TABLE, Renderer.TABLE_BAR_CHART, Renderer.HEATMAP, Renderer.ROW_HEATMAP, Renderer.COL_HEATMAP));

    protected PivotTableExcelExporter excelExporter;
    protected PivotTable<?> pivotTable;
    protected String fileName;

    protected String dateTimeParseFormat;
    protected String dateParseFormat;
    protected String timeParseFormat;

    public PivotTableExporterImpl(PivotTable<?> pivotTable, PivotTableExcelExporter exporter) {
        Preconditions.checkNotNullArgument(pivotTable);
        Preconditions.checkNotNullArgument(exporter);

        this.pivotTable = pivotTable;
        this.excelExporter = exporter;
        this.excelExporter.init(pivotTable);
    }

    @Override
    public void exportTableToXls() {
        checkSupportedRenderer();

        setupParseFormats();

        pivotTable.requestPivotData(dateTimeParseFormat, dateParseFormat, timeParseFormat,
                pivotData -> excelExporter.exportPivotTable(pivotData, fileName));
    }

    @Override
    public void exportTableToXls(Downloader downloader) {
        checkSupportedRenderer();

        setupParseFormats();

        pivotTable.requestPivotData(dateTimeParseFormat, dateParseFormat, timeParseFormat,
                pivotData -> excelExporter.exportPivotTable(pivotData, fileName, downloader));
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public PivotData getPivotData() {
        return new PivotData();
    }

    @Override
    public String getDateTimeParseFormat() {
        return dateTimeParseFormat;
    }

    @Override
    public void setDateTimeParseFormat(String dateTimeParseFormat) {
        this.dateTimeParseFormat = dateTimeParseFormat;
    }

    @Override
    public String getDateParseFormat() {
        return dateParseFormat;
    }

    @Override
    public void setDateParseFormat(String dateParseFormat) {
        this.dateParseFormat = dateParseFormat;
    }

    @Override
    public String getTimeParseFormat() {
        return timeParseFormat;
    }

    @Override
    public void setTimeParseFormat(String timeParseFormat) {
        this.timeParseFormat = timeParseFormat;
    }

    @Override
    public boolean isRendererSupported(Renderer renderer) {
        return supportedRenderers.contains(renderer);
    }

    @Override
    public PivotTableExcelExporter.ExportFormat getExportFormat() {
        return excelExporter.getExportFormat();
    }

    @Override
    public void setExportFormat(PivotTableExcelExporter.ExportFormat exportFormat) {
        excelExporter.setExportFormat(exportFormat);
    }

    protected void setupParseFormats() {
        if (excelExporter != null) {
            excelExporter.setDateTimeParseFormat(dateTimeParseFormat);
            excelExporter.setDateParseFormat(dateParseFormat);
            excelExporter.setTimeParseFormat(timeParseFormat);
        }
    }

    protected void checkSupportedRenderer() {
        boolean showUI = pivotTable.isEnabled() && (pivotTable.isShowUI() == null || pivotTable.isShowUI());
        if (showUI) {
            checkRenderer(pivotTable.getRenderers() != null ? pivotTable.getRenderers().getSelectedRenderer() : null);
        } else {
            checkRenderer(pivotTable.getRenderer());
        }
    }

    protected void checkRenderer(@Nullable Renderer renderer) {
        Preconditions.checkNotNullArgument(renderer, "PivotTable component doesn't have a renderer");
        if (!supportedRenderers.contains(renderer)) {
            throw new IllegalStateException(
                    String.format("'%s' renderer is not supported for data export", renderer.name()));
        }
    }
}
