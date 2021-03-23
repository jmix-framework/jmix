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

package io.jmix.dashboardsui.dashboard.converter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboards.utils.DashboardLayoutManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Supports the conversion to json for non-persistent objects with types: {@link DashboardModel},
 * {@link Widget}, {@link Parameter}
 */
@Component("dshbrd_JsonConverter")
public class JsonConverter {

    protected Gson gson;

    @Autowired
    protected DashboardLayoutManager layoutManager;

    public JsonConverter() {
        GsonBuilder builder = new GsonBuilder();
        builder.addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getAnnotations().contains(Transient.class) || f.hasModifier(Modifier.TRANSIENT);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        builder.addSerializationExclusionStrategy(new AnnotationExclusionStrategy());
        builder.addSerializationExclusionStrategy(new EntityEntryExclusionStrategy());
        builder.addDeserializationExclusionStrategy(new EntityEntryExclusionStrategy());
        builder.serializeNulls();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(ParameterValue.class, new InheritanceAdapter());
        builder.registerTypeAdapter(DashboardLayout.class, new InheritanceAdapter());
        gson = builder.create();
    }

    public String widgetToJson(Widget widget) {
        return gson.toJson(widget, new TypeToken<Widget>() {
        }.getType());
    }

    public String dashboardToJson(DashboardModel dashboard) {
        return gson.toJson(dashboard, new TypeToken<DashboardModel>() {
        }.getType());
    }

    public DashboardModel dashboardFromJson(String json) {
        DashboardModel dashboard = gson.fromJson(json, DashboardModel.class);
        RootLayout rootLayout = dashboard.getVisualModel();
        initLayoutParents(rootLayout);
        return dashboard;
    }

    private void initLayoutParents(DashboardLayout rootLayout) {
        rootLayout.setCaption(layoutManager.getCaption(rootLayout));
        if (rootLayout instanceof GridLayout) {
            GridLayout gridLayout = (GridLayout) rootLayout;
            Set<GridArea> gridAreas = gridLayout.getAreas();
            for (GridArea gridArea : gridAreas) {
                DashboardLayout dashboardLayout = gridArea.getComponent();
                dashboardLayout.setParent(rootLayout);
                initLayoutParents(dashboardLayout);
            }
        } else if (rootLayout instanceof ResponsiveLayout) {
            ResponsiveLayout gridLayout = (ResponsiveLayout) rootLayout;
            Set<ResponsiveArea> areas = gridLayout.getAreas();
            for (ResponsiveArea area : areas) {
                DashboardLayout dashboardLayout = area.getComponent();
                dashboardLayout.setParent(rootLayout);
                initLayoutParents(dashboardLayout);
            }
        } else if (rootLayout.getChildren() != null) {
            for (DashboardLayout layout : rootLayout.getChildren()) {
                layout.setParent(rootLayout);
                initLayoutParents(layout);
            }
        }
    }

    public Widget widgetFromJson(String json) {
        return gson.fromJson(json, Widget.class);
    }

    public Parameter parameterFromJson(String json) {
        return gson.fromJson(json, Parameter.class);
    }
}
