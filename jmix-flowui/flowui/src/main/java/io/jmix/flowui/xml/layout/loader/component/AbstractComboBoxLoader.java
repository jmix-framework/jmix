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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public abstract class AbstractComboBoxLoader<T extends ComboBox<?>> extends AbstractComponentLoader<T> {

    @Override
    public void loadComponent() {
        loadBoolean(element, "opened", resultComponent::setOpened);
        loadString(element, "pattern", resultComponent::setPattern);
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadBoolean(element, "allowCustomValue", resultComponent::setAllowCustomValue);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAllowedCharPattern(resultComponent, element, context);
    }
}
