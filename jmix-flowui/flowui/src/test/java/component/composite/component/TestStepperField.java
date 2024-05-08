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

package component.composite.component;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.component.textfield.TypedTextField;
import org.springframework.beans.factory.annotation.Autowired;

//@CompositeDescriptor("test-stepper-field.xml")
public class TestStepperField extends CompositeComponent<HorizontalLayout>
        implements HasValue<ComponentValueChangeEvent<TestStepperField, Integer>, Integer> {

//    private UiComponents uiComponents;

    private TypedTextField<Integer> valueField;
    private Button upBtn;
    private Button downBtn;

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout horizontalLayout = super.initContent();
        horizontalLayout.setWidthFull();

        valueField = uiComponents.create(TypedTextField.class);
        valueField.setWidthFull();

        upBtn = uiComponents.create(Button.class);
        upBtn.setIcon(VaadinIcon.CHEVRON_UP.create());
        upBtn.addClickListener(__ -> updateValue(1));

        downBtn = uiComponents.create(Button.class);
        downBtn.setIcon(VaadinIcon.CHEVRON_DOWN.create());
        downBtn.addClickListener(__ -> updateValue(-1));

        return horizontalLayout;
    }

    private void updateValue(int adjustment) {
        Integer currentValue = getValue();
        setValue(currentValue != null ? currentValue + adjustment : adjustment);
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
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<TestStepperField, Integer>> listener) {
        return valueField.addTypedValueChangeListener(event ->
                listener.valueChanged(new ComponentValueChangeEvent<>(this, this, event.getOldValue(), event.isFromClient())));
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
