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
import com.vaadin.flow.data.provider.Query;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentUtils;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

/**
 * Strategy for generating visual components for a {@link MessageTemplateParameter}.
 * The generation process uses information from the {@link MessageTemplateParameterGenerationContext}.
 *
 * @see MessageTemplateParameterGenerationContext
 */
@org.springframework.stereotype.Component("msgtmp_MessageTemplateParameterGenerationStrategy")
public class MessageTemplateParameterGenerationStrategy extends AbstractComponentGenerationStrategy
        implements Ordered {

    protected MessageParameterResolver messageParameterResolver;
    protected DataManager dataManager;

    public MessageTemplateParameterGenerationStrategy(UiComponents uiComponents,
                                                      Metadata metadata,
                                                      MetadataTools metadataTools,
                                                      Actions actions,
                                                      DatatypeRegistry datatypeRegistry,
                                                      Messages messages,
                                                      EntityFieldCreationSupport entityFieldCreationSupport,
                                                      MessageParameterResolver messageParameterResolver,
                                                      DataManager dataManager) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);

        this.messageParameterResolver = messageParameterResolver;
        this.dataManager = dataManager;
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
            case NUMERIC, TEXT -> createTextField(parameter);
            case DATE -> createDateField(parameter);
            case TIME -> createTimeField(parameter);
            case DATETIME -> createDateTimeField(parameter);
            case ENUMERATION -> parameter.getEnumerationClass() != null ? createEnumField(context) : null;
            case ENTITY_LIST -> parameter.getEntityMetaClass() != null ? createEntityCollectionField(context) : null;
            case ENTITY -> parameter.getEntityMetaClass() != null ? createEntityField(context) : null;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTextField(MessageTemplateParameter parameter) {
        TypedTextField field = uiComponents.create(TypedTextField.class);

        field.setWidthFull();

        Datatype<?> datatype = messageParameterResolver.resolveDatatype(parameter);
        field.setDatatype(datatype);

        return field;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateTimeField(MessageTemplateParameter parameter) {
        TypedDateTimePicker dateTimeField = uiComponents.create(TypedDateTimePicker.class);

        Datatype<?> datatype = messageParameterResolver.resolveDatatype(parameter);
        dateTimeField.setDatatype(datatype);
        dateTimeField.setWidthFull();

        if (Boolean.TRUE.equals(parameter.getDefaultDateIsCurrent())) {
            LocalDateTime now = LocalDateTime.now();
            UiComponentUtils.setValue(dateTimeField, now);
        }

        return dateTimeField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateField(MessageTemplateParameter parameter) {
        TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);

        Datatype<?> datatype = messageParameterResolver.resolveDatatype(parameter);
        dateField.setDatatype(datatype);
        dateField.setWidthFull();

        if (Boolean.TRUE.equals(parameter.getDefaultDateIsCurrent())) {
            LocalDate now = LocalDate.now();
            UiComponentUtils.setValue(dateField, now);
        }

        return dateField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTimeField(MessageTemplateParameter parameter) {
        TypedTimePicker timeField = uiComponents.create(TypedTimePicker.class);

        Datatype<?> datatype = messageParameterResolver.resolveDatatype(parameter);
        timeField.setDatatype(datatype);
        timeField.setWidthFull();

        if (Boolean.TRUE.equals(parameter.getDefaultDateIsCurrent())) {
            LocalTime now = LocalTime.now();
            UiComponentUtils.setValue(timeField, now);
        }

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

        multiSelectComboBoxPicker.setItemsFetchCallback(
                query -> getItemsByQuery(query, metaClass.getJavaClass()));
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

    protected <T> Stream<T> getItemsByQuery(Query<T, String> query, Class<T> entityClass) {
        return dataManager.load(entityClass)
                .all()
                .firstResult(query.getOffset())
                .maxResults(query.getLimit())
                .list()
                .stream()
                .filter(entity -> metadataTools.getInstanceName(entity)
                        .toLowerCase()
                        .contains(
                                query.getFilter()
                                        .orElse("")
                                        .toLowerCase()
                        ));
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 600;
    }
}
