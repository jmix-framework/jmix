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

package io.jmix.charts.model.period;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Position;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Displays date input fields and predefined period buttons.
 * <br>
 * See documentation for properties of PeriodSelector JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/PeriodSelector">http://docs.amcharts.com/3/javascriptstockchart/PeriodSelector</a>
 */
@StudioElement(
        caption = "PeriodSelector",
        xmlElement = "periodSelector",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class PeriodSelector extends AbstractChartObject {

    private static final long serialVersionUID = 1614139299608700421L;

    private String dateFormat;

    private String fromText;

    private Boolean hideOutOfScopePeriods;

    private Boolean inputFieldsEnabled;

    private Integer inputFieldWidth;

    private List<Period> periods;

    private String periodsText;

    private Position position;

    private Boolean selectFromStart;

    private String toText;

    private Integer width;

    /**
     * @return date format of date input fields
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format of date input fields. Note, only numeric date formats are allowed, so don't use "MMM" or "MMMM"
     * month format, two-digit years "YY" is NOT supported in this setting. If not set the default value is
     * "DD-MM-YYYY".
     *
     * @param dateFormat date format string
     * @return period selector
     */
    @StudioProperty(defaultValue = "DD-MM-YYYY")
    public PeriodSelector setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * @return from text
     */
    public String getFromText() {
        return fromText;
    }

    /**
     * Sets text displayed next to "from" date input field. If not set the default value is "From:".
     *
     * @param fromText from text
     * @return period selector
     */
    @StudioProperty(defaultValue = "From:")
    public PeriodSelector setFromText(String fromText) {
        this.fromText = fromText;
        return this;
    }

    /**
     * @return true if period buttons with date range bigger than available data are hidden
     */
    public Boolean getHideOutOfScopePeriods() {
        return hideOutOfScopePeriods;
    }

    /**
     * Set hideOutOfScopePeriods to false if period buttons with date range bigger than available data shouldn't be
     * hidden. If not set the default value is true.
     *
     * @param hideOutOfScopePeriods hideOutOfScopePeriods option
     * @return period selector
     */
    @StudioProperty(defaultValue = "true")
    public PeriodSelector setHideOutOfScopePeriods(Boolean hideOutOfScopePeriods) {
        this.hideOutOfScopePeriods = hideOutOfScopePeriods;
        return this;
    }

    /**
     * @return true if period selector displays "from" and "to" date input fields
     */
    public Boolean getInputFieldsEnabled() {
        return inputFieldsEnabled;
    }

    /**
     * Set inputFieldsEnabled to false if period selector shouldn't display "from" and "to" date input fields. If not
     * set the default value is true.
     *
     * @param inputFieldsEnabled inputFieldsEnabled option
     * @return period selector
     */
    @StudioProperty(defaultValue = "true")
    public PeriodSelector setInputFieldsEnabled(Boolean inputFieldsEnabled) {
        this.inputFieldsEnabled = inputFieldsEnabled;
        return this;
    }

    /**
     * @return width of date input fields, in pixels
     */
    public Integer getInputFieldWidth() {
        return inputFieldWidth;
    }

    /**
     * Sets width of date input fields, in pixels. Works only if period selector is horizontal. If not set the
     * default value is 100.
     *
     * @param inputFieldWidth width
     * @return period selector
     */
    @StudioProperty(defaultValue = "100")
    public PeriodSelector setInputFieldWidth(Integer inputFieldWidth) {
        this.inputFieldWidth = inputFieldWidth;
        return this;
    }

    /**
     * @return list of periods
     */
    public List<Period> getPeriods() {
        return periods;
    }

    /**
     * Sets list of predefined periods.
     *
     * @param periods list of periods
     * @return period selector
     */
    @StudioElementsGroup(caption = "Periods", xmlElement = "periods")
    public PeriodSelector setPeriods(List<Period> periods) {
        this.periods = periods;
        return this;
    }

    /**
     * Adds periods.
     *
     * @param periods periods
     * @return period selector
     */
    public PeriodSelector addPeriods(Period... periods) {
        if (periods != null) {
            if (this.periods == null) {
                this.periods = new ArrayList<>();
            }
            this.periods.addAll(Arrays.asList(periods));
        }
        return this;
    }

    /**
     * @return text displayed next to predefined period buttons
     */
    public String getPeriodsText() {
        return periodsText;
    }

    /**
     * Sets text displayed next to predefined period buttons. If not set the default value is "Zoom:".
     *
     * @param periodsText periods text
     * @return period selector
     */
    @StudioProperty(defaultValue = "Zoom:")
    public PeriodSelector setPeriodsText(String periodsText) {
        this.periodsText = periodsText;
        return this;
    }

    /**
     * @return position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets position. If not set the default value is BOTTOM.
     *
     * @param position position
     * @return period selector
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "BOTTOM")
    public PeriodSelector setPosition(Position position) {
        this.position = position;
        return this;
    }

    /**
     * @return true if predefined period buttons select a period from the beginning
     */
    public Boolean getSelectFromStart() {
        return selectFromStart;
    }

    /**
     * Set selectFromStart to true if predefined period buttons should select a period from the beginning. If not set
     * the default value is false.
     *
     * @param selectFromStart selectFromStart option
     * @return period selector
     */
    @StudioProperty(defaultValue = "false")
    public PeriodSelector setSelectFromStart(Boolean selectFromStart) {
        this.selectFromStart = selectFromStart;
        return this;
    }

    /**
     * @return text displayed next to "to" date input field
     */
    public String getToText() {
        return toText;
    }

    /**
     * Sets text displayed next to "to" date input field. If not set the default value is "To:".
     *
     * @param toText text
     * @return period selector
     */
    @StudioProperty(defaultValue = "To:")
    public PeriodSelector setToText(String toText) {
        this.toText = toText;
        return this;
    }

    /**
     * @return width of a period selector
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets width of a period selector, when position is "left" or "right". If not set the default value is 180.
     *
     * @param width width
     * @return period selector
     */
    @StudioProperty(defaultValue = "180")
    public PeriodSelector setWidth(Integer width) {
        this.width = width;
        return this;
    }
}
