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

import com.google.common.base.Strings;
import io.jmix.core.BeanLocator;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.datagrid.ContainerDataGridItems;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.component.data.table.ContainerTableItems;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@org.springframework.stereotype.Component(TableEmbeddingStrategy.NAME)
public class TableEmbeddingStrategy extends ListEmbeddingStrategy {
    public static final String NAME = "dynattrui_TableEmbeddingStrategy";

    @Autowired
    public TableEmbeddingStrategy(BeanLocator beanLocator) {
        super(beanLocator);
    }

    @Override
    public boolean supportComponent(Component component) {
        return component instanceof Table && ((Table<?>) component).getItems() instanceof ContainerTableItems;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void embed(Component component, List<AttributeDefinition> attributes) {
        Table table = (Table) component;
        for (AttributeDefinition attribute : attributes) {
            addAttributeColumn(table, attribute);
        }
    }

    protected void addAttributeColumn(Table table, AttributeDefinition attribute) {
        MetaProperty metaProperty = DynAttrUtils.getMetaProperty(attribute);
        MetaClass entityMetaClass = getEntityMetaClass(table);

        Table.Column column = new Table.Column(new MetaPropertyPath(entityMetaClass, metaProperty));

        column.setDescription(getColumnDescription(attribute));

        column.setCaption(getColumnCaption(attribute));

        column.setFormatter(getColumnFormatter(attribute));

        setMaxTextLength(column, attribute);

        setColumnAlignment(column, attribute);

        setColumnWidth(column, attribute);

        table.addColumn(column);
    }

    @Override
    protected MetaClass getEntityMetaClass(Component component) {
        Table table = (Table) component;
        if (table.getItems() instanceof EntityDataUnit) {
            return ((EntityDataUnit) table.getItems()).getEntityMetaClass();
        }
        return null;
    }

    @Override
    protected void setLoadDynamicAttributes(Component component) {
        Table table = (Table) component;
        if (table.getItems() instanceof ContainerTableItems) {
            setLoadDynamicAttributes(((ContainerDataGridItems<?>) table.getItems()).getContainer());
        }
    }

    protected void setMaxTextLength(Table.Column column, AttributeDefinition attribute) {
        if (attribute.getDataType() == AttributeType.STRING) {
            column.setMaxTextLength(50);
        }
    }

    protected void setColumnAlignment(Table.Column column, AttributeDefinition attribute) {
        if (!Strings.isNullOrEmpty(attribute.getConfiguration().getColumnAlignment())) {
            column.setAlignment(Table.ColumnAlignment.valueOf(attribute.getConfiguration().getColumnAlignment()));
        }
    }

    protected void setColumnWidth(Table.Column column, AttributeDefinition attribute) {
        if (attribute.getConfiguration().getColumnWidth() != null) {
            column.setWidth(attribute.getConfiguration().getColumnWidth());
        }
    }
}
