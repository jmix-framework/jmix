/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Strings;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.datagrid.ContainerTreeDataGridItems;
import io.jmix.ui.component.data.datagrid.EmptyTreeDataGridItems;
import io.jmix.ui.model.CollectionContainer;
import org.dom4j.Element;

public class TreeDataGridLoader extends AbstractDataGridLoader<TreeDataGrid> {

    @Override
    protected TreeDataGrid createComponentInternal() {
        return factory.create(TreeDataGrid.NAME);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadHierarchyColumn(resultComponent, element);
    }

    protected void loadHierarchyColumn(TreeDataGrid component, Element element) {
        String hierarchyColumn = element.attributeValue("hierarchyColumn");
        if (!Strings.isNullOrEmpty(hierarchyColumn)) {
            component.setHierarchyColumn(hierarchyColumn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DataGridItems createContainerDataGridSource(CollectionContainer container) {
        String hierarchyProperty = element.attributeValue("hierarchyProperty");
        if (Strings.isNullOrEmpty(hierarchyProperty)) {
            throw new GuiDevelopmentException("TreeDataGrid doesn't have 'hierarchyProperty' attribute",
                    context, "TreeDataGrid ID", element.attributeValue("id"));
        }

        String showOrphansAttr = element.attributeValue("showOrphans");
        boolean showOrphans = showOrphansAttr == null || Boolean.parseBoolean(showOrphansAttr);

        return new ContainerTreeDataGridItems(container, hierarchyProperty, showOrphans);
    }

    @Override
    protected DataGridItems createEmptyDataGridItems(MetaClass metaClass) {
        return new EmptyTreeDataGridItems(metaClass);
    }
}
