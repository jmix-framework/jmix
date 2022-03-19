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

package io.jmix.ui.component.jpqlfilter;

import io.jmix.core.ClassManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.filter.converter.AbstractFilterComponentConverter;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.JpqlFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("rawtypes")
@Internal
@Component("ui_JpqlFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpqlFilterConverter extends AbstractFilterComponentConverter<JpqlFilter, JpqlFilterCondition> {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected JpqlFilterSupport jpqlFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;

    protected JpqlFilterConverter(Filter filter) {
        super(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JpqlFilter<?> convertToComponent(JpqlFilterCondition model) {
        JpqlFilter jpqlFilter = super.convertToComponent(model);
        jpqlFilter.setCaption(model.getCaption());
        jpqlFilter.setCaptionPosition(model.getCaptionPosition());
        jpqlFilter.setRequired(model.getRequired());
        jpqlFilter.setParameterName(model.getParameterName());
        if (model.getParameterClass() != null) {
            jpqlFilter.setParameterClass(classManager.loadClass(model.getParameterClass()));
        }
        jpqlFilter.setCondition(model.getWhere(), model.getJoin());
        jpqlFilter.setHasInExpression(model.getHasInExpression());

        HasValue valueComponent = convertValueComponentToComponent(jpqlFilter, model);
        Object defaultValue = convertDefaultValueToComponent(jpqlFilter, model);
        jpqlFilter.setValueComponent(valueComponent);
        jpqlFilter.setValue(defaultValue);

        return jpqlFilter;
    }

    @Override
    public JpqlFilterCondition convertToModel(JpqlFilter jpqlFilter) {
        JpqlFilterCondition condition = super.convertToModel(jpqlFilter);
        condition.setCaption(jpqlFilter.getCaption());
        condition.setLocalizedCaption(getLocalizedModelCaption(jpqlFilter));
        condition.setCaptionPosition(jpqlFilter.getCaptionPosition());
        condition.setRequired(jpqlFilter.isRequired());
        condition.setParameterName(jpqlFilter.getParameterName());
        condition.setParameterClass(jpqlFilter.getParameterClass().getName());
        condition.setWhere(jpqlFilter.getWhere());
        condition.setJoin(jpqlFilter.getJoin());
        condition.setHasInExpression(jpqlFilter.hasInExpression());

        FilterValueComponent valueComponent = convertValueComponentToModel(jpqlFilter);
        String modelDefaultValue = convertDefaultValueToModel(jpqlFilter);
        valueComponent.setDefaultValue(modelDefaultValue);
        condition.setValueComponent(valueComponent);

        return condition;
    }

    @Override
    protected JpqlFilter createComponent() {
        return uiComponents.create(JpqlFilter.NAME);
    }

    @Override
    protected JpqlFilterCondition createModel() {
        return metadata.create(JpqlFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelCaption(JpqlFilter component) {
        return component.getCaption();
    }

    protected HasValue generateValueComponent(JpqlFilter component, JpqlFilterCondition model) {
        MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        return singleFilterSupport.generateValueComponent(metaClass, model.getHasInExpression(),
                component.getParameterClass());
    }

    protected HasValue convertValueComponentToComponent(JpqlFilter component, JpqlFilterCondition model) {
        HasValue valueComponent = generateValueComponent(component, model);
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
    protected Object convertDefaultValueToComponent(JpqlFilter component, JpqlFilterCondition model) {
        String defaultValueModel = model.getValueComponent().getDefaultValue();
        return jpqlFilterSupport.parseDefaultValue(component.getParameterClass(), model.getHasInExpression(),
                defaultValueModel);
    }

    protected FilterValueComponent convertValueComponentToModel(JpqlFilter component) {
        HasValue<?> valueField = component.getValueComponent();

        FilterValueComponent valueComponent = metadata.create(FilterValueComponent.class);
        valueComponent.setComponentId(valueField.getId());
        valueComponent.setStyleName(valueField.getStyleName());
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(valueField));

        return valueComponent;
    }

    @Nullable
    protected String convertDefaultValueToModel(JpqlFilter component) {
        Object defaultValue = component.getValue();
        return jpqlFilterSupport.formatDefaultValue(component.getParameterClass(), component.hasInExpression(),
                defaultValue);
    }
}
