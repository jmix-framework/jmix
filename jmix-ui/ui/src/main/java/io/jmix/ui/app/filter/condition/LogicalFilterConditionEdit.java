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

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.groupfilter.LogicalFilterSupport;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public abstract class LogicalFilterConditionEdit<E extends LogicalFilterCondition> extends FilterConditionEdit<E> {

    @Autowired
    protected Messages messages;
    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected LogicalFilterSupport logicalFilterSupport;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;

    public abstract CollectionContainer<FilterCondition> getCollectionContainer();

    @Nullable
    public abstract FilterAddConditionAction getAddAction();

    @Nullable
    public abstract EditAction<FilterCondition> getEditAction();

    @Nullable
    public abstract RemoveAction<FilterCondition> getRemoveAction();

    @Nullable
    public abstract ListComponent<FilterCondition> getListComponent();

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
            addAction.setFilter(currentConfiguration.getOwner());
            addAction.refreshState();
            addAction.setSelectHandler(this::addActionSelectHandler);
        }
    }

    protected void addActionSelectHandler(Collection<FilterCondition> selectedConditions) {
        if (getListComponent() != null) {
            if (!selectedConditions.isEmpty()) {
                for (FilterCondition selectedCondition : selectedConditions) {
                    updatePropertyConditionLocalizedCaption(selectedCondition);

                    LogicalFilterCondition parent;
                    FilterCondition singleSelected = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                            getEditedEntity(), getListComponent().getSingleSelected());
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

    protected void updatePropertyConditionLocalizedCaption(FilterCondition condition) {
        if (condition instanceof PropertyFilterCondition
                && Strings.isNullOrEmpty(condition.getCaption())) {
            PropertyFilterCondition propertyFilterCondition = (PropertyFilterCondition) condition;

            MetaClass metaClass = currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass();
            String property = propertyFilterCondition.getProperty();
            PropertyFilter.Operation operation = propertyFilterCondition.getOperation();
            boolean operationCaptionVisible = propertyFilterCondition.getOperationCaptionVisible()
                    && !propertyFilterCondition.getOperationEditable();

            String localizedCaption = propertyFilterSupport.getPropertyFilterCaption(metaClass, property, operation,
                    operationCaptionVisible);
            propertyFilterCondition.setLocalizedCaption(localizedCaption);
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
        if (screen instanceof FilterConditionEdit) {
            ((FilterConditionEdit<?>) screen).setCurrentConfiguration(currentConfiguration);
        }
    }

    protected void editActionAfterCloseHandler(AfterCloseEvent afterCloseEvent) {
        Screen screen = afterCloseEvent.getSource();
        if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)
                && screen instanceof EditorScreen
                && getListComponent() != null) {

            FilterCondition singleSelected = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                    getEditedEntity(), getListComponent().getSingleSelected());

            if (singleSelected != null && singleSelected.getParent() instanceof LogicalFilterCondition) {
                LogicalFilterCondition parent = (LogicalFilterCondition) singleSelected.getParent();
                int index = parent.getOwnFilterConditions().indexOf(singleSelected);

                FilterCondition editedCondition = (FilterCondition) ((EditorScreen<?>) screen).getEditedEntity();
                parent.getOwnFilterConditions().set(index, editedCondition);
                editedCondition.setParent(parent);

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
            FilterCondition removedCondition = logicalFilterSupport.findSelectedConditionFromRootFilterCondition(
                    getEditedEntity(), filterCondition);
            if (removedCondition != null) {
                FilterCondition parent = removedCondition.getParent();
                if (parent instanceof LogicalFilterCondition) {
                    ((LogicalFilterCondition) parent).getOwnFilterConditions().remove(filterCondition);
                }
                removedCondition.setParent(null);
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
        Filter filter = currentConfiguration.getOwner();
        return filter.getConfiguration(currentConfiguration.getId()) != null
                && !Objects.equals(filter.getCurrentConfiguration().getId(), currentConfiguration.getId());
    }

    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        String caption = getEditedEntity().getCaption();
        String localizedCaption;
        if (!Strings.isNullOrEmpty(caption)) {
            localizedCaption = caption;
        } else {
            localizedCaption = logicalFilterSupport.getOperationCaption(getEditedEntity().getOperation(),
                    getEditedEntity().getOperationCaptionVisible());
        }
        getEditedEntity().setLocalizedCaption(localizedCaption);
    }
}
