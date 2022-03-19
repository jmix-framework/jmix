/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Component.Focusable;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

/**
 * Class that provides fluent interface for removing entity instances. <br>
 * Inject the class into your screen controller and use {@link #builder(Class, FrameOwner)} method as an entry point.
 */
@Component("ui_RemoveOperation")
public class RemoveOperation {

    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Metadata metadata;

    /**
     * Creates a remove builder.
     *
     * @param entityClass entity class
     * @param origin      invoking screen
     * @param <E>         type of entity
     */
    public <E> RemoveBuilder<E> builder(Class<E> entityClass, FrameOwner origin) {
        checkNotNullArgument(entityClass);
        checkNotNullArgument(origin);

        return new RemoveBuilder<>(origin, entityClass, this::triggerAction);
    }

    /**
     * Creates a remove builder using list component, e.g. {@link Table} or {@link DataGrid}.
     *
     * @param listComponent list component
     * @param <E>           type of entity
     */
    public <E> RemoveBuilder<E> builder(ListComponent<E> listComponent) {
        checkNotNullArgument(listComponent);
        checkNotNullArgument(listComponent.getFrame());

        FrameOwner frameOwner = listComponent.getFrame().getFrameOwner();
        Class<E> entityClass;
        DataUnit items = listComponent.getItems();
        if (items instanceof ContainerDataUnit) {
            entityClass = ((ContainerDataUnit) items).getEntityMetaClass().getJavaClass();
        } else {
            throw new IllegalStateException(String.format("Component %s is not bound to data", listComponent));
        }

        return builder(entityClass, frameOwner)
                .withListComponent(listComponent);
    }

    /**
     * Removes selected items from the list component with confirmation dialog. <br>
     * After confirmation removes items from DB if the bound container is not nested.
     *
     * @param target list component
     * @param <E>    entity type
     */
    public <E> void removeSelected(ListComponent<E> target) {
        builder(target)
                .withConfirmation(true)
                .remove();
    }

    /**
     * Excludes selected items from the list component without confirmation. Works with nested containers only.
     *
     * @param target list component
     * @param <E>    entity type
     */
    public <E> void excludeSelected(ListComponent<E> target) {
        builder(target)
                .withConfirmation(false)
                .exclude();
    }

    protected <E> void triggerAction(RemoveBuilder<E> builder) {
        List<E> selectedItems = Collections.emptyList();
        if (builder.getItems() != null) {
            selectedItems = builder.getItems();
        } else if (builder.getListComponent() != null) {
            selectedItems = new ArrayList<>(builder.getListComponent().getSelected());
        }

        if (!selectedItems.isEmpty()) {
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

        if (builder.getAfterActionPerformedHandler() != null) {
            AfterActionPerformedEvent<E> event = new AfterActionPerformedEvent<>(builder.origin, selectedItems);
            builder.getAfterActionPerformedHandler().accept(event);
        }
    }

    protected <E> void removeItems(RemoveBuilder<E> builder, List<E> selectedItems) {
        FrameOwner origin = builder.getOrigin();
        ScreenData screenData = UiControllerUtils.getScreenData(origin);

        CollectionContainer<E> container = getCollectionContainer(builder);

        commitIfNeeded(selectedItems, container, screenData);

        if (selectedItems.size() == 1) {
            container.getMutableItems().remove(selectedItems.get(0));
        } else {
            container.getMutableItems().removeAll(selectedItems);
        }

        focusListComponent(builder);
    }

    protected <E> void focusListComponent(RemoveBuilder<E> builder) {
        if (builder.getListComponent() instanceof Focusable) {
            ((Focusable) builder.getListComponent()).focus();
        }
    }

    protected void commitIfNeeded(Collection<?> entitiesToRemove, CollectionContainer container,
                                  ScreenData screenData) {

        List<?> entitiesToCommit = entitiesToRemove.stream()
                .filter(entity -> !entityStates.isNew(entity))
                .collect(Collectors.toList());

        boolean needCommit = !entitiesToCommit.isEmpty();
        if (container instanceof Nested) {
            InstanceContainer masterContainer = ((Nested) container).getMaster();
            String property = ((Nested) container).getProperty();

            MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(property);

            needCommit = needCommit && (metaProperty.getType() != MetaProperty.Type.COMPOSITION);
        }

        DataContext dataContext = screenData.getDataContextOrNull();
        if (needCommit) {
            SaveContext saveContext = new SaveContext();
            for (Object entity : entitiesToCommit) {
                saveContext.removing(entity);
            }
            dataManager.save(saveContext);
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

    @SuppressWarnings("unchecked")
    protected <E> void excludeItems(RemoveBuilder<E> builder, List<E> selectedItems) {
        CollectionContainer<E> container = getCollectionContainer(builder);

        if (!(container instanceof Nested)) {
            throw new IllegalArgumentException("Exclude action supports only Nested containers");
        }

        InstanceContainer masterDc = ((Nested) container).getMaster();

        String property = ((Nested) container).getProperty();
        Object masterItem = masterDc.getItem();

        MetaProperty metaProperty = metadata.getClass(masterItem).getProperty(property);
        MetaProperty inverseMetaProperty = metaProperty.getInverse();

        if (inverseMetaProperty != null
                && !inverseMetaProperty.getRange().getCardinality().isMany()) {

            Class inversePropClass = extendedEntities.getEffectiveClass(inverseMetaProperty.getDomain());
            Class dcClass = extendedEntities.getEffectiveClass(container.getEntityMetaClass());

            if (inversePropClass.isAssignableFrom(dcClass)) {
                // update reference for One-To-Many
                for (Object item : selectedItems) {
                    EntityValues.setValue(item, inverseMetaProperty.getName(), null);
                }
            }
        }

        container.getMutableItems().removeAll(selectedItems);

        focusListComponent(builder);
    }

    @SuppressWarnings("unchecked")
    protected <E> CollectionContainer<E> getCollectionContainer(RemoveBuilder<E> builder) {
        CollectionContainer<E> container;
        if (builder.getContainer() != null) {
            container = builder.getContainer();
        } else if (builder.getListComponent() != null) {
            DataUnit items = builder.getListComponent().getItems();
            container = ((ContainerDataUnit) items).getContainer();
        } else {
            throw new IllegalArgumentException("Neither container nor list component is specified");
        }
        return container;
    }

    @SuppressWarnings("CodeBlock2Expr")
    protected <E> void performActionWithConfirmation(RemoveBuilder<E> builder, List<E> selectedItems) {
        ScreenContext screenContext = getScreenContext(builder.getOrigin());

        Dialogs dialogs = screenContext.getDialogs();

        String title = builder.getConfirmationTitle() != null ?
                builder.getConfirmationTitle() : messages.getMessage("dialogs.Confirmation");

        String message = builder.getConfirmationMessage() != null ?
                builder.getConfirmationMessage() : messages.getMessage("dialogs.Confirmation.Remove");

        dialogs.createOptionDialog()
                .withCaption(title)
                .withMessage(message)
                .withActions(
                        new DialogAction(DialogAction.Type.YES).withHandler(e -> {

                            performAction(builder, selectedItems);
                        }),
                        new DialogAction(DialogAction.Type.NO).withHandler(e -> {
                            focusListComponent(builder);

                            if (builder.getActionCancelledHandler() != null) {
                                ActionCancelledEvent<E> event =
                                        new ActionCancelledEvent<>(builder.getOrigin(), selectedItems);
                                builder.getActionCancelledHandler().accept(event);
                            }
                        })
                )
                .show();
    }

    /**
     * Remove builder.
     *
     * @param <E> entity type
     */
    public static class RemoveBuilder<E> {

        protected final FrameOwner origin;
        protected final Class<E> entityClass;
        protected final Consumer<RemoveBuilder<E>> handler;

        protected Operation operation;

        protected ListComponent<E> listComponent;
        protected CollectionContainer<E> container;
        protected List<E> items;

        protected boolean confirmation = true;
        protected String confirmationMessage;
        protected String confirmationTitle;

        protected Consumer<BeforeActionPerformedEvent<E>> beforeActionPerformedHandler;
        protected Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler;
        protected Consumer<ActionCancelledEvent<E>> actionCancelledHandler;

        public RemoveBuilder(FrameOwner origin, Class<E> entityClass, Consumer<RemoveBuilder<E>> actionHandler) {
            this.origin = origin;
            this.entityClass = entityClass;
            this.handler = actionHandler;
        }

        public RemoveBuilder<E> withListComponent(ListComponent<E> listComponent) {
            this.listComponent = listComponent;
            return this;
        }

        public RemoveBuilder<E> withContainer(CollectionContainer<E> container) {
            this.container = container;
            return this;
        }

        public RemoveBuilder<E> withItems(List<E> items) {
            this.items = items;
            return this;
        }

        /**
         * Sets whether to ask confirmation from the user.
         */
        public RemoveBuilder<E> withConfirmation(boolean confirmation) {
            this.confirmation = confirmation;
            return this;
        }

        /**
         * Sets confirmation dialog message.
         */
        public RemoveBuilder<E> withConfirmationMessage(String confirmationMessage) {
            this.confirmationMessage = confirmationMessage;
            return this;
        }

        /**
         * Sets confirmation dialog title.
         */
        public RemoveBuilder<E> withConfirmationTitle(String confirmationTitle) {
            this.confirmationTitle = confirmationTitle;
            return this;
        }

        public RemoveBuilder<E> beforeActionPerformed(Consumer<BeforeActionPerformedEvent<E>> handler) {
            this.beforeActionPerformedHandler = handler;
            return this;
        }

        public RemoveBuilder<E> afterActionPerformed(Consumer<AfterActionPerformedEvent<E>> handler) {
            this.afterActionPerformedHandler = handler;
            return this;
        }

        public RemoveBuilder<E> onCancel(Consumer<ActionCancelledEvent<E>> handler) {
            this.actionCancelledHandler = handler;
            return this;
        }

        @Nullable
        public ListComponent<E> getListComponent() {
            return listComponent;
        }

        @Nullable
        public CollectionContainer<E> getContainer() {
            return container;
        }

        @Nullable
        public List<E> getItems() {
            return items;
        }

        @Nullable
        public String getConfirmationTitle() {
            return confirmationTitle;
        }

        @Nullable
        public String getConfirmationMessage() {
            return confirmationMessage;
        }

        public boolean isConfirmationRequired() {
            return confirmation;
        }

        public FrameOwner getOrigin() {
            return origin;
        }

        public Class<E> getEntityClass() {
            return entityClass;
        }

        public Operation getOperation() {
            return operation;
        }

        @Nullable
        public Consumer<BeforeActionPerformedEvent<E>> getBeforeActionPerformedHandler() {
            return beforeActionPerformedHandler;
        }

        @Nullable
        public Consumer<AfterActionPerformedEvent<E>> getAfterActionPerformedHandler() {
            return afterActionPerformedHandler;
        }

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

    protected enum Operation {
        REMOVE,
        EXCLUDE
    }

    /**
     * Event sent before selected entities are removed.
     */
    public static class BeforeActionPerformedEvent<E> extends EventObject {
        protected final List<E> items;
        protected boolean actionPrevented = false;

        public BeforeActionPerformedEvent(FrameOwner origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public FrameOwner getSource() {
            return (FrameOwner) super.getSource();
        }

        public FrameOwner getScreen() {
            return (FrameOwner) super.getSource();
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

        public AfterActionPerformedEvent(FrameOwner origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public FrameOwner getSource() {
            return (FrameOwner) super.getSource();
        }

        public FrameOwner getScreen() {
            return (FrameOwner) super.getSource();
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

        public ActionCancelledEvent(FrameOwner origin, List<E> items) {
            super(origin);

            this.items = items;
        }

        @Override
        public FrameOwner getSource() {
            return (FrameOwner) super.getSource();
        }

        public FrameOwner getScreen() {
            return (FrameOwner) super.getSource();
        }

        /**
         * @return the list of entities selected for removal
         */
        public List<E> getItems() {
            return items;
        }
    }
}
