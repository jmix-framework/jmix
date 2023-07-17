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
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.flowui.app.jmxconsole.AttributeHelper;

import java.util.UUID;

@JmixEntity(name = "ui_ManagedBeanAttribute", annotatedPropertiesOnly = true)
@SystemLevel
public class ManagedBeanAttribute {

    @JmixId
    @JmixGeneratedValue
    @JmixProperty
    protected UUID id;

    @JmixProperty
    protected String name;

    @JmixProperty
    protected String description;

    @JmixProperty
    protected String type;

    @JmixProperty
    protected String readableWriteable;

    @JmixProperty
    protected Boolean readable;

    @JmixProperty
    protected Boolean writeable;

    protected Object value;

    @JmixProperty
    protected ManagedBeanInfo mbean;

    @JmixProperty
    protected String valueString;

    public ManagedBeanInfo getMbean() {
        return mbean;
    }

    public void setMbean(ManagedBeanInfo mbean) {
        this.mbean = mbean;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.valueString = AttributeHelper.convertToString(value);
    }

    public String getReadableWriteable() {
        return readableWriteable;
    }

    public void setReadableWriteable(String readableWriteable) {
        this.readableWriteable = readableWriteable;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    public Boolean getWriteable() {
        return writeable;
    }

    public void setWriteable(Boolean writeable) {
        this.writeable = writeable;
    }

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getValueString() {
        return valueString;
    }
}
