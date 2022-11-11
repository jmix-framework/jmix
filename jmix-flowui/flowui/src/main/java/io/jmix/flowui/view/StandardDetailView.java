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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.validation.group.UiCrossFieldChecks;
import io.jmix.flowui.model.*;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base class of entity detail views.
 *
 * @param <T> entity class
 */
public class StandardDetailView<T> extends StandardView implements DetailView<T>, ReadOnlyAwareView {

    public static final String NEW_ENTITY_ID = "new";
    public static final String DEFAULT_ROUTE_PARAM = "id";
    public static final String MODE_PARAM = "mode";
    public static final String MODE_READONLY = "readonly";

    public static final String LOCKED_BEFORE_REFRESH_ATTR_NAME = "lockedBeforeRefresh";
    public static final String READ_ONLY_BEFORE_REFRESH_ATTR_NAME = "readOnlyBeforeRefresh";

    private T entityToEdit;
    private String serializedEntityIdToEdit;

    private PessimisticLockStatus entityLockStatus = PessimisticLockStatus.NOT_SUPPORTED;

    private boolean showValidationErrors = true;
    private boolean crossFieldValidationEnabled = true;
    private boolean readOnly = false;

    // whether user has edited entity after view opening
    private boolean modifiedAfterOpen = false;

    private boolean showSaveNotification = true;
    private boolean saveActionPerformed = false;

    /**
     * Create views using {@link io.jmix.flowui.ViewNavigators} or {@link io.jmix.flowui.DialogWindows}.
     */
    @Internal
    public StandardDetailView() {
        addBeforeShowListener(this::onBeforeShow);
        addReadyListener(this::onReady);
        addBeforeCloseListener(this::onBeforeClose);
    }

    private void onBeforeShow(BeforeShowEvent event) {
        setupEntityToEdit();
        setupLockHandler();
    }

    private void onReady(ReadyEvent event) {
        setupModifiedTracking();
    }

    private void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    private void setupModifiedTracking() {
        DataContext dataContext = getViewData().getDataContextOrNull();
        if (dataContext != null) {
            dataContext.addChangeListener(this::onChangeEvent);
            dataContext.addPostSaveListener(this::onPostSaveEvent);
        }
    }

    private void onChangeEvent(DataContext.ChangeEvent changeEvent) {
        setModifiedAfterOpen(true);
    }

    private void onPostSaveEvent(DataContext.PostSaveEvent postSaveEvent) {
        setModifiedAfterOpen(false);

        if (!postSaveEvent.getSavedInstances().isEmpty()
                && showSaveNotification) {
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
        checkReadOnlyState(event);

        super.beforeEnter(event);
    }

    private void checkReadOnlyState(BeforeEnterEvent event) {
        Location location = event.getLocation();
        List<String> mode = location
                .getQueryParameters()
                .getParameters()
                .get(MODE_PARAM);

        if (CollectionUtils.isNotEmpty(mode)
                && mode.contains(MODE_READONLY)) {
            setReadOnlyBeforeRefresh();
            setReadOnly(true);
        } else if (isReadOnlyBeforeRefresh()) {
            RouteSupport routeSupport = getApplicationContext().getBean(RouteSupport.class);
            routeSupport.addQueryParameter(getUI().orElse(UI.getCurrent()), MODE_PARAM, MODE_READONLY);

            setReadOnly(true);
        }
    }

    protected void findEntityId(BeforeEnterEvent event) {
        String routeParamName = getRouteParamName();
        serializedEntityIdToEdit = event.getRouteParameters().get(routeParamName)
                .orElseThrow(() ->
                        new IllegalStateException(String.format("Entity '%s' not found", routeParamName)));
    }

    protected String getRouteParamName() {
        return DEFAULT_ROUTE_PARAM;
    }

    private OperationResult saveChanges() {
        ValidationErrors validationErrors = validateView();
        if (!validationErrors.isEmpty()) {
            ViewValidation viewValidation = getViewValidation();
            if (showValidationErrors) {
                viewValidation.showValidationErrors(validationErrors);
            }
            viewValidation.focusProblemComponent(validationErrors);

            return OperationResult.fail();
        }

        Runnable standardSaveAction = createStandardSaveAction();

        BeforeSaveEvent beforeEvent = new BeforeSaveEvent(this, standardSaveAction);
        fireEvent(beforeEvent);

        if (beforeEvent.isSavePrevented()) {
            return beforeEvent.getSaveResult()
                    .orElse(OperationResult.fail());
        }

        standardSaveAction.run();

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

    private Runnable createStandardSaveAction() {
        return () -> {
            EntitySet savedEntities = getViewData().getDataContext().save();

            InstanceContainer<T> container = getEditedEntityContainer();
            if (container instanceof HasLoader) {
                DataLoader loader = ((HasLoader) container).getLoader();
                if (loader instanceof InstanceLoader) {
                    //noinspection rawtypes
                    InstanceLoader instanceLoader = (InstanceLoader) loader;
                    if (instanceLoader.getEntityId() == null) {
                        savedEntities.optional(getEditedEntity())
                                .ifPresent(entity ->
                                        instanceLoader.setEntityId(
                                                requireNonNull(EntityValues.getId(entity))
                                        )
                                );
                    }
                }
            }

            fireEvent(new AfterSaveEvent(this));
        };
    }

    protected boolean isSaveActionPerformed() {
        return saveActionPerformed;
    }

    @Override
    public OperationResult save() {
        return saveChanges()
                .then(() -> saveActionPerformed = true);
    }

    @Override
    public OperationResult closeWithSave() {
        return saveChanges()
                .compose(() -> close(StandardOutcome.SAVE));
    }

    @Override
    public OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }

    /**
     * @return whether a notification will be shown in case of successful save
     */
    public boolean isShowSaveNotification() {
        return showSaveNotification;
    }

    /**
     * Sets whether a notification should be shown in case of successful save. The default value is {@code true}.
     *
     * @param showSaveNotification {@code true} if a notification should be shown, {@code false} otherwise
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
     * Sets whether cross-field validation should be performed before saving changes.
     * It uses {@link UiCrossFieldChecks} constraint group to validate bean instance.
     * The default value is {@code true}.
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
                    .getBean(FlowuiViewProperties.class).isUseSaveConfirmation();

            if (action instanceof NavigateCloseAction) {
                BeforeLeaveEvent beforeLeaveEvent = ((NavigateCloseAction) action).getBeforeLeaveEvent();
                ContinueNavigationAction navigationAction = beforeLeaveEvent.postpone();

                if (useSaveConfirmation) {
                    getViewValidation().showSaveConfirmationDialog(this)
                            .onSave(() -> result.resume(navigateWithSave(navigationAction)))
                            .onDiscard(() -> result.resume(navigateWithDiscard(navigationAction)))
                            .onCancel(result::fail);
                } else {
                    getViewValidation().showUnsavedChangesDialog(this)
                            .onDiscard(() -> result.resume(navigateWithDiscard(navigationAction)))
                            .onCancel(result::fail);
                }
            } else {
                if (useSaveConfirmation) {
                    getViewValidation().showSaveConfirmationDialog(this)
                            .onSave(() -> result.resume(closeWithSave()))
                            .onDiscard(() -> result.resume(closeWithDiscard()))
                            .onCancel(result::fail);
                } else {
                    getViewValidation().showUnsavedChangesDialog(this)
                            .onDiscard(() -> result.resume(closeWithDiscard()))
                            .onCancel(result::fail);
                }
            }

            event.preventClose(result);
        }
    }

    private OperationResult navigateWithDiscard(ContinueNavigationAction navigationAction) {
        return navigate(navigationAction, StandardOutcome.DISCARD.getCloseAction());
    }

    private OperationResult navigateWithSave(ContinueNavigationAction navigationAction) {
        return saveChanges()
                .compose(() -> navigate(navigationAction, StandardOutcome.SAVE.getCloseAction()));
    }

    private OperationResult navigate(ContinueNavigationAction navigationAction,
                                     CloseAction closeAction) {
        navigationAction.proceed();

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        return OperationResult.success();
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
        T item = getEditedEntityContainer().getItemOrNull();
        if (item == null && entityToEdit == null) {
            throw new IllegalStateException("Edited entity isn't initialized yet");
        }

        return item != null ? item : entityToEdit;
    }

    @Override
    public void setEntityToEdit(T entity) {
        this.entityToEdit = entity;
    }

    protected void setupEntityToEdit() {
        if (serializedEntityIdToEdit != null) {
            setupEntityToEdit(serializedEntityIdToEdit);
        } else if (entityToEdit != null) {
            setupEntityToEdit(entityToEdit);
        } else {
            throw new IllegalStateException("Nether entity nor entity id to edit is defined");
        }
    }

    protected void setupEntityToEdit(String serializedEntityId) {
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

    protected void initNewEntity(Class<T> entityClass) {
        DataContext dataContext = getViewData().getDataContext();

        T newEntity = dataContext.create(entityClass);

        fireEvent(new InitEntityEvent<>(this, newEntity));

        InstanceContainer<T> container = getEditedEntityContainer();
        container.setItem(newEntity);
    }

    protected void initExistingEntity(String serializedEntityId) {
        Object entityId = getUrlParamSerializer().deserialize(getSerializedIdType(), serializedEntityId);
        getEditedEntityLoader().setEntityId(entityId);
    }

    private Class<?> getSerializedIdType() {
        MetaClass entityMetaClass = getEditedEntityContainer().getEntityMetaClass();
        MetaProperty primaryKeyProperty = getMetadataTools().getPrimaryKeyProperty(entityMetaClass);
        if (primaryKeyProperty == null) {
            throw new IllegalStateException(String.format(
                    "Entity %s has no primary key", entityMetaClass.getName()));
        }

        return primaryKeyProperty.getJavaType();
    }

    protected void setupEntityToEdit(T entityToEdit) {
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

    private void setupLockHandler() {
        getEditedEntityContainer().addItemChangeListener(this::itemChangeLockHandler);
    }

    private void itemChangeLockHandler(InstanceContainer.ItemChangeEvent<T> event) {
        if (entityLockStatus == PessimisticLockStatus.LOCKED) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }

        Object editedEntityId = null;

        if (entityToEdit != null) {
            editedEntityId = EntityValues.getId(entityToEdit);
        } else if (serializedEntityIdToEdit != null) {
            // Do not set up lock, if it's new instance
            if (NEW_ENTITY_ID.equals(serializedEntityIdToEdit)) {
                return;
            }
            editedEntityId = getUrlParamSerializer()
                    .deserialize(getSerializedIdType(), serializedEntityIdToEdit);
        }

        Object id = EntityValues.getId(event.getItem());
        // If it's the same instance, set up lock
        if (EntityValues.propertyValueEquals(id, editedEntityId)) {
            setupLock();
        }
    }


    private void setupLock() {
        T editedEntity = getEditedEntity();
        //noinspection ConstantConditions
        if (editedEntity == null) {
            return;
        }

        if (isLockedBeforeRefresh()) {
            // restore state after refresh
            entityLockStatus = PessimisticLockStatus.LOCKED;
            addAfterCloseListener(__ -> releaseLock());
            return;
        }

        Object entityId = EntityValues.getId(editedEntity);

        if (!getEntityStates().isNew(editedEntity) && entityId != null) {
            AccessManager accessManager = getApplicationContext().getBean(AccessManager.class);
            MetaClass metaClass = getEditedEntityContainer().getEntityMetaClass();

            FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, getApplicationContext());
            accessManager.applyRegisteredConstraints(inMemoryContext);

            boolean isPermittedBySecurity = entityContext.isEditPermitted()
                    && (inMemoryContext.updatePredicate() == null
                    || inMemoryContext.isUpdatePermitted(getEditedEntity()));

            if (isPermittedBySecurity) {
                entityLockStatus = getLockingSupport().lock(entityId);
                if (entityLockStatus == PessimisticLockStatus.LOCKED) {
                    setLockedBeforeRefresh();
                    addAfterCloseListener(__ -> releaseLock());
                } else if (entityLockStatus == PessimisticLockStatus.FAILED) {
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

    /**
     * @return {@code true} if the entity instance has been pessimistically locked in this view before refreshing the
     * page.
     */
    private boolean isLockedBeforeRefresh() {
        return Boolean.TRUE.equals(getViewAttributes().getAttribute(LOCKED_BEFORE_REFRESH_ATTR_NAME));
    }

    private void setLockedBeforeRefresh() {
        getViewAttributes().setAttribute(LOCKED_BEFORE_REFRESH_ATTR_NAME, true);
    }

    /**
     * @return {@code true} if the view instance has been set to read-only mode before refreshing the page.
     */
    private boolean isReadOnlyBeforeRefresh() {
        return Boolean.TRUE.equals(getViewAttributes().getAttribute(READ_ONLY_BEFORE_REFRESH_ATTR_NAME));
    }

    private void setReadOnlyBeforeRefresh() {
        getViewAttributes().setAttribute(READ_ONLY_BEFORE_REFRESH_ATTR_NAME, true);
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

    private UrlParamSerializer getUrlParamSerializer() {
        return getApplicationContext().getBean(UrlParamSerializer.class);
    }

    /**
     * Adds a listener to {@link InitEntityEvent}.
     *
     * @param listener listener
     * @return registration object for removing the listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Registration addInitEntityListener(ComponentEventListener<InitEntityEvent<T>> listener) {
        return getEventBus().addListener(InitEntityEvent.class, ((ComponentEventListener) listener));
    }

    /**
     * Adds a listener to {@link BeforeSaveEvent}.
     *
     * @param listener listener
     * @return registration object for removing the listener
     */
    protected Registration addBeforeSaveListener(ComponentEventListener<BeforeSaveEvent> listener) {
        return getEventBus().addListener(BeforeSaveEvent.class, listener);
    }

    /**
     * Adds a listener to {@link AfterSaveEvent}.
     *
     * @param listener listener
     * @return registration object for removing the listener
     */
    protected Registration addAfterSaveListener(ComponentEventListener<AfterSaveEvent> listener) {
        return getEventBus().addListener(AfterSaveEvent.class, listener);
    }

    /**
     * Adds a listener to {@link ValidationEvent}.
     *
     * @param listener listener
     * @return registration object for removing the listener
     */
    protected Registration addValidationEventListener(ComponentEventListener<ValidationEvent> listener) {
        return getEventBus().addListener(ValidationEvent.class, listener);
    }

    /**
     * Event sent before the new entity instance is set to edited entity container.
     * <p>
     * Use this event listener to initialize default values in the new entity instance, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onInitEntity(InitEntityEvent&lt;Foo&gt; event) {
     *         event.getEntity().setStatus(Status.ACTIVE);
     *     }
     * </pre>
     *
     * @param <E> type of entity
     * @see #addInitEntityListener(ComponentEventListener)
     */
    public static class InitEntityEvent<E> extends ComponentEvent<View<?>> {

        protected final E entity;

        public InitEntityEvent(View<?> source, E entity) {
            super(source, false);

            this.entity = entity;
        }

        /**
         * @return entity to initialize
         */
        public E getEntity() {
            return entity;
        }
    }

    /**
     * Event sent before saving the view data context.
     * <br>
     * Use this event listener to prevent saving and/or interact with the user before save, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeSave(BeforeSaveEvent event) {
     *         if (getEditedEntity().getDescription() == null) {
     *             notifications.show("Description required");
     *             event.preventSave();
     *         }
     *     }
     * </pre>
     * <p>
     * Show dialog and resume saving after:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeSave(BeforeSaveEvent event) {
     *         if (getEditedEntity().getDescription() == null) {
     *             dialogs.createOptionDialog()
     *                     .withHeader("Question")
     *                     .withText("Do you want to set default description?")
     *                     .withActions(
     *                             new DialogAction(DialogAction.Type.YES).withHandler(e -&gt; {
     *                                 getEditedEntity().setDescription("No description");
     *
     *                                 // retry save and resume action
     *                                 event.resume(save());
     *                             }),
     *                             new DialogAction(DialogAction.Type.NO).withHandler(e -&gt; {
     *                                 // trigger standard save and resume action
     *                                 event.resume();
     *                             })
     *                     )
     *                     .open();
     *
     *             event.preventSave();
     *         }
     *     }
     * </pre>
     *
     * @see #addBeforeSaveListener(ComponentEventListener)
     */
    public static class BeforeSaveEvent extends ComponentEvent<View<?>> {

        protected final Runnable resumeAction;

        protected boolean savePrevented = false;
        protected OperationResult saveResult;

        public BeforeSaveEvent(View<?> source, @Nullable Runnable resumeAction) {
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
         * Prevents saving of the view data.
         */
        public void preventSave() {
            preventSave(new UnknownOperationResult());
        }

        /**
         * Prevents saving of the view data.
         *
         * @param saveResult result object that will be returned from the {@link #saveChanges()}} method
         */
        public void preventSave(OperationResult saveResult) {
            this.savePrevented = true;
            this.saveResult = saveResult;
        }

        /**
         * Resume standard execution.
         */
        public void resume() {
            if (resumeAction != null) {
                resumeAction.run();
            }
            if (saveResult instanceof UnknownOperationResult) {
                ((UnknownOperationResult) saveResult).resume(OperationResult.success());
            }
        }

        /**
         * Resume with the passed result ignoring standard execution.
         * The standard save will not be performed.
         */
        public void resume(OperationResult result) {
            if (saveResult instanceof UnknownOperationResult) {
                ((UnknownOperationResult) saveResult).resume(result);
            }
        }

        /**
         * @return result passed to the {@link #preventSave(OperationResult)} method
         */
        public Optional<OperationResult> getSaveResult() {
            return Optional.ofNullable(saveResult);
        }

        /**
         * @return whether the saving process was prevented by invoking {@link #preventSave()} method
         */
        public boolean isSavePrevented() {
            return savePrevented;
        }
    }

    /**
     * Event sent after saving the view data context.
     * <br>
     * Use this event listener to notify users after save, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onAfterSave(AfterSaveEvent event) {
     *         notifications.show("Saved");
     *     }
     * </pre>
     *
     * @see #addAfterSaveListener(ComponentEventListener)
     */
    public static class AfterSaveEvent extends ComponentEvent<View<?>> {

        public AfterSaveEvent(View source) {
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
     * Event sent when the view is validated on saving the view data context.
     * <br>
     * Use this event listener to perform additional validation of the view, for example:
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

        /**
         * Add errors found by a custom validation.
         */
        public void addErrors(ValidationErrors errors) {
            Preconditions.checkNotNullArgument(errors, "Validation errors cannot be null");

            this.errors.addAll(errors);
        }

        public ValidationErrors getErrors() {
            return errors;
        }
    }
}
