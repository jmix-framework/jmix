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

package io.jmix.ui.action.entitypicker;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jmix.ui.screen.FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION;

/**
 * Standard entity picker action for opening an entity instance in its editor screen.
 * <p>
 * Should be defined for {@link EntityPicker} or its subclass in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(target = "io.jmix.ui.component.EntityPicker", description = "Opens an entity using the entity edit screen")
@ActionType(EntityOpenAction.ID)
public class EntityOpenAction<E> extends BaseAction
        implements EntityPicker.EntityPickerAction, Action.ScreenOpeningAction, InitializingBean,
        Action.ExecutableAction {

    public static final String ID = "entity_open";

    protected EntityPicker entityPicker;
    protected Icons icons;

    protected Messages messages;
    protected UiComponentProperties componentProperties;

    protected ScreenBuilders screenBuilders;
    @Autowired
    protected MetadataTools metadataTools;

    protected boolean editable = true;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    private Consumer<E> afterCommitHandler;
    private Function<E, E> transformation;

    public EntityOpenAction() {
        super(ID);
    }

    public EntityOpenAction(String id) {
        super(id);
    }

    /**
     * Returns the editor screen open mode if it was set by {@link #setOpenMode(OpenMode)}
     * or in the screen XML, otherwise returns null.
     */
    @Nullable
    @Override
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    /**
     * Sets the editor screen open mode.
     */
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
     * &#64;Install(to = "petField.open", subject = "screenOptionsSupplier")
     * protected ScreenOptions petFieldOpenScreenOptionsSupplier() {
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
     * &#64;Install(to = "petField.open", subject = "screenConfigurer")
     * protected void petFieldOpenScreenConfigurer(Screen editorScreen) {
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
     * &#64;Install(to = "petField.open", subject = "afterCloseHandler")
     * protected void petFieldOpenAfterCloseHandler(AfterCloseEvent event) {
     *     CloseAction closeAction = event.getCloseAction();
     *     System.out.println("Closed with " + closeAction);
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
     * &#64;Install(to = "petField.open", subject = "afterCommitHandler")
     * protected void petFieldOpenAfterCommitHandler(Pet entity) {
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
     * &#64;Install(to = "petField.open", subject = "transformation")
     * protected Pet petFieldOpenTransformation(Pet entity) {
     *     return doTransform(entity);
     * }
     * </pre>
     */
    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void afterPropertiesSet() {
        setShortcut(componentProperties.getPickerOpenShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("entityPicker.action.open.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("entityPicker.action.open.tooltip"));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setEntityPicker(@Nullable EntityPicker entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        // open action is available in read-only picker
        if (editable) {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_OPEN));
        } else {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_OPEN_READONLY));
        }
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    protected void setEditable(boolean editable) {
        boolean oldValue = this.editable;
        if (oldValue != editable) {
            this.editable = editable;
            firePropertyChange(PROP_EDITABLE, oldValue, editable);
        }
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icons = icons;

        setIcon(icons.get(JmixIcon.ENTITYPICKER_OPEN));
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            // call action perform handlers from super, delegate execution
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void execute() {
        if (!checkFieldValue())
            return;

        Object entity = entityPicker.getValue();

        if (entity != null && EntityValues.isSoftDeleted(entity)) {
            ScreenContext screenContext = ComponentsHelper.getScreenContext(entityPicker);
            Notifications notifications = screenContext.getNotifications();

            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage("OpenAction.objectIsDeleted"))
                    .show();

            return;
        }

        MetaClass metaClass = entityPicker.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the EntityPicker", "action ID", getId());
        }

        EditorBuilder builder = screenBuilders.editor(entityPicker);

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

    protected boolean checkFieldValue() {
        return entityPicker.getValue() != null;
    }
}
