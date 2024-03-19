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

package io.jmix.gridexportflowui.exporter.limitoffset;

import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.DataExporter;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.function.Predicate;

@Component
public class LimitOffsetExporter implements DataExporter {

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public LimitOffsetExporter(MetadataTools metadataTools,
                               DataManager dataManager,
                               PlatformTransactionManager platformTransactionManager,
                               GridExportProperties gridExportProperties) {
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.platformTransactionManager = platformTransactionManager;
        this.gridExportProperties = gridExportProperties;
    }

    @Override
    public String getPaginationType() {
        return LimitOffsetAllRecordsExporter.LIMIT_OFFSET_EXPORT_DATA_PROVIDER;
    }

    @Override
    public void exportAll(DataUnit dataUnit, Predicate<EntityExportContext> entityExporter) {
        LimitOffsetAllRecordsExporter dataProvider = new LimitOffsetAllRecordsExporter(metadataTools, dataManager,
                platformTransactionManager, gridExportProperties);
        dataProvider.exportAll(dataUnit, entityExporter);
    }
}
