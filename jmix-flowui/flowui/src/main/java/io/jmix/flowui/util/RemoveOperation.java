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

package io.jmix.flowui.util;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Class that provides fluent interface for removing entity instances. <br>
 * Inject the class into your view controller and use {@link #builder(Class, View)} method as an entry point.
 */
@Component("flowui_RemoveOperation")
public class RemoveOperation {

    protected DataManager dataManager;
    protected Messages messages;
    protected ExtendedEntities extendedEntities;
    protected EntityStates entityStates;
    protected Metadata metadata;

    @SuppressWarnings("rawtypes")
    protected Consumer removeDelegate;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setExtendedEntities(ExtendedEntities extendedEntities) {
        this.extendedEntities = extendedEntities;
    }

    @Autowired
    public void setEntityStates(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a remove builder.
     *
     * @param entityClass entity class
     * @param origin      invoking view
     * @param <E>         type of entity
     */
    public <E> RemoveBuilder<E> builder(Class<E> entityClass, View<?> origin) {
        checkNotNullArgument(entityClass);
        checkNotNullArgument(origin);

        return new RemoveBuilder<>(origin, entityClass, this::triggerAction);
    }

    /**
     * Creates a remove builder using {@link ListDataComponent} component.
     *
     * @param listDataComponent list data component
     * @param <E>               type of entity
     */
    public <E> RemoveBuilder<E> builder(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);
        if (!(listDataComponent instanceof com.vaadin.flow.component.Component)) {
            throw new IllegalArgumentException("ListDataComponent must extend the Component class");
        }

        View<?> view = UiComponentUtils.findView((com.vaadin.flow.component.Component) listDataComponent);
        Class<E> entityClass;
        DataUnit items = listDataComponent.getItems();
        if (items instanceof ContainerDataUnit) {
            entityClass = ((ContainerDataUnit<?>) items).getEntityMetaClass().getJavaClass();
        } else {
            throw new IllegalStateException(String.format("Component %s is not bound to data", listDataComponent));
        }

        return builder(entityClass, view)
                .withListDataComponent(listDataComponent);
    }

    /**
     * Removes selected items from the {@link ListDataComponent} component with confirmation dialog. <br>
     * After confirmation removes items from DB if the bound container is not nested.
     *
     * @param listDataComponent list data component
     * @param <E>               entity type
     */
    public <E> void removeSelected(ListDataComponent<E> listDataComponent) {
        builder(listDataComponent)
                .withConfirmation(true)
                .remove();
    }

    /**
     * Excludes selected items from the {@link ListDataComponent} component without confirmation. Works with nested containers only.
     *
     * @param listDataComponent list data component
     * @param <E>               entity type
     */
    public <E> void excludeSelected(ListDataComponent<E> listDataComponent) {
        builder(listDataComponent)
                .withConfirmation(false)
                .exclude();
    }

    protected <E> void triggerAction(RemoveBuilder<E> builder) {
        List<E> selectedItems = Collections.emptyList();
        if (builder.getItems() != null) {
            selectedItems = builder.getItems();
        } else if (builder.getListDataComponent() != null) {
            selectedItems = new ArrayList<>(builder.getListDataComponent().getSelectedItems());
        }

        if (!selectedItems.isEmpty()) {
            removeDelegate = builder.removeDelegate;
            if (builder.isConfirmationRequired()) {
                performActionWithConfirmation(builder, selectedItems);
            } else {
                performAction(builder, selectedItems);
            }
        }
    }

    protected <E> void performAction(RemoveBuilder<E> builder, List<E> selectedItems) {
        if (builder.getBeforeActionPerformedHandler() != null) {
            BeforeActionPerformedEvent<E> event = new BeforeActionPerformedEvent<>(builder.getOrigin(), selectedItems);
            builder.getBeforeActionPerformedHandler().accept(event);

            if (event.isActionPrevented()) {
                // do not perform action
                return;
            }
        }

        if (builder.getOperation() == Operation.EXCLUDE) {
            excludeItems(builder, selectedItems);
        } else {
            removeItems(builder, selectedItems);
        }

        if (builder.getListDataComponent() != null) {
            selectedItems.forEach(item -> builder.getListDataComponent().deselect(item));
        }

        if (builder.getAfterActionPerformedHandler() != null) {
            AfterActionPerformedEvent<E> event = new AfterActionPerformedEvent<>(builder.origin, selectedItems);
            builder.getAfterActionPerformedHandler().accept(event);
        }
    }

    protected <E> void removeItems(RemoveBuilder<E> builder, List<E> selectedItems) {
        View<?> origin = builder.getOrigin();
        ViewData viewData = ViewControllerUtils.getViewData(origin);

        CollectionContainer<E> container = getCollectionContainer(builder);

        saveIfNeeded(selectedItems, container, viewData);

        if (selectedItems.size() == 1) {
            container.getMutableItems().remove(selectedItems.get(0));
        } else {
            container.getMutableItems().removeAll(selectedItems);
        }

        focusListDataComponent(builder);
    }

    protected <E> void focusListDataComponent(RemoveBuilder<E> builder) {
        if (builder.getListDataComponent() instanceof Focusable) {
            ((Focusable<?>) builder.getListDataComponent()).focus();
        }
    }

    protected void saveIfNeeded(Collection<?> entitiesToRemove,
                                CollectionContainer<?> container, ViewData viewData) {

        List<?> entitiesToSave = entitiesToRemove.stream()
                .filter(entity -> !entityStates.isNew(entity))
                .collect(Collectors.toList());

        boolean needSave = !entitiesToSave.isEmpty();
        if (container instanceof Nested) {
            InstanceContainer<?> masterContainer = ((Nested) container).getMaster();
            String property = ((Nested) container).getProperty();

            MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(property);

            needSave = needSave && (metaProperty.getType() != MetaProperty.Type.COMPOSITION);
        }

        DataContext dataContext = viewData.getDataContextOrNull();
        if (needSave) {
            if (removeDelegate == null) {
                SaveContext saveContext = new SaveContext();
                for (Object entity : entitiesToSave) {
                    saveContext.removing(entity);
                }
                dataManager.save(saveContext);
            } else {
                //noinspection unchecked
                removeDelegate.accept(entitiesToSave);
            }
            for (Object entity : entitiesToRemove) {
                if (dataContext != null) {
                    dataContext.evict(entity);
                }
            }
        } else {
            for (Object entity : entitiesToRemove) {
                if (dataContext != null) {
                    dataContext.remove(entity);
                }
            }
        }
    }

    protected <E> void excludeItems(RemoveBuilder<E> builder, List<E> selectedItems) {
        CollectionContainer<E> container = getCollectionContainer(builder);

        if (!(container instanceof Nested)) {
            throw new IllegalArgumentException("Exclude action supports only Nested containers");
        }

        InstanceContainer<?> masterDc = ((Nested) container).getMaster();

        String property = ((Nested) container).getProperty();
        Object masterItem = masterDc.getItem();

        MetaProperty metaProperty = metadata.getClass(masterItem).getProperty(property);
        MetaProperty inverseMetaProperty = metaProperty.getInverse();

        if (inverseMetaProperty != null
                && !inverseMetaProperty.getRange().getCardinality().isMany()) {

            Class<?> inversePropClass = extendedEntities.getEffectiveClass(inverseMetaProperty.getDomain());
            Class<?> dcClass = extendedEntities.getEffectiveClass(container.getEntityMetaClass());

            if (inversePropClass.isAssignableFrom(dcClass)) {
                // update reference for One-To-Many
                for (Object item : selectedItems) {
                    EntityValues.setValue(item, inverseMetaProperty.getName(), null);
                }
            }
        }

        container.getMutableItems().removeAll(selectedItems);

        focusListDataComponent(builder);
    }

    @SuppressWarnings("unchecked")
    protected <E> CollectionContainer<E> getCollectionContainer(RemoveBuilder<E> builder) {
        CollectionContainer<E> container;
        if (builder.getContainer() != null) {
            container = builder.getContainer();
        } else if (builder.getListDataComponent() != null) {
            DataUnit items = builder.getListDataComponent().getItems();
            container = ((ContainerDataUnit<E>) items).getContainer();
        } else {
            throw new IllegalArgumentException(String.format("Neither container nor %s is specified",
                    ListDataComponent.class.getSimpleName()));
        }
        return container;
    }

    protected <E> void performActionWithConfirmation(RemoveBuilder<E> builder, List<E> selectedItems) {
        builder.getOrigin().getUI().ifPresent(ui -> {
                    Dialogs dialogs = Instantiator.get(ui).getOrCreate(Dialogs.class);

                    String header = builder.getConfirmationTitle() != null ?
                            builder.getConfirmationTitle() : messages.getMessage("dialogs.Confirmation");

                    String text = builder.getConfirmationMessage() != null ?
                            builder.getConfirmationMessage() : messages.getMessage("dialogs.Confirmation.Remove");

                    dialogs.createOptionDialog()
                            .withHeader(header)
                            .withText(text)
                            .withActions(
                                    new DialogAction(DialogAction.Type.YES)
                                            .withHandler(e -> performAction(builder, selectedItems))
                                            .withVariant(ActionVariant.PRIMARY),
                                    new DialogAction(DialogAction.Type.NO).withHandler(e -> {
                                        focusListDataComponent(builder);

                                        if (builder.getActionCancelledHandler() != null) {
                                            ActionCancelledEvent<E> event =
                                                    new ActionCancelledEvent<>(builder.getOrigin(), selectedItems);
                                            builder.getActionCancelledHandler().accept(event);
                                        }
                                    })
                            )
                            .open();
                }
        );
    }

    /**
     * Remove builder.
     *
     * @param <E> entity type
     */
    public static class RemoveBuilder<E> {

        protected final View<?> origin;
        protected final Class<E> entityClass;
        protected final Consumer<RemoveBuilder<E>> handler;

        protected Operation operation;

        protected ListDataComponent<E> listDataComponent;
        protected CollectionContainer<E> container;
        protected List<E> items;

        protected boolean confirmation = true;
        protected String confirmationMessage;
        protected String confirmationTitle;

        protected Consumer<BeforeActionPerformedEvent<E>> beforeActionPerformedHandler;
        protected Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler;
        protected Consumer<ActionCancelledEvent<E>> actionCancelledHandler;
        protected Consumer<Collection<E>> removeDelegate;

        public RemoveBuilder(View<?> origin, Class<E> entityClass, Consumer<RemoveBuilder<E>> actionHandler) {
            this.origin = origin;
            this.entityClass = entityClass;
            this.handler = actionHandler;
        }

        /**
         * Sets the {@link ListDataComponent} to be used by the builder.
         *
         * @param listDataComponent the {@link ListDataComponent} instance to be associated with this builder
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withListDataComponent(ListDataComponent<E> listDataComponent) {
            this.listDataComponent = listDataComponent;
            return this;
        }

        /**
         * Sets the {@link CollectionContainer} to be used by the builder.
         *
         * @param container the {@link CollectionContainer} instance to be associated with this builder
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withContainer(CollectionContainer<E> container) {
            this.container = container;
            return this;
        }

        /**
         * Sets the items to be removed by the builder.
         *
         * @param items the list of items to be removed
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withItems(List<E> items) {
            this.items = items;
            return this;
        }

        /**
         * Sets whether a confirmation dialog is required before performing the remove operation.
         *
         * @param confirmation {@code true} to enable confirmation dialog, {@code false} to disable it
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withConfirmation(boolean confirmation) {
            this.confirmation = confirmation;
            return this;
        }

        /**
         * Sets the confirmation message to be displayed in the confirmation dialog for the remove operation.
         *
         * @param confirmationMessage the confirmation message to be displayed
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withConfirmationMessage(String confirmationMessage) {
            this.confirmationMessage = confirmationMessage;
            return this;
        }

        /**
         * Sets the confirmation title to be displayed in the confirmation dialog for the remove operation.
         *
         * @param confirmationTitle the confirmation title to be displayed
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withConfirmationTitle(String confirmationTitle) {
            this.confirmationTitle = confirmationTitle;
            return this;
        }

        /**
         * Sets a handler to be invoked before the action is performed.
         *
         * @param handler a handler to set
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> beforeActionPerformed(Consumer<BeforeActionPerformedEvent<E>> handler) {
            this.beforeActionPerformedHandler = handler;
            return this;
        }

        /**
         * Sets a handler to be invoked after the action is performed.
         *
         * @param handler a handler to set
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> afterActionPerformed(Consumer<AfterActionPerformedEvent<E>> handler) {
            this.afterActionPerformedHandler = handler;
            return this;
        }

        /**
         * Sets a handler to be invoked when the remove operation is cancelled by the user in the confirmation dialog.
         *
         * @param handler a handler to set
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> onCancel(Consumer<ActionCancelledEvent<E>> handler) {
            this.actionCancelledHandler = handler;
            return this;
        }

        /**
         * Sets the delegate to be invoked instead of {@link DataManager} to remove the entities from a storage.
         *
         * @param removeDelegate a delegate to set
         * @return the current instance for method chaining
         */
        public RemoveBuilder<E> withRemoveDelegate(Consumer<Collection<E>> removeDelegate) {
            this.removeDelegate = removeDelegate;
            return this;
        }

        /**
         * Returns the {@link ListDataComponent} associated with this builder.
         *
         * @return the {@link ListDataComponent} instance, or {@code null} if not set
         */
        @Nullable
        public ListDataComponent<E> getListDataComponent() {
            return listDataComponent;
        }

        /**
         * Returns the {@link CollectionContainer} associated with this builder.
         *
         * @return the {@link CollectionContainer} instance, or {@code null} if not set
         */
        @Nullable
        public CollectionContainer<E> getContainer() {
            return container;
        }

        /**
         * Returns the list of items to be removed associated with this builder.
         *
         * @return the list of items, or {@code null} if no items are set
         */
        @Nullable
        public List<E> getItems() {
            return items;
        }

        /**
         * Returns the confirmation title to be displayed in the confirmation dialog for the remove operation.
         *
         * @return the confirmation title, or {@code null} if not set
         */
        @Nullable
        public String getConfirmationTitle() {
            return confirmationTitle;
        }

        /**
         * Returns the confirmation message to be displayed in the confirmation dialog for the remove operation.
         *
         * @return the confirmation message, or {@code null} if not set
         */
        @Nullable
        public String getConfirmationMessage() {
            return confirmationMessage;
        }

        /**
         * Returns whether a confirmation dialog is required before performing the remove operation.
         *
         * @return {@code true} if a confirmation dialog is required; {@code false} otherwise
         */
        public boolean isConfirmationRequired() {
            return confirmation;
        }

        /**
         * Returns the {@link View} associated with this builder.
         *
         * @return the {@link View} instance
         */
        public View<?> getOrigin() {
            return origin;
        }

        /**
         * Returns the class of the entity associated with this builder.
         *
         * @return the {@link Class} of the entity
         */
        public Class<E> getEntityClass() {
            return entityClass;
        }

        /**
         * Returns the {@link Operation} associated with this builder.
         *
         * @return the {@link Operation} instance
         */
        public Operation getOperation() {
            return operation;
        }

        /**
         * Returns the handler to be invoked before the action is performed.
         *
         * @return the handler to be invoked before the action is performed,
         * or {@code null} if no handler is set
         */
        @Nullable
        public Consumer<BeforeActionPerformedEvent<E>> getBeforeActionPerformedHandler() {
            return beforeActionPerformedHandler;
        }

        /**
         * Returns the handler to be invoked after the action is performed.
         *
         * @return the handler to be invoked after the action is performed,
         * or {@code null} if no handler is set
         */
        @Nullable
        public Consumer<AfterActionPerformedEvent<E>> getAfterActionPerformedHandler() {
            return afterActionPerformedHandler;
        }

        /**
         * Returns the handler to be invoked when the remove operation is cancelled
         * by the user in the confirmation dialog.
         *
         * @return the handler to be invoked upon removal cancellation,
         * or {@code null} if no handler is set
         */
        @Nullable
        public Consumer<ActionCancelledEvent<E>> getActionCancelledHandler() {
            return actionCancelledHandler;
        }

        /**
         * Excludes items from relation: One-To-Many or Many-To-Many.
         */
        public void exclude() {
            this.operation = Operation.EXCLUDE;

            this.handler.accept(this);
        }

        /**
         * Removes items.
         */
        public void remove() {
            this.operation = Operation.REMOVE;

            this.handler.accept(this);
        }
    }

    /**
     * Represents the possible operations that can be performed by {@link RemoveOperation}.
     */
    public enum Operation {

        /**
         * Removes selected items from from the database.
         */
        REMOVE,

        /**
         * Excludes selected items from relation: One-To-Many or Many-To-Many
         */
        EXCLUDE
    }

    /**
     * Event sent before selected entities are removed.
     */
    public static class BeforeActionPerformedEvent<E> extends EventObject {

        protected final List<E> items;
        protected boolean actionPrevented = false;

        public BeforeActionPerformedEvent(View<?> origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public View<?> getSource() {
            return (View<?>) super.getSource();
        }

        /**
         * @return the list of entities selected for removal
         */
        public List<E> getItems() {
            return items;
        }

        /**
         * @return whether the removal was prevented by invoking {@link #preventAction()}
         */
        public boolean isActionPrevented() {
            return actionPrevented;
        }

        /**
         * Prevents the removal.
         */
        public void preventAction() {
            this.actionPrevented = true;
        }
    }

    /**
     * Event sent after selected entities are removed.
     */
    public static class AfterActionPerformedEvent<E> extends EventObject {

        protected final List<E> items;

        public AfterActionPerformedEvent(View<?> origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public View<?> getSource() {
            return (View<?>) super.getSource();
        }

        /**
         * @return the list of entities selected for removal
         */
        public List<E> getItems() {
            return items;
        }
    }

    /**
     * Event sent when the remove operation is cancelled by user in the confirmation dialog.
     */
    public static class ActionCancelledEvent<E> extends EventObject {

        protected final List<E> items;

        public ActionCancelledEvent(View<?> origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public View<?> getSource() {
            return (View<?>) super.getSource();
        }

        /**
         * @return the list of entities selected for removal
         */
        public List<E> getItems() {
            return items;
        }
    }
}
