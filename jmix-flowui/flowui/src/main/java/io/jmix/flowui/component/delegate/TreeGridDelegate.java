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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_TreeGridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TreeGridDelegate<E, ITEMS extends DataGridItems<E>>
        extends AbstractGridDelegate<TreeDataGrid<E>, E, ITEMS> {

    public TreeGridDelegate(TreeDataGrid<E> component) {
        super(component);
    }

    public Grid.Column<E> addHierarchyColumn(String key, MetaPropertyPath metaPropertyPath) {
        Grid.Column<E> column = addHierarchyColumnInternal(key, metaPropertyPath);
        propertyColumns.put(column, metaPropertyPath);
        return column;
    }

    @Override
    protected void setupEmptyDataProvider() {
        component.setDataProvider(new TreeDataProvider<>(new TreeData<>()));
    }

    protected Grid.Column<E> addHierarchyColumnInternal(String key, MetaPropertyPath metaPropertyPath) {
        ValueProvider<E, ?> valueProvider = getValueProvider(metaPropertyPath);

        // Also it leads to adding column to {@link #columns} list
        Grid.Column<E> column = component.addHierarchyColumn(valueProvider);
        column.setKey(key);

        initColumn(column, metaPropertyPath);

        return column;
    }
}
