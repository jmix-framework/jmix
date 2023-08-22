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

package io.jmix.searchflowui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.AbstractFilterComponentConverter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchflowui.component.FullTextFilter;
import io.jmix.searchflowui.entity.FullTextFilterCondition;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import java.util.Objects;

@Internal
@org.springframework.stereotype.Component("search_FullTextFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FullTextFilterConverter extends AbstractFilterComponentConverter<FullTextFilter, FullTextFilterCondition> {
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SearchStrategyManager searchStrategyManager;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;

    public FullTextFilterConverter(GenericFilter filter) {
        super(filter);
    }

    @Override
    protected FullTextFilter createComponent() {
        return uiComponents.create(FullTextFilter.class);
    }

    @Override
    protected FullTextFilterCondition createModel() {
        return metadata.create(FullTextFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelLabel(FullTextFilter component) {
        return component.getLabel();
    }

    public FullTextFilter convertToComponent(FullTextFilterCondition model) {
        FullTextFilter fullTextFilter = super.convertToComponent(model);
        fullTextFilter.setLabel(model.getLabel());
        fullTextFilter.setRequired(model.getRequired());
        fullTextFilter.setParameterName(model.getParameterName());

        String searchStrategyName = model.getSearchStrategyName();
        SearchStrategy searchStrategy = !Strings.isNullOrEmpty(searchStrategyName) ?
                searchStrategyManager.findSearchStrategyByName(searchStrategyName) :
                null;
        fullTextFilter.setSearchStrategy(searchStrategy);

        fullTextFilter.setValueComponent(convertValueComponentToComponent(model));
        if (model.getValueComponent() != null) {
            fullTextFilter.setValue(model.getValueComponent().getDefaultValue());
        }

        return fullTextFilter;
    }

    @Override
    public FullTextFilterCondition convertToModel(FullTextFilter fullTextFilter) {
        FullTextFilterCondition condition = super.convertToModel(fullTextFilter);
        condition.setLabel(fullTextFilter.getLabel());
        condition.setLocalizedLabel(getLocalizedModelLabel(fullTextFilter));
        condition.setRequired(fullTextFilter.isRequired());
        condition.setParameterName(fullTextFilter.getParameterName());

        if (fullTextFilter.getSearchStrategy() != null) {
            condition.setSearchStrategyName(fullTextFilter.getSearchStrategy().getName());
        }

        condition.setValueComponent(convertValueComponentToModel(fullTextFilter));

        return condition;
    }

    protected HasValueAndElement<?, String> convertValueComponentToComponent(FullTextFilterCondition model) {
        HasValueAndElement<?, String> valueComponent = uiComponents.create(TypedTextField.class);
        FilterValueComponent filterValueComponent = model.getValueComponent();
        if (filterValueComponent != null) {

            String componentName = filterValueComponent.getComponentName();
            if (componentName != null) {
                String defaultName = singleFilterSupport.getValueComponentName(valueComponent);
                if (!Objects.equals(defaultName, componentName)) {
                    valueComponent = createValueComponent(componentName);
                }
            }

            ((Component) valueComponent).setId(com.google.common.base.Strings.nullToEmpty(filterValueComponent.getComponentId()));
            if (valueComponent instanceof HasStyle) {
                ((HasStyle) valueComponent).setClassName(filterValueComponent.getStyleName());
            }

        }

        return valueComponent;
    }

    protected HasValueAndElement<?, String> createValueComponent(String componentName) {
        HasValueAndElement<?, String> valueComponent;
        Class componentType = singleFilterSupport.getValueComponentType(componentName);
        Component generatedComponent = uiComponents.create(componentType);
        if (!(generatedComponent instanceof HasValueAndElement)) {
            throw new IllegalStateException("Generated component doesn't implement "
                    + HasValueAndElement.class.getSimpleName());
        }
        valueComponent = ((HasValueAndElement<?, String>) generatedComponent);
        return valueComponent;
    }

    protected FilterValueComponent convertValueComponentToModel(FullTextFilter component) {
        HasValueAndElement<?, ?> valueField = component.getValueComponent();

        FilterValueComponent valueComponent = metadata.create(FilterValueComponent.class);
        valueComponent.setComponentId(((Component) valueField).getId().orElse(null));
        if (valueField instanceof HasStyle) {
            valueComponent.setStyleName(((HasStyle) valueField).getClassName());
        }
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(valueField));

        valueComponent.setDefaultValue(component.getValue());

        return valueComponent;
    }
}
