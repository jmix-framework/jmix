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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.textfield.TextArea;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.*;
import io.jmix.dynattrflowui.impl.*;
import io.jmix.dynattrflowui.utils.DataProviderUtils;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.ComponentGenerationStrategy;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    protected ViewRegistry viewRegistry;
    protected Actions actions;
    protected AttributeDependencies attributeDependencies;
    protected FormatStringsRegistry formatStringsRegistry;
    protected ApplicationContext applicationContext;
    protected DatatypeRegistry datatypeRegistry;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public DynAttrComponentGenerationStrategy(Messages messages,
                                              UiComponents uiComponents,
                                              DynAttrMetadata dynamicModelMetadata,
                                              Metadata metadata,
                                              MsgBundleTools msgBundleTools,
                                              AttributeOptionsLoader optionsLoader,
                                              AttributeValidators attributeValidators,
                                              ViewRegistry viewRegistry,
                                              Actions actions,
                                              AttributeDependencies attributeDependencies,
                                              FormatStringsRegistry formatStringsRegistry,
                                              ApplicationContext applicationContext,
                                              DatatypeRegistry datatypeRegistry) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.dynamicModelMetadata = dynamicModelMetadata;
        this.metadata = metadata;
        this.msgBundleTools = msgBundleTools;
        this.optionsLoader = optionsLoader;
        this.attributeValidators = attributeValidators;
        this.viewRegistry = viewRegistry;
        this.actions = actions;
        this.attributeDependencies = attributeDependencies;
        this.formatStringsRegistry = formatStringsRegistry;
        this.applicationContext = applicationContext;
        this.datatypeRegistry = datatypeRegistry;
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
            resultComponent = createEntityField(context, attribute);
        } else {
            resultComponent = createDatatypeField(context, attribute);
        }

        if (resultComponent instanceof HasValue) {
            setValueChangedListeners((HasValue<?, ?>) resultComponent, attribute);
        }

        if (resultComponent instanceof HasValueAndElement) {
            setEditable((HasValueAndElement<?, ?>) resultComponent, attribute);
            setRequired((HasValueAndElement<?, ?>) resultComponent, attribute);
        }

        if (resultComponent instanceof HasLabel) {
            setLabel((HasLabel) resultComponent, attribute);
        }

        if (resultComponent instanceof HasTitle) {
            setDescription((HasTitle) resultComponent, attribute);
        }

        return resultComponent;
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
        } else if (type == DATE) {
            return createDateField(context, attribute);
        } else if (type == DATE_WITHOUT_TIME) {
            return createDateWithoutTimeField(context, attribute);
        } else if (type == INTEGER || type == DOUBLE || type == DECIMAL) {
            return createNumberField(type, context, attribute);
        } else if (type == ENUMERATION) {
            return createEnumerationField(context, attribute);
        }

        return null;
    }

    protected Component createCollectionField(ComponentGenerationContext context, AttributeDefinition attribute) {
        if (attribute.getDataType().equals(ENUMERATION)) {
            return createEnumCollectionField(context, attribute);
        }
        if (!attribute.getConfiguration().isLookup()) {
            return createNonLookupCollectionField(context, attribute);
        }

        JmixMultiSelectComboBoxPicker<?> valuesPicker = uiComponents.create(JmixMultiSelectComboBoxPicker.class);

        setValidators(valuesPicker, attribute);
        setValueProvider(valuesPicker, attribute, context);
        setValueSource(valuesPicker, context);

        MultiValueSelectAction<?> selectAction = actions.create(MultiValueSelectAction.ID);
        initValuesSelectActionByAttribute(selectAction, attribute);
        valuesPicker.addAction(selectAction);

        ContainerValueSource<?, ?> valueSource = (ContainerValueSource<?, ?>) ((SupportsValueSource<?>) valuesPicker).getValueSource();
        if (valueSource != null) {
            setValuesPickerOptionsLoader(valuesPicker, attribute, valueSource);
        }

        ValueClearAction<?> valueClearAction = actions.create(ValueClearAction.ID);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    protected Component createEnumCollectionField(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixMultiSelectComboBoxPicker<?> valuesPicker = uiComponents.create(JmixMultiSelectComboBoxPicker.class);

        setValidators(valuesPicker, attribute);
        setValueProvider(valuesPicker, attribute, context);
        setValueSource(valuesPicker, context);
        setEnumValueSource(valuesPicker, attribute);

        MultiValueSelectAction<?> selectAction = actions.create(MultiValueSelectAction.ID);
        initValuesSelectActionByAttribute(selectAction, attribute);
        valuesPicker.addAction(selectAction);

        ValueClearAction<?> valueClearAction = actions.create(ValueClearAction.ID);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    private void setEnumValueSource(JmixMultiSelectComboBoxPicker<?> valuesPicker, AttributeDefinition attribute) {
        Map values = getLocalizedEnumerationMap(attribute);
        valuesPicker.setItemLabelGenerator(e -> values.get(e).toString());
        valuesPicker.setItems(DataProviderUtils.createCallbackDataProvider(values.keySet().stream().toList()));
    }

    private Component createNonLookupCollectionField(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixMultiValuePicker<?> multiValuePicker = uiComponents.create(JmixMultiValuePicker.class);

        setValidators(multiValuePicker, attribute);
        setValueSource(multiValuePicker, context);

        MultiValueSelectAction<?> selectAction = actions.create(MultiValueSelectAction.ID);
        initValuesSelectActionByAttribute(selectAction, attribute);
        multiValuePicker.addAction(selectAction);

        ValueClearAction<?> valueClearAction = actions.create(ValueClearAction.ID);
        multiValuePicker.addAction(valueClearAction);

        return multiValuePicker;
    }

    private void setValueProvider(JmixMultiSelectComboBoxPicker<?> valuesPicker,
                                  AttributeDefinition attribute,
                                  ComponentGenerationContext context) {
        ContainerValueSource<?, ?> valueSource = (ContainerValueSource<?, ?>) context.getValueSource();
        if (valueSource == null) {
            return;
        }

        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        List<?> options = optionsLoader.loadOptions(entity, attribute);
        //noinspection unchecked
        valuesPicker.setItems(DataProviderUtils.createCallbackDataProvider(options));
    }

    protected Component createStringField(ComponentGenerationContext context, AttributeDefinition attribute) {
        AbstractField<?, ?> textField;

        Integer rowsCount = attribute.getConfiguration().getRowsCount();
        if (rowsCount != null && rowsCount > 1) {
            TextArea textArea = uiComponents.create(JmixTextArea.class);
            textArea.setMinLength(rowsCount);
            textField = textArea;
        } else {
            textField = uiComponents.create(TypedTextField.class);
        }

        setValidators((SupportsValidation<?>) textField, attribute);
        setValueSource((SupportsValueSource<?>) textField, context);
        return textField;
    }

    protected Component createEnumerationField(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixComboBox<?> comboBox = uiComponents.create(JmixComboBox.class);
        //noinspection unchecked
        ComponentUtils.setItemsMap(comboBox, getLocalizedEnumerationMap(attribute));

        setValueSource(comboBox, context);
        setValidators(comboBox, attribute);

        return comboBox;
    }

    protected Component createComboBox(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixComboBox<?> comboBox = uiComponents.create(JmixComboBox.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setComboBoxOptionsLoader(comboBox, attribute, (ContainerValueSource<?, ?>) context.getValueSource());
        }

        setValueSource(comboBox, context);
        setValidators(comboBox, attribute);

        return comboBox;
    }

    protected AbstractField<?, ?> createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        JmixCheckbox component = uiComponents.create(JmixCheckbox.class);

        // todo setValidators(component, attribute); JmixCheckbox - has no validators
        setValueSource(component, context);

        return component;
    }

    protected TypedDateTimePicker<?> createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TypedDateTimePicker<?> dateField = uiComponents.create(TypedDateTimePicker.class);

        setValidators(dateField, attribute);
        setValueSource(dateField, context);

        return dateField;
    }

    private Component createDateWithoutTimeField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TypedDatePicker<?> dateField = uiComponents.create(TypedDatePicker.class);

        setValidators(dateField, attribute);
        setValueSource(dateField, context);

        return dateField;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AbstractField<?, ?> createNumberField(AttributeType type, ComponentGenerationContext context, AttributeDefinition attribute) {
        TypedTextField<?> component = uiComponents.create(TypedTextField.class);
        switch (type) {
            case INTEGER -> component.setDatatype((Datatype) datatypeRegistry.get(Integer.class));
            case DOUBLE -> component.setDatatype((Datatype) datatypeRegistry.get(Double.class));
            case DECIMAL -> component.setDatatype((Datatype) datatypeRegistry.get(BigDecimal.class));
            default -> throw new IllegalStateException();
        }
        setValidators(component, attribute);
        setCustomDatatype(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected AbstractField<?, ?> createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        if (attribute.getConfiguration().isLookup()) {
            EntityComboBox<?> entityComboBox = uiComponents.create(EntityComboBox.class);

            if (context.getValueSource() instanceof ContainerValueSource) {
                setComboBoxOptionsLoader(entityComboBox, attribute, (ContainerValueSource<?, ?>) context.getValueSource());
            }

            setValueSource(entityComboBox, context);
            setValidators(entityComboBox, attribute);

            return entityComboBox;
        } else {
            EntityPicker<?> entityPicker = uiComponents.create(EntityPicker.class);

            EntityLookupAction<?> lookupAction = actions.create(EntityLookupAction.ID);

            setLookupActionScreen(lookupAction, attribute);

            entityPicker.addAction(lookupAction);
            entityPicker.addAction(actions.create(EntityClearAction.ID));

            setValueSource(entityPicker, context);
            setValidators(entityPicker, attribute);

            return entityPicker;
        }
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(SupportsValueSource<?> field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }

    protected void setValidators(SupportsValidation<?> field, AttributeDefinition attribute) {
        Collection<Validator<?>> validators = attributeValidators.getValidators(attribute);
        //noinspection rawtypes
        for (Validator validator : validators) {
            //noinspection unchecked
            field.addValidator(validator);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setCustomDatatype(TypedTextField field, AttributeDefinition attribute) {
        String formatPattern = attribute.getConfiguration().getNumberFormatPattern();
        if (!Strings.isNullOrEmpty(formatPattern)) {
            Class<?> type = attribute.getDataType() == DECIMAL ? BigDecimal.class : Number.class;
            field.setDatatype(new AdaptiveNumberDatatype(type, formatPattern, "", "", formatStringsRegistry));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setValueChangedListeners(HasValue component, AttributeDefinition attribute) {
        Set<AttributeDefinition> dependentAttributes = attributeDependencies.getDependentAttributes(attribute);
        if (!dependentAttributes.isEmpty() && component instanceof SupportsValueSource) {
            //noinspection unchecked
            if(component instanceof SupportsTypedValue) {
                ((TypedTextField<?>) component).addTypedValueChangeListener(e ->
                        valueChangeListenerHandler((Component) component, attribute, e.getOldValue(), e.getValue()));
            } else {
                component.addValueChangeListener(e ->
                        valueChangeListenerHandler((Component) component, attribute, e.getOldValue(), e.getValue()));
            }
        }
    }

    protected void valueChangeListenerHandler(Component source, AttributeDefinition attributeDefinition,
                                              Object oldValue, Object value) {
        AttributeRecalculationManager manager = applicationContext.getBean(AttributeRecalculationManager.class);

        Assert.notNull(((SupportsValueSource<?>) source).getValueSource(), "Not value source for component");
        applicationContext.getBean(AttributeRecalculationListener.class, manager, attributeDefinition)
                .accept(new ValueSource.ValueChangeEvent(((SupportsValueSource<?>) source).getValueSource(), oldValue, value));

    }

    protected void setEditable(HasValueAndElement<?, ?> component, AttributeDefinition attribute) {
        if (attribute.isReadOnly()) {
            component.setReadOnly(false);
        }
    }

    protected void setLabel(HasLabel component, AttributeDefinition attribute) {
        //noinspection DataFlowIssue
        component.setLabel(
                msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()));
    }

    protected void setDescription(HasTitle component, AttributeDefinition attribute) {
        //noinspection DataFlowIssue
        component.setTitle(
                msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription()));
    }

    protected void setRequired(HasValueAndElement<?, ?> field, AttributeDefinition attribute) {
        field.setRequiredIndicatorVisible(attribute.isRequired());
        if (field instanceof HasValidationProperties) {
            //noinspection DataFlowIssue
            ((HasValidationProperties) field).setErrorMessage(messages.formatMessage("",
                    "validation.required.defaultMsg",
                    msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName())));
        }
    }

    @SuppressWarnings({"rawtypes"})
    protected void initValuesSelectActionByAttribute(MultiValueSelectAction<?> selectAction, AttributeDefinition attribute) {
        AttributeType attributeType = attribute.getDataType();
        switch (attributeType) {
            case DATE -> selectAction.setJavaClass(LocalDateTime.class);

//                selectAction.setResolution(DateField.Resolution.MIN);
            case DATE_WITHOUT_TIME -> selectAction.setJavaClass(LocalDate.class);

//                selectAction.setResolution(DateField.Resolution.DAY);
            case STRING -> selectAction.setJavaClass(String.class);
            case ENUMERATION -> {
                selectAction.setJavaClass(String.class);
                Map values = getLocalizedEnumerationMap(attribute);
                selectAction.setItemLabelGenerator(item -> values.get(item).toString());
                selectAction.setItems(DataProviderUtils.createCallbackDataProvider(new ArrayList<>(values.keySet())));
            }
            case DOUBLE -> selectAction.setJavaClass(Double.class);
            case DECIMAL -> selectAction.setJavaClass(BigDecimal.class);
            case INTEGER -> selectAction.setJavaClass(Integer.class);
            case BOOLEAN -> selectAction.setJavaClass(Boolean.class);
            case ENTITY -> {
                Class<?> javaType = attribute.getJavaType();
                if (javaType != null) {
                    MetaClass metaClass = metadata.getClass(javaType);
                    selectAction.setEntityName(metaClass.getName());
                }
                selectAction.setLookupViewId(attribute.getConfiguration().getLookupScreen());
                selectAction.setUseComboBox(attribute.getConfiguration().isLookup());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected Map getLocalizedEnumerationMap(AttributeDefinition attribute) {
        String enumeration = attribute.getEnumeration();
        Map<String, Object> result = new LinkedHashMap<>();
        if (enumeration != null) {
            for (String value : Splitter.on(",").omitEmptyStrings().split(enumeration)) {
                //noinspection DataFlowIssue
                result.put(value, msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), value));
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
    protected <T extends HasActions & HasValue> void setValuesPickerOptionsLoader(T valuesPicker,
                                                                                  AttributeDefinition attribute,
                                                                                  ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        if (entity != null) {
            List options = optionsLoader.loadOptions(entity, attribute);
            ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                    .setItems(DataProviderUtils.createCallbackDataProvider(options));
        }
        container.addItemChangeListener(e -> {
            List options = optionsLoader.loadOptions(e.getItem(), attribute);

            ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                    .setItems(DataProviderUtils.createCallbackDataProvider(options));
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {

            List<String> codesWithMarkers = dependsOnAttributeCodes.stream()
                    .map(a -> '+' + a)
                    .toList();

            container.addItemPropertyChangeListener(e -> {
                if (codesWithMarkers.contains(e.getProperty())) {
                    List options = optionsLoader.loadOptions(e.getItem(), attribute);
                    ((MultiValueSelectAction) valuesPicker.getAction(MultiValueSelectAction.ID))
                            .setItems(DataProviderUtils.createCallbackDataProvider(options));
                    if (!options.contains(valuesPicker.getValue())) {
                        valuesPicker.clear();
                    }
                }
            });
        }
    }

    @SuppressWarnings("rawtypes")
    protected void setComboBoxOptionsLoader(ComboBox lookupField, AttributeDefinition attribute, ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        if (entity != null) {
            List<?> options = optionsLoader.loadOptions(entity, attribute);
            //noinspection unchecked
            lookupField.setItems(options);
        }
        container.addItemChangeListener(e -> {
            List<?> options = optionsLoader.loadOptions(e.getItem(), attribute);
            //noinspection unchecked
            lookupField.setItems(options);
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {
            List<String> codesWithMarkers = dependsOnAttributeCodes.stream().map(a -> '+' + a).toList();
            container.addItemPropertyChangeListener(e -> {
                if (codesWithMarkers.contains(e.getProperty())) {
                    List<?> options = optionsLoader.loadOptions(e.getItem(), attribute);
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

    protected void setLookupActionScreen(EntityLookupAction<?> lookupAction, AttributeDefinition attribute) {
        String screen = attribute.getConfiguration().getLookupScreen();
        if (!Strings.isNullOrEmpty(screen)) {
            lookupAction.setViewId(screen);
        } else {
            Class<?> javaType = attribute.getJavaType();
            Assert.notNull(javaType, "Java type is null for current attribute");
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
