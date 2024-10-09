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

import io.jmix.flowui.download.Downloader;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.kit.component.model.Renderer;

public interface PivotTableExporter {
    /**
     * Exports PivotTable to Xls file.
     */
    void exportTableToXls();

    /**
     * Exports PivotTable to Xls file.
     *
     * @param downloader downloader to save file
     */
    void exportTableToXls(Downloader downloader);

    /**
     * Sets the file name.
     *
     * @param fileName file name
     */
    void setFileName(String fileName);

    /**
     * @return file name
     */
    String getFileName();

    /**
     * @return serialized object from JSON which represents PivotTable with aggregated data
     */
    PivotData getPivotData();

    /**
     * Checks whether renderer is supported by the exporter.
     *
     * @param renderer renderer to check
     * @return {@code true} if renderer is supported by the exporter
     */
    boolean isRendererSupported(Renderer renderer);

    /**
     * @return export format {@code XLS} or {@code XLSX}
     */
    PivotTableExcelExporter.ExportFormat getExportFormat();

    /**
     * Sets export format {@code XLS} or {@code XLSX}. The default value is {@code XLSX}.
     *
     * @param exportFormat format that should have exported file
     */
    void setExportFormat(PivotTableExcelExporter.ExportFormat exportFormat);
}
