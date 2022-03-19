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

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.balloon.BalloonPointerOrientation;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.cursor.CursorPosition;
import io.jmix.charts.model.date.DateFormat;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines set of properties for chart cursor. If there is no default value specified, default value of {@link Cursor}
 * class will be used.
 * <br>
 * See documentation for properties of ChartCursorSettings JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/ChartCursorSettings">http://docs.amcharts.com/3/javascriptstockchart/ChartCursorSettings</a>
 */
@StudioElement(
        caption = "ChartCursorSettings",
        xmlElement = "chartCursorSettings",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class ChartCursorSettings extends AbstractChartObject {

    private static final long serialVersionUID = 3106275318894887204L;

    private BalloonPointerOrientation balloonPointerOrientation;

    private Boolean bulletsEnabled;

    private Integer bulletSize;

    private Double categoryBalloonAlpha;

    private Color categoryBalloonColor;

    private List<DateFormat> categoryBalloonDateFormats;

    private Boolean categoryBalloonEnabled;

    private String categoryBalloonText;

    private Color color;

    private Double cursorAlpha;

    private Color cursorColor;

    private CursorPosition cursorPosition;

    private Boolean enabled;

    private Boolean fullWidth;

    private Double graphBulletSize;

    private Boolean leaveAfterTouch;

    private Boolean leaveCursor;

    private Boolean onePanelOnly;

    private Boolean pan;

    private Boolean valueBalloonsEnabled;

    private Double valueLineAlpha;

    private Boolean valueLineBalloonEnabled;

    private Boolean valueLineEnabled;

    private Boolean zoomable;

    private Boolean selectWithoutZooming;

    private Boolean showNextAvailable;

    /**
     * @return orientation of value balloon pointer
     */
    public BalloonPointerOrientation getBalloonPointerOrientation() {
        return balloonPointerOrientation;
    }

    /**
     * Sets orientation of value balloon pointer. If not set the default value is HORIZONTAL.
     *
     * @param balloonPointerOrientation orientation of value balloon pointer
     * @return chart cursor settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "HORIZONTAL")
    public ChartCursorSettings setBalloonPointerOrientation(BalloonPointerOrientation balloonPointerOrientation) {
        this.balloonPointerOrientation = balloonPointerOrientation;
        return this;
    }

    /**
     * @return true if bulletsEnabled is enabled
     */
    public Boolean getBulletsEnabled() {
        return bulletsEnabled;
    }

    /**
     * Specifies if bullet for each graph will follow the cursor.
     *
     * @param bulletsEnabled bulletsEnabled option
     * @return chart cursor settings
     */
    @StudioProperty
    public ChartCursorSettings setBulletsEnabled(Boolean bulletsEnabled) {
        this.bulletsEnabled = bulletsEnabled;
        return this;
    }

    /**
     * @return bullet size
     */
    public Integer getBulletSize() {
        return bulletSize;
    }

    /**
     * Sets size of bullets, following the cursor.
     *
     * @param bulletSize bullet size
     * @return chart cursor settings
     */
    @StudioProperty
    public ChartCursorSettings setBulletSize(Integer bulletSize) {
        this.bulletSize = bulletSize;
        return this;
    }

    /**
     * @return opacity of the category balloon
     */
    public Double getCategoryBalloonAlpha() {
        return categoryBalloonAlpha;
    }

    /**
     * Sets opacity of the category balloon.
     *
     * @param categoryBalloonAlpha opacity of the category balloon
     * @return chart cursor settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartCursorSettings setCategoryBalloonAlpha(Double categoryBalloonAlpha) {
        this.categoryBalloonAlpha = categoryBalloonAlpha;
        return this;
    }

    /**
     * @return color of the category balloon
     */
    public Color getCategoryBalloonColor() {
        return categoryBalloonColor;
    }

    /**
     * Sets color of the category balloon.
     *
     * @param categoryBalloonColor color of the category balloon
     * @return chart cursor settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartCursorSettings setCategoryBalloonColor(Color categoryBalloonColor) {
        this.categoryBalloonColor = categoryBalloonColor;
        return this;
    }

    /**
     * @return list of date formats
     */
    public List<DateFormat> getCategoryBalloonDateFormats() {
        return categoryBalloonDateFormats;
    }

    /**
     * Sets list of date formats. If not set the default value is
     * <pre>{@code
     * [{period:"YYYY", format:"YYYY"},
     *  {period:"MM",   format:"MMM, YYYY"},
     *  {period:"WW",   format:"MMM DD, YYYY"},
     *  {period:"DD",   format:"MMM DD, YYYY"},
     *  {period:"hh",   format:"JJ:NN"},
     *  {period:"mm",   format:"JJ:NN"},
     *  {period:"ss",   format:"JJ:NN:SS"},
     *  {period:"fff",  format:"JJ:NN:SS"}]}
     * </pre>
     *
     * @param categoryBalloonDateFormats list of date formats
     * @return chart cursor settings
     */
    @StudioElementsGroup(caption = "Category Ballon Date Formats", xmlElement = "categoryBalloonDateFormats")
    public ChartCursorSettings setCategoryBalloonDateFormats(List<DateFormat> categoryBalloonDateFormats) {
        this.categoryBalloonDateFormats = categoryBalloonDateFormats;
        return this;
    }

    /**
     * Adds date formats.
     *
     * @param categoryBalloonDateFormats date formats
     * @return chart cursor settings
     */
    public ChartCursorSettings addCategoryBalloonDateFormats(DateFormat... categoryBalloonDateFormats) {
        if (categoryBalloonDateFormats != null) {
            if (this.categoryBalloonDateFormats == null) {
                this.categoryBalloonDateFormats = new ArrayList<>();
            }
            this.categoryBalloonDateFormats.addAll(Arrays.asList(categoryBalloonDateFormats));
        }
        return this;
    }

    /**
     * @return true if categoryBalloonEnabled is enabled
     */
    public Boolean getCategoryBalloonEnabled() {
        return categoryBalloonEnabled;
    }

    /**
     * Specifies whether category balloon is enabled.
     *
     * @param categoryBalloonEnabled categoryBalloonEnabled option
     * @return chart cursor settings
     */
    @StudioProperty
    public ChartCursorSettings setCategoryBalloonEnabled(Boolean categoryBalloonEnabled) {
        this.categoryBalloonEnabled = categoryBalloonEnabled;
        return this;
    }

    /**
     * @return category balloon text
     */
    public String getCategoryBalloonText() {
        return categoryBalloonText;
    }

    /**
     * Sets category balloon text. You can have [[category]] - [[toCategory]] tags in there and show category ranges
     * this way. If not set the default value is "[[category]]".
     *
     * @param categoryBalloonText category balloon text
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "[[category]]")
    public ChartCursorSettings setCategoryBalloonText(String categoryBalloonText) {
        this.categoryBalloonText = categoryBalloonText;
        return this;
    }

    /**
     * @return opacity of the cursor line
     */
    public Double getCursorAlpha() {
        return cursorAlpha;
    }

    /**
     * Sets opacity of the cursor line.
     *
     * @param cursorAlpha opacity of the cursor line
     * @return chart cursor settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartCursorSettings setCursorAlpha(Double cursorAlpha) {
        this.cursorAlpha = cursorAlpha;
        return this;
    }

    /**
     * @return color of the cursor line
     */
    public Color getCursorColor() {
        return cursorColor;
    }

    /**
     * Sets color of the cursor line. If not set the default value is #FFFFFF.
     *
     * @param cursorColor color of the cursor line
     * @return chart cursor settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public ChartCursorSettings setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
        return this;
    }

    /**
     * @return cursor position
     */
    public CursorPosition getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Sets cursor position. Possible values: start, middle, mouse.
     *
     * @param cursorPosition cursor position
     * @return chart cursor settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public ChartCursorSettings setCursorPosition(CursorPosition cursorPosition) {
        this.cursorPosition = cursorPosition;
        return this;
    }

    /**
     * @return true if this property is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled to false if you don't want chart cursor to appear in your charts. If not set the default value is
     * true.
     *
     * @param enabled enabled option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartCursorSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return true if a fill width always equals to the width of one data item
     */
    public Boolean getFullWidth() {
        return fullWidth;
    }

    /**
     * Set to true if you want instead of a cursor line user should see a fill which width will always be equal to the
     * width of one data item. It is recommended setting cursorAlpha to 0.1 or some other small number if using this
     * feature. If not set the default value is false.
     *
     * @param fullWidth fullWidth option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setFullWidth(Boolean fullWidth) {
        this.fullWidth = fullWidth;
        return this;
    }

    /**
     * @return size of a graph's bullet
     */
    public Double getGraphBulletSize() {
        return graphBulletSize;
    }

    /**
     * Sets size of a graph's bullet (if available) at the cursor position. If you don't want the bullet to change
     * its size, set this property to 1. If not set the default value is 1.7
     *
     * @param graphBulletSize size of a graph's bullet
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "1.7")
    public ChartCursorSettings setGraphBulletSize(Double graphBulletSize) {
        this.graphBulletSize = graphBulletSize;
        return this;
    }

    /**
     * @return true if cursor and balloons remain after the user touches the chart
     */
    public Boolean getLeaveAfterTouch() {
        return leaveAfterTouch;
    }

    /**
     * Set leaveAfterTouch to false if cursor and balloons shouldn't remain after the user touches the chart. If not
     * set the default value is true.
     *
     * @param leaveAfterTouch leaveAfterTouch option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartCursorSettings setLeaveAfterTouch(Boolean leaveAfterTouch) {
        this.leaveAfterTouch = leaveAfterTouch;
        return this;
    }

    /**
     * @return true if the cursor remains in the last position
     */
    public Boolean getLeaveCursor() {
        return leaveCursor;
    }

    /**
     * Set leaveCursor to true if cursor should be left at it's last position. Useful for touch devices - user might
     * want to see the balloons after he moves finger away. If not set the default value is false.
     *
     * @param leaveCursor leaveCursor option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setLeaveCursor(Boolean leaveCursor) {
        this.leaveCursor = leaveCursor;
        return this;
    }

    /**
     * @return true if stock chart displays value balloons on currently hovered panel only
     */
    public Boolean getOnePanelOnly() {
        return onePanelOnly;
    }

    /**
     * Set onePanelOnly to true if stock chart should display value balloons on currently hovered panel only. If not
     * set the default value is false.
     *
     * @param onePanelOnly onePanelOnly option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setOnePanelOnly(Boolean onePanelOnly) {
        this.onePanelOnly = onePanelOnly;
        return this;
    }

    /**
     * @return true if the user can pan the chart instead of zooming
     */
    public Boolean getPan() {
        return pan;
    }

    /**
     * Set pan to true and the user will be able to pan the chart instead of zooming.
     *
     * @param pan pan option
     * @return chart cursor settings
     */
    @StudioProperty
    public ChartCursorSettings setPan(Boolean pan) {
        this.pan = pan;
        return this;
    }

    /**
     * @return true if value balloons is enabled
     */
    public Boolean getValueBalloonsEnabled() {
        return valueBalloonsEnabled;
    }

    /**
     * Set valueBalloonsEnabled to true if value balloons should be enabled. In case they are not enabled, the balloons
     * might be displayed anyway, when the user rolls-over the column or bullet. If not set the default value is false.
     *
     * @param valueBalloonsEnabled valueBalloonsEnabled option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setValueBalloonsEnabled(Boolean valueBalloonsEnabled) {
        this.valueBalloonsEnabled = valueBalloonsEnabled;
        return this;
    }

    /**
     * @return opacity of value line
     */
    public Double getValueLineAlpha() {
        return valueLineAlpha;
    }

    /**
     * Sets opacity of value line. Will use cursorAlpha value if not set.
     *
     * @param valueLineAlpha opacity of value line
     * @return chart cursor settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartCursorSettings setValueLineAlpha(Double valueLineAlpha) {
        this.valueLineAlpha = valueLineAlpha;
        return this;
    }

    /**
     * @return true if value balloon next to value axis labels is displayed
     */
    public Boolean getValueLineBalloonEnabled() {
        return valueLineBalloonEnabled;
    }

    /**
     * Set valueLineBalloonEnabled to true if value balloon next to value axis labels should be displayed. If you
     * have more than one axis, set {@link Cursor#valueLineAxis} to indicate which axis should display the balloon.
     * If not set the default value is false.
     *
     * @param valueLineBalloonEnabled valueLineBalloonEnabled option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setValueLineBalloonEnabled(Boolean valueLineBalloonEnabled) {
        this.valueLineBalloonEnabled = valueLineBalloonEnabled;
        return this;
    }

    /**
     * @return true if cursor of serial chart displays horizontal (or vertical if chart is rotated) line
     */
    public Boolean getValueLineEnabled() {
        return valueLineEnabled;
    }

    /**
     * Set valueLineEnabled to true if cursor of serial chart should display horizontal (or vertical if chart is
     * rotated) line. This line might help users to compare distant values of a chart. You can also enable value
     * balloon on this line by setting {@link Cursor#valueLineAxis} property. If not set the default value is false.
     *
     * @param valueLineEnabled valueLineEnabled option
     * @return chart cursor settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartCursorSettings setValueLineEnabled(Boolean valueLineEnabled) {
        this.valueLineEnabled = valueLineEnabled;
        return this;
    }

    /**
     * @return true if the user can zoom-in the chart
     */
    public Boolean getZoomable() {
        return zoomable;
    }

    /**
     * Set zoomable to true if the user should zoom-in the chart. If pan is set to true, zoomable is switched to false
     * automatically.
     *
     * @param zoomable zoomable option
     * @return chart cursor settings
     */
    @StudioProperty
    public ChartCursorSettings setZoomable(Boolean zoomable) {
        this.zoomable = zoomable;
        return this;
    }

    /**
     * @return text color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets text color.
     *
     * @param color text color
     * @return chart cursor settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartCursorSettings setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return true if cursor selects area but not zoom-in
     */
    public Boolean getSelectWithoutZooming() {
        return selectWithoutZooming;
    }

    /**
     * Set selectWithoutZooming to true if cursor should only mark selected area but not zoom-in after user releases
     * mouse button. If not set the default value is false.
     *
     * @param selectWithoutZooming selectWithoutZooming option
     */
    @StudioProperty(defaultValue = "false")
    public void setSelectWithoutZooming(Boolean selectWithoutZooming) {
        this.selectWithoutZooming = selectWithoutZooming;
    }

    /**
     * @return true if graph displays balloon on next available data point while currently hovered item doesn't have
     * value for this graph
     */
    public Boolean getShowNextAvailable() {
        return showNextAvailable;
    }

    /**
     * If true, the graph will display balloon on next available data point if currently hovered item doesn't have
     * value for this graph. If not set the default value is false.
     *
     * @param showNextAvailable showNextAvailable option
     */
    @StudioProperty(defaultValue = "false")
    public void setShowNextAvailable(Boolean showNextAvailable) {
        this.showNextAvailable = showNextAvailable;
    }
}
