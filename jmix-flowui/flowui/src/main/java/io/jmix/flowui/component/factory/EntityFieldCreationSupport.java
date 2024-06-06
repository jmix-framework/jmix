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

import com.vaadin.flow.component.Component;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
@org.springframework.stereotype.Component("flowui_EntityFieldCreationSupport")
public class EntityFieldCreationSupport {

    protected final UiComponents uiComponents;
    protected final Actions actions;
    protected final MetadataTools metadataTools;
    protected final UiComponentProperties componentProperties;
    protected final DataComponents dataComponents;
    protected final DataManager dataManager;
    protected final ApplicationContext applicationContext;

    public EntityFieldCreationSupport(UiComponents uiComponents,
                                      Actions actions,
                                      MetadataTools metadataTools,
                                      UiComponentProperties componentProperties,
                                      DataComponents dataComponents,
                                      DataManager dataManager, ApplicationContext applicationContext) {
        this.uiComponents = uiComponents;
        this.actions = actions;
        this.metadataTools = metadataTools;
        this.componentProperties = componentProperties;
        this.dataComponents = dataComponents;
        this.dataManager = dataManager;
        this.applicationContext = applicationContext;
    }

    @Nullable
    public Component createEntityField(ComponentGenerationContext context) {
        return createEntityField(context, false);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Component createEntityField(ComponentGenerationContext context,
                                       boolean considerComposition) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(
                metaClass,
                context.getProperty()
        );

        if (metaPropertyPath == null) {
            return null;
        }

        MetaClass propertyMetaClass = metaPropertyPath.getMetaProperty().getRange().asClass();
        CollectionContainer<?> collectionItems = context.getCollectionItems();

        EntityPickerComponent field;

        String componentFqn = componentProperties.getEntityFieldFqn().get(propertyMetaClass.getName());

        if (componentFqn != null) {
            Class<?> aClass = applicationContext.getBean(ClassManager.class).loadClass(componentFqn);
            if (!Component.class.isAssignableFrom(aClass)) {
                throw new DevelopmentException("Class '%s' is not a component".formatted(componentFqn));
            }

            // cast is required for the compiler
            //noinspection RedundantCast
            field = (Component & EntityPickerComponent) uiComponents.create(aClass.asSubclass(Component.class));
        } else if (collectionItems != null) {
            field = uiComponents.create(EntityComboBox.class);
        } else {
            field = uiComponents.create(EntityPicker.class);
        }

        if (field instanceof SupportsItemsContainer supportsItemsContainer) {
            if (collectionItems != null && !collectionItems.getEntityMetaClass().equals(propertyMetaClass)) {
                throw new IllegalStateException("Wrong collection metaClass provided for container");
            }

            supportsItemsContainer.setItems(
                    collectionItems != null
                            ? collectionItems
                            : createCollectionContainer(propertyMetaClass)

            );
        }

        field.setMetaClass(propertyMetaClass);
        createFieldActions(propertyMetaClass, metaPropertyPath.getMetaProperty().getType(), field, considerComposition);

        return (Component) field;
    }

    protected void createFieldActions(MetaClass metaClass, MetaProperty.Type metaPropertyType,
                                      EntityPickerComponent field, boolean considerComposition) {
        List<String> actionIds = componentProperties.getEntityFieldActions().get(metaClass.getName());

        if (actionIds == null || actionIds.isEmpty()) {
            if (!(field instanceof EntityComboBox)) {
                if (metaPropertyType == MetaProperty.Type.ASSOCIATION || considerComposition) {
                    field.addAction(actions.create(EntityLookupAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                } else if (metaPropertyType == MetaProperty.Type.COMPOSITION) {
                    field.addAction(actions.create(EntityOpenCompositionAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                }
            }
        } else {
            for (String actionId : actionIds) {
                field.addAction(actions.create(actionId));
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected CollectionContainer createCollectionContainer(MetaClass metaClass) {
        CollectionContainer container = dataComponents.createCollectionContainer(metaClass.getJavaClass());

        List list = dataManager.load(metaClass.getJavaClass())
                .all()
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .sort(Sort.by(getInstanceNameSortOrders(metaClass)))
                .list();
        container.setItems(list);
        return container;
    }

    protected List<Sort.Order> getInstanceNameSortOrders(MetaClass metaClass) {
        return metadataTools.getInstanceNameRelatedProperties(metaClass, true)
                .stream()
                .filter(metaProperty -> !metaProperty.getRange().isClass())
                .map(metaProperty -> Sort.Order.asc(metaProperty.getName()))
                .collect(Collectors.toList());
    }
}
