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

package io.jmix.flowui.component.jpqlfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.jmix.core.ClassManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.AbstractFilterComponentConverter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.JpqlFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import java.util.Objects;

@Internal
@SpringComponent("flowui_JpqlFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpqlFilterConverter extends AbstractFilterComponentConverter<JpqlFilter, JpqlFilterCondition> {

    protected UiComponents uiComponents;
    protected Metadata metadata;
    protected ClassManager classManager;
    protected JpqlFilterSupport jpqlFilterSupport;
    protected SingleFilterSupport singleFilterSupport;

    protected JpqlFilterConverter(GenericFilter filter) {
        super(filter);
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
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Autowired
    public void setJpqlFilterSupport(JpqlFilterSupport jpqlFilterSupport) {
        this.jpqlFilterSupport = jpqlFilterSupport;
    }

    @Autowired
    public void setSingleFilterSupport(SingleFilterSupport singleFilterSupport) {
        this.singleFilterSupport = singleFilterSupport;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JpqlFilter convertToComponent(JpqlFilterCondition model) {
        JpqlFilter jpqlFilter = super.convertToComponent(model);
        jpqlFilter.setLabel(model.getLabel());
        jpqlFilter.setRequired(model.getRequired());
        jpqlFilter.setParameterName(model.getParameterName());
        if (model.getParameterClass() != null) {
            jpqlFilter.setParameterClass(classManager.loadClass(model.getParameterClass()));
        }
        jpqlFilter.setCondition(model.getWhere(), model.getJoin());
        jpqlFilter.setHasInExpression(model.getHasInExpression());

        HasValueAndElement<?, ?> valueComponent = convertValueComponentToComponent(jpqlFilter, model);
        Object defaultValue = convertDefaultValueToComponent(jpqlFilter, model);
        jpqlFilter.setValueComponent(valueComponent);
        jpqlFilter.setValue(defaultValue);

        return jpqlFilter;
    }

    @Override
    public JpqlFilterCondition convertToModel(JpqlFilter jpqlFilter) {
        JpqlFilterCondition condition = super.convertToModel(jpqlFilter);
        condition.setLabel(jpqlFilter.getLabel());
        condition.setLocalizedLabel(getLocalizedModelLabel(jpqlFilter));
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
        return uiComponents.create(JpqlFilter.class);
    }

    @Override
    protected JpqlFilterCondition createModel() {
        return metadata.create(JpqlFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelLabel(JpqlFilter component) {
        return component.getLabel();
    }

    protected HasValueAndElement<?, ?> generateValueComponent(JpqlFilter component, JpqlFilterCondition model) {
        MetaClass metaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        return singleFilterSupport.generateValueComponent(metaClass, model.getHasInExpression(),
                component.getParameterClass());
    }

    protected HasValueAndElement<?, ?> convertValueComponentToComponent(JpqlFilter component, JpqlFilterCondition model) {
        HasValueAndElement<?, ?> valueComponent = generateValueComponent(component, model);
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
    protected Object convertDefaultValueToComponent(JpqlFilter component, JpqlFilterCondition model) {
        String defaultValueModel = model.getValueComponent().getDefaultValue();
        return jpqlFilterSupport.parseDefaultValue(component.getParameterClass(), model.getHasInExpression(),
                defaultValueModel);
    }

    protected FilterValueComponent convertValueComponentToModel(JpqlFilter component) {
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
    protected String convertDefaultValueToModel(JpqlFilter component) {
        Object defaultValue = component.getValue();
        return jpqlFilterSupport.formatDefaultValue(component.getParameterClass(), component.hasInExpression(),
                defaultValue);
    }
}
