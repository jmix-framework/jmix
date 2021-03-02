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

package com.haulmont.cuba.gui.dynamicattributes;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.components.FileUploadField;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattrui.MsgBundleTools;
import io.jmix.dynattrui.impl.AttributeDependencies;
import io.jmix.dynattrui.impl.AttributeOptionsLoader;
import io.jmix.dynattrui.impl.AttributeValidators;
import io.jmix.dynattrui.impl.DynAttrComponentGenerationStrategy;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.Field;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@org.springframework.stereotype.Component("cuba_DynamicAttributeComponentGenerationStrategy")
public class CubaDynAttrComponentGenerationStrategy extends DynAttrComponentGenerationStrategy {

    protected Icons icons;

    @Autowired
    public CubaDynAttrComponentGenerationStrategy(Messages messages,
                                                  UiComponents uiComponents,
                                                  DynAttrMetadata dynamicModelMetadata,
                                                  Metadata metadata,
                                                  MsgBundleTools msgBundleTools,
                                                  AttributeOptionsLoader optionsLoader,
                                                  AttributeValidators attributeValidators,
                                                  WindowConfig windowConfig,
                                                  ScreensHelper screensHelper,
                                                  Actions actions,
                                                  AttributeDependencies attributeDependencies,
                                                  FormatStringsRegistry formatStringsRegistry,
                                                  Icons icons,
                                                  ApplicationContext applicationContext) {
        super(messages, uiComponents, dynamicModelMetadata, metadata, msgBundleTools, optionsLoader, attributeValidators,
                windowConfig, screensHelper, actions, attributeDependencies, formatStringsRegistry, applicationContext);

        this.icons = icons;
    }

    @Override
    protected Component createClassField(ComponentGenerationContext context, AttributeDefinition attribute) {
        Class<?> javaType = attribute.getJavaType();

        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createCubaFileUploadField(context);
        } else {
            return createEntityField(context, attribute);
        }
    }

    /**
     * Creates FileUploadField working with FileDescriptor.
     */
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
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 15;
    }
}
