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

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.entitypicker.OpenAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.impl.GuiActionSupport;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static io.jmix.ui.component.DateField.Resolution;

public abstract class AbstractComponentGenerationStrategy implements ComponentGenerationStrategy {

    protected Actions actions;
    protected Messages messages;
    protected UiComponents uiComponents;
    protected GuiActionSupport guiActionSupport;
    protected Metadata metadata;
    protected MetadataTools metadataTools;

    public AbstractComponentGenerationStrategy(Messages messages,
                                               GuiActionSupport guiActionSupport,
                                               Metadata metadata,
                                               MetadataTools metadataTools) {
        this.messages = messages;
        this.guiActionSupport = guiActionSupport;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
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

        if (mppRange.isDatatype()) {
            resultComponent = createDatatypeField(context, mpp);
        } else if (mppRange.isClass()) {
            resultComponent = createClassField(context, mpp);
        } else if (mppRange.isEnum()) {
            resultComponent = createEnumField(context);
        }

        return resultComponent;
    }

    @Nullable
    protected Component createClassField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        MetaProperty metaProperty = mpp.getMetaProperty();
        Class<?> javaType = metaProperty.getJavaType();

        if (!Collection.class.isAssignableFrom(javaType)) {
            return createEntityField(context, mpp);
        }

        return null;
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
        } else if (type.equals(URI.class)) {
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
            String showSeconds = xmlDescriptor.attributeValue("showSeconds");
            if (Boolean.parseBoolean(showSeconds)) {
                timeField.setResolution(TimeField.Resolution.SEC);
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
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    protected Field createFileStorageUploadField(ComponentGenerationContext context) {
        FileStorageUploadField fileUploadField = uiComponents.create(FileStorageUploadField.NAME);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

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
            Options options = context.getOptions();

            EntityPicker entityPicker;
            if (options == null) {
                entityPicker = uiComponents.create(EntityPicker.class);
                setValueSource(entityPicker, context);

                if (mpp.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
                    entityPicker.addAction(actions.create(LookupAction.ID));
                    boolean actionsByMetaAnnotations = guiActionSupport.createActionsByMetaAnnotations(entityPicker);
                    if (!actionsByMetaAnnotations) {
                        entityPicker.addAction(actions.create(EntityClearAction.ID));
                    }
                } else {
                    entityPicker.addAction(actions.create(OpenAction.ID));
                    entityPicker.addAction(actions.create(EntityClearAction.ID));
                }
            } else {
                EntityComboBox entityComboBox = uiComponents.create(EntityComboBox.class);

                setValueSource(entityComboBox, context);
                entityComboBox.setOptions(options);

                entityPicker = entityComboBox;

                guiActionSupport.createActionsByMetaAnnotations(entityPicker);
            }

            if (xmlDescriptor != null) {
                String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    entityPicker.setOptionCaptionProvider(
                            new CaptionAdapter(captionProperty, metadata, metadataTools));
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

    protected void setLinkFieldAttributes(EntityLinkField linkField, ComponentGenerationContext context) {
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String linkScreen = xmlDescriptor.attributeValue("linkScreen");
            if (StringUtils.isNotEmpty(linkScreen)) {
                linkField.setScreen(linkScreen);
            }

            String invokeMethodName = xmlDescriptor.attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                linkField.setCustomClickHandler(new InvokeEntityLinkClickHandler(invokeMethodName));
            }

            String openTypeAttribute = xmlDescriptor.attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                OpenType openType = OpenType.valueOf(openTypeAttribute);
                linkField.setScreenOpenType(openType);
            }
        }
    }

    @Nullable
    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        return metaClass.getPropertyPath(property);
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(Field field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }

    protected static class InvokeEntityLinkClickHandler implements EntityLinkField.EntityLinkClickHandler {
        protected final String invokeMethodName;

        public InvokeEntityLinkClickHandler(String invokeMethodName) {
            this.invokeMethodName = invokeMethodName;
        }

        @Override
        public void onClick(EntityLinkField field) {
            Window frame = ComponentsHelper.getWindow(field);
            if (frame == null) {
                throw new IllegalStateException("Please specify Frame for EntityLinkField");
            }

            FrameOwner controller = frame.getFrameOwner();
            Method method;
            try {
                method = controller.getClass().getMethod(invokeMethodName, EntityLinkField.class);
                try {
                    method.invoke(controller, field);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Can't invoke method with name '%s'",
                            invokeMethodName), e);
                }
            } catch (NoSuchMethodException e) {
                try {
                    method = controller.getClass().getMethod(invokeMethodName);
                    try {
                        method.invoke(controller);
                    } catch (Exception ex) {
                        throw new RuntimeException(String.format("Can't invoke method with name '%s'",
                                invokeMethodName), ex);
                    }
                } catch (NoSuchMethodException e1) {
                    throw new IllegalStateException(String.format("No suitable methods named '%s' for invoke",
                            invokeMethodName));
                }
            }
        }
    }
}
