/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.entity.filter;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;

import org.springframework.lang.Nullable;
import java.util.Objects;

@JmixEntity(name = "flowui_PropertyFilterCondition")
@SystemLevel
public class PropertyFilterCondition extends AbstractSingleFilterCondition {

    protected String property;

    protected String parameterName;

    protected String operation;

    protected Boolean operationEditable = true;

    protected Boolean operationTextVisible = true;

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
        return operationFromId(operation);
    }

    public void setOperation(Operation operation) {
        this.operation = operation != null ? operation.name() : null;
    }

    public Boolean getOperationEditable() {
        return operationEditable;
    }

    public void setOperationEditable(Boolean operationEditable) {
        this.operationEditable = operationEditable;
    }

    public Boolean getOperationTextVisible() {
        return operationTextVisible;
    }

    public void setOperationTextVisible(Boolean operationTextVisible) {
        this.operationTextVisible = operationTextVisible;
    }

    @Nullable
    public static Operation operationFromId(String id) {
        for (Operation operation : Operation.values()) {
            if (Objects.equals(id, operation.name())) {
                return operation;
            }
        }
        return null;
    }
}
