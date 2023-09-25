/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.*;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("prefix-suffix-view")
@ViewController
@ViewDescriptor("prefix-suffix-view.xml")
public class PrefixSuffixView extends StandardView {

    @ViewComponent
    public JmixBigDecimalField bigDecimalField;
    @ViewComponent
    public JmixButton button;
    @ViewComponent
    public JmixEmailField emailField;
    @ViewComponent
    public EntityPicker<?> entityPicker;
    @ViewComponent
    public JmixIntegerField integerField;
    @ViewComponent
    public JmixMultiValuePicker<?> multiValuePicker;
    @ViewComponent
    public JmixNumberField numberField;
    @ViewComponent
    public JmixPasswordField passwordField;
    @ViewComponent
    public JmixTextArea textArea;
    @ViewComponent
    public TypedTextField<String> textField;
    @ViewComponent
    public JmixValuePicker<?> valuePicker;
    @ViewComponent
    public JmixTabSheet tabSheet;

    @ViewComponent
    public JmixComboBox<?> comboBox;
    @ViewComponent
    public TypedDatePicker<?> datePicker;
    @ViewComponent
    public TypedDateTimePicker<?> dateTimePicker;
    @ViewComponent
    public EntityComboBox<?> entityComboBox;
    @ViewComponent
    public JmixSelect<?> select;
    @ViewComponent
    public TypedTimePicker<?> timePicker;
}
