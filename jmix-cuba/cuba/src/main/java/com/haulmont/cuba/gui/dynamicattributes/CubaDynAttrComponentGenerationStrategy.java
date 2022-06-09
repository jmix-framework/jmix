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

package com.haulmont.cuba.gui.dynamicattributes;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.actions.picker.ClearAction;
import com.haulmont.cuba.gui.actions.picker.LookupAction;
import com.haulmont.cuba.gui.components.*;
import io.jmix.core.Entity;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrui.impl.AttributeDependencies;
import io.jmix.dynattrui.impl.AttributeOptionsLoader;
import io.jmix.dynattrui.impl.AttributeValidators;
import io.jmix.dynattrui.impl.factory.DynAttrComponentGenerationStrategy;
import io.jmix.ui.Actions;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.sys.ScreensHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.function.Consumer;

@org.springframework.stereotype.Component("cuba_DynamicAttributeComponentGenerationStrategy")
public class CubaDynAttrComponentGenerationStrategy extends DynAttrComponentGenerationStrategy {

    protected Icons icons;
    protected UiComponents cubaUiComponents;

    @Autowired
    public CubaDynAttrComponentGenerationStrategy(Messages messages,
                                                  io.jmix.ui.UiComponents uiComponents,
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
                                                  Icons icons,
                                                  ApplicationContext applicationContext,
                                                  UiComponents cubaUiComponents) {
        super(messages, uiComponents, dynamicModelMetadata, metadata, msgBundleTools, optionsLoader, attributeValidators,
                windowConfig, screensHelper, actions, attributeDependencies, formatStringsRegistry, applicationContext);

        this.icons = icons;
        this.cubaUiComponents = cubaUiComponents;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 15;
    }

    @Override
    protected Component createClassField(ComponentGenerationContext context, AttributeDefinition attribute) {
        Class<?> javaType = attribute.getJavaType();

        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createCubaFileUploadField(context);
        } else {
            return createEntityField(context, attribute);
        }
    }

    /**
     * Creates FileUploadField working with FileDescriptor.
     */
    protected Field createCubaFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = cubaUiComponents.create(FileUploadField.NAME);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon(icons.get(JmixIcon.UPLOAD));

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon(icons.get(JmixIcon.TIMES));

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    @Override
    protected Component createCollectionField(ComponentGenerationContext context, AttributeDefinition attribute) {
        ListEditor listEditor = cubaUiComponents.create(ListEditor.NAME);
        listEditor.setEntityJoinClause(attribute.getConfiguration().getJoinClause());
        listEditor.setEntityWhereClause(attribute.getConfiguration().getWhereClause());

        Collection<Validator<?>> validators = attributeValidators.getValidators(attribute);
        if (validators != null && !validators.isEmpty()) {
            for (Consumer<?> validator : validators) {
                //noinspection unchecked
                listEditor.addListItemValidator(validator);
            }
        }

        ListEditor.ItemType itemType = getListEditorItemType(attribute);
        listEditor.setItemType(itemType);

        setValueSource(listEditor, context);

        Class<?> entityClass = attribute.getJavaType();
        if (entityClass != null && entityClass.isAssignableFrom(Entity.class)) {
            MetaClass metaClass = metadata.getClass(entityClass);
            listEditor.setEntityName(metaClass.getName());
            listEditor.setUseLookupField(BooleanUtils.isTrue(attribute.getConfiguration().isLookup()));
        }

        if (AttributeType.ENUMERATION.equals(attribute.getDataType())) {
            //noinspection unchecked
            listEditor.setOptionsMap(getLocalizedEnumerationMap(attribute));
        }

        return listEditor;
    }

    @Override
    protected Component createStringField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TextInputField textField;

        Integer rowsCount = attribute.getConfiguration().getRowsCount();
        if (rowsCount != null && rowsCount > 1) {
            TextArea textArea = uiComponents.create(TextArea.class);
            textArea.setRows(rowsCount);
            textField = textArea;
        } else {
            textField = cubaUiComponents.create(TextField.class);
        }

        setValidators(textField, attribute);
        setValueSource(textField, context);

        return textField;
    }

    @Override
    protected Component createEnumerationField(ComponentGenerationContext context, AttributeDefinition attribute) {
        LookupField lookupField = cubaUiComponents.create(LookupField.class);

        lookupField.setOptionsMap(getLocalizedEnumerationMap(attribute));

        setValueSource(lookupField, context);
        setValidators(lookupField, attribute);

        return lookupField;
    }

    @Override
    protected Component createComboBox(ComponentGenerationContext context, AttributeDefinition attribute) {
        LookupField lookupField = cubaUiComponents.create(LookupField.class);

        if (context.getValueSource() instanceof ContainerValueSource) {
            setComboBoxOptionsLoader(lookupField, attribute, (ContainerValueSource) context.getValueSource());
        }

        setValueSource(lookupField, context);
        setValidators(lookupField, attribute);

        return lookupField;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        CheckBox component = cubaUiComponents.create(CheckBox.class);

        setValidators(component, attribute);
        setValueSource(component, context);

        return component;
    }

    @Override
    protected Component createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        DateField dateField = cubaUiComponents.create(DateField.class);

        setValidators(dateField, attribute);
        setValueSource(dateField, context);

        return dateField;
    }

    @Override
    protected Field createNumberField(ComponentGenerationContext context, AttributeDefinition attribute) {
        TextField component = cubaUiComponents.create(TextField.class);

        setValidators(component, attribute);
        setCustomDatatype(component, attribute);
        setValueSource(component, context);

        return component;
    }

    protected EntityPicker createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        if (attribute.getConfiguration().isLookup()) {
            LookupPickerField lookupPickerField = cubaUiComponents.create(LookupPickerField.class);

            if (context.getValueSource() instanceof ContainerValueSource) {
                setComboBoxOptionsLoader(lookupPickerField, attribute, (ContainerValueSource) context.getValueSource());
            }

            setValueSource(lookupPickerField, context);
            setValidators(lookupPickerField, attribute);

            return lookupPickerField;
        } else {
            PickerField pickerField = cubaUiComponents.create(PickerField.class);

            LookupAction lookupAction = actions.create(LookupAction.class);

            setLookupActionScreen(lookupAction, attribute);

            pickerField.addAction(lookupAction);
            pickerField.addAction(actions.create(ClearAction.class));

            setValueSource(pickerField, context);
            setValidators(pickerField, attribute);

            return pickerField;
        }
    }

    protected ListEditor.ItemType getListEditorItemType(AttributeDefinition attribute) {
        AttributeType attributeType = attribute.getDataType();
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
                throw new IllegalStateException(String.format("Attribute type %s not supported", attributeType));
        }
    }

    protected Map<String, String> getLocalizedEnumerationMap(AttributeDefinition attribute) {
        String enumeration = attribute.getEnumeration();
        if (enumeration == null) {
            return Collections.emptyMap();
        }

        List<String> enumValues = Lists.newArrayList(Splitter.on(",").omitEmptyStrings().split(enumeration));
        Map<String, String> enumMsgBundleValues = msgBundleTools.getEnumMsgBundleValues(attribute.getEnumerationMsgBundle());

        Map<String, String> localizedEnumerationMap = new LinkedHashMap<>();
        for (String enumValue : enumValues) {
            localizedEnumerationMap.put(enumMsgBundleValues.get(enumValue), enumValue);
        }
        return localizedEnumerationMap;
    }
}
