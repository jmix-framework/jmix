/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.action.propertyfilter;

import io.jmix.core.Messages;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalDialog;
import io.jmix.ui.app.propertyfilter.dateinterval.BaseDateInterval;
import io.jmix.ui.builder.AfterScreenCloseEvent;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardOutcome;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ActionType(DateIntervalAction.ID)
public class DateIntervalAction extends BaseAction implements ValuePicker.ValuePickerAction, InitializingBean {

    public static final String ID = "value_dateInterval";

    protected Icons icons;
    protected Messages messages;
    protected ScreenBuilders screenBuilders;

    protected ValuePicker<BaseDateInterval> valuePicker;

    protected boolean editable = true;

    public DateIntervalAction() {
        super(DateIntervalAction.ID);
    }

    public DateIntervalAction(String id) {
        super(id);
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setIcon(icons.get(JmixIcon.VALUEPICKER_DATEINTERVAL));
        setDescription(messages.getMessage("valuePicker.action.dateInterval.tooltip"));
    }

    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        //noinspection unchecked
        this.valuePicker = valuePicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);

        if (editable) {
            setIcon(icons.get(JmixIcon.VALUEPICKER_DATEINTERVAL));
        } else {
            setIcon(icons.get(JmixIcon.VALUEPICKER_DATEINTERVAL_READONLY));
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

    @Override
    public void actionPerform(Component component) {
        super.actionPerform(component);

        if (component instanceof Component.BelongToFrame) {
            Frame frame = ((Component.BelongToFrame) component).getFrame();
            if (frame == null) {
                throw new IllegalStateException("Component is not attached to the frame");
            }

            screenBuilders.screen(frame.getFrameOwner())
                    .withScreenClass(DateIntervalDialog.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .withAfterCloseListener(this::onDateIntervalDialogCloseEvent)
                    .build()
                    .withValue(valuePicker.getValue())
                    .show();
        }
    }

    protected void onDateIntervalDialogCloseEvent(AfterScreenCloseEvent<DateIntervalDialog> closeEvent) {
        if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
            valuePicker.setValueFromUser(closeEvent.getSource().getValue());
        }
    }
}
