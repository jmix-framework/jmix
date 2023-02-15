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

package io.jmix.flowui.component.factory;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.ComponentGenerationContext;

import javax.annotation.Nullable;

public class JpqlFilterComponentGenerationContext extends ComponentGenerationContext {

    protected final Class<?> parameterClass;
    protected final boolean hasInExpression;

    /**
     * Creates an instance of ComponentGenerationContext.
     *
     * @param metaClass       the entity for which the component is created
     * @param property        the entity attribute for which the component is created
     * @param hasInExpression whether the query condition has an IN expression and the value is a collection
     * @param parameterClass  the component value type
     */
    public JpqlFilterComponentGenerationContext(MetaClass metaClass,
                                                String property, boolean hasInExpression,
                                                @Nullable Class<?> parameterClass) {
        super(metaClass, property);

        this.hasInExpression = hasInExpression;
        this.parameterClass = parameterClass != null
                ? parameterClass
                : String.class;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public boolean hasInExpression() {
        return hasInExpression;
    }
}
