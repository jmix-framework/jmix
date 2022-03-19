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

package io.jmix.dataimport.property.populator;

import io.jmix.dataimport.property.populator.impl.CreatedReference;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
    protected Object entity;
    protected List<CreatedReference> createdReferences = new ArrayList<>();

    public EntityInfo(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    public EntityInfo setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public List<CreatedReference> getCreatedReferences() {
        return createdReferences;
    }

    public EntityInfo setCreatedReferences(List<CreatedReference> createdReferences) {
        this.createdReferences = createdReferences;
        return this;
    }
}