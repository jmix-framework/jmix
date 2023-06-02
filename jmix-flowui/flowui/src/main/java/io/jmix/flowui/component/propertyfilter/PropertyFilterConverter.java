/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.propertyfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.AbstractFilterComponentConverter;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import org.springframework.lang.Nullable;
import java.util.Objects;

@Internal
@SpringComponent("flowui_PropertyFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertyFilterConverter
        extends AbstractFilterComponentConverter<PropertyFilter, PropertyFilterCondition> {

    protected PropertyFilterSupport propertyFilterSupport;
    protected SingleFilterSupport singleFilterSupport;
    protected UiComponents uiComponents;
    protected Metadata metadata;
    protected MetadataTools metadataTools;

    public PropertyFilterConverter(GenericFilter filter) {
        super(filter);
    }

    @Autowired
    public void setPropertyFilterSupport(PropertyFilterSupport propertyFilterSupport) {
        this.propertyFilterSupport = propertyFilterSupport;
    }

    @Autowired
    public void setSingleFilterSupport(SingleFilterSupport singleFilterSupport) {
        this.singleFilterSupport = singleFilterSupport;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PropertyFilter convertToComponent(PropertyFilterCondition model) {
        PropertyFilter propertyFilter = super.convertToComponent(model);
        propertyFilter.setLabel(model.getLabel());
        propertyFilter.setRequired(model.getRequired());
        propertyFilter.setProperty(model.getProperty());
        propertyFilter.setOperation(model.getOperation());
        propertyFilter.setOperationEditable(model.getOperationEditable());
        propertyFilter.setOperationTextVisible(model.getOperationTextVisible());
        propertyFilter.setParameterName(model.getParameterName());

        HasValueAndElement<?, ?> valueComponent = convertValueComponentToComponent(model);
        propertyFilter.setValueComponent(valueComponent);
        Object defaultValue = convertDefaultValueToComponent(model);
        propertyFilter.setValue(defaultValue);

        return propertyFilter;
    }

    @Override
    public PropertyFilterCondition convertToModel(PropertyFilter propertyFilter) {
        PropertyFilterCondition condition = super.convertToModel(propertyFilter);
        condition.setLabel(propertyFilter.getLabel());
        condition.setLocalizedLabel(getLocalizedModelLabel(propertyFilter));
        condition.setRequired(propertyFilter.isRequired());
        condition.setProperty(propertyFilter.getProperty());
        condition.setOperation(propertyFilter.getOperation());
        condition.setOperationEditable(propertyFilter.isOperationEditable());
        condition.setOperationTextVisible(propertyFilter.isOperationTextVisible());
        condition.setParameterName(propertyFilter.getParameterName());

        FilterValueComponent valueComponent = convertValueComponentToModel(propertyFilter);
        String modelDefaultValue = convertDefaultValueToModel(propertyFilter);
        valueComponent.setDefaultValue(modelDefaultValue);
        condition.setValueComponent(valueComponent);

        return condition;
    }

    @Override
    protected PropertyFilter createComponent() {
        return uiComponents.create(PropertyFilter.class);
    }

    @Override
    protected PropertyFilterCondition createModel() {
        return metadata.create(PropertyFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelLabel(PropertyFilter component) {
        String caption = component.getLabel();
        if (Strings.isNullOrEmpty(caption)) {
            MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
            return propertyFilterSupport.getPropertyFilterCaption(metaClass,
                    component.getProperty(), component.getOperation(),
                    component.isOperationTextVisible() && !component.isOperationEditable());
        } else {
            return caption;
        }
    }

    protected HasValueAndElement<?, ?> generateValueComponent(PropertyFilterCondition model) {
        MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        return singleFilterSupport.generateValueComponent(metaClass, model.getProperty(), model.getOperation());
    }

    protected HasValueAndElement<?, ?> convertValueComponentToComponent(PropertyFilterCondition model) {
        HasValueAndElement<?, ?> valueComponent = generateValueComponent(model);
        FilterValueComponent filterValueComponent = model.getValueComponent();
        if (filterValueComponent != null) {
            String componentName = filterValueComponent.getComponentName();
            if (componentName != null) {
                String defaultName = singleFilterSupport.getValueComponentName(valueComponent);
                if (!Objects.equals(defaultName, componentName)) {
                    valueComponent = createValueComponent(componentName);
                }
            }

            ((Component) valueComponent).setId(Strings.nullToEmpty(filterValueComponent.getComponentId()));
            if (valueComponent instanceof HasStyle) {
                ((HasStyle) valueComponent).setClassName(filterValueComponent.getStyleName());
            }
        }

        return valueComponent;
    }

    protected HasValueAndElement<?, ?> createValueComponent(String componentName) {
        HasValueAndElement<?, ?> valueComponent;
        Class componentType = singleFilterSupport.getValueComponentType(componentName);
        Component generatedComponent = uiComponents.create(componentType);
        if (!(generatedComponent instanceof HasValueAndElement)) {
            throw new IllegalStateException("Generated component doesn't implement "
                    + HasValueAndElement.class.getSimpleName());
        }
        valueComponent = ((HasValueAndElement<?, ?>) generatedComponent);
        return valueComponent;
    }

    @Nullable
    protected Object convertDefaultValueToComponent(PropertyFilterCondition model) {
        String modelDefaultValue = model.getValueComponent().getDefaultValue();
        Object value = null;
        if (model.getProperty() != null && model.getOperation() != null) {
            MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
            MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(metaClass, model.getProperty());
            if (mpp != null) {
                value = propertyFilterSupport.parseDefaultValue(mpp.getMetaProperty(),
                        model.getOperation().getType(), modelDefaultValue);
            }
        }

        return value;
    }

    protected FilterValueComponent convertValueComponentToModel(PropertyFilter component) {
        HasValueAndElement<?, ?> valueField = component.getValueComponent();

        FilterValueComponent valueComponent = metadata.create(FilterValueComponent.class);
        valueComponent.setComponentId(((Component) valueField).getId().orElse(null));
        if (valueField instanceof HasStyle) {
            valueComponent.setStyleName(((HasStyle) valueField).getClassName());
        }
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(valueField));

        return valueComponent;
    }

    @Nullable
    protected String convertDefaultValueToModel(PropertyFilter component) {
        Object defaultValue = component.getValue();
        MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(metaClass, component.getProperty());
        String modelDefaultValue = null;
        if (mpp != null) {
            modelDefaultValue = propertyFilterSupport.formatDefaultValue(mpp.getMetaProperty(),
                    component.getOperation().getType(), defaultValue);
        }

        return modelDefaultValue;
    }
}
