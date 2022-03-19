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

package io.jmix.ui.app.jmxconsole.model;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.List;
import java.util.UUID;

@JmixEntity(name = "ui_ManagedBeanOperation")
@SystemLevel
public class ManagedBeanOperation {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    protected String name;

    protected String returnType;

    protected String description;

    protected Long timeout;

    protected ManagedBeanInfo mbean;

    protected List<ManagedBeanOperationParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ManagedBeanInfo getMbean() {
        return mbean;
    }

    public void setMbean(ManagedBeanInfo mbean) {
        this.mbean = mbean;
    }

    public List<ManagedBeanOperationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ManagedBeanOperationParameter> parameters) {
        this.parameters = parameters;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
