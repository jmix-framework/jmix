/*
 * Copyright (c) 2008-2019 Haulmont.
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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jmix.ui.screen.FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION;

/**
 * Standard action for opening an editor screen in the read-only mode.
 * The editor screen must implement the {@link ReadOnlyAwareScreen} interface.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Opens an editor screen for an entity instance in read-only mode")
@ActionType(ViewAction.ID)
public class ViewAction<E> extends SecuredListAction implements Action.ScreenOpeningAction, Action.ExecutableAction {

    public static final String ID = "view";

    protected ScreenBuilders screenBuilders;
    protected ReadOnlyScreensSupport readOnlyScreensSupport;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Consumer<E> afterCommitHandler;

    protected Function<E, E> transformation;

    public ViewAction() {
        this(ID);
    }

    public ViewAction(String id) {
        super(id);
        super.setConstraintEntityOp(EntityOp.READ);
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
     * &#64;Install(to = "petsTable.view", subject = "screenOptionsSupplier")
     * protected ScreenOptions petsTableViewScreenOptionsSupplier() {
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
     * &#64;Install(to = "petsTable.view", subject = "screenConfigurer")
     * protected void petsTableViewScreenConfigurer(Screen editorScreen) {
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
     * &#64;Install(to = "petsTable.view", subject = "afterCloseHandler")
     * protected void petsTableViewAfterCloseHandler(AfterCloseEvent event) {
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
     * Sets the handler to be invoked when the editor screen commits the entity if "enable editing" action was executed.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "afterCommitHandler")
     * protected void petsTableViewAfterCommitHandler(Pet entity) {
     *     System.out.println("Committed " + entity);
     * }
     * </pre>
     */
    public void setAfterCommitHandler(Consumer<E> afterCommitHandler) {
        this.afterCommitHandler = afterCommitHandler;
    }

    /**
     * Sets the function to transform the committed in the editor screen entity (if "enable editing" action was executed)
     * before setting it to the target data container.
     * <p>
     * The preferred way to set the function is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "transformation")
     * protected Pet petsTableViewTransformation(Pet entity) {
     *     return doTransform(entity);
     * }
     * </pre>
     */
    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Autowired
    protected void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.VIEW_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.View");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        setShortcut(componentProperties.getTableViewShortcut());
    }

    @Autowired
    protected void setReadOnlyScreensSupport(ReadOnlyScreensSupport readOnlyScreensSupport) {
        this.readOnlyScreensSupport = readOnlyScreensSupport;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isViewPermitted()) {
            return false;
        }

        return super.isPermitted();
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
            throw new IllegalStateException("ViewAction target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("ViewAction target dataSource is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        Object editedEntity = target.getSingleSelected();
        if (editedEntity == null) {
            throw new IllegalStateException("There is not selected item in ViewAction target");
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

        if (editor instanceof ReadOnlyAwareScreen) {
            ((ReadOnlyAwareScreen) editor).setReadOnly(true);

            if (isReadOnlyCompositionEditor(editor)) {
                readOnlyScreensSupport.setScreenReadOnly(editor, true, false);
            }
        } else {
            throw new IllegalStateException(String.format("Screen '%s' does not implement ReadOnlyAwareScreen: %s",
                    editor.getId(), editor.getClass()));
        }

        editor.show();
    }

    /**
     * In case of composition relation, editor for nested entities should be in read-only mode with hidden
     * "enableEditing" action if master editor is in read-only mode too.
     *
     * @param editor editor to check
     * @return {@code true} if the relation between entities is a composition
     */
    protected boolean isReadOnlyCompositionEditor(Screen editor) {
        Frame frame = target.getFrame();
        if (frame == null) {
            throw new IllegalStateException("Component is not attached to the Frame");
        }

        FrameOwner origin = target.getFrame().getFrameOwner();
        if (!(origin instanceof ReadOnlyAwareScreen)
                || !((ReadOnlyAwareScreen) origin).isReadOnly()
                || !(editor instanceof StandardEditor)) {
            return false;
        }

        DataUnit items = target.getItems();
        if (!(items instanceof ContainerDataUnit)) {
            return false;
        }

        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        if (!(container instanceof CollectionPropertyContainer)) {
            return false;
        }

        InstanceContainer masterContainer = ((CollectionPropertyContainer) container).getMaster();
        String property = ((CollectionPropertyContainer) container).getProperty();

        MetaClass metaClass = masterContainer.getEntityMetaClass();
        MetaProperty metaProperty = metaClass.getProperty(property);

        return metaProperty.getType() == MetaProperty.Type.COMPOSITION;
    }
}
