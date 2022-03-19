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

package io.jmix.reportsui.screen.report.wizard.query;

import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.wizard.QueryParameter;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@UiController("report_QueryParameter.edit")
@UiDescriptor("query-parameter-edit.xml")
@EditedEntityContainer("queryParameterDc")
public class QueryParameterEditor extends StandardEditor<QueryParameter> {
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected HBoxLayout defaultValueBox;
    @Autowired
    protected Label entityMetaClassLabel;
    @Autowired
    protected ComboBox<MetaClass> entityMetaClassField;
    @Autowired
    protected Label enumerationLabel;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ComboBox<Class> enumerationField;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ObjectToStringConverter objectToStringConverter;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected Label defaultValueLabel;
    @Autowired
    protected Actions actions;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (getEditedEntity().getParameterType() == null) {
            getEditedEntity().setParameterType(ParameterType.TEXT);
        }
        initEnumsComboBox();
        initEntityComboBox();
        enableControlsByParamType();
        initDefaultValueField();
    }

    @Subscribe(id = "queryParameterDc", target = Target.DATA_CONTAINER)
    public void onQueryParameterDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<QueryParameter> event) {
        String property = event.getProperty();
        boolean typeChanged = property.equalsIgnoreCase("parameterType");

        if (typeChanged) {
            updateJavaClass();
            getEditedEntity().setDefaultValueString(null);
            initDefaultValueField();
        }
    }

    @Subscribe("parameterTypeField")
    public void onParameterTypeFieldValueChange(HasValue.ValueChangeEvent<ParameterType> event) {
        enableControlsByParamType();
    }

    protected void updateJavaClass() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        if (parameterType == ParameterType.ENTITY || parameterType == ParameterType.ENTITY_LIST) {
            MetaClass value = entityMetaClassField.getValue();
            String javaClassName = value != null ? value.getJavaClass().getName() : null;
            String entityClassName = value != null ? value.getName() : null;

            getEditedEntity().setJavaClassName(javaClassName);
            getEditedEntity().setEntityMetaClassName(entityClassName);
        } else {
            Class aClass = resolveClass();
            getEditedEntity().setJavaClassName(aClass != null ? aClass.getName() : null);
        }
    }

    @Subscribe("entityMetaClassField")
    public void onMetaClassFieldValueChange(HasValue.ValueChangeEvent<MetaClass> event) {
        updateJavaClass();
        initDefaultValueField();
    }

    @Subscribe("enumerationField")
    public void onEnumerationFieldValueChange(HasValue.ValueChangeEvent<Class> event) {
        updateJavaClass();
        initDefaultValueField();
    }

    protected void initEnumsComboBox() {
        Map<String, Class> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadataTools.getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumLocalizedName + " (" + enumClass.getSimpleName() + ")", enumClass);
        }
        enumerationField.setOptionsMap(enumsOptionsMap);
    }

    protected void initEntityComboBox() {
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        Map<String, MetaClass> metaClassesOptionsMap = classes.stream()
                .filter(metaClass -> !metadataTools.isSystemLevel(metaClass))
                .collect(Collectors.toMap(metaClass ->
                                messageTools.getDetailedEntityCaption(metaClass),
                        metaClass -> metaClass));
        entityMetaClassField.setOptionsMap(metaClassesOptionsMap);
        if (StringUtils.isNotEmpty(getEditedEntity().getEntityMetaClassName())) {
            entityMetaClassField.setValue(metadata.findClass(getEditedEntity().getEntityMetaClassName()));
        }
    }

    protected void initDefaultValueField() {
        defaultValueLabel.setVisible(false);
        defaultValueBox.removeAll();
        Field<Object> defaultValueField = createDefaultValueField();
        if (defaultValueField != null) {
            if (StringUtils.isNotEmpty(getEditedEntity().getJavaClassName())) {
                try {
                    Object value = objectToStringConverter.convertFromString(Class.forName(getEditedEntity().getJavaClassName()), getEditedEntity().getDefaultValueString());
                    defaultValueField.setValue(value);
                } catch (ClassNotFoundException e) {

                }
            }
            defaultValueField.setRequired(false);
            defaultValueField.setWidth("100%");
            defaultValueField.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    getEditedEntity().setDefaultValueString(objectToStringConverter.convertToString(e.getValue().getClass(), e.getValue()));
                } else {
                    getEditedEntity().setDefaultValueString(null);
                }
            });
            defaultValueBox.add(defaultValueField);
            defaultValueLabel.setVisible(true);
        }
    }

    @Nullable
    protected Field createDefaultValueField() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        if (parameterType != null) {
            switch (parameterType) {
                case BOOLEAN:
                    return uiComponents.create(CheckBox.class);
                case TEXT:
                    return createTextField(null);
                case NUMERIC:
                    return createTextField(datatypeRegistry.get(Double.class));
                case DATE:
                    return createDateField(DateField.Resolution.DAY);
                case DATETIME:
                    return createDateField(DateField.Resolution.MIN);
                case TIME:
                    return uiComponents.create(TimeField.class);
                case ENUMERATION:
                    return createEnumComboBox();
                case ENTITY:
                    return createEntityPicker();
            }
        }
        return null;
    }

    protected ComboBox createEnumComboBox() {
        ComboBox comboBox = uiComponents.create(ComboBox.class);
        Class enumClass = enumerationField.getValue();
        if (enumClass != null) {
            comboBox.setOptionsEnum(enumClass);
            if (enumClass.getEnumConstants().length < 10) {
                comboBox.setTextInputAllowed(false);
            }
        }
        return comboBox;
    }

    protected EntityPicker createEntityPicker() {
        MetaClass metaClass = entityMetaClassField.getValue();
        EntityPicker entityPicker = uiComponents.create(EntityPicker.class);
        entityPicker.setMetaClass(metaClass);

        EntityLookupAction pickerLookupAction = (EntityLookupAction) actions.create(EntityLookupAction.ID);
        entityPicker.addAction(pickerLookupAction);

        Action entityClearAction = actions.create(EntityClearAction.ID);
        entityPicker.addAction(entityClearAction);

        return entityPicker;
    }

    protected DateField createDateField(DateField.Resolution resolution) {
        DateField dateField = uiComponents.create(DateField.class);
        dateField.setResolution(resolution);
        return dateField;
    }

    protected TextField createTextField(@Nullable Datatype datatype) {
        TextField textField = uiComponents.create(TextField.class);
        textField.setDatatype(datatype);
        return textField;
    }

    protected void enableControlsByParamType() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        boolean isEntity = parameterType == ParameterType.ENTITY || parameterType == ParameterType.ENTITY_LIST;
        entityMetaClassLabel.setVisible(isEntity);
        entityMetaClassField.setVisible(isEntity);

        boolean isEnum = parameterType == ParameterType.ENUMERATION;
        enumerationLabel.setVisible(isEnum);
        enumerationField.setVisible(isEnum);
    }

    @Nullable
    protected Class resolveClass() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        switch (parameterType) {
            case DATE:
            case DATETIME:
            case TIME:
                return Date.class;
            case TEXT:
                return String.class;
            case BOOLEAN:
                return Boolean.class;
            case NUMERIC:
                return Double.class;
            case ENUMERATION:
                return enumerationField.getValue();
        }
        return null;
    }
}