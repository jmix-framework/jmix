/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.property.populator.impl;

import io.jmix.dataimport.configuration.mapping.PropertyMapping;

public class CreatedReference {
    protected Object ownerEntity;
    protected PropertyMapping propertyMapping;
    protected Object createdObject;

    public Object getCreatedObject() {
        return createdObject;
    }

    public CreatedReference setCreatedObject(Object createdObject) {
        this.createdObject = createdObject;
        return this;
    }

    public Object getOwnerEntity() {
        return ownerEntity;
    }

    public CreatedReference setOwnerEntity(Object ownerEntity) {
        this.ownerEntity = ownerEntity;
        return this;
    }

    public PropertyMapping getPropertyMapping() {
        return propertyMapping;
    }

    public CreatedReference setPropertyMapping(PropertyMapping propertyMapping) {
        this.propertyMapping = propertyMapping;
        return this;
    }
}
