/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.component.factory;

import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.factory.AbstractComponentGenerationStrategy;
import io.jmix.flowui.component.factory.EntityFieldCreationSupport;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplatesflowui.MessageParameterResolver;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("msgtmp_MessageTemplateParameterGenerationStrategy")
public class MessageTemplateParameterGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected MessageParameterResolver messageParameterResolver;

    public MessageTemplateParameterGenerationStrategy(UiComponents uiComponents,
                                                      Metadata metadata,
                                                      MetadataTools metadataTools,
                                                      Actions actions,
                                                      DatatypeRegistry datatypeRegistry,
                                                      Messages messages,
                                                      EntityFieldCreationSupport entityFieldCreationSupport,
                                                      MessageParameterResolver messageParameterResolver) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);

        this.messageParameterResolver = messageParameterResolver;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context instanceof MessageTemplateParameterGenerationContext) {
            return createComponentInternal(context);
        }

        return null;
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        MessageTemplateParameter parameter =
                ((MessageTemplateParameterGenerationContext) context).getMessageTemplateParameter();

        return switch (parameter.getType()) {
            case BOOLEAN -> createBooleanField();
            case NUMERIC, TEXT -> createTextField(messageParameterResolver.resolveDatatype(parameter));
            case DATE -> createDateField(messageParameterResolver.resolveDatatype(parameter));
            case TIME -> createTimeField(messageParameterResolver.resolveDatatype(parameter));
            case DATETIME -> createDateTimeField(messageParameterResolver.resolveDatatype(parameter));
            case ENUMERATION -> parameter.getEnumerationClass() != null ? createEnumField(context) : null;
            case ENTITY_LIST -> parameter.getEntityMetaClass() != null ? createEntityCollectionField(context) : null;
            case ENTITY -> parameter.getEntityMetaClass() != null ? createEntityField(context) : null;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTextField(@Nullable Datatype<?> datatype) {
        TypedTextField field = uiComponents.create(TypedTextField.class);

        field.setWidthFull();
        field.setDatatype(datatype);

        return field;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateTimeField(@Nullable Datatype<?> datatype) {
        TypedDateTimePicker dateTimeField = uiComponents.create(TypedDateTimePicker.class);

        dateTimeField.setWidthFull();
        dateTimeField.setDatatype(datatype);

        return dateTimeField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateField(@Nullable Datatype<?> datatype) {
        TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);

        dateField.setWidthFull();
        dateField.setDatatype(datatype);

        return dateField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTimeField(@Nullable Datatype<?> datatype) {
        TypedTimePicker timeField = uiComponents.create(TypedTimePicker.class);

        timeField.setWidthFull();
        timeField.setDatatype(datatype);

        return timeField;
    }

    @Nullable
    @Override
    protected Component createEntityField(ComponentGenerationContext context) {
        MessageTemplateParameter parameter =
                ((MessageTemplateParameterGenerationContext) context).getMessageTemplateParameter();

        MetaClass metaClass = metadata.getClass(parameter.getEntityMetaClass());

        EntityPicker<?> entityPicker = uiComponents.create(EntityPicker.class);

        entityPicker.setMetaClass(metaClass);
        entityPicker.setWidthFull();
        entityPicker.addAction(actions.create(EntityLookupAction.ID));
        entityPicker.addAction(actions.create(EntityClearAction.ID));

        return entityPicker;
    }

    @Nullable
    protected Component createEntityCollectionField(ComponentGenerationContext context) {
        MessageTemplateParameter parameter =
                ((MessageTemplateParameterGenerationContext) context).getMessageTemplateParameter();

        MetaClass metaClass = metadata.getClass(parameter.getEntityMetaClass());

        JmixMultiSelectComboBoxPicker<?> multiSelectComboBoxPicker =
                uiComponents.create(JmixMultiSelectComboBoxPicker.class);

        multiSelectComboBoxPicker.setMetaClass(metaClass);
        multiSelectComboBoxPicker.setWidthFull();

        multiSelectComboBoxPicker.addAction(actions.create(EntityLookupAction.ID));
        multiSelectComboBoxPicker.addAction(actions.create(EntityClearAction.ID));

        return multiSelectComboBoxPicker;
    }

    protected Component createBooleanField() {
        return uiComponents.create(JmixCheckbox.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Component createEnumField(ComponentGenerationContext context) {
        MessageTemplateParameter parameter =
                ((MessageTemplateParameterGenerationContext) context).getMessageTemplateParameter();

        JmixSelect enumField = uiComponents.create(JmixSelect.class);
        enumField.setWidthFull();

        Class<?> enumClass = messageParameterResolver.resolveClass(parameter);
        if (enumClass != null) {
            enumField.setItems(enumClass);
        }

        return enumField;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 600;
    }
}
