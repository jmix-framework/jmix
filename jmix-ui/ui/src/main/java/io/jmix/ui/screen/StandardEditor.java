/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.screen;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.TriggerOnce;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.*;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Base class for editor screens. <br>
 * Supports pessimistic locking, cross field validation and checks for unsaved changes on close.
 *
 * @param <T> type of entity
 */
public abstract class StandardEditor<T> extends Screen
        implements EditorScreen<T>, ReadOnlyAwareScreen {

    protected boolean showSaveNotification = true;
    protected boolean commitActionPerformed = false;

    private T entityToEdit;
    private boolean crossFieldValidate = true;
    private boolean justLocked = false;
    private boolean readOnly = false;
    private boolean readOnlyDueToLock = false;
    protected boolean showEnableEditingBtn = true;

    // whether user has edited entity after screen opening
    private boolean modifiedAfterOpen = false;

    protected StandardEditor() {
        addInitListener(this::initActions);
        addBeforeShowListener(this::beforeShow);
        addBeforeCloseListener(this::beforeClose);
        addAfterShowListener(this::afterShow);
    }

    protected void initActions(@SuppressWarnings("unused") InitEvent event) {
        Messages messages = getApplicationContext().getBean(Messages.class);
        Icons icons = getApplicationContext().getBean(Icons.class);

        BaseAction commitAndCloseAction = (BaseAction) getWindowActionOptional(WINDOW_COMMIT_AND_CLOSE)
                .orElseGet(() ->
                        addDefaultCommitAndCloseAction(messages, icons));
        commitAndCloseAction.addActionPerformedListener(this::commitAndClose);

        BaseAction commitAction = (BaseAction) getWindowActionOptional(WINDOW_COMMIT)
                .orElseGet(() ->
                        addDefaultCommitAction(messages, icons));
        commitAction.addActionPerformedListener(this::commit);

        BaseAction closeAction = (BaseAction) getWindowActionOptional(WINDOW_CLOSE)
                .orElseGet(() ->
                        addDefaultCloseAction(messages, icons));
        closeAction.addActionPerformedListener(this::cancel);

        BaseAction enableEditingAction = (BaseAction) getWindowActionOptional(ENABLE_EDITING)
                .orElseGet(() ->
                        addDefaultEnableEditingAction(messages, icons));
        enableEditingAction.addActionPerformedListener(this::enableEditing);
    }

    protected Optional<Action> getWindowActionOptional(String id) {
        Action action = getWindow().getAction(id);
        return Optional.ofNullable(action);
    }

    protected Action addDefaultCommitAndCloseAction(Messages messages, Icons icons) {
        String commitShortcut = getApplicationContext().getBean(UiScreenProperties.class).getCommitShortcut();

        Action action = new BaseAction(WINDOW_COMMIT_AND_CLOSE)
                .withCaption(messages.getMessage("actions.Ok"))
                .withIcon(icons.get(JmixIcon.EDITOR_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut);

        getWindow().addAction(action);

        return action;
    }

    protected Action addDefaultCommitAction(Messages messages, Icons icons) {
        Action action = new BaseAction(WINDOW_COMMIT)
                .withCaption(messages.getMessage("actions.Save"))
                .withIcon(icons.get(JmixIcon.EDITOR_SAVE));

        getWindow().addAction(action);

        return action;
    }

    protected Action addDefaultCloseAction(Messages messages, Icons icons) {
        Action action = new BaseAction(WINDOW_CLOSE)
                .withCaption(messages.getMessage("actions.Cancel"))
                .withIcon(icons.get(JmixIcon.EDITOR_CANCEL));

        getWindow().addAction(action);

        return action;
    }

    protected Action addDefaultEnableEditingAction(Messages messages, Icons icons) {
        Action action = new BaseAction(ENABLE_EDITING)
                .withCaption(messages.getMessage("actions.EnableEditing"))
                .withIcon(icons.get(JmixIcon.ENABLE_EDITING));
        action.setVisible(false);

        getWindow().addAction(action);

        return action;
    }

    protected void enableEditing(Action.ActionPerformedEvent actionPerformedEvent) {
        setReadOnly(false);
    }

    private void beforeShow(@SuppressWarnings("unused") BeforeShowEvent beforeShowEvent) {
        setupEntityToEdit();
        setupLock();
    }

    private void afterShow(@SuppressWarnings("unused") AfterShowEvent event) {
        setupModifiedTracking();
    }

    private void beforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    protected void setupModifiedTracking() {
        DataContext dataContext = getScreenData().getDataContext();
        if (dataContext != null) {
            dataContext.addChangeListener(this::onChangeEvent);
            dataContext.addPostCommitListener(this::onPostCommitEvent);
        }
    }

    protected void onChangeEvent(@SuppressWarnings("unused") DataContext.ChangeEvent event) {
        setModifiedAfterOpen(true);
    }

    protected void onPostCommitEvent(@SuppressWarnings("unused") DataContext.PostCommitEvent event) {
        setModifiedAfterOpen(false);
    }

    protected void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) action).isCheckForUnsavedChanges()
                && hasUnsavedChanges()) {
            ScreenValidation screenValidation = getApplicationContext().getBean(ScreenValidation.class);

            UnknownOperationResult result = new UnknownOperationResult();

            if (getApplicationContext().getBean(UiScreenProperties.class).isUseSaveConfirmation()) {
                screenValidation.showSaveConfirmationDialog(this, action)
                        .onCommit(() -> result.resume(closeWithCommit()))
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            } else {
                screenValidation.showUnsavedChangesDialog(this, action)
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            }

            event.preventWindowClose(result);
        }
    }

    protected void setupEntityToEdit() {
        if (getScreenData().getDataContextOrNull() == null) {
            throw new IllegalStateException("No DataContext defined. Make sure the editor screen XML descriptor has <data> element");
        }

        if (getEntityStates().isNew(entityToEdit) || doNotReloadEditedEntity()) {
            T mergedEntity = getScreenData().getDataContext().merge(entityToEdit);

            DataContext parentDc = getScreenData().getDataContext().getParent();
            if (parentDc == null || !parentDc.contains(mergedEntity)) {
                fireEvent(InitEntityEvent.class, new InitEntityEvent<>(this, mergedEntity));
            }

            InstanceContainer<T> container = getEditedEntityContainer();
            container.setItem(mergedEntity);
        } else {
            InstanceLoader loader = getEditedEntityLoader();
            loader.setEntityId(EntityValues.getId(entityToEdit));
        }
    }

    protected void setupLock() {
        Object entityId = EntityValues.getId(entityToEdit);

        if (!getEntityStates().isNew(entityToEdit) && entityId != null) {

            AccessManager accessManager = getApplicationContext().getBean(AccessManager.class);
            MetaClass metaClass = getEditedEntityContainer().getEntityMetaClass();

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);
            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, getApplicationContext());
            accessManager.applyRegisteredConstraints(inMemoryContext);

            if (entityContext.isEditPermitted() && (inMemoryContext.updatePredicate() == null || inMemoryContext.isUpdatePermitted(getEditedEntity()))) {
                readOnlyDueToLock = false;
                PessimisticLockStatus lockStatus = getLockingSupport().lock(entityId);
                if (lockStatus == PessimisticLockStatus.LOCKED) {
                    justLocked = true;
                    addAfterDetachListener(afterDetachEvent ->
                            releaseLock()
                    );
                } else if (lockStatus == PessimisticLockStatus.FAILED) {
                    readOnlyDueToLock = true;
                    showEnableEditingBtn = false;
                    setReadOnly(true);
                }
            } else {
                showEnableEditingBtn = false;
                setReadOnly(true);
            }
        }
    }

    protected void releaseLock() {
        if (isLocked()) {
            Object entity = getEditedEntityContainer().getItemOrNull();
            if (entity != null) {
                Object entityId = Objects.requireNonNull(EntityValues.getId(entity));
                getLockingSupport().unlock(entityId);
            }
        }
    }

    private PessimisticLockSupport getLockingSupport() {
        return getApplicationContext().getBean(PessimisticLockSupport.class, this, getEditedEntityContainer());
    }

    protected boolean doNotReloadEditedEntity() {
        if (isEntityModifiedInParentContext()) {
            InstanceContainer<T> container = getEditedEntityContainer();
            if (getEntityStates().isLoadedWithFetchPlan(entityToEdit, container.getFetchPlan())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isEntityModifiedInParentContext() {
        boolean result = false;
        DataContext parentDc = getScreenData().getDataContext().getParent();
        while (!result && parentDc != null) {
            result = isEntityModifiedRecursive(entityToEdit, parentDc, new HashSet<>());
            parentDc = parentDc.getParent();
        }
        return result;
    }

    protected boolean isEntityModifiedRecursive(Object entity, DataContext dataContext, HashSet<Object> visited) {
        if (visited.contains(entity)) {
            return false;
        }
        visited.add(entity);

        if (dataContext.isModified(entity) || dataContext.isRemoved(entity))
            return true;

        Metadata metadata = getApplicationContext().getBean(Metadata.class);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (property.getRange().isClass()) {
                if (getEntityStates().isLoaded(entity, property.getName())) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value != null) {
                        if (value instanceof Collection) {
                            for (Object item : ((Collection) value)) {
                                if (isEntityModifiedRecursive(item, dataContext, visited)) {
                                    return true;
                                }
                            }
                        } else {
                            if (isEntityModifiedRecursive(value, dataContext, visited)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    protected InstanceLoader getEditedEntityLoader() {
        InstanceContainer<T> container = getEditedEntityContainer();
        if (container == null) {
            throw new IllegalStateException("Edited entity container not defined");
        }
        DataLoader loader = null;
        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }
        if (loader == null) {
            throw new IllegalStateException("Loader of edited entity container not found");
        }
        if (!(loader instanceof InstanceLoader)) {
            throw new IllegalStateException(String.format(
                    "Loader %s of edited entity container %s must implement InstanceLoader", loader, container));
        }
        return (InstanceLoader) loader;
    }

    protected InstanceContainer<T> getEditedEntityContainer() {
        EditedEntityContainer annotation = getClass().getAnnotation(EditedEntityContainer.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("StandardEditor %s does not declare @EditedEntityContainer", getClass())
            );
        }
        String[] parts = annotation.value().split("\\.");
        ScreenData screenData;
        if (parts.length == 1) {
            screenData = getScreenData();
        } else {
            Frame frame = getWindow();
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                Component component = frame.getComponent(part);
                if (!(component instanceof Frame)) {
                    throw new IllegalStateException("Path to EditedEntityContainer must include frames only");
                }
                frame = (Frame) component;
            }
            screenData = UiControllerUtils.getScreenData(frame.getFrameOwner());
        }
        return screenData.getContainer(parts[parts.length - 1]);
    }

    @Override
    public T getEditedEntity() {
        T editedEntity = getEditedEntityContainer().getItemOrNull();
        return editedEntity != null ? editedEntity : entityToEdit;
    }

    @Override
    public void setEntityToEdit(T item) {
        this.entityToEdit = item;
    }

    @Override
    public boolean hasUnsavedChanges() {
        if (isReadOnlyDueToLock()) {
            return false;
        }

        // The editor has to be saved if its edited entity was changed after merging into DataContext or if
        // the DataContext contains other modified entities. If the editor is opened for a new instance and it wasn't
        // modified after that, there is no unsaved changes from the user point of view, although DataContext considers
        // this instance as modified.

        DataContext dataContext = getScreenData().getDataContext();

        if (!dataContext.getRemoved().isEmpty()) {
            return true;
        }
        for (Object modified : dataContext.getModified()) {
            if (!getEntityStates().isNew(modified)) {
                return true;
            }
        }
        // if only new entities are registered as modified in DataContext, check whether they were modified after
        // opening the screen
        return isModifiedAfterOpen();
    }

    /**
     * Validates screen and commits data context.
     *
     * @return operation result
     */
    protected OperationResult commitChanges() {
        ValidationErrors validationErrors = validateScreen();
        if (!validationErrors.isEmpty()) {
            ScreenValidation screenValidation = getApplicationContext().getBean(ScreenValidation.class);
            screenValidation.showValidationErrors(this, validationErrors);

            return OperationResult.fail();
        }

        Runnable standardCommitAction = () -> {
            EntitySet committedEntities = getScreenData().getDataContext().commit();

            InstanceContainer<T> container = getEditedEntityContainer();
            if (container instanceof HasLoader) {
                DataLoader loader = ((HasLoader) container).getLoader();
                if (loader instanceof InstanceLoader) {
                    @SuppressWarnings("rawtypes") InstanceLoader instanceLoader = (InstanceLoader) loader;
                    if (instanceLoader.getEntityId() == null
                            && EntityValues.getId(getEditedEntity()) != null) { // id can still be null for identity entity and composition
                        committedEntities.optional(getEditedEntity())
                                .ifPresent(entity -> instanceLoader.setEntityId(EntityValues.getId(entity)));
                    }
                }
            }

            fireEvent(AfterCommitChangesEvent.class, new AfterCommitChangesEvent(this));
        };

        BeforeCommitChangesEvent beforeEvent = new BeforeCommitChangesEvent(this, standardCommitAction);
        fireEvent(BeforeCommitChangesEvent.class, beforeEvent);

        if (beforeEvent.isCommitPrevented()) {
            if (beforeEvent.getCommitResult() != null) {
                return beforeEvent.getCommitResult();
            }

            return OperationResult.fail();
        }

        standardCommitAction.run();

        return OperationResult.success();
    }

    @Override
    public boolean isLocked() {
        return justLocked;
    }

    protected void setModifiedAfterOpen(boolean entityModified) {
        this.modifiedAfterOpen = entityModified;
    }

    /**
     * @return true if data is modified after screen opening
     */
    protected boolean isModifiedAfterOpen() {
        return modifiedAfterOpen;
    }

    /**
     * @return true if the editor switched to read-only mode because the entity is locked by another user
     */
    protected boolean isReadOnlyDueToLock() {
        return readOnlyDueToLock;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;

            ReadOnlyScreensSupport readOnlyScreensSupport = getApplicationContext().getBean(ReadOnlyScreensSupport.class);
            readOnlyScreensSupport.setScreenReadOnly(this, readOnly, showEnableEditingBtn);

            if (readOnlyDueToLock) {
                disableCommitActions();
            }
        }
    }

    protected void disableCommitActions() {
        Action commitAction = getWindow().getAction(WINDOW_COMMIT);
        if (commitAction != null) {
            commitAction.setEnabled(false);
        }

        Action commitCloseAction = getWindow().getAction(WINDOW_COMMIT_AND_CLOSE);
        if (commitCloseAction != null) {
            commitCloseAction.setEnabled(false);
        }
    }

    /**
     * @return true if cross-field validation is enabled
     */
    protected boolean isCrossFieldValidate() {
        return crossFieldValidate;
    }

    protected void setCrossFieldValidate(boolean crossFieldValidate) {
        this.crossFieldValidate = crossFieldValidate;
    }

    /**
     * Validates screen data. Default implementation validates visible and enabled UI components. <br>
     * Can be overridden in subclasses.
     *
     * @return validation errors
     */
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = validateUiComponents();

        validateAdditionalRules(validationErrors);

        return validationErrors;
    }

    /**
     * Validates visible and enabled UI components. <br>
     * Can be overridden in subclasses.
     *
     * @return validation errors
     */
    protected ValidationErrors validateUiComponents() {
        ScreenValidation screenValidation = getApplicationContext().getBean(ScreenValidation.class);
        return screenValidation.validateUiComponents(getWindow());
    }

    protected void validateAdditionalRules(ValidationErrors errors) {
        // all previous validations return no errors
        if (isCrossFieldValidate() && errors.isEmpty()) {
            ScreenValidation screenValidation = getApplicationContext().getBean(ScreenValidation.class);

            ValidationErrors validationErrors = screenValidation.validateCrossFieldRules(this, getEditedEntity());

            errors.addAll(validationErrors);

            ValidationEvent validateEvent = new ValidationEvent(this);
            fireEvent(ValidationEvent.class, validateEvent);
            errors.addAll(validateEvent.getErrors());
        }
    }

    private EntityStates getEntityStates() {
        return getApplicationContext().getBean(EntityStates.class);
    }

    protected void commitAndClose(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        closeWithCommit();
    }

    protected void commit(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        commitChanges()
                .then(() -> {
                    commitActionPerformed = true;
                    if (showSaveNotification) {
                        showSaveNotification();
                    }
                });
    }

    protected void showSaveNotification() {
        getScreenContext().getNotifications().create(NotificationType.TRAY)
                .withCaption(getSaveNotificationCaption())
                .show();
    }

    protected String getSaveNotificationCaption() {
        Metadata metadata = getApplicationContext().getBean(Metadata.class);
        Messages messages = getApplicationContext().getBean(Messages.class);
        MessageTools messageTools = getApplicationContext().getBean(MessageTools.class);
        InstanceNameProvider instanceNameProvider = getApplicationContext().getBean(InstanceNameProvider.class);

        MetaClass metaClass = metadata.getClass(getEditedEntity());

        return messages.formatMessage("", "info.EntitySave",
                messageTools.getEntityCaption(metaClass),
                instanceNameProvider.getInstanceName(getEditedEntity()));
    }


    protected void cancel(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        close(commitActionPerformed ?
                WINDOW_COMMIT_AND_CLOSE_ACTION : WINDOW_CLOSE_ACTION);
    }

    /**
     * Tries to validate and commit data. If data committed successfully then closes the screen with
     * {@link #WINDOW_COMMIT_AND_CLOSE} action. May show validation errors or open an additional dialog before closing
     * the screen.
     *
     * @return result of close request
     */
    public OperationResult closeWithCommit() {
        return commitChanges()
                .compose(() -> close(WINDOW_COMMIT_AND_CLOSE_ACTION));
    }

    /**
     * Ignores the unsaved changes and closes the screen with {@link #WINDOW_DISCARD_AND_CLOSE_ACTION} action.
     *
     * @return result of close request
     */
    public OperationResult closeWithDiscard() {
        return close(WINDOW_DISCARD_AND_CLOSE_ACTION);
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
     * Adds a listener to {@link InitEntityEvent}.
     *
     * @param listener listener
     * @return subscription
     */
    @SuppressWarnings("unchecked")
    protected Subscription addInitEntityListener(Consumer<InitEntityEvent<T>> listener) {
        return getEventHub().subscribe(InitEntityEvent.class, (Consumer) listener);
    }

    /**
     * Adds a listener to {@link BeforeCommitChangesEvent}.
     *
     * @param listener listener
     * @return subscription
     */
    protected Subscription addBeforeCommitChangesListener(Consumer<BeforeCommitChangesEvent> listener) {
        return getEventHub().subscribe(BeforeCommitChangesEvent.class, listener);
    }

    /**
     * Adds a listener to {@link AfterCommitChangesEvent}.
     *
     * @param listener listener
     * @return subscription
     */
    protected Subscription addAfterCommitChangesListener(Consumer<AfterCommitChangesEvent> listener) {
        return getEventHub().subscribe(AfterCommitChangesEvent.class, listener);
    }

    /**
     * Adds a listener to {@link ValidationEvent}.
     *
     * @param listener listener
     * @return subscription
     */
    protected Subscription addValidationEventListener(Consumer<ValidationEvent> listener) {
        return getEventHub().subscribe(ValidationEvent.class, listener);
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
     * @see #addInitEntityListener(Consumer)
     */
    @TriggerOnce
    public static class InitEntityEvent<E> extends EventObject {
        protected final E entity;

        public InitEntityEvent(Screen source, E entity) {
            super(source);
            this.entity = entity;
        }

        @Override
        public Screen getSource() {
            return (Screen) super.getSource();
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
     *
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
     * @see #addBeforeCommitChangesListener(Consumer)
     */
    public static class BeforeCommitChangesEvent extends EventObject {

        protected final Runnable resumeAction;

        protected boolean commitPrevented = false;
        protected OperationResult commitResult;

        public BeforeCommitChangesEvent(Screen source, @Nullable Runnable resumeAction) {
            super(source);
            this.resumeAction = resumeAction;
        }

        @Override
        public Screen getSource() {
            return (Screen) super.getSource();
        }

        /**
         * @return data context of the screen
         */
        public DataContext getDataContext() {
            return getSource().getScreenData().getDataContext();
        }

        /**
         * Prevents commit of the screen.
         */
        public void preventCommit() {
            preventCommit(new UnknownOperationResult());
        }

        /**
         * Prevents commit of the screen.
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
        @Nullable
        public OperationResult getCommitResult() {
            return commitResult;
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
     * @see #addAfterCommitChangesListener(Consumer)
     */
    public static class AfterCommitChangesEvent extends EventObject {

        public AfterCommitChangesEvent(Screen source) {
            super(source);
        }

        @Override
        public Screen getSource() {
            return (Screen) super.getSource();
        }

        /**
         * @return data context of the screen
         */
        public DataContext getDataContext() {
            return getSource().getScreenData().getDataContext();
        }
    }

    /**
     * Event sent when screen is validated from {@link #validateAdditionalRules(ValidationErrors)} call.
     * <br>
     * Use this event listener to perform additional screen validation, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onScreenValidation(ValidationEvent event) {
     *          if (!checkNameFormat()) {
     *             ValidationErrors errors = new ValidationErrors();
     *             errors.add(nameField, "Invalid name format");
     *             event.addErrors(errors);
     *         }
     *     }
     * </pre>
     */
    public static class ValidationEvent extends EventObject {

        ValidationErrors errors = new ValidationErrors();

        public ValidationEvent(Screen source) {
            super(source);
        }

        @Override
        public Screen getSource() {
            return (Screen) super.getSource();
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
