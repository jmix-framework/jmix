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

package io.jmix.charts.model;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Creates a title on above the chart, multiple can be assigned.
 * <br>
 * See documentation for properties of Title JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/Title">http://docs.amcharts.com/3/javascriptcharts/Title</a>
 */
@StudioElement(
        caption = "Title",
        xmlElement = "title",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Title extends AbstractChartObject {

    private static final long serialVersionUID = -2398460960797730153L;

    private Double alpha;

    private Boolean bold;

    private Color color;

    private String id;

    private Integer size;

    private Integer tabIndex;

    private String text;

    /**
     * @return opacity of a title
     */
    public Double getAlpha() {
        return alpha;
    }

    /**
     * Sets opacity of a title. If not set the default value is 1.
     *
     * @param alpha opacity
     * @return title
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Title setAlpha(Double alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * @return true if title is bold
     */
    public Boolean getBold() {
        return bold;
    }

    /**
     * Set bold to false if title shouldn't be bold. If not set the default value is true.
     *
     * @param bold bold option
     * @return title
     */
    @StudioProperty(defaultValue = "true")
    public Title setBold(Boolean bold) {
        this.bold = bold;
        return this;
    }

    /**
     * @return text color of a title
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets text color of a title.
     *
     * @param color color
     * @return title
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Title setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return text size of a title
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Sets text size of a title.
     *
     * @param size text size
     * @return title
     */
    @StudioProperty
    public Title setSize(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * @return text of a title
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text of a title.
     *
     * @param text text
     * @return title
     */
    @StudioProperty
    public Title setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * @return unique id of a title
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of a title.
     *
     * @param id id
     * @return title
     */
    @StudioProperty
    public Title setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return tab index
     */
    public Integer getTabIndex() {
        return tabIndex;
    }

    /**
     * In case you set it to some number, the chart will set focus on the title when user clicks tab key. When a
     * focus is set, screen readers like NVDA screen reader will read the title. Note, not all browsers and readers
     * support this.
     *
     * @param tabIndex tab index
     * @return title
     */
    @StudioProperty
    public Title setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
        return this;
    }
}