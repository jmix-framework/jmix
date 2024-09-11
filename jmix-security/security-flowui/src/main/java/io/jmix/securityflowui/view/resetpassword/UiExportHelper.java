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

package io.jmix.securityflowui.view.resetpassword;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.Downloader;
import io.jmix.gridexportflowui.exporter.ExportMode;
import io.jmix.gridexportflowui.exporter.excel.ExcelExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Helper that provides grid exporter.
 */
@Internal
@Component("sec_ExportUiHelper")
@ConditionalOnBean(ExcelExporter.class)
public class UiExportHelper {

    protected Downloader downloader;
    protected ExcelExporter excelExporter;

    public UiExportHelper(Downloader downloader, ExcelExporter excelExporter) {
        this.downloader = downloader;
        this.excelExporter = excelExporter;
    }

    public <T> void exportDataGrid(DataGrid<T> dataGrid) {
        //noinspection unchecked,rawtypes
        excelExporter.exportDataGrid(downloader, ((Grid) dataGrid), ExportMode.CURRENT_PAGE);
    }
}