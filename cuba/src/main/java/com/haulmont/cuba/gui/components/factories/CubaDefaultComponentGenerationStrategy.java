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
import com.haulmont.cuba.gui.components.FileUploadField;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.factory.DefaultComponentGenerationStrategy;
import io.jmix.ui.component.impl.GuiActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component(CubaDefaultComponentGenerationStrategy.NAME)
public class CubaDefaultComponentGenerationStrategy extends DefaultComponentGenerationStrategy {
    public static final String NAME = "cuba_DefaultMetaComponentStrategy";

    @Autowired
    public CubaDefaultComponentGenerationStrategy(Messages messages, GuiActionSupport guiActionSupport) {
        super(messages, guiActionSupport);
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
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    @Override
    public int getOrder() {
        return LOWEST_PLATFORM_PRECEDENCE - 10;
    }
}
