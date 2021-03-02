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

import io.jmix.core.Messages;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.groupfilter.LogicalFilterSupport;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class LogicalFilterConditionEdit<E extends LogicalFilterCondition> extends FilterConditionEdit<E> {

    @Autowired
    protected Messages messages;
    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected LogicalFilterSupport logicalFilterSupport;

    protected Filter.Configuration configuration;
    protected List<FilterCondition> filterConditions = new ArrayList<>();

    public abstract CollectionContainer<FilterCondition> getCollectionContainer();

    @Nullable
    public abstract FilterAddConditionAction getAddAction();

    @Nullable
    public abstract EditAction<FilterCondition> getEditAction();

    @Nullable
    public abstract RemoveAction<FilterCondition> getRemoveAction();

    @Nullable
    public abstract ListComponent<FilterCondition> getListComponent();

    public Filter.Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Filter.Configuration configuration) {
        this.configuration = configuration;
    }

    @Subscribe
    protected void onBeforeShowEvent(BeforeShowEvent event) {
        refreshChildrenConditions();
        initAddAction();
        initEditAction();
        initRemoveAction();
    }

    protected void refreshChildrenConditions() {
        getCollectionContainer().setItems(logicalFilterSupport.getChildrenConditions(getEditedEntity()));
    }

    protected void initAddAction() {
        FilterAddConditionAction addAction = getAddAction();
        if (addAction != null && getListComponent() != null) {
            addAction.setIcon(null);
            addAction.setFilter(getConfiguration().getOwner());
            addAction.refreshState();
            addAction.setSelectHandler(this::addActionSelectHandler);
        }
    }

    protected void addActionSelectHandler(Collection<FilterCondition> selectedConditions) {
        if (getListComponent() != null) {
            if (!selectedConditions.isEmpty()) {
                for (FilterCondition selectedCondition : selectedConditions) {
                    LogicalFilterCondition parent;
                    FilterCondition singleSelected = getListComponent().getSingleSelected();
                    if (singleSelected != null) {
                        if (singleSelected instanceof LogicalFilterCondition) {
                            parent = (LogicalFilterCondition) singleSelected;
                        } else {
                            parent = (LogicalFilterCondition) singleSelected.getParent();
                        }
                    } else {
                        parent = getEditedEntity();
                    }

                    parent.getOwnFilterConditions().add(selectedCondition);
                    selectedCondition.setParent(parent);

                    refreshChildrenConditions();
                    expandItems();
                }
            }
        }
    }

    protected void expandItems() {
        if (getListComponent() instanceof Tree) {
            ((Tree<FilterCondition>) getListComponent()).expandTree();
        }
    }

    protected void initEditAction() {
        EditAction<FilterCondition> editAction = getEditAction();
        if (getListComponent() != null && editAction != null) {
            editAction.setIcon(null);
            editAction.setScreenConfigurer(this::editActionScreenConfigurer);
            editAction.setAfterCloseHandler(this::editActionAfterCloseHandler);

            getCollectionContainer().addItemChangeListener(event -> {
                FilterCondition item = event.getItem();
                if (item != null) {
                    editAction.setScreenId(filterComponents.getEditScreenId(item.getClass()));
                }
            });
        }
    }

    protected void editActionScreenConfigurer(Screen screen) {
        if (getListComponent() != null) {
            FilterCondition selectedCondition = getListComponent().getSingleSelected();
            if (screen instanceof LogicalFilterConditionEdit
                    && selectedCondition instanceof LogicalFilterCondition) {
                ((LogicalFilterConditionEdit<?>) screen).setConfiguration(configuration);
            }
        }
    }

    protected void editActionAfterCloseHandler(AfterCloseEvent afterCloseEvent) {
        Screen screen = afterCloseEvent.getScreen();
        if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)
                && screen instanceof EditorScreen
                && getListComponent() != null) {
            FilterCondition selectedCondition = getListComponent().getSingleSelected();
            FilterCondition editedCondition = (FilterCondition) ((EditorScreen<?>) screen).getEditedEntity();

            if (selectedCondition != null
                    && selectedCondition.getParent() instanceof LogicalFilterCondition) {
                LogicalFilterCondition parent = (LogicalFilterCondition) selectedCondition.getParent();
                int index = parent.getOwnFilterConditions().indexOf(selectedCondition);
                parent.getOwnFilterConditions().set(index, editedCondition);

                refreshChildrenConditions();
                expandItems();
            }
        }
    }

    protected void initRemoveAction() {
        RemoveAction<FilterCondition> removeAction = getRemoveAction();
        if (removeAction != null && getListComponent() != null) {
            removeAction.setIcon(null);
            removeAction.setAfterActionPerformedHandler(this::afterActionPerformedHandler);
        }
    }

    protected void afterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<FilterCondition> event) {
        event.getItems().forEach(filterCondition -> {
            FilterCondition parent = filterCondition.getParent();
            if (parent instanceof LogicalFilterCondition) {
                ((LogicalFilterCondition) parent).getOwnFilterConditions().remove(filterCondition);
            }
        });

        refreshChildrenConditions();
    }

    @Subscribe
    protected void onValidationEvent(ValidationEvent event) {
        ValidationErrors errors = performOwnFilterComponentsValidation();
        event.addErrors(errors);
    }

    protected ValidationErrors performOwnFilterComponentsValidation() {
        if (hasEmptyLogicalFilterConditions()) {
            return ValidationErrors.of(messages.getMessage(LogicalFilterConditionEdit.class,
                    "logicalFilterConditionEdit.logicalConditionCannotBeEmpty"));
        }

        if (configurationExist()) {
            return ValidationErrors.of(messages.getMessage(LogicalFilterConditionEdit.class,
                    "logicalFilterConditionEdit.uniqueConfigurationId"));
        }

        return ValidationErrors.none();
    }

    protected boolean hasEmptyLogicalFilterConditions() {
        for (FilterCondition filterCondition : getCollectionContainer().getItems()) {
            if (filterCondition instanceof LogicalFilterCondition
                    && ((LogicalFilterCondition) filterCondition).getOwnFilterConditions().isEmpty()) {
                return true;
            }
        }

        return getCollectionContainer().getItems().isEmpty();
    }

    protected boolean configurationExist() {
        Filter filter = configuration.getOwner();
        return filter.getConfiguration(configuration.getId()) != null
                && !Objects.equals(filter.getCurrentConfiguration().getId(), configuration.getId());
    }
}
