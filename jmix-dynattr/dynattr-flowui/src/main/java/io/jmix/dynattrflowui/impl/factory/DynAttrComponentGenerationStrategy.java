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

package io.jmix.dynattrflowui.impl.factory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.*;
import io.jmix.dynattrflowui.impl.AttributeDependencies;
import io.jmix.dynattrflowui.impl.AttributeOptionsLoader;
import io.jmix.dynattrflowui.impl.AttributeRecalculationListener;
import io.jmix.dynattrflowui.impl.AttributeValidators;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.Views;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.ComponentGenerationStrategy;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerBase;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.dynattr.AttributeType.*;

@org.springframework.stereotype.Component("dynat_DynAttrComponentGenerationStrategy")
public class DynAttrComponentGenerationStrategy implements ComponentGenerationStrategy, Ordered {
    protected Messages messages;
    protected UiComponents uiComponents;
    protected DynAttrMetadata dynamicModelMetadata;
    protected Metadata metadata;
    protected MsgBundleTools msgBundleTools;
    protected AttributeOptionsLoader optionsLoader;
    protected AttributeValidators attributeValidators;
//    protected WindowConfig windowConfig;
    protected ViewRegistry viewRegistry;
    protected Actions actions;
    protected AttributeDependencies attributeDependencies;
    protected FormatStringsRegistry formatStringsRegistry;
    protected ApplicationContext applicationContext;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public DynAttrComponentGenerationStrategy(Messages messages,
                                              UiComponents uiComponents,
                                              DynAttrMetadata dynamicModelMetadata,
                                              Metadata metadata,
                                              MsgBundleTools msgBundleTools,
                                              AttributeOptionsLoader optionsLoader,
                                              AttributeValidators attributeValidators,
//                                              WindowConfig windowConfig,
                                              ViewRegistry viewRegistry,
                                              Actions actions,
                                              AttributeDependencies attributeDependencies,
                                              FormatStringsRegistry formatStringsRegistry,
                                              ApplicationContext applicationContext) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.dynamicModelMetadata = dynamicModelMetadata;
        this.metadata = metadata;
        this.msgBundleTools = msgBundleTools;
        this.optionsLoader = optionsLoader;
        this.attributeValidators = attributeValidators;
//        this.windowConfig = windowConfig;
        this.viewRegistry = viewRegistry;
        this.actions = actions;
        this.attributeDependencies = attributeDependencies;
        this.formatStringsRegistry = formatStringsRegistry;
        this.applicationContext = applicationContext;
    }

    public Component createComponent(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        String propertyName = context.getProperty();

        if (!DynAttrUtils.isDynamicAttributeProperty(propertyName)) {
            return null;
        }

        return createComponentInternal(context, metaClass, propertyName);
    }

    protected Component createComponentInternal(ComponentGenerationContext context, MetaClass metaClass, String propertyName) {
        AttributeDefinition attribute = dynamicModelMetadata.getAttributeByCode(metaClass,
                DynAttrUtils.getAttributeCodeFromProperty(propertyName)).orElse(null);

        if (attribute == null) {
            return null;
        }

        Component resultComponent;
        if (attribute.isCollection()) {
            resultComponent = createCollectionField(context, attribute);
        } else if (attribute.getDataType() == ENTITY) {
            resultComponent = createClassField(context, attribute);
        } else {
            resultComponent = createDatatypeField(context, attribute);
        }

        if (resultComponent instanceof HasValue) {
            setValueChangedListeners((HasValue) resultComponent, attribute);
        }

        if (resultComponent instanceof HasValueAndElement) {
            setEditable((HasValueAndElement) resultComponent, attribute);
            setRequired((HasValueAndElement) resultComponent, attribute);
        }

        if (resultComponent instanceof HasLabel) {
            setCaption((HasLabel) resultComponent, attribute);
        }

        if (resultComponent instanceof HasTitle) {
            setDescription((HasTitle) resultComponent, attribute);
        }

        if (resultComponent instanceof HasValueAndElement) {

        }

        return resultComponent;
    }

    protected Component createClassField(ComponentGenerationContext context, AttributeDefinition attribute) {
        return createEntityField(context, attribute);
    }

    protected Component createDatatypeField(ComponentGenerationContext context, AttributeDefinition attribute) {
        AttributeType type = attribute.getDataType();
        AttributeDefinition.Configuration configuration = attribute.getConfiguration();

        if (configuration.isLookup()) {
            return createComboBox(context, attribute);
        } else if (type == STRING) {
            return createStringField(context, attribute);
        } else if (type == BOOLEAN) {
            return createBooleanField(context, attribute);
        } else if (type == DATE || type == DATE_WITHOUT_TIME) {
            return createDateField(context, attribute);
        } else if (type == INTEGER || type == DOUBLE || type == DECIMAL) {
            return createNumberField(context, attribute);
        } else if (type == ENUMERATION) {
            return createEnumerationField(context, attribute);
        }

        return null;
    }

    protected Component createCollectionField(ComponentGenerationContext context, AttributeDefinition attribute) {
        MultiValuePicker valuesPicker = uiComponents.create(MultiValuePicker.class);

//        setValidators((HasValidationProperties) valuesPicker, attribute);
        setValueSource(valuesPicker, context);

        MultiValueSelectAction selectAction = actions.create(MultiValueSelectAction.ID);
        initValuesSelectActionByAttribute(selectAction, attribute);

        if (attribute.getConfiguration().isLookup()) {
            ContainerValueSource valueSource = (ContainerValueSource) ((SupportsValueSource<Object>)valuesPicker).getValueSource();
            setValuesPickerOptionsLoader(valuesPicker, attribute, valueSource);
        }

        valuesPicker.addAction(selectAction);

        ValueClearAction valueClearAction = actions.create(ValueClearAction.ID);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    protected Component createStringField(ComponentGenerationContext context, AttributeDefinition attribute) {
        AbstractField textField;

        Integer rowsCount = attribute.getConfiguration().getRowsCount();
        if (rowsCount != null && rowsCount > 1) {
            TextArea textArea = uiComponents.create(TextArea.class);
//            textArea.setMinLength(rowsCount);
            textField = textArea;
        } else {
            textField = uiComponents.create(TextField.class);
        }

        setValidators((SupportsValidation) textField, attribute);
        setValueSource(textField, context);

        return textField;
    }

    protected Component createEnumerationField(ComponentGenerationContext context, AttributeDefinition attribute) {
        ComboBox comboBox = uiComponents.create(ComboBox.class);
        ComponentUtils.setItemsMap(comboBox, getLocalizedEnumerationMap(attribute));

        setValueSource(comboBox, context);
        setValidators((SupportsValidation) comboBox, attribute);

        return comboBox;
    }

    protected Component createComboBox(ComponentGenerationContext context, AttributeDefinition attribute) {
        ComboBox comboBox = uiComponents.create(ComboBox.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setComboBoxOptionsLoader(comboBox, attribute, (ContainerValueSource) context.getValueSource());
        }

        setValueSource(comboBox, context);
        setValidators((SupportsValidation) comboBox, attribute);

        return comboBox;
    }

    protected AbstractField createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixCheckbox component = uiComponents.create(JmixCheckbox.class);

        //      todo  setValidators(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected Component createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);

        setValidators(dateField, attribute);
        setValueSource(dateField, context);

        return dateField;
    }

    protected AbstractField createNumberField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TypedTextField component = uiComponents.create(TypedTextField.class);

        setValidators(component, attribute);
        setCustomDatatype(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected AbstractField createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        if (attribute.getConfiguration().isLookup()) {
            EntityComboBox entityComboBox = uiComponents.create(EntityComboBox.class);

            if (context.getValueSource() instanceof ContainerValueSource) {
                setComboBoxOptionsLoader(entityComboBox, attribute, (ContainerValueSource) context.getValueSource());
            }

            setValueSource(entityComboBox, context);
            setValidators(entityComboBox, attribute);

            return entityComboBox;
        } else {
            EntityPicker entityPicker = uiComponents.create(EntityPicker.class);

            EntityLookupAction lookupAction = actions.create(EntityLookupAction.ID);

            setLookupActionScreen(lookupAction, attribute);

            entityPicker.addAction(lookupAction);
            entityPicker.addAction(actions.create(EntityClearAction.ID));

            setValueSource(entityPicker, context);
            setValidators(entityPicker, attribute);

            return entityPicker;
        }
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(HasValue field, ComponentGenerationContext context) {

//        field.setValue(.getValue());
    }

    protected void setValidators(SupportsValidation field, AttributeDefinition attribute) {
        Collection<Validator<?>> validators = attributeValidators.getValidators(attribute);
        for (Validator<?> validator : validators) {
            field.addValidator(validator);
        }
    }

    protected void setCustomDatatype(TypedTextField field, AttributeDefinition attribute) {
        String formatPattern = attribute.getConfiguration().getNumberFormatPattern();
        if (!Strings.isNullOrEmpty(formatPattern)) {
            Class<?> type = attribute.getDataType() == DECIMAL ? BigDecimal.class : Number.class;
            //noinspection unchecked
            field.setDatatype(new AdaptiveNumberDatatype(type, formatPattern, "", "", formatStringsRegistry));
        }
    }

    protected void setValueChangedListeners(HasValue component, AttributeDefinition attribute) {
        Set<AttributeDefinition> dependentAttributes = attributeDependencies.getDependentAttributes(attribute);
        if (!dependentAttributes.isEmpty()) {
            //noinspection unchecked
            component.addValueChangeListener(e -> {
                applicationContext.getBean(AttributeRecalculationListener.class, attribute)
                        .accept(new ValueSource.ValueChangeEvent(new ValueSource() {
                            @Override
                            public Object getValue() {
                                return e.getValue();
                            }

                            @Override
                            public void setValue(Object value) {
                                component.setValue(value);
                            }

                            @Override
                            public boolean isReadOnly() {
                                return false;
                            }

                            @Override
                            public Registration addValueChangeListener(Consumer listener) {
                                return null;
                            }

                            @Override
                            public BindingState getState() {
                                return null;
                            }

                            @Override
                            public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
                                return null;
                            }

                            @Override
                            public Class getType() {
                                return null;
                            }
                        }, e.getOldValue(), e.getValue()));
            });
        }
    }

    protected void setEditable(HasValueAndElement component, AttributeDefinition attribute) {
        if (Boolean.TRUE.equals(attribute.isReadOnly())) {
            component.setReadOnly(false);
        }
    }

    protected void setCaption(HasLabel component, AttributeDefinition attribute) {
        component.setLabel(
                msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()));
    }

    protected void setDescription(HasTitle component, AttributeDefinition attribute) {
        component.setTitle(
                msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription()));
    }

    protected void setRequired(HasValueAndElement field, AttributeDefinition attribute) {
        field.setRequiredIndicatorVisible(attribute.isRequired());
        if(field instanceof HasValidationProperties) {
            ((HasValidationProperties) field).setErrorMessage(messages.formatMessage("",
                    "validation.required.defaultMsg",
                    msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName())));
        }
    }

    protected void initValuesSelectActionByAttribute(MultiValueSelectAction selectAction, AttributeDefinition attribute) {
        AttributeType attributeType = attribute.getDataType();
        switch (attributeType) {
            case DATE:
                selectAction.setJavaClass(Date.class);
//                selectAction.setResolution(DateField.Resolution.MIN);
                break;
            case DATE_WITHOUT_TIME:
                selectAction.setJavaClass(LocalDate.class);
//                selectAction.setResolution(DateField.Resolution.DAY);
                break;
            case STRING:
                selectAction.setJavaClass(String.class);
                break;
            case ENUMERATION:
                selectAction.setJavaClass(String.class);
                Map values = getLocalizedEnumerationMap(attribute);
                selectAction.setItemLabelGenerator(item -> values.get(item).toString());
                selectAction.setItems(new ListDataProvider<>(values.keySet().stream().toList()));
                break;
            case DOUBLE:
                selectAction.setJavaClass(Double.class);
                break;
            case DECIMAL:
                selectAction.setJavaClass(BigDecimal.class);
                break;
            case INTEGER:
                selectAction.setJavaClass(Integer.class);
                break;
            case BOOLEAN:
                selectAction.setJavaClass(Boolean.class);
                break;
            case ENTITY:
                Class<?> javaType = attribute.getJavaType();
                if (javaType != null) {
                    MetaClass metaClass = metadata.getClass(javaType);
                    selectAction.setEntityName(metaClass.getName());
                }
                selectAction.setViewId(attribute.getConfiguration().getLookupScreen());
                selectAction.setUseComboBox(attribute.getConfiguration().isLookup());
                break;
        }
    }

    protected Map getLocalizedEnumerationMap(AttributeDefinition attribute) {
        String enumeration = attribute.getEnumeration();
        Map<String, Object> result = new LinkedHashMap<>();
        if (enumeration != null) {
            for (String value : Splitter.on(",").omitEmptyStrings().split(enumeration)) {
                result.put(value, msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), value));
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setValuesPickerOptionsLoader(MultiValuePicker valuesPicker,
                                                AttributeDefinition attribute,
                                                ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        if (entity != null) {
            List options = optionsLoader.loadOptions(entity, attribute);
            ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                    .setItems(new ListDataProvider<>(options));
        }
        container.addItemChangeListener(e -> {
            List options = optionsLoader.loadOptions(e.getItem(), attribute);

            ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                    .setItems(new ListDataProvider(options));
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {
            List<String> codesWithMarkers = dependsOnAttributeCodes.stream().map(a -> '+' + a).collect(Collectors.toList());
            container.addItemPropertyChangeListener(e -> {
                if (codesWithMarkers.contains(e.getProperty())) {
                    List options = optionsLoader.loadOptions(e.getItem(), attribute);
                    ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                            .setItems(new ListDataProvider<>(options));
                    if (!options.contains(valuesPicker.getValue())) {
                        valuesPicker.setValue(null);
                    }
                }
            });
        }
    }

    protected void setComboBoxOptionsLoader(ComboBox lookupField, AttributeDefinition attribute, ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        if (entity != null) {
            List options = optionsLoader.loadOptions(entity, attribute);
            //noinspection unchecked
            lookupField.setItems(options);
        }
        container.addItemChangeListener(e -> {
            List options = optionsLoader.loadOptions(e.getItem(), attribute);
            //noinspection unchecked
            lookupField.setItems(options);
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {
            List<String> codesWithMarkers = dependsOnAttributeCodes.stream().map(a -> '+' + a).collect(Collectors.toList());
            container.addItemPropertyChangeListener(e -> {
                if (codesWithMarkers.contains(e.getProperty())) {
                    List options = optionsLoader.loadOptions(e.getItem(), attribute);
                    //noinspection unchecked
                    lookupField.setItems(options);
                    if (!options.contains(lookupField.getValue())) {
                        //noinspection unchecked
                        lookupField.setValue(null);
                    }
                }
            });
        }
    }

    protected void setLookupActionScreen(EntityLookupAction lookupAction, AttributeDefinition attribute) {
        String screen = attribute.getConfiguration().getLookupScreen();
        if (!Strings.isNullOrEmpty(screen)) {
            lookupAction.setViewId(screen);
        } else {
            Class<?> javaType = attribute.getJavaType();
            MetaClass metaClass = metadata.getClass(javaType);
            screen = viewRegistry.getLookupViewId(metaClass);
            lookupAction.setViewId(screen);
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 20;
    }
}
