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

package validator.screen;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.screen.ComponentId;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

import java.time.LocalTime;
import java.util.Date;

@Route(value = "validator-test-view")
@UiController("ValidatorTestView")
@UiDescriptor("validator-test-view.xml")
public class ValidatorTestView extends StandardScreen {

    @ComponentId
    public TypedTextField<?> numberField;

    @ComponentId
    public TypedTextField<String> stringField;

    @ComponentId
    public TypedDatePicker<Date> datePicker;

    @ComponentId
    public TypedTimePicker<LocalTime> timePicker;
}
