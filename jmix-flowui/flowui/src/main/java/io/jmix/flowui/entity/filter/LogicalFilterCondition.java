/*
 * Copyright 2023 Haulmont.
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
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent.Operation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "flowui_LogicalFilterCondition")
@SystemLevel
public abstract class LogicalFilterCondition extends FilterCondition {

    @InstanceName
    protected String operation;

    protected Boolean operationTextVisible = true;

    protected List<FilterCondition> ownFilterConditions = new ArrayList<>();

    @Nullable
    public Operation getOperation() {
        return Operation.fromId(operation);
    }

    public void setOperation(Operation operation) {
        this.operation = operation != null ? operation.name() : null;
    }

    public Boolean getOperationTextVisible() {
        return operationTextVisible;
    }

    public void setOperationTextVisible(Boolean operationTextVisible) {
        this.operationTextVisible = operationTextVisible;
    }

    public List<FilterCondition> getOwnFilterConditions() {
        return ownFilterConditions;
    }

    public void setOwnFilterConditions(List<FilterCondition> ownFilterConditions) {
        this.ownFilterConditions = ownFilterConditions;
    }
}
