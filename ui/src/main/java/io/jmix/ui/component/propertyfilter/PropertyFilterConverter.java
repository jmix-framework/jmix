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

package io.jmix.ui.component.propertyfilter;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.filter.converter.AbstractFilterComponentConverter;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.PropertyFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("rawtypes")
@Internal
@Component("ui_PropertyFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertyFilterConverter
        extends AbstractFilterComponentConverter<PropertyFilter, PropertyFilterCondition> {

    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;

    protected PropertyFilterConverter(Filter filter) {
        super(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PropertyFilter<?> convertToComponent(PropertyFilterCondition model) {
        PropertyFilter propertyFilter = super.convertToComponent(model);
        propertyFilter.setCaption(model.getCaption());
        propertyFilter.setCaptionPosition(model.getCaptionPosition());
        propertyFilter.setRequired(model.getRequired());
        propertyFilter.setProperty(model.getProperty());
        propertyFilter.setOperation(model.getOperation());
        propertyFilter.setOperationEditable(model.getOperationEditable());
        propertyFilter.setParameterName(model.getParameterName());

        HasValue valueComponent = convertValueComponentToComponent(model);
        propertyFilter.setValueComponent(valueComponent);
        Object defaultValue = convertDefaultValueToComponent(model);
        propertyFilter.setValue(defaultValue);

        return propertyFilter;
    }

    @Override
    public PropertyFilterCondition convertToModel(PropertyFilter propertyFilter) {
        PropertyFilterCondition condition = super.convertToModel(propertyFilter);
        condition.setCaption(propertyFilter.getCaption());
        condition.setCaptionPosition(propertyFilter.getCaptionPosition());
        condition.setRequired(propertyFilter.isRequired());
        condition.setProperty(propertyFilter.getProperty());
        condition.setOperation(propertyFilter.getOperation());
        condition.setOperationEditable(propertyFilter.isOperationEditable());
        condition.setParameterName(propertyFilter.getParameterName());

        FilterValueComponent valueComponent = convertValueComponentToModel(propertyFilter);
        String modelDefaultValue = convertDefaultValueToModel(propertyFilter);
        valueComponent.setDefaultValue(modelDefaultValue);
        condition.setValueComponent(valueComponent);

        return condition;
    }

    @Override
    protected PropertyFilter createComponent() {
        return uiComponents.create(PropertyFilter.NAME);
    }

    @Override
    protected PropertyFilterCondition createModel() {
        return metadata.create(PropertyFilterCondition.class);
    }

    protected HasValue generateValueComponent(PropertyFilterCondition model) {
        return singleFilterSupport.generateValueComponent(metadata.getClass(model.getMetaClass()),
                model.getProperty(), model.getOperation());
    }

    protected HasValue convertValueComponentToComponent(PropertyFilterCondition model) {
        HasValue valueComponent = generateValueComponent(model);
        FilterValueComponent filterValueComponent = model.getValueComponent();
        if (filterValueComponent != null) {
            String componentName = filterValueComponent.getComponentName();
            if (componentName != null) {
                String defaultName = singleFilterSupport.getValueComponentName(valueComponent);
                if (!Objects.equals(defaultName, componentName)) {
                    valueComponent = uiComponents.create(componentName);
                }
            }

            valueComponent.setId(filterValueComponent.getComponentId());
            valueComponent.setStyleName(filterValueComponent.getStyleName());
        }

        return valueComponent;
    }

    @Nullable
    protected Object convertDefaultValueToComponent(PropertyFilterCondition model) {
        String modelDefaultValue = model.getValueComponent().getDefaultValue();
        Object value = null;
        if (model.getProperty() != null) {
            MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
            MetaProperty metaProperty = metaClass.findProperty(model.getProperty());
            if (metaProperty != null) {
                value = propertyFilterSupport.parseDefaultValue(metaProperty, modelDefaultValue);
            }
        }

        return value;
    }

    protected FilterValueComponent convertValueComponentToModel(PropertyFilter component) {
        HasValue<?> valueField = component.getValueComponent();

        FilterValueComponent valueComponent = metadata.create(FilterValueComponent.class);
        valueComponent.setComponentId(valueField.getId());
        valueComponent.setStyleName(valueField.getStyleName());
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(valueField));

        return valueComponent;
    }

    @Nullable
    protected String convertDefaultValueToModel(PropertyFilter component) {
        Object defaultValue = component.getValue();
        MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        MetaProperty metaProperty = metaClass.findProperty(component.getProperty());

        String modelDefaultValue = null;
        if (metaProperty != null) {
            modelDefaultValue = propertyFilterSupport.formatDefaultValue(metaProperty, defaultValue);
        }

        return modelDefaultValue;
    }
}
