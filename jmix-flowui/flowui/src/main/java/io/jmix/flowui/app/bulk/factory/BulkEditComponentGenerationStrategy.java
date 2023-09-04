/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.bulk.factory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextFieldBase;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.bulk.BulkEditView;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.factory.AbstractComponentGenerationStrategy;
import io.jmix.flowui.component.factory.EntityFieldCreationSupport;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.SupportsValueSource;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("ui_BulkEditComponentGenerationStrategy")
public class BulkEditComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected static final int MAX_TEXTFIELD_STRING_LENGTH = 255;
    protected static final String TEXT_AREA_HEIGHT = "5em";

    public BulkEditComponentGenerationStrategy(UiComponents uiComponents,
                                               Metadata metadata,
                                               MetadataTools metadataTools,
                                               Actions actions,
                                               DatatypeRegistry datatypeRegistry,
                                               Messages messages,
                                               EntityFieldCreationSupport entityFieldCreationSupport) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !BulkEditView.class.isAssignableFrom(context.getTargetClass())) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Override
    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Component datatypeField = super.createDatatypeField(context, mpp);
        if (datatypeField instanceof SupportsDatatype<?> supportsDatatype) {
            //noinspection rawtypes
            Datatype datatype = datatypeRegistry.find(mpp.getMetaProperty().getJavaType());
            //noinspection unchecked
            supportsDatatype.setDatatype(datatype);
        }
        return datatypeField;
    }

    @Override
    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Integer textLength = (Integer) mpp.getMetaProperty().getAnnotations().get(MetadataTools.LENGTH_ANN_NAME);

        boolean isLong = textLength == null || textLength > MAX_TEXTFIELD_STRING_LENGTH;

        TextFieldBase<?, ?> textField;
        if (isLong) {
            JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
            textArea.setHeight(TEXT_AREA_HEIGHT);
            if (textLength != null) {
                textArea.setMaxLength(textLength);
            }
            textField = textArea;
        } else {
            TypedTextField<?> typedTextField = uiComponents.create(TypedTextField.class);
            typedTextField.setMaxLength(textLength);
            textField = typedTextField;
        }

        setValueSource((SupportsValueSource<?>) textField, context);

        return textField;
    }

    @Override
    protected Component createBooleanField(ComponentGenerationContext context) {
        //noinspection unchecked
        Select<Boolean> select = uiComponents.create(Select.class);
        setValueSource((SupportsValueSource<?>) select, context);

        select.setItemLabelGenerator(item -> {
            if (Boolean.TRUE.equals(item)) {
                return messages.getMessage("boolean.yes");
            } else if (Boolean.FALSE.equals(item)) {
                return messages.getMessage("boolean.no");
            } else {
                return "";
            }
        });
        select.setItems(Boolean.TRUE, Boolean.FALSE);
        select.setEmptySelectionAllowed(true);

        return select;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 30;
    }
}
