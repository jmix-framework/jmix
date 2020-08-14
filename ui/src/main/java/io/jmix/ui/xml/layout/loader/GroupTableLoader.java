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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.FetchPlan;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.table.ContainerGroupTableItems;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.component.data.table.EmptyGroupTableItems;
import io.jmix.ui.gui.data.impl.AggregatableDelegate;
import io.jmix.ui.model.CollectionContainer;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GroupTableLoader extends AbstractTableLoader<GroupTable> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(GroupTable.NAME);
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        String fixedGroupingString = element.attributeValue("fixedGrouping");
        if (StringUtils.isNotEmpty(fixedGroupingString)) {
            resultComponent.setFixedGrouping(Boolean.parseBoolean(fixedGroupingString));
        }
    }

    @Override
    protected List<Table.Column> loadColumns(Table component, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        List<Table.Column> columns = new ArrayList<>();

        Element groupElement = columnsElement.element("group");
        if (groupElement != null) {
            columns.addAll(super.loadColumns(component, groupElement, metaClass, fetchPlan));
            final List<Object> groupProperties = new ArrayList<>(columns.size());
            for (Table.Column column : columns) {
                if (column.isCollapsed()) {
                    String msg = String.format("Can't group by collapsed column: %s", column.getId());
                    throw new GuiDevelopmentException(msg, context);
                }

                if (column.isGroupAllowed()) {
                    groupProperties.add(column.getId());
                }
            }
            getComponentContext().addPostInitTask((context1, window) ->
                    ((GroupTable) component).groupBy(groupProperties.toArray())
            );
        }

        // check for duplicate
        String includeAll = columnsElement.attributeValue("includeAll");
        if (StringUtils.isNotBlank(includeAll)) {
            List<Table.Column> columnList = super.loadColumns(component, columnsElement, metaClass, fetchPlan);
            for (Table.Column column : columnList) {
                if (!columns.contains(column)) {
                    columns.add(column);
                }
            }
        } else {
            columns.addAll(super.loadColumns(component, columnsElement, metaClass, fetchPlan));
        }

        return columns;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContainerTableItems createContainerTableSource(CollectionContainer container) {
        return new ContainerGroupTableItems(container, applicationContext.getBean(AggregatableDelegate.class));
    }

    @Override
    protected TableItems createEmptyTableItems(MetaClass metaClass) {
        return new EmptyGroupTableItems(metaClass);
    }
}
