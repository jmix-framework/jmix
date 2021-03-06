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
import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * Standard action for setting an entity to the entity picker using the entity lookup screen.
 * <p>
 * Should be defined for {@link EntityPicker} or its subclass in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> type of entity
 */
@StudioAction(category = "EntityPicker Actions", description = "Sets an entity to the entity picker using the entity lookup screen")
@ActionType(LookupAction.ID)
public class LookupAction<E extends JmixEntity> extends BaseAction implements EntityPicker.EntityPickerAction, InitializingBean {

    public static final String ID = "entity_lookup";

    protected EntityPicker entityPicker;

    protected ScreenBuilders screenBuilders;
    protected Icons icons;
    protected Messages messages;
    protected UiProperties properties;

    protected boolean editable = true;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Predicate<LookupScreen.ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    public LookupAction() {
        super(LookupAction.ID);
    }

    public LookupAction(String id) {
        super(id);
    }

    /**
     * Returns the lookup screen open mode if it was set by {@link #setOpenMode(OpenMode)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    /**
     * Sets the lookup screen open mode.
     */
    @StudioPropertiesItem
    public void setOpenMode(OpenMode openMode) {
        screenInitializer.setOpenMode(openMode);
    }

    /**
     * Returns the lookup screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the lookup screen id.
     */
    @StudioPropertiesItem
    public void setScreenId(String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the lookup screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Class getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the lookup screen id.
     */
    @StudioPropertiesItem
    public void setScreenClass(Class screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    /**
     * Sets the lookup screen options supplier. The supplier provides {@code ScreenOptions} to the
     * opened screen.
     * <p>
     * The preferred way to set the supplier is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "screenOptionsSupplier")
     * protected ScreenOptions petFieldLookupScreenOptionsSupplier() {
     *     return new MapScreenOptions(ParamsMap.of("someParameter", 10));
     * }
     * </pre>
     */
    public void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier) {
        screenInitializer.setScreenOptionsSupplier(screenOptionsSupplier);
    }

    /**
     * Sets the lookup screen configurer. Use the configurer if you need to provide parameters to the
     * opened screen through setters.
     * <p>
     * The preferred way to set the configurer is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "screenConfigurer")
     * protected void petFieldLookupScreenConfigurer(Screen lookupScreen) {
     *     ((PetBrowse) lookupScreen).setSomeParameter(someValue);
     * }
     * </pre>
     */
    public void setScreenConfigurer(Consumer<Screen> screenConfigurer) {
        screenInitializer.setScreenConfigurer(screenConfigurer);
    }

    /**
     * Sets the handler to be invoked when the lookup screen closes.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "afterCloseHandler")
     * protected void petFieldLookupAfterCloseHandler(AfterCloseEvent event) {
     *     CloseAction closeAction = event.getCloseAction();
     *     System.out.println("Closed with " + closeAction);
     * }
     * </pre>
     */
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the validator to be invoked when the user selects entities in the lookup screen.
     * <p>
     * The preferred way to set the validator is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "selectValidator")
     * protected void petFieldLookupSelectValidator(LookupScreen.ValidationContext&lt;Pet&gt; context) {
     *     return checkSelected(context.getSelectedItems());
     * }
     * </pre>
     */
    public void setSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    /**
     * Sets the function to transform selected in the lookup screen entities.
     * <p>
     * The preferred way to set the function is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "transformation")
     * protected Collection&lt;Pet&gt; petFieldLookupTransformation(Collection&lt;Pet&gt; entities) {
     *     return doTransform(entities);
     * }
     * </pre>
     */
    public void setTransformation(Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
    }

    @Override
    public void setEntityPicker(@Nullable EntityPicker entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);
        if (editable) {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
        } else {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP_READONLY));
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

        setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setUiProperties(UiProperties properties) {
        this.properties = properties;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void afterPropertiesSet() {
        setShortcut(properties.getPickerLookupShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip"));
        }
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
    @SuppressWarnings("unchecked")
    public void execute() {
        MetaClass metaClass = entityPicker.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor datasource/property is specified " +
                    "for the EntityPicker", "action ID", getId());
        }

        LookupBuilder builder = screenBuilders.lookup(entityPicker);

        builder = screenInitializer.initBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        Screen lookupScreen = builder.build();

        screenInitializer.initScreen(lookupScreen);

        lookupScreen.show();
    }
}
