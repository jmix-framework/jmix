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

package io.jmix.reports.entity.pivottable;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.util.UUID;

@JmixEntity(name = "report_PivotTableProperty")
public class PivotTableProperty {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    protected String name;

    @JmixProperty
    protected String caption;

    @Lob
    @JmixProperty
    protected String function;

    @JmixProperty
    protected Boolean hidden = false;

    @JmixProperty
    protected PivotTablePropertyType type;

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
        if (StringUtils.isNotEmpty(name)) {
            setCaption(StringUtils.capitalize(name.replace('_', ' ')));
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public PivotTablePropertyType getType() {
        return type;
    }

    public void setType(PivotTablePropertyType type) {
        this.type = type;
    }
}
