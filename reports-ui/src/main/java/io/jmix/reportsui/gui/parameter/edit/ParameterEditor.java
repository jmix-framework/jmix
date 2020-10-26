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
package io.jmix.reportsui.gui.parameter.edit;

import io.jmix.core.Id;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reportsui.gui.report.run.ParameterFieldCreator;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.GridLayout;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.component.autocomplete.JpqlSuggestionFactory;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.sys.ScreensHelper;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.*;

import static java.lang.String.format;

public class ParameterEditor extends AbstractEditor<ReportInputParameter> {
    protected final static String LOOKUP_SETTINGS_TAB_ID = "lookupSettingsTab";
    protected final static String WHERE = " where ";

    @Autowired
    protected Label<String> defaultValueLabel;
    @Autowired
    protected BoxLayout defaultValueBox;

    @Autowired
    protected LookupField<String> screen;
    @Autowired
    protected LookupField<String> enumeration;
    @Autowired
    protected LookupField<ParameterType> type;
    @Autowired
    protected LookupField<String> metaClass;
    @Autowired
    protected CheckBox lookup;
    @Autowired
    protected Label<String> lookupLabel;
    @Autowired
    protected SourceCodeEditor lookupWhere;
    @Autowired
    protected SourceCodeEditor lookupJoin;
    @Named("tabsheet.lookupSettingsTab")
    protected VBoxLayout lookupSettingsTab;
    @Autowired
    protected TabSheet tabsheet;
    @Autowired
    protected Label<String> enumerationLabel;

    @Autowired
    protected Label<String> screenLabel;

    @Autowired
    protected Label<String> metaClassLabel;

    @Autowired
    protected GridLayout predefinedTransformationBox;

    @Autowired
    protected CheckBox predefinedTransformation;

    @Autowired
    protected SourceCodeEditor transformationScript;

    @Autowired
    protected SourceCodeEditor validationScript;

    @Autowired
    protected Label<String> transformationScriptLabel;

    @Autowired
    protected LookupField<PredefinedTransformation> wildcards;

    @Autowired
    protected Label<String> wildcardsLabel;

    @Autowired
    protected CheckBox defaultDateIsCurrentCheckBox;

    @Autowired
    protected Label<String> defaultDateIsCurrentLabel;

    @Autowired
    protected Label<String> requiredLabel;

    @Autowired
    protected CheckBox required;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Security security;
    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected Datasource<ReportInputParameter> parameterDs;

    @Autowired
    protected ScreensHelper screensHelper;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    @Autowired
    protected TextArea localeTextField;

    @Autowired
    protected JpqlSuggestionFactory jpqlSuggestionFactory;

    protected ReportInputParameter parameter;

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    @Override
    public void setItem(JmixEntity item) {
        ReportInputParameter newParameter = (ReportInputParameter) metadata.create(parameterDs.getMetaClass());
        metadata.getTools().copy(item, newParameter);
        newParameter.setId((UUID) Id.of(item).getValue());
        if (newParameter.getParameterClass() == null) {
            newParameter.setParameterClass(parameterClassResolver.resolveClass(newParameter));
        }

        super.setItem(newParameter);
        enableControlsByParamType(newParameter.getType());
        initScreensLookup();
        initTransformations();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        type.setOptionsList(Arrays.asList(ParameterType.TEXT, ParameterType.NUMERIC, ParameterType.BOOLEAN, ParameterType.ENUMERATION,
                ParameterType.DATE, ParameterType.TIME, ParameterType.DATETIME, ParameterType.ENTITY, ParameterType.ENTITY_LIST));

        initMetaClassLookup();

        initEnumsLookup();

        initListeners();

        initHelpButtons();

        initCodeEditors();
    }

    protected void initHelpButtons() {
        localeTextField.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("localeText"), getMessage("parameter.localeTextHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(700f)));
        transformationScript.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("transformationScript"), getMessage("parameter.transformationScriptHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(700f)));
        validationScript.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("validationScript"), getMessage("validationScriptHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(700f)));
    }

    @Override
    public boolean commit() {
        if (super.commit()) {
            metadata.getTools().copy(getItem(), parameter);
            return true;
        }
        return false;
    }

    protected void initListeners() {
        type.addValueChangeListener(e ->
                enableControlsByParamType(e.getValue())
        );

        parameterDs.addItemPropertyChangeListener(e -> {
            boolean typeChanged = e.getProperty().equalsIgnoreCase("type");
            boolean classChanged = e.getProperty().equalsIgnoreCase("entityMetaClass")
                    || e.getProperty().equalsIgnoreCase("enumerationClass");
            boolean defaultDateIsCurrentChanged = e.getProperty().equalsIgnoreCase("defaultDateIsCurrent");
            ReportInputParameter parameter = getItem();
            if (typeChanged || classChanged) {
                parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));

                if (typeChanged) {
                    parameter.setEntityMetaClass(null);
                    parameter.setEnumerationClass(null);
                }

                parameter.setDefaultValue(null);
                parameter.setScreen(null);

                initScreensLookup();

                initDefaultValueField();
            }

            if (defaultDateIsCurrentChanged) {
                initDefaultValueField();
                initCurrentDateTimeField();
            }

            ((DatasourceImplementation<ReportInputParameter>) parameterDs).modified(e.getItem());
        });

        lookup.addValueChangeListener(e -> {
            if (Boolean.TRUE.equals(e.getValue())) {
                if (tabsheet.getTab(LOOKUP_SETTINGS_TAB_ID) == null) {
                    tabsheet.addTab(LOOKUP_SETTINGS_TAB_ID, lookupSettingsTab);
                }
            } else {
                if (tabsheet.getTab(LOOKUP_SETTINGS_TAB_ID) != null) {
                    tabsheet.removeTab(LOOKUP_SETTINGS_TAB_ID);
                }
            }
        });
    }

    protected void initCodeEditors() {
        lookupWhere.setSuggester((source, text, cursorPosition) -> requestHint(lookupWhere, cursorPosition));
        lookupWhere.setHeight(themeConstants.get("cuba.gui.customConditionFrame.whereField.height"));

        lookupJoin.setSuggester((source, text, cursorPosition) -> requestHint(lookupJoin, cursorPosition));
        lookupJoin.setHeight(themeConstants.get("cuba.gui.customConditionFrame.joinField.height"));
    }

    protected void initScreensLookup() {
        ReportInputParameter parameter = getItem();
        if (parameter.getType() == ParameterType.ENTITY || parameter.getType() == ParameterType.ENTITY_LIST) {
            Class clazz = parameterClassResolver.resolveClass(parameter);
            if (clazz != null) {
                Map<String, String> screensMap = screensHelper.getAvailableBrowserScreens(clazz);
                screen.setOptionsMap(screensMap);
            }
        }
    }

    protected void initEnumsLookup() {
        Map<String, String> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadata.getTools().getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumLocalizedName + " (" + enumClass.getSimpleName() + ")", enumClass.getCanonicalName());
        }
        enumeration.setOptionsMap(enumsOptionsMap);
    }

    protected void initMetaClassLookup() {
        Map<String, String> metaClassesOptionsMap = new TreeMap<>();
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        for (MetaClass clazz : classes) {
            if (!metadata.getTools().isSystemLevel(clazz)) {
                String caption = messages.getTools().getDetailedEntityCaption(clazz);
                metaClassesOptionsMap.put(caption, clazz.getName());
            }
        }
        metaClass.setOptionsMap(metaClassesOptionsMap);
    }

    @Override
    protected boolean preCommit() {
        if (!(getEditedEntity().getType() == ParameterType.ENTITY && Boolean.TRUE.equals(lookup.getValue()))) {
            lookupWhere.setValue(null);
            lookupJoin.setValue(null);
        }
        return super.preCommit();
    }

    protected void initDefaultValueField() {
        defaultValueLabel.setVisible(false);
        defaultValueBox.removeAll();
        ReportInputParameter parameter = getItem();
        if (canHaveDefaultValue()) {
            Field<Object> field;
            if (ParameterType.ENTITY.equals(parameter.getType()) && Boolean.TRUE.equals(parameter.getLookup())) {
                ReportInputParameter entityParam = metadata.create(ReportInputParameter.class);
                entityParam.setReport(parameter.getReport());
                entityParam.setType(parameter.getType());
                entityParam.setEntityMetaClass(parameter.getEntityMetaClass());
                entityParam.setScreen(parameter.getScreen());
                entityParam.setAlias(parameter.getAlias());
                entityParam.setRequired(parameter.getRequired());
                field = parameterFieldCreator.createField(entityParam);
            } else {
                field = parameterFieldCreator.createField(parameter);
            }

            field.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    parameter.setDefaultValue(reportService.convertToString(e.getValue().getClass(), e.getValue()));
                } else {
                    parameter.setDefaultValue(null);
                }
            });

            if (parameter.getParameterClass() != null) {
                field.setValue(reportService.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue()));
            }
            field.setRequired(false);

            defaultValueBox.add(field);
            defaultValueLabel.setVisible(true);
        }
        defaultValueBox.setEnabled(security.isEntityOpPermitted(metadata.getClassNN(ReportInputParameter.class), EntityOp.UPDATE));
    }

    protected void initCurrentDateTimeField() {
        boolean parameterDateOrTime = isParameterDateOrTime();
        defaultDateIsCurrentLabel.setVisible(parameterDateOrTime);
        defaultDateIsCurrentCheckBox.setVisible(parameterDateOrTime);
    }

    protected boolean canHaveDefaultValue() {
        ReportInputParameter parameter = getItem();
        if (parameter == null) {
            return false;
        }

        if (isParameterDateOrTime() && BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
            return false;
        }

        ParameterType type = parameter.getType();
        return type != null
                && type != ParameterType.ENTITY_LIST
                && (type != ParameterType.ENTITY || StringUtils.isNotBlank(parameter.getEntityMetaClass()))
                && (type != ParameterType.ENUMERATION || StringUtils.isNotBlank(parameter.getEnumerationClass()));
    }

    protected void enableControlsByParamType(ParameterType type) {
        boolean isSingleEntity = type == ParameterType.ENTITY;
        boolean isEntity = isSingleEntity || type == ParameterType.ENTITY_LIST;
        boolean isEnum = type == ParameterType.ENUMERATION;
        boolean isText = type == ParameterType.TEXT;

        metaClass.setVisible(isEntity);
        metaClassLabel.setVisible(isEntity);

        lookup.setVisible(isSingleEntity);
        lookupLabel.setVisible(isSingleEntity);
        if (isSingleEntity && Boolean.TRUE.equals(lookup.getValue())) {
            if (tabsheet.getTab(LOOKUP_SETTINGS_TAB_ID) == null) {
                tabsheet.addTab(LOOKUP_SETTINGS_TAB_ID, lookupSettingsTab);
            }
        } else {
            if (tabsheet.getTab(LOOKUP_SETTINGS_TAB_ID) != null) {
                tabsheet.removeTab(LOOKUP_SETTINGS_TAB_ID);
            }
        }

        screen.setVisible(isEntity);
        screenLabel.setVisible(isEntity);

        enumeration.setVisible(isEnum);
        enumerationLabel.setVisible(isEnum);

        predefinedTransformationBox.setVisible(isText);

        initDefaultValueField();
        initCurrentDateTimeField();
    }

    protected void initTransformations() {
        ReportInputParameter parameter = getItem();
        predefinedTransformation.setValue(parameter.getPredefinedTransformation() != null);
        enableControlsByTransformationType(parameter.getPredefinedTransformation() != null);
        predefinedTransformation.addValueChangeListener(e -> {
            boolean hasPredefinedTransformation = e.getValue() != null && e.getValue();

            enableControlsByTransformationType(hasPredefinedTransformation);
            if (hasPredefinedTransformation) {
                parameter.setTransformationScript(null);
            } else {
                parameter.setPredefinedTransformation(null);
            }
        });
        predefinedTransformation.setEditable(security.isEntityOpPermitted(ReportInputParameter.class, EntityOp.UPDATE));
    }

    protected void enableControlsByTransformationType(boolean hasPredefinedTransformation) {
        transformationScript.setVisible(!hasPredefinedTransformation);
        transformationScriptLabel.setVisible(!hasPredefinedTransformation);
        wildcards.setVisible(hasPredefinedTransformation);
        wildcardsLabel.setVisible(hasPredefinedTransformation);
    }

    protected boolean isParameterDateOrTime() {
        ReportInputParameter parameter = getItem();
        return Optional.ofNullable(parameter)
                .map(reportInputParameter ->
                        ParameterType.DATE.equals(parameter.getType()) ||
                                ParameterType.DATETIME.equals(parameter.getType()) ||
                                ParameterType.TIME.equals(parameter.getType()))
                .orElse(false);
    }

    protected List<Suggestion> requestHint(SourceCodeEditor sender, int senderCursorPosition) {
        String joinStr = lookupJoin.getValue();
        String whereStr = lookupWhere.getValue();

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        Class javaClassForEntity = getItem().getParameterClass();
        if (javaClassForEntity == null) {
            return new ArrayList<>();
        }

        String queryStart = format("select %s from %s %s ", entityAlias, metadata.getClassNN(javaClassForEntity), entityAlias);

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == lookupJoin) {
                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, "join") && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append("join ").append(joinStr);
                queryPosition += "join ".length();
            } else {
                queryBuilder.append(joinStr);
            }
        }
        if (StringUtils.isNotEmpty(whereStr)) {
            if (sender == lookupWhere) {
                queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition - 1;
            }
            queryBuilder.append(WHERE).append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

        return jpqlSuggestionFactory.requestHint(query, queryPosition, sender.getAutoCompleteSupport(), senderCursorPosition);
    }
}
