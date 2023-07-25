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

import com.google.common.base.Strings;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.component.data.table.ContainerTableItems;

import java.util.List;

@org.springframework.stereotype.Component("dynat_TableEmbeddingStrategy")
public class TableEmbeddingStrategy extends ListEmbeddingStrategy {

    @Override
    public boolean supportComponent(Component component) {
        return component instanceof Table && ((Table<?>) component).getItems() instanceof ContainerTableItems;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void embed(Component component, Frame frame, List<AttributeDefinition> attributes) {
        Table table = (Table) component;
        for (AttributeDefinition attribute : attributes) {
            addAttributeColumn(table, attribute);
        }
    }

    protected void addAttributeColumn(Table table, AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        MetaClass entityMetaClass = getEntityMetaClass(table);

        MetaPropertyPath propertyPath = new MetaPropertyPath(entityMetaClass, metaProperty);
        Table.Column column = table.addColumn(propertyPath);

        column.setDescription(getColumnDescription(attribute));

        column.setCaption(getColumnCaption(attribute));

        column.setFormatter(getColumnFormatter(attribute));

        column.setSortable(false);

        setMaxTextLength(column, attribute);

        setColumnAlignment(column, attribute);

        setColumnWidth(column, attribute);
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
            setLoadDynamicAttributes(((ContainerTableItems<?>) table.getItems()).getContainer());
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
