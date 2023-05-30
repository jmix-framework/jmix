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

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import org.springframework.core.Ordered;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Component("flowui_InputDialogGenerationStrategy")
public class InputDialogGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected static final List<Class<?>> dateTimeDatatypes = ImmutableList.of(DateTimeDatatype.class,
            LocalDateTimeDatatype.class, OffsetDateTimeDatatype.class);

    protected static final List<Class<?>> dateDatatypes = ImmutableList.of(DateDatatype.class,
            LocalDateDatatype.class);

    protected static final List<Class<?>> timeDatatypes = ImmutableList.of(TimeDatatype.class, LocalTimeDatatype.class,
            OffsetTimeDatatype.class);

    public InputDialogGenerationStrategy(UiComponents uiComponents,
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
        if (context.getTargetClass() != null &&
                InputDialog.class.isAssignableFrom(context.getTargetClass())
                && context instanceof InputDialogGenerationContext) {
            return createComponentInternal(context);
        }

        return null;
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        InputParameter parameter = ((InputDialogGenerationContext) context).getInputParameter();

        if (parameter.getEntityClass() != null) {
            return createEntityField(context);
        } else if (parameter.getEnumClass() != null) {
            return createEnumField(context);
        }

        Datatype<?> datatype = null;
        if (parameter.getDatatypeJavaClass() != null) {
            datatype = datatypeRegistry.find(parameter.getDatatypeJavaClass());
        } else if (parameter.getDatatype() != null) {
            datatype = parameter.getDatatype();
        }

        if (datatype == null) {
            datatype = datatypeRegistry.get(String.class);
        }

        if (datatype instanceof NumberDatatype
                || datatype instanceof StringDatatype) {
            return createTextField(datatype);
        } else if (isDateTimeBasedDatatype(datatype)) {
            return createDateTimeField(datatype);
        } else if (isDateBasedDatatype(datatype)) {
            return createDateField(datatype);
        } else if (isTimeBasedDatatype(datatype)) {
            return createTimeField(datatype);
        } else if (datatype instanceof BooleanDatatype) {
            return createBooleanField();
        } else {
            throw new IllegalArgumentException("InputDialog doesn't support datatype: " + datatype.getClass());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTextField(Datatype<?> datatype) {
        TypedTextField field = uiComponents.create(TypedTextField.class);

        field.setWidthFull();
        field.setDatatype(datatype);

        return field;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateTimeField(Datatype<?> datatype) {
        TypedDateTimePicker dateTimeField = uiComponents.create(TypedDateTimePicker.class);
        dateTimeField.setDatatype(datatype);

        return dateTimeField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createDateField(Datatype<?> datatype) {
        TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);
        dateField.setDatatype(datatype);

        return dateField;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Component createTimeField(Datatype<?> datatype) {
        TypedTimePicker timeField = uiComponents.create(TypedTimePicker.class);
        timeField.setDatatype(datatype);

        return timeField;
    }

    @Nullable
    @Override
    protected Component createEntityField(ComponentGenerationContext context) {
        InputParameter parameter = ((InputDialogGenerationContext) context).getInputParameter();

        MetaClass metaClass = metadata.getClass(Objects.requireNonNull(parameter.getEntityClass()));

        EntityPicker<?> entityPicker = uiComponents.create(EntityPicker.class);

        entityPicker.setMetaClass(metaClass);
        entityPicker.setWidthFull();
        entityPicker.addAction(actions.create(EntityLookupAction.ID));
        entityPicker.addAction(actions.create(EntityClearAction.ID));

        return entityPicker;
    }

    protected Component createBooleanField() {
        return uiComponents.create(JmixCheckbox.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Component createEnumField(ComponentGenerationContext context) {
        InputParameter parameter = ((InputDialogGenerationContext) context).getInputParameter();

        JmixComboBox comboBox = uiComponents.create(JmixComboBox.class);

        comboBox.setItems(Objects.requireNonNull(parameter.getEnumClass()));
        comboBox.setWidthFull();

        return comboBox;
    }

    protected boolean isDateBasedDatatype(Datatype<?> datatype) {
        return dateDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    protected boolean isTimeBasedDatatype(Datatype<?> datatype) {
        return timeDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    protected boolean isDateTimeBasedDatatype(Datatype<?> datatype) {
        return dateTimeDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 500;
    }
}
