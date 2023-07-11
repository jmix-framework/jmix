/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;

public class MultiSelectComboBoxPickerLoader extends AbstractMultiSelectComboBoxLoader<JmixMultiSelectComboBoxPicker<?>> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    protected JmixMultiSelectComboBoxPicker<?> createComponent() {
        return factory.create(JmixMultiSelectComboBoxPicker.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadRequired(resultComponent, element, context);

        super.loadComponent();

        getActionLoaderSupport().loadActions(resultComponent, element);

        if (resultComponent.getValueSource() == null) {
            componentLoader().loadMetaClass(resultComponent, element);
        }
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
