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

package io.jmix.dynattrui;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.FileDescriptor;
import io.jmix.core.metamodel.datatypes.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattrui.impl.AttributeOptionsLoader;
import io.jmix.dynattrui.impl.AttributeValidators;
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.*;
import io.jmix.ui.components.data.options.ListOptions;
import io.jmix.ui.components.data.value.ContainerValueSource;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.core.Ordered;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Inject
    public DynAttrComponentGenerationStrategy(Messages messages, UiComponents uiComponents,
                                              DynAttrMetadata dynamicModelMetadata,
                                              MsgBundleTools msgBundleTools,
                                              AttributeOptionsLoader optionsLoader,
                                              AttributeValidators attributeValidators) {
        this.messages = messages;
        this.uiComponents = uiComponents;
        this.dynamicModelMetadata = dynamicModelMetadata;
        this.msgBundleTools = msgBundleTools;
        this.optionsLoader = optionsLoader;
        this.attributeValidators = attributeValidators;
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
        AttributeDefinition attributeDefinition = dynamicModelMetadata.getAttributeByCode(metaClass,
                DynAttrUtils.getAttributeCodeFromProperty(propertyName)).orElse(null);

        if (attributeDefinition == null) {
            return null;
        }

        Component resultComponent = null;
        if (attributeDefinition.isCollection()) {
            resultComponent = createCollectionField(context, attributeDefinition);
        } else if (attributeDefinition.getDataType() == ENTITY) {
            resultComponent = createClassField(context, attributeDefinition);
        } else {
            resultComponent = createDatatypeField(context, attributeDefinition);
        }

        if (resultComponent instanceof HasValue) {
            setValueChangedListeners((HasValue<?>) resultComponent, context);
        }

        if (resultComponent instanceof Component.Editable) {
            setEditable((Component.Editable) resultComponent, attributeDefinition);
        }

        if (resultComponent instanceof Component.HasCaption) {
            setCaption((Component.HasCaption) resultComponent, attributeDefinition);
        }

        if (resultComponent instanceof Field) {
            setRequired((Field<?>) resultComponent, attributeDefinition);
        }

        return resultComponent;
    }

    protected Component createClassField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        Class<?> javaType = attributeDefinition.getJavaType();

        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createFileUploadField(context);
        } else {
            return createEntityField(context, attributeDefinition);
        }
    }

    protected Component createDatatypeField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        AttributeType type = attributeDefinition.getDataType();
        AttributeDefinition.Configuration configuration = attributeDefinition.getConfiguration();

        if (configuration.isLookup()) {
            return createLookupField(context, attributeDefinition);
        } else if (type == STRING) {
            return createStringField(context, attributeDefinition);
        } else if (type == BOOLEAN) {
            return createBooleanField(context, attributeDefinition);
        } else if (type == DATE || type == DATE_WITHOUT_TIME) {
            return createDateField(context, attributeDefinition);
        } else if (type == INTEGER || type == DOUBLE || type == DECIMAL) {
            return createNumberField(context, attributeDefinition);
        } else if (type == ENUMERATION) {
            return createStringField(context, attributeDefinition);
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

    protected Component createStringField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        TextInputField textField;

        Integer rowsCount = attributeDefinition.getConfiguration().getRowsCount();
        if (rowsCount != null && rowsCount > 1) {
            TextArea textArea = uiComponents.create(TextArea.class);
            textArea.setRows(rowsCount);
            textField = textArea;
        } else {
            textField = uiComponents.create(TextField.class);
        }

        setValidators(textField, attributeDefinition);
        setValueSource(textField, context);

        return textField;
    }

    protected Component createLookupField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        LookupField lookupField = uiComponents.create(LookupField.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setOptionsLoader(lookupField, attributeDefinition, (ContainerValueSource) context.getValueSource());
        }

        setValueSource(lookupField, context);
        setValidators(lookupField, attributeDefinition);

        return lookupField;
    }

    protected Field createBooleanField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        CheckBox component = uiComponents.create(CheckBox.class);

        setValidators(component, attributeDefinition);
        setValueSource(component, context);

        return component;
    }

    protected Component createDateField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        DateField dateField = uiComponents.create(DateField.class);

        setValidators(dateField, attributeDefinition);
        setValueSource(dateField, context);

        return dateField;
    }

    protected Field createNumberField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        TextField component = uiComponents.create(TextField.class);

        setValidators(component, attributeDefinition);
        setCustomDatatype(component, attributeDefinition);
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
    protected Component createEntityField(ComponentGenerationContext context, AttributeDefinition attributeDefinition) {
        if (attributeDefinition.getConfiguration().isLookup()) {
            LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);

            if (context.getValueSource() instanceof ContainerValueSource) {
                setOptionsLoader(lookupPickerField, attributeDefinition, (ContainerValueSource) context.getValueSource());
            }

            setValueSource(lookupPickerField, context);
            setValidators(lookupPickerField, attributeDefinition);

            return lookupPickerField;
        } else {
            PickerField pickerField = uiComponents.create(PickerField.class);

            //todo:
            pickerField.addClearAction();
            pickerField.addLookupAction();

            // todo: filter support FilteringLookupAction
            //getDynamicAttributesGuiTools().initEntityPickerField(pickerField, attributeDefinition);

            setValueSource(pickerField, context);
            setValidators(pickerField, attributeDefinition);

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

    protected void setValueChangedListeners(HasValue component, ComponentGenerationContext context) {
        //todo: dynamic attributes (calculated attributes)
//        Consumer<HasValue.ValueChangeEvent> valueChangedListener = getDynamicAttributesGuiTools()
//                .getValueChangeEventListener(attribute);
//
//        if (valueChangedListener != null) {
//            //noinspection unchecked
//            component.addValueChangeListener(valueChangedListener);
//        }
    }

    protected void setEditable(Component.Editable component, AttributeDefinition attributeDefinition) {
        if (Boolean.TRUE.equals(attributeDefinition.isReadOnly())) {
            component.setEditable(false);
        }
    }

    protected void setCaption(Component.HasCaption component, AttributeDefinition attributeDefinition) {
        component.setCaption(
                msgBundleTools.getLocalizedValue(attributeDefinition.getNameMsgBundle(), attributeDefinition.getName()));
        component.setDescription(
                msgBundleTools.getLocalizedValue(attributeDefinition.getDescriptionsMsgBundle(), attributeDefinition.getDescription()));
    }

    protected void setRequired(Field<?> field, AttributeDefinition attributeDefinition) {
        field.setRequired(attributeDefinition.isRequired());
        field.setRequiredMessage(messages.formatMessage(
                "validation.required.defaultMsg",
                msgBundleTools.getLocalizedValue(attributeDefinition.getNameMsgBundle(), attributeDefinition.getName())));
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

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 20;
    }
}
