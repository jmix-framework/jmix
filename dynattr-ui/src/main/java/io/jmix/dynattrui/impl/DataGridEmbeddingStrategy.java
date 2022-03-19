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

package io.jmix.dynattrui.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.data.datagrid.ContainerDataGridItems;
import io.jmix.ui.component.data.meta.EntityDataUnit;

import java.util.List;

@org.springframework.stereotype.Component("dynat_DataGridEmbeddingStrategy")
public class DataGridEmbeddingStrategy extends ListEmbeddingStrategy {

    @Override
    public boolean supportComponent(Component component) {
        return component instanceof DataGrid && ((DataGrid<?>) component).getItems() instanceof ContainerDataGridItems;
    }

    @Override
    protected void embed(Component component, Frame frame, List<AttributeDefinition> attributes) {
        DataGrid dataGrid = (DataGrid) component;
        for (AttributeDefinition attribute : attributes) {
            addAttributeColumn(dataGrid, attribute);
        }
    }

    @Override
    protected MetaClass getEntityMetaClass(Component component) {
        DataGrid dataGrid = (DataGrid) component;
        if (dataGrid.getItems() instanceof EntityDataUnit) {
            return ((EntityDataUnit) dataGrid.getItems()).getEntityMetaClass();
        }
        return null;
    }

    @Override
    protected void setLoadDynamicAttributes(Component component) {
        DataGrid dataGrid = (DataGrid) component;
        if (dataGrid.getItems() instanceof ContainerDataGridItems) {
            setLoadDynamicAttributes(((ContainerDataGridItems<?>) dataGrid.getItems()).getContainer());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void addAttributeColumn(DataGrid dataGrid, AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        MetaClass metaClass = getEntityMetaClass(dataGrid);

        DataGrid.Column column = dataGrid.addColumn(metaProperty.getName(), new MetaPropertyPath(metaClass, metaProperty));

        column.setDescriptionProvider(item -> getColumnDescription(attribute));

        column.setCaption(getColumnCaption(attribute));

        column.setRenderer(null, getColumnFormatter(attribute));

        column.setSortable(false);

        setColumnWidth(column, attribute);
    }

    protected void setColumnWidth(DataGrid.Column column, AttributeDefinition attribute) {
        if (attribute.getConfiguration().getColumnWidth() != null) {
            column.setWidth(attribute.getConfiguration().getColumnWidth());
        }
    }
}
