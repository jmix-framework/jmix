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

package io.jmix.gridexportflowui.exporter;

import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;

public abstract class AbstractAllRecordsExporter {

    protected static String LAST_LOADED_PK_CONDITION_PARAMETER_NAME = "lastLoadedPkValue";

    protected MetadataTools metadataTools;

    public AbstractAllRecordsExporter(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    /**
     * Generates the load context using the given {@code DataUnit}.
     *
     * @param dataUnit data unit linked with the data
     */
    @SuppressWarnings("rawtypes")
    public LoadContext generateLoadContext(DataUnit dataUnit) {
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
        query.setSort(Sort.by(primaryKeyName));

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
}

