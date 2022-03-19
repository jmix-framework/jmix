/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.component;


import io.jmix.pivottable.component.impl.PivotExcelExporter;
import io.jmix.pivottable.component.impl.PivotExcelExporter.ExportFormat;
import io.jmix.pivottable.model.Renderer;
import io.jmix.ui.download.Downloader;
import io.jmix.pivottable.model.extension.PivotData;

import javax.annotation.Nullable;

public interface PivotTableExtension {

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
     * @return JSON string which represents PivotTable with aggregated data
     */
    String getPivotDataJSON();

    /**
     * @return serialized object from JSON which represents PivotTable with aggregated data
     */
    PivotData getPivotData();

    /**
     * @return dateTime format or null
     */
    @Nullable
    String getDateTimeParseFormat();

    /**
     * Sets dateTime format that will be used to finding dateTime value and exporting it to excel with dateTime type.
     *
     * @param dateTimeParseFormat dateTime format (e.g. dd/MM/yyyy HH:mm)
     */
    void setDateTimeParseFormat(String dateTimeParseFormat);

    /**
     * @return date format or null
     */
    @Nullable
    String getDateParseFormat();

    /**
     * Sets date format that will be used to finding dateTime value and exporting it to excel with date type. If there
     * is no format set, date properties will be recognized as text value.
     *
     * @param dateParseFormat date format (e.g. dd/MM/yyyy)
     */
    void setDateParseFormat(String dateParseFormat);

    /**
     * @return time format or null
     */
    @Nullable
    String getTimeParseFormat();

    /**
     * Sets date format that will be used to finding dateTime value and exporting it to excel with date type. If there
     * is no format set, time properties will be recognized as text value.
     *
     * @param timeParseFormat time format (e.g. HH:mm)
     */
    void setTimeParseFormat(String timeParseFormat);

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
    ExportFormat getExportFormat();

    /**
     * Sets export format {@code XLS} or {@code XLSX}. The default value is {@code XLSX}.
     *
     * @param exportFormat format that should have exported file
     */
    void setExportFormat(ExportFormat exportFormat);
}
