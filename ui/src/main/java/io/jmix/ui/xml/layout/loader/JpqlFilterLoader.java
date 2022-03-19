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

package io.jmix.ui.xml.layout.loader;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import java.util.List;

public class JpqlFilterLoader extends AbstractSingleFilterComponentLoader<JpqlFilter<?>> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(JpqlFilter.NAME);
        loadId(resultComponent, element);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();

        loadString(element, "parameterClass", parameterClass ->
                resultComponent.setParameterClass(getClassManager().loadClass(parameterClass)));

        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(getJpqlFilterSupport().generateParameterName(resultComponent.getId(),
                        resultComponent.getParameterClass().getSimpleName())));

        loadCondition(resultComponent, element);
        loadBoolean(element, "hasInExpression", resultComponent::setHasInExpression);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadDefaultValue(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Component createValueComponent(List<Element> elements) {
        return elements.stream()
                .filter(valueComponentElement -> !"condition".equals(valueComponentElement.getName()))
                .map(valueComponentElement -> {
                    ComponentLoader valueComponentLoader = getLayoutLoader().createComponent(valueComponentElement);
                    valueComponentLoader.loadComponent();
                    return valueComponentLoader.getResultComponent();
                })
                .findFirst()
                .orElseGet(this::generateValueComponent);
    }

    @Override
    protected HasValue<?> generateValueComponent() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
        return getSingleFilterSupport().generateValueComponent(metaClass, resultComponent.hasInExpression(),
                resultComponent.getParameterClass());
    }

    protected void loadCondition(JpqlFilter<?> component, Element element) {
        List<Element> conditions = element.elements("condition");
        if (conditions.size() != 1) {
            throw new GuiDevelopmentException("JPQL filter element must have exactly one 'condition' nested element",
                    context);
        }

        Element jpqlElement = conditions.get(0).element("jpql");
        if (jpqlElement == null) {
            throw new GuiDevelopmentException("'condition' element must have exactly one 'jpql' nested element",
                    context);
        }

        Element whereElement = jpqlElement.element("where");
        if (whereElement == null) {
            throw new GuiDevelopmentException("'jpql' element must have exactly one 'where' nested element",
                    context);
        }

        Element joinElement = jpqlElement.element("join");

        component.setCondition(whereElement.getText(),
                joinElement != null ? joinElement.getText() : null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadDefaultValue(JpqlFilter component, Element element) {
        if (element.attribute("defaultValue") != null) {
            String defaultValue = element.attributeValue("defaultValue");
            Class parameterClass = component.getParameterClass();
            Object value = getJpqlFilterSupport().parseDefaultValue(parameterClass, component.hasInExpression(),
                    defaultValue);
            component.setValue(value);
        }
    }

    protected JpqlFilterSupport getJpqlFilterSupport() {
        return applicationContext.getBean(JpqlFilterSupport.class);
    }
}
