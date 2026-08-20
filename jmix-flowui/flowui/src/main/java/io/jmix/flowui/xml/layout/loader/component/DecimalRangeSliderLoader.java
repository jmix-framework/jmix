/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.slider.DecimalRangeSlider;
import com.vaadin.flow.component.slider.DecimalRangeSliderValue;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

import java.util.Optional;

public class DecimalRangeSliderLoader extends AbstractComponentLoader<DecimalRangeSlider> {

    @Override
    protected DecimalRangeSlider createComponent() {
        return factory.create(DecimalRangeSlider.class);
    }

    @Override
    public void loadComponent() {
        loadDouble(element, "min", resultComponent::setMin);
        loadDouble(element, "max", resultComponent::setMax);
        loadDouble(element, "step", resultComponent::setStep);

        loadValue();

        loadBoolean(element, "valueAlwaysVisible", resultComponent::setValueAlwaysVisible);
        loadBoolean(element, "minMaxVisible", resultComponent::setMinMaxVisible);
        loadResourceString(element, "accessibleNameStart", context.getMessageGroup(),
                resultComponent::setAccessibleNameStart);
        loadResourceString(element, "accessibleNameEnd", context.getMessageGroup(),
                resultComponent::setAccessibleNameEnd);

        loadBoolean(element, "required", resultComponent::setRequiredIndicatorVisible);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
    }

    protected void loadValue() {
        Optional<Double> startValue = loadDouble(element, "startValue");
        Optional<Double> endValue = loadDouble(element, "endValue");

        if (startValue.isPresent() || endValue.isPresent()) {
            resultComponent.setValue(new DecimalRangeSliderValue(
                    startValue.orElseGet(resultComponent::getMin),
                    endValue.orElseGet(resultComponent::getMax)));
        }
    }
}
