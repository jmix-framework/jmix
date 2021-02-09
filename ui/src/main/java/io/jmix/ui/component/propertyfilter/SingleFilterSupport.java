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

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.UiComponentsGenerator;
import io.jmix.ui.component.factory.JpqlFilterComponentGenerationContext;
import io.jmix.ui.component.factory.PropertyFilterComponentGenerationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Internal
@Component("ui_SingleFilterSupport")
public class SingleFilterSupport {

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    /**
     * Generates filter value component by given metaClass, entity property and operation.
     * In general case the value component is created for {@link PropertyFilter}.
     *
     * @param metaClass an entity meta class associated with filter
     * @param property  an entity attribute associated with filter
     * @param operation an operation
     * @return a filter value component
     */
    public HasValue generateValueComponent(MetaClass metaClass,
                                           String property,
                                           PropertyFilter.Operation operation) {
        ComponentGenerationContext context =
                new PropertyFilterComponentGenerationContext(metaClass, property, operation);
        context.setTargetClass(PropertyFilter.class);

        return ((HasValue) uiComponentsGenerator.generate(context));
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
    public HasValue generateValueComponent(MetaClass metaClass,
                                           boolean hasInExpression,
                                           @Nullable Class parameterClass) {
        ComponentGenerationContext context =
                new JpqlFilterComponentGenerationContext(metaClass, "", hasInExpression, parameterClass);
        context.setTargetClass(JpqlFilter.class);

        return ((HasValue) uiComponentsGenerator.generate(context));
    }

    /**
     * @return a value component name
     */
    public String getValueComponentName(HasValue<?> valueComponent) {
        try {
            return (String) valueComponent.getClass().getField("NAME").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("Class '%s' doesn't have NAME field",
                    valueComponent.getClass().getName()));
        }
    }
}
