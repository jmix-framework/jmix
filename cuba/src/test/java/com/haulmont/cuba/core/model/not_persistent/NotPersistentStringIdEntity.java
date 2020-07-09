/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.model.not_persistent;

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;

import javax.persistence.Id;

@ModelObject(name = "test$NotPersistentStringIdEntity")
public class NotPersistentStringIdEntity implements JmixEntity {

    @Id
    @ModelProperty(mandatory = true)
    protected String identifier;

    @ModelProperty
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
