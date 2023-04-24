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

import io.jmix.core.Metadata;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.exception.GuiDevelopmentException;

public class MultiSelectComboBoxLoader extends AbstractMultiSelectComboBoxLoader<JmixMultiSelectComboBox<?>> {

    @Override
    protected JmixMultiSelectComboBox<?> createComponent() {
        return factory.create(JmixMultiSelectComboBox.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadRequired(resultComponent, element, context);

        super.loadComponent();

        if (resultComponent.getValueSource() == null) {
            loadMetaClass();

            if (resultComponent.getMetaClass() == null) {
                String message = String.format(
                        "%s doesn't have data binding. Set either dataContainer and property or metaClass attribute.",
                        resultComponent.getClass().getSimpleName()
                );

                throw new GuiDevelopmentException(message,
                        context, "Component ID", resultComponent.getId().orElse("null"));
            }
        }
    }

    protected void loadMetaClass() {
        loadString(element, "metaClass")
                .ifPresent(metaClass ->
                        resultComponent.setMetaClass(applicationContext.getBean(Metadata.class).getClass(metaClass)));
    }
}
