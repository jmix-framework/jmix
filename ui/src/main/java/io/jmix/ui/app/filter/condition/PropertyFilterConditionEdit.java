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

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@UiController("ui_PropertyFilterCondition.edit")
@UiDescriptor("property-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class PropertyFilterConditionEdit extends FilterConditionEdit<PropertyFilterCondition> {

    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected InstanceContainer<PropertyFilterCondition> filterConditionDc;

    @Autowired
    protected TextField<String> propertyField;
    @Autowired
    protected TextField<String> captionField;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected TextField<String> parameterNameField;
    @Autowired
    protected ComboBox<PropertyFilter.Operation> operationField;
    @Autowired
    protected HBoxLayout defaultValueBox;

    protected MetaClass filterMetaClass = null;
    protected HasValue defaultValueField;

    @Override
    public InstanceContainer<PropertyFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initFilterMetaClass();
        initOperationField();
        initDefaultValueField();
    }

    protected void initFilterMetaClass() {
        if (getEditedEntity().getMetaClass() != null) {
            filterMetaClass = metadata.getClass(getEditedEntity().getMetaClass());
        }
    }

    @SuppressWarnings("unchecked")
    protected void initOperationField() {
        List<PropertyFilter.Operation> operations;
        if (getEditedEntity().getProperty() != null && getEditedEntity().getMetaClass() != null) {
            EnumSet<PropertyFilter.Operation> availableOperations = propertyFilterSupport
                    .getAvailableOperations(filterMetaClass, getEditedEntity().getProperty());
            operations = new ArrayList<>(availableOperations);
        } else {
            operations = Collections.EMPTY_LIST;
        }

        operationField.setOptionsList(operations);
    }

    @SuppressWarnings("unchecked")
    protected void initDefaultValueField() {
        if (filterMetaClass != null
                && getEditedEntity().getProperty() != null
                && getEditedEntity().getOperation() != null) {
            defaultValueField = singleFilterSupport.generateValueComponent(filterMetaClass,
                    getEditedEntity().getProperty(), getEditedEntity().getOperation());

            if (getEditedEntity().getValueComponent() != null
                    && getEditedEntity().getValueComponent().getDefaultValue() != null) {
                String modelDefaultValue = getEditedEntity().getValueComponent().getDefaultValue();
                MetaProperty metaProperty = filterMetaClass.findProperty(getEditedEntity().getProperty());
                if (metaProperty != null) {
                    Object defaultValue = propertyFilterSupport.parseDefaultValue(metaProperty,
                            getEditedEntity().getOperation().getType(), modelDefaultValue);
                    defaultValueField.setValue(defaultValue);
                }
            }
        } else {
            defaultValueField = uiComponents.create(TextField.TYPE_STRING);
            defaultValueField.setEnabled(false);
        }

        defaultValueBox.removeAll();
        defaultValueBox.add(defaultValueField);
        defaultValueField.setWidthFull();
    }

    @Install(to = "operationField", subject = "optionCaptionProvider")
    protected String operationFieldOptionCaptionProvider(PropertyFilter.Operation operation) {
        return propertyFilterSupport.getOperationCaption(operation);
    }

    @Subscribe("propertyField")
    protected void onPropertyFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String property = event.getValue();
        if (StringUtils.isNotEmpty(property) && event.isUserOriginated()) {
            String parameterName = PropertyConditionUtils.generateParameterName(property);
            getEditedEntity().setParameterName(parameterName);

            String caption = propertyFilterSupport.getPropertyFilterCaption(filterMetaClass, property);
            getEditedEntity().setCaption(caption);

            EnumSet<PropertyFilter.Operation> availableOperations = propertyFilterSupport
                    .getAvailableOperations(filterMetaClass, property);
            operationField.setOptionsList(new ArrayList<>(availableOperations));
            if (operationField.getValue() != null && !availableOperations.contains(operationField.getValue())) {
                operationField.setValue(null);
            }

            initDefaultValueField();
        }
    }

    @Subscribe("operationField")
    protected void onOperationFieldValueChange(HasValue.ValueChangeEvent<PropertyFilter.Operation> event) {
        PropertyFilter.Operation operation = event.getValue();
        if (operation != null && event.isUserOriginated()) {
            initDefaultValueField();
        }
    }

    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (defaultValueField != null
                && getEditedEntity().getProperty() != null
                && getEditedEntity().getOperation() != null) {
            MetaProperty metaProperty = filterMetaClass.findProperty(getEditedEntity().getProperty());

            String modelDefaultValue = null;
            if (metaProperty != null) {
                modelDefaultValue = propertyFilterSupport.formatDefaultValue(metaProperty,
                        getEditedEntity().getOperation().getType(), defaultValueField.getValue());
            }

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            valueComponent.setDefaultValue(modelDefaultValue);
            valueComponent.setComponentName(singleFilterSupport.getValueComponentName(defaultValueField));
        }
    }
}
