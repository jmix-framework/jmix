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

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.autocomplete.JpqlUiSuggestionProvider;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.JpqlFilterCondition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@UiController("ui_JpqlFilterCondition.edit")
@UiDescriptor("jpql-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class JpqlFilterConditionEdit extends FilterConditionEdit<JpqlFilterCondition> {

    @Autowired
    protected JpqlUiSuggestionProvider jpqlSuggestionFactory;
    @Autowired
    protected JpqlFilterSupport jpqlFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
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
    protected HBoxLayout defaultValueBox;
    @Autowired
    protected ComboBox<Class> parameterClassField;
    @Autowired
    protected ComboBox<Class> entityClassField;
    @Autowired
    protected ComboBox<Class> enumClassField;
    @Autowired
    protected CheckBox hasInExpressionField;
    @Autowired
    protected TextField<String> parameterNameField;

    protected HasValue defaultValueField;

    protected MetaClass filterMetaClass;

    @Override
    public InstanceContainer<JpqlFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Override
    public void setCurrentConfiguration(Filter.Configuration currentConfiguration) {
        super.setCurrentConfiguration(currentConfiguration);

        filterMetaClass = currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        initParameterClassFieldOptionsMap();
        initEntityClassField();
        initEnumClassField();
    }

    protected void initParameterClassFieldOptionsMap() {
        Map<String, Class> optionsMap = new LinkedHashMap<>();
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.string"), String.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.dateWithoutTime"), LocalDate.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.date"), Date.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.boolean"), Boolean.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.double"), Double.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.decimal"), BigDecimal.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.integer"), Integer.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.long"), Long.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.uuid"), UUID.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.entity"), Entity.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.enum"), Enum.class);
        optionsMap.put(messages.getMessage(JpqlFilterConditionEdit.class, "parameterClassField.void"), Void.class);
        parameterClassField.setOptionsMap(optionsMap);
        parameterClassField.setPageLength(optionsMap.size());
    }

    protected void initEntityClassField() {
        Map<String, Class> optionsMap = new TreeMap<>();
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (!metadataTools.isSystemLevel(metaClass)) {
                optionsMap.put(messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")",
                        metaClass.getJavaClass());
            }
        }
        entityClassField.setOptionsMap(optionsMap);
    }

    protected void initEnumClassField() {
        Map<String, Class> optionsMap = new TreeMap<>();
        for (Class enumClass : metadataTools.getAllEnums()) {
            optionsMap.put(getEnumClassName(enumClass), enumClass);
        }
        enumClassField.setOptionsMap(optionsMap);
    }

    protected String getEnumClassName(Class enumClass) {
        return enumClass.getSimpleName() + " (" + messages.getMessage(enumClass, enumClass.getSimpleName()) + ")";
    }

    @Subscribe("entityClassField")
    protected void onEntityClassFieldValueChange(HasValue.ValueChangeEvent<Class> event) {
        if (event.isUserOriginated()) {
            updateDefaultValueByClass(event.getValue());
        }
    }

    @Subscribe("enumClassField")
    protected void onEnumClassFieldValueChange(HasValue.ValueChangeEvent<Class> event) {
        if (event.isUserOriginated()) {
            updateDefaultValueByClass(event.getValue());
        }
    }

    protected void updateDefaultValueByClass(@Nullable Class parameterClass) {
        if (parameterClass != null) {
            getEditedEntity().setParameterClass(parameterClass.getName());
            updateParameterName(parameterClass);
        } else {
            getEditedEntity().setParameterClass(null);
        }
        resetDefaultValue();
        initDefaultValueField();
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initSuggesters();
        initParameterClassField();
        initDefaultValueField();
    }

    protected void initSuggesters() {
        if (filterMetaClass != null) {
            joinField.setSuggester((source, text, cursorPosition) -> requestHint(true));
            whereField.setSuggester((source, text, cursorPosition) -> requestHint(false));
        }
    }

    protected void initParameterClassField() {
        if (getEditedEntity().getParameterClass() != null) {
            Class parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());
            if (Entity.class.isAssignableFrom(parameterClass)) {
                parameterClassField.setValue(Entity.class);
                entityClassField.setValue(parameterClass);
            } else if (Enum.class.isAssignableFrom(parameterClass)) {
                parameterClassField.setValue(Enum.class);
                enumClassField.setValue(parameterClass);
            } else {
                parameterClassField.setValue(parameterClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void initDefaultValueField() {
        if (filterMetaClass != null
                && getEditedEntity().getParameterClass() != null) {
            Class parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());
            defaultValueField = singleFilterSupport.generateValueComponent(filterMetaClass,
                    getEditedEntity().getHasInExpression(), parameterClass);

            if (getEditedEntity().getValueComponent() != null
                    && getEditedEntity().getValueComponent().getDefaultValue() != null) {
                String modelDefaultValue = getEditedEntity().getValueComponent().getDefaultValue();
                Object defaultValue = jpqlFilterSupport.parseDefaultValue(parameterClass,
                        getEditedEntity().getHasInExpression(), modelDefaultValue);
                defaultValueField.setValue(defaultValue);
            }
        } else {
            defaultValueField = uiComponents.create(TextField.TYPE_STRING);
            defaultValueField.setEnabled(false);
        }

        defaultValueBox.removeAll();
        defaultValueBox.add(defaultValueField);
        defaultValueField.setWidthFull();
    }

    protected List<Suggestion> requestHint(boolean inJoinClause) {
        return jpqlSuggestionFactory.getSuggestions(
                inJoinClause ? joinField.getAutoCompleteSupport() : whereField.getAutoCompleteSupport(),
                joinField.getValue(),
                whereField.getValue(),
                filterMetaClass.getName(),
                inJoinClause
        );
    }

    @Subscribe("parameterClassField")
    protected void onParameterClassFieldValueChange(HasValue.ValueChangeEvent<Class> event) {
        Class parameterClass = event.getValue();

        entityClassField.setVisible(parameterClass == Entity.class);
        if (parameterClass != Entity.class) {
            entityClassField.setValue(null);
        }

        enumClassField.setVisible(parameterClass == Enum.class);
        if (parameterClass != Enum.class) {
            enumClassField.setValue(null);
        }

        hasInExpressionField.setVisible(parameterClass != Boolean.class && parameterClass != Void.class);
        if (parameterClass == Boolean.class || parameterClass == Void.class) {
            hasInExpressionField.setValue(false);
        }

        parameterNameField.setVisible(parameterClass != Void.class);

        if (event.isUserOriginated()) {
            if (parameterClass != null
                    && parameterClass != Entity.class
                    && parameterClass != Enum.class) {
                getEditedEntity().setParameterClass(parameterClass.getName());
                updateParameterName(parameterClass);
            } else {
                getEditedEntity().setParameterClass(null);
            }

            resetDefaultValue();
            initDefaultValueField();
        }
    }

    protected void updateParameterName(Class parameterClass) {
        String parameterName = jpqlFilterSupport.generateParameterName(getEditedEntity().getComponentId(),
                parameterClass.getSimpleName());
        getEditedEntity().setParameterName(parameterName);
    }

    @Subscribe("hasInExpressionField")
    protected void onHasInExpressionFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            resetDefaultValue();
            initDefaultValueField();
        }
    }

    protected void resetDefaultValue() {
        FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
        if (valueComponent != null) {
            valueComponent.setDefaultValue(null);
        }
    }

    @SuppressWarnings("rawtypes")
    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (defaultValueField != null
                && getEditedEntity().getParameterClass() != null
                && getEditedEntity().getValueComponent() != null) {
            Class parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());
            String modelDefaultValue = jpqlFilterSupport.formatDefaultValue(parameterClass,
                    getEditedEntity().getHasInExpression(), defaultValueField.getValue());

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            valueComponent.setDefaultValue(modelDefaultValue);
            valueComponent.setComponentName(singleFilterSupport.getValueComponentName(defaultValueField));
        }

        String caption = getEditedEntity().getCaption();
        if (!Strings.isNullOrEmpty(caption)) {
            getEditedEntity().setLocalizedCaption(caption);
        }
    }
}
