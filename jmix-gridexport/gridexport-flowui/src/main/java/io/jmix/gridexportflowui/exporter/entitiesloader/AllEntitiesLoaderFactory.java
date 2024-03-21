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

package io.jmix.gridexportflowui.exporter.entitiesloader;

import io.jmix.gridexportflowui.GridExportProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Gives access to the all entities loader with current pagination strategy
 * set in application.properties {@link GridExportProperties#getExportAllPaginationStrategy()}
 */
@Component
public class AllEntitiesLoaderFactory {

    protected GridExportProperties gridExportProperties;
    protected List<AllEntitiesLoader> allEntitiesLoaders;

    public AllEntitiesLoaderFactory(GridExportProperties gridExportProperties,
                                    List<AllEntitiesLoader> allEntitiesLoaders) {
        this.gridExportProperties = gridExportProperties;
        this.allEntitiesLoaders = allEntitiesLoaders;
    }

    /**
     * Return appropriate {@link AllEntitiesLoader} component with accordance to
     * {@link GridExportProperties#getExportAllPaginationStrategy()}
     */
    public AllEntitiesLoader getEntitiesLoader() {
        String paginationStrategy = gridExportProperties.getExportAllPaginationStrategy();
        Optional<? extends AllEntitiesLoader> entityLoader = allEntitiesLoaders.stream()
                .filter(provider -> paginationStrategy.equals(provider.getPaginationStrategy()))
                .findFirst();
        if (entityLoader.isPresent()) {
            return entityLoader.get();
        } else {
            throw new IllegalStateException(String.format("Unknown export pagination strategy %s", paginationStrategy));
        }
    }
}
