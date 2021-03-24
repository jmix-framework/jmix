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

package io.jmix.ui.app.bulk.factory;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.app.bulk.BulkEditorWindow;
import io.jmix.ui.component.*;
import io.jmix.ui.component.factory.AbstractComponentGenerationStrategy;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component("ui_BulkEditComponentGenerationStrategy")
public class BulkEditComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected static final int MAX_TEXTFIELD_STRING_LENGTH = 255;

    @Autowired
    public BulkEditComponentGenerationStrategy(Messages messages,
                                               UiComponents uiComponents,
                                               EntityFieldCreationSupport entityFieldCreationSupport,
                                               Metadata metadata,
                                               MetadataTools metadataTools,
                                               Icons icons,
                                               Actions actions) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
    }


    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !BulkEditorWindow.class.isAssignableFrom(context.getTargetClass())) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Override
    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Integer textLength = (Integer) mpp.getMetaProperty().getAnnotations().get("length");
        boolean isLong = textLength == null || textLength > MAX_TEXTFIELD_STRING_LENGTH;

        TextInputField textField;
        if (isLong) {
            TextArea textArea = uiComponents.create(TextArea.NAME);
            textArea.setRows(3);
            textField = textArea;
        } else {
            textField = uiComponents.create(TextField.NAME);
        }

        setValueSource(textField, context);

        if (textLength != null
                && textField instanceof TextInputField.MaxLengthLimited) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(textLength);
        }

        return textField;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context) {
        ComboBox<Boolean> comboBox = uiComponents.create(ComboBox.NAME);
        setValueSource(comboBox, context);

        Map<String, Boolean> options = new HashMap<>();
        options.put(messages.getMessage("boolean.yes"), Boolean.TRUE);
        options.put(messages.getMessage("boolean.no"), Boolean.FALSE);

        comboBox.setOptionsMap(options);

        return comboBox;
    }

    @Override
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        MetaClass metaClass = mpp.getMetaProperty().getRange().asClass();
        EntityPicker entityPicker = entityFieldCreationSupport.createEntityField(metaClass, context.getOptions());

        setValueSource(entityPicker, context);

        return entityPicker;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 30;
    }
}
