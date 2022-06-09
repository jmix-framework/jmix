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
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.component.valuepicker.JmixValuesPicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.screen.ComponentId;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;
import test_support.entity.sales.Order;

@Route(value = "component-view")
@UiController("ComponentView")
@UiDescriptor("component-view.xml")
public class ComponentView extends StandardScreen {

    public void loadData() {
        comboBoxPickerId.setItems("comboBoxPickerItem1", "comboBoxPickerItem2");
        comboBoxId.setItems("number", "notNumber");
        radioButtonGroupId.setItems("radioButton1", "radioButton2", "radioButton3");
        selectId.setItems("select1", "select2", "select3");

        getScreenData().loadAll();
    }

    @ComponentId
    public Action buttonAction;

    @ComponentId
    public InstanceContainer<Order> orderDc;

    @ComponentId
    public Avatar avatarId;

    @ComponentId
    public JmixBigDecimalField bigDecimalFieldId;

    @ComponentId
    public JmixBigDecimalField bigDecimalFieldWithValueId;

    @ComponentId
    public JmixButton buttonId;

    @ComponentId
    public JmixButton buttonWithActionId;

    @ComponentId
    public JmixCheckbox checkBoxId;

    @ComponentId
    public JmixCheckbox checkBoxWithDataId;

    @ComponentId
    public JmixComboBox<String> comboBoxId;

    @ComponentId
    public TypedDatePicker<?> datePickerId;

    @ComponentId
    public TypedTimePicker<?> timePickerId;

    @ComponentId
    public TypedDateTimePicker<?> dateTimePickerId;

    @ComponentId
    public Details detailsId;

    @ComponentId
    public EmailField emailFieldId;

    @ComponentId
    public EmailField emailFieldWithValueId;

    @ComponentId
    public NumberField numberFieldId;

    @ComponentId
    public NumberField numberFieldWithValueId;

    @ComponentId
    public PasswordField passwordFieldId;

    @ComponentId
    public PasswordField passwordFieldWithValueId;

    @ComponentId
    public ProgressBar progressBarId;

    @ComponentId
    public RadioButtonGroup<String> radioButtonGroupId;

    @ComponentId
    public Select<String> selectId;

    @ComponentId
    public TextArea textAreaId;

    @ComponentId
    public TextArea textAreaWithValueId;

    @ComponentId
    public TypedTextField<Integer> textFieldId;

    @ComponentId
    public TypedTextField<String> textFieldWithValueId;

    @ComponentId
    public JmixValuePicker<?> valuePickerId;

    @ComponentId
    public JmixValuesPicker<?> valuesPickerId;

    @ComponentId
    public EntityPicker<?> entityPickerId;

    @ComponentId
    public EntityPicker<?> metaClassEntityPickerId;

    @ComponentId
    public ComboBoxPicker<String> comboBoxPickerId;

    @ComponentId
    public EntityComboBox<?> entityComboBoxId;

    @ComponentId
    public EntityComboBox<?> metaClassComboBoxId;
}
