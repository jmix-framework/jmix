/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui;

import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.ColumnExportFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Export actions configuration interface
 */
@ConfigurationProperties(prefix = "jmix.gridexport")
public class GridExportProperties {

    /**
     * Loading batch size used when exporting all records.
     */
    int exportAllBatchSize;

    /**
     * Pagination strategy for all entities export.
     */
    String exportAllPaginationStrategy;

    /**
     * A list of mods that used by default in the {@link ExportAction}
     */
    List<String> defaultExportModes;

    /**
     * A {@link ColumnExportFilter} that used by default in the {@link ExportAction}
     */
    String defaultColumnExportFilter;

    /**
     * Excel exporting configuration.
     */
    ExcelExporterProperties excel;

    public GridExportProperties(@DefaultValue("1000") int exportAllBatchSize,
                                @DefaultValue("keyset") String exportAllPaginationStrategy,
                                @DefaultValue({"ALL_ROWS", "CURRENT_PAGE", "SELECTED_ROWS"})
                                List<String> defaultExportModes,
                                @DefaultValue("VISIBLE_COLUMNS") String defaultColumnExportFilter,
                                @DefaultValue ExcelExporterProperties excel) {
        this.exportAllBatchSize = exportAllBatchSize;
        this.exportAllPaginationStrategy = exportAllPaginationStrategy;
        this.defaultExportModes = defaultExportModes;
        this.defaultColumnExportFilter = defaultColumnExportFilter;
        this.excel = excel;
    }

    /**
     * @see #exportAllBatchSize
     */
    public int getExportAllBatchSize() {
        return exportAllBatchSize;
    }

    /**
     * @see #exportAllPaginationStrategy
     */
    public String getExportAllPaginationStrategy() {
        return exportAllPaginationStrategy;
    }

    /**
     * @see #defaultExportModes
     */
    public List<String> getDefaultExportModes() {
        return defaultExportModes;
    }

    /**
     * @see #defaultColumnExportFilter
     */
    public String getDefaultColumnExportFilter() {
        return defaultColumnExportFilter;
    }

    public ExcelExporterProperties getExcel() {
        return excel;
    }

    public static class ExcelExporterProperties {

        /**
         * Whether to use POI SXSSF API for building XLSX files.
         */
        boolean useSxssf;

        public ExcelExporterProperties(@DefaultValue("true") boolean useSxssf) {
            this.useSxssf = useSxssf;
        }

        /**
         * @see #useSxssf
         */
        public boolean isUseSxssf() {
            return useSxssf;
        }
    }
}
