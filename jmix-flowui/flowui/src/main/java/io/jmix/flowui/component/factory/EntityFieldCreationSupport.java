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

package io.jmix.flowui.component.factory;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import org.springframework.stereotype.Component;

@SuppressWarnings({"rawtypes"})
@Component("flowui_EntityFieldCreationSupport")
public class EntityFieldCreationSupport {

    protected final UiComponents uiComponents;
    protected final Actions actions;

    public EntityFieldCreationSupport(UiComponents uiComponents,
                                      Actions actions) {
        this.uiComponents = uiComponents;
        this.actions = actions;
    }

    public com.vaadin.flow.component.Component createEntityField(MetaPropertyPath metaPropertyPath) {
        MetaClass metaClass = metaPropertyPath.getMetaProperty().getRange().asClass();

        com.vaadin.flow.component.Component field;

        EntityPicker component = uiComponents.create(EntityPicker.class);
        component.setMetaClass(metaClass);
        createFieldActions(metaPropertyPath.getMetaProperty().getType(), component);
        field = component;

        return field;
    }

    protected void createFieldActions(MetaProperty.Type metaPropertyType, EntityPickerComponent field) {
        if (!(field instanceof EntityComboBox)) {
            if (metaPropertyType == MetaProperty.Type.ASSOCIATION) {
                field.addAction(actions.create(EntityLookupAction.ID));
                field.addAction(actions.create(EntityClearAction.ID));
            } else if (metaPropertyType == MetaProperty.Type.COMPOSITION) {
                field.addAction(actions.create(EntityOpenCompositionAction.ID));
                field.addAction(actions.create(EntityClearAction.ID));
            }
        }
    }
}
