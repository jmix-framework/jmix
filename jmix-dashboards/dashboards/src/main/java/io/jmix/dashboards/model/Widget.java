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
package io.jmix.dashboards.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.dashboards.model.json.Exclude;
import io.jmix.dashboards.model.parameter.Parameter;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "dshbrd_Widget")
public class Widget {
    @Id
    @JmixGeneratedValue
    @JmixProperty
    protected UUID id;

    @JmixProperty
    protected Boolean showWidgetCaption = false;
    @JmixProperty
    protected String widgetId;
    @JmixProperty
    @InstanceName
    protected String caption;
    @JmixProperty
    protected String name;
    @JmixProperty
    protected String description;
    @JmixProperty
    protected List<Parameter> parameters = new ArrayList<>();
    @JmixProperty
    protected List<Parameter> widgetFields = new ArrayList<>();
    @JmixProperty
    protected String fragmentId;
    @JmixProperty
    @Exclude
    protected DashboardModel dashboard;

    /**
     * Stores a login of the user, who created entity
     */
    @JmixProperty
    protected String createdBy;

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<Parameter> getWidgetFields() {
        return widgetFields;
    }

    public void setWidgetFields(List<Parameter> widgetFields) {
        this.widgetFields = widgetFields;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(String fragmentId) {
        this.fragmentId = fragmentId;
    }

    public DashboardModel getDashboard() {
        return dashboard;
    }

    public void setDashboard(DashboardModel dashboard) {
        this.dashboard = dashboard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getShowWidgetCaption() {
        return showWidgetCaption;
    }

    public void setShowWidgetCaption(Boolean showWidgetCaption) {
        this.showWidgetCaption = showWidgetCaption;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}