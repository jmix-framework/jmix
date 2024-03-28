/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.action.valuepicker;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.app.propertyfilter.dateinterval.DateIntervalDialog;
import io.jmix.flowui.app.propertyfilter.dateinterval.DateIntervalSupport;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

@Internal
@ActionType(DateIntervalAction.ID)
public class DateIntervalAction
        extends PickerAction<DateIntervalAction, PickerComponent<BaseDateInterval>, BaseDateInterval> {

    public static final String ID = "value_dateInterval";

    protected DateIntervalSupport dateIntervalSupport;
    protected DialogWindows dialogWindows;

    protected MetaPropertyPath metaPropertyPath;

    protected Registration valueChangeSubscription;

    public DateIntervalAction() {
        super(DateIntervalAction.ID);
    }

    public DateIntervalAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.valuePicker.dateInterval.description");
    }

    @Autowired
    protected void setDateIntervalSupport(DateIntervalSupport dateIntervalSupport) {
        this.dateIntervalSupport = dateIntervalSupport;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
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
    public void execute() {
        View<?> origin = UiComponentUtils.getView((Component) target);

        // dialog is opened using id because the view can be overridden by the flowuidata module
        dialogWindows.view(origin, DateIntervalDialog.class)
                .withViewId("flowui_DateIntervalDialog")
                .withViewConfigurer(this::dateIntervalDialogConfigurer)
                .withAfterCloseListener(this::onDateIntervalDialogClose)
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        if (valueChangeSubscription != null) {
            valueChangeSubscription.remove();
            valueChangeSubscription = null;
        }

        if (target != null) {
            //noinspection unchecked
            ValuePicker<BaseDateInterval> valuePicker = (ValuePicker<BaseDateInterval>) target;
            valueChangeSubscription = valuePicker.addValueChangeListener(this::onValuePickerValueChange);
            valuePicker.setFormatter(value -> dateIntervalSupport.getLocalizedValue(value));
        }
    }

    protected void dateIntervalDialogConfigurer(DateIntervalDialog dateIntervalDialog) {
        //noinspection unchecked
        dateIntervalDialog.setValue(((HasValue<?, BaseDateInterval>) target).getValue());
        dateIntervalDialog.setMetaPropertyPath(metaPropertyPath);
    }

    protected void onDateIntervalDialogClose(DialogWindow.AfterCloseEvent<DateIntervalDialog> event) {
        if (event.closedWith(StandardOutcome.SAVE)) {
            target.setValueFromClient(event.getView().getValue());
        }
    }

    protected void onValuePickerValueChange(
            ComponentValueChangeEvent<ValuePicker<BaseDateInterval>, BaseDateInterval> event) {
        if (event.getValue() != null) {
            checkValueType(event.getValue());
        }

        event.getSource().setTooltipText(dateIntervalSupport.getLocalizedValue(event.getValue()));
    }

    protected void checkValueType(BaseDateInterval value) {
        Preconditions.checkNotNullArgument(value);

        if (metaPropertyPath != null &&
                !dateIntervalSupport.isIntervalTypeSupportsDatatype(value, metaPropertyPath)) {
            Datatype<?> datatype = metaPropertyPath.getRange().asDatatype();
            throw new IllegalStateException(
                    "Date interval with %s type does not support '%s'".formatted(value.getType(), datatype.getJavaClass())
            );
        }
    }
}
