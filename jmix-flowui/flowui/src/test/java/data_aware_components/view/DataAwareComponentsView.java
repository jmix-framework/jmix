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

package data_aware_components.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.JmixEmailField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.time.OffsetTime;
import java.util.Date;

@Route(value = "data-aware-components-view")
@ViewController("DataAwareComponentsView")
@ViewDescriptor("data-aware-components-view.xml")
public class DataAwareComponentsView extends StandardView {

    @ViewComponent
    public TypedTextField<String> sizeTextField;
    @ViewComponent
    public JmixTextArea sizeTextArea;
    @ViewComponent
    public JmixEmailField sizeEmailField;

    @ViewComponent
    public TypedTextField<String> lengthTextField;
    @ViewComponent
    public JmixTextArea lengthTextArea;
    @ViewComponent
    public JmixEmailField lengthEmailField;

    @ViewComponent
    public TypedDateTimePicker<Date> zoneDateTimePicker;
    @ViewComponent
    public TypedTimePicker<Date> zoneTimePicker;

    @ViewComponent
    public TypedDatePicker<java.sql.Date> rangeDatePicker;
    @ViewComponent
    public TypedTimePicker<OffsetTime> rangeTimePicker;
    @ViewComponent
    public TypedDateTimePicker<Date> rangeDateTimePicker;
}
