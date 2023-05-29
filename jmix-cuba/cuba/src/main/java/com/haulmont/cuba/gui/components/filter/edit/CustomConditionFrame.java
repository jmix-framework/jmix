/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter.edit;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.filter.*;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.CustomCondition;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.QueryParamValuesManager;
import io.jmix.data.impl.jpql.DomainModel;
import io.jmix.data.impl.jpql.DomainModelBuilder;
import io.jmix.data.impl.jpql.DomainModelWithCaptionsBuilder;
import io.jmix.ui.component.*;
import io.jmix.ui.component.autocomplete.JpqlUiSuggestionProvider;
import io.jmix.ui.component.autocomplete.Suggestion;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomConditionFrame extends ConditionFrame<CustomCondition> {

    protected static final String WHERE = " where ";

    @Autowired
    protected LookupField<ParamType> typeSelect;
    @Autowired
    protected LookupField<Object> entitySelect;

    @Autowired
    protected CheckBox inExprCb;

    @Autowired
    protected TextField<String> nameField;
    @Autowired
    protected TextField<String> entityParamViewField;

    @Autowired
    protected SourceCodeEditor joinField;
    @Autowired
    protected SourceCodeEditor whereField;
    @Autowired
    protected SourceCodeEditor entityParamWhereField;

    @Autowired
    protected CheckBox useUserTimeZone;

    @Autowired
    protected Label<String> paramViewLab;
    @Autowired
    protected Label<String> paramWhereLab;
    @Autowired
    protected Label<String> entityLab;
    @Autowired
    protected Label<String> nameLab;
    @Autowired
    protected Label<String> useUserTimeZoneLab;

    @Autowired
    protected UserSessionSource userSessionSource;

    @Autowired
    protected JpqlUiSuggestionProvider jpqlUiSuggestionProvider;

    @Autowired
    protected QueryParamValuesManager queryParamValuesManager;

    protected boolean initializing;

    protected ConditionsTree conditionsTree;

    protected static final Pattern PARAM_PATTERN = Pattern.compile(":([a-zA-Z_0-9$\\.]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        conditionsTree = (ConditionsTree) params.get("conditionsTree");
    }

    @Override
    public void initComponents() {
        super.initComponents();

        typeSelect.addValueChangeListener(e -> {
            boolean disableTypeCheckBox = ParamType.UNARY.equals(typeSelect.getValue()) ||
                    ParamType.BOOLEAN.equals(typeSelect.getValue());
            inExprCb.setEnabled(!disableTypeCheckBox);
            if (disableTypeCheckBox)
                inExprCb.setValue(false);

            boolean isEntity = ParamType.ENTITY.equals(typeSelect.getValue());
            boolean isEnum = ParamType.ENUM.equals(typeSelect.getValue());
            boolean isDate = ParamType.DATE.equals(typeSelect.getValue());

            entityLab.setEnabled(isEntity || isEnum);
            entitySelect.setEnabled(isEntity || isEnum);
            entitySelect.setRequired(entitySelect.isEnabled());
            paramWhereLab.setEnabled(isEntity);
            entityParamWhereField.setEnabled(isEntity);
            paramViewLab.setEnabled(isEntity);
            entityParamViewField.setEnabled(isEntity);

            useUserTimeZoneLab.setVisible(isDate);
            useUserTimeZone.setVisible(isDate);

            Param param = condition.getParam();
            fillEntitySelect(param);

            //recreate default value component based on param type
            if (!initializing && defaultValueLayout.isVisibleRecursive()) {
                ParamType paramType = e.getValue();
                if ((isEntity || isEnum) && (entitySelect.getValue() == null)) {
                    defaultValueLayout.remove(defaultValueComponent);
                    param.setJavaClass(null);
                } else {
                    Class paramJavaClass = getParamJavaClass(paramType);
                    param.setJavaClass(paramJavaClass);
                    param.setDefaultValue(null);
                    createDefaultValueComponent();
                }
            }
        });

        inExprCb.addValueChangeListener(e -> {
            condition.getParam().setInExpr(BooleanUtils.isTrue(e.getValue()));
            //recreate default value component based on "in list" checkbox value
            if (!initializing && defaultValueLayout.isVisibleRecursive()) {
                condition.getParam().setDefaultValue(null);
                createDefaultValueComponent();
            }
        });

        useUserTimeZone.addValueChangeListener(e -> {
            if (defaultValueComponent != null) {
                if (defaultValueComponent instanceof DateField) {
                    DateField dateField = (DateField) defaultValueComponent;
                    if (Boolean.TRUE.equals(e.getValue())) {
                        UserSession userSession = userSessionSource.getUserSession();
                        if (userSession.getTimeZone() != null) {
                            dateField.setTimeZone(userSession.getTimeZone());
                        }
                        dateField.setValue(null);
                        dateField.setEditable(false);
                    } else {
                        dateField.setTimeZone(TimeZone.getDefault());
                        dateField.setEditable(true);
                    }
                }
            }
        });

        entitySelect.addValueChangeListener(e -> {
            if (initializing || !defaultValueLayout.isVisibleRecursive()) {
                return;
            }
            if (e.getValue() == null) {
                defaultValueLayout.remove(defaultValueComponent);
                return;
            }
            Param param = condition.getParam();
            Class paramJavaClass = e.getValue() instanceof Class ?
                    (Class) e.getValue() : ((MetaClass) e.getValue()).getJavaClass();
            param.setJavaClass(paramJavaClass);
            param.setDefaultValue(null);
            createDefaultValueComponent();
        });

        FilterHelper filterHelper = AppBeans.get(FilterHelper.class);
        filterHelper.setLookupNullSelectionAllowed(typeSelect, false);
        filterHelper.setLookupFieldPageLength(typeSelect, 12);

        joinField.setSuggester((source, text, cursorPosition) -> requestHint(joinField, text, cursorPosition));
        joinField.setHighlightActiveLine(false);
        joinField.setShowGutter(false);

        whereField.setSuggester((source, text, cursorPosition) -> requestHint(whereField, text, cursorPosition));
        whereField.setHighlightActiveLine(false);
        whereField.setShowGutter(false);

        entityParamWhereField.setSuggester((source, text, cursorPosition) -> requestHintParamWhere(entityParamWhereField, text, cursorPosition));
        entityParamWhereField.setHighlightActiveLine(false);
        entityParamWhereField.setShowGutter(false);
    }

    @Override
    public void setCondition(final CustomCondition condition) {
        super.setCondition(condition);
        initializing = true;

        nameField.setValue(condition.getLocCaption());
        boolean isNameEditable = Strings.isNullOrEmpty(condition.getCaption()) || !condition.getCaption().startsWith("msg://");
        nameField.setEnabled(isNameEditable);
        nameLab.setEnabled(isNameEditable);
        joinField.setValue(condition.getJoin());
        String where = replaceParamWithQuestionMark(condition.getWhere());
        whereField.setValue(where);
        inExprCb.setValue(condition.getInExpr());
        entityParamWhereField.setValue(condition.getEntityParamWhere());
        entityParamViewField.setValue(condition.getEntityParamView());
        useUserTimeZone.setValue(condition.getUseUserTimeZone());

        fillTypeSelect(condition.getParam());
        fillEntitySelect(condition.getParam());
        initializing = false;
    }

    protected void fillEntitySelect(Param param) {
        if (!entitySelect.isEnabled()) {
            entitySelect.setValue(null);
            return;
        }

        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        MessageTools messageTools = AppBeans.get(MessageTools.class);

        Map<String, Object> items = new TreeMap<>();
        Object selectedItem = null;
        if (ParamType.ENTITY.equals(typeSelect.getValue())) {
            for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
                if (!metadataTools.isSystemLevel(metaClass)) {
                    items.put(messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")", metaClass);
                }
            }

            if (param != null && Param.Type.ENTITY.equals(param.getType())) {
                Class javaClass = param.getJavaClass();
                Metadata metadata = AppBeans.get(Metadata.class);
                selectedItem = metadata.getClass(javaClass);
            }
            entitySelect.setOptionsMap(items);
            entitySelect.setValue(selectedItem);

        } else if (ParamType.ENUM.equals(typeSelect.getValue())) {
            if (param != null && Param.Type.ENUM.equals(param.getType())) {
                selectedItem = param.getJavaClass();
            }

            boolean selectedItemFound = false;
            for (Class enumClass : metadataTools.getAllEnums()) {
                items.put(getEnumClassName(enumClass), enumClass);

                if (selectedItem == null || selectedItem.equals(enumClass))
                    selectedItemFound = true;
            }
            // In case of a predefined custom condition parameter value may be of type which is not contained in
            // the meta model, hence not in MetadataHelper.getAllEnums(). So we just add it here.
            if (selectedItem != null && !selectedItemFound) {
                items.put(getEnumClassName((Class) selectedItem), selectedItem);
            }

            entitySelect.setOptionsMap(items);
            entitySelect.setValue(selectedItem);
        }
    }

    protected String getEnumClassName(Class enumClass) {
        return enumClass.getSimpleName() + " (" + messages.getMessage(enumClass, enumClass.getSimpleName()) + ")";
    }


    protected void fillTypeSelect(Param param) {
        Map<String, ParamType> values = new LinkedHashMap<>();
        for (ParamType paramType : ParamType.values()) {
            values.put(paramType.getLocCaption(), paramType);
        }
        typeSelect.setOptionsMap(values);

        if (param == null) {
            typeSelect.setValue(ParamType.STRING);
        } else {
            switch (param.getType()) {
                case ENTITY:
                    typeSelect.setValue(ParamType.ENTITY);
                    break;
                case ENUM:
                    typeSelect.setValue(ParamType.ENUM);
                    break;
                case DATATYPE:
                    if (String.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.STRING);
                    else if (java.sql.Date.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.DATE);
                    else if (Date.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.DATETIME);
                    else if (Boolean.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.BOOLEAN);
                    else if (BigDecimal.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.BIGDECIMAL);
                    else if (Double.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.DOUBLE);
                    else if (Integer.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.INTEGER);
                    else if (Long.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.LONG);
                    else if (UUID.class.equals(param.getJavaClass()))
                        typeSelect.setValue(ParamType.UUID);
                    else
                        throw new UnsupportedOperationException("Unsupported param class: " + param.getJavaClass());
                    break;
                case UNARY:
                    typeSelect.setValue(ParamType.UNARY);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported param type: " + param.getType());
            }
        }
    }

    protected String replaceParamWithQuestionMark(String where) {
        String res = StringUtils.trim(where);
        if (!StringUtils.isBlank(res)) {
            Matcher matcher = PARAM_PATTERN.matcher(res);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                if (!queryParamValuesManager.supports(matcher.group(1))) {
                    matcher.appendReplacement(sb, "?");
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return res;
    }

    @Override
    public boolean commit() {
        if (!super.commit())
            return false;

        ParamType type = typeSelect.getValue();
        if (ParamType.ENTITY.equals(type) && entitySelect.getValue() == null) {
            showNotification("Select entity", NotificationType.HUMANIZED);
            return false;
        }

        if (nameField.isEnabled()) {
            String nameText = nameField.getValue();
            if (!Strings.isNullOrEmpty(nameText)) {
                condition.setLocCaption(nameText);
            }
        }

        condition.setJoin(joinField.<String>getValue());

        ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
        String paramName = condition.getParam() != null ? condition.getParam().getName() : paramBuilder.createParamName(condition);
        String where = whereField.getValue();
        if (where != null) {
            where = where.replace("?", ":" + paramName);
        }

        condition.setWhere(where);
        condition.setUnary(ParamType.UNARY.equals(type));
        condition.setInExpr(BooleanUtils.isTrue(inExprCb.getValue()));

        Class javaClass = getParamJavaClass(type);
        condition.setJavaClass(javaClass);

        String entityParamWhere = entityParamWhereField.getValue();
        condition.setEntityParamWhere(entityParamWhere);

        String entityParamView = entityParamViewField.getValue();
        condition.setEntityParamView(entityParamView);

        condition.setUseUserTimeZone(useUserTimeZone.getValue());

        Param param = Param.Builder.getInstance()
                .setName(paramName)
                .setJavaClass(javaClass)
                .setEntityWhere(entityParamWhere)
                .setEntityView(entityParamView)
                .setMetaClass(condition.getEntityMetaClass())
                .setInExpr(condition.getInExpr())
                .setRequired(condition.getRequired())
                .setUseUserTimeZone(condition.getUseUserTimeZone())
                .build();

        param.setDefaultValue(condition.getParam().getDefaultValue());

        condition.setParam(param);

        return true;
    }

    @Nullable
    protected Class getParamJavaClass(ParamType type) {
        switch (type) {
            case STRING:
                return String.class;
            case DATE:
                return java.sql.Date.class;
            case DATETIME:
                return Date.class;
            case DOUBLE:
                return Double.class;
            case BIGDECIMAL:
                return BigDecimal.class;
            case INTEGER:
                return Integer.class;
            case LONG:
                return Long.class;
            case BOOLEAN:
                return Boolean.class;
            case UUID:
                return UUID.class;
            case ENTITY:
                MetaClass entity = (MetaClass) entitySelect.getValue();
                if (entity == null)
                    return null;
                return entity.getJavaClass();
            case ENUM:
                Class enumClass = (Class) entitySelect.getValue();
                if (enumClass == null)
                    return null;
                return enumClass;
            case UNARY:
                return null;
        }
        return null;
    }

    protected List<Suggestion> requestHint(SourceCodeEditor sender, String text, int senderCursorPosition) {
        String joinStr = joinField.getValue();
        String whereStr = whereField.getValue();

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        String queryStart = "select " + entityAlias + " from " + condition.getEntityMetaClass().getName() + " " + entityAlias + " ";

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == joinField) {
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
            if (sender == whereField) {
                queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition - 1;
            }
            queryBuilder.append(WHERE).append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

        return jpqlUiSuggestionProvider.getSuggestions(query, queryPosition, sender.getAutoCompleteSupport());
    }

    protected List<Suggestion> requestHintParamWhere(SourceCodeEditor sender, String text, int senderCursorPosition) {
        String whereStr = entityParamWhereField.getValue();
        MetaClass metaClass = (MetaClass) entitySelect.getValue();
        if (metaClass == null) {
            return new ArrayList<>();
        }

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        String queryStart = "select " + entityAlias + " from " + metaClass.getName() + " " + entityAlias + " ";

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (whereStr != null && !whereStr.equals("")) {
            queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition - 1;
            queryBuilder.append(WHERE).append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

        DomainModelBuilder builder = AppBeans.get(DomainModelWithCaptionsBuilder.class);
        DomainModel domainModel = builder.produce();

        return jpqlUiSuggestionProvider.getSuggestions(query, queryPosition, sender.getAutoCompleteSupport(),
                this::buildParameterOptions);
    }

    public void getJoinClauseHelp() {
        showMessageDialog(messages.getMessage("filter.customConditionFrame.join"),
                messages.getMessage("filter.customConditionFrame.joinClauseHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width("600px"));
    }

    public void getWhereClauseHelp() {
        showMessageDialog(messages.getMessage("filter.customConditionFrame.where"),
                messages.getMessage("filter.customConditionFrame.whereClauseHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width("600px"));
    }

    public void getParamWhereClauseHelp() {
        showMessageDialog(messages.getMessage("filter.customConditionFrame.entityParamWhere"),
                messages.getMessage("filter.customConditionFrame.paramWhereClauseHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width("600px"));
    }

    protected Map<String, String> buildParameterOptions() {
        return conditionsTree.toConditionsList().stream()
                .filter(c -> Objects.nonNull(c.getParam()))
                .collect(Collectors.toMap(c -> c.getParam().getName(), AbstractCondition::getLocCaption));
    }
}
