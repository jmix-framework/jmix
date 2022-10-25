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

package io.jmix.datatoolsflowui.view.entityinspector.assistant;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.core.AccessManager;
import io.jmix.core.EntityStates;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.datatoolsflowui.action.EntityInspectorLookupAction;
import io.jmix.datatoolsflowui.view.entityinspector.EntityFormLayoutUtils;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorListView;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.layout.ViewLayout;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Convert;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component("datatl_EntityInspectorFormBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorFormLayoutBuilder {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected Actions actions;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected AccessManager accessManager;

    protected final InstanceContainer container;

    protected com.vaadin.flow.component.Component ownerComponent;

    protected List<String> disabledProperties;

    public static InspectorFormLayoutBuilder from(
            ApplicationContext applicationContext,
            InstanceContainer container) {
        return applicationContext.getBean(InspectorFormLayoutBuilder.class, container);
    }

    protected InspectorFormLayoutBuilder(InstanceContainer container) {
        this.container = container;
    }

    public InspectorFormLayoutBuilder withOwnerComponent(com.vaadin.flow.component.Component component) {
        this.ownerComponent = component;
        return this;
    }

    public InspectorFormLayoutBuilder withDisabledProperties(String... properties) {
        this.disabledProperties = Arrays.asList(properties);
        return this;
    }

    public FormLayout build() {
        MetaClass metaClass = container.getEntityMetaClass();
        Object item = getItem();

        FormLayout form = uiComponents.create(FormLayout.class);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep(
                        "9.5em",
                        2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        if (ownerComponent != null) {
            ((ViewLayout) ownerComponent).addComponentAsFirst(form);
        }

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            boolean isReadonly = metaProperty.isReadOnly();
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    boolean includeId = primaryKeyProperty != null
                            && primaryKeyProperty.equals(metaProperty)
                            && String.class.equals(metaProperty.getJavaType());
                    //skip system properties
                    if (metadataTools.isSystem(metaProperty) && !includeId) {
                        continue;
                    }
                    if (metaProperty.getType() != MetaProperty.Type.ENUM
                            && (EntityFormLayoutUtils.isByteArray(metaProperty)
                            || EntityFormLayoutUtils.isUuid(metaProperty))
                    ) {
                        continue;
                    }
                    if (metadataTools.isJpa(metaProperty)
                            && metadataTools.isAnnotationPresent(
                            item,
                            metaProperty.getName(),
                            Convert.class)) {
                        continue;
                    }

                    if (includeId && !entityStates.isNew(item)) {
                        isReadonly = true;
                    }

                    addField(container, form, metaProperty, isReadonly);
                    break;
                case COMPOSITION:
                case ASSOCIATION:
                    if (!EntityFormLayoutUtils.isMany(metaProperty)) {
                        addField(container, form, metaProperty, isReadonly);
                    }
                    break;
                default:
                    break;
            }
        }
        return form;
    }

    private Object getItem() {
        return container.getItem();
    }

    /**
     * Adds field to the specified form.
     * If the field should be custom, adds it to the specified customFields collection
     * which can be used later to create fieldGenerators
     *
     * @param metaProperty meta property of the item's property which field is creating
     * @param form         field group to which created field will be added
     */
    protected void addField(InstanceContainer container, FormLayout form, MetaProperty metaProperty, boolean isReadonly) {
        MetaClass metaClass = container.getEntityMetaClass();
        Range range = metaProperty.getRange();

        boolean isRequired = EntityFormLayoutUtils.isRequired(metaProperty);

        FlowuiEntityAttributeContext attributeContext =
                new FlowuiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        if (!attributeContext.canView())
            return;

        if (range.isClass()) {
            FlowuiEntityContext entityContext = new FlowuiEntityContext(range.asClass());
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isViewPermitted()) {
                return;
            }
        }

        ValueSource valueSource = new ContainerValueSource<>(container, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(metaClass, metaProperty.getName());
        componentContext.setValueSource(valueSource);
        componentContext.setTargetClass(FormLayout.class);

        com.vaadin.flow.component.Component field;

        if (range.isClass()) {
            EntityPicker pickerField = uiComponents.create(EntityPicker.class);

            MetaClass propertyMetaClass = metaProperty.getRange().asClass();
            pickerField.setValueSource(componentContext.getValueSource());

            EntityInspectorLookupAction lookupAction = actions.create(EntityInspectorLookupAction.class);
            lookupAction.setViewClass(EntityInspectorListView.class);
            lookupAction.setEntityNameParameter(propertyMetaClass.getName());
            lookupAction.setTarget(pickerField);

            EntityClearAction entityClearAction = actions.create(EntityClearAction.class);
            entityClearAction.setTarget(pickerField);

            pickerField.addAction(lookupAction);
            pickerField.addAction(entityClearAction);
            pickerField.setTitle(getPropertyTitle(metaClass, metaProperty));
            pickerField.setRequired(isRequired);
            pickerField.setWidthFull();

            isReadonly = isReadonly || (disabledProperties != null && disabledProperties.contains(metaProperty.getName()));
            if (range.isClass() && !metadataTools.isEmbedded(metaProperty)) {
                pickerField.setReadOnly(!metadataTools.isOwningSide(metaProperty) || isReadonly);
            } else {
                pickerField.setReadOnly(isReadonly);
            }

            field = pickerField;
        } else {
            field = uiComponentsGenerator.generate(componentContext);
        }

        form.addFormItem(field, getPropertyTitle(metaClass, metaProperty));
    }

    protected String getPropertyTitle(MetaClass metaClass, MetaProperty metaProperty) {
        return messageTools.getPropertyCaption(metaClass, metaProperty.getName());
    }
}
