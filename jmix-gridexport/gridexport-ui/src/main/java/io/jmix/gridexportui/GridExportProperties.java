/*
 * Copyright 2022 Haulmont.
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

package io.jmix.gridexportui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Export actions configuration interface
 */
@ConfigurationProperties(prefix = "jmix.gridexport")
@ConstructorBinding
public class GridExportProperties {

    /**
     * Loading batch size used when exporting all records.
     */
    int exportAllBatchSize;

    /**
     * Excel exporting configuration.
     */
    ExcelExporterProperties excel;

    /**
     * @see #exportAllBatchSize
     */
    public int getExportAllBatchSize() {
        return exportAllBatchSize;
    }


    public GridExportProperties(@DefaultValue("1000") int exportAllBatchSize,
                                @DefaultValue ExcelExporterProperties excel) {
        this.exportAllBatchSize = exportAllBatchSize;
        this.excel = excel;
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
