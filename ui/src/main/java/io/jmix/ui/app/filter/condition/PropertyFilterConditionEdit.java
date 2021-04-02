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
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.filter.FilterMetadataTools;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

@UiController("ui_PropertyFilterCondition.edit")
@UiDescriptor("property-filter-condition-edit.xml")
@EditedEntityContainer("filterConditionDc")
public class PropertyFilterConditionEdit extends FilterConditionEdit<PropertyFilterCondition> {

    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected FilterMetadataTools filterMetadataTools;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected InstanceContainer<PropertyFilterCondition> filterConditionDc;

    @Autowired
    protected ComboBox<String> propertyField;
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

    protected HasValue defaultValueField;

    protected MetaClass filterMetaClass;
    protected String query;
    protected Predicate<MetaPropertyPath> propertiesFilterPredicate;

    @Override
    public InstanceContainer<PropertyFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Override
    public void setCurrentConfiguration(Filter.Configuration currentConfiguration) {
        super.setCurrentConfiguration(currentConfiguration);

        filterMetaClass = currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass();
        query = currentConfiguration.getOwner().getDataLoader().getQuery();
        propertiesFilterPredicate = currentConfiguration.getOwner().getPropertiesFilterPredicate();
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initPropertyField();
        initOperationField();
        initDefaultValueField();
    }

    protected void initPropertyField() {
        if (filterMetaClass != null) {
            List<MetaPropertyPath> paths = filterMetadataTools.getPropertyPaths(filterMetaClass, query,
                    propertiesFilterPredicate);
            Map<String, String> properties = new TreeMap<>();
            for (MetaPropertyPath mpp : paths) {
                String property = mpp.toPathString();
                String caption = propertyFilterSupport.getPropertyFilterCaption(filterMetaClass, property);
                properties.put(caption, property);
            }
            propertyField.setOptionsMap(properties);
        }
    }

    @SuppressWarnings("unchecked")
    protected void initOperationField() {
        List<PropertyFilter.Operation> operations;
        if (filterMetaClass != null && getEditedEntity().getProperty() != null) {
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
                MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass,
                        getEditedEntity().getProperty());
                if (mpp != null) {
                    Object defaultValue = propertyFilterSupport.parseDefaultValue(mpp.getMetaProperty(),
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

            EnumSet<PropertyFilter.Operation> availableOperations = propertyFilterSupport
                    .getAvailableOperations(filterMetaClass, property);
            operationField.setOptionsList(new ArrayList<>(availableOperations));
            if (operationField.getValue() != null && !availableOperations.contains(operationField.getValue())) {
                operationField.setValue(null);
            }

            resetDefaultValue();
            initDefaultValueField();
        }
    }

    @Subscribe("operationField")
    protected void onOperationFieldValueChange(HasValue.ValueChangeEvent<PropertyFilter.Operation> event) {
        PropertyFilter.Operation operation = event.getValue();
        if (operation != null && event.isUserOriginated()) {
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

    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (defaultValueField != null
                && getEditedEntity().getProperty() != null
                && getEditedEntity().getOperation() != null) {
            MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass,
                    getEditedEntity().getProperty());
            String modelDefaultValue = null;
            if (mpp != null) {
                modelDefaultValue = propertyFilterSupport.formatDefaultValue(mpp.getMetaProperty(),
                        getEditedEntity().getOperation().getType(), defaultValueField.getValue());
            }

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            valueComponent.setDefaultValue(modelDefaultValue);
            valueComponent.setComponentName(singleFilterSupport.getValueComponentName(defaultValueField));

            String caption = getEditedEntity().getCaption();
            String localizedCaption;
            if (!Strings.isNullOrEmpty(caption)) {
                localizedCaption = caption;
            } else {
                localizedCaption = propertyFilterSupport.getPropertyFilterCaption(filterMetaClass,
                        getEditedEntity().getProperty(), getEditedEntity().getOperation(),
                        getEditedEntity().getOperationCaptionVisible()
                                && !getEditedEntity().getOperationEditable());
            }
            getEditedEntity().setLocalizedCaption(localizedCaption);
        }
    }
}
