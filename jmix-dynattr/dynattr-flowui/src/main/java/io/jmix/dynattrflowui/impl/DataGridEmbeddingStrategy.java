/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattrflowui.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.view.View;

import java.util.List;

@org.springframework.stereotype.Component("dynat_DataGridEmbeddingStrategy")
public class DataGridEmbeddingStrategy extends ListEmbeddingStrategy {

    protected DataGridEmbeddingStrategy(Metadata metadata,
                                        MetadataTools metadataTools,
                                        DynAttrMetadata dynAttrMetadata,
                                        AccessManager accessManager,
                                        MsgBundleTools msgBundleTools,
                                        CurrentAuthentication currentAuthentication,
                                        DataManager dataManager,
                                        AttributeRecalculationManager attributeRecalculationManager) {
        super(metadata, metadataTools, dynAttrMetadata, accessManager, msgBundleTools, currentAuthentication,
                dataManager, attributeRecalculationManager);
    }

    @Override
    public boolean supportComponent(Component component) {
        return component instanceof DataGrid && ((DataGrid<?>) component).getItems() instanceof ContainerDataGridItems;
    }

    @Override
    protected void embed(Component component, View<?> owner, List<AttributeDefinition> attributes) {
        DataGrid<?> dataGrid = (DataGrid<?>) component;
        for (AttributeDefinition attribute : attributes) {
            addAttributeColumn(dataGrid, attribute);
        }
    }

    @Override
    protected MetaClass getEntityMetaClass(Component component) {
        DataGrid<?> dataGrid = (DataGrid<?>) component;
        if (dataGrid.getItems() instanceof EntityDataUnit) {
            return ((EntityDataUnit) dataGrid.getItems()).getEntityMetaClass();
        }
        return null;
    }

    @Override
    protected void setLoadDynamicAttributes(Component component) {
        DataGrid<?> dataGrid = (DataGrid<?>) component;
        if (dataGrid.getItems() instanceof ContainerDataGridItems) {
            setLoadDynamicAttributes(((ContainerDataGridItems<?>) dataGrid.getItems()).getContainer());
        }
    }

    protected void addAttributeColumn(DataGrid<?> dataGrid, AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        MetaClass metaClass = getEntityMetaClass(dataGrid);

        DataGrid.Column<?> column = dataGrid.addColumn(metaProperty.getName(), new MetaPropertyPath(metaClass, metaProperty));

        column.setTooltipGenerator(item -> getColumnDescription(attribute));

        column.setHeader(getColumnCaption(attribute));

        column.setRenderer(getColumnRenderer(attribute));

        column.setSortable(false);

        setColumnWidth(column, attribute);
    }

    protected void setColumnWidth(DataGrid.Column<?> column, AttributeDefinition attribute) {
        if (attribute.getConfiguration().getColumnWidth() != null) {
            column.setWidth(attribute.getConfiguration().getColumnWidth() + "px");
        }
    }
}
