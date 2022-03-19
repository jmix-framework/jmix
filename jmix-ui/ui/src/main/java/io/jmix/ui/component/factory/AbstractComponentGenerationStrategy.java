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

package io.jmix.ui.component.factory;

import io.jmix.core.FileRef;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.action.valuespicker.ValuesSelectAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.sql.Time;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static io.jmix.ui.component.DateField.Resolution;

public abstract class AbstractComponentGenerationStrategy implements ComponentGenerationStrategy {

    protected Messages messages;
    protected UiComponents uiComponents;
    protected EntityFieldCreationSupport entityFieldCreationSupport;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected Icons icons;
    protected Actions actions;

    public AbstractComponentGenerationStrategy(Messages messages,
                                               UiComponents uiComponents,
                                               EntityFieldCreationSupport entityFieldCreationSupport,
                                               Metadata metadata,
                                               MetadataTools metadataTools,
                                               Icons icons,
                                               Actions actions) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.entityFieldCreationSupport = entityFieldCreationSupport;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.icons = icons;
        this.actions = actions;
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
            resultComponent = createCollectionField(context, mpp);
        } else if (mppRange.isDatatype()) {
            resultComponent = createDatatypeField(context, mpp);
        } else if (mppRange.isClass()) {
            resultComponent = createClassField(context, mpp);
        } else if (mppRange.isEnum()) {
            resultComponent = createEnumField(context);
        }

        return resultComponent;
    }

    protected Component createClassField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        return createEntityField(context, mpp);
    }

    @Nullable
    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Range mppRange = mpp.getRange();
        Element xmlDescriptor = context.getXmlDescriptor();

        Class type = mppRange.asDatatype().getJavaClass();

        if (xmlDescriptor != null
                && "true".equalsIgnoreCase(xmlDescriptor.attributeValue("link"))) {
            return createDatatypeLinkField(context);
        }

        boolean hasMaskAttribute = xmlDescriptor != null
                && xmlDescriptor.attribute("mask") != null;

        if (type.equals(String.class)) {
            return hasMaskAttribute
                    ? createMaskedField(context)
                    : createStringField(context, mpp);
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

            Field currencyField = createCurrencyField(context, mpp);
            if (currencyField != null) {
                return currencyField;
            }

            return createNumberField(context);
        } else if (type.equals(byte[].class)) {
            return createFileUploadField(context);
        } else if (type.equals(FileRef.class)) {
            return createFileStorageUploadField(context);
        }
        return null;
    }

    protected Component createDatatypeLinkField(ComponentGenerationContext context) {
        EntityLinkField linkField = uiComponents.create(EntityLinkField.class);

        setValueSource(linkField, context);
        setLinkFieldAttributes(linkField, context);

        return linkField;
    }

    protected Field createEnumField(ComponentGenerationContext context) {
        ComboBox component = uiComponents.create(ComboBox.class);
        setValueSource(component, context);
        return component;
    }

    protected Component createMaskedField(ComponentGenerationContext context) {
        MaskedField maskedField = uiComponents.create(MaskedField.class);
        setValueSource(maskedField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            maskedField.setMask(xmlDescriptor.attributeValue("mask"));

            String valueModeStr = xmlDescriptor.attributeValue("valueMode");
            if (StringUtils.isNotEmpty(valueModeStr)) {
                maskedField.setValueMode(MaskedField.ValueMode.valueOf(valueModeStr.toUpperCase()));
            }
        }
        maskedField.setValueMode(MaskedField.ValueMode.MASKED);
        maskedField.setSendNullRepresentation(false);

        return maskedField;
    }

    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
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

    protected Field createUuidField(ComponentGenerationContext context) {
        MaskedField maskedField = uiComponents.create(MaskedField.class);
        setValueSource(maskedField, context);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        return maskedField;
    }

    protected Field createBooleanField(ComponentGenerationContext context) {
        CheckBox component = uiComponents.create(CheckBox.class);
        setValueSource(component, context);
        return component;
    }

    protected Component createDateField(ComponentGenerationContext context) {
        DateField dateField = uiComponents.create(DateField.class);
        setValueSource(dateField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        String resolution = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("resolution");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        if (StringUtils.isNotEmpty(resolution)) {
            Resolution dateResolution = Resolution.valueOf(resolution);
            dateField.setResolution(dateResolution);

            if (dateFormat == null) {
                if (dateResolution == Resolution.DAY) {
                    dateFormat = "msg://dateFormat";
                } else if (dateResolution == Resolution.MIN) {
                    dateFormat = "msg://dateTimeFormat";
                }
            }
        }

        if (StringUtils.isNotEmpty(dateFormat)) {
            if (dateFormat.startsWith("msg://")) {
                dateFormat = messages.getMessage(dateFormat.substring(6));
            }
            dateField.setDateFormat(dateFormat);
        }

        return dateField;
    }

    protected Component createTimeField(ComponentGenerationContext context) {
        TimeField timeField = uiComponents.create(TimeField.class);
        setValueSource(timeField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String resolution = xmlDescriptor.attributeValue("resolution");
            if (StringUtils.isNotEmpty(resolution)) {
                TimeField.Resolution res = TimeField.Resolution.valueOf(resolution);
                timeField.setResolution(res);
            }
        }

        return timeField;
    }

    protected Field createNumberField(ComponentGenerationContext context) {
        TextField component = uiComponents.create(TextField.class);
        setValueSource(component, context);

        return component;
    }

    @Nullable
    protected Field createCurrencyField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Object currencyAnnotation = mpp.getMetaProperty().getAnnotations().get(CurrencyValue.class.getName());
        if (currencyAnnotation == null) {
            return null;
        }

        CurrencyField component = uiComponents.create(CurrencyField.class);
        setValueSource(component, context);
        return component;
    }

    protected Field createFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = uiComponents.create(FileUploadField.NAME);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon(icons.get(JmixIcon.UPLOAD));

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon(icons.get(JmixIcon.TIMES_CIRCLE));

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    protected Field createFileStorageUploadField(ComponentGenerationContext context) {
        FileStorageUploadField fileUploadField = uiComponents.create(FileStorageUploadField.NAME);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon(icons.get(JmixIcon.UPLOAD));

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon(icons.get(JmixIcon.TIMES));

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    @SuppressWarnings("unchecked")
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        String linkAttribute = null;
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            linkAttribute = xmlDescriptor.attributeValue("link");
        }

        if (!Boolean.parseBoolean(linkAttribute)) {
            EntityPicker entityPicker = entityFieldCreationSupport.createEntityField(mpp, context.getOptions());

            setValueSource(entityPicker, context);

            if (xmlDescriptor != null) {
                String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    entityPicker.setFormatter(new CaptionAdapter(captionProperty, metadata, metadataTools));
                }
            }

            return entityPicker;
        } else {
            EntityLinkField linkField = uiComponents.create(EntityLinkField.class);

            setValueSource(linkField, context);
            setLinkFieldAttributes(linkField, context);

            return linkField;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createCollectionField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        ValuesPicker valuesPicker = uiComponents.create(ValuesPicker.class);
        setValueSource(valuesPicker, context);

        ValuesSelectAction selectAction = actions.create(ValuesSelectAction.class);
        Range range = mpp.getRange();
        if (range.isClass()) {
            selectAction.setEntityName(range.asClass().getName());
        } else if (range.isDatatype()) {
            selectAction.setJavaClass(range.asDatatype().getJavaClass());
        } else if (range.isEnum()) {
            selectAction.setEnumClass(range.asEnumeration().getJavaClass());
        }
        valuesPicker.addAction(selectAction);

        ValueClearAction valueClearAction = actions.create(ValueClearAction.class);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    protected void setLinkFieldAttributes(EntityLinkField linkField, ComponentGenerationContext context) {
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String linkScreenId = xmlDescriptor.attributeValue("linkScreenId");
            if (StringUtils.isNotEmpty(linkScreenId)) {
                linkField.setScreen(linkScreenId);
            }

            String linkScreenOpenMode = xmlDescriptor.attributeValue("linkScreenOpenMode");
            if (StringUtils.isNotEmpty(linkScreenOpenMode)) {
                linkField.setOpenMode(OpenMode.valueOf(linkScreenOpenMode));
            }
        }
    }

    @Nullable
    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        return metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(Field field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }
}
