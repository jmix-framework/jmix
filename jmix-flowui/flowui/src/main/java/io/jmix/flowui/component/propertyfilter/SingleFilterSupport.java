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

import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.factory.JpqlFilterComponentGenerationContext;
import io.jmix.flowui.component.factory.PropertyFilterComponentGenerationContext;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

@Internal
@Component("flowui_SingleFilterSupport")
public class SingleFilterSupport {

    protected UiComponentsGenerator uiComponentsGenerator;

    public SingleFilterSupport(UiComponentsGenerator uiComponentsGenerator) {
        this.uiComponentsGenerator = uiComponentsGenerator;
    }

    /**
     * Generates filter value component by given metaClass, entity property and operation.
     * In general case the value component is created for {@link PropertyFilter}.
     *
     * @param metaClass an entity meta class associated with filter
     * @param property  an entity attribute associated with filter
     * @param operation an operation
     * @return a filter value component
     */
    @SuppressWarnings("rawtypes")
    public HasValueAndElement generateValueComponent(MetaClass metaClass,
                                                     String property,
                                                     PropertyFilter.Operation operation) {
        ComponentGenerationContext context =
                new PropertyFilterComponentGenerationContext(metaClass, property, operation);
        context.setTargetClass(PropertyFilter.class);

        return ((HasValueAndElement) uiComponentsGenerator.generate(context));
    }

    /**
     * Generates filter value component by given metaClass and value type.
     * In general case the value component is created for {@link JpqlFilter}.
     *
     * @param metaClass       an entity meta class associated with filter
     * @param hasInExpression whether the query condition has an IN expression and the value is a collection
     * @param parameterClass  a value type
     * @return a filter value component
     */
    @SuppressWarnings({"rawtypes"})
    public HasValueAndElement generateValueComponent(MetaClass metaClass,
                                                     boolean hasInExpression,
                                                     @Nullable Class<?> parameterClass) {
        ComponentGenerationContext context =
                new JpqlFilterComponentGenerationContext(metaClass, "", hasInExpression, parameterClass);
        context.setTargetClass(JpqlFilter.class);

        return ((HasValueAndElement) uiComponentsGenerator.generate(context));
    }

    /**
     * @return a value component name
     */
    public String getValueComponentName(HasValueAndElement<?, ?> valueComponent) {
        // TODO: gg, something better?
        return valueComponent.getClass().getName();
    }

    public Class<?> getValueComponentType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("Cannot get class for '%s'", name), e);
        }
    }
}
