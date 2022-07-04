package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.FlowUiViewProperties;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.validation.group.UiCrossFieldChecks;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.navigation.UrlIdSerializer;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class StandardDetailView<T> extends StandardView implements DetailView<T>, ReadOnlyAwareView {

    public static final String NEW_ENTITY_ID = "new";

    private T entityToEdit;
    private String serializedEntityIdToEdit;

    private PessimisticLockStatus entityLockStatus = PessimisticLockStatus.NOT_SUPPORTED;

    private boolean showValidationErrors = true;
    private boolean crossFieldValidationEnabled = true;
    private boolean readOnly = false;

    // whether user has edited entity after view opening
    private boolean modifiedAfterOpen = false;

    private boolean showSaveNotification = true;
    private boolean commitActionPerformed = false;

    public StandardDetailView() {
        addBeforeShowListener(this::onBeforeShow);
        addAfterShowListener(this::onAfterShow);
        addBeforeCloseListener(this::onBeforeClose);
    }

    private void onBeforeShow(BeforeShowEvent event) {
        setupEntityToEdit();
    }

    private void onAfterShow(AfterShowEvent afterShowEvent) {
        setupModifiedTracking();
        setupLock(); // todo rp move to onBeforeShow?
    }

    private void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
        releaseLock();
    }

    private void setupModifiedTracking() {
        DataContext dataContext = getViewData().getDataContextOrNull();
        if (dataContext != null) {
            dataContext.addChangeListener(this::onChangeEvent);
            dataContext.addPostCommitListener(this::onPostCommitEvent);
        }
    }

    private void onChangeEvent(DataContext.ChangeEvent changeEvent) {
        setModifiedAfterOpen(true);
    }

    private void onPostCommitEvent(DataContext.PostCommitEvent postCommitEvent) {
        setModifiedAfterOpen(false);

        if (showSaveNotification) {
            showSaveNotification();
        }
    }

    private void showSaveNotification() {
        getNotifications().create(getSaveNotificationText())
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Position.TOP_END)
                .show();
    }

    private String getSaveNotificationText() {
        Messages messages = getMessages();
        Metadata metadata = getMetadata();
        MessageTools messageTools = getApplicationContext().getBean(MessageTools.class);
        InstanceNameProvider instanceNameProvider = getApplicationContext().getBean(InstanceNameProvider.class);

        MetaClass metaClass = metadata.getClass(getEditedEntity());

        return messages.formatMessage("", "info.EntitySaved",
                messageTools.getEntityCaption(metaClass),
                instanceNameProvider.getInstanceName(getEditedEntity()));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        findEntityId(event);
        super.beforeEnter(event);
    }

    private void findEntityId(BeforeEnterEvent event) {
        serializedEntityIdToEdit = event.getRouteParameters().get("id")
                .orElseThrow(() -> new IllegalStateException("Entity id not found"));
    }

    private OperationResult commitChanges() {
        ValidationErrors validationErrors = validateView();
        if (!validationErrors.isEmpty()) {
            ViewValidation viewValidation = getViewValidation();
            if (showValidationErrors) {
                viewValidation.showValidationErrors(validationErrors);
            }
            viewValidation.focusProblemComponent(validationErrors);

            return OperationResult.fail();
        }

        Runnable standardCommitAction = createStandardCommitAction();

        BeforeCommitChangesEvent beforeEvent = new BeforeCommitChangesEvent(this, standardCommitAction);
        fireEvent(beforeEvent);

        if (beforeEvent.isCommitPrevented()) {
            return beforeEvent.getCommitResult()
                    .orElse(OperationResult.fail());
        }

        standardCommitAction.run();

        return OperationResult.success();
    }

    private ValidationErrors validateView() {
        ValidationErrors validationErrors = validateUiComponents();

        if (!validationErrors.isEmpty()) {
            return validationErrors;
        }

        validationErrors.addAll(validateAdditionalRules());

        return validationErrors;
    }

    private ValidationErrors validateUiComponents() {
        ViewValidation viewValidation = getViewValidation();
        return viewValidation.validateUiComponents(getContent());
    }

    private ValidationErrors validateAdditionalRules() {
        ValidationErrors errors = new ValidationErrors();
        if (isCrossFieldValidationEnabled()) {
            ViewValidation viewValidation = getViewValidation();
            errors.addAll(viewValidation.validateBeanGroup(UiCrossFieldChecks.class, getEditedEntity()));
        }

        ValidationEvent validateEvent = new ValidationEvent(this);
        fireEvent(validateEvent);
        errors.addAll(validateEvent.getErrors());

        return errors;
    }

    private Runnable createStandardCommitAction() {
        return () -> {
            EntitySet committedEntities = getViewData().getDataContext().commit();

            InstanceContainer<T> container = getEditedEntityContainer();
            if (container instanceof HasLoader) {
                DataLoader loader = ((HasLoader) container).getLoader();
                if (loader instanceof InstanceLoader) {
                    //noinspection rawtypes
                    InstanceLoader instanceLoader = (InstanceLoader) loader;
                    if (instanceLoader.getEntityId() == null) {
                        committedEntities.optional(getEditedEntity())
                                .ifPresent(entity ->
                                        instanceLoader.setEntityId(
                                                requireNonNull(EntityValues.getId(entity))
                                        )
                                );
                    }
                }
            }

            fireEvent(new AfterCommitChangesEvent(this));
        };
    }

    protected boolean isCommitActionPerformed() {
        return commitActionPerformed;
    }

    @Override
    public OperationResult commit() {
        return commitChanges()
                .then(() -> commitActionPerformed = true);
    }

    @Override
    public OperationResult closeWithCommit() {
        return commitChanges()
                .compose(() -> close(StandardOutcome.COMMIT));
    }

    @Override
    public OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }

    /**
     * @return whether a notification will be shown in case of successful commit
     */
    public boolean isShowSaveNotification() {
        return showSaveNotification;
    }

    /**
     * Sets whether a notification will be shown in case of successful commit.
     *
     * @param showSaveNotification {@code true} if a notification needs to be shown, {@code false} otherwise
     */
    public void setShowSaveNotification(boolean showSaveNotification) {
        this.showSaveNotification = showSaveNotification;
    }

    /**
     * @return whether to indicate about errors after components validation
     */
    public boolean isShowValidationErrors() {
        return showValidationErrors;
    }

    /**
     * Sets whether to indicate about errors after components validation. The default value is {@code true}.
     *
     * @param showValidationErrors {@code true} if notification needs to be shown, {@code false} otherwise
     */
    public void setShowValidationErrors(boolean showValidationErrors) {
        this.showValidationErrors = showValidationErrors;
    }

    /**
     * @return {@code true} if cross-field validation is enabled
     */
    public boolean isCrossFieldValidationEnabled() {
        return crossFieldValidationEnabled;
    }

    /**
     * Sets whether cross-field validation should be performed before commit changes. It uses {@link UiCrossFieldChecks}
     * constraint group to validate bean instance. The default value is {@code true}.
     *
     * @param crossFieldValidationEnabled {@code true} if cross-field should be enabled, {@code false} otherwise
     */
    public void setCrossFieldValidationEnabled(boolean crossFieldValidationEnabled) {
        this.crossFieldValidationEnabled = crossFieldValidationEnabled;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;

            getReadOnlyViewSupport().setViewReadOnly(this, readOnly);
        }
    }

    private void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) action).isCheckForUnsavedChanges()
                && hasUnsavedChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            boolean useSaveConfirmation = getApplicationContext()
                    .getBean(FlowUiViewProperties.class).isUseSaveConfirmation();
            if (useSaveConfirmation) {
                getViewValidation().showSaveConfirmationDialog(this)
                        .onCommit(() -> result.resume(closeWithCommit()))
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            } else {
                getViewValidation().showUnsavedChangesDialog(this)
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            }

            event.preventWindowClose(result);
        }
    }

    @Override
    public boolean hasUnsavedChanges() {
        if (isReadOnlyDueToLock()) {
            return false;
        }

        DataContext dataContext = getViewData().getDataContext();

        if (!dataContext.getRemoved().isEmpty()) {
            return true;
        }
        for (Object modified : dataContext.getModified()) {
            if (!getEntityStates().isNew(modified)) {
                return true;
            }
        }

        // if only new entities are registered as modified in DataContext,
        // check whether they were modified after opening the view
        return isModifiedAfterOpen();
    }

    protected InstanceContainer<T> getEditedEntityContainer() {
        EditedEntityContainer annotation = getClass().getAnnotation(EditedEntityContainer.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("'%s' does not declare @%s", getClass(), EditedEntityContainer.class.getSimpleName())
            );
        }
        String[] parts = annotation.value().split("\\.");
        ViewData viewData;
        if (parts.length == 1) {
            viewData = getViewData();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Can't obtain edited entity container with id: '%s'", annotation.value()));
        }
        return viewData.getContainer(parts[parts.length - 1]);
    }

    @SuppressWarnings("unchecked")
    protected InstanceLoader<T> getEditedEntityLoader() {
        InstanceContainer<T> container = getEditedEntityContainer();
        DataLoader loader = null;

        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }

        if (loader == null) {
            throw new IllegalStateException("Loader of edited entity container not found");
        }

        if (!(loader instanceof InstanceLoader)) {
            throw new IllegalStateException(String.format(
                    "Loader %s of edited entity container %s must implement %s",
                    loader, container, InstanceLoader.class.getSimpleName()));
        }

        return (InstanceLoader<T>) loader;
    }

    @Override
    public T getEditedEntity() {
        T editedEntity = getEditedEntityContainer().getItemOrNull();
        return editedEntity != null ? editedEntity : entityToEdit;
    }

    @Override
    public void setEntityToEdit(T entity) {
        this.entityToEdit = entity;
        // TODO: gg, why we don't setup 'entity to edit' here?
//        setupEntityToEdit();
    }

    private void setupEntityToEdit() {
        if (serializedEntityIdToEdit != null) {
            setupEntityToEdit(serializedEntityIdToEdit);
        } else if (entityToEdit != null) {
            setupEntityToEdit(entityToEdit);
        } else {
            throw new IllegalStateException("Nether entity nor entity id to edit is defined");
        }
    }

    private void setupEntityToEdit(String serializedEntityId) {
        //noinspection unchecked
        Class<T> entityClass = (Class<T>) DetailViewTypeExtractor.extractEntityClass(getClass())
                .orElseThrow(() ->
                        new IllegalStateException("Failed to determine entity type. " +
                                "Detail class: " + getClass().getName()));

        if (NEW_ENTITY_ID.equals(serializedEntityId)) {
            initNewEntity(entityClass);
        } else {
            initExistingEntity(serializedEntityId);
        }
    }

    private void initNewEntity(Class<T> entityClass) {
        DataContext dataContext = getViewData().getDataContext();

        T newEntity = dataContext.create(entityClass);

        fireEvent(new InitEntityEvent<>(this, newEntity));

        InstanceContainer<T> container = getEditedEntityContainer();
        container.setItem(newEntity);
    }

    private void initExistingEntity(String serializedEntityId) {
        MetaClass entityMetaClass = getEditedEntityContainer().getEntityMetaClass();
        MetaProperty primaryKeyProperty = getMetadataTools().getPrimaryKeyProperty(entityMetaClass);
        if (primaryKeyProperty == null) {
            throw new IllegalStateException(String.format(
                    "Entity %s has no primary key", entityMetaClass.getName()));
        }

        Class<?> idType = primaryKeyProperty.getJavaType();
        Object entityId = UrlIdSerializer.deserializeId(idType, serializedEntityId);
        getEditedEntityLoader().setEntityId(entityId);
    }

    private void setupEntityToEdit(T entityToEdit) {
        DataContext dataContext = getViewData().getDataContext();

        if (getEntityStates().isNew(entityToEdit) || doNotReloadEditedEntity()) {
            T mergedEntity = dataContext.merge(entityToEdit);

            DataContext parentDc = dataContext.getParent();
            if (parentDc == null || !parentDc.contains(mergedEntity)) {
                fireEvent(new InitEntityEvent<>(this, mergedEntity));
            }

            InstanceContainer<T> container = getEditedEntityContainer();
            container.setItem(mergedEntity);
        } else {
            getEditedEntityLoader().setEntityId(requireNonNull(EntityValues.getId(entityToEdit)));
        }
    }

    private boolean doNotReloadEditedEntity() {
        if (isEntityModifiedInParentContext()) {
            InstanceContainer<T> container = getEditedEntityContainer();
            return getEntityStates().isLoadedWithFetchPlan(entityToEdit, container.getFetchPlan());
        }

        return false;
    }

    private boolean isEntityModifiedInParentContext() {
        boolean result = false;
        DataContext parentDc = getViewData().getDataContext().getParent();
        while (!result && parentDc != null) {
            result = isEntityModifiedRecursive(entityToEdit, parentDc, new HashSet<>());
            parentDc = parentDc.getParent();
        }
        return result;
    }

    private boolean isEntityModifiedRecursive(Object entity, DataContext dataContext, HashSet<Object> visited) {
        if (visited.contains(entity)) {
            return false;
        }
        visited.add(entity);

        if (dataContext.isModified(entity) || dataContext.isRemoved(entity))
            return true;

        Metadata metadata = getMetadata();

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (property.getRange().isClass()
                    && getEntityStates().isLoaded(entity, property.getName())) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value == null) {
                    continue;
                }

                if (value instanceof Collection) {
                    for (Object item : ((Collection<?>) value)) {
                        if (isEntityModifiedRecursive(item, dataContext, visited)) {
                            return true;
                        }
                    }
                } else if (isEntityModifiedRecursive(value, dataContext, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void setupLock() {
        T editedEntity = getEditedEntity();
        //noinspection ConstantConditions
        if (editedEntity == null) {
            return;
        }

        Object entityId = EntityValues.getId(editedEntity);

        if (!getEntityStates().isNew(editedEntity) && entityId != null) {

            // todo security
            /*AccessManager accessManager = getApplicationContext().getBean(AccessManager.class);
            MetaClass metaClass = getEditedEntityContainer().getEntityMetaClass();

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);
            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, getApplicationContext());
            accessManager.applyRegisteredConstraints(inMemoryContext);*/

            boolean isPermittedBySecurity = true; /* entityContext.isEditPermitted() && (inMemoryContext.updatePredicate() == null || inMemoryContext.isUpdatePermitted(getEditedEntity())) */
            //noinspection ConstantConditions
            if (isPermittedBySecurity) {
                entityLockStatus = getLockingSupport().lock(entityId);
                if (entityLockStatus == PessimisticLockStatus.FAILED) {
                    setReadOnly(true);
                }
            } else {
                setReadOnly(true);
            }
        }
    }

    private void releaseLock() {
        if (isLocked()) {
            T entity = getEditedEntityContainer().getItemOrNull();
            if (entity != null) {
                Object entityId = requireNonNull(EntityValues.getId(entity));
                getLockingSupport().unlock(entityId);
            }
        }
    }

    @Override
    public PessimisticLockStatus getPessimisticLockStatus() {
        return entityLockStatus;
    }

    /**
     * @return {@code true} if the detail is switched to read-only mode because the entity is locked by another user
     */
    private boolean isReadOnlyDueToLock() {
        return entityLockStatus == PessimisticLockStatus.FAILED;
    }

    /**
     * @return {@code true} if the entity instance has been pessimistically locked when the view is opened
     */
    private boolean isLocked() {
        return entityLockStatus == PessimisticLockStatus.LOCKED;
    }

    private void setModifiedAfterOpen(boolean entityModified) {
        this.modifiedAfterOpen = entityModified;
    }

    /**
     * @return {@code true} if data is modified after view opening
     */
    private boolean isModifiedAfterOpen() {
        return modifiedAfterOpen;
    }

    private EntityStates getEntityStates() {
        return getApplicationContext().getBean(EntityStates.class);
    }

    private Metadata getMetadata() {
        return getApplicationContext().getBean(Metadata.class);
    }

    private MetadataTools getMetadataTools() {
        return getApplicationContext().getBean(MetadataTools.class);
    }

    private Messages getMessages() {
        return getApplicationContext().getBean(Messages.class);
    }

    private ViewValidation getViewValidation() {
        return getApplicationContext().getBean(ViewValidation.class);
    }

    private Notifications getNotifications() {
        return getApplicationContext().getBean(Notifications.class);
    }

    private PessimisticLockSupport getLockingSupport() {
        return getApplicationContext().getBean(PessimisticLockSupport.class, this, getEditedEntityContainer());
    }

    private ReadOnlyViewsSupport getReadOnlyViewSupport() {
        return getApplicationContext().getBean(ReadOnlyViewsSupport.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Registration addInitEntityListener(ComponentEventListener<InitEntityEvent<T>> listener) {
        return getEventBus().addListener(InitEntityEvent.class, ((ComponentEventListener) listener));
    }

    protected Registration addBeforeCommitChangesListener(ComponentEventListener<BeforeCommitChangesEvent> listener) {
        return getEventBus().addListener(BeforeCommitChangesEvent.class, listener);
    }

    protected Registration addAfterCommitChangesListener(ComponentEventListener<AfterCommitChangesEvent> listener) {
        return getEventBus().addListener(AfterCommitChangesEvent.class, listener);
    }

    /**
     * Event sent before the new entity instance is set to edited entity container.
     * <p>
     * Use this event listener to initialize default values in the new entity instance, for example:
     * <pre>
     *     &#64;Subscribe
     *     public void onInitEntity(InitEntityEvent&lt;Foo&gt; event) {
     *         event.getEntity().setStatus(Status.ACTIVE);
     *     }
     * </pre>
     *
     * @param <E> type of entity
     * @see #addInitEntityListener(ComponentEventListener)
     */
//    @TriggerOnce
    public static class InitEntityEvent<E> extends ComponentEvent<View<?>> {

        protected final E entity;

        public InitEntityEvent(View<?> source, E entity) {
            super(source, false);

            this.entity = entity;
        }

        /**
         * @return initializing entity
         */
        public E getEntity() {
            return entity;
        }
    }

    /**
     * Event sent before commit of data context from {@link #commitChanges()} call.
     * <br>
     * Use this event listener to prevent commit and/or show additional dialogs to user before commit, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeCommit(BeforeCommitChangesEvent event) {
     *         if (getEditedEntity().getDescription() == null) {
     *             notifications.create().withCaption("Description required").show();
     *             event.preventCommit();
     *         }
     *     }
     * </pre>
     * <p>
     * Show dialog and resume commit after:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeCommit(BeforeCommitChangesEvent event) {
     *         if (getEditedEntity().getDescription() == null) {
     *             dialogs.createOptionDialog()
     *                     .withCaption("Question")
     *                     .withMessage("Do you want to set default description?")
     *                     .withActions(
     *                             new DialogAction(DialogAction.Type.YES).withHandler(e -&gt; {
     *                                 getEditedEntity().setDescription("No description");
     *
     *                                 // retry commit and resume action
     *                                 event.resume(commitChanges());
     *                             }),
     *                             new DialogAction(DialogAction.Type.NO).withHandler(e -&gt; {
     *                                 // trigger standard commit and resume action
     *                                 event.resume();
     *                             })
     *                     )
     *                     .show();
     *
     *             event.preventCommit();
     *         }
     *     }
     * </pre>
     *
     * @see #addBeforeCommitChangesListener(ComponentEventListener)
     */
    public static class BeforeCommitChangesEvent extends ComponentEvent<View<?>> {

        protected final Runnable resumeAction;

        protected boolean commitPrevented = false;
        protected OperationResult commitResult;

        public BeforeCommitChangesEvent(View<?> source, @Nullable Runnable resumeAction) {
            super(source, false);
            this.resumeAction = resumeAction;
        }

        /**
         * @return data context of the view
         */
        public DataContext getDataContext() {
            return getSource().getViewData().getDataContext();
        }

        /**
         * Prevents commit of the view.
         */
        public void preventCommit() {
            preventCommit(new UnknownOperationResult());
        }

        /**
         * Prevents commit of the view.
         *
         * @param commitResult result object that will be returned from the {@link #commitChanges()}} method
         */
        public void preventCommit(OperationResult commitResult) {
            this.commitPrevented = true;
            this.commitResult = commitResult;
        }

        /**
         * Resume standard execution.
         */
        public void resume() {
            if (resumeAction != null) {
                resumeAction.run();
            }
            if (commitResult instanceof UnknownOperationResult) {
                ((UnknownOperationResult) commitResult).resume(OperationResult.success());
            }
        }

        /**
         * Resume with the passed result ignoring standard execution. The standard commit will not be performed.
         */
        public void resume(OperationResult result) {
            if (commitResult instanceof UnknownOperationResult) {
                ((UnknownOperationResult) commitResult).resume(result);
            }
        }

        /**
         * @return result passed to the {@link #preventCommit(OperationResult)} method
         */
        public Optional<OperationResult> getCommitResult() {
            return Optional.ofNullable(commitResult);
        }

        /**
         * @return whether the commit was prevented by invoking {@link #preventCommit()} method
         */
        public boolean isCommitPrevented() {
            return commitPrevented;
        }
    }

    /**
     * Event sent after commit of data context from {@link #commitChanges()} call.
     * <br>
     * Use this event listener to notify users after commit, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onAfterCommit(AfterCommitChanges event) {
     *         notifications.create().withCaption("Committed").show();
     *     }
     * </pre>
     *
     * @see #addAfterCommitChangesListener(ComponentEventListener)
     */
    public static class AfterCommitChangesEvent extends ComponentEvent<View> {

        public AfterCommitChangesEvent(View source) {
            super(source, false);
        }

        /**
         * @return data context of the view
         */
        public DataContext getDataContext() {
            return getSource().getViewData().getDataContext();
        }
    }

    /**
     * Event sent when view is validated from {@link #validateAdditionalRules()} call.
     * <br>
     * Use this event listener to perform additional view validation, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onViewValidation(ValidationEvent event) {
     *         ValidationErrors errors = performCustomValidation();
     *         event.addErrors(errors);
     *     }
     * </pre>
     */
    public static class ValidationEvent extends ComponentEvent<View<?>> {

        protected ValidationErrors errors = new ValidationErrors();

        public ValidationEvent(View<?> source) {
            super(source, false);
        }

        @Override
        public View<?> getSource() {
            return super.getSource();
        }

        public void addErrors(ValidationErrors errors) {
            Preconditions.checkNotNullArgument(errors, "Validation errors cannot be null");

            this.errors.addAll(errors);
        }

        public ValidationErrors getErrors() {
            return errors;
        }
    }
}
