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

package io.jmix.reports.entity.wizard;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;

import java.util.UUID;

@JmixEntity(name = "report_QueryParameter", annotatedPropertiesOnly = true)
@SystemLevel
public class QueryParameter {

    @JmixId
    @JmixGeneratedValue
    @JmixProperty
    protected UUID id;

    @JmixProperty
    protected String name;

    @JmixProperty
    protected String javaClassName;

    @JmixProperty
    protected String entityMetaClassName;

    @JmixProperty
    protected ParameterType parameterType;

    @JmixProperty
    protected String defaultValueString;

    protected Object defaultValue;

    @JmixProperty
    protected PredefinedTransformation predefinedTransformation;

    @JmixProperty
    protected Boolean hidden;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public String getDefaultValueString() {
        return defaultValueString;
    }

    public void setDefaultValueString(String defaultValueString) {
        this.defaultValueString = defaultValueString;
    }

    public PredefinedTransformation getPredefinedTransformation() {
        return predefinedTransformation;
    }

    public void setPredefinedTransformation(PredefinedTransformation predefinedTransformation) {
        this.predefinedTransformation = predefinedTransformation;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getEntityMetaClassName() {
        return entityMetaClassName;
    }

    public void setEntityMetaClassName(String entityMetaClassName) {
        this.entityMetaClassName = entityMetaClassName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
