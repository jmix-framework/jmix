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

import io.jmix.dashboardsui.annotation.DashboardWidget;

import java.io.Serializable;

/**
 * Contains values of annotation {@link DashboardWidget}
 */
public class WidgetTypeInfo implements Serializable {

    protected String name;
    protected String fragmentId;
    protected String editFragmentId;

    public WidgetTypeInfo() {
    }

    public WidgetTypeInfo(String name, String fragmentId, String editFragmentId) {
        this.name = name;
        this.fragmentId = fragmentId;
        this.editFragmentId = editFragmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(String fragmentId) {
        this.fragmentId = fragmentId;
    }

    public String getEditFragmentId() {
        return editFragmentId;
    }

    public void setEditFragmentId(String editFragmentId) {
        this.editFragmentId = editFragmentId;
    }
}
