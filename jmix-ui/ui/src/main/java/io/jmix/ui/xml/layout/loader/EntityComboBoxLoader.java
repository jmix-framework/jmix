/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import org.dom4j.Element;

public class EntityComboBoxLoader extends ComboBoxLoader {

    @Override
    public void createComponent() {
        resultComponent = factory.create(EntityComboBox.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public EntityComboBox getResultComponent() {
        return (EntityComboBox) super.getResultComponent();
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadCaptionProperty(resultComponent, element);
        loadMetaClass(element, getResultComponent()::setMetaClass);
        loadActions(getResultComponent(), element);

        if (getResultComponent().getActions().isEmpty()) {
            boolean added = addGloballyDefaultActions();
            if (!added) {
                addDefaultActions();
            }
        }
    }

    @Override
    protected void loadData(ComboBox component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));
    }

    protected void addDefaultActions() {
        // no actions by default
    }

    protected boolean addGloballyDefaultActions() {
        return getEntityFieldCreationSupport().addDefaultActions(getResultComponent());
    }

    protected EntityFieldCreationSupport getEntityFieldCreationSupport() {
        return applicationContext.getBean(EntityFieldCreationSupport.class);
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadValuePickerDeclarativeAction(actionsHolder, element);
    }

    @SuppressWarnings("rawtypes")
    protected void loadCaptionProperty(ComboBox resultComponent, Element element) {
        loadString(element, "captionProperty", captionProperty -> {
            resultComponent.setOptionCaptionProvider(
                    new CaptionAdapter(
                            captionProperty,
                            applicationContext.getBean(Metadata.class),
                            applicationContext.getBean(MetadataTools.class)));
        });
    }
}
