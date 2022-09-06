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

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.util.Date;

@Route(value = "validator-view")
@ViewController("ValidatorView")
@ViewDescriptor("validator-view.xml")
public class ValidatorView extends StandardView {

    @ViewComponent
    public BigDecimalField decimalField;

    @ViewComponent
    public BigDecimalField digitsField;

    @ViewComponent
    public TypedTextField<Double> doubleField;

    @ViewComponent
    public TypedTextField<String> emailField;

    @ViewComponent
    public TypedDatePicker<Date> futureOrPresentField;

    @ViewComponent
    public TypedDatePicker<Date> futureField;

    @ViewComponent
    public TypedTextField<Integer> integerField;

    @ViewComponent
    public TypedTextField<Integer> negativeOrZeroField;

    @ViewComponent
    public TypedTextField<Integer> negativeField;

    @ViewComponent
    public TypedTextField<String> notBlankField;

    @ViewComponent
    public TypedTextField<String> notEmptyField;

    @ViewComponent
    public TypedDatePicker<Date> notNullField;

    @ViewComponent
    public TypedDatePicker<Date> pastOrPresentField;

    @ViewComponent
    public TypedDatePicker<Date> pastField;

    @ViewComponent
    public TypedTextField<Integer> positiveOrZeroField;

    @ViewComponent
    public TypedTextField<Integer> positiveField;

    @ViewComponent
    public TypedTextField<String> regexpField;

    @ViewComponent
    public TypedTextField<String> sizeField;
}
