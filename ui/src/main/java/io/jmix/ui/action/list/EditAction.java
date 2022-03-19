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

package io.jmix.ui.action.list;

import io.jmix.core.Messages;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jmix.ui.screen.FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION;

/**
 * Standard action for editing an entity instance using its editor screen.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> type of entity
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Edits an entity instance using its editor screen",
        availableInScreenWizard = true)
@ActionType(EditAction.ID)
public class EditAction<E> extends SecuredListAction
        implements Action.AdjustWhenScreenReadOnly, Action.ScreenOpeningAction, Action.ExecutableAction {

    public static final String ID = "edit";

    protected ScreenBuilders screenBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Consumer<E> afterCommitHandler;

    protected Function<E, E> transformation;

    protected boolean captionInitialized = false;
    protected Messages messages;

    public EditAction() {
        super(ID);
    }

    public EditAction(String id) {
        super(id);
        super.setConstraintEntityOp(EntityOp.UPDATE);
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    @StudioPropertiesItem
    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        screenInitializer.setOpenMode(openMode);
    }

    /**
     * Returns the editor screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    @Override
    public void setScreenId(@Nullable String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the editor screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public Class<? extends Screen> getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    @Override
    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    /**
     * Sets the editor screen options supplier. The supplier provides {@code ScreenOptions} to the
     * opened screen.
     * <p>
     * The preferred way to set the supplier is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "screenOptionsSupplier")
     * protected ScreenOptions petsTableEditScreenOptionsSupplier() {
     *     return new MapScreenOptions(ParamsMap.of("someParameter", 10));
     * }
     * </pre>
     */
    @Override
    public void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier) {
        screenInitializer.setScreenOptionsSupplier(screenOptionsSupplier);
    }

    /**
     * Sets the editor screen configurer. Use the configurer if you need to provide parameters to the
     * opened screen through setters.
     * <p>
     * The preferred way to set the configurer is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "screenConfigurer")
     * protected void petsTableEditScreenConfigurer(Screen editorScreen) {
     *     ((PetEdit) editorScreen).setSomeParameter(someValue);
     * }
     * </pre>
     */
    @Override
    public void setScreenConfigurer(Consumer<Screen> screenConfigurer) {
        screenInitializer.setScreenConfigurer(screenConfigurer);
    }

    /**
     * Sets the handler to be invoked when the editor screen closes.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "afterCloseHandler")
     * protected void petsTableEditAfterCloseHandler(AfterCloseEvent event) {
     *     if (event.closedWith(StandardOutcome.COMMIT)) {
     *         System.out.println("Committed");
     *     }
     * }
     * </pre>
     */
    @Override
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the handler to be invoked when the editor screen commits the entity.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "afterCommitHandler")
     * protected void petsTableEditAfterCommitHandler(Pet entity) {
     *     System.out.println("Committed " + entity);
     * }
     * </pre>
     */
    public void setAfterCommitHandler(Consumer<E> afterCommitHandler) {
        this.afterCommitHandler = afterCommitHandler;
    }

    /**
     * Sets the function to transform the committed in the editor screen entity before setting it to the target data container.
     * <p>
     * The preferred way to set the function is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "transformation")
     * protected Pet petsTableEditTransformation(Pet entity) {
     *     return doTransform(entity);
     * }
     * </pre>
     */
    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.EDIT_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.Edit");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        setShortcut(componentProperties.getTableEditShortcut());
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void setCaption(@Nullable String caption) {
        super.setCaption(caption);
        this.captionInitialized = true;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || target.getSingleSelected() == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        InMemoryCrudEntityContext inMemoryCrudEntityContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyRegisteredConstraints(inMemoryCrudEntityContext);

        if (!entityContext.isViewPermitted() && !entityContext.isEditPermitted()) {
            return false;
        }

        if (inMemoryCrudEntityContext.updatePredicate() != null) {
            return true;
        }

        return super.isPermitted();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return;
        }
        if (!captionInitialized) {
            MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);
            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyRegisteredConstraints(inMemoryContext);

            if (metaClass != null) {
                Object entity = target.getSingleSelected();
                if (entityContext.isEditPermitted()
                        && (inMemoryContext.updatePredicate() == null
                        || entity != null && inMemoryContext.isUpdatePermitted(entity))) {
                    super.setCaption(messages.getMessage("actions.Edit"));
                } else {
                    super.setCaption(messages.getMessage("actions.View"));
                }
            }
        }
    }

    @Override
    public boolean isDisabledWhenScreenReadOnly() {
        if (!(target.getItems() instanceof EntityDataUnit)) {
            return true;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass != null) {
            // Even though the screen is read-only, this edit action may remain active
            // because the related entity cannot be edited and the corresponding edit screen
            // will be opened in read-only mode either.
            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return entityContext.isEditPermitted();
        }

        return true;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("EditAction target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("EditAction target dataSource is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        Object editedEntity = target.getSingleSelected();
        if (editedEntity == null) {
            throw new IllegalStateException("There is not selected item in EditAction target");
        }

        EditorBuilder builder = screenBuilders.editor(target)
                .editEntity(editedEntity);

        builder = screenInitializer.initBuilder(builder);

        if (transformation != null) {
            builder.withTransformation(transformation);
        }

        Screen editor = builder.build();

        if (afterCommitHandler != null) {
            editor.addAfterCloseListener(afterCloseEvent -> {
                CloseAction closeAction = afterCloseEvent.getCloseAction();
                if (closeAction.equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                    Object committedEntity = ((EditorScreen) editor).getEditedEntity();
                    afterCommitHandler.accept((E) committedEntity);
                }
            });
        }

        screenInitializer.initScreen(editor);

        editor.show();
    }
}
