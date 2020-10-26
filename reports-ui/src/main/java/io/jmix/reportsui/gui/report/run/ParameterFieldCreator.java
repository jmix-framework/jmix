/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.gui.report.run;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.QueryTransformer;
import io.jmix.core.QueryTransformerFactory;
import com.haulmont.chile.core.datatypes.Datatypes;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.CollectionDatasource.RefreshMode;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportType;
import io.jmix.ui.component.Component;
import io.jmix.ui.xml.layout.ComponentsFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public class ParameterFieldCreator {

    public static final String COMMON_LOOKUP_SCREEN_ID = "commonLookup";

    protected ComponentsFactory componentsFactory = AppBeans.get(ComponentsFactory.class);

    protected Messages messages = AppBeans.get(Messages.class);
    protected Metadata metadata = AppBeans.get(Metadata.class);
    protected Scripting scripting = AppBeans.get(Scripting.class);
    protected ReportService reportService = AppBeans.get(ReportService.class);
    protected QueryTransformerFactory queryTransformerFactory = AppBeans.get(QueryTransformerFactory.class);

    protected LegacyFrame frame;

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

    public ParameterFieldCreator(LegacyFrame frame) {
        this.frame = frame;
    }

    public Label createLabel(ReportInputParameter parameter, Field field) {
        Label label = componentsFactory.createComponent(Label.class);
        label.setAlignment(field instanceof TokenList ? Component.Alignment.TOP_LEFT : Component.Alignment.MIDDLE_LEFT);
        label.setWidth(Component.AUTO_SIZE);
        label.setValue(parameter.getLocName());
        return label;
    }

    public Field createField(ReportInputParameter parameter) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setRequiredMessage(messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", parameter.getLocName()));

        field.setId("param_" + parameter.getAlias());
        field.setWidth("100%");
        field.setFrame(frame.getWrappedFrame());
        field.setEditable(true);

        field.setRequired(parameter.getRequired());
        return field;
    }

    protected void setCurrentDateAsNow(ReportInputParameter parameter, Field dateField) {
        Date now = reportService.currentDateOrTime(parameter.getType());
        dateField.setValue(now);
        parameter.setDefaultValue(reportService.convertToString(now.getClass(), now));
    }

    protected interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    protected class DateFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = componentsFactory.createComponent(DateField.class);
            dateField.setResolution(DateField.Resolution.DAY);
            dateField.setDateFormat(messages.getMainMessage("dateFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = componentsFactory.createComponent(DateField.class);
            dateField.setResolution(DateField.Resolution.MIN);
            dateField.setDateFormat(messages.getMainMessage("dateTimeFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            Field timeField = componentsFactory.createComponent(TimeField.class);
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, timeField);
            }
            return componentsFactory.createComponent(TimeField.class);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            CheckBox checkBox = componentsFactory.createComponent(CheckBox.class);
            checkBox.setAlignment(Component.Alignment.MIDDLE_LEFT);
            return checkBox;
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return componentsFactory.createComponent(TextField.class);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = componentsFactory.createComponent(TextField.class);
            textField.addValidator(new DoubleValidator());
            textField.setDatatype(Datatypes.getNN(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            LookupField lookupField = componentsFactory.createComponent(LookupField.class);
            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotBlank(enumClassName)) {
                Class enumClass = scripting.loadClass(enumClassName);

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
            PickerField field;
            MetaClass entityMetaClass = metadata.getClassNN(parameter.getEntityMetaClass());

            if (isLookup) {
                field = componentsFactory.createComponent(LookupPickerField.class);

                CollectionDatasource ds = DsBuilder.create()
                        .setViewName(View.MINIMAL)
                        .setMetaClass(entityMetaClass)
                        .buildCollectionDatasource();
                ds.setRefreshOnComponentValueChange(true);

                String whereClause = parameter.getLookupWhere();
                String joinClause = parameter.getLookupJoin();
                if (!Strings.isNullOrEmpty(whereClause)) {
                    String query = String.format("select e from %s e", entityMetaClass.getName());
                    QueryTransformer queryTransformer = queryTransformerFactory.transformer(query);
                    queryTransformer.addWhere(whereClause);
                    if (!Strings.isNullOrEmpty(joinClause)) {
                        queryTransformer.addJoin(joinClause);
                    }
                    query = queryTransformer.getResult();
                    ds.setQuery(query);
                }
                ((DatasourceImplementation) ds).initialized();
                ((LookupPickerField) field).setOptionsDatasource(ds);
            } else {
                field = componentsFactory.createComponent(PickerField.class);
            }
            field.setMetaClass(entityMetaClass);
            PickerField.LookupAction pickerLookupAction = field.addLookupAction();
            field.addAction(pickerLookupAction);
            field.addClearAction();

            String parameterScreen = parameter.getScreen();

            if (StringUtils.isNotEmpty(parameterScreen)) {
                pickerLookupAction.setLookupScreen(parameterScreen);
                pickerLookupAction.setLookupScreenParams(Collections.emptyMap());
            } else {
                pickerLookupAction.setLookupScreen(COMMON_LOOKUP_SCREEN_ID);

                Map<String, Object> params = new HashMap<>();
                //TODO class parameter
//                params.put(CLASS_PARAMETER, entityMetaClass);

                if (parameter.getReport().getReportType() == ReportType.SIMPLE) {
                    WindowParams.MULTI_SELECT.set(params, false);
                }

                pickerLookupAction.setLookupScreenParams(params);
            }

            return field;
        }
    }

    protected class MultiFieldCreator implements FieldCreator {

        @Override
        public Field createField(final ReportInputParameter parameter) {
            TokenList tokenList = componentsFactory.createComponent(TokenList.class);
            MetaClass entityMetaClass = metadata.getClassNN(parameter.getEntityMetaClass());

            DsBuilder builder = DsBuilder.create(frame.getDsContext());
            CollectionDatasource cds = builder
                    .setRefreshMode(RefreshMode.NEVER)
                    .setId("entities_" + parameter.getAlias())
                    .setMetaClass(entityMetaClass)
                    .setViewName(View.LOCAL)
                    .setAllowCommit(false)
                    .buildCollectionDatasource();

            cds.refresh();

            tokenList.setDatasource(cds,tokenList.getMetaProperty().getName());
            tokenList.setEditable(true);
            tokenList.setLookup(true);
            tokenList.setHeight("120px");

            String screen = parameter.getScreen();

            if (StringUtils.isNotEmpty(screen)) {
                tokenList.setLookupScreen(screen);
                tokenList.setLookupScreenParams(Collections.emptyMap());
            } else {
                tokenList.setLookupScreen("commonLookup");
                //TODO class parameter
//                tokenList.setLookupScreenParams(ParamsMap.of(CLASS_PARAMETER, entityMetaClass));
            }

            tokenList.setAddButtonCaption(messages.getMessage(TokenList.class, "actions.Select"));
            tokenList.setInline(true);
            tokenList.setSimple(true);

            return tokenList;
        }
    }
}