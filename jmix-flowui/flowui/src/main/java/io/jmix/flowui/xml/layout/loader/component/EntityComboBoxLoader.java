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
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class EntityComboBoxLoader extends AbstractComboBoxLoader<EntityComboBox<?>> {

    protected ActionLoaderSupport actionLoaderSupport;
    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected EntityComboBox<?> createComponent() {
        return factory.create(EntityComboBox.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        getDataLoaderSupport().loadItems(resultComponent, element);
        getDataLoaderSupport().loadData(resultComponent, element);

        //These properties are loaded after the data provider is loaded,
        // because setting the data provider resets the value of the readOnly attribute to default.
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadRequired(resultComponent, element, context);

        getActionLoaderSupport().loadActions(resultComponent, element);

        if (resultComponent.getValueSource() == null) {
            loadMetaClass();

            if (resultComponent.getMetaClass() == null) {
                throw new GuiDevelopmentException(
                        String.format("%s doesn't have data binding", resultComponent.getClass().getSimpleName()),
                        context, "Component ID", resultComponent.getId().orElse("null"));
            }
        }
    }

    protected void loadMetaClass() {
        loadString(element, "metaClass")
                .ifPresent(metaClass ->
                        resultComponent.setMetaClass(applicationContext.getBean(Metadata.class).getClass(metaClass)));
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
