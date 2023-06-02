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

package io.jmix.reportsflowui.view.run;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("report_ParameterFieldCreator")
public class ParameterFieldCreator {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected ReportsUtils reportsUtils;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected Actions actions;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    protected Map<ParameterType, FieldCreator> fieldCreationMapping = new ImmutableMap.Builder<ParameterType, FieldCreator>()
            .put(ParameterType.BOOLEAN, new CheckBoxCreator())
            .put(ParameterType.DATE, new DateFieldCreator())
            .put(ParameterType.ENTITY, new SingleFieldCreator())
            .put(ParameterType.ENUMERATION, new EnumFieldCreator())
            .put(ParameterType.TEXT, new TextFieldCreator())
            .put(ParameterType.NUMERIC, new NumericFieldCreator())
            .put(ParameterType.ENTITY_LIST, new MultiFieldCreator())
            .put(ParameterType.DATETIME, new DateTimeFieldCreator())
            .put(ParameterType.TIME, new TimeFieldCreator())
            .build();

    public AbstractField createField(ReportInputParameter parameter) {
        AbstractField field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        if (field instanceof HasRequired requiredField) {
            String message = messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", metadataTools.getInstanceName(parameter));
            requiredField.setRequiredMessage(message);
            requiredField.setRequired(parameter.getRequired());
        }

        if (field instanceof HasLabel) {
            ((HasLabel) field).setLabel(metadataTools.getInstanceName(parameter));
        }

        field.setId("param_" + parameter.getAlias());

        return field;
    }

    protected void setCurrentDateAsNow(ReportInputParameter parameter, AbstractField dateField) {
        Date now = reportsUtils.currentDateOrTime(parameter.getType());
        UiComponentUtils.setValue(dateField, now);
        parameter.setDefaultValue(objectToStringConverter.convertToString(now.getClass(), now));
    }

    protected interface FieldCreator {
        AbstractField createField(ReportInputParameter parameter);
    }

    protected class DateFieldCreator implements FieldCreator {
        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            TypedDatePicker dateField = uiComponents.create(TypedDatePicker.class);
            //todo
//            dateField.setResolution(DateField.Resolution.DAY);
//            dateField.setDateFormat(messages.getMessage("dateFormat"));

            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {
        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            DateTimePicker dateField = uiComponents.create(DateTimePicker.class);
            //todo
//            dateField.setResolution(DateField.Resolution.MIN);
//            dateField.setDateFormat(messages.getMessage("dateTimeFormat"));

            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            TypedTimePicker timeField = uiComponents.create(TypedTimePicker.class);
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, timeField);
            }
            return uiComponents.create(TimePicker.class);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            JmixCheckbox checkBox = uiComponents.create(JmixCheckbox.class);
            //todo
            //checkBox.set(Component.Alignment.MIDDLE_LEFT);
            return checkBox;
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            return uiComponents.create(TypedTextField.class);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            TypedTextField<Double> textField = uiComponents.create(TypedTextField.class);
            textField.setDatatype(datatypeRegistry.get(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            JmixSelect select = uiComponents.create(JmixSelect.class);
            select.setEmptySelectionAllowed(true);

            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotBlank(enumClassName)) {
                Class enumClass = classManager.findClass(enumClassName);

                if (enumClass != null) {
                    Object[] constants = enumClass.getEnumConstants();
                    List<Object> optionsList = new ArrayList<>();
                    Collections.addAll(optionsList, constants);

                    select.setItems(optionsList);
                }
            }
            return select;
        }
    }

    protected class SingleFieldCreator implements FieldCreator {
        @Override
        public AbstractField createField(ReportInputParameter parameter) {
            boolean isLookup = Boolean.TRUE.equals(parameter.getLookup());
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());

            return isLookup
                    ? createEntityComboBox(entityMetaClass, parameter)
                    : createEntityPicker(entityMetaClass, parameter);
        }

        protected EntityPicker createEntityPicker(MetaClass entityMetaClass, ReportInputParameter parameter) {
            EntityPicker entityPicker = uiComponents.create(EntityPicker.class);
            entityPicker.setMetaClass(entityMetaClass);

            EntityLookupAction pickerLookupAction = createLookupAction(parameter);
            entityPicker.addAction(pickerLookupAction);

            Action entityClearAction = createClearAction();
            entityPicker.addAction(entityClearAction);
            return entityPicker;
        }

        protected EntityComboBox createEntityComboBox(MetaClass entityMetaClass, ReportInputParameter parameter) {
            EntityComboBox entityComboBox = uiComponents.create(EntityComboBox.class);
            entityComboBox.setMetaClass(entityMetaClass);

            CollectionContainer collectionContainer = getCollectionContainer(parameter, entityMetaClass);
            entityComboBox.setItems(collectionContainer);

            EntityLookupAction pickerLookupAction = createLookupAction(parameter);
            entityComboBox.addAction(pickerLookupAction);

            Action entityClearAction = createClearAction();
            entityComboBox.addAction(entityClearAction);
            return entityComboBox;
        }

        private EntityLookupAction createLookupAction(ReportInputParameter parameter) {
            EntityLookupAction pickerLookupAction = (EntityLookupAction) actions.create(EntityLookupAction.ID);

            String parameterScreen = parameter.getScreen();
            if (StringUtils.isNotEmpty(parameterScreen)) {
                pickerLookupAction.setViewId(parameterScreen);
            }
            return pickerLookupAction;
        }

        private Action createClearAction() {
            return actions.create(EntityClearAction.ID);
        }

        protected CollectionContainer getCollectionContainer(ReportInputParameter parameter, MetaClass entityMetaClass) {
            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.INSTANCE_NAME)
                    .build();

            CollectionContainer collectionContainer = dataComponents.createCollectionContainer(entityMetaClass.getJavaClass());
            collectionContainer.setFetchPlan(fetchPlan);

            CollectionLoader loader = dataComponents.createCollectionLoader();

            String query = createQueryString(entityMetaClass, parameter);
            loader.setQuery(query);
            loader.setContainer(collectionContainer);
            loader.load();

            return collectionContainer;
        }

        protected String createQueryString(MetaClass entityMetaClass, ReportInputParameter parameter) {
            String whereClause = parameter.getLookupWhere();
            String joinClause = parameter.getLookupJoin();

            String query = String.format("select e from %s e", entityMetaClass.getName());

            if (!Strings.isNullOrEmpty(whereClause)) {
                QueryTransformer queryTransformer = queryTransformerFactory.transformer(query);
                queryTransformer.addWhere(whereClause);
                if (!Strings.isNullOrEmpty(joinClause)) {
                    queryTransformer.addJoin(joinClause);
                }
                query = queryTransformer.getResult();
            }
            return query;
        }
    }

    protected class MultiFieldCreator implements FieldCreator {

        @Override
        public AbstractField createField(final ReportInputParameter parameter) {
            JmixMultiSelectComboBoxPicker multiComboBoxPicker = uiComponents.create(JmixMultiSelectComboBoxPicker.class);
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());
            multiComboBoxPicker.setMetaClass(entityMetaClass);

            CollectionContainer collectionContainer = createCollectionContainer(entityMetaClass);
            multiComboBoxPicker.setItems(collectionContainer.getItems());

            EntityLookupAction pickerLookupAction = (EntityLookupAction) actions.create(EntityLookupAction.ID);
            String screen = parameter.getScreen();
            if (StringUtils.isNotEmpty(screen)) {
                pickerLookupAction.setViewId(screen);
            }
            multiComboBoxPicker.addAction(pickerLookupAction);

            ValueClearAction valueClearAction = createValueClearAction();
            multiComboBoxPicker.addAction(valueClearAction);

            return multiComboBoxPicker;
        }

        protected ValueClearAction createValueClearAction() {
            ValueClearAction valueClearAction = (ValueClearAction) actions.create(ValueClearAction.ID);
            return valueClearAction;
        }

        protected CollectionContainer createCollectionContainer(MetaClass entityMetaClass) {
            CollectionContainer collectionContainer = dataComponents.createCollectionContainer(entityMetaClass.getJavaClass());
            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.LOCAL)
                    .build();

            collectionContainer.setFetchPlan(fetchPlan);
            CollectionLoader loader = dataComponents.createCollectionLoader();
            loader.setContainer(collectionContainer);

            String query = String.format("select e from %s e", entityMetaClass.getName());
            loader.setQuery(query);
            loader.load();

            return collectionContainer;
        }
    }
}