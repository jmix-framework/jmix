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

package io.jmix.gridexportui.exporter.entitiesloader;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.gridexportui.GridExportProperties;
import io.jmix.gridexportui.exporter.EntityExportContext;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This loader implements the keyset pagination strategy. Entities retrieval is based on sorting
 * by primary key. The next page starts after the last entity on the previous page.
 */
@Component
public class KeysetAllEntitiesLoader extends AbstractAllEntitiesLoader {

    public static final String PAGINATION_STRATEGY = "keyset";

    protected static String LAST_LOADED_PK_CONDITION_PARAMETER_NAME = "lastLoadedPkValue";

    public KeysetAllEntitiesLoader(MetadataTools metadataTools,
                                   DataManager dataManager,
                                   PlatformTransactionManager platformTransactionManager,
                                   GridExportProperties gridExportProperties) {
        super(metadataTools, dataManager, platformTransactionManager, gridExportProperties);
    }

    @Override
    public String getPaginationStrategy() {
        return PAGINATION_STRATEGY;
    }

    /**
     * Generates the load context using the given {@code DataUnit}.
     *
     * @param dataUnit data unit linked with the data
     * @param sort An optional sorting specification for the data.
     *             If {@code null} sorting will be applied by the primary key.
     */
    @SuppressWarnings("rawtypes")
    protected LoadContext generateLoadContext(DataUnit dataUnit, @Nullable Sort sort) {
        if (!(dataUnit instanceof ContainerDataUnit)) {
            throw new RuntimeException("Cannot export all rows. DataUnit must be an instance of ContainerDataUnit.");
        }
        CollectionContainer collectionContainer = ((ContainerDataUnit) dataUnit).getContainer();
        if (!(collectionContainer instanceof HasLoader)) {
            throw new RuntimeException("Cannot export all rows. Collection container must be an instance of HasLoader.");
        }

        DataLoader dataLoader = ((HasLoader) collectionContainer).getLoader();
        if (!(dataLoader instanceof CollectionLoader)) {
            throw new RuntimeException("Cannot export all rows. Data loader must be an instance of CollectionLoader.");
        }

        LoadContext loadContext = ((CollectionLoader) dataLoader).createLoadContext();
        LoadContext.Query query = loadContext.getQuery();
        if (query == null) {
            throw new RuntimeException("Cannot export all rows. Query in LoadContext is null.");
        }

        MetaClass entityMetaClass = loadContext.getEntityMetaClass();
        if (metadataTools.hasCompositePrimaryKey(entityMetaClass)) {
            throw new RuntimeException("Cannot export all rows. Exporting of entities with composite key is not supported.");
        }

        //sort data by primary key. Next batch is loaded using the condition that compares the last primary key value
        //from the previous batch.
        String primaryKeyName = metadataTools.getPrimaryKeyName(entityMetaClass);
        if (primaryKeyName == null) {
            throw new RuntimeException("Cannot find a primary key for a meta class " + entityMetaClass.getName());
        }
        sort = sort != null ? sort : Sort.by(primaryKeyName);

        query.setSort(sort);

        Condition condition = loadContext.getQuery().getCondition();

        LogicalCondition wrappingCondition = new LogicalCondition(LogicalCondition.Type.AND);
        if (condition != null) {
            //in case there is no filter on the screen a condition in the query may be null
            wrappingCondition.add(condition);
        }

        PropertyCondition lastPkCondition = PropertyCondition.createWithParameterName(primaryKeyName,
                PropertyCondition.Operation.GREATER, LAST_LOADED_PK_CONDITION_PARAMETER_NAME);
        wrappingCondition.add(lastPkCondition);
        loadContext.getQuery().setCondition(wrappingCondition);

        return loadContext;
    }

    /**
     * Method loads all entity instances associated with the given {@code dataUnit} and pass
     * each loaded entity instance to the {@code exportedEntityVisitor}. Creation of the output file row is the
     * responsibility of that visitor. Data is loaded in batches, the batch size is configured by the
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param dataUnit        data unit linked with the data
     * @param exportedEntityVisitor function that is responsible for export
     * @param sort An optional sorting specification for the data.
     *             If {@code null} sorting will be applied by the primary key.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor, @Nullable Sort sort) {
        Preconditions.checkNotNullArgument(exportedEntityVisitor,
                "Cannot export all rows. ExportedEntityVisitor can't be null");

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            long count = dataManager.getCount(generateLoadContext(dataUnit, sort));
            int loadBatchSize = gridExportProperties.getExportAllBatchSize();

            int rowNumber = 0;
            boolean initialLoading = true;
            boolean proceedToExport = true;
            Object lastLoadedPkValue = null;

            for (int firstResult = 0; firstResult < count && proceedToExport; firstResult += loadBatchSize) {
                LoadContext loadContext = generateLoadContext(dataUnit, sort);
                LoadContext.Query query = loadContext.getQuery();

                if (initialLoading) {
                    initialLoading = false;
                } else {
                    query.setParameter(LAST_LOADED_PK_CONDITION_PARAMETER_NAME, lastLoadedPkValue);
                }
                query.setMaxResults(loadBatchSize);

                List entities = dataManager.loadList(loadContext);
                for (Object entity : entities) {
                    EntityExportContext entityExportContext = new EntityExportContext(entity, ++rowNumber);
                    proceedToExport = exportedEntityVisitor.visitEntity(entityExportContext);
                    if (!proceedToExport) {
                        break;
                    }
                }

                Object lastEntity = entities.get(entities.size() - 1);
                lastLoadedPkValue = Id.of(lastEntity).getValue();
            }
        });
    }
}
