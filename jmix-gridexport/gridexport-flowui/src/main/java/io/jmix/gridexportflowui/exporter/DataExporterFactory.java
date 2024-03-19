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

package io.jmix.gridexportflowui.exporter;

import io.jmix.gridexportflowui.GridExportProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataExporterFactory {

    protected GridExportProperties gridExportProperties;
    protected List<DataExporter> dataExporters;

    public DataExporterFactory(GridExportProperties gridExportProperties,
                               List<DataExporter> dataExporters) {
        this.gridExportProperties = gridExportProperties;
        this.dataExporters = dataExporters;
    }

    public DataExporter getDataExporter() {
        String paginationType = gridExportProperties.getPaginationType();
        Optional<? extends DataExporter> dataExporter = dataExporters.stream()
                .filter(provider -> paginationType.equals(provider.getPaginationType()))
                .findFirst();
        if (dataExporter.isPresent()) {
            return dataExporter.get();
        } else {
            throw new IllegalStateException(String.format("Unknown export pagination with type %s", paginationType));
        }
    }
}
