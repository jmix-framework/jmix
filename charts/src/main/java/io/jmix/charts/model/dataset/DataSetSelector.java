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

package io.jmix.charts.model.dataset;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Position;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * DataSetSelector is a tool for selecting data set's as main and for comparing with main data set
 * <br>
 * See documentation for properties of DataSetSelector JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/DataSetSelector">http://docs.amcharts.com/3/javascriptstockchart/DataSetSelector</a>
 */
@StudioElement(
        caption = "DataSetSelector",
        xmlElement = "dataSetSelector",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class DataSetSelector extends AbstractChartObject {

    private static final long serialVersionUID = 3890644574314511185L;

    private String comboBoxSelectText;

    private String compareText;

    private Integer listHeight;

    private Position position;

    private String selectText;

    private Integer width;

    /**
     * @return text displayed in the "compare to" combobox
     */
    public String getComboBoxSelectText() {
        return comboBoxSelectText;
    }

    /**
     * Sets text displayed in the "compare to" combobox (when position is "top" or "bottom"). If not set the default
     * value is "Select..."
     *
     * @param comboBoxSelectText text
     * @return data set selector
     */
    @StudioProperty(defaultValue = "Select...")
    public DataSetSelector setComboBoxSelectText(String comboBoxSelectText) {
        this.comboBoxSelectText = comboBoxSelectText;
        return this;
    }

    /**
     * @return text displayed near "compare to" list
     */
    public String getCompareText() {
        return compareText;
    }

    /**
     * Sets text displayed near "compare to" list. If not set the default value is "Compare to:".
     *
     * @param compareText compare text
     * @return data set selector
     */
    @StudioProperty(defaultValue = "Compare to:")
    public DataSetSelector setCompareText(String compareText) {
        this.compareText = compareText;
        return this;
    }

    /**
     * @return maximum height, in pixels
     */
    public Integer getListHeight() {
        return listHeight;
    }

    /**
     * Sets the maximum height of the "compare to" field in pixels. If not set the default value is 150.
     *
     * @param listHeight list height
     * @return data set selector
     */
    @StudioProperty(defaultValue = "150")
    public DataSetSelector setListHeight(Integer listHeight) {
        this.listHeight = listHeight;
        return this;
    }

    /**
     * @return position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets position. Possible values: "right", "left", "top", "bottom". "top" and "bottom" positions has a
     * limitation - only one data set can be selected for comparing. If not set the default value is "right, left,
     * top, bottom".
     *
     * @param position position
     * @return data set selector
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public DataSetSelector setPosition(Position position) {
        this.position = position;
        return this;
    }

    /**
     * @return select text
     */
    public String getSelectText() {
        return selectText;
    }

    /**
     * Sets text displayed near "Select" drop down. If not set the default value is "Select:".
     *
     * @param selectText select text
     * @return data set selector
     */
    @StudioProperty(defaultValue = "Select:")
    public DataSetSelector setSelectText(String selectText) {
        this.selectText = selectText;
        return this;
    }

    /**
     * @return width of a data set selector
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets width of a data set selector, when position is "left" or "right". If not set the default value is 180.
     *
     * @param width width
     * @return data set selector
     */
    @StudioProperty(defaultValue = "180")
    public DataSetSelector setWidth(Integer width) {
        this.width = width;
        return this;
    }
}
