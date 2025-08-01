/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.filter.condition;

import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.action.logicalfilter.LogicalFilterEditAction;
import io.jmix.flowui.action.view.DetailSaveCloseAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ViewController("flowui_GroupFilterCondition.detail")
@ViewDescriptor("group-filter-condition-detail-view.xml")
@EditedEntityContainer("filterConditionDc")
@DialogMode(width = "64em")
public class GroupFilterConditionDetailView extends LogicalFilterConditionDetailView<GroupFilterCondition> {

    protected static final String GROUP_FILTER_CLASS_NAME = "jmix-group-filter";

    @ViewComponent
    protected InstanceContainer<GroupFilterCondition> filterConditionDc;
    @ViewComponent
    protected CollectionContainer<FilterCondition> filterConditionsDc;
    @ViewComponent
    protected JmixSelect<LogicalFilterComponent.Operation> operationField;
    @ViewComponent
    protected JmixCheckbox operationTextVisibleField;
    @ViewComponent
    protected H4 groupConditionTitle;

    @ViewComponent
    protected JmixButton moveDownBtn;
    @ViewComponent
    protected JmixButton moveUpBtn;
    @ViewComponent
    protected TreeDataGrid<FilterCondition> conditionsTreeDataGrid;

    @ViewComponent
    private DetailSaveCloseAction<?> saveAction;

    protected String title;

    @Override
    public InstanceContainer<GroupFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Override
    public CollectionContainer<FilterCondition> getCollectionContainer() {
        return filterConditionsDc;
    }

    @Nullable
    @Override
    public GenericFilterAddConditionAction getAddAction() {
        return (GenericFilterAddConditionAction) conditionsTreeDataGrid.getAction("addCondition");
    }

    @Nullable
    @Override
    public LogicalFilterEditAction getEditAction() {
        return (LogicalFilterEditAction) conditionsTreeDataGrid.getAction("edit");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public RemoveAction<FilterCondition> getRemoveAction() {
        return (RemoveAction<FilterCondition>) conditionsTreeDataGrid.getAction("remove");
    }

    @Nullable
    @Override
    public ListDataComponent<FilterCondition> getListDataComponent() {
        return conditionsTreeDataGrid;
    }

    public void setGroupConditionTitle(@Nullable String title) {
        groupConditionTitle.setText(Strings.nullToEmpty(title));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getPageTitle() {
        return title != null
                ? title
                : super.getPageTitle();
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<GroupFilterCondition> event) {
        event.getEntity().setStyleName(GROUP_FILTER_CLASS_NAME);
    }

    @Override
    protected void setupEntityToEdit(GroupFilterCondition entityToEdit) {
        initCheckboxesState(entityToEdit);

        super.setupEntityToEdit(entityToEdit);
    }

    protected void initCheckboxesState(GroupFilterCondition entityToEdit) {
        operationTextVisibleField.setVisible(entityToEdit.getVisible());
    }

    @Override
    protected void refreshChildrenConditions() {
        super.refreshChildrenConditions();

        // lazy initialization of a multi selection model
        if (!getCollectionContainer().getItems().isEmpty()
                && conditionsTreeDataGrid.getSelectionMode() != Grid.SelectionMode.MULTI) {
            conditionsTreeDataGrid.enableMultiSelect();
        }
    }

    @Subscribe(id = "filterConditionDc", target = Target.DATA_CONTAINER)
    public void onFilterConditionDcItemPropertyChange(ItemPropertyChangeEvent<GroupFilterCondition> event) {
        String property = event.getProperty();

        if ("visible".equals(property)) {
            operationTextVisibleField.setVisible(Boolean.TRUE.equals(event.getValue()));
        }
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        getCollectionContainer().addItemChangeListener(itemChangeEvent ->
                refreshMoveButtonsState(itemChangeEvent.getItem()));
        operationField.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getValue() != null) {
                getEditedEntity().setLocalizedLabel(logicalFilterSupport.getOperationText(valueChangeEvent.getValue()));
            }
        });

        expandItems();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        saveAction.setEnabled(!readOnly);
    }

    @Subscribe("conditionsTreeDataGrid.moveUp")
    public void onMoveUp(ActionPerformedEvent event) {
        FilterCondition selectedCondition = conditionsTreeDataGrid.getSingleSelectedItem();

        if (selectedCondition == null) {
            return;
        }

        FilterCondition parent = selectedCondition.getParent();

        if (parent instanceof LogicalFilterCondition) {
            List<FilterCondition> items = getCollectionContainer().getMutableItems();
            List<FilterCondition> ownConditions = ((LogicalFilterCondition) parent).getOwnFilterConditions();

            int selectedItemIndex = items.indexOf(selectedCondition);
            int selectedOwnItemIndex = ownConditions.indexOf(selectedCondition);
            FilterCondition replacedCondition = ownConditions.get(selectedOwnItemIndex - 1);

            Collections.swap(items, selectedItemIndex, items.indexOf(replacedCondition));
            Collections.swap(ownConditions, selectedOwnItemIndex, selectedOwnItemIndex - 1);
            refreshMoveButtonsState(selectedCondition);
        }
    }

    @Subscribe("conditionsTreeDataGrid.moveDown")
    protected void onMoveDown(ActionPerformedEvent event) {
        FilterCondition selectedCondition = conditionsTreeDataGrid.getSingleSelectedItem();

        if (selectedCondition == null) {
            return;
        }

        FilterCondition parent = selectedCondition.getParent();

        if (parent instanceof LogicalFilterCondition) {
            List<FilterCondition> items = getCollectionContainer().getMutableItems();
            List<FilterCondition> ownConditions = ((LogicalFilterCondition) parent).getOwnFilterConditions();

            int selectedItemIndex = items.indexOf(selectedCondition);
            int selectedOwnItemIndex = ownConditions.indexOf(selectedCondition);
            FilterCondition replacedCondition = ownConditions.get(selectedOwnItemIndex + 1);

            Collections.swap(items, selectedItemIndex, items.indexOf(replacedCondition));
            Collections.swap(ownConditions, selectedOwnItemIndex, selectedOwnItemIndex + 1);
            refreshMoveButtonsState(selectedCondition);
        }
    }

    @Install(to = "conditionsTreeDataGrid", subject = "itemSelectableProvider")
    protected boolean conditionsTreeDataGridItemSelectableProvider(FilterCondition filterCondition) {
        return !Objects.equals(filterCondition, getEditedEntity());
    }

    @Install(to = "operationField", subject = "itemLabelGenerator")
    protected String operationFieldItemLabelGenerator(LogicalFilterComponent.Operation operation) {
        return logicalFilterSupport.getOperationText(operation);
    }

    protected void refreshMoveButtonsState(@Nullable FilterCondition selectedCondition) {
        if (selectedCondition == null) {
            moveUpBtn.setEnabled(false);
            moveDownBtn.setEnabled(false);

            return;
        }

        boolean moveUpButtonEnabled = false;
        boolean moveDownButtonEnabled = false;

        FilterCondition parent = selectedCondition.getParent();

        if (parent instanceof LogicalFilterCondition) {
            List<FilterCondition> conditions = ((LogicalFilterCondition) parent).getOwnFilterConditions();
            int index = conditions.indexOf(selectedCondition);
            moveUpButtonEnabled = index > 0;
            moveDownButtonEnabled = index < conditions.size() - 1;
        }

        boolean notReadOnly = !isReadOnly();

        moveUpBtn.setEnabled(moveUpButtonEnabled && notReadOnly);
        moveDownBtn.setEnabled(moveDownButtonEnabled && notReadOnly);
    }
}
