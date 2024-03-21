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

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

/**
 * This loader implements the keyset pagination strategy. Entities retrieval is based on sorting
 * by primary key. The next page starts after the last entity on the previous page.
 */
@Component
public class KeysetAllEntitiesLoader extends AbstractAllEntitiesLoader {

    public static final String PAGINATION_STRATEGY = "keyset";

    protected static String LAST_LOADED_PK_CONDITION_PARAMETER_NAME = "lastLoadedPkValue";

    public KeysetAllEntitiesLoader(MetadataTools metadataTools, DataManager dataManager,
                                   PlatformTransactionManager platformTransactionManager,
                                   GridExportProperties gridExportProperties) {
        super(metadataTools, dataManager, platformTransactionManager, gridExportProperties);
    }

    @Override
    public String getPaginationStrategy() {
        return PAGINATION_STRATEGY;
    }

    protected LoadContext generateLoadContext(CollectionLoader loader) {
        LoadContext loadContext = loader.createLoadContext();
        LoadContext.Query query = loadContext.getQuery();
        if (query == null) {
            throw new IllegalArgumentException("Cannot export all rows. Query in LoadContext is null.");
        }

        MetaClass entityMetaClass = loadContext.getEntityMetaClass();
        if (metadataTools.hasCompositePrimaryKey(entityMetaClass)) {
            throw new IllegalArgumentException(
                    "Cannot export all rows. Exporting of entities with composite key is not supported.");
        }

        //sort data by primary key. Next batch is loaded using the condition that compares the last primary key value
        //from the previous batch. In some databases (for example, PostgreSQL) it's faster than paging using firstResult
        String primaryKeyName = metadataTools.getPrimaryKeyName(entityMetaClass);
        if (primaryKeyName == null) {
            throw new IllegalStateException("Cannot find a primary key for a meta class " + entityMetaClass.getName());
        }
        query.setSort(Sort.by(primaryKeyName));

        Condition condition = loadContext.getQuery().getCondition();

        LogicalCondition wrappingCondition = new LogicalCondition(LogicalCondition.Type.AND);
        //noinspection ConstantValue
        if (condition != null) {
            //in case there is no filter on the screen a condition in the query may be null
            wrappingCondition.add(condition);
        }

        PropertyCondition lastPkCondition = PropertyCondition.createWithParameterName(primaryKeyName,
                        PropertyCondition.Operation.GREATER, LAST_LOADED_PK_CONDITION_PARAMETER_NAME)
                .skipNullOrEmpty();
        wrappingCondition.add(lastPkCondition);
        query.setCondition(wrappingCondition);
        query.setFirstResult(0);

        return loadContext;
    }

    /**
     * Sort entities by the primary key, load the first batch and save the last entity primary key value.
     * Load the next batch with primary keys after the last entity primary key.
     * @param exportedEntityVisitor {@link ExportedEntityVisitor#visitEntity(EntityExportContext)}
     * @param loadBatchSize {@link GridExportProperties#getExportAllBatchSize()} number of entities loaded in one query
     */
    protected void loadEntities(CollectionLoader<?> collectionLoader,
                                ExportedEntityVisitor exportedEntityVisitor,
                                int loadBatchSize) {
        int rowNumber = 0;
        boolean initialLoading = true;
        Object lastLoadedPkValue = null;
        boolean proceedToExport = true;
        boolean lastBatchLoaded = false;

        while (!lastBatchLoaded && proceedToExport) {
            LoadContext<?> loadContext = generateLoadContext(collectionLoader);
            //query is not null - checked when generated load context
            LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());

            if (initialLoading) {
                initialLoading = false;
            } else {
                query.setParameter(LAST_LOADED_PK_CONDITION_PARAMETER_NAME, lastLoadedPkValue);
            }
            query.setMaxResults(loadBatchSize);

            List<?> entities = dataManager.loadList(loadContext);
            for (Object entity : entities) {
                EntityExportContext entityExportContext = new EntityExportContext(entity, ++rowNumber);
                proceedToExport = exportedEntityVisitor.visitEntity(entityExportContext);
                if (!proceedToExport) {
                    break;
                }
            }

            int loadedEntitiesAmount = entities.size();
            if (loadedEntitiesAmount > 0) {
                Object lastEntity = entities.get(loadedEntitiesAmount - 1);
                lastLoadedPkValue = Id.of(lastEntity).getValue();
            }
            lastBatchLoaded = loadedEntitiesAmount == 0 || loadedEntitiesAmount < loadBatchSize;
        }
    }
}
