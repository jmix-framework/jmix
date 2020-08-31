/*
 * Copyright 2020 Haulmont.
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

package io.jmix.uiexport.exporter;

import io.jmix.core.JmixEntity;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.Downloader;
import io.jmix.uiexport.action.ExportAction;

/**
 * Table exporter interface.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
public interface TableExporter {

    /**
     * download <code>table</code> content via <code>downloader</code>
     */
    void exportTable(Downloader downloader, Table<JmixEntity> table, ExportMode exportMode);

    /**
     * download <code>dataGrid</code> content via <code>downloader</code>
     */
    void exportDataGrid(Downloader downloader, DataGrid<JmixEntity> dataGrid, ExportMode exportMode);

    /**
     * returns exporter caption
     */
    String getCaption();
}
