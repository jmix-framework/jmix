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
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.valuespicker.selectvalue.SelectValueController;
import io.jmix.ui.app.valuespicker.selectvalue.SelectValueController.SelectValueContext;
import io.jmix.ui.builder.ScreenBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.component.ValuePicker.ValuePickerAction;
import io.jmix.ui.component.ValuesPicker;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import static io.jmix.ui.screen.FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION;

/**
 * Standard values picker action for selection the field value.
 * <p>
 * Should be defined for {@link ValuesPicker} or its subclass in a screen XML descriptor.
 */
@StudioAction(
        category = "ValuesPicker Actions",
        description = "Sets a value to the values picker using the selection screen")
@ActionType(SelectAction.ID)
public class SelectAction<V> extends BaseAction implements ValuePickerAction, InitializingBean {

    public static final String ID = "values_select";
    public static final String DEFAULT_SELECT_VALUE_SCREEN = "selectValueDialog";

    protected ValuesPicker<V> valuesPicker;

    protected Icons icons;
    protected Messages messages;
    protected UiProperties properties;
    protected ScreenBuilders screenBuilders;

    protected boolean editable = true;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();
    protected SelectValueContext<V> selectValueContext;

    public SelectAction() {
        this(ID);
    }

    public SelectAction(String id) {
        super(id);
    }

    @Autowired
    protected void setUiProperties(UiProperties properties) {
        this.properties = properties;
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
        setShortcut(properties.getPickerLookupShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("valuesPicker.action.select.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("valuesPicker.action.select.tooltip"));
        }

        setScreenId(DEFAULT_SELECT_VALUE_SCREEN);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        if (!(valuePicker instanceof ValuesPicker)) {
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

    public SelectValueContext<V> getSelectValueContext() {
        return selectValueContext;
    }

    public void setSelectValueContext(SelectValueContext<V> selectValueContext) {
        this.selectValueContext = selectValueContext;
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
        ScreenBuilder builder = screenBuilders.screen(valuesPicker.getFrame().getFrameOwner());
        builder = screenInitializer.initBuilder(builder);
        Screen screen = builder.build();

        if (!(screen instanceof SelectValueController)) {
            throw new IllegalArgumentException("Screen must implement " +
                    "'io.jmix.ui.app.valuespicker.selectvalue.SelectValueController");
        }

        if (selectValueContext == null) {
            throw new IllegalStateException("SelectValueContext is not set");
        }

        selectValueContext.setInitialValues(valuesPicker.getValue());

        ((SelectValueController<V>) screen).setSelectValueContext(selectValueContext);

        screen.addAfterCloseListener(event -> {
            CloseAction closeAction = event.getCloseAction();
            if (closeAction.equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                SelectValueController<V> selectValueScreen = (SelectValueController<V>) screen;
                valuesPicker.setValue((selectValueScreen).getValue());
            }
        });

        screen.show();
    }
}
