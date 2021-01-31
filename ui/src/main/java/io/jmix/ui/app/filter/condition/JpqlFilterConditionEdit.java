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

package io.jmix.ui.app.filter.condition;

import io.jmix.core.ClassManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.autocomplete.JpqlSuggestionFactory;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.JpqlFilterCondition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("ui_JpqlFilterCondition.edit")
@UiDescriptor("jpql-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class JpqlFilterConditionEdit extends FilterConditionEdit<JpqlFilterCondition> {

    protected static final String JOIN = "join ";
    protected static final String WHERE = " where ";
    protected static final String PLACEHOLDER = "{E}";

    @Autowired
    protected JpqlSuggestionFactory jpqlSuggestionFactory;
    @Autowired
    protected JpqlFilterSupport jpqlFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected InstanceContainer<JpqlFilterCondition> filterConditionDc;

    @Autowired
    protected SourceCodeEditor joinField;

    @Autowired
    protected SourceCodeEditor whereField;
    @Autowired
    protected TextField<String> parameterClassField;
    @Autowired
    protected HBoxLayout defaultValueBox;

    protected MetaClass filterMetaClass = null;
    protected HasValue defaultValueField;

    @Override
    public InstanceContainer<JpqlFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        joinField.setSuggester((source, text, cursorPosition) -> requestHint(joinField, cursorPosition));
        whereField.setSuggester((source, text, cursorPosition) -> requestHint(whereField, cursorPosition));
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initFilterMetaClass();
        initDefaultValueField();
    }

    protected void initFilterMetaClass() {
        if (getEditedEntity().getMetaClass() != null) {
            filterMetaClass = metadata.getClass(getEditedEntity().getMetaClass());
        }
    }

    @SuppressWarnings("unchecked")
    protected void initDefaultValueField() {
        if (filterMetaClass != null
                && getEditedEntity().getParameterClass() != null) {
            Class parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());
            defaultValueField = singleFilterSupport.generateValueComponent(filterMetaClass, parameterClass);

            if (getEditedEntity().getValueComponent() != null
                    && getEditedEntity().getValueComponent().getDefaultValue() != null) {
                String modelDefaultValue = getEditedEntity().getValueComponent().getDefaultValue();
                Object defaultValue = jpqlFilterSupport.parseDefaultValue(parameterClass, modelDefaultValue);
                defaultValueField.setValue(defaultValue);
            }
        } else {
            defaultValueField = uiComponents.create(TextField.TYPE_STRING);
        }

        defaultValueBox.removeAll();
        defaultValueBox.add(defaultValueField);
        defaultValueField.setWidthFull();
    }

    protected List<Suggestion> requestHint(SourceCodeEditor sender, int senderCursorPosition) {
        String joinStr = joinField.getValue();
        String whereStr = whereField.getValue();

        // CAUTION: the magic entity name! The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        String queryStart = "select " + entityAlias + " from " + getEditedEntity().getMetaClass() + " "
                + entityAlias + " ";

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == joinField) {
                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, JOIN.trim())
                    && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append(JOIN).append(joinStr);
                queryPosition += JOIN.length();
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
        query = query.replace(PLACEHOLDER, entityAlias);

        return jpqlSuggestionFactory.requestHint(query, queryPosition, sender.getAutoCompleteSupport(),
                senderCursorPosition);
    }

    @Subscribe("parameterClassField")
    protected void onParameterClassFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String parameterClass = event.getValue();
        if (event.isUserOriginated() && StringUtils.isNotEmpty(parameterClass)) {
            String parameterName = jpqlFilterSupport.generateParameterName(getEditedEntity().getComponentId(),
                    parameterClass);
            getEditedEntity().setParameterName(parameterName);

            initDefaultValueField();
        }
    }

    @SuppressWarnings("rawtypes")
    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (defaultValueField != null
                && getEditedEntity().getParameterClass() != null
                && getEditedEntity().getValueComponent() != null) {
            Class parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());
            String modelDefaultValue = jpqlFilterSupport.formatDefaultValue(parameterClass, defaultValueField.getValue());
            getEditedEntity().getValueComponent().setDefaultValue(modelDefaultValue);
        }
    }
}
