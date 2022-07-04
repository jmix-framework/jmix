package io.jmix.flowui.util;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
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
     * @param <E>  type of entity
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
     * @param <E>    entity type
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
     * @param <E>    entity type
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
        View<?> origin = builder.getOrigin();
        ViewData viewData = UiControllerUtils.getViewData(origin);

        CollectionContainer<E> container = getCollectionContainer(builder);

        commitIfNeeded(selectedItems, container, viewData);

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

    protected void commitIfNeeded(Collection<?> entitiesToRemove, CollectionContainer container,
                                  ViewData viewData) {

        List<?> entitiesToCommit = entitiesToRemove.stream()
                .filter(entity -> !entityStates.isNew(entity))
                .collect(Collectors.toList());

        boolean needCommit = !entitiesToCommit.isEmpty();
        if (container instanceof Nested) {
            InstanceContainer<?> masterContainer = ((Nested) container).getMaster();
            String property = ((Nested) container).getProperty();

            MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(property);

            needCommit = needCommit && (metaProperty.getType() != MetaProperty.Type.COMPOSITION);
        }

        DataContext dataContext = viewData.getDataContextOrNull();
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

        public RemoveBuilder(View<?> origin, Class<E> entityClass, Consumer<RemoveBuilder<E>> actionHandler) {
            this.origin = origin;
            this.entityClass = entityClass;
            this.handler = actionHandler;
        }

        public RemoveBuilder<E> withListDataComponent(ListDataComponent<E> listDataComponent) {
            this.listDataComponent = listDataComponent;
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
        public ListDataComponent<E> getListDataComponent() {
            return listDataComponent;
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

        public View<?> getOrigin() {
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
