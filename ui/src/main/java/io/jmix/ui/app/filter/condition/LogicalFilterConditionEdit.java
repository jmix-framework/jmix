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
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.LookupComponent;
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

    public List<FilterCondition> getFilterConditions() {
        return getCollectionContainer().getMutableItems();
    }

    public void setFilterConditions(List<FilterCondition> filterConditions) {
        getCollectionContainer().setItems(filterConditions);
    }

    public Filter.Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Filter.Configuration configuration) {
        this.configuration = configuration;
    }

    @Subscribe
    protected void onBeforeShowEvent(BeforeShowEvent event) {
        initAddAction();
        initEditAction();
        initRemoveAction();
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

                    List<FilterCondition> items = getCollectionContainer().getMutableItems();
                    items.add(selectedCondition);
                    if (selectedCondition instanceof LogicalFilterCondition) {
                        List<FilterCondition> childrenConditions =
                                logicalFilterSupport.getChildrenConditions((LogicalFilterCondition) selectedCondition);
                        items.addAll(childrenConditions);
                    }

                    refreshParentItems(selectedCondition);
                }
            }
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

            if (getListComponent() instanceof LookupComponent) {
                ((LookupComponent<?>) getListComponent())
                        .setLookupSelectHandler(collection -> getEditAction().execute());
            }
        }
    }

    protected void editActionScreenConfigurer(Screen screen) {
        if (getListComponent() != null) {
            FilterCondition selectedCondition = getListComponent().getSingleSelected();
            if (screen instanceof LogicalFilterConditionEdit
                    && selectedCondition instanceof LogicalFilterCondition) {
                ((LogicalFilterConditionEdit<?>) screen).setConfiguration(configuration);
                ((LogicalFilterConditionEdit<?>) screen).setFilterConditions(
                        getChildrenConditionsFromContainer((LogicalFilterCondition) selectedCondition));
            }
        }
    }

    protected void editActionAfterCloseHandler(AfterCloseEvent afterCloseEvent) {
        Screen screen = afterCloseEvent.getScreen();
        if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)
                && screen instanceof EditorScreen) {
            FilterCondition filterCondition = (FilterCondition) ((EditorScreen<?>) screen).getEditedEntity();
            refreshParentItems(filterCondition);

            if (filterCondition instanceof LogicalFilterCondition
                    && screen instanceof LogicalFilterConditionEdit) {
                List<FilterCondition> mutableItems = getCollectionContainer().getMutableItems();
                List<FilterCondition> editedConditions =
                        getChildrenConditionsFromContainer((LogicalFilterCondition) filterCondition);
                mutableItems.removeAll(editedConditions);
                mutableItems.addAll(((LogicalFilterConditionEdit<?>) screen).getFilterConditions());
            }
        }
    }

    protected void initRemoveAction() {
        RemoveAction<FilterCondition> removeAction = getRemoveAction();
        if (removeAction != null && getListComponent() != null) {
            removeAction.setIcon(null);
            removeAction.setAfterActionPerformedHandler(event ->
                    event.getItems().forEach(this::removeItemFromOwnFilterConditions));
        }
    }

    protected void removeItemFromOwnFilterConditions(FilterCondition filterCondition) {
        FilterCondition parent = filterCondition.getParent();
        if (containerContainsParentCondition(parent)) {
            ((LogicalFilterCondition) parent).getOwnFilterConditions().remove(filterCondition);
        }
    }

    protected List<FilterCondition> getChildrenConditionsFromContainer(LogicalFilterCondition parent) {
        List<FilterCondition> filterConditions = new ArrayList<>();
        getCollectionContainer().getMutableItems().stream()
                .filter(filterCondition -> Objects.equals(filterCondition.getParent(), parent))
                .forEach(filterCondition -> {
                    filterConditions.add(filterCondition);
                    if (filterCondition instanceof LogicalFilterCondition) {
                        filterConditions.addAll(
                                getChildrenConditionsFromContainer((LogicalFilterCondition) filterCondition));
                    }
                });

        return filterConditions;
    }

    @SuppressWarnings("unchecked")
    protected void refreshParentItems(FilterCondition filterCondition) {
        FilterCondition parent = filterCondition.getParent();
        if (containerContainsParentCondition(parent)) {
            int index = ((LogicalFilterCondition) parent).getOwnFilterConditions().indexOf(filterCondition);
            if (index > -1) {
                ((LogicalFilterCondition) parent).getOwnFilterConditions().set(index, filterCondition);

                if (getInstanceContainer().getItem().equals(parent)) {
                    getInstanceContainer().setItem((E) parent);
                } else {
                    getCollectionContainer().replaceItem(parent);
                    refreshParentItems(parent);
                }
            }
        }
    }

    protected boolean containerContainsParentCondition(FilterCondition parent) {
        return parent instanceof LogicalFilterCondition
                && (getInstanceContainer().getItem().equals(parent)
                || getCollectionContainer().containsItem(parent));
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
