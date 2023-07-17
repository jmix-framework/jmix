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
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.List;
import java.util.UUID;

@JmixEntity(name = "ui_ManagedBeanInfo")
@SystemLevel
public class ManagedBeanInfo {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    protected String className;

    protected String description;

    @InstanceName
    protected String objectName;

    protected String domain;

    protected String propertyList;

    protected List<ManagedBeanAttribute> attributes;

    protected List<ManagedBeanOperation> operations;

    public List<ManagedBeanOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<ManagedBeanOperation> operations) {
        this.operations = operations;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(String propertyList) {
        this.propertyList = propertyList;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<ManagedBeanAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ManagedBeanAttribute> attributes) {
        this.attributes = attributes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}