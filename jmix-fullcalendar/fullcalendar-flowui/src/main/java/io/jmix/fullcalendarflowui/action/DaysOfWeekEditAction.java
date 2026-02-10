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
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.fullcalendarflowui.FullCalendarProperties;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.DaysOfWeek;
import io.jmix.fullcalendarflowui.datatype.DaysOfWeekDatatypeUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Action is used for editing {@link DaysOfWeek} datatype in {@link ValuePicker} field.
 */
@ActionType(DaysOfWeekEditAction.ID)
public class DaysOfWeekEditAction extends PickerAction<DaysOfWeekEditAction, PickerComponent<DaysOfWeek>, DaysOfWeek>
        implements InitializingBean {

    public static final String ID = "fcalen_daysOfWeekEdit";

    protected Icons icons;
    protected Messages messages;
    protected FullCalendarProperties calendarFlowuiProperties;
    protected CurrentAuthentication currentAuthentication;
    protected UiComponents uiComponents;

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
    public void setIcons(Icons icons) {
        this.icons = icons;
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.DAYS_OF_WEEK_EDIT_ACTION);
        }
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setCalendarFlowuiProperties(FullCalendarProperties calendarFlowuiProperties) {
        this.calendarFlowuiProperties = calendarFlowuiProperties;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setShortcutCombination(KeyCombination.create(calendarFlowuiProperties.getPickerDaysOfWeekEditShortcut()));

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage(getClass(), "daysOfWeekEdit.description")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage(getClass(), "daysOfWeekEdit.description"));
        }
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

    @SuppressWarnings("unchecked")
    protected Dialog createEditorDialog(@Nullable DaysOfWeek daysOfWeek) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messages.getMessage(
                "io.jmix.fullcalendarflowui.action", "daysOfWeekSelectDialog.headerTitle"));
        dialog.getHeader().add(createHeaderCloseButton(dialog));

        JmixCheckboxGroup<DayOfWeek> checkboxGroup = uiComponents.create(JmixCheckboxGroup.class);
        checkboxGroup.setItems(getAllDaysOfWeek());
        checkboxGroup.setItemLabelGenerator(item -> messages.getMessage(item));
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        if (daysOfWeek != null) {
            checkboxGroup.setValue(new HashSet<>(daysOfWeek.getDaysOfWeek()));
        }

        dialog.add(checkboxGroup);

        JmixButton okBtn = uiComponents.create(JmixButton.class);
        okBtn.setText(messages.getMessage("actions.Ok"));
        okBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okBtn.addClickListener(__ -> onOkBtnClick(dialog, checkboxGroup.getValue()));

        JmixButton cancelBtn = uiComponents.create(JmixButton.class);
        cancelBtn.setText(messages.getMessage("actions.Cancel"));
        cancelBtn.addClickListener(__ -> onCancelBtnClick(dialog));

        dialog.getFooter().add(okBtn, cancelBtn);

        return dialog;
    }

    protected Button createHeaderCloseButton(Dialog dialog) {
        JmixButton closeButton = uiComponents.create(JmixButton.class);
        closeButton.setIcon(icons.get(JmixFontIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_CONTRAST
        );
        closeButton.setTitle(messages.getMessage("actions.Close"));
        closeButton.addClickListener(__ -> onCloseButtonClick(dialog));
        return closeButton;
    }

    protected void onOkBtnClick(Dialog dialog, Set<DayOfWeek> selectedValues) {
        target.setValueFromClient(new DaysOfWeek(selectedValues));
        dialog.close();
    }

    protected void onCancelBtnClick(Dialog dialog) {
        dialog.close();
    }

    protected void onCloseButtonClick(Dialog dialog) {
        dialog.close();
    }

    protected List<DayOfWeek> getAllDaysOfWeek() {
        Locale locale = currentAuthentication.getLocale();

        DayOfWeek firstDay = DayOfWeek.fromDayOfWeek(WeekFields.of(locale).getFirstDayOfWeek());

        return DaysOfWeekDatatypeUtils.getOrderedByFirstDay(firstDay);
    }
}
