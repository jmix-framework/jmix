/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.axis;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.shared.InnerTooltip;

public class Polar extends ChartObservableObject {

    protected String id;

    protected Integer zLevel;

    protected Integer z;

    protected String[] center;

    protected String[] radius;

    protected InnerTooltip tooltip;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    public String[] getCenter() {
        return center;
    }

    public void setCenter(String x, String y) {
        this.center = new String[]{x, y};
        markAsDirty();
    }

    public String[] getRadius() {
        return radius;
    }

    public void setRadius(String inner, String outer) {
        this.radius = new String[]{inner, outer};
        markAsDirty();
    }

    public InnerTooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(InnerTooltip tooltip) {
        if (this.tooltip != null) {
            removeChild(this.tooltip);
        }

        this.tooltip = tooltip;
        addChild(tooltip);
    }

    public Polar withId(String id) {
        setId(id);
        return this;
    }

    public Polar withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public Polar withZ(Integer z) {
        setZ(z);
        return this;
    }

    public Polar withCenter(String x, String y) {
        setCenter(x, y);
        return this;
    }

    public Polar withRadius(String inner, String outer) {
        setRadius(inner, outer);
        return this;
    }

    public Polar withTooltip(InnerTooltip tooltip) {
        setTooltip(tooltip);
        return this;
    }
}
