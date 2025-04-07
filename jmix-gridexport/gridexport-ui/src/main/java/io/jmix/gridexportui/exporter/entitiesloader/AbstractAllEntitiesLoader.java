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

package io.jmix.gridexportui.exporter.entitiesloader;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Nullable;

/**
 * Base class for the all entities loader which is used to export to other data formats such as excel or json
 */
public abstract class AbstractAllEntitiesLoader implements AllEntitiesLoader {

    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected PlatformTransactionManager platformTransactionManager;
    protected GridExportProperties gridExportProperties;

    public AbstractAllEntitiesLoader(MetadataTools metadataTools,
                                     DataManager dataManager,
                                     PlatformTransactionManager platformTransactionManager,
                                     GridExportProperties gridExportProperties) {

        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.platformTransactionManager = platformTransactionManager;
        this.gridExportProperties = gridExportProperties;
    }

    @SuppressWarnings("rawtypes")
    protected abstract LoadContext generateLoadContext(DataUnit dataUnit, @Nullable Sort sort);

    protected CollectionLoader<?> getDataLoader(DataUnit dataUnit) {
        if (!(dataUnit instanceof ContainerDataUnit<?>)) {
            throw new IllegalArgumentException(
                    String.format("Cannot get data loader. %s must be an instance of %s.",
                            DataUnit.class.getSimpleName(), dataUnit.getClass().getSimpleName())
            );
        }

        CollectionContainer<?> collectionContainer = ((ContainerDataUnit<?>) dataUnit).getContainer();
        if (!(collectionContainer instanceof HasLoader)) {
            throw new IllegalArgumentException(
                    String.format("Cannot data loader. %s must be an instance of %s.",
                            CollectionContainer.class.getSimpleName(), HasLoader.class.getSimpleName())
            );
        }

        DataLoader loader = ((HasLoader) collectionContainer).getLoader();
        if (!(loader instanceof CollectionLoader)) {
            throw new RuntimeException(
                    String.format("Cannot export all rows. %s must be an instance of %s.",
                            DataLoader.class.getSimpleName(), CollectionLoader.class.getSimpleName())
            );
        }

        return (CollectionLoader<?>) loader;
    }
}
