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

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("flowui_PropertyFilterComponentGenerationStrategy")
public class PropertyFilterComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    public static final String UNARY_FIELD_CLASS_NAME = "unary-field";

    protected ApplicationContext applicationContext;
//    protected DataAwareComponentsTools dataAwareComponentsTools;

    public PropertyFilterComponentGenerationStrategy(UiComponents uiComponents,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools,
                                                     Actions actions,
                                                     DatatypeRegistry datatypeRegistry,
                                                     Messages messages,
                                                     EntityFieldCreationSupport entityFieldCreationSupport,
                                                     ApplicationContext applicationContext/*,
                                                     DataAwareComponentsTools dataAwareComponentsTools*/) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
//        this.dataAwareComponentsTools = dataAwareComponentsTools;
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !PropertyFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof PropertyFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());

        if (mpp == null) {
            return null;
        }

        PropertyFilterComponentGenerationContext pfContext = (PropertyFilterComponentGenerationContext) context;
        if (pfContext.getOperation().getType() == Operation.Type.UNARY) {
            return createUnaryField(context);
        } else if (pfContext.getOperation().getType() == Operation.Type.LIST) {
            return createCollectionField(context, mpp);
        } else if (pfContext.getOperation().getType() == Operation.Type.INTERVAL) {
            return createIntervalField(context, mpp);
        }

        return super.createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createEntityField(ComponentGenerationContext context) {
        Component entityComponent = entityFieldCreationSupport.createEntityField(context, true);

        if (entityComponent == null) {
            return null;
        }

        setValueSource((SupportsValueSource<?>) entityComponent, context);
        return entityComponent;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Datatype datatype = mpp.getRange().asDatatype();

        Component field = super.createDatatypeField(context, mpp);
        if (field instanceof SupportsDatatype) {
            ((SupportsDatatype<?>) field).setDatatype(datatype);
        }

        return field;
    }

    @Override
    protected Component createDatePicker(ComponentGenerationContext context) {
        Component datePicker = super.createDatePicker(context);

        // TODO: gg, implement
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        if (mpp != null) {
//            dataAwareComponentsTools.setupDateFormat(datePicker, mpp.getMetaProperty());
        }

        return datePicker;
    }

    @Override
    protected Component createDateTimePicker(ComponentGenerationContext context) {
        Component dateTimePicker = super.createDateTimePicker(context);

        // TODO: gg, implement
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        if (mpp != null) {
//            dataAwareComponentsTools.setupDateFormat(datePicker, mpp.getMetaProperty());
        }

        return dateTimePicker;
    }

    @Override
    protected Component createBooleanField(ComponentGenerationContext context) {
        return createUnaryField(context);
    }

    @SuppressWarnings("unchecked")
    protected Component createUnaryField(ComponentGenerationContext context) {
        JmixSelect<Boolean> component = uiComponents.create(JmixSelect.class);
        component.setEmptySelectionAllowed(true);
        component.addClassName(UNARY_FIELD_CLASS_NAME);

        ComponentUtils.setItemsMap(component, ImmutableMap.of(
                Boolean.TRUE, messages.getMessage("boolean.yes"),
                Boolean.FALSE, messages.getMessage("boolean.no")
        ));

        return component;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Component createEnumField(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        if (mpp == null) {
            throw new RuntimeException(String.format("Meta properties path not found: %s.%s",
                    metaClass.getName(), context.getProperty()));
        }

        Enumeration<?> enumeration = mpp.getRange().asEnumeration();
        JmixSelect<?> component = uiComponents.create(JmixSelect.class);
        component.setItems(enumeration.getJavaClass());
        component.setEmptySelectionAllowed(true);

        return component;
    }

    protected Component createIntervalField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        /*ValuePicker<BaseDateInterval> valuePicker = uiComponents.create(ValuePicker.NAME);

        DateIntervalAction intervalAction = applicationContext.getBean(DateIntervalAction.class);
        intervalAction.setMetaPropertyPath(mpp);

        valuePicker.addAction(intervalAction);
        valuePicker.addAction(actions.create(ValueClearAction.ID));
        return valuePicker;*/
        // TODO: gg, implement
        return null;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
