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
import io.jmix.ui.Actions;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.impl.GuiActionSupport;
import org.apache.commons.lang3.StringUtils;
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

        loadMetaClass(getResultComponent(), element);
        loadActions(getResultComponent(), element);

        if (getResultComponent().getActions().isEmpty()) {
            boolean actionsByMetaAnnotations = createActionsByMetaAnnotations();
            if (!actionsByMetaAnnotations) {
                addDefaultActions();
            }
        }
    }

    protected void loadMetaClass(EntityComboBox resultComponent, Element element) {
        String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            resultComponent.setMetaClass(getMetadata().getClass(metaClass));
        }
    }

    @Override
    protected void loadData(ComboBox component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));
    }

    protected boolean createActionsByMetaAnnotations() {
        return getGuiActionSupport().createActionsByMetaAnnotations(getResultComponent());
    }

    protected void addDefaultActions() {
        Actions actions = getActions();

        getResultComponent().addAction(actions.create(LookupAction.ID));
        getResultComponent().addAction(actions.create(EntityClearAction.ID));
    }

    protected GuiActionSupport getGuiActionSupport() {
        return (GuiActionSupport) applicationContext.getBean(GuiActionSupport.NAME);
    }

    protected Actions getActions() {
        return (Actions) applicationContext.getBean(Actions.NAME);
    }

    protected Metadata getMetadata() {
        return (Metadata) applicationContext.getBean(Metadata.NAME);
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadValuePickerDeclarativeAction(actionsHolder, element);
    }
}
