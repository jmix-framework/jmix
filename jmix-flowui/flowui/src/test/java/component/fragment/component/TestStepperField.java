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

package component.fragment.component;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.component.textfield.TypedTextField;

@FragmentDescriptor("/component/fragment/component/test-stepper-field.xml")
public class TestStepperField extends Fragment<HorizontalLayout>
        implements HasValue<ComponentValueChangeEvent<TestStepperField, Integer>, Integer> {

    private TypedTextField<Integer> valueField;
    private Button upBtn;
    private Button downBtn;

    public TestStepperField() {
        addReadyListener(this::onReady);
    }

    private void onReady(ReadyEvent readyEvent) {
        valueField = getInnerComponent("valueField");
        setValue(0);

        upBtn = getInnerComponent("upBtn");
        upBtn.addClickListener(__ -> updateValue(1));

        downBtn = getInnerComponent("downBtn");
        downBtn.addClickListener(__ -> updateValue(-1));
    }

    private void updateValue(int adjustment) {
        Integer currentValue = getValue();
        setValue(currentValue != null ? currentValue + adjustment : adjustment);
    }

    public void clickUp() {
        upBtn.click();
    }

    public void clickDown() {
        downBtn.click();
    }

    @Override
    public void setValue(Integer value) {
        valueField.setTypedValue(value);
    }

    @Override
    public Integer getValue() {
        return valueField.getTypedValue();
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<TestStepperField, Integer>> listener) {
        return valueField.addTypedValueChangeListener(event ->
                listener.valueChanged(new ComponentValueChangeEvent<>(this, this,
                        event.getOldValue(), event.isFromClient())));
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        valueField.setReadOnly(readOnly);
        upBtn.setEnabled(!readOnly);
        downBtn.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return valueField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        valueField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return valueField.isRequiredIndicatorVisible();
    }
}
