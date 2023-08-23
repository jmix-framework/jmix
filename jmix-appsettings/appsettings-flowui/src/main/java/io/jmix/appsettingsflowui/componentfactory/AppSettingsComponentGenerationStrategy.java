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

package io.jmix.appsettingsflowui.componentfactory;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.select.Select;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.factory.AbstractComponentGenerationStrategy;
import io.jmix.flowui.component.factory.EntityFieldCreationSupport;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("appsettings_AppSettingsComponentGenerationStrategy")
public class AppSettingsComponentGenerationStrategy
        extends AbstractComponentGenerationStrategy implements Ordered {

    private static final int MAX_TEXT_FIELD_STRING_LENGTH = 255;

    public AppSettingsComponentGenerationStrategy(UiComponents uiComponents,
                                                  Metadata metadata,
                                                  MetadataTools metadataTools,
                                                  Actions actions,
                                                  DatatypeRegistry datatypeRegistry,
                                                  Messages messages,
                                                  EntityFieldCreationSupport entityFieldCreationSupport) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getMetaClass() != null
                && context.getProperty() != null
                && AppSettingsEntity.class.isAssignableFrom(context.getMetaClass().getJavaClass())) {
            MetaProperty metaProperty = context.getMetaClass().getProperty(context.getProperty());
            Range range = metaProperty.getRange();

            AbstractField field = null;

            ValueSource valueSource = context.getValueSource();
            if (valueSource != null) {
                Object containerItem = ((ContainerValueSource) valueSource).getContainer().getItemOrNull();
                if (requireTextArea(metaProperty, containerItem)) {
                    field = uiComponents.create(TypedTextField.class);
                }
            }

            if (isBoolean(metaProperty)) {
                field = createBooleanField();
            }

            if (isSecret(metaProperty)) {
                field = createPasswordField();
            }

            if (range.isEnum()) {
                field = createEnumField(range);
            }

            if (range.isClass()) {
                field = createEntityPickerField();
            }

            if (field instanceof SupportsValueSource) {
                ((SupportsValueSource<?>) field).setValueSource(valueSource);
            }
            return field;
        }

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Select<?> createEnumField(Range range) {
        JmixSelect enumField = uiComponents.create(JmixSelect.class);
        enumField.setItems(range.asEnumeration().getJavaClass());

        return enumField;
    }

    protected EntityPicker<?> createEntityPickerField() {
        EntityPicker<?> pickerField = uiComponents.create(EntityPicker.class);
        EntityLookupAction<?> lookupAction = actions.create(EntityLookupAction.ID);
        pickerField.addAction(lookupAction);
        pickerField.addAction(actions.create(EntityClearAction.ID));

        return pickerField;
    }

    protected Select<Boolean> createBooleanField() {
        //noinspection unchecked
        JmixSelect<Boolean> field = uiComponents.create(JmixSelect.class);

        ComponentUtils.setItemsMap(field, ImmutableMap.of(
                Boolean.TRUE, messages.getMessage("trueString"),
                Boolean.FALSE, messages.getMessage("falseString")
        ));

        return field;
    }

    @SuppressWarnings("rawtypes")
    protected AbstractField createPasswordField() {
        return uiComponents.create(JmixPasswordField.class);
    }

    protected static boolean isSecret(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().isAnnotationPresent(Secret.class);
    }


    protected boolean isBoolean(MetaProperty metaProperty) {
        return metaProperty.getRange().isDatatype()
                && metaProperty.getRange().asDatatype().getJavaClass().equals(Boolean.class);
    }

    protected boolean requireTextArea(MetaProperty metaProperty, @Nullable Object item) {
        if (!String.class.equals(metaProperty.getJavaType())) {
            return false;
        }

        Integer textLength = (Integer) metaProperty.getAnnotations().get("length");
        boolean isLong = textLength != null && textLength > MAX_TEXT_FIELD_STRING_LENGTH;

        Object value = item != null ? EntityValues.getValue(item, metaProperty.getName()) : null;
        boolean isContainsSeparator = value != null && containsSeparator((String) value);

        return isLong || isContainsSeparator;
    }

    protected boolean containsSeparator(String s) {
        return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
