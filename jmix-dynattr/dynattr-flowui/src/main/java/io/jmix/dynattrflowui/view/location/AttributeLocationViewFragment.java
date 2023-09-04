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

package io.jmix.dynattrflowui.view.location;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattrflowui.DynAttrUiProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_AttributeLocationViewFragment")
@ViewDescriptor("attribute-location-view-fragment.xml")
public class AttributeLocationViewFragment extends StandardView {

    protected static String EMPTY_CATEGORY_ATTRIBUTE_NAME = "   ";

    @Autowired
    protected DynAttrUiProperties properties;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;

    @ViewComponent
    protected JmixComboBox<Integer> columnsCountLookupField;
    @ViewComponent
    protected HorizontalLayout targetDataGridBox;
    @ViewComponent
    protected DataGrid<CategoryAttribute> sourceDataGrid;
    @ViewComponent
    private JmixButton saveConfigurationBtn;

    protected List<CategoryAttribute> sourceDataContainer;
    protected List<List<CategoryAttribute>> dataContainers;
    protected List<DataGrid<CategoryAttribute>> childComponents;

    protected CategoryAttribute draggedItem;
    protected boolean droppedSuccessful;
    protected DataGrid<CategoryAttribute> dragSourceGrid;

    protected List<CategoryAttribute> attributesSourceDataContainer = new ArrayList<>();
    protected DataProvider<CategoryAttribute, SerializablePredicate<CategoryAttribute>> attributesSourceDataProvider;
    protected DataGrid<CategoryAttribute> attributesSourceGrid;

    protected int[] rowsCounts;
    private boolean isEnabled;

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        setupFieldsLock();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refresh();
    }

    protected void setupFieldsLock() {
        if (!this.isEnabled) {
            saveConfigurationBtn.setEnabled(false);
            columnsCountLookupField.setEnabled(false);
        }
    }

    public void setCategoryAttributes(List<CategoryAttribute> categoryAttributes) {
        this.sourceDataContainer = categoryAttributes;
        refresh();
    }

    @Subscribe("columnsCountLookupField")
    protected void onColumnsCountLookupFieldValueChange(HasValue.ValueChangeEvent<Integer> event) {
        if (event.getOldValue() == null
                || event.getValue() == null
                || !event.isFromClient()) {
            return;
        }
        int range = event.getOldValue() - event.getValue();
        if (range > 0) {
            for (int i = 1; i <= range; i++) {
                removeColumn(event.getOldValue() - i);
            }
        } else {
            for (int i = 0; i < -range; i++) {
                addColumn(event.getOldValue() + i, 0);
            }
        }
    }

    @Subscribe("saveConfigurationBtn")
    protected void onSaveConfigurationBtnClick(ClickEvent<Button> event) {
        for (List<CategoryAttribute> currentList : dataContainers) {
            for (CategoryAttribute attribute : currentList) {
                if (!EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(attribute.getName())) {
                    CategoryAttributeConfiguration configuration = attribute.getConfiguration();
                    if (configuration != null) {
                        configuration.setColumnNumber(dataContainers.indexOf(currentList));
                        configuration.setRowNumber(currentList.indexOf(attribute));
                        attribute.setConfiguration((CategoryAttributeConfiguration) configuration.clone());
                    }
                }
            }
        }

        for (CategoryAttribute attribute : sourceDataContainer) {
            if (!EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(attribute.getName())) {
                CategoryAttributeConfiguration configuration = attribute.getConfiguration();
                if (configuration != null) {
                    configuration.setColumnNumber(null);
                    configuration.setRowNumber(null);
                    attribute.setConfiguration((CategoryAttributeConfiguration) configuration.clone());
                }
            }
        }
    }

    protected void refresh() {
        refreshTargetDataGridBox();
        refreshContainers();
        refreshDataGridDragAndDrop(sourceDataGrid, sourceDataContainer, true);
        refreshRowsCounts();
        refreshColumnsCountLookupField();
        refreshTargetDataGrids(getMaxColumnIndex() + 1);
    }

    protected void refreshTargetDataGridBox() {
        targetDataGridBox.removeAll();
    }

    protected void refreshContainers() {
        sourceDataContainer.add(createEmptyCategoryAttribute());
        dataContainers = new ArrayList<>();
        childComponents = new ArrayList<>();
    }

    protected CategoryAttribute createEmptyCategoryAttribute() {
        CategoryAttribute emptyCategoryAttribute = metadata.create(CategoryAttribute.class);
        emptyCategoryAttribute.setName(EMPTY_CATEGORY_ATTRIBUTE_NAME);
        return emptyCategoryAttribute;
    }

    protected void refreshRowsCounts() {
        int maxColumnIndex = getMaxColumnIndex();
        rowsCounts = new int[maxColumnIndex + 1];

        for (CategoryAttribute entity : sourceDataContainer) {
            CategoryAttributeConfiguration conf = entity.getConfiguration();
            if (conf.getColumnNumber() != null
                    && conf.getRowNumber() != null) {
                if (rowsCounts[conf.getColumnNumber()] <= conf.getRowNumber()) {
                    rowsCounts[conf.getColumnNumber()] = conf.getRowNumber() + 1;
                }
            }
        }
    }

    protected int getMaxColumnIndex() {
        return sourceDataContainer.stream()
                .filter(categoryAttribute -> categoryAttribute.getConfiguration() != null
                        && categoryAttribute.getConfiguration().getColumnNumber() != null)
                .mapToInt(categoryAttribute -> categoryAttribute.getConfiguration().getColumnNumber())
                .max()
                .orElse(0);
    }

    protected void refreshColumnsCountLookupField() {
        columnsCountLookupField.setItems(getColumnsCountLookupFieldOptionsList());
        columnsCountLookupField.setValue(getMaxColumnIndex() + 1);
    }

    protected List<Integer> getColumnsCountLookupFieldOptionsList() {
        int maxColumns = properties.getDynamicAttributesPanelMaxColumnsCount();
        if (maxColumns < 1) {
            maxColumns = 1;
        }

        return IntStream.range(1, maxColumns + 1)
                .boxed()
                .collect(Collectors.toList());
    }

    protected void addColumn(int index, int elementsCount) {
        DataGrid<CategoryAttribute> targetDataGrid = createDataGrid(index + 1);

        childComponents.add(targetDataGrid);
        targetDataGridBox.add(targetDataGrid);

        List<CategoryAttribute> dataContainer;

        if (elementsCount > 0) {
            dataContainer = new ArrayList<>();
            for (int i = 0; i < elementsCount; i++) {
                dataContainer.add(createEmptyCategoryAttribute());
            }
        } else {
            dataContainer = new ArrayList<>();
        }
        dataContainers.add(dataContainer);

        refreshDataGridDragAndDrop(targetDataGrid, dataContainer, false);
    }

    protected DataGrid<CategoryAttribute> createDataGrid(int i) {
        DataGrid<CategoryAttribute> dataGrid = uiComponents.create(DataGrid.class);
        DataGrid.Column<CategoryAttribute> column =
                dataGrid.addColumn("column",
                        metadataTools.resolveMetaPropertyPathOrNull(metadata.getClass(CategoryAttribute.class), "name"));
        column.setSortable(false);
        column.setHeader(messages.getMessage(AttributeLocationViewFragment.class, "targetDataGrid.column.title") + " " + i);
        dataGrid.setWidth("175px");
        return dataGrid;
    }

    protected void removeColumn(int index) {
        DataGrid<CategoryAttribute> columnToRemove = childComponents.get(index);
        List<CategoryAttribute> dataContainer = dataContainers.get(index);

        sourceDataContainer.addAll(dataContainer.stream()
                .filter(e -> !EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(e.getName()))
                .collect(Collectors.toList()));
        targetDataGridBox.remove(columnToRemove);
        dataContainers.remove(index);
        childComponents.remove(index);

        refreshSourceDataProvider();
    }

    protected void refreshTargetDataGrids(int columnsCount) {
        for (int i = 0; i < columnsCount; i++) {
            if (i < rowsCounts.length) {
                addColumn(i, rowsCounts[i]);
            } else {
                addColumn(i, 0);
            }
        }

        List<CategoryAttribute> removeFromSource = new ArrayList<>();
        for (CategoryAttribute entity : sourceDataContainer) {
            CategoryAttributeConfiguration conf = entity.getConfiguration();
            if (conf.getColumnNumber() != null && conf.getRowNumber() != null) {
                dataContainers.get(conf.getColumnNumber()).set(conf.getRowNumber(), entity);
                removeFromSource.add(entity);
            }
        }
        sourceDataContainer.removeAll(removeFromSource);
    }

    protected void refreshDataGridDragAndDrop(DataGrid<CategoryAttribute> dataGrid,
                                              List<CategoryAttribute> dataContainer,
                                              boolean isSourceDataGrid) {
        DataProvider<CategoryAttribute, SerializablePredicate<CategoryAttribute>> dataProvider = new ListDataProvider<>(dataContainer);
        if (isSourceDataGrid) {
            attributesSourceDataContainer = dataContainer;
            attributesSourceDataProvider = dataProvider;
            attributesSourceGrid = dataGrid;
        }

        dataGrid.setDataProvider(dataProvider);
        dataGrid.addDragStartListener(this::onGridDragStart);
        dataGrid.addDropListener(e -> onGridDrop(e, isSourceDataGrid));

    }

    protected void onGridDragStart(GridDragStartEvent<CategoryAttribute> event) {
        if (isEnabled) {
            dragSourceGrid = (DataGrid<CategoryAttribute>) event.getSource();
            draggedItem = event.getDraggedItems().get(0);
            droppedSuccessful = false;
        }
    }

    protected void onGridDrop(GridDropEvent<CategoryAttribute> event, boolean isAttributesSourceGrid) {
        if (isEnabled) {
            event.getDropTargetItem().ifPresent(source -> {
                int dropIndex = addToDestinationGrid(event, isAttributesSourceGrid);
                removeFromSourceGrid(dragSourceGrid, dragSourceGrid == attributesSourceGrid, dropIndex);
            });
        }
    }
    // todo check drag and drop
    protected int addToDestinationGrid(GridDropEvent<CategoryAttribute> event, boolean isSourceGrid) {
        if (isSourceGrid && EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(draggedItem.getName())) {
            droppedSuccessful = true;
            return -1;
        }
        //noinspection unchecked
        ListDataProvider<CategoryAttribute> dataProvider = (ListDataProvider<CategoryAttribute>) sourceDataGrid.getDataProvider();
        List<CategoryAttribute> items = (List<CategoryAttribute>) dataProvider.getItems();
        int i = event.getDropLocation().ordinal();
        items.add(i, draggedItem);
        dataProvider.refreshAll();
        droppedSuccessful = true;
        return i;
    }

    protected void removeFromSourceGrid(DataGrid<?> currentSourceGrid, boolean isAttributesSourceGrid, int dropIndex) {
        if (!droppedSuccessful || draggedItem == null) {
            return;
        }
        //noinspection unchecked
        List<CategoryAttribute> items = (List<CategoryAttribute>) ((ListDataProvider<?>) currentSourceGrid.getDataProvider()).getItems();
        if (dropIndex >= 0) {
            int removeIndex = items.indexOf(draggedItem) == dropIndex
                    ? items.lastIndexOf(draggedItem)
                    : items.indexOf(draggedItem);
            if (removeIndex >= 0 && removeIndex != dropIndex) {
                items.remove(removeIndex);
            }
        } else {
            items.remove(draggedItem);
        }
        if (isAttributesSourceGrid && EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(draggedItem.getName())) {
            attributesSourceDataContainer.add(createEmptyCategoryAttribute());
        }
        currentSourceGrid.getDataProvider().refreshAll();
    }

    protected void refreshSourceDataProvider() {
        if (attributesSourceDataProvider != null) {
            attributesSourceDataProvider.refreshAll();
        }
    }
}
