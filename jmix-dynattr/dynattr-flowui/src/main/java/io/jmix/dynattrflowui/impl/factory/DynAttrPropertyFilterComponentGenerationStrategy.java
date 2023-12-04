/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dynattrflowui.impl.factory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrflowui.impl.AttributeDependencies;
import io.jmix.dynattrflowui.impl.AttributeOptionsLoader;
import io.jmix.dynattrflowui.impl.AttributeValidators;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.factory.PropertyFilterComponentGenerationContext;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.ViewRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.List;

import static io.jmix.dynattr.AttributeType.ENTITY;

@org.springframework.stereotype.Component("dynat_DynAttrPropertyFilterComponentGenerationStrategy")
public class DynAttrPropertyFilterComponentGenerationStrategy extends DynAttrComponentGenerationStrategy {

    private static final Logger log = LoggerFactory.getLogger(DynAttrPropertyFilterComponentGenerationStrategy.class);

    public DynAttrPropertyFilterComponentGenerationStrategy(Messages messages,
                                                            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                                            UiComponents uiComponents,
                                                            DynAttrMetadata dynamicModelMetadata,
                                                            Metadata metadata,
                                                            MsgBundleTools msgBundleTools,
                                                            AttributeOptionsLoader optionsLoader,
                                                            AttributeValidators attributeValidators,
                                                            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                                            ViewRegistry viewRegistry,
                                                            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                                            Actions actions,
                                                            AttributeDependencies attributeDependencies,
                                                            FormatStringsRegistry formatStringsRegistry,
                                                            ApplicationContext applicationContext,
                                                            DatatypeRegistry datatypeRegistry) {
        super(messages, uiComponents, dynamicModelMetadata, metadata, msgBundleTools, optionsLoader,
                attributeValidators, viewRegistry, actions, attributeDependencies,
                formatStringsRegistry, applicationContext, datatypeRegistry);
    }


    @Override
    public Component createComponent(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        String propertyName = context.getProperty();

        if (!DynAttrUtils.isDynamicAttributeProperty(propertyName)
                || context.getTargetClass() == null
                || !PropertyFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof PropertyFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context, metaClass, propertyName);
    }

    @Override
    protected Component createComponentInternal(ComponentGenerationContext context, MetaClass metaClass, String propertyName) {
        AttributeDefinition attribute = dynamicModelMetadata.getAttributeByCode(metaClass,
                DynAttrUtils.getAttributeCodeFromProperty(propertyName)).orElse(null);

        if (attribute == null) {
            return null;
        }

        PropertyFilterComponentGenerationContext pfContext = (PropertyFilterComponentGenerationContext) context;
        PropertyFilter.Operation.Type type = pfContext.getOperation().getType();

        Component resultComponent;
        if (type == PropertyFilter.Operation.Type.UNARY) {
            resultComponent = createUnaryField(context);
        } else if (attribute.isCollection() || type == PropertyFilter.Operation.Type.LIST) {
            resultComponent = createCollectionField(context, attribute);
        } else if (attribute.getDataType() == ENTITY) {
            resultComponent = createEntityField(context, attribute);
        } else if (type == PropertyFilter.Operation.Type.INTERVAL) {
            resultComponent = createIntervalField(context);
        } else {
            resultComponent = createDatatypeField(context, attribute);
        }

        return resultComponent;
    }

    @Override
    protected AbstractField<?, ?> createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        return createUnaryField(context);
    }

    protected AbstractField<?, ?> createUnaryField(@SuppressWarnings("unused") ComponentGenerationContext context) {
        //noinspection unchecked
        JmixComboBox<Boolean> component = uiComponents.create(JmixComboBox.class);
        ComponentUtils.setItemsMap(component, ImmutableMap.of(
                Boolean.TRUE, messages.getMessage("boolean.yes"),
                Boolean.FALSE, messages.getMessage("boolean.no")
        ));

        return component;
    }

    @Override
    protected AbstractField createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        EntityPickerComponent<?> entityPicker = (EntityPickerComponent<?>) super.createEntityField(context, attribute);

        Class<?> javaType = attribute.getJavaType();
        Assert.notNull(javaType, "Java type is null for current attribute");
        MetaClass metaClass = metadata.getClass(javaType);

        entityPicker.setMetaClass(metaClass);

        loadOptionsIfNeed((Component) entityPicker, attribute);

        return (AbstractField) entityPicker;
    }

    protected void loadOptionsIfNeed(Component component, AttributeDefinition attribute) {
        if (attribute.getConfiguration().isLookup() && component instanceof ComboBox) {
            try {
                List<?> options = optionsLoader.loadOptions(null, attribute);
                //noinspection rawtypes,unchecked
                ((ComboBox) component).setItems(options);
            } catch (RuntimeException e) {
                log.error("Cannot load options for dynamic attribute '{}' in filter condition. " +
                        "It may be caused by NULL value of 'entity' options script parameter." +
                        "Please, consider nullability, do not use lookup field for this attribute " +
                        "or do not use this attribute in filter.", attribute.getName(), e);
            }
        }
    }

    protected AbstractField<?, ?> createIntervalField(@SuppressWarnings("unused") ComponentGenerationContext context) {
        ValuePicker<?> valuePicker = uiComponents.create(ValuePicker.class);
//        valuePicker.addAction(applicationContext.getBean(DateIntervalAction.class));
        valuePicker.addAction(actions.create(ValueClearAction.ID));
        return valuePicker;
    }

    @Override
    protected TypedDateTimePicker<?> createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        //     todo   dataAwareComponentsTools.setupDateFormat(dateField, attribute.getMetaProperty());
        return super.createDateField(context, attribute);
    }

    @Override
    protected Component createDatatypeField(ComponentGenerationContext context, AttributeDefinition attribute) {
        Component field = super.createDatatypeField(context, attribute);

        Range range = attribute.getMetaProperty().getRange();
        if (field instanceof SupportsTypedValue && range.isDatatype()) {
            //noinspection rawtypes,unchecked
            ((SupportsTypedValue) field).setTypedValue(attribute.getJavaType());
        }

        loadOptionsIfNeed(field, attribute);
        return field;
    }

    @Override
    protected void setLookupActionScreen(EntityLookupAction<?> lookupAction, AttributeDefinition attribute) {
        String screen = attribute.getConfiguration().getLookupScreen();
        if (!Strings.isNullOrEmpty(screen)) {
            lookupAction.setViewId(screen);
        }
    }


    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 10;
    }
}
