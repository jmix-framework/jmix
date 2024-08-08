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

package io.jmix.fullcalendarflowui.action;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.DaysOfWeek;
import io.jmix.fullcalendar.datatype.DaysOfWeekDatatypeUtils;
import io.jmix.fullcalendarflowui.FullCalendarFlowuiProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.time.temporal.WeekFields;
import java.util.*;

@ActionType(DaysOfWeekEditAction.ID)
public class DaysOfWeekEditAction extends PickerAction<DaysOfWeekEditAction, PickerComponent<DaysOfWeek>, DaysOfWeek>
        implements InitializingBean {
    public static final String ID = "fullcalendar_daysOfWeekEdit";

    protected Messages messages;
    protected FullCalendarFlowuiProperties calendarFlowuiProperties;
    protected CurrentAuthentication currentAuthentication;

    public DaysOfWeekEditAction() {
        this(ID);
    }

    public DaysOfWeekEditAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setCalendarFlowuiProperties(FullCalendarFlowuiProperties calendarFlowuiProperties) {
        this.calendarFlowuiProperties = calendarFlowuiProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setShortcutCombination(KeyCombination.create(calendarFlowuiProperties.getPickerDaysOfWeekEditShortcut()));

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage(
                    "io.jmix.fullcalendarflowui.action", "daysOfWeekEdit.description")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage(
                    "io.jmix.fullcalendarflowui.action", "daysOfWeekEdit.description"));
        }
    }

    @Override
    protected void initAction() {
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        if (getTarget() == null) {
            throw new IllegalStateException("N target component is set");
        }
        DaysOfWeek value = ((HasValueAndElement<?, DaysOfWeek>) getTarget()).getValue();

        Dialog editorDialog = createEditorDialog(value);

        editorDialog.open();
    }

    protected Dialog createEditorDialog(@Nullable DaysOfWeek daysOfWeek) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messages.getMessage(
                "io.jmix.fullcalendarflowui.action", "daysOfWeekSelectDialog.headerTitle"));

        CheckboxGroup<DayOfWeek> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems(getAllDaysOfWeek());
        checkboxGroup.setItemLabelGenerator(item -> messages.getMessage(item));
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        if (daysOfWeek != null) {
            checkboxGroup.setValue(new HashSet<>(daysOfWeek.getDaysOfWeek()));
        }

        dialog.add(checkboxGroup);

        Button okBtn = new Button(messages.getMessage("actions.Ok"));
        okBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okBtn.addClickListener(__ -> onOkBtnClick(dialog, checkboxGroup.getValue()));

        Button cancelBtn = new Button(messages.getMessage("actions.Cancel"));
        cancelBtn.addClickListener(__ -> onCancelBtnClick(dialog));

        dialog.getFooter().add(okBtn, cancelBtn);

        return dialog;
    }

    protected void onOkBtnClick(Dialog dialog, Set<DayOfWeek> selectedValues) {
        target.setValueFromClient(new DaysOfWeek(selectedValues));
        dialog.close();
    }

    protected void onCancelBtnClick(Dialog dialog) {
        dialog.close();
    }

    protected List<DayOfWeek> getAllDaysOfWeek() {
        Locale locale = currentAuthentication.getLocale();

        DayOfWeek firstDay = DayOfWeek.fromDayOfWeek(WeekFields.of(locale).getFirstDayOfWeek());

        return DaysOfWeekDatatypeUtils.getOrderedByFirstDay(firstDay);
    }
}
