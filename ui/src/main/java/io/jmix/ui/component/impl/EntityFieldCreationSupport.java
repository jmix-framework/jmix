/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.entitypicker.OpenCompositionAction;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.OptionsField;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helps to create and configure {@link EntityPicker} and {@link EntityComboBox} components
 * considering {@code jmix.ui.entityFieldType} and {@code jmix.ui.entityFieldActions} properties.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component("ui_EntityFieldCreationSupport")
public class EntityFieldCreationSupport {

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Actions actions;

    public EntityPicker createEntityField(MetaPropertyPath metaPropertyPath, @Nullable Options options) {
        EntityPicker field = createFieldComponent(metaPropertyPath, options);
        createFieldActions(metaPropertyPath, field);
        return field;
    }

    @Nullable
    public ContainerOptions createDefaultContainerOptions(MetaClass metaClass) {
        String componentName = uiProperties.getEntityFieldType().get(metaClass.getName());
        if (EntityComboBox.NAME.equals(componentName)) {
            return new ContainerOptions(createCollectionContainer(metaClass));
        }
        return null;
    }

    public CollectionContainer createCollectionContainer(MetaClass metaClass) {
        CollectionContainer container = dataComponents.createCollectionContainer(metaClass.getJavaClass());
        List list = dataManager.load(metaClass.getJavaClass()).fetchPlan(FetchPlan.INSTANCE_NAME).list(); // todo sorting
        container.setItems(list);
        return container;
    }

    public boolean addDefaultActions(EntityPicker entityPicker) {
        ValueSource valueSource = entityPicker.getValueSource();
        if (!(valueSource instanceof EntityValueSource)) {
            return false;
        }

        EntityValueSource entityValueSource = (EntityValueSource) entityPicker.getValueSource();
        MetaPropertyPath mpp = entityValueSource.getMetaPropertyPath();

        MetaClass metaClass = mpp.getMetaProperty().getRange().asClass();

        List<String> actionIds = uiProperties.getEntityFieldActions().get(metaClass.getName());
        if (actionIds == null || actionIds.isEmpty()) {
            return false;
        }
        addActions(entityPicker, actionIds);
        return true;
    }

    protected EntityPicker createFieldComponent(MetaPropertyPath metaPropertyPath, @Nullable Options options) {
        MetaClass metaClass = metaPropertyPath.getMetaProperty().getRange().asClass();
        String componentName = uiProperties.getEntityFieldType().get(metaClass.getName());

        EntityPicker field;

        if (options != null || EntityComboBox.NAME.equals(componentName)) {
            EntityComboBox entityComboBox = uiComponents.create(EntityComboBox.class);
            entityComboBox.setOptions(options != null ?
                    options :
                    new ContainerOptions(createCollectionContainer(metaClass)));

            field = entityComboBox;

        } else {
            if (componentName == null || EntityPicker.NAME.equals(componentName)) {
                field = uiComponents.create(EntityPicker.class);
            } else {
                EntityPicker component = uiComponents.create(componentName);
                if (component instanceof OptionsField) {
                    ((OptionsField) component).setOptions(new ContainerOptions(createCollectionContainer(metaClass)));
                }
                field = component;
            }
        }
        return field;
    }

    protected void createFieldActions(MetaPropertyPath metaPropertyPath, EntityPicker field) {
        MetaClass metaClass = metaPropertyPath.getMetaProperty().getRange().asClass();
        List<String> actionIds = uiProperties.getEntityFieldActions().get(metaClass.getName());

        if (actionIds == null || actionIds.isEmpty()) {
            if (!(field instanceof EntityComboBox)) {
                if (metaPropertyPath.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
                    field.addAction(actions.create(LookupAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                } else if (metaPropertyPath.getMetaProperty().getType() == MetaProperty.Type.COMPOSITION) {
                    field.addAction(actions.create(OpenCompositionAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                }
            }
        } else {
            addActions(field, actionIds);
        }
    }

    protected void addActions(EntityPicker entityPicker, List<String> actionIds) {
        for (String actionId : actionIds) {
            entityPicker.addAction(actions.create(actionId));
        }
    }
}
