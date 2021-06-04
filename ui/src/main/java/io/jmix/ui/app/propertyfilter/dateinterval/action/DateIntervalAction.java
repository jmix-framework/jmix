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

package io.jmix.ui.app.propertyfilter.dateinterval.action;

import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalDialog;
import io.jmix.ui.builder.AfterScreenCloseEvent;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalUtils;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.StandardOutcome;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;

@Internal
@org.springframework.stereotype.Component("ui_DateIntervalAction")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateIntervalAction extends BaseAction implements ValuePicker.ValuePickerAction, InitializingBean {

    public static final String ID = "value_dateInterval";

    protected Icons icons;
    protected Messages messages;
    protected ScreenBuilders screenBuilders;
    protected DateIntervalUtils dateIntervalUtils;

    protected ValuePicker<BaseDateInterval> valuePicker;
    protected MetaPropertyPath metaPropertyPath;

    protected Subscription valueChangeSubscription;

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

    @Autowired
    public void setDateIntervalUtils(DateIntervalUtils dateIntervalUtils) {
        this.dateIntervalUtils = dateIntervalUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setIcon(icons.get(JmixIcon.VALUEPICKER_DATEINTERVAL));
        setDescription(messages.getMessage("valuePicker.action.dateInterval.tooltip"));
    }

    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        if (this.valuePicker != valuePicker) {
            if (valueChangeSubscription != null) {
                valueChangeSubscription.remove();
                valueChangeSubscription = null;
            }

            //noinspection unchecked
            this.valuePicker = valuePicker;
            if (this.valuePicker != null) {
                valueChangeSubscription = this.valuePicker.addValueChangeListener(this::onValuePickerValueChange);
                this.valuePicker.setFormatter(interval -> dateIntervalUtils.getLocalizedValue(interval));
            }
        }
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

    /**
     * @return meta property path of entity's property for Date Interval
     */
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    /**
     * Sets meta property path of entity's property for Date Interval.
     *
     * @param metaPropertyPath meta property path
     */
    public void setMetaPropertyPath(MetaPropertyPath metaPropertyPath) {
        this.metaPropertyPath = metaPropertyPath;
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
                    .withMetaPropertyPath(metaPropertyPath)
                    .show();
        }
    }

    protected void onDateIntervalDialogCloseEvent(AfterScreenCloseEvent<DateIntervalDialog> closeEvent) {
        if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
            valuePicker.setValueFromUser(closeEvent.getSource().getValue());
        }
    }

    protected void onValuePickerValueChange(HasValue.ValueChangeEvent<BaseDateInterval> event) {
        if (event.getValue() != null) {
            checkValueType(event.getValue());
        }

        valuePicker.setDescription(dateIntervalUtils.getLocalizedValue(event.getValue()));
    }

    protected void checkValueType(BaseDateInterval value) {
        Preconditions.checkNotNullArgument(value);

        if (metaPropertyPath != null) {
            if (!dateIntervalUtils.isIntervalTypeSupportsDatatype(value, metaPropertyPath)) {
                Datatype datatype = metaPropertyPath.getRange().asDatatype();
                throw new IllegalStateException("Date interval with " + value.getType() + " type does not support '"
                        + datatype.getJavaClass() + "'");
            }
        }
    }
}
