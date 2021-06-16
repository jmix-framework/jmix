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

package io.jmix.charts.model.legend;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.MarkerType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

@StudioElement(
        caption = "Legend Item",
        xmlElement = "item",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class LegendItem extends AbstractChartObject {

    private static final long serialVersionUID = 8563526782768492986L;

    private String title;

    private Color color;

    private MarkerType markerType;

    public Color getColor() {
        return color;
    }

    @StudioProperty(type = PropertyType.OPTIONS)
    public LegendItem setColor(Color color) {
        this.color = color;
        return this;
    }

    public MarkerType getMarkerType() {
        return markerType;
    }

    @StudioProperty(type = PropertyType.ENUMERATION)
    public LegendItem setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @StudioProperty
    public LegendItem setTitle(String title) {
        this.title = title;
        return this;
    }
}