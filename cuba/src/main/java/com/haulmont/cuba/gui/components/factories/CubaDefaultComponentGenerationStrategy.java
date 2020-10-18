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
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.GuiActionSupport;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.factory.DefaultComponentGenerationStrategy;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component(CubaDefaultComponentGenerationStrategy.NAME)
public class CubaDefaultComponentGenerationStrategy extends DefaultComponentGenerationStrategy {
    public static final String NAME = "cuba_DefaultComponentGenerationStrategy";

    protected GuiActionSupport guiActionSupport;

    @Autowired
    public CubaDefaultComponentGenerationStrategy(Messages messages,
                                                  UiComponents uiComponents,
                                                  EntityFieldCreationSupport entityFieldCreationSupport,
                                                  Metadata metadata,
                                                  MetadataTools metadataTools,
                                                  Icons icons,
                                                  Actions actions,
                                                  GuiActionSupport guiActionSupport) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);

        this.guiActionSupport = guiActionSupport;
    }

    @Override
    protected Component createClassField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Component field = super.createClassField(context, mpp);
        if (field != null) {
            return field;
        }

        MetaProperty metaProperty = mpp.getMetaProperty();
        Class<?> javaType = metaProperty.getJavaType();
        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createCubaFileUploadField(context);
        }

        return null;
    }

    protected Field createCubaFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = uiComponents.create(FileUploadField.NAME);

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
    protected void setLinkFieldAttributes(io.jmix.ui.component.EntityLinkField linkField, ComponentGenerationContext context) {
        super.setLinkFieldAttributes(linkField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String openTypeAttribute = xmlDescriptor.attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                ((EntityLinkField) linkField).setScreenOpenType(OpenType.valueOf(openTypeAttribute));
            }
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE - 10;
    }

    @Override
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
                pickerField = uiComponents.create(PickerField.class);
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
                LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);

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
//            setValidators(pickerField, context);

            return pickerField;
        } else {
            EntityLinkField linkField = uiComponents.create(EntityLinkField.class);

            setValueSource(linkField, context);
            setLinkFieldAttributes(linkField, context);

            return linkField;
        }
    }
}
