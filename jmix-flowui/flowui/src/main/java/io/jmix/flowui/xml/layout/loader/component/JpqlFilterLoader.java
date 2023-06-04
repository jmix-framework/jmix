/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.core.ClassManager;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.flowui.exception.GuiDevelopmentException;
import org.dom4j.Element;

import java.util.List;

public class JpqlFilterLoader extends AbstractSingleFilterComponentLoader<JpqlFilter<?>> {

    protected ClassManager classManager;
    protected JpqlFilterSupport jpqlFilterSupport;

    @Override
    protected JpqlFilter<?> createComponent() {
        return factory.create(JpqlFilter.class);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();

        loadString(element, "parameterClass", parameterClass ->
                resultComponent.setParameterClass(getClassManager().loadClass(parameterClass)));

        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(getDefaultParameterName()));

        loadCondition(resultComponent, element);
        loadBoolean(element, "hasInExpression", resultComponent::setHasInExpression);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadDefaultValue(resultComponent, element);
    }

    @Override
    protected Component generateValueComponent() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();

        return (Component) getSingleFilterSupport().generateValueComponent(
                metaClass,
                resultComponent.hasInExpression(),
                resultComponent.getParameterClass()
        );
    }

    protected void loadCondition(JpqlFilter<?> resultComponent, Element element) {
        List<Element> conditions = element.elements("condition");
        if (conditions.size() != 1) {
            throw new GuiDevelopmentException(
                    String.format("%s element must have exactly one 'condition' nested element",
                            resultComponent.getClass().getSimpleName()),
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

        resultComponent.setCondition(whereElement.getText(),
                joinElement != null ? joinElement.getText() : null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadDefaultValue(JpqlFilter resultComponent, Element element) {
        loadString(element, "defaultValue", defaultValue -> {
            Class parameterClass = resultComponent.getParameterClass();

            Object value = getJpqlFilterSupport().parseDefaultValue(parameterClass, resultComponent.hasInExpression(),
                    defaultValue);

            resultComponent.setValue(value);
        });
    }

    @Override
    protected Element getValueComponentElement(List<Element> elements) {
        if (elements.size() > 3) {
            throw new GuiDevelopmentException("Only one value component can be defined", context);
        }

        return elements.stream()
                .filter(this::isValueComponent)
                .findAny()
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("Unknown value component for %s", resultComponent.getClass().getSimpleName()),
                        context)
                );
    }

    @Override
    protected boolean isValueComponent(Element subElement) {
        return !"tooltip".equals(subElement.getName()) && !"condition".equals(subElement.getName());
    }

    protected String getDefaultParameterName() {
        return getJpqlFilterSupport().generateParameterName(
                resultComponent.getId().orElse(""),
                resultComponent.getParameterClass().getSimpleName()
        );
    }

    protected ClassManager getClassManager() {
        if (classManager == null) {
            classManager = applicationContext.getBean(ClassManager.class);
        }

        return classManager;
    }

    protected JpqlFilterSupport getJpqlFilterSupport() {
        if (jpqlFilterSupport == null) {
            jpqlFilterSupport = applicationContext.getBean(JpqlFilterSupport.class);
        }

        return jpqlFilterSupport;
    }
}
