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
import io.jmix.flowui.view.ComponentId;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;

import java.util.Date;

@Route(value = "validator-view")
@UiController("ValidatorView")
@UiDescriptor("validator-view.xml")
public class ValidatorView extends StandardView {

    @ComponentId
    public BigDecimalField decimalField;

    @ComponentId
    public BigDecimalField digitsField;

    @ComponentId
    public TypedTextField<Double> doubleField;

    @ComponentId
    public TypedTextField<String> emailField;

    @ComponentId
    public TypedDatePicker<Date> futureOrPresentField;

    @ComponentId
    public TypedDatePicker<Date> futureField;

    @ComponentId
    public TypedTextField<Integer> integerField;

    @ComponentId
    public TypedTextField<Integer> negativeOrZeroField;

    @ComponentId
    public TypedTextField<Integer> negativeField;

    @ComponentId
    public TypedTextField<String> notBlankField;

    @ComponentId
    public TypedTextField<String> notEmptyField;

    @ComponentId
    public TypedDatePicker<Date> notNullField;

    @ComponentId
    public TypedDatePicker<Date> pastOrPresentField;

    @ComponentId
    public TypedDatePicker<Date> pastField;

    @ComponentId
    public TypedTextField<Integer> positiveOrZeroField;

    @ComponentId
    public TypedTextField<Integer> positiveField;

    @ComponentId
    public TypedTextField<String> regexpField;

    @ComponentId
    public TypedTextField<String> sizeField;
}
