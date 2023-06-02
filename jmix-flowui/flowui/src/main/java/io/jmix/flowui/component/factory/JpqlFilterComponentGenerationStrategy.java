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

package io.jmix.flowui.component.factory;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import org.springframework.lang.Nullable;
import java.sql.Time;
import java.time.*;
import java.util.Date;
import java.util.UUID;

@org.springframework.stereotype.Component("flowui_JpqlFilterComponentGenerationStrategy")
public class JpqlFilterComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    public static final String UNARY_FIELD_CLASS_NAME = "unary-field";

//    protected DataAwareComponentsTools dataAwareComponentsTools;

    @Autowired
    public JpqlFilterComponentGenerationStrategy(UiComponents uiComponents,
                                                 Metadata metadata,
                                                 MetadataTools metadataTools,
                                                 Actions actions,
                                                 DatatypeRegistry datatypeRegistry,
                                                 Messages messages,
                                                 EntityFieldCreationSupport entityFieldCreationSupport/*,
                                                 DataAwareComponentsTools dataAwareComponentsTools*/) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
//        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !JpqlFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof JpqlFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext filterContext = (JpqlFilterComponentGenerationContext) context;
        Class<?> parameterClass = filterContext.getParameterClass();

        if (filterContext.hasInExpression()) {
            return createCollectionField(context);
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            return createEntityField(context);
        } else if (Enum.class.isAssignableFrom(parameterClass)) {
            return createEnumField(context);
        } else if (datatypeRegistry.find(parameterClass) != null) {
            Component dataTypeField = createDatatypeField(context, parameterClass);
            if (dataTypeField instanceof SupportsDatatype) {
                Datatype datatype = datatypeRegistry.find(parameterClass);
                if (datatype != null) {
                    ((SupportsDatatype<?>) dataTypeField).setDatatype(datatype);
                }
            }
            return dataTypeField;
        } else if (Void.class.isAssignableFrom(parameterClass)) {
            return createVoidField(context);
        }

        return super.createComponentInternal(context);
    }

    @Nullable
    protected Component createDatatypeField(ComponentGenerationContext context, Class<?> type) {
        if (type.equals(String.class)
                || type.equals(UUID.class)) {
            return createStringField(context);
        } else if (type.equals(Boolean.class)) {
            return createBooleanField(context);
        } else if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)) {
            return createDatePicker(context);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {
            return createTimePicker(context);
        } else if (type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {
            return createDateTimePicker(context);
        } else if (Number.class.isAssignableFrom(type)) {
            return createNumberField(context);
        }
        return null;
    }

    protected Component createStringField(ComponentGenerationContext context) {
        return uiComponents.create(TypedTextField.class);
    }

    protected Component createCollectionField(ComponentGenerationContext context) {
        // TODO: gg, implement
        /*JpqlFilterComponentGenerationContext cfContext = (JpqlFilterComponentGenerationContext) context;
        Class parameterClass = cfContext.getParameterClass();

        ValuesPicker valuesPicker = uiComponents.create(ValuesPicker.class);
        setValueSource(valuesPicker, cfContext);

        ValuesSelectAction selectAction = actions.create(ValuesSelectAction.class);

        if (Entity.class.isAssignableFrom(parameterClass)) {
            MetaClass metaClass = metadata.getClass(cfContext.getParameterClass());
            selectAction.setEntityName(metaClass.getName());
        } else if (EnumClass.class.isAssignableFrom(parameterClass)) {
            selectAction.setEnumClass(parameterClass);
        } else if (datatypeRegistry.find(parameterClass) != null) {
            Datatype datatype = datatypeRegistry.get(parameterClass);
            selectAction.setJavaClass(datatype.getJavaClass());
        }
        valuesPicker.addAction(selectAction);

        ValueClearAction valueClearAction = actions.create(ValueClearAction.class);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;*/
        return null;
    }

    protected Component createEntityField(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext filterContext = (JpqlFilterComponentGenerationContext) context;
        MetaClass metaClass = metadata.getClass(filterContext.getParameterClass());

        EntityPicker<?> field = uiComponents.create(EntityPicker.class);
        field.setMetaClass(metaClass);
        field.addAction(actions.create(EntityLookupAction.ID));
        field.addAction(actions.create(EntityClearAction.ID));

        return field;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Component createEnumField(ComponentGenerationContext context) {
        JpqlFilterComponentGenerationContext filterContext = (JpqlFilterComponentGenerationContext) context;
        Class parameterClass = filterContext.getParameterClass();

        JmixSelect<?> component = uiComponents.create(JmixSelect.class);
        component.setItems(parameterClass);
        component.setEmptySelectionAllowed(true);

        return component;
    }

    @Override
    protected Component createDatePicker(ComponentGenerationContext context) {
        Component datePicker = super.createDatePicker(context);

        // TODO: gg, implement
        JpqlFilterComponentGenerationContext filterContext = (JpqlFilterComponentGenerationContext) context;
        Class<?> parameterClass = filterContext.getParameterClass();
//        dataAwareComponentsTools.setupDateFormat(dateField, parameterClass);

        return datePicker;
    }

    @Override
    protected Component createDateTimePicker(ComponentGenerationContext context) {
        Component dateTimePicker = super.createDateTimePicker(context);

        // TODO: gg, implement
        JpqlFilterComponentGenerationContext filterContext = (JpqlFilterComponentGenerationContext) context;
        Class<?> parameterClass = filterContext.getParameterClass();
//        dataAwareComponentsTools.setupDateFormat(dateField, parameterClass);

        return dateTimePicker;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Component createBooleanField(ComponentGenerationContext context) {
        JmixSelect<Boolean> component = uiComponents.create(JmixSelect.class);
        // TODO: gg, style or theme?
        component.addClassName(UNARY_FIELD_CLASS_NAME);

        FlowuiComponentUtils.setItemsMap(component, ImmutableMap.of(
                Boolean.TRUE, messages.getMessage("boolean.yes"),
                Boolean.FALSE, messages.getMessage("boolean.no")
        ));

        return component;
    }

    protected Component createVoidField(ComponentGenerationContext context) {
        // TODO: gg, check box, really?
        //  kd, by now - select with yes/no seems much better
        return createBooleanField(context);
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
