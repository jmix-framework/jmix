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

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.GridDragStartEvent;
import com.vaadin.ui.components.grid.GridDropEvent;
import com.vaadin.ui.components.grid.GridDropTarget;
import com.vaadin.ui.dnd.DragSourceExtension;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattrflowui.DynAttrUiProperties;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.widget.JmixGrid;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UiController("dynat_AttributeLocationFragment")
@UiDescriptor("attribute-location-fragment.xml")
public class AttributeLocationFragment extends ScreenFragment {

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

    @Autowired
    protected ComboBox<Integer> columnsCountLookupField;
    @Autowired
    protected HBoxLayout targetDataGridBox;
    @Autowired
    protected DataGrid<CategoryAttribute> sourceDataGrid;
    @Autowired
    private Button saveConfigurationBtn;

    protected List<CategoryAttribute> sourceDataContainer;
    protected List<List<CategoryAttribute>> dataContainers;
    protected List<DataGrid<CategoryAttribute>> childComponents;

    protected CategoryAttribute draggedItem;
    protected boolean droppedSuccessful;
    protected Grid<CategoryAttribute> dragSourceGrid;

    protected List<CategoryAttribute> attributesSourceDataContainer = new ArrayList<>();
    protected DataProvider<CategoryAttribute, SerializablePredicate<CategoryAttribute>> attributesSourceDataProvider;
    protected Grid<CategoryAttribute> attributesSourceGrid;

    protected int[] rowsCounts;
    private boolean isEnabled;

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Subscribe
    public void onAttach(AttachEvent event) {
        setupFieldsLock();
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
        if (event.getPrevValue() == null
                || event.getValue() == null
                || !event.isUserOriginated()) {
            return;
        }
        int range = event.getPrevValue() - event.getValue();
        if (range > 0) {
            for (int i = 1; i <= range; i++) {
                removeColumn(event.getPrevValue() - i);
            }
        } else {
            for (int i = 0; i < -range; i++) {
                addColumn(event.getPrevValue() + i, 0);
            }
        }
    }

    @Subscribe("saveConfigurationBtn")
    protected void onSaveConfigurationBtnClick(Button.ClickEvent event) {
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
        columnsCountLookupField.setOptionsList(getColumnsCountLookupFieldOptionsList());
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
        DataGrid<CategoryAttribute> dataGrid = uiComponents.create(DataGrid.NAME);
        DataGrid.Column<CategoryAttribute> column =
                dataGrid.addColumn("column",
                        metadataTools.resolveMetaPropertyPathOrNull(metadata.getClass(CategoryAttribute.class), "name"));
        column.setSortable(false);
        column.setCaption(messages.getMessage(AttributeLocationFragment.class, "targetDataGrid.column.caption") + " " + i);
        dataGrid.setColumnsCollapsingAllowed(false);
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
            attributesSourceGrid = dataGrid.unwrap(JmixGrid.class);
        }

        dataGrid.withUnwrapped(JmixGrid.class, grid -> {
            grid.setDataProvider(dataProvider);

            GridDragSource<CategoryAttribute> gridDragSource = new GridDragSource<>(grid);
            gridDragSource.addGridDragStartListener(this::onGridDragStart);

            GridDropTarget<CategoryAttribute> gridDropTarget = new GridDropTarget<>(grid, DropMode.BETWEEN);
            gridDropTarget.addGridDropListener(e -> onGridDrop(e, isSourceDataGrid));
        });
    }

    protected void onGridDragStart(GridDragStartEvent<CategoryAttribute> event) {
        if (isEnabled) {
            dragSourceGrid = event.getComponent();
            draggedItem = event.getDraggedItems().get(0);
            droppedSuccessful = false;
        }
    }

    protected void onGridDrop(GridDropEvent<CategoryAttribute> event, boolean isAttributesSourceGrid) {
        if (isEnabled) {
            event.getDragSourceExtension().ifPresent(source -> {
                int dropIndex = addToDestinationGrid(event, isAttributesSourceGrid, source);
                removeFromSourceGrid(dragSourceGrid, dragSourceGrid == attributesSourceGrid, event.getComponent(), dropIndex);
            });
        }
    }

    protected int addToDestinationGrid(GridDropEvent<CategoryAttribute> event, boolean isSourceGrid, DragSourceExtension source) {
        if (isSourceGrid && EMPTY_CATEGORY_ATTRIBUTE_NAME.equals(draggedItem.getName())) {
            droppedSuccessful = true;
            return -1;
        }

        if (source instanceof GridDragSource) {
            //noinspection unchecked
            ListDataProvider<CategoryAttribute> dataProvider = (ListDataProvider<CategoryAttribute>)
                    event.getComponent().getDataProvider();
            List<CategoryAttribute> items = (List<CategoryAttribute>) dataProvider.getItems();

            int i = items.size();
            if (event.getDropTargetRow().isPresent()) {
                i = items.indexOf(event.getDropTargetRow().get())
                        + (event.getDropLocation() == DropLocation.BELOW ? 1 : 0);
            }

            items.add(i, draggedItem);
            dataProvider.refreshAll();

            droppedSuccessful = true;

            return i;
        }

        return -1;
    }

    protected void removeFromSourceGrid(Grid<?> currentSourceGrid, boolean isAttributesSourceGrid, AbstractComponent dropComponent, int dropIndex) {
        if (!droppedSuccessful || draggedItem == null) {
            return;
        }

        //noinspection unchecked
        List<CategoryAttribute> items = (List<CategoryAttribute>) ((ListDataProvider<?>) currentSourceGrid.getDataProvider()).getItems();
        if (currentSourceGrid.equals(dropComponent) && dropIndex >= 0) {
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
