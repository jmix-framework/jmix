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

package io.jmix.dashboardsui.dashboard.assistant;

import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.event.DashboardEvent;

/**
 * Implementation of this interface can be linked to the dashboard and invoked on every {@link DashboardEvent}.
 */
public interface DashboardViewAssistant {

    /**
     * @param dashboard dashboard component which assistant is related to
     */
    void init(Dashboard dashboard);
}
