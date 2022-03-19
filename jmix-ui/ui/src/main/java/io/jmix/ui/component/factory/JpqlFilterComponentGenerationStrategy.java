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

package io.jmix.ui.component.factory;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.Entity;
import io.jmix.core.FileRef;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.action.valuespicker.ValuesSelectAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.screen.OpenMode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
@org.springframework.stereotype.Component("ui_JpqlFilterComponentGenerationStrategy")
public class JpqlFilterComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected static final String UNARY_FIELD_STYLENAME = "unary-field";

    protected DatatypeRegistry datatypeRegistry;
    protected DataAwareComponentsTools dataAwareComponentsTools;

    @Autowired
    public JpqlFilterComponentGenerationStrategy(Messages messages,
                                                 UiComponents uiComponents,
                                                 EntityFieldCreationSupport entityFieldCreationSupport,
                                                 Metadata metadata,
                                                 MetadataTools metadataTools,
                                                 Icons icons,
                                                 Actions actions,
                                                 DatatypeRegistry datatypeRegistry,
                                                 DataAwareComponentsTools dataAwareComponentsTools) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
        this.datatypeRegistry = datatypeRegistry;
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !JpqlFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof JpqlFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        Class parameterClass = cfContext.getParameterClass();

        if (cfContext.hasInExpression()) {
            return createCollectionField(context);
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            return createEntityField(context);
        } else if (Enum.class.isAssignableFrom(parameterClass)) {
            return createEnumField(context);
        } else if (datatypeRegistry.find(parameterClass) != null) {
            Component dataTypeField = createDatatypeField(context, parameterClass);
            if (dataTypeField instanceof HasDatatype) {
                Datatype datatype = datatypeRegistry.find(parameterClass);
                if (datatype != null) {
                    ((HasDatatype<?>) dataTypeField).setDatatype(datatype);
                }
            }
            return dataTypeField;
        } else if (Void.class.isAssignableFrom(parameterClass)) {
            return createVoidField(context);
        }

        return super.createComponentInternal(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createCollectionField(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        Class parameterClass = cfContext.getParameterClass();

        ValuesPicker valuesPicker = uiComponents.create(ValuesPicker.class);
        setValueSource(valuesPicker, cfContext);

        ValuesSelectAction selectAction = actions.create(ValuesSelectAction.class);

        if (Entity.class.isAssignableFrom(parameterClass)) {
            MetaClass metaClass = metadata.getClass(cfContext.getParameterClass());
            selectAction.setEntityName(metaClass.getName());
        } else if (EnumClass.class.isAssignableFrom(parameterClass)) {
            selectAction.setEnumClass(parameterClass);
        } else if (datatypeRegistry.find(parameterClass) != null) {
            Datatype datatype = datatypeRegistry.get(parameterClass);
            selectAction.setJavaClass(datatype.getJavaClass());
        }
        valuesPicker.addAction(selectAction);

        ValueClearAction valueClearAction = actions.create(ValueClearAction.class);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    protected Component createEntityField(ComponentGenerationContext context) {
        EntityPicker<?> field = uiComponents.create(EntityPicker.class);

        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        MetaClass metaClass = metadata.getClass(cfContext.getParameterClass());
        field.setMetaClass(metaClass);

        EntityLookupAction<?> lookupAction = (EntityLookupAction<?>) actions.create(EntityLookupAction.ID);
        lookupAction.setOpenMode(OpenMode.DIALOG);
        field.addAction(lookupAction);
        field.addAction(actions.create(EntityClearAction.ID));

        return field;
    }

    @Override
    protected Field createEnumField(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        ComboBox<?> component = uiComponents.create(ComboBox.class);
        component.setOptionsEnum(cfContext.getParameterClass());
        return component;
    }

    @Nullable
    protected Component createDatatypeField(ComponentGenerationContext context, Class type) {
        Element xmlDescriptor = context.getXmlDescriptor();

        if (xmlDescriptor != null
                && "true".equalsIgnoreCase(xmlDescriptor.attributeValue("link"))) {
            return createDatatypeLinkField(context);
        }

        boolean hasMaskAttribute = xmlDescriptor != null
                && xmlDescriptor.attribute("mask") != null;

        if (type.equals(String.class)) {
            return hasMaskAttribute
                    ? createMaskedField(context)
                    : createStringField(context);
        } else if (type.equals(UUID.class)) {
            return createUuidField(context);
        } else if (type.equals(Boolean.class)) {
            return createBooleanField(context);
        } else if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)
                || type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {
            return createDateField(context);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {
            return createTimeField(context);
        } else if (Number.class.isAssignableFrom(type)) {
            if (hasMaskAttribute) {
                return createMaskedField(context);
            }
            return createNumberField(context);
        } else if (type.equals(byte[].class)) {
            return createFileUploadField(context);
        } else if (type.equals(FileRef.class)) {
            return createFileStorageUploadField(context);
        }
        return null;
    }

    protected Component createStringField(ComponentGenerationContext context) {
        TextInputField textField = null;

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            final String rows = xmlDescriptor.attributeValue("rows");
            if (!StringUtils.isEmpty(rows)) {
                TextArea textArea = uiComponents.create(TextArea.class);
                textArea.setRows(Integer.parseInt(rows));
                textField = textArea;
            }
        }

        if (textField == null) {
            textField = uiComponents.create(TextField.class);
        }

        setValueSource(textField, context);

        String maxLength = xmlDescriptor != null ? xmlDescriptor.attributeValue("maxLength") : null;
        if (StringUtils.isNotEmpty(maxLength)) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(Integer.parseInt(maxLength));
        }

        return textField;
    }

    @Override
    protected Component createDateField(ComponentGenerationContext context) {
        DateField dateField = (DateField) super.createDateField(context);

        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        Class parameterClass = cfContext.getParameterClass();
        dataAwareComponentsTools.setupDateFormat(dateField, parameterClass);

        return dateField;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context) {
        ComboBox<Boolean> component = uiComponents.create(ComboBox.of(Boolean.class));
        component.setTextInputAllowed(false);
        component.addStyleName(UNARY_FIELD_STYLENAME);

        component.setOptionsMap(ImmutableMap.of(
                messages.getMessage("boolean.yes"), Boolean.TRUE,
                messages.getMessage("boolean.no"), Boolean.FALSE
        ));

        return component;
    }

    protected Field createVoidField(ComponentGenerationContext context) {
        return uiComponents.create(CheckBox.NAME);
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
