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

package io.jmix.ui.component.impl;

import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.CompositeComponent;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.GroupFilter;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.ResponsiveGridLayout;
import io.jmix.ui.model.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFilterImpl extends CompositeComponent<GroupBoxLayout> implements GroupFilter {

    protected UiComponents uiComponents;
    protected UiProperties uiProperties;

    protected DataLoader dataLoader;
    protected boolean autoApply = true;
    protected Integer columnsCount;

    protected Operation operation = Operation.AND;
    protected LogicalCondition queryCondition = LogicalCondition.and();

    protected List<FilterComponent> ownFilterComponentsOrder = new ArrayList<>();

    protected ResponsiveGridLayout conditionsLayout;

    public GroupFilterImpl() {
        addCreateListener(this::onCreate);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply != autoApply) {
            this.autoApply = autoApply;

            getFilterComponents().forEach(filterComponent -> filterComponent.setAutoApply(autoApply));
        }
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        if (this.operation != operation) {
            this.operation = operation;

            updateQueryCondition();
        }
    }

    @Override
    public int getColumnsCount() {
        return columnsCount != null ? columnsCount : uiProperties.getGenericFilterColumnsCount();
    }

    @Override
    public void setColumnsCount(int columnsCount) {
        if (this.columnsCount == null || this.columnsCount != columnsCount) {
            this.columnsCount = columnsCount;

            updateConditionsLayout();
        }
    }

    @Override
    public void add(FilterComponent filterComponent) {
        if (dataLoader != filterComponent.getDataLoader()) {
            throw new IllegalArgumentException("The data loader of child component must be the same as the owner " +
                    "GroupFilter component");
        }

        filterComponent.setAutoApply(isAutoApply());
        queryCondition.add(filterComponent.getQueryCondition());
        ownFilterComponentsOrder.add(filterComponent);
        updateConditionsLayout();
    }

    @Override
    public void remove(FilterComponent filterComponent) {
        if (ownFilterComponentsOrder.contains(filterComponent)) {
            ownFilterComponentsOrder.remove(filterComponent);

            updateQueryCondition();
            updateConditionsLayout();
        } else {
            ownFilterComponentsOrder.stream()
                    .filter(ownComponent -> ownComponent instanceof LogicalFilterComponent)
                    .map(ownComponent -> (LogicalFilterComponent) ownComponent)
                    .forEach(childLogicalFilterComponent -> childLogicalFilterComponent.remove(filterComponent));
        }
    }

    @Override
    public void removeAll() {
        getComposition().removeAll();
        ownFilterComponentsOrder = new ArrayList<>();
        updateQueryCondition();
        updateConditionsLayout();
    }

    @Override
    public LogicalCondition getQueryCondition() {
        return queryCondition;
    }

    @Override
    public List<FilterComponent> getOwnFilterComponents() {
        return ownFilterComponentsOrder;
    }

    @Override
    public List<FilterComponent> getFilterComponents() {
        List<FilterComponent> components = new ArrayList<>();
        for (FilterComponent ownComponent : ownFilterComponentsOrder) {
            components.add(ownComponent);
            if (ownComponent instanceof LogicalFilterComponent) {
                components.addAll(((LogicalFilterComponent) ownComponent).getFilterComponents());
            }
        }

        return components;
    }

    protected void onCreate(CreateEvent createEvent) {
        root = createRootComponent();
    }

    protected GroupBoxLayout createRootComponent() {
        GroupBoxLayout rootLayout = uiComponents.create(GroupBoxLayout.class);
        rootLayout.setWidthFull();
        return rootLayout;
    }

    protected void updateQueryCondition() {
        queryCondition = new LogicalCondition(WrapperUtils.toLogicalConditionType(operation));

        for (FilterComponent ownComponent : ownFilterComponentsOrder) {
            queryCondition.add(ownComponent.getQueryCondition());
        }
    }

    protected void updateConditionsLayout() {
        getComposition().removeAll();

        if (!getOwnFilterComponents().isEmpty()) {
            conditionsLayout = createConditionsLayout();
            getComposition().add(conditionsLayout);

            ResponsiveGridLayout.Row row = createConditionsLayoutRow(conditionsLayout);
            for (FilterComponent ownFilterComponent : getOwnFilterComponents()) {
                if (ownFilterComponent instanceof LogicalFilterComponent) {
                    addLogicalFilterComponentToConditionsLayoutRow((LogicalFilterComponent) ownFilterComponent, row);
                } else {
                    addFilterComponentToConditionsLayoutRow(ownFilterComponent, row);
                }
            }
        }

        if (dataLoader != null) {
            dataLoader.setCondition(getQueryCondition());
            if (isAutoApply()) {
                dataLoader.load();
            }
        }
    }

    protected ResponsiveGridLayout createConditionsLayout() {
        ResponsiveGridLayout layout = uiComponents.create(ResponsiveGridLayout.NAME);
        layout.setStyleName("px-0");
        return layout;
    }

    protected ResponsiveGridLayout.Row createConditionsLayoutRow(ResponsiveGridLayout layout) {
        ResponsiveGridLayout.Row row = layout.addRow();

        Map<ResponsiveGridLayout.Breakpoint, ResponsiveGridLayout.RowColumnsValue> rowColumns = new HashMap<>();
        rowColumns.put(ResponsiveGridLayout.Breakpoint.XS, ResponsiveGridLayout.RowColumnsValue.columns(1));
        rowColumns.put(ResponsiveGridLayout.Breakpoint.LG, ResponsiveGridLayout.RowColumnsValue.columns(2));
        rowColumns.put(ResponsiveGridLayout.Breakpoint.XL, ResponsiveGridLayout.RowColumnsValue.columns(3));
        row.setRowColumns(rowColumns);

        row.setAlignItems(ResponsiveGridLayout.AlignItems.CENTER);
        return row;
    }

    protected void addLogicalFilterComponentToConditionsLayoutRow(LogicalFilterComponent logicalFilterComponent,
                                                                  ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column column = createLogicalFilterComponentColumn(row);
        logicalFilterComponent.setParent(null);
        ComponentsHelper.getComposition(logicalFilterComponent).setParent(null);
        column.setComponent(logicalFilterComponent);
    }

    protected ResponsiveGridLayout.Column createLogicalFilterComponentColumn(ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column column = row.addColumn();
        column.setColumns(ResponsiveGridLayout.Breakpoint.XL, ResponsiveGridLayout.ColumnsValue.columns(12));

        int columnIndex = row.getColumns().indexOf(column);
        if (columnIndex != 0) {
            column.addStyleName("pt-2");
        }

        return column;
    }

    protected void addFilterComponentToConditionsLayoutRow(FilterComponent filterComponent,
                                                           ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column conditionValueColumn = createFilterComponentColumn(row);
        filterComponent.setParent(null);
        ComponentsHelper.getComposition(filterComponent).setParent(null);
        filterComponent.setWidthFull();
        conditionValueColumn.setComponent(filterComponent);
    }

    protected ResponsiveGridLayout.Column createFilterComponentColumn(ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column column = row.addColumn();

        boolean logicalFilterComponentAdded = row.getColumns().stream()
                .anyMatch(rowColumn -> rowColumn.getComponent() instanceof LogicalFilterComponent);

        int columnIndex = row.getColumns().indexOf(column);
        if (columnIndex != 0) {
            column.addStyleName("pt-2");
        }

        if (!logicalFilterComponentAdded) {
            if (columnIndex == 1) {
                column.addStyleName("pt-lg-0");
            }

            if (columnIndex == 2) {
                column.addStyleName("pt-xl-0");
            }
        }

        return column;
    }
}
