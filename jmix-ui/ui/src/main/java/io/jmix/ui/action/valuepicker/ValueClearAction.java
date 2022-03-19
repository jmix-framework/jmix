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

package io.jmix.ui.action.valuepicker;

import io.jmix.core.Messages;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.UiComponentProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Standard value picker action for clearing the field value.
 * <p>
 * Should be defined for {@link ValuePicker} or its subclass in a screen XML descriptor.
 */
@StudioAction(target = "io.jmix.ui.component.ValuePicker", description = "Clears the value picker value")
@ActionType(ValueClearAction.ID)
public class ValueClearAction extends BaseAction implements ValuePicker.ValuePickerAction, InitializingBean,
        Action.ExecutableAction {

    public static final String ID = "value_clear";

    protected ValuePicker valuePicker;

    protected Icons icons;
    protected Messages messages;
    protected UiComponentProperties componentProperties;

    protected boolean editable = true;

    public ValueClearAction() {
        this(ID);
    }

    public ValueClearAction(String id) {
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

    @Override
    public void afterPropertiesSet() {
        setShortcut(componentProperties.getPickerClearShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("valuePicker.action.clear.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("valuePicker.action.clear.tooltip"));
        }
    }

    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        this.valuePicker = valuePicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);

        if (editable) {
            setIcon(icons.get(JmixIcon.VALUEPICKER_CLEAR));
        } else {
            setIcon(icons.get(JmixIcon.VALUEPICKER_CLEAR_READONLY));
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

        setIcon(icons.get(JmixIcon.VALUEPICKER_CLEAR));
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
    @Override
    public void execute() {
        // Set the value as if the user had set it
        valuePicker.setValueFromUser(valuePicker.getEmptyValue());
    }
}
