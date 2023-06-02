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

package io.jmix.reportsflowui.view.queryparameter;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.wizard.QueryParameter;
import io.jmix.reports.libintegration.JmixObjectToStringConverter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Route(value = "queryParameter/:id", layout = DefaultMainViewParent.class)
@ViewController("report_QueryParameter.detail")
@ViewDescriptor("query-parameter-detail-view.xml")
@EditedEntityContainer("queryParameterDc")
public class QueryParameterDetailView extends StandardDetailView<QueryParameter> {

    @ViewComponent
    private JmixComboBox<MetaClass> entityMetaClassField;
    @ViewComponent
    protected ComboBox<Class> enumerationField;
    @ViewComponent
    protected HorizontalLayout defaultValueBox;
    @Autowired
    private JmixObjectToStringConverter jmixObjectToStringConverter;
    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @Autowired
    private DatatypeRegistry datatypeRegistry;
    @Autowired
    private Actions actions;


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
    public void onParameterTypeFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixComboBox<ParameterType>, ParameterType> event) {
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
    public void onMetaClassFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixComboBox<MetaClass>, MetaClass> event) {
        updateJavaClass();
        initDefaultValueField();
    }

    @Subscribe("enumerationField")
    public void onEnumerationFieldValueChange(HasValue.ValueChangeEvent<Class> event) {
        updateJavaClass();
        initDefaultValueField();
    }

    //todo replace with parameterFieldCreator
    protected void initEnumsComboBox() {
        Map<String, Class> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadataTools.getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumLocalizedName + " (" + enumClass.getSimpleName() + ")", enumClass);
        }
        FlowuiComponentUtils.setItemsMap(enumerationField, MapUtils.invertMap(enumsOptionsMap));
    }

    protected void initEntityComboBox() {
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        Map<String, MetaClass> metaClassesOptionsMap = classes.stream()
                .filter(metaClass -> !metadataTools.isSystemLevel(metaClass))
                .collect(Collectors.toMap(metaClass ->
                                messageTools.getDetailedEntityCaption(metaClass),
                        metaClass -> metaClass));
        FlowuiComponentUtils.setItemsMap(entityMetaClassField, MapUtils.invertMap(metaClassesOptionsMap));
        if (StringUtils.isNotEmpty(getEditedEntity().getEntityMetaClassName())) {
            entityMetaClassField.setValue(metadata.findClass(getEditedEntity().getEntityMetaClassName()));
        }
    }

    protected void initDefaultValueField() {
        defaultValueBox.removeAll();
        AbstractField defaultValueField = createDefaultValueField();
        if (defaultValueField != null) {
            if (StringUtils.isNotEmpty(getEditedEntity().getJavaClassName()) && StringUtils.isNotEmpty(getEditedEntity().getDefaultValueString())) {
                try {
                    Object value = jmixObjectToStringConverter.convertFromString(Class.forName(getEditedEntity().getJavaClassName()), getEditedEntity().getDefaultValueString());
                    defaultValueField.setValue(value);
                } catch (ClassNotFoundException e) {

                }
            }
            defaultValueField.getElement().setProperty("required", false);
            defaultValueField.getElement().setProperty("width", "100%");
            defaultValueField.getElement().setProperty("label", messages.getMessage(getClass(), "parameters.defaultValue"));
            defaultValueField.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    getEditedEntity().setDefaultValueString(jmixObjectToStringConverter.convertToString(e.getValue().getClass(), e.getValue()));
                } else {
                    getEditedEntity().setDefaultValueString(null);
                }
            });
            defaultValueBox.add(defaultValueField);
        }
    }

    @Nullable
    protected AbstractField createDefaultValueField() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        if (parameterType != null) {
            switch (parameterType) {
                case BOOLEAN:
                    return uiComponents.create(JmixCheckbox.class);
                case TEXT:
                    return uiComponents.create(TypedTextField.class);
                case NUMERIC:
                    return createNumericTextField();
                case DATE:
                    return uiComponents.create(DatePicker.class);
                case DATETIME:
                    return uiComponents.create(DateTimePicker.class);
                case TIME:
                    return uiComponents.create(TimePicker.class);
                case ENUMERATION:
                    return createEnumComboBox();
                case ENTITY:
                    return createEntityPicker();
            }
        }
        return null;
    }

    protected JmixComboBox createEnumComboBox() {
        JmixComboBox comboBox = uiComponents.create(JmixComboBox.class);
        Class enumClass = enumerationField.getValue();
        if (enumClass != null) {
            comboBox.setItems(enumClass);
            if (enumClass.getEnumConstants().length < 10) {
                comboBox.setAllowCustomValue(false);
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


    protected TypedTextField createNumericTextField() {
        TypedTextField<Double> textField = uiComponents.create(TypedTextField.class);
        textField.setDatatype(datatypeRegistry.get(Double.class));
        return textField;
    }

    protected void enableControlsByParamType() {
        ParameterType parameterType = getEditedEntity().getParameterType();
        boolean isEntity = parameterType == ParameterType.ENTITY || parameterType == ParameterType.ENTITY_LIST;
        entityMetaClassField.setVisible(isEntity);

        boolean isEnum = parameterType == ParameterType.ENUMERATION;
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
