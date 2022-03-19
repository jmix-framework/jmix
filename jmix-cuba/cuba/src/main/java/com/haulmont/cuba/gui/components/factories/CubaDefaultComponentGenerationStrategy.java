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

package com.haulmont.cuba.gui.components.factories;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.GuiActionSupport;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.factory.DefaultComponentGenerationStrategy;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Consumer;

@org.springframework.stereotype.Component(CubaDefaultComponentGenerationStrategy.NAME)
public class CubaDefaultComponentGenerationStrategy extends DefaultComponentGenerationStrategy {
    public static final String NAME = "cuba_DefaultComponentGenerationStrategy";

    protected GuiActionSupport guiActionSupport;
    protected UiComponents cubaUiComponents;

    @Autowired
    public CubaDefaultComponentGenerationStrategy(Messages messages,
                                                  io.jmix.ui.UiComponents uiComponents,
                                                  EntityFieldCreationSupport entityFieldCreationSupport,
                                                  Metadata metadata,
                                                  MetadataTools metadataTools,
                                                  Icons icons,
                                                  Actions actions,
                                                  GuiActionSupport guiActionSupport,
                                                  UiComponents cubaUiComponents) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);

        this.guiActionSupport = guiActionSupport;
        this.cubaUiComponents = cubaUiComponents;
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE - 10;
    }

    @Override
    protected Component createClassField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        MetaProperty metaProperty = mpp.getMetaProperty();
        Class<?> javaType = metaProperty.getJavaType();
        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createCubaFileUploadField(context);
        }

        if (!Collection.class.isAssignableFrom(javaType)) {
            return createEntityField(context, mpp);
        }

        return null;
    }

    @Override
    protected Component createDatatypeLinkField(ComponentGenerationContext context) {
        EntityLinkField linkField = cubaUiComponents.create(EntityLinkField.NAME);

        setValueSource(linkField, context);
        setLinkFieldAttributes(linkField, context);

        return linkField;
    }

    @Override
    protected Field createEnumField(ComponentGenerationContext context) {
        LookupField component = cubaUiComponents.create(LookupField.class);
        setValueSource(component, context);
        return component;
    }

    @Override
    protected Component createMaskedField(ComponentGenerationContext context) {
        MaskedField maskedField = cubaUiComponents.create(MaskedField.class);
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

    @Override
    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        TextInputField textField = null;

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            final String rows = xmlDescriptor.attributeValue("rows");
            if (!StringUtils.isEmpty(rows)) {
                TextArea textArea = cubaUiComponents.create(TextArea.class);
                textArea.setRows(Integer.parseInt(rows));
                textField = textArea;
            }
        }

        if (textField == null) {
            textField = cubaUiComponents.create(TextField.class);
        }

        setValueSource(textField, context);

        String maxLength = xmlDescriptor != null ? xmlDescriptor.attributeValue("maxLength") : null;
        if (StringUtils.isNotEmpty(maxLength)) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(Integer.parseInt(maxLength));
        }

        return textField;
    }

    @Override
    protected Field createUuidField(ComponentGenerationContext context) {
        MaskedField maskedField = cubaUiComponents.create(MaskedField.class);
        setValueSource(maskedField, context);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        return maskedField;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context) {
        CheckBox component = cubaUiComponents.create(CheckBox.class);
        setValueSource(component, context);
        return component;
    }

    @Override
    protected Component createDateField(ComponentGenerationContext context) {
        DateField dateField = cubaUiComponents.create(DateField.class);
        setValueSource(dateField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        String resolution = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("resolution");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        if (StringUtils.isNotEmpty(resolution)) {
            io.jmix.ui.component.DateField.Resolution dateResolution = io.jmix.ui.component.DateField.Resolution.valueOf(resolution);
            dateField.setResolution(dateResolution);

            if (dateFormat == null) {
                if (dateResolution == io.jmix.ui.component.DateField.Resolution.DAY) {
                    dateFormat = "msg://dateFormat";
                } else if (dateResolution == io.jmix.ui.component.DateField.Resolution.MIN) {
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

    @Override
    protected Component createTimeField(ComponentGenerationContext context) {
        TimeField timeField = cubaUiComponents.create(TimeField.class);
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

    @Override
    protected Field createNumberField(ComponentGenerationContext context) {
        TextField component = cubaUiComponents.create(TextField.class);
        setValueSource(component, context);

        return component;
    }

    @Nullable
    @Override
    protected Field createCurrencyField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Object currencyAnnotation = mpp.getMetaProperty().getAnnotations().get(CurrencyValue.class.getName());
        if (currencyAnnotation == null) {
            return null;
        }

        CurrencyField component = cubaUiComponents.create(CurrencyField.class);
        setValueSource(component, context);
        return component;
    }

    @Override
    protected Field createFileUploadField(ComponentGenerationContext context) {
        return createCubaFileUploadField(context);
    }

    protected Field createCubaFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = cubaUiComponents.create(FileUploadField.NAME);

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

    @Override
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        String linkAttribute = null;
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            linkAttribute = xmlDescriptor.attributeValue("link");
        }

        if (!Boolean.parseBoolean(linkAttribute)) {
            Options options = context.getOptions();

            PickerField pickerField;
            if (options == null) {
                pickerField = cubaUiComponents.create(PickerField.class);
                setValueSource(pickerField, context);

                if (mpp.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
                    guiActionSupport.createActionById(pickerField, PickerField.ActionType.LOOKUP.getId());
                    boolean actionsByMetaAnnotations = guiActionSupport.createActionsByMetaAnnotations(pickerField);
                    if (!actionsByMetaAnnotations) {
                        guiActionSupport.createActionById(pickerField, PickerField.ActionType.CLEAR.getId());
                    }
                } else {
                    guiActionSupport.createActionById(pickerField, PickerField.ActionType.OPEN.getId());
                    guiActionSupport.createActionById(pickerField, PickerField.ActionType.CLEAR.getId());
                }
            } else {
                LookupPickerField lookupPickerField = cubaUiComponents.create(LookupPickerField.class);

                setValueSource(lookupPickerField, context);

                lookupPickerField.setOptions(options);

                pickerField = lookupPickerField;

                guiActionSupport.createActionsByMetaAnnotations(pickerField);
            }

            if (xmlDescriptor != null) {
                String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    pickerField.setCaptionMode(CaptionMode.PROPERTY);
                    pickerField.setCaptionProperty(captionProperty);
                }
            }

            return pickerField;
        } else {
            EntityLinkField linkField = cubaUiComponents.create(EntityLinkField.class);

            setValueSource(linkField, context);
            setLinkFieldAttributes(linkField, context);

            return linkField;
        }
    }

    @Override
    protected void setLinkFieldAttributes(io.jmix.ui.component.EntityLinkField linkField,
                                          ComponentGenerationContext context) {
        super.setLinkFieldAttributes(linkField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String linkScreenOpenType = xmlDescriptor.attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(linkScreenOpenType)) {
                ((EntityLinkField) linkField).setScreenOpenType(OpenType.valueOf(linkScreenOpenType));
            }

            String linkScreenAttribute = xmlDescriptor.attributeValue("linkScreen");
            if (StringUtils.isNotEmpty(linkScreenAttribute)) {
                linkField.setScreen(linkScreenAttribute);
            }

            String invokeMethodName = xmlDescriptor.attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                linkField.setCustomClickHandler(new InvokeEntityLinkClickHandler(invokeMethodName));
            }
        }
    }

    protected static class InvokeEntityLinkClickHandler implements Consumer<io.jmix.ui.component.EntityLinkField> {
        protected final String invokeMethodName;

        public InvokeEntityLinkClickHandler(String invokeMethodName) {
            this.invokeMethodName = invokeMethodName;
        }

        @Override
        public void accept(io.jmix.ui.component.EntityLinkField field) {
            io.jmix.ui.component.Window frame = ComponentsHelper.getWindow(field);
            if (frame == null) {
                throw new IllegalStateException("Please specify Frame for EntityLinkField");
            }

            FrameOwner controller = frame.getFrameOwner();
            Method method;
            try {
                method = controller.getClass().getMethod(invokeMethodName, io.jmix.ui.component.EntityLinkField.class);
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
