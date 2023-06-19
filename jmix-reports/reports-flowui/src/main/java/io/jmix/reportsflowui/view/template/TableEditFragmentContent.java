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

package io.jmix.reportsflowui.view.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.ItemTrackingAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableEditFragmentContent extends VerticalLayout {

    protected static final String CREATE_BAND_ID = "createBand";
    protected static final String REMOVE_BAND_ID = "removeBand";
    protected static final String UP_BAND_ID = "upBand";
    protected static final String DOWN_BAND_ID = "downBand";
    protected static final String CREATE_COLUMN_ID = "createColumn";
    protected static final String REMOVE_COLUMN_ID = "removeColumn";
    protected static final String UP_COLUMN_ID = "upColumn";
    protected static final String DOWN_COLUMN_ID = "downColumn";

    protected static final String DATA_GRIDS_BOX_CLASS_NAME = "table-edit-fragment-data-grids-box";
    protected static final String BAND_BOX_CLASS_NAME = "table-edit-fragment-band-box";

    protected UiComponents uiComponents;
    protected Metadata metadata;
    protected Actions actions;
    protected Messages messages;

    protected FlexLayout dataGridsBox;
    protected VerticalLayout bandBox;
    protected VerticalLayout columnsBox;

    protected HorizontalLayout bandsButtonsBox;
    protected HorizontalLayout columnsButtonsBox;

    protected DataGrid<TemplateTableBand> bandsDataGrid;
    protected DataGrid<TemplateTableColumn> columnsDataGrid;

    public TableEditFragmentContent(UiComponents uiComponents, Metadata metadata, Actions actions, Messages messages) {
        this.uiComponents = uiComponents;
        this.metadata = metadata;
        this.actions = actions;
        this.messages = messages;

        initComponent();
    }

    public DataGrid<TemplateTableBand> getBandsDataGrid() {
        return bandsDataGrid;
    }

    public DataGrid<TemplateTableColumn> getColumnsDataGrid() {
        return columnsDataGrid;
    }

    public Optional<ListDataComponentAction<?, ?>> getDataGridAction(String actionId) {
        return getDataGridsActions().stream()
                .filter(action -> action.getId().equals(actionId))
                .findFirst();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<ListDataComponentAction<?, ?>> getDataGridsActions() {
        Collection<ListDataComponentAction<?, ?>> actions = (Collection) getBandsDataGridActions();
        actions.addAll(getColumnsDataGridActions());
        return actions;
    }

    @SuppressWarnings("unchecked")
    public Collection<ListDataComponentAction<?, TemplateTableBand>> getBandsDataGridActions() {
        return bandsButtonsBox.getChildren()
                .map(button -> (ListDataComponentAction<?, TemplateTableBand>) ((JmixButton) button).getAction())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Collection<ListDataComponentAction<?, TemplateTableColumn>> getColumnsDataGridActions() {
        return columnsButtonsBox.getChildren()
                .map(button -> (ListDataComponentAction<?, TemplateTableColumn>) ((JmixButton) button).getAction())
                .collect(Collectors.toList());
    }

    public void bindWithData(CollectionPropertyContainer<TemplateTableBand> templateTableBandsDc,
                             CollectionPropertyContainer<TemplateTableColumn> templateTableColumnsDc) {
        bandsDataGrid.setItems(new ContainerDataGridItems<>(templateTableBandsDc));
        columnsDataGrid.setItems(new ContainerDataGridItems<>(templateTableColumnsDc));

        getBandsDataGridActions().forEach(action -> action.setTarget(bandsDataGrid));
        getColumnsDataGridActions().forEach(action -> action.setTarget(columnsDataGrid));
    }

    protected void initComponent() {
        dataGridsBox = createDataGridsBox();
        add(dataGridsBox);

        bandBox = createBandBox();
        dataGridsBox.add(bandBox);
        dataGridsBox.setFlexGrow(1, bandBox);

        columnsBox = createColumnsBox();
        dataGridsBox.add(columnsBox);
        dataGridsBox.setFlexGrow(1, columnsBox);
    }

    protected FlexLayout createDataGridsBox() {
        FlexLayout layout = uiComponents.create(FlexLayout.class);
        layout.setId("tableEditDataGridsBox");
        layout.setWidth("100%");
        layout.setClassName(DATA_GRIDS_BOX_CLASS_NAME);
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);
        return layout;
    }

    protected VerticalLayout createBandBox() {
        VerticalLayout bandBox = uiComponents.create(VerticalLayout.class);
        bandBox.setId("tableEditBandBox");
        bandBox.setPadding(false);
        bandBox.setSizeUndefined();
        bandBox.setClassName(BAND_BOX_CLASS_NAME);

        bandsButtonsBox = createBandsButtonsBox();
        bandsDataGrid = createBandsDataGrid();

        bandBox.add(bandsButtonsBox, bandsDataGrid);

        return bandBox;
    }

    protected HorizontalLayout createBandsButtonsBox() {
        HorizontalLayout bandsButtonsBox = uiComponents.create(HorizontalLayout.class);
        bandsButtonsBox.setId("tableEditBandsButtonsBox");

        bandsButtonsBox.add(
                createButtonWithAction(VaadinIcon.PLUS, CreateAction.ID, CREATE_BAND_ID, ActionVariant.PRIMARY));
        bandsButtonsBox.add(
                createButtonWithAction(VaadinIcon.TRASH, RemoveAction.ID, REMOVE_BAND_ID, ActionVariant.DANGER));
        bandsButtonsBox.add(
                createButtonWithAction(VaadinIcon.ARROW_UP, ItemTrackingAction.ID, UP_BAND_ID, null));
        bandsButtonsBox.add(
                createButtonWithAction(VaadinIcon.ARROW_DOWN, ItemTrackingAction.ID, DOWN_BAND_ID, null));

        return bandsButtonsBox;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected DataGrid<TemplateTableBand> createBandsDataGrid() {
        DataGrid<TemplateTableBand> bandsDataGrid = uiComponents.create(DataGrid.class);
        bandsDataGrid.setId("tableEditBandsDataGrid");
        bandsDataGrid.setWidthFull();

        bandsDataGrid
                .addComponentColumn(this::bandsDataGridBandNameColumnProvider)
                .setHeader(messages.getMessage(getClass(), "tableEditBandsDataGrid.bandNameColumn.header"));

        return bandsDataGrid;
    }

    @SuppressWarnings("unchecked")
    protected Component bandsDataGridBandNameColumnProvider(TemplateTableBand item) {
        TypedTextField<String> field = uiComponents.create(TypedTextField.class);
        field.setRequired(true);
        field.setValue(item.getBandName() == null ? field.getEmptyValue() : item.getBandName());
        field.setWidthFull();
        field.addValueChangeListener(event -> item.setBandName(event.getValue()));
        field.setStatusChangeHandler(__ -> {/* do nothing */});
        return field;
    }

    protected VerticalLayout createColumnsBox() {
        VerticalLayout columnsBox = uiComponents.create(VerticalLayout.class);
        columnsBox.setPadding(false);
        columnsBox.setMinWidth("20em");
        columnsBox.setSizeUndefined();

        columnsDataGrid = createColumnsDataGrid();
        columnsButtonsBox = createColumnsButtonsBox();

        columnsBox.add(columnsButtonsBox, columnsDataGrid);

        return columnsBox;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected DataGrid<TemplateTableColumn> createColumnsDataGrid() {
        DataGrid<TemplateTableColumn> columnsDataGrid = uiComponents.create(DataGrid.class);
        columnsDataGrid.setId("tableEditColumnsDataGrid");
        columnsDataGrid.setWidthFull();

        columnsDataGrid
                .addComponentColumn(this::columnsDataGridKeyColumnProvider)
                .setHeader(messages.getMessage(getClass(), "columnsDataGrid.keyColumn.header"));

        columnsDataGrid
                .addComponentColumn(this::columnsDataGridCaptionColumnProvider)
                .setHeader(messages.getMessage(getClass(), "columnsDataGrid.captionColumn.header"));

        return columnsDataGrid;
    }

    @SuppressWarnings("unchecked")
    protected Component columnsDataGridKeyColumnProvider(TemplateTableColumn item) {
        TypedTextField<String> field = uiComponents.create(TypedTextField.class);
        field.setRequired(true);
        field.setValue(item.getKey() == null ? field.getEmptyValue() : item.getKey());
        field.setWidthFull();
        field.addValueChangeListener(event -> item.setKey(event.getValue()));
        field.setStatusChangeHandler(__ -> {/* do nothing */});
        return field;
    }

    @SuppressWarnings("unchecked")
    protected Component columnsDataGridCaptionColumnProvider(TemplateTableColumn item) {
        TypedTextField<String> field = uiComponents.create(TypedTextField.class);
        field.setRequired(true);
        field.setValue(item.getCaption() == null ? field.getEmptyValue() : item.getCaption());
        field.setWidthFull();
        field.addValueChangeListener(event -> item.setCaption(event.getValue()));
        field.setStatusChangeHandler(__ -> {/* do nothing */});
        return field;
    }

    protected HorizontalLayout createColumnsButtonsBox() {
        HorizontalLayout columnsButtonsBox = uiComponents.create(HorizontalLayout.class);
        columnsButtonsBox.setId("tableEditColumnsButtonsBox");

        columnsButtonsBox.add(
                createButtonWithAction(VaadinIcon.PLUS, CreateAction.ID, CREATE_COLUMN_ID, ActionVariant.PRIMARY));
        columnsButtonsBox.add(
                createButtonWithAction(VaadinIcon.TRASH, RemoveAction.ID, REMOVE_COLUMN_ID, ActionVariant.DANGER));
        columnsButtonsBox.add(
                createButtonWithAction(VaadinIcon.ARROW_UP, ItemTrackingAction.ID, UP_COLUMN_ID, null));
        columnsButtonsBox.add(
                createButtonWithAction(VaadinIcon.ARROW_DOWN, ItemTrackingAction.ID, DOWN_COLUMN_ID, null));

        return columnsButtonsBox;
    }

    protected JmixButton createButtonWithAction(VaadinIcon icon, String actionType, String actionId,
                                                @Nullable ActionVariant variant) {
        Action action = actions.create(actionType, actionId);
        action.setText("");
        if (variant != null) {
            action.setVariant(variant);
        }

        JmixButton button = uiComponents.create(JmixButton.class);
        button.setAction(action, true);
        button.setIcon(icon.create());
        return button;
    }
}
