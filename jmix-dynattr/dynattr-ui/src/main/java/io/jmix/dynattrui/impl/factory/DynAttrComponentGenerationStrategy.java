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

package io.jmix.dynattrui.impl.factory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.*;
import io.jmix.dynattrui.impl.AttributeDependencies;
import io.jmix.dynattrui.impl.AttributeOptionsLoader;
import io.jmix.dynattrui.impl.AttributeRecalculationListener;
import io.jmix.dynattrui.impl.AttributeValidators;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.action.valuespicker.ValuesSelectAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.options.ListOptions;
import io.jmix.ui.component.data.options.MapOptions;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    protected WindowConfig windowConfig;
    protected ScreensHelper screensHelper;
    protected Actions actions;
    protected AttributeDependencies attributeDependencies;
    protected FormatStringsRegistry formatStringsRegistry;
    protected ApplicationContext applicationContext;

    @Autowired
    public DynAttrComponentGenerationStrategy(Messages messages, UiComponents uiComponents,
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
                                              ApplicationContext applicationContext) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.dynamicModelMetadata = dynamicModelMetadata;
        this.metadata = metadata;
        this.msgBundleTools = msgBundleTools;
        this.optionsLoader = optionsLoader;
        this.attributeValidators = attributeValidators;
        this.windowConfig = windowConfig;
        this.screensHelper = screensHelper;
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
            setValueChangedListeners((HasValue<?>) resultComponent, attribute);
        }

        if (resultComponent instanceof Component.Editable) {
            setEditable((Component.Editable) resultComponent, attribute);
        }

        if (resultComponent instanceof Component.HasCaption) {
            setCaption((Component.HasCaption) resultComponent, attribute);
        }

        if (resultComponent instanceof Field) {
            setRequired((Field<?>) resultComponent, attribute);
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
        ValuesPicker valuesPicker = uiComponents.create(ValuesPicker.NAME);

        setValidators(valuesPicker, attribute);
        setValueSource(valuesPicker, context);

        ValuesSelectAction selectAction = actions.create(ValuesSelectAction.class);
        initValuesSelectActionByAttribute(selectAction, attribute);

        if (valuesPicker.getValueSource() instanceof ContainerValueSource && attribute.getConfiguration().isLookup()) {
            ContainerValueSource valueSource = (ContainerValueSource) valuesPicker.getValueSource();
            setValuesPickerOptionsLoader(valuesPicker, attribute, valueSource);
        }

        valuesPicker.addAction(selectAction);

        ValueClearAction valueClearAction = actions.create(ValueClearAction.class);
        valuesPicker.addAction(valueClearAction);

        return valuesPicker;
    }

    protected Component createStringField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TextInputField textField;

        Integer rowsCount = attribute.getConfiguration().getRowsCount();
        if (rowsCount != null && rowsCount > 1) {
            TextArea textArea = uiComponents.create(TextArea.class);
            textArea.setRows(rowsCount);
            textField = textArea;
        } else {
            textField = uiComponents.create(TextField.class);
        }

        setValidators(textField, attribute);
        setValueSource(textField, context);

        return textField;
    }

    protected Component createEnumerationField(ComponentGenerationContext context, AttributeDefinition attribute) {
        ComboBox comboBox = uiComponents.create(ComboBox.class);

        comboBox.setOptionsMap(getLocalizedEnumerationMap(attribute));

        setValueSource(comboBox, context);
        setValidators(comboBox, attribute);

        return comboBox;
    }

    protected Component createComboBox(ComponentGenerationContext context, AttributeDefinition attribute) {
        ComboBox comboBox = uiComponents.create(ComboBox.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setComboBoxOptionsLoader(comboBox, attribute, (ContainerValueSource) context.getValueSource());
        }

        setValueSource(comboBox, context);
        setValidators(comboBox, attribute);

        return comboBox;
    }

    protected Field createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        CheckBox component = uiComponents.create(CheckBox.class);

        setValidators(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected Component createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        DateField dateField = uiComponents.create(DateField.class);

        setValidators(dateField, attribute);
        setValueSource(dateField, context);

        return dateField;
    }

    protected Field createNumberField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TextField component = uiComponents.create(TextField.class);

        setValidators(component, attribute);
        setCustomDatatype(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected EntityPicker createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
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

            EntityLookupAction lookupAction = actions.create(EntityLookupAction.class);

            setLookupActionScreen(lookupAction, attribute);

            entityPicker.addAction(lookupAction);
            entityPicker.addAction(actions.create(EntityClearAction.class));

            setValueSource(entityPicker, context);
            setValidators(entityPicker, attribute);

            return entityPicker;
        }
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(Field field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }

    protected void setValidators(Field field, AttributeDefinition attribute) {
        Collection<Validator<?>> validators = attributeValidators.getValidators(attribute);
        for (Validator<?> validator : validators) {
            field.addValidator(validator);
        }
    }

    protected void setCustomDatatype(TextField field, AttributeDefinition attribute) {
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
            component.addValueChangeListener(applicationContext.getBean(AttributeRecalculationListener.class, attribute));
        }
    }

    protected void setEditable(Component.Editable component, AttributeDefinition attribute) {
        if (Boolean.TRUE.equals(attribute.isReadOnly())) {
            component.setEditable(false);
        }
    }

    protected void setCaption(Component.HasCaption component, AttributeDefinition attribute) {
        component.setCaption(
                msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()));
        component.setDescription(
                msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription()));
    }

    protected void setRequired(Field<?> field, AttributeDefinition attribute) {
        field.setRequired(attribute.isRequired());
        field.setRequiredMessage(messages.formatMessage("",
                "validation.required.defaultMsg",
                msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName())));
    }

    protected void initValuesSelectActionByAttribute(ValuesSelectAction selectAction, AttributeDefinition attribute) {
        AttributeType attributeType = attribute.getDataType();
        switch (attributeType) {
            case DATE:
                selectAction.setJavaClass(Date.class);
                selectAction.setResolution(DateField.Resolution.MIN);
                break;
            case DATE_WITHOUT_TIME:
                selectAction.setJavaClass(LocalDate.class);
                selectAction.setResolution(DateField.Resolution.DAY);
                break;
            case STRING:
                selectAction.setJavaClass(String.class);
                break;
            case ENUMERATION:
                selectAction.setJavaClass(String.class);
                selectAction.setOptions(new MapOptions(getLocalizedEnumerationMap(attribute)));
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
                selectAction.setLookupScreenId(attribute.getConfiguration().getLookupScreen());
                selectAction.setUseComboBox(attribute.getConfiguration().isLookup());
                break;
        }
    }

    protected Map<String, ?> getLocalizedEnumerationMap(AttributeDefinition attribute) {
        String enumeration = attribute.getEnumeration();
        Map<String, Object> result = new LinkedHashMap<>();
        if (enumeration != null) {
            for (String value : Splitter.on(",").omitEmptyStrings().split(enumeration)) {
                result.put(msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), value), value);
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setValuesPickerOptionsLoader(ValuesPicker valuesPicker,
                                                AttributeDefinition attribute,
                                                ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Object entity = container.getItemOrNull();
        if (entity != null) {
            List options = optionsLoader.loadOptions(entity, attribute);
            ((ValuesSelectAction) valuesPicker.getActionNN(ValuesSelectAction.ID))
                    .setOptions(new ListOptions(options));
        }
        container.addItemChangeListener(e -> {
            List options = optionsLoader.loadOptions(e.getItem(), attribute);

            ((ValuesSelectAction) valuesPicker.getActionNN(ValuesSelectAction.ID))
                    .setOptions(new ListOptions(options));
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {

            container.addItemPropertyChangeListener(e -> {
                if (dependsOnAttributeCodes.contains(e.getProperty())) {
                    List options = optionsLoader.loadOptions(e.getItem(), attribute);
                    ((ValuesSelectAction) valuesPicker.getActionNN(ValuesSelectAction.ID))
                            .setOptions(new ListOptions(options));
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
            lookupField.setOptions(new ListOptions(options));
        }
        container.addItemChangeListener(e -> {
            List options = optionsLoader.loadOptions(e.getItem(), attribute);
            //noinspection unchecked
            lookupField.setOptions(new ListOptions(options));
        });

        List<String> dependsOnAttributeCodes = attribute.getConfiguration().getDependsOnAttributeCodes();
        if (dependsOnAttributeCodes != null && !dependsOnAttributeCodes.isEmpty()) {

            container.addItemPropertyChangeListener(e -> {
                if (dependsOnAttributeCodes.contains(e.getProperty())) {
                    List options = optionsLoader.loadOptions(e.getItem(), attribute);
                    //noinspection unchecked
                    lookupField.setOptions(new ListOptions(options));
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
            lookupAction.setScreenId(screen);
        } else {
            Class<?> javaType = attribute.getJavaType();
            MetaClass metaClass = metadata.getClass(javaType);
            screen = windowConfig.getBrowseScreenId(metaClass);
            Map<String, String> screensMap = screensHelper.getAvailableBrowserScreens(javaType);
            if (windowConfig.findWindowInfo(screen) != null && screensMap.containsValue(screen)) {
                lookupAction.setScreenId(screen);
                lookupAction.setOpenMode(OpenMode.THIS_TAB);
            } else {
//                lookupAction.setLookupScreen(CommonLookupController.SCREEN_ID);
//                lookupAction.setLookupScreenParams(ParamsMap.of(CommonLookupController.CLASS_PARAMETER, metaClass));
//                lookupAction.setLookupScreenOpenType(OpenType.DIALOG);
            }
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 20;
    }
}
