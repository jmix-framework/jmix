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
import io.jmix.ui.component.TreeTable;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.component.data.table.ContainerTreeTableItems;
import io.jmix.ui.component.data.table.EmptyTreeTableItems;
import io.jmix.ui.model.CollectionContainer;
import org.dom4j.Element;

public class TreeTableLoader extends AbstractTableLoader<TreeTable> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(TreeTable.NAME);
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContainerTableItems createContainerTableSource(CollectionContainer container) {
        Element rowsEl = element.element("rows");
        String hierarchyProperty = element.attributeValue("hierarchyProperty");
        if (hierarchyProperty == null && rowsEl != null) {
            hierarchyProperty = rowsEl.attributeValue("hierarchyProperty");
        }

        if (Strings.isNullOrEmpty(hierarchyProperty)) {
            throw new GuiDevelopmentException("TreeTable doesn't have 'hierarchyProperty' attribute", context,
                    "TreeTable ID", element.attributeValue("id"));
        }

        String showOrphansAttr = element.attributeValue("showOrphans");
        boolean showOrphans = showOrphansAttr == null || Boolean.parseBoolean(showOrphansAttr);

        return new ContainerTreeTableItems(container, hierarchyProperty, showOrphans);
    }

    @Override
    protected TableItems createEmptyTableItems(MetaClass metaClass) {
        return new EmptyTreeTableItems(metaClass);
    }
}