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

package io.jmix.ui.component.factory;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.PropertyFilter.Operation;

public class PropertyFilterComponentGenerationContext extends ComponentGenerationContext {

    protected final Operation operation;

    /**
     * Creates an instance of PropertyFilterComponentGenerationContext.
     *
     * @param metaClass the entity for which the component is created
     * @param property  the entity attribute for which the component is created
     * @param operation the property filter operation for which the component is created
     */
    public PropertyFilterComponentGenerationContext(MetaClass metaClass, String property, Operation operation) {
        super(metaClass, property);
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }
}
