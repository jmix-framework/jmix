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

package io.jmix.ui.actions.picker;

import io.jmix.core.ConfigInterfaces;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.SoftDelete;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.ClientConfig;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.actions.ActionType;
import io.jmix.ui.actions.BaseAction;
import io.jmix.ui.builders.EditorBuilder;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentsHelper;
import io.jmix.ui.components.PickerField;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioDelegate;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Standard picker field action for opening an entity instance in its editor screen.
 * <p>
 * Should be defined for {@code PickerField} or its subclass in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(category = "Picker Actions", description = "Opens an entity using the entity edit screen")
@ActionType(OpenAction.ID)
public class OpenAction extends BaseAction implements PickerField.PickerFieldAction, InitializingBean {

    public static final String ID = "picker_open";

    protected PickerField<Entity> pickerField;
    protected Icons icons;

    protected Messages messages;
    protected ConfigInterfaces configuration;

    @Inject
    protected ScreenBuilders screenBuilders;

    protected boolean editable = true;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    public OpenAction() {
        super(ID);
    }

    public OpenAction(String id) {
        super(id);
    }

    /**
     * Returns the editor screen open mode if it was set by {@link #setOpenMode(OpenMode)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    /**
     * Sets the editor screen open mode.
     */
    @StudioPropertiesItem
    public void setOpenMode(OpenMode openMode) {
        screenInitializer.setOpenMode(openMode);
    }

    /**
     * Returns the editor screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    public void setScreenId(String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the editor screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Class getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    public void setScreenClass(Class screenClass) {
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
    @StudioDelegate
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
    @StudioDelegate
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
    @StudioDelegate
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Inject
    protected void setConfiguration(ConfigInterfaces configuration) {
        this.configuration = configuration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setShortcut(configuration.getConfig(ClientConfig.class).getPickerOpenShortcut());
        setDescription(messages.getMessage("pickerField.action.open.tooltip")
                + " (" + getShortcutCombination().format() + ")");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPickerField(PickerField pickerField) {
        this.pickerField = pickerField;
    }

    @Override
    public void editableChanged(PickerField pickerField, boolean editable) {
        // open action is available in read-only picker
        if (editable) {
            setIcon(icons.get(CubaIcon.PICKERFIELD_OPEN));
        } else {
            setIcon(icons.get(CubaIcon.PICKERFIELD_OPEN_READONLY));
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

    @Inject
    protected void setIcons(Icons icons) {
        this.icons = icons;

        setIcon(icons.get(CubaIcon.PICKERFIELD_OPEN));
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
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
    public void execute() {
        if (!checkFieldValue())
            return;

        Entity entity = pickerField.getValue();

        if (entity instanceof SoftDelete && ((SoftDelete) entity).isDeleted()) {
            ScreenContext screenContext = ComponentsHelper.getScreenContext(pickerField);
            Notifications notifications = screenContext.getNotifications();

            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage("OpenAction.objectIsDeleted"))
                    .show();

            return;
        }

        MetaClass metaClass = pickerField.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor datasource/property is specified " +
                    "for the PickerField", "action ID", getId());
        }

        EditorBuilder builder = screenBuilders.editor(pickerField);

        builder = screenInitializer.initBuilder(builder);

        Screen editorScreen = builder.build();

        screenInitializer.initScreen(editorScreen);

        editorScreen.show();
    }

    protected boolean checkFieldValue() {
        Entity entity = pickerField.getValue();
        return entity != null;
    }
}
