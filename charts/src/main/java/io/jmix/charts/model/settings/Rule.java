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

package io.jmix.charts.model.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jmix.charts.model.AbstractChartObject;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * Responsive rule for chart. See documentation for responsive plugin.<br>
 *
 * <a href="https://github.com/amcharts/responsive">https://github.com/amcharts/responsive</a>
 */
@StudioElement(
        caption = "Rule",
        xmlElement = "rule",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Rule extends AbstractChartObject {

    private static final long serialVersionUID = -7360797549434731632L;

    private Integer minWidth;
    private Integer maxWidth;
    private Integer minHeight;
    private Integer maxHeight;

    private JsonElement overridesJson;
    private String rawOverridesJson;

    public Integer getMinWidth() {
        return minWidth;
    }

    @StudioProperty
    public Rule setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    @StudioProperty
    public Rule setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Integer getMinHeight() {
        return minHeight;
    }

    @StudioProperty
    public Rule setMinHeight(Integer minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    public Integer getMaxHeight() {
        return maxHeight;
    }

    @StudioProperty
    public Rule setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public JsonElement getOverridesJson() {
        return overridesJson;
    }

    public String getRawOverridesJson() {
        return rawOverridesJson;
    }

    public Rule setRawOverridesJson(String rawOverridesJson) {
        this.rawOverridesJson = rawOverridesJson;

        JsonParser parser = new JsonParser();
        this.overridesJson = parser.parse(rawOverridesJson);

        return this;
    }
}