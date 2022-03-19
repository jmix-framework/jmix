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
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboards.model.visualmodel.*;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "dshbrd_DashboardModel")
public class DashboardModel {

    @Id
    @JmixGeneratedValue
    @JmixProperty
    protected UUID id;

    @NotNull
    @JmixProperty(mandatory = true)
    @InstanceName
    protected String title;

    /**
     * The unique identifier for searching in a database.
     */
    @NotNull
    @JmixProperty(mandatory = true)
    protected String code;

    /**
     * Stores a hierarchy of a visual model
     */
    @JmixProperty
    protected RootLayout visualModel;
    @JmixProperty
    protected List<Parameter> parameters = new ArrayList<>();
    @JmixProperty
    protected Boolean isAvailableForAllUsers = true;

    /**
     * Stores a login of the user, who created entity
     */
    @JmixProperty
    protected String createdBy;

    /**
     * Stores delay for publishing DashboardUpdatedEvent in view mode
     */
    @JmixProperty
    protected Integer timerDelay = 0;

    /**
     * Stores assistance bean name for dashboard
     */
    @JmixProperty
    protected String assistantBeanName;

    public Boolean getIsAvailableForAllUsers() {
        return isAvailableForAllUsers;
    }

    public void setIsAvailableForAllUsers(Boolean availableForAllUsers) {
        isAvailableForAllUsers = availableForAllUsers;
    }

    public List<Widget> getWidgets() {
        List<Widget> widgets = new ArrayList<>();
        getWidgets(visualModel, widgets);
        return widgets;
    }

    private void getWidgets(DashboardLayout dashboardLayout, List<Widget> widgets) {
        if (dashboardLayout == null) {
            return;
        }
        if (WidgetLayout.class.isAssignableFrom(dashboardLayout.getClass())) {
            widgets.add(((WidgetLayout) dashboardLayout).getWidget());
        }
        if (GridLayout.class.isAssignableFrom(dashboardLayout.getClass())) {
            for (GridArea gridArea : ((GridLayout) dashboardLayout).getAreas()) {
                DashboardLayout component = gridArea.getComponent();
                getWidgets(component, widgets);

            }
        } else {
            for (DashboardLayout child : dashboardLayout.getChildren()) {
                getWidgets(child, widgets);
            }
        }

    }

    public WidgetLayout getWidgetLayout(UUID widgetId) {
        return getWidgetLayout(visualModel, widgetId);
    }

    private WidgetLayout getWidgetLayout(DashboardLayout dashboardLayout, UUID widgetId) {
        if (dashboardLayout == null || widgetId == null) {
            return null;
        }
        if (WidgetLayout.class.isAssignableFrom(dashboardLayout.getClass())) {
            Widget widget = ((WidgetLayout) dashboardLayout).getWidget();
            if (widgetId.equals(widget.getId())) {
                return (WidgetLayout) dashboardLayout;
            }
        }
        if (GridLayout.class.isAssignableFrom(dashboardLayout.getClass())) {
            for (GridArea gridArea : ((GridLayout) dashboardLayout).getAreas()) {
                DashboardLayout component = gridArea.getComponent();
                WidgetLayout tmp = getWidgetLayout(component, widgetId);
                if (tmp != null) {
                    return tmp;
                }
            }
        } else {
            for (DashboardLayout child : dashboardLayout.getChildren()) {
                WidgetLayout tmp = getWidgetLayout(child, widgetId);
                if (tmp != null) {
                    return tmp;
                }

            }
        }
        return null;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public RootLayout getVisualModel() {
        return visualModel;
    }

    public void setVisualModel(RootLayout visualModel) {
        this.visualModel = visualModel;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTimerDelay() {
        return timerDelay;
    }

    public void setTimerDelay(Integer timerDelay) {
        this.timerDelay = timerDelay;
    }

    public String getAssistantBeanName() {
        return assistantBeanName;
    }

    public void setAssistantBeanName(String assistantBeanName) {
        this.assistantBeanName = assistantBeanName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}