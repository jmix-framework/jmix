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

package io.jmix.reportsui.screen.report.run;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.tagpicker.TagLookupAction;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@org.springframework.stereotype.Component("report_ParameterFieldCreator")
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
    protected DataComponents factory;

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

    public Label<String> createLabel(ReportInputParameter parameter, Field field) {
        Label<String> label = uiComponents.create(Label.TYPE_STRING);
        label.setAlignment(field instanceof TagPicker ? Component.Alignment.TOP_LEFT : Component.Alignment.MIDDLE_LEFT);
        label.setWidth(Component.AUTO_SIZE);
        label.setValue(metadataTools.getInstanceName(parameter));
        return label;
    }

    public Field createField(ReportInputParameter parameter) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setRequiredMessage(messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", metadataTools.getInstanceName(parameter)));

        field.setId("param_" + parameter.getAlias());
        field.setWidth("100%");
        field.setEditable(true);

        field.setRequired(parameter.getRequired());
        return field;
    }

    protected void setCurrentDateAsNow(ReportInputParameter parameter, Field dateField) {
        Date now = reportsUtils.currentDateOrTime(parameter.getType());
        dateField.setValue(now);
        parameter.setDefaultValue(objectToStringConverter.convertToString(now.getClass(), now));
    }

    protected interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    protected class DateFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = uiComponents.create(DateField.class);
            dateField.setResolution(DateField.Resolution.DAY);
            dateField.setDateFormat(messages.getMessage("dateFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = uiComponents.create(DateField.class);
            dateField.setResolution(DateField.Resolution.MIN);
            dateField.setDateFormat(messages.getMessage("dateTimeFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            Field timeField = uiComponents.create(TimeField.class);
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, timeField);
            }
            return uiComponents.create(TimeField.class);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            CheckBox checkBox = uiComponents.create(CheckBox.class);
            checkBox.setAlignment(Component.Alignment.MIDDLE_LEFT);
            return checkBox;
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return uiComponents.create(TextField.class);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = uiComponents.create(TextField.class);
            textField.setDatatype(datatypeRegistry.get(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            ComboBox lookupField = uiComponents.create(ComboBox.class);
            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotBlank(enumClassName)) {
                Class enumClass = classManager.findClass(enumClassName);

                if (enumClass != null) {
                    Object[] constants = enumClass.getEnumConstants();
                    List<Object> optionsList = new ArrayList<>();
                    Collections.addAll(optionsList, constants);

                    lookupField.setOptionsList(optionsList);
                    if (optionsList.size() < 10) {
                        lookupField.setTextInputAllowed(false);
                    }
                }
            }
            return lookupField;
        }
    }

    protected class SingleFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            boolean isLookup = Boolean.TRUE.equals(parameter.getLookup());
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());

            EntityPicker field = isLookup
                    ? createEntityComboBox(parameter, entityMetaClass)
                    : createEntityPicker();

            field.setMetaClass(entityMetaClass);

            EntityLookupAction pickerLookupAction = (EntityLookupAction) actions.create(EntityLookupAction.ID);
            field.addAction(pickerLookupAction);

            String parameterScreen = parameter.getScreen();

            if (StringUtils.isNotEmpty(parameterScreen)) {
                pickerLookupAction.setScreenId(parameterScreen);
            }

            Action entityClearAction = actions.create(EntityClearAction.ID);
            field.addAction(entityClearAction);

            return field;
        }

        protected EntityPicker createEntityPicker() {
            return uiComponents.create(EntityPicker.class);
        }

        protected EntityPicker createEntityComboBox(ReportInputParameter parameter, MetaClass entityMetaClass) {
            EntityComboBox field = uiComponents.create(EntityComboBox.class);
            CollectionContainer collectionContainer = getCollectionContainer(parameter, entityMetaClass);
            field.setOptionsContainer(collectionContainer);
            return field;
        }

        protected CollectionContainer getCollectionContainer(ReportInputParameter parameter, MetaClass entityMetaClass) {
            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.INSTANCE_NAME)
                    .build();

            CollectionContainer collectionContainer = factory.createCollectionContainer(entityMetaClass.getJavaClass());
            collectionContainer.setFetchPlan(fetchPlan);

            CollectionLoader loader = factory.createCollectionLoader();
            loader.setContainer(collectionContainer);

            String query = createQueryString(entityMetaClass, parameter);
            loader.setQuery(query);
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
        public Field createField(final ReportInputParameter parameter) {
            TagPicker tagPicker = uiComponents.create(TagPicker.class);
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());

            CollectionContainer collectionContainer = createCollectionContainer(entityMetaClass);

            ContainerOptions options = new ContainerOptions(collectionContainer);

            tagPicker.setOptions(options);
            tagPicker.setEditable(true);
            tagPicker.setHeight("120px");
            tagPicker.setMetaClass(entityMetaClass);

            TagLookupAction tagLookupAction = createTagLookupAction(parameter, tagPicker);
            tagPicker.addAction(tagLookupAction);

            ValueClearAction valueClearAction = createValueClearAction();
            tagPicker.addAction(valueClearAction);

            tagPicker.setInlineTags(true);

            return tagPicker;
        }

        protected ValueClearAction createValueClearAction() {
            ValueClearAction valueClearAction = (ValueClearAction) actions.create(ValueClearAction.ID);
            return valueClearAction;
        }

        protected TagLookupAction createTagLookupAction(ReportInputParameter parameter, TagPicker tagPicker) {
            TagLookupAction tagLookupAction = (TagLookupAction) actions.create(TagLookupAction.ID);
            tagLookupAction.setTagPicker(tagPicker);
            tagLookupAction.setMultiSelect(true);

            String screen = parameter.getScreen();
            if (StringUtils.isNotEmpty(screen)) {
                tagLookupAction.setScreenId(screen);
            }
            return tagLookupAction;
        }

        protected CollectionContainer createCollectionContainer(MetaClass entityMetaClass) {
            CollectionContainer collectionContainer = factory.createCollectionContainer(entityMetaClass.getJavaClass());
            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.LOCAL)
                    .build();

            collectionContainer.setFetchPlan(fetchPlan);
            CollectionLoader loader = factory.createCollectionLoader();
            loader.setContainer(collectionContainer);

            String query = String.format("select e from %s e", entityMetaClass.getName());
            loader.setQuery(query);
            loader.load();

            return collectionContainer;
        }
    }
}