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

package io.jmix.ui.app.filter.condition;

import io.jmix.ui.action.Action;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.Tree;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.GroupFilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@UiController("ui_GroupFilterCondition.edit")
@UiDescriptor("group-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class GroupFilterConditionEdit extends LogicalFilterConditionEdit<GroupFilterCondition> {

    @Autowired
    protected InstanceContainer<GroupFilterCondition> filterConditionDc;
    @Autowired
    protected CollectionContainer<FilterCondition> filterConditionsDc;

    @Autowired
    protected Button moveDownButton;
    @Autowired
    protected Button moveUpButton;
    @Autowired
    protected Tree<FilterCondition> conditionsTree;

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
    public FilterAddConditionAction getAddAction() {
        return (FilterAddConditionAction) conditionsTree.getAction("addCondition");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public EditAction<FilterCondition> getEditAction() {
        return (EditAction<FilterCondition>) conditionsTree.getAction("edit");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public RemoveAction<FilterCondition> getRemoveAction() {
        return (RemoveAction<FilterCondition>) conditionsTree.getAction("remove");
    }

    @Nullable
    @Override
    public ListComponent<FilterCondition> getListComponent() {
        return conditionsTree;
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        expandItems();
    }

    @Install(to = "conditionsTree", subject = "lookupSelectHandler")
    protected void conditionsTreeLookupSelectHandler(Collection<FilterCondition> collection) {
        if (getEditAction() != null) {
            getEditAction().execute();
        }
    }

    @Install(to = "operationField", subject = "optionCaptionProvider")
    protected String operationFieldOptionCaptionProvider(LogicalFilterComponent.Operation operation) {
        return logicalFilterSupport.getOperationCaption(operation);
    }

    @Subscribe("conditionsTree.moveUp")
    protected void onConditionsTreeMoveUp(Action.ActionPerformedEvent event) {
        FilterCondition selectedCondition = conditionsTree.getSingleSelected();
        if (selectedCondition != null) {
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
    }

    @Subscribe("conditionsTree.moveDown")
    protected void onConditionsTreeMoveDown(Action.ActionPerformedEvent event) {
        FilterCondition selectedCondition = conditionsTree.getSingleSelected();
        if (selectedCondition != null) {
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
    }

    @Subscribe("conditionsTree")
    protected void onConditionsTreeSelection(Tree.SelectionEvent<FilterCondition> event) {
        if (!event.getSelected().isEmpty()) {
            FilterCondition selectedCondition = event.getSelected().iterator().next();
            refreshMoveButtonsState(selectedCondition);
        }
    }

    protected void refreshMoveButtonsState(FilterCondition selectedCondition) {
        boolean moveUpButtonEnabled = false;
        boolean moveDownButtonEnabled = false;
        FilterCondition parent = selectedCondition.getParent();
        if (parent instanceof LogicalFilterCondition) {
            int index = ((LogicalFilterCondition) parent).getOwnFilterConditions().indexOf(selectedCondition);
            moveUpButtonEnabled = index > 0;
            moveDownButtonEnabled = index < ((LogicalFilterCondition) parent).getOwnFilterConditions().size() - 1;
        }

        boolean notReadOnly = !isReadOnly();
        moveUpButton.setEnabled(moveUpButtonEnabled && notReadOnly);
        moveDownButton.setEnabled(moveDownButtonEnabled && notReadOnly);
    }
}
