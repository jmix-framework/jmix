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

package io.jmix.ui.action.valuespicker;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.valuespicker.selectvalue.SelectValueController;
import io.jmix.ui.app.valuespicker.selectvalue.SelectValueController.SelectValueContext;
import io.jmix.ui.builder.ScreenBuilder;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.component.ValuePicker.ValuePickerAction;
import io.jmix.ui.component.ValuesPicker;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import static io.jmix.ui.screen.FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION;

/**
 * Standard values picker action for selection the field value.
 * <p>
 * Should be defined for {@link ValuesPicker} or its subclass in a screen XML descriptor.
 */
@StudioAction(
        target = "io.jmix.ui.component.ValuesPicker",
        description = "Sets a value to the values picker using the selection screen")
@ActionType(ValuesSelectAction.ID)
public class ValuesSelectAction<V> extends BaseAction implements ValuePickerAction, InitializingBean,
        Action.ExecutableAction {

    public static final String ID = "values_select";
    public static final String DEFAULT_SELECT_VALUE_SCREEN = "selectValueDialog";

    protected ValuesPicker<V> valuesPicker;

    protected Icons icons;
    protected Messages messages;
    protected UiComponentProperties componentProperties;
    protected ScreenBuilders screenBuilders;

    protected boolean editable = true;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();
    protected SelectValueContext<V> selectValueContext = new SelectValueContext<>();

    public ValuesSelectAction() {
        this(ID);
    }

    public ValuesSelectAction(String id) {
        super(id);
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icons = icons;

        setIcon(icons.get(JmixIcon.VALUESPICKER_SELECT));
    }

    @Override
    public void afterPropertiesSet() {
        setShortcut(componentProperties.getPickerLookupShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("valuesPicker.action.select.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("valuesPicker.action.select.tooltip"));
        }

        setSelectValueScreenId(DEFAULT_SELECT_VALUE_SCREEN);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        if (valuePicker != null && !(valuePicker instanceof ValuesPicker)) {
            throw new IllegalArgumentException("Incorrect component type. Must be " +
                    "'ValuesPicker' or its inheritors");
        }

        this.valuesPicker = (ValuesPicker<V>) valuePicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);

        if (editable) {
            setIcon(icons.get(JmixIcon.VALUESPICKER_SELECT));
        } else {
            setIcon(icons.get(JmixIcon.VALUESPICKER_SELECT_READONLY));
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

    /**
     * Returns the id of {@link SelectValueController} screen if it was set by {@link #setSelectValueScreenId(String)}
     * or in the screen XML.
     *
     * @return the id of {@link SelectValueController} screen
     */
    public String getSelectValueScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the id of {@link SelectValueController} screen.
     *
     * @param selectValueScreenId the id of {@link SelectValueController} screen
     */
    @StudioPropertiesItem(type = PropertyType.SCREEN_ID, defaultValue = DEFAULT_SELECT_VALUE_SCREEN,
            options = {"io.jmix.ui.app.valuespicker.selectvalue.SelectValueController"})
    public void setSelectValueScreenId(String selectValueScreenId) {
        screenInitializer.setScreenId(selectValueScreenId);
    }

    /**
     * Returns the class of {@link SelectValueController} screen if it was set by
     * {@link #setSelectValueScreenId(String)} or in the screen XML. Otherwise returns {@code null}.
     *
     * @return the class of {@link SelectValueController} screen
     */
    @Nullable
    public Class getSelectValueScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the class of {@link SelectValueController} screen.
     *
     * @param selectValueScreenClass the class of {@link SelectValueController} screen
     */
    @StudioPropertiesItem
    public void setSelectValueScreenClass(Class selectValueScreenClass) {
        screenInitializer.setScreenClass(selectValueScreenClass);
    }

    /**
     * @return the lookup screen id
     */
    @Nullable
    public String getLookupScreenId() {
        return selectValueContext.getLookupScreenId();
    }

    /**
     * Sets the lookup screen id to be passed into {@link SelectValueController} screen.
     *
     * @param lookupScreenId the lookup screen id
     */
    @StudioPropertiesItem(type = PropertyType.SCREEN_ID, options = {"io.jmix.ui.screen.LookupScreen"})
    public void setLookupScreenId(@Nullable String lookupScreenId) {
        selectValueContext.setLookupScreenId(lookupScreenId);
    }

    /**
     * @return the entity name which is used as the component value type
     */
    @Nullable
    public String getEntityName() {
        return selectValueContext.getEntityName();
    }

    /**
     * Sets the entity name which is used as the component value type in {@link SelectValueController} screen.
     *
     * @param entityName the entity name which is used as the component value type
     */
    @StudioPropertiesItem(type = PropertyType.ENTITY_NAME)
    public void setEntityName(@Nullable String entityName) {
        selectValueContext.setEntityName(entityName);
    }

    /**
     * @return the java class which is used as the component value type
     */
    @Nullable
    public Class<?> getJavaClass() {
        return selectValueContext.getJavaClass();
    }

    /**
     * Sets the java class which is used as the component value type in {@link SelectValueController} screen.
     *
     * @param javaClass the java class which is used as the component value type
     */
    @StudioPropertiesItem(type = PropertyType.JAVA_CLASS_NAME)
    public void setJavaClass(@Nullable Class<?> javaClass) {
        selectValueContext.setJavaClass(javaClass);
    }

    /**
     * @return the enumeration class which is used as the component value type
     */
    @Nullable
    public Class<? extends Enum> getEnumClass() {
        return selectValueContext.getEnumClass();
    }

    /**
     * Sets the enumeration class which is used as the component value type in {@link SelectValueController} screen.
     *
     * @param enumClass the enumeration class which is used as the component value type
     */
    @StudioPropertiesItem(type = PropertyType.JAVA_CLASS_NAME)
    public void setEnumClass(@Nullable Class<? extends Enum> enumClass) {
        selectValueContext.setEnumClass(enumClass);
    }

    /**
     * @return whether or not the {@link ComboBox} should be used in {@link SelectValueController} screen
     */
    public boolean isUseComboBox() {
        return selectValueContext.isUseComboBox();
    }

    /**
     * Sets whether the {@link ComboBox} should be used in {@link SelectValueController} screen.
     *
     * @param useComboBox whether the {@link ComboBox} should be used in {@link SelectValueController} screen
     */
    @StudioPropertiesItem(type = PropertyType.BOOLEAN, defaultValue = "false")
    public void setUseComboBox(boolean useComboBox) {
        selectValueContext.setUseComboBox(useComboBox);
    }

    /**
     * @return the resolution of {@link DateField}
     */
    @Nullable
    public DateField.Resolution getResolution() {
        return selectValueContext.getResolution();
    }

    /**
     * Sets the resolution of {@link DateField} component. The {@link DateField} component is used to select values
     * which have date value type. The component is used in {@link SelectValueController} screen.
     *
     * @param resolution the resolution of {@link DateField}
     */
    @StudioPropertiesItem(type = PropertyType.ENUMERATION)
    public void setResolution(@Nullable DateField.Resolution resolution) {
        selectValueContext.setResolution(resolution);
    }

    /**
     * @return the time zone of {@link DateField}
     */
    @Nullable
    public TimeZone getTimeZone() {
        return selectValueContext.getTimeZone();
    }

    /**
     * Sets the time zone of {@link DateField} component. The {@link DateField} component is used to select values
     * which have date value type. The component is used in {@link SelectValueController} screen.
     *
     * @param timeZone the time zone of {@link DateField}
     */
    public void setTimeZone(@Nullable TimeZone timeZone) {
        selectValueContext.setTimeZone(timeZone);
    }

    /**
     * @return options
     */
    @Nullable
    public Options<V> getOptions() {
        return selectValueContext.getOptions();
    }

    /**
     * Sets options which are used in {@link SelectValueController} screen.
     *
     * @param options options
     */
    public void setOptions(@Nullable Options<V> options) {
        selectValueContext.setOptions(options);
    }

    /**
     * @return caption provider for options
     */
    @Nullable
    public Function<V, String> getOptionCaptionProvider() {
        return selectValueContext.getOptionCaptionProvider();
    }

    /**
     * Sets function that provides caption for option items.
     *
     * @param optionCaptionProvider caption provider for options
     */
    public void setOptionCaptionProvider(@Nullable Function<V, String> optionCaptionProvider) {
        selectValueContext.setOptionCaptionProvider(optionCaptionProvider);
    }

    /**
     * @return the collection of validators
     */
    public List<Validator<V>> getValidators() {
        return selectValueContext.getValidators();
    }

    /**
     * Sets the collection of {@link Validator}'s which are used in {@link SelectValueController} screen to validate
     * component values.
     *
     * @param validators the collection of validators
     */
    public void setValidators(@Nullable List<Validator<V>> validators) {
        selectValueContext.setValidators(validators);
    }

    /**
     * Adds validator which is used in {@link SelectValueController} screen to validate component values.
     *
     * @param validator validator
     */
    public void addValidator(Validator<V> validator) {
        selectValueContext.addValidator(validator);
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
        if (valuesPicker == null) {
            throw new IllegalStateException("Action is not bound to a ValuesPicker");
        }

        if (valuesPicker.getFrame() == null) {
            throw new IllegalStateException("ValuesPicker component is not bound to a frame");
        }

        ScreenBuilder builder = screenBuilders.screen(valuesPicker.getFrame().getFrameOwner());
        builder = screenInitializer.initBuilder(builder);
        Screen screen = builder.build();

        if (!(screen instanceof SelectValueController)) {
            throw new IllegalArgumentException("Select value screen must implement " +
                    "'io.jmix.ui.app.valuespicker.selectvalue.SelectValueController");
        }

        if (valuesPicker.getValueSource() instanceof EntityValueSource) {
            initSelectValueComponentValueType((EntityValueSource) valuesPicker.getValueSource());
        }

        selectValueContext.setInitialValues(valuesPicker.getValue());

        ((SelectValueController<V>) screen).setSelectValueContext(selectValueContext);

        screen.addAfterCloseListener(event -> {
            CloseAction closeAction = event.getCloseAction();
            if (closeAction.equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                SelectValueController<V> selectValueScreen = (SelectValueController<V>) screen;
                valuesPicker.setValueFromUser((selectValueScreen).getValue());
            }
        });

        screen.show();
    }

    @SuppressWarnings("unchecked")
    protected void initSelectValueComponentValueType(EntityValueSource<?, ?> valueSource) {
        Range range = valueSource.getMetaPropertyPath()
                .getRange();

        if (getEntityName() == null && range.isClass()) {
            setEntityName(range.asClass().getName());
        } else if (getJavaClass() == null && range.isDatatype()) {
            setJavaClass(range.asDatatype().getJavaClass());
        } else if (getEnumClass() == null && range.isEnum()) {
            setEnumClass(range.asEnumeration().getJavaClass());
        }
    }
}
