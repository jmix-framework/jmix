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

import java.util.Collections;
import java.util.Set;

public class PivotTableExporterImpl implements PivotTableExporter {

    public static final Set<Renderer> supportedRenderers = Collections.unmodifiableSet(Sets.newHashSet(
            Renderer.TABLE, Renderer.TABLE_BAR_CHART, Renderer.HEATMAP, Renderer.ROW_HEATMAP, Renderer.COL_HEATMAP));

//    protected JmixPivotTableExtension pivotTableExtension;

    protected String fileName;

    protected PivotTableExcelExporter excelExporter;

    protected PivotTable pivotTable;

    public PivotTableExporterImpl(PivotTable pivotTable, PivotTableExcelExporter exporter) {
        Preconditions.checkNotNullArgument(pivotTable);
        Preconditions.checkNotNullArgument(exporter);

        this.pivotTable = pivotTable;
        this.excelExporter = exporter;
        this.excelExporter.init(pivotTable);

//        JmixPivotTable jmixPivotTable = pivotTable.unwrap(JmixPivotTable.class);
//        this.pivotTableExtension = new JmixPivotTableExtension(jmixPivotTable);
    }

    @Override
    public void exportTableToXls() {
        checkSupportedRenderer();

        setupParseFormats();

        excelExporter.exportPivotTable(new PivotData(), fileName);
    }

    @Override
    public void exportTableToXls(Downloader downloader) {
        checkSupportedRenderer();

        setupParseFormats();

        excelExporter.exportPivotTable(null, /*pivotTableExtension.getPivotData(),*/ fileName, downloader);
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
    public String getPivotDataJSON() {
        return "";//pivotTableExtension.getPivotDataJSON();
    }

    @Override
    public PivotData getPivotData() {
        return new PivotData();
    }

    @Override
    public String getDateTimeParseFormat() {
        return "";//pivotTableExtension.getDateTimeParseFormat();
    }

    @Override
    public void setDateTimeParseFormat(String dateTimeParseFormat) {
        //pivotTableExtension.setDateTimeParseFormat(dateTimeParseFormat);
    }

    @Override
    public String getDateParseFormat() {
        return "";//pivotTableExtension.getDateParseFormat();
    }

    @Override
    public void setDateParseFormat(String dateParseFormat) {
        //pivotTableExtension.setDateParseFormat(dateParseFormat);
    }

    @Override
    public String getTimeParseFormat() {
        return "";//pivotTableExtension.getTimeParseFormat();
    }

    @Override
    public void setTimeParseFormat(String timeParseFormat) {
        //pivotTableExtension.setTimeParseFormat(timeParseFormat);
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
            excelExporter.setDateTimeParseFormat(getDateTimeParseFormat());
            excelExporter.setDateParseFormat(getDateParseFormat());
            excelExporter.setTimeParseFormat(getTimeParseFormat());
        }
    }

    protected void checkSupportedRenderer() {
        /*String json = pivotTable.getNativeJson();
        Boolean editable = PivotNativeJsonUtils.isEditable(json);

        if (!Boolean.FALSE.equals(editable)) {
            Renderer currentRenderer = pivotTableExtension.getCurrentRenderer();

            if (currentRenderer != null) {
                checkRenderer(currentRenderer);
            } else if (pivotTable.getRenderers() != null) {
                Renderer defaultRenderer = pivotTable.getRenderers().getSelectedRenderer();
                if (defaultRenderer != null) {
                    checkRenderer(defaultRenderer);
                }
            }
        }

        if ((editable == null && !pivotTable.isEditable()) || Boolean.FALSE.equals(editable)) {
            String rendererId = PivotNativeJsonUtils.getRenderer(json);

            if (rendererId != null) {
                if (Renderer.fromId(rendererId) != null) { // check render in native json
                    checkRenderer(Renderer.fromId(rendererId));
                }
            } else if (pivotTable.getRenderer() != null) { // check in server configuration
                checkRenderer(pivotTable.getRenderer());
            }
        }*/
    }

    protected void checkRenderer(Renderer renderer) {
        if (!supportedRenderers.contains(renderer)) {
            throw new IllegalStateException(String.format("'%s' renderer is not supported for data export", renderer.name()));
        }
    }
}
