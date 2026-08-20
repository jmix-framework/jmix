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

import io.jmix.flowui.component.slider.JmixDecimalSlider;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class DecimalSliderLoader extends AbstractComponentLoader<JmixDecimalSlider> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixDecimalSlider createComponent() {
        return factory.create(JmixDecimalSlider.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        loadDouble(element, "min", resultComponent::setMin);
        loadDouble(element, "max", resultComponent::setMax);
        loadDouble(element, "step", resultComponent::setStep);

        loadDouble(element, "value", resultComponent::setValue);

        loadBoolean(element, "valueAlwaysVisible", resultComponent::setValueAlwaysVisible);
        loadBoolean(element, "minMaxVisible", resultComponent::setMinMaxVisible);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAriaLabel(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }

        return dataLoaderSupport;
    }
}
