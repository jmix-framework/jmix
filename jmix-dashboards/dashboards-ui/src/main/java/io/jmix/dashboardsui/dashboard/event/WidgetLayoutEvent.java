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

package io.jmix.dashboardsui.dashboard.event;

import io.jmix.dashboards.model.visualmodel.DashboardLayout;

import java.util.UUID;

public class WidgetLayoutEvent extends AbstractDashboardEditEvent {

    private UUID parentLayoutUuid;
    private WidgetDropLocation location;

    public WidgetLayoutEvent(DashboardLayout source, UUID targetLayoutUuid, String location) {
        super(source);
        this.parentLayoutUuid = targetLayoutUuid;
        this.location = WidgetDropLocation.valueOf(location);
    }

    public WidgetLayoutEvent(DashboardLayout source, UUID targetLayoutUuid, WidgetDropLocation dropLocation) {
        super(source);
        this.parentLayoutUuid = targetLayoutUuid;
        this.location = dropLocation;
    }

    @Override
    public DashboardLayout getSource() {
        return (DashboardLayout) super.getSource();
    }

    public UUID getParentLayoutUuid() {
        return parentLayoutUuid;
    }

    public WidgetDropLocation getLocation() {
        return location;
    }

}
