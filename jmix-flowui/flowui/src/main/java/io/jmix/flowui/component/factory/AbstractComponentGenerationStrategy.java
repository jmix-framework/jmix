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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.datepicker.DatePicker;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.ComponentGenerationStrategy;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.data.SupportsValueSource;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import javax.persistence.Lob;
import java.lang.annotation.Annotation;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public abstract class AbstractComponentGenerationStrategy implements ComponentGenerationStrategy {

    protected UiComponents uiComponents;
    protected EntityFieldCreationSupport entityFieldCreationSupport;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected Actions actions;
    protected Messages messages;
    protected DatatypeRegistry datatypeRegistry;

    public AbstractComponentGenerationStrategy(UiComponents uiComponents,
                                               Metadata metadata,
                                               MetadataTools metadataTools,
                                               Actions actions,
                                               DatatypeRegistry datatypeRegistry,
                                               Messages messages,
                                               EntityFieldCreationSupport entityFieldCreationSupport) {
        this.uiComponents = uiComponents;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.actions = actions;
        this.datatypeRegistry = datatypeRegistry;
        this.messages = messages;
        this.entityFieldCreationSupport = entityFieldCreationSupport;
    }

    @Nullable
    protected Component createComponentInternal(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());

        if (mpp == null) {
            return null;
        }

        Range mppRange = mpp.getRange();
        Component resultComponent = null;

        if (Collection.class.isAssignableFrom(mpp.getMetaProperty().getJavaType())) {
            resultComponent = createCollectionField(context);
        } else if (mppRange.isDatatype()) {
            resultComponent = createDatatypeField(context, mpp);
        } else if (mppRange.isClass()) {
            resultComponent = createEntityField(context);
        } else if (mppRange.isEnum()) {
            resultComponent = createEnumField(context);
        }

        if (resultComponent instanceof HasSize) {
            ((HasSize) resultComponent).setWidthFull();
        }
        return resultComponent;
    }

    @Nullable
    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Range mppRange = mpp.getRange();

        Class type = mppRange.asDatatype().getJavaClass();

        if (type.equals(String.class)
                || type.equals(UUID.class)) {
            return createStringField(context, mpp);
        } else if (type.equals(Boolean.class)) {
            return createBooleanField(context);
        } else if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)
                || type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {
            return createDatePicker(context);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {
            return createTimePicker(context);
        } else if (Number.class.isAssignableFrom(type)) {
            return createNumberField(context);
        }
        return null;
    }

    protected Component createEnumField(ComponentGenerationContext context) {
        JmixSelect enumField = uiComponents.create(JmixSelect.class);
        setValueSource(enumField, context);
        return enumField;
    }

    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Component textField;

        Annotation lob = mpp.getMetaProperty().getAnnotatedElement().getAnnotation(Lob.class);
        if (lob != null) {
            textField = uiComponents.create(JmixTextArea.class);
        } else {
            textField = uiComponents.create(TypedTextField.class);
        }
        setValueSource((SupportsValueSource<?>) textField, context);

        return textField;
    }

    protected Component createBooleanField(ComponentGenerationContext context) {
        JmixCheckbox booleanField = uiComponents.create(JmixCheckbox.class);
        setValueSource(booleanField, context);
        return booleanField;
    }

    @SuppressWarnings("unchecked")
    protected Component createDatePicker(ComponentGenerationContext context) {
        TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);
        setValueSource(dateField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        String datatype = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("datatype");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        if (StringUtils.isNotEmpty(datatype)) {
            dateField.setDatatype(datatypeRegistry.get(datatype));
        }

        if (StringUtils.isNotEmpty(dateFormat)) {
            if (dateFormat.startsWith("msg://")) {
                dateFormat = messages.getMessage(dateFormat.substring(6));
                dateField.setI18n(new DatePicker.DatePickerI18n()
                        .setDateFormat(dateFormat));
            }
        }

        return dateField;
    }

    @SuppressWarnings("unchecked")
    protected Component createTimePicker(ComponentGenerationContext context) {
        TypedTimePicker timeField = uiComponents.create(TypedTimePicker.class);
        setValueSource(timeField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        String datatype = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("datatype");

        if (StringUtils.isNotEmpty(datatype)) {
            timeField.setDatatype(datatypeRegistry.get(datatype));
        }
        return timeField;
    }

    protected Component createNumberField(ComponentGenerationContext context) {
        TypedTextField numberField = uiComponents.create(TypedTextField.class);
        setValueSource(numberField, context);
        return numberField;
    }

    @Nullable
    protected Component createEntityField(ComponentGenerationContext context) {
        Component entityComponent = entityFieldCreationSupport.createEntityField(context);

        if (entityComponent == null) {
            return null;
        }

        setValueSource((SupportsValueSource<?>) entityComponent, context);
        return entityComponent;
    }

    //TODO: kremnevda, implement after https://github.com/jmix-framework/jmix/issues/1044 27.09.2022
    protected Component createCollectionField(ComponentGenerationContext context) {
        return null;
    }

    @Nullable
    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        return metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(SupportsValueSource<?> field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }
}
