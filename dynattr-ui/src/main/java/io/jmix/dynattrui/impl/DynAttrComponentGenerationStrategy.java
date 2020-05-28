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

package io.jmix.dynattrui.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.BeanLocator;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.FileDescriptor;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattrui.MsgBundleTools;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.picker.ClearAction;
import io.jmix.ui.action.picker.LookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.options.ListOptions;
import io.jmix.ui.component.data.options.MapOptions;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.dynattr.AttributeType.*;

@org.springframework.stereotype.Component("dynattr_DynamicAttributeComponentGenerationStrategy")
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
    protected BeanLocator beanLocator;

    @Autowired
    public DynAttrComponentGenerationStrategy(Messages messages, UiComponents uiComponents,
                                              DynAttrMetadata dynamicModelMetadata,
                                              MsgBundleTools msgBundleTools,
                                              AttributeOptionsLoader optionsLoader,
                                              AttributeValidators attributeValidators,
                                              WindowConfig windowConfig,
                                              ScreensHelper screensHelper,
                                              Actions actions,
                                              AttributeDependencies attributeDependencies,
                                              BeanLocator beanLocator) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.dynamicModelMetadata = dynamicModelMetadata;
        this.msgBundleTools = msgBundleTools;
        this.optionsLoader = optionsLoader;
        this.attributeValidators = attributeValidators;
        this.windowConfig = windowConfig;
        this.screensHelper = screensHelper;
        this.actions = actions;
        this.attributeDependencies = attributeDependencies;
        this.beanLocator = beanLocator;
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

        Component resultComponent = null;
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
        Class<?> javaType = attribute.getJavaType();

        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createFileUploadField(context);
        } else {
            return createEntityField(context, attribute);
        }
    }

    protected Component createDatatypeField(ComponentGenerationContext context, AttributeDefinition attribute) {
        AttributeType type = attribute.getDataType();
        AttributeDefinition.Configuration configuration = attribute.getConfiguration();

        if (configuration.isLookup()) {
            return createLookupField(context, attribute);
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
        ListEditor listEditor = uiComponents.create(ListEditor.NAME);

        listEditor.setEntityJoinClause(attribute.getConfiguration().getJoinClause());
        listEditor.setEntityWhereClause(attribute.getConfiguration().getWhereClause());

        setValidators(listEditor, attribute);

        AttributeType type = attribute.getDataType();

        ListEditor.ItemType itemType = getListEditorItemType(attribute.getDataType());
        listEditor.setItemType(itemType);

        if (type == ENTITY) {
            Class<?> javaType = attribute.getJavaType();
            if (javaType != null) {
                MetaClass metaClass = metadata.getClass(javaType);
                listEditor.setEntityName(metaClass.getName());
                listEditor.setUseLookupField(attribute.getConfiguration().isLookup());
            }
        }

        if (type == ENUMERATION) {
            //noinspection unchecked
            listEditor.setOptionsMap(getLocalizedEnumerationMap(attribute));
        }

        setValueSource(listEditor, context);

        return listEditor;
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
        LookupField lookupField = uiComponents.create(LookupField.class);

        lookupField.setOptions(new MapOptions(getLocalizedEnumerationMap(attribute)));

        setValueSource(lookupField, context);
        setValidators(lookupField, attribute);

        return lookupField;
    }

    protected Component createLookupField(ComponentGenerationContext context, AttributeDefinition attribute) {
        LookupField lookupField = uiComponents.create(LookupField.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setOptionsLoader(lookupField, attribute, (ContainerValueSource) context.getValueSource());
        }

        setValueSource(lookupField, context);
        setValidators(lookupField, attribute);

        return lookupField;
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


    protected Field createFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = uiComponents.create(FileUploadField.NAME);
        fileUploadField.setMode(FileUploadField.FileStoragePutMode.IMMEDIATE);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    @SuppressWarnings("unchecked")
    protected Component createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        if (attribute.getConfiguration().isLookup()) {
            LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);

            if (context.getValueSource() instanceof ContainerValueSource) {
                setOptionsLoader(lookupPickerField, attribute, (ContainerValueSource) context.getValueSource());
            }

            setValueSource(lookupPickerField, context);
            setValidators(lookupPickerField, attribute);

            return lookupPickerField;
        } else {
            PickerField pickerField = uiComponents.create(PickerField.class);

            LookupAction lookupAction = actions.create(LookupAction.class);

            setLookupActionScreen(lookupAction, attribute);

            pickerField.addAction(lookupAction);
            pickerField.addAction(actions.create(ClearAction.class));

            setValueSource(pickerField, context);
            setValidators(pickerField, attribute);

            return pickerField;
        }
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(Field field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }

    protected void setValidators(Field field, AttributeDefinition attribute) {
        Collection<Consumer<?>> validators = attributeValidators.getValidators(attribute);
        for (Consumer<?> validator : validators) {
            if (field instanceof ListEditor) {
                //noinspection unchecked
                ((ListEditor) field).addListItemValidator(validator);
            } else {
                //noinspection unchecked
                field.addValidator(validator);
            }
        }
    }

    protected void setCustomDatatype(TextField field, AttributeDefinition attribute) {
        String formatPattern = attribute.getConfiguration().getNumberFormatPattern();
        if (!Strings.isNullOrEmpty(formatPattern)) {
            Class<?> type = attribute.getDataType() == DECIMAL ? BigDecimal.class : Number.class;
            //noinspection unchecked
            field.setDatatype(new AdaptiveNumberDatatype(type, formatPattern, "", ""));
        }
    }

    protected void setValueChangedListeners(HasValue component, AttributeDefinition attribute) {
        Set<AttributeDefinition> dependentAttributes = attributeDependencies.getDependentAttributes(attribute);
        if (!dependentAttributes.isEmpty()) {
            //noinspection unchecked
            component.addValueChangeListener(beanLocator.getPrototype(AttributeRecalculationListener.class, attribute));
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
        field.setRequiredMessage(messages.formatMessage(
                "validation.required.defaultMsg",
                msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName())));
    }

    protected ListEditor.ItemType getListEditorItemType(AttributeType attributeType) {
        switch (attributeType) {
            case ENTITY:
                return ListEditor.ItemType.ENTITY;
            case DATE:
                return ListEditor.ItemType.DATETIME;
            case DATE_WITHOUT_TIME:
                return ListEditor.ItemType.DATE;
            case DOUBLE:
                return ListEditor.ItemType.DOUBLE;
            case DECIMAL:
                return ListEditor.ItemType.BIGDECIMAL;
            case INTEGER:
                return ListEditor.ItemType.INTEGER;
            case STRING:
            case ENUMERATION:
                return ListEditor.ItemType.STRING;
            default:
                throw new IllegalStateException(String.format("PropertyType %s not supported", attributeType));
        }
    }

    protected Map<String, ?> getLocalizedEnumerationMap(AttributeDefinition attribute) {
        String enumeration = attribute.getEnumeration();
        Map<String, Object> result = new LinkedHashMap<>();
        for (String value : Splitter.on(",").omitEmptyStrings().split(enumeration)) {
            result.put(msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), value), value);
        }
        return result;
    }


    protected void setOptionsLoader(LookupField lookupField, AttributeDefinition attribute, ContainerValueSource valueSource) {
        InstanceContainer<?> container = valueSource.getContainer();
        Entity entity = container.getItemOrNull();
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

        List<CategoryAttribute> dependsOnAttributes = attribute.getConfiguration().getDependsOnAttributes();
        if (dependsOnAttributes != null && !dependsOnAttributes.isEmpty()) {
            List<String> dependsOnAttributesCodes = dependsOnAttributes.stream()
                    .map(a -> DynAttrUtils.getPropertyFromAttributeCode(a.getCode()))
                    .collect(Collectors.toList());

            container.addItemPropertyChangeListener(e -> {
                if (dependsOnAttributesCodes.contains(e.getProperty())) {
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

    protected void setLookupActionScreen(LookupAction lookupAction, AttributeDefinition attribute) {
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
        return HIGHEST_PLATFORM_PRECEDENCE + 20;
    }
}
