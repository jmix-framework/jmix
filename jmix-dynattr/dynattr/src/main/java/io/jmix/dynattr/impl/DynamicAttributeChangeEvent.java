/*
 * Copyright 2025 Haulmont.
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

package io.jmix.dynattr.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.DynamicAttributes;
import org.springframework.context.ApplicationEvent;

public class DynamicAttributeChangeEvent extends ApplicationEvent /*implements ResolvableTypeProvider */{
    private final MetaClass metaClass;
    private final DynamicAttributes dynamicAttributes;

    public DynamicAttributeChangeEvent(MetaClass metaClass, Object entity, DynamicAttributes dynamicAttributes) {
        super(entity);
        this.metaClass = metaClass;
        this.dynamicAttributes = dynamicAttributes;
      //  this.originalMetaClass ;
    }

    public DynamicAttributes getDynamicAttributes() {
        return dynamicAttributes;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

/*
    @Internal
    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(),
                ResolvableType.forClass(originalMetaClass.getJavaClass()));
    }
*/
}
