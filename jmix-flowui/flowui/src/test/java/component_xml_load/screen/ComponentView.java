/*
 * Copyright 2022 Haulmont.
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

package component_xml_load.screen;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.component.textfield.JmixNumberField;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

@Route(value = "component-view")
@ViewController("ComponentView")
@ViewDescriptor("component-view.xml")
public class ComponentView extends StandardView {

    public void loadData() {
        comboBoxPickerId.setItems("comboBoxPickerItem1", "comboBoxPickerItem2");
        comboBoxId.setItems("number", "notNumber");
        radioButtonGroupId.setItems("radioButton1", "radioButton2", "radioButton3");
        selectId.setItems("select1", "select2", "select3");

        getViewData().loadAll();
    }

    @ViewComponent
    public Action buttonAction;

    @ViewComponent
    public InstanceContainer<Order> orderDc;

    @ViewComponent
    public Avatar avatarId;

    @ViewComponent
    public JmixBigDecimalField bigDecimalFieldId;

    @ViewComponent
    public JmixBigDecimalField bigDecimalFieldWithValueId;

    @ViewComponent
    public JmixButton buttonId;

    @ViewComponent
    public JmixButton buttonWithActionId;

    @ViewComponent
    public JmixCheckbox checkBoxId;

    @ViewComponent
    public JmixCheckbox checkBoxWithDataId;

    @ViewComponent
    public JmixComboBox<String> comboBoxId;

    @ViewComponent
    public TypedDatePicker<?> datePickerId;

    @ViewComponent
    public TypedTimePicker<?> timePickerId;

    @ViewComponent
    public TypedDateTimePicker<?> dateTimePickerId;

    @ViewComponent
    public Details detailsId;

    @ViewComponent
    public JmixNumberField numberFieldId;

    @ViewComponent
    public JmixNumberField numberFieldWithValueId;

    @ViewComponent
    public JmixPasswordField passwordFieldId;

    @ViewComponent
    public JmixPasswordField passwordFieldWithValueId;

    @ViewComponent
    public ProgressBar progressBarId;

    @ViewComponent
    public JmixRadioButtonGroup<String> radioButtonGroupId;

    @ViewComponent
    public JmixSelect<String> selectId;

    @ViewComponent
    public JmixTextArea textAreaId;

    @ViewComponent
    public JmixTextArea textAreaWithValueId;

    @ViewComponent
    public TypedTextField<Integer> textFieldId;

    @ViewComponent
    public TypedTextField<String> textFieldWithValueId;

    @ViewComponent
    public JmixValuePicker<?> valuePickerId;

    @ViewComponent
    public JmixMultiValuePicker<?> valuesPickerId;

    @ViewComponent
    public EntityPicker<?> entityPickerId;

    @ViewComponent
    public EntityPicker<?> metaClassEntityPickerId;

    @ViewComponent
    public ComboBoxPicker<String> comboBoxPickerId;

    @ViewComponent
    public EntityComboBox<?> entityComboBoxId;

    @ViewComponent
    public EntityComboBox<?> metaClassComboBoxId;

    @ViewComponent
    public DropdownButton dropdownButtonId;

    @ViewComponent
    public ComboButton comboButtonId;
}
