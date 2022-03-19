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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.io.Serializable;

@JmixEntity(name = "ui_FilterCondition")
@SystemLevel
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(value = {"parent"})
public abstract class FilterCondition implements Serializable {

    private static final long serialVersionUID = -2993349561173596671L;

    @JmixProperty
    protected String componentId;

    @JmixProperty
    protected Boolean visible = true;

    @JmixProperty
    protected Boolean enabled = true;

    @JmixProperty
    protected String caption;

    @JmixProperty
    @InstanceName
    protected String localizedCaption;

    @JmixProperty
    protected String styleName;

    @JmixProperty
    protected FilterCondition parent;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocalizedCaption() {
        return localizedCaption;
    }

    public void setLocalizedCaption(String localizedCaption) {
        this.localizedCaption = localizedCaption;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public FilterCondition getParent() {
        return parent;
    }

    public void setParent(FilterCondition parent) {
        this.parent = parent;
    }
}
