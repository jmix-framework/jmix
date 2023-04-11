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
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.action.logicalfilter.LogicalFilterEditAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.logicalfilter.LogicalFilterSupport;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class LogicalFilterConditionDetailView<E extends LogicalFilterCondition>
        extends FilterConditionDetailView<E> {

    @Autowired
    protected Messages messages;
    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected LogicalFilterSupport logicalFilterSupport;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;

    public abstract CollectionContainer<FilterCondition> getCollectionContainer();

    @Nullable
    public abstract GenericFilterAddConditionAction getAddAction();

    @Nullable
    public abstract LogicalFilterEditAction getEditAction();

    @Nullable
    public abstract RemoveAction<FilterCondition> getRemoveAction();

    @Nullable
    public abstract ListDataComponent<FilterCondition> getListDataComponent();

    protected FilterCondition currentParent;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        prepareEditedEntity();
        refreshChildrenConditions();
        initAddAction();
        initEditAction();
        initRemoveAction();
    }

    protected void prepareEditedEntity() {
        String localizedLabel = getEditedEntity().getOperation() == null
                ? messages.getMessage(getClass(), "logicalFilterConditionDetailView.emptyOperationTreeLabel")
                : getEditedEntity().getOperation().getId();

        getEditedEntity().setLocalizedLabel(localizedLabel);

        if (getEditedEntity().getParent() != null) {
            currentParent = getEditedEntity().getParent();
            getEditedEntity().setParent(null);
        }
    }

    protected void refreshChildrenConditions() {
        List<FilterCondition> conditions = logicalFilterSupport.getChildrenConditions(getEditedEntity());

        if (conditions.isEmpty()) {
            getCollectionContainer().setItems(Collections.emptyList());
        } else {
            getCollectionContainer().setItems(conditions);
            getCollectionContainer().getMutableItems().add(getEditedEntity());
        }
    }

    protected void initAddAction() {
        GenericFilterAddConditionAction addConditionAction = getAddAction();

        if (addConditionAction != null && getListDataComponent() != null) {
            addConditionAction.setTarget(currentConfiguration.getOwner());
            addConditionAction.setSelectHandler(this::addActionSelectHandler);
        }
    }

    protected void initEditAction() {
        LogicalFilterEditAction editAction = getEditAction();

        if (editAction != null && getListDataComponent() != null) {
            editAction.setConfiguration(currentConfiguration);
            editAction.addEnabledRule(() -> getListDataComponent().getSingleSelectedItem() != getEditedEntity());
            editAction.setAfterSaveHandler(this::editActionAfterSaveHandler);

            getCollectionContainer().addItemChangeListener(event -> {
                FilterCondition item = event.getItem();

                if (item != null) {
                    editAction.setViewId(filterComponents.getDetailViewId(item.getClass()));
                }
            });
        }
    }

    protected void initRemoveAction() {
        RemoveAction<FilterCondition> removeAction = getRemoveAction();

        if (removeAction != null && getListDataComponent() != null) {
            removeAction.addEnabledRule(() -> getListDataComponent().getSingleSelectedItem() != getEditedEntity());
            removeAction.setAfterActionPerformedHandler(this::afterRemoveActionPerformedHandler);
        }
    }

    protected void addActionSelectHandler(Collection<FilterCondition> selectedConditions) {
        if (getListDataComponent() == null || selectedConditions.isEmpty()) {
            return;
        }

        for (FilterCondition selectedCondition : selectedConditions) {
            updatePropertyConditionLocalizedLabel(selectedCondition);

            LogicalFilterCondition parent;
            FilterCondition singleSelectedCondition = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                    getEditedEntity(), getListDataComponent().getSingleSelectedItem());
            if (singleSelectedCondition != null) {
                if (singleSelectedCondition instanceof LogicalFilterCondition) {
                    parent = (LogicalFilterCondition) singleSelectedCondition;
                } else {
                    parent = (LogicalFilterCondition) singleSelectedCondition.getParent();
                }
            } else {
                parent = getEditedEntity();
            }

            selectedCondition.setParent(parent);
            parent.getOwnFilterConditions().add(selectedCondition);

            refreshChildrenConditions();
            expandItems();
        }
    }

    protected void editActionAfterSaveHandler(FilterCondition editedCondition) {
        if (getListDataComponent() == null) {
            return;
        }

        FilterCondition singleSelected = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                getEditedEntity(), getListDataComponent().getSingleSelectedItem());

        if (singleSelected != null) {
            LogicalFilterCondition parent;

            if (singleSelected.getParent() instanceof LogicalFilterCondition) {
                parent = (LogicalFilterCondition) singleSelected.getParent();
            } else {
                return;
            }

            int index = parent.getOwnFilterConditions().indexOf(singleSelected);

            editedCondition.setParent(parent);
            parent.getOwnFilterConditions().set(index, editedCondition);

            refreshChildrenConditions();
            expandItems();
        }
    }

    protected void afterRemoveActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<FilterCondition> event) {
        event.getItems().forEach(this::doRemoveCondition);

        refreshChildrenConditions();
        expandItems();
    }

    @Subscribe
    protected void onValidation(ValidationEvent event) {
        ValidationErrors errors = performOwnFilterComponentsValidation();
        event.addErrors(errors);
    }

    protected ValidationErrors performOwnFilterComponentsValidation() {
        if (hasEmptyLogicalFilterConditions()) {
            return ValidationErrors.of(messages.getMessage(getClass(),
                    "logicalFilterConditionDetailView.logicalConditionCannotBeEmpty"));
        }

        if (configurationExist()) {
            return ValidationErrors.of(messages.getMessage(getClass(),
                    "logicalFilterConditionDetailView.uniqueConfigurationId"));
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
        GenericFilter filter = currentConfiguration.getOwner();
        return filter.getConfiguration(currentConfiguration.getId()) != null
                && !Objects.equals(filter.getCurrentConfiguration().getId(), currentConfiguration.getId());
    }

    protected void doRemoveCondition(FilterCondition filterCondition) {
        FilterCondition removedCondition = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                getEditedEntity(), filterCondition);

        if (removedCondition != null) {
            FilterCondition parent = removedCondition.getParent();
            if (parent instanceof LogicalFilterCondition) {
                ((LogicalFilterCondition) parent).getOwnFilterConditions().remove(filterCondition);
            }

            removedCondition.setParent(null);
        }
    }

    protected void updatePropertyConditionLocalizedLabel(FilterCondition condition) {
        if (condition instanceof PropertyFilterCondition && Strings.isNullOrEmpty(condition.getLabel())) {
            PropertyFilterCondition propertyFilterCondition = (PropertyFilterCondition) condition;

            MetaClass metaClass = currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass();
            String property = propertyFilterCondition.getProperty();
            PropertyFilter.Operation operation = propertyFilterCondition.getOperation();
            boolean operationTextVisible = propertyFilterCondition.getOperationTextVisible()
                    && !propertyFilterCondition.getOperationEditable();

            String localizedLabel = propertyFilterSupport.getPropertyFilterCaption(metaClass, property, operation,
                    operationTextVisible);
            propertyFilterCondition.setLocalizedLabel(localizedLabel);
        }
    }

    protected void expandItems() {
        if (getListDataComponent() instanceof TreeDataGrid) {
            getListDataComponent().deselectAll();
            ((TreeDataGrid<FilterCondition>) getListDataComponent()).expand(getEditedEntity());
        }
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        if (currentParent != null) {
            getEditedEntity().setParent(currentParent);
        }

        String label = getEditedEntity().getLabel();
        String localizedLabel;

        if (!Strings.isNullOrEmpty(label)) {
            localizedLabel = label;
        } else {
            localizedLabel = logicalFilterSupport.getOperationText(
                    Objects.requireNonNull(getEditedEntity().getOperation()),
                    getEditedEntity().getOperationTextVisible()
            );
        }

        getEditedEntity().setLocalizedLabel(localizedLabel);
    }
}
