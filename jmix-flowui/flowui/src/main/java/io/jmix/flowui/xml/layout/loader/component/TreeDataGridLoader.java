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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.data.grid.EmptyTreeDataGridItems;
import io.jmix.flowui.exception.GuiDevelopmentException;

public class TreeDataGridLoader extends AbstractGridLoader<TreeDataGrid<?>> {

    protected boolean hierarchyColumnAdded = false;

    @Override
    protected TreeDataGrid<?> createComponent() {
        return factory.create(TreeDataGrid.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setupDataProvider(GridDataHolder holder) {
        String hierarchyProperty = loadString(element, "hierarchyProperty")
                .orElseThrow(
                        () -> new GuiDevelopmentException(
                                String.format(
                                        "%s doesn't have 'hierarchyProperty' attribute",
                                        resultComponent.getClass().getSimpleName()
                                ),
                                context,
                                "Component ID",
                                resultComponent.getId()
                                        .orElse("null")
                        )
                );

        if (holder.getContainer() != null) {
            Boolean showOrphans = loadBoolean(element, "showOrphans").orElse(false);

            resultComponent.setDataProvider(
                    new ContainerTreeDataGridItems(holder.getContainer(),
                            hierarchyProperty, showOrphans));
        } else if (holder.getMetaClass() != null) {
            resultComponent.setDataProvider(new EmptyTreeDataGridItems<>(holder.getMetaClass()));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Grid.Column addColumn(String key, MetaPropertyPath metaPropertyPath) {
        String hierarchyColumn = loadString(element, "hierarchyColumn").orElse(null);
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        // Initialize first column as hierarchy or
        // column that is defined in property
        if (!hierarchyColumnAdded &&
                (Strings.isNullOrEmpty(hierarchyColumn)
                        || hierarchyColumn.equals(metaProperty.getName()))) {
            hierarchyColumnAdded = true;
            return resultComponent.addHierarchyColumn(key, metaPropertyPath);
        }

        return super.addColumn(key, metaPropertyPath);
    }
}
