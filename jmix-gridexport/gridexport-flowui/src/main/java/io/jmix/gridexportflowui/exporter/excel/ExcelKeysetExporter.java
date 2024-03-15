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

package io.jmix.gridexportflowui.exporter.excel;

import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.AbstractAllRecordsExporter;
import org.springframework.transaction.PlatformTransactionManager;

public class ExcelKeysetExporter extends AbstractAllRecordsExporter {
    public ExcelKeysetExporter(MetadataTools metadataTools, DataManager dataManager,
                               PlatformTransactionManager platformTransactionManager,
                               GridExportProperties gridExportProperties) {
        super(metadataTools, dataManager, platformTransactionManager, gridExportProperties);
    }
}
