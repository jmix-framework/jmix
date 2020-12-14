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
import io.jmix.ui.component.SupportsCaptionPosition;

import javax.persistence.Convert;
import javax.persistence.Transient;

@JmixEntity(name = "ui_PropertyFilterCondition", annotatedPropertiesOnly = true)
@SystemLevel
public class PropertyFilterCondition extends FilterCondition {

    @JmixProperty
    protected String property;

    @JmixProperty
    protected String parameterName;

    @JmixProperty
    @Convert(converter = PropertyFilterOperationConverter.class)
    protected Operation operation;

    @JmixProperty
    protected Boolean operationEditable = true;

    @JmixProperty
    @Convert(converter = CaptionPositionConverter.class)
    protected SupportsCaptionPosition.CaptionPosition captionPosition;

    @JmixProperty
    protected PropertyFilterValueComponent valueComponent;

    @JmixProperty
    protected Boolean required = false;

    @Transient
    protected transient Object parameterValue;

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
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Boolean getOperationEditable() {
        return operationEditable;
    }

    public void setOperationEditable(Boolean operationEditable) {
        this.operationEditable = operationEditable;
    }

    public SupportsCaptionPosition.CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    public void setCaptionPosition(SupportsCaptionPosition.CaptionPosition captionPosition) {
        this.captionPosition = captionPosition;
    }

    public PropertyFilterValueComponent getValueComponent() {
        return valueComponent;
    }

    public void setValueComponent(PropertyFilterValueComponent valueComponent) {
        this.valueComponent = valueComponent;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(Object parameterValue) {
        this.parameterValue = parameterValue;
    }
}
