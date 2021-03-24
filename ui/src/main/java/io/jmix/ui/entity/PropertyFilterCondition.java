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

package io.jmix.ui.entity;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.ui.component.PropertyFilter.Operation;

import javax.persistence.Convert;

@JmixEntity(name = "ui_PropertyFilterCondition")
@SystemLevel
public class PropertyFilterCondition extends AbstractSingleFilterCondition {

    private static final long serialVersionUID = 486148668136186191L;

    @JmixProperty
    protected String property;

    @JmixProperty
    protected String parameterName;

    @JmixProperty
    protected String operation;

    @JmixProperty
    protected Boolean operationEditable = true;

    @JmixProperty
    protected Boolean operationCaptionVisible = true;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Operation getOperation() {
        return Operation.fromId(operation);
    }

    public void setOperation(Operation operation) {
        this.operation = operation != null ? operation.getId() : null;
    }

    public Boolean getOperationEditable() {
        return operationEditable;
    }

    public void setOperationEditable(Boolean operationEditable) {
        this.operationEditable = operationEditable;
    }

    public Boolean getOperationCaptionVisible() {
        return operationCaptionVisible;
    }

    public void setOperationCaptionVisible(Boolean operationCaptionVisible) {
        this.operationCaptionVisible = operationCaptionVisible;
    }
}
