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

package io.jmix.dashboardsui.repository;


import io.jmix.dashboards.model.Widget;
import io.jmix.ui.screen.ScreenFragment;

import java.util.List;
import java.util.Map;

/**
 * Scanning the project for use classes with the annotation {@link Widget} and
 * provides information {@link WidgetTypeInfo} about these classes
 */
public interface WidgetRepository {

    List<WidgetTypeInfo> getWidgetTypesInfo();

    void initializeWidgetFields(ScreenFragment widgetFragment, Widget widget);

    void serializeWidgetFields(ScreenFragment widgetFragment, Widget widget);

    Map<String, Object> getWidgetParams(Widget widget);

    String getLocalizedWidgetName(Widget widget);
}
