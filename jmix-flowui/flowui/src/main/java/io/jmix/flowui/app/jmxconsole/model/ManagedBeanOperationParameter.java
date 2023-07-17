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

package io.jmix.flowui.app.jmxconsole.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

@JmixEntity(name = "ui_ManagedBeanOperationParameter")
@SystemLevel
public class ManagedBeanOperationParameter {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    protected String name;

    protected String description;

    protected String type;

    protected String javaType;

    protected ManagedBeanOperation operation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public ManagedBeanOperation getOperation() {
        return operation;
    }

    public void setOperation(ManagedBeanOperation operation) {
        this.operation = operation;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}