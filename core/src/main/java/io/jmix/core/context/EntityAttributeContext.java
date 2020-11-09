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

package io.jmix.core.context;

import io.jmix.core.metamodel.model.MetaClass;

import java.util.HashSet;
import java.util.Set;

public class EntityAttributeContext implements AccessContext {

    protected final MetaClass entityClass;
    protected Set<String> viewableAttributes = new HashSet<>();
    protected Set<String> modifiableAttributes = new HashSet<>();

    public EntityAttributeContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isAttributeViewPermitted(String attribute) {
        return viewableAttributes.contains(attribute) || isAttributeModifyPermitted(attribute);
    }

    public boolean isAttributeModifyPermitted(String attribute) {
        return modifiableAttributes.contains(attribute);
    }

    public void addViewableAttribute(String name) {
        viewableAttributes.add(name);
    }

    public void addModifiableAttribute(String name) {
        modifiableAttributes.add(name);
    }

}
