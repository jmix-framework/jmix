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

package io.jmix.flowui.xml.layout.loader.html;

import com.vaadin.flow.component.html.RangeInput;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class RangeInputLoader extends AbstractComponentLoader<RangeInput> {
    @Override
    protected RangeInput createComponent() {
        return factory.create(RangeInput.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);

        componentLoader().loadSizeAttributes(resultComponent, element);

        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);

        componentLoader().loadValueAndElementAttributes(resultComponent, element);

        loadDouble(element, "min", resultComponent::setMin);
        loadDouble(element, "max", resultComponent::setMax);
        loadDouble(element, "step", resultComponent::setStep);
        loadEnum(element, RangeInput.Orientation.class, "orientation")
                .ifPresent(resultComponent::setOrientation);
    }
}
