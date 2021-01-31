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

import io.jmix.core.Entity;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.datatype.impl.EnumerationImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.screen.OpenMode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import java.net.URI;
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

    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    public JpqlFilterComponentGenerationStrategy(Messages messages,
                                                 UiComponents uiComponents,
                                                 EntityFieldCreationSupport entityFieldCreationSupport,
                                                 Metadata metadata,
                                                 MetadataTools metadataTools,
                                                 Icons icons,
                                                 Actions actions,
                                                 DatatypeRegistry datatypeRegistry) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
        this.datatypeRegistry = datatypeRegistry;
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

        if (Entity.class.isAssignableFrom(parameterClass)) {
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
        }

        return super.createComponentInternal(context);
    }

    protected Component createEntityField(ComponentGenerationContext context) {
        EntityPicker<?> field = uiComponents.create(EntityPicker.class);

        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        MetaClass metaClass = metadata.getClass(cfContext.getParameterClass());
        field.setMetaClass(metaClass);

        LookupAction<?> lookupAction = (LookupAction<?>) actions.create(LookupAction.ID);
        lookupAction.setOpenMode(OpenMode.DIALOG);
        field.addAction(lookupAction);
        field.addAction(actions.create(EntityClearAction.ID));

        return field;
    }

    @Override
    protected Field createEnumField(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        Enumeration<?> enumeration = new EnumerationImpl<>(cfContext.getParameterClass());
        ComboBox<?> component = uiComponents.create(ComboBox.class);
        component.setOptionsEnum(enumeration.getJavaClass());
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
        } else if (type.equals(URI.class)) {
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
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
