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
import io.jmix.pivottableflowui.component.PivotTableUtils;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Prepare the data {@link PivotData} requested from {@link PivotTable}, then export to an XLS file.
 */
@Component("pvttbl_PivotTableExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PivotTableExporterImpl implements PivotTableExporter {

    public static final Set<Renderer> supportedRenderers = Collections.unmodifiableSet(Sets.newHashSet(
            Renderer.TABLE, Renderer.TABLE_BAR_CHART, Renderer.HEATMAP, Renderer.ROW_HEATMAP, Renderer.COL_HEATMAP));

    protected PivotTableExcelExporter excelExporter;
    protected PivotTable<?> pivotTable;
    protected String fileName;

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

        PivotTableUtils.requestPivotTableData(pivotTable,
                pivotData -> excelExporter.exportPivotTable(pivotData, fileName));
    }

    @Override
    public void exportTableToXls(Downloader downloader) {
        checkSupportedRenderer();

        PivotTableUtils.requestPivotTableData(pivotTable,
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
