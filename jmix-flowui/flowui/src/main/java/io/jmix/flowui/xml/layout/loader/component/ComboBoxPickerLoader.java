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

import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;

public class ComboBoxPickerLoader extends AbstractComboBoxLoader<ComboBoxPicker<?>> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    protected ComboBoxPicker<?> createComponent() {
        return factory.create(ComboBoxPicker.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadBoolean(element, "required", resultComponent::setRequired);

        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);

        getActionLoaderSupport().loadActions(resultComponent, element);
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
