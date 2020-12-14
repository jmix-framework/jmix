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
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.entity.PropertyFilterValueComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Internal
@Component("ui_PropertyFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertyFilterConverter implements FilterConverter<PropertyFilter, PropertyFilterCondition> {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected Metadata metadata;

    protected final Filter filter;

    public PropertyFilterConverter(Filter filter) {
        this.filter = filter;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public PropertyFilter<?> convertToComponent(PropertyFilterCondition model) {
        PropertyFilter propertyFilter = uiComponents.create(PropertyFilter.NAME);
        propertyFilter.setAutoApply(filter.isAutoApply());
        propertyFilter.setDataLoader(filter.getDataLoader());

        propertyFilter.setVisible(model.getVisible());
        propertyFilter.setEnabled(model.getEnabled());
        propertyFilter.setProperty(model.getProperty());
        propertyFilter.setCaption(model.getCaption());
        propertyFilter.setOperation(model.getOperation());
        propertyFilter.setOperationEditable(model.getOperationEditable());
        propertyFilter.setParameterName(model.getParameterName());
        propertyFilter.setCaptionPosition(model.getCaptionPosition());
        propertyFilter.setId(model.getComponentId());
        propertyFilter.setStyleName(model.getStyleName());
        propertyFilter.setRequired(model.getRequired());

        MetaClass componentMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        HasValue<?> valueComponent = propertyFilterSupport.generateValueComponent(componentMetaClass,
                model.getProperty(), model.getOperation(), propertyFilter.getId());

        PropertyFilterValueComponent propertyFilterValueComponent = model.getValueComponent();
        if (propertyFilterValueComponent != null) {
            String componentName = propertyFilterValueComponent.getComponentName();
            if (componentName != null) {
                String defaultName = propertyFilterSupport.getValueComponentName(valueComponent);
                if (!Objects.equals(defaultName, componentName)) {
                    valueComponent = uiComponents.create(componentName);
                }
            }

            valueComponent.setId(propertyFilterValueComponent.getComponentId());
            valueComponent.setStyleName(propertyFilterValueComponent.getStyleName());
        }

        propertyFilter.setValueComponent(valueComponent);
        propertyFilter.setValue(model.getParameterValue());

        propertyFilter.setWidthFull();

        propertyFilter.addValueChangeListener(valueChangeEvent ->
                model.setParameterValue(((HasValue.ValueChangeEvent<?>) valueChangeEvent).getValue()));

        propertyFilter.addOperationChangeListener(operationChangeEvent -> model
                .setOperation(((PropertyFilter.OperationChangeEvent) operationChangeEvent).getNewOperation()));

        return propertyFilter;
    }

    @Override
    public PropertyFilterCondition convertToModel(PropertyFilter propertyFilter) {
        PropertyFilterCondition condition = metadata.create(PropertyFilterCondition.class);
        condition.setVisible(propertyFilter.isVisible());
        condition.setEnabled(propertyFilter.isEnabled());
        condition.setProperty(propertyFilter.getProperty());
        condition.setCaption(propertyFilter.getCaption());
        condition.setOperation(propertyFilter.getOperation());
        condition.setOperationEditable(propertyFilter.isOperationEditable());
        condition.setParameterName(propertyFilter.getParameterName());
        condition.setCaptionPosition(propertyFilter.getCaptionPosition());
        condition.setComponentId(propertyFilter.getId());
        condition.setStyleName(propertyFilter.getStyleName());
        condition.setRequired(propertyFilter.isRequired());

        HasValue<?> valueField = propertyFilter.getValueComponent();
        PropertyFilterValueComponent valueComponent = metadata.create(PropertyFilterValueComponent.class);
        valueComponent.setComponentId(valueField.getId());
        valueComponent.setStyleName(valueField.getStyleName());
        valueComponent.setComponentName(propertyFilterSupport.getValueComponentName(valueField));
        condition.setValueComponent(valueComponent);

        return condition;
    }
}
