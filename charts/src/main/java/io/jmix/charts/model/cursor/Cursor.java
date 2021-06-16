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

package io.jmix.charts.model.cursor;


import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.JsFunction;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;
import io.jmix.charts.model.graph.AbstractGraph;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Creates a cursor for the chart which follows the mouse movements. In case of {@link SerialChartModelImpl} charts it shows
 * the balloons of hovered data points.
 * <br>
 * See documentation for properties of ChartCursor JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/ChartCursor">http://docs.amcharts.com/3/javascriptcharts/ChartCursor</a>
 */
@StudioElement(
        caption = "Cursor",
        xmlElement = "chartCursor",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Cursor extends AbstractChartObject {

    private static final long serialVersionUID = 4196605135301917493L;

    private Integer adjustment;

    private Double animationDuration;

    private Boolean avoidBalloonOverlapping;

    private String balloonPointerOrientation;

    private Boolean bulletsEnabled;

    private Integer bulletSize;

    private Double categoryBalloonAlpha;

    private Color categoryBalloonColor;

    private String categoryBalloonDateFormat;

    private Boolean categoryBalloonEnabled;

    private JsFunction categoryBalloonFunction;

    private String categoryBalloonText;

    private Color color;

    private Double cursorAlpha;

    private Color cursorColor;

    private CursorPosition cursorPosition;

    private Boolean enabled;

    private Boolean fullWidth;

    private Double graphBulletAlpha;

    private Double graphBulletSize;

    private Boolean leaveAfterTouch;

    private Boolean leaveCursor;

    private String limitToGraph;

    private Boolean oneBalloonOnly;

    private Boolean pan;

    private Double selectionAlpha;

    private Boolean selectWithoutZooming;

    private Boolean showNextAvailable;

    private Boolean valueBalloonsEnabled;

    private Double valueLineAlpha;

    private String valueLineAxis;

    private Boolean valueLineBalloonEnabled;

    private Boolean valueLineEnabled;

    private Boolean valueZoomable;

    private Boolean zoomable;

    private Integer tabIndex;

    /**
     * @return adjustment
     */
    public Integer getAdjustment() {
        return adjustment;
    }

    /**
     * Sets adjustment. If you set adjustment to -1, the balloon will be shown near previous, if you set it to 1 - near
     * next data point. If not set the default value is 0.
     *
     * @param adjustment adjustment
     * @return cursor
     */
    @StudioProperty(defaultValue = "0")
    public Cursor setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
        return this;
    }

    /**
     * @return true if avoidBalloonOverlapping is enabled
     */
    public Boolean getAvoidBalloonOverlapping() {
        return avoidBalloonOverlapping;
    }

    /**
     * Specifies if cursor should arrange balloons so they won't overlap. If chart is rotated, it might be good idea
     * to turn this off. If not set the default value is true.
     *
     * @param avoidBalloonOverlapping avoidBalloonOverlapping option
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setAvoidBalloonOverlapping(Boolean avoidBalloonOverlapping) {
        this.avoidBalloonOverlapping = avoidBalloonOverlapping;
        return this;
    }

    /**
     * @return balloon pointer orientation
     */
    public String getBalloonPointerOrientation() {
        return balloonPointerOrientation;
    }

    /**
     * Sets the balloon pointer orientation. It defines if the balloon should be shown above the datapoint or sideways.
     * If not set the default value is "horizontal".
     *
     * @param balloonPointerOrientation balloon pointer orientation
     * @return cursor
     */
    @StudioProperty(defaultValue = "horizontal")
    public Cursor setBalloonPointerOrientation(String balloonPointerOrientation) {
        this.balloonPointerOrientation = balloonPointerOrientation;
        return this;
    }

    /**
     * @return duration of animation of a line, in seconds
     */
    public Double getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets duration of animation of a line, in seconds. If not set the default value is 0.3.
     *
     * @param animationDuration duration of animation of a line, in seconds
     * @return cursor
     */
    @StudioProperty(defaultValue = "0.3")
    public Cursor setAnimationDuration(Double animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    /**
     * @return true if bullet for each graph follows the cursor
     */
    public Boolean getBulletsEnabled() {
        return bulletsEnabled;
    }

    /**
     * Set bulletsEnabled to true if bullet for each graph should follow the cursor. If not set the default value is
     * false.
     *
     * @param bulletsEnabled bulletsEnabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setBulletsEnabled(Boolean bulletsEnabled) {
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
     * Sets bullet size, that following the cursor. If not set the default value is 8.
     *
     * @param bulletSize bullet size
     * @return cursor
     */
    @StudioProperty(defaultValue = "8")
    public Cursor setBulletSize(Integer bulletSize) {
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
     * Sets opacity of the category balloon. If not set the default value is 1.
     *
     * @param categoryBalloonAlpha opacity of the category balloon
     * @return cursor
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Cursor setCategoryBalloonAlpha(Double categoryBalloonAlpha) {
        this.categoryBalloonAlpha = categoryBalloonAlpha;
        return this;
    }

    /**
     * @return category balloon color
     */
    public Color getCategoryBalloonColor() {
        return categoryBalloonColor;
    }

    /**
     * Sets color of the category balloon. Default color is cursorColor.
     *
     * @param categoryBalloonColor category balloon color
     * @return cursor
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Cursor setCategoryBalloonColor(Color categoryBalloonColor) {
        this.categoryBalloonColor = categoryBalloonColor;
        return this;
    }

    /**
     * @return category balloon date format
     */
    public String getCategoryBalloonDateFormat() {
        return categoryBalloonDateFormat;
    }

    /**
     * Sets category balloon date format (used only if category axis parses dates). If not set the default value is
     * "MMM DD, YYYY".
     *
     * @param categoryBalloonDateFormat category balloon date format string
     * @return cursor
     */
    @StudioProperty(defaultValue = "MMM DD, YYYY")
    public Cursor setCategoryBalloonDateFormat(String categoryBalloonDateFormat) {
        this.categoryBalloonDateFormat = categoryBalloonDateFormat;
        return this;
    }

    /**
     * @return true if balloon is enabled
     */
    public Boolean getCategoryBalloonEnabled() {
        return categoryBalloonEnabled;
    }

    /**
     * Set categoryBalloonEnabled to false if you want balloon shouldn't be enabled. If not set the default value is
     * true.
     *
     * @param categoryBalloonEnabled categoryBalloonEnabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setCategoryBalloonEnabled(Boolean categoryBalloonEnabled) {
        this.categoryBalloonEnabled = categoryBalloonEnabled;
        return this;
    }

    /**
     * @return text color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets text color. If not set the default value is #FFFFFF.
     *
     * @param color text color
     * @return cursor
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public Cursor setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return opacity of the cursor line
     */
    public Double getCursorAlpha() {
        return cursorAlpha;
    }

    /**
     * Sets opacity of the cursor line. If not set the default value is 1.
     *
     * @param cursorAlpha opacity
     * @return cursor
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Cursor setCursorAlpha(Double cursorAlpha) {
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
     * Sets color of the cursor line. If not set the default value is #CC0000.
     *
     * @param cursorColor color
     * @return cursor
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#CC0000")
    public Cursor setCursorColor(Color cursorColor) {
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
     * Sets cursor position: on the beginning of the period (day, hour, etc) or in the middle (only when
     * {@link CategoryAxis#parseDates} is set to true). If you want the cursor to follow mouse and not to glue to the
     * nearest data point, set "mouse" here. Possible values are: start, middle, mouse. If not set the default value
     * is MIDDLE.
     *
     * @param cursorPosition cursor position
     * @return cursor
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "MIDDLE")
    public Cursor setCursorPosition(CursorPosition cursorPosition) {
        this.cursorPosition = cursorPosition;
        return this;
    }

    /**
     * @return true if cursor is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled to false if you want cursor shouldn't be enabled. If not set the default value is true.
     *
     * @param enabled enabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
     * its size, set this property to 1. If not set the default value is 1.7.
     *
     * @param graphBulletSize graph bullet size
     * @return cursor
     */
    @StudioProperty(defaultValue = "1.7")
    public Cursor setGraphBulletSize(Double graphBulletSize) {
        this.graphBulletSize = graphBulletSize;
        return this;
    }

    /**
     * @return true if only one balloon at a time is displayed
     */
    public Boolean getOneBalloonOnly() {
        return oneBalloonOnly;
    }

    /**
     * Set to oneBalloonOnly true if only one balloon at a time should be displayed. Note, this is quite CPU consuming.
     * If not set the default value is false.
     *
     * @param oneBalloonOnly oneBalloonOnly option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setOneBalloonOnly(Boolean oneBalloonOnly) {
        this.oneBalloonOnly = oneBalloonOnly;
        return this;
    }

    /**
     * @return true if the user can pan the chart instead of zooming
     */
    public Boolean getPan() {
        return pan;
    }

    /**
     * Set pan to true and the user will be able to pan the chart instead of zooming. If not set the default value is
     * false.
     *
     * @param pan pan option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setPan(Boolean pan) {
        this.pan = pan;
        return this;
    }

    /**
     * @return opacity of the selection
     */
    public Double getSelectionAlpha() {
        return selectionAlpha;
    }

    /**
     * Sets opacity of the selection. If not set the default value is 0.2.
     *
     * @param selectionAlpha opacity
     * @return cursor
     */
    @StudioProperty(defaultValue = "0.2")
    @Max(1)
    @Min(0)
    public Cursor setSelectionAlpha(Double selectionAlpha) {
        this.selectionAlpha = selectionAlpha;
        return this;
    }

    /**
     * @return true if cursor only marks selected area but not zoom-in after user releases mouse button
     */
    public Boolean getSelectWithoutZooming() {
        return selectWithoutZooming;
    }

    /**
     * Set selectWithoutZooming to true if cursor should only mark selected area but not zoom-in after user releases
     * mouse button. If not set the default value is false.
     *
     * @param selectWithoutZooming selectWithoutZooming option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setSelectWithoutZooming(Boolean selectWithoutZooming) {
        this.selectWithoutZooming = selectWithoutZooming;
        return this;
    }

    /**
     * @return true if the graph displays balloon on next available data point if currently hovered item doesn't have
     * value for this graph. If not set the default value is false.
     */
    public Boolean getShowNextAvailable() {
        return showNextAvailable;
    }

    /**
     * Set showNextAvailable to true if the graph should display balloon on next available data point if currently
     * hovered item doesn't have value for this graph.
     *
     * @param showNextAvailable showNextAvailable option
     * @return cursor
     */
    @StudioProperty
    public Cursor setShowNextAvailable(Boolean showNextAvailable) {
        this.showNextAvailable = showNextAvailable;
        return this;
    }

    /**
     * @return true if valueBalloonsEnabled is enabled
     */
    public Boolean getValueBalloonsEnabled() {
        return valueBalloonsEnabled;
    }

    /**
     * Set valueBalloonsEnabled to false if you want value balloons will not be enabled. In case they are
     * not, the balloons might be displayed anyway, when the user rolls-over the column or bullet. If not set the
     * default value is true.
     *
     * @param valueBalloonsEnabled valueBalloonsEnabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setValueBalloonsEnabled(Boolean valueBalloonsEnabled) {
        this.valueBalloonsEnabled = valueBalloonsEnabled;
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
     * automatically. If not set the default value is true.
     *
     * @param zoomable zoomable option
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setZoomable(Boolean zoomable) {
        this.zoomable = zoomable;
        return this;
    }

    /**
     * @return JS function that format category balloon text
     */
    public JsFunction getCategoryBalloonFunction() {
        return categoryBalloonFunction;
    }

    /**
     * Sets JS function that format category balloon text. This function should return a string which will be
     * displayed in a balloon. When categoryBalloonFunction is called, category value (or date) is passed as
     * an argument.
     *
     * @param categoryBalloonFunction JS function
     * @return cursor
     */
    public Cursor setCategoryBalloonFunction(JsFunction categoryBalloonFunction) {
        this.categoryBalloonFunction = categoryBalloonFunction;
        return this;
    }

    /**
     * @return true if instead of a cursor line, used fill which width will always be equal to the
     * width of one data item
     */
    public Boolean getFullWidth() {
        return fullWidth;
    }

    /**
     * Set to true if you want instead of a cursor line user should see a fill which width will always be equal to the
     * width of one data item. It recommended setting cursorAlpha to 0.1 or some other small number if using this
     * feature. If not set the default value is false.
     *
     * @param fullWidth fullWidth option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setFullWidth(Boolean fullWidth) {
        this.fullWidth = fullWidth;
        return this;
    }

    /**
     * @return opacity of graph bullet
     */
    public Double getGraphBulletAlpha() {
        return graphBulletAlpha;
    }

    /**
     * Sets opacity of graph bullet. If you make graph's bullets invisible by setting their opacity to 0
     * ({@link AbstractGraph#bulletAlpha}) and will set graphBulletAlpha to 1, the bullets will only appear at the
     * cursor's position.
     *
     * @param graphBulletAlpha opacity of graph bullet
     * @return cursor
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public Cursor setGraphBulletAlpha(Double graphBulletAlpha) {
        this.graphBulletAlpha = graphBulletAlpha;
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
     * @param valueLineAlpha opacity of value line.
     * @return cursor
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public Cursor setValueLineAlpha(Double valueLineAlpha) {
        this.valueLineAlpha = valueLineAlpha;
        return this;
    }

    /**
     * @return value line axis id
     */
    public String getValueLineAxis() {
        return valueLineAxis;
    }

    /**
     * Sets axis which should display the balloon.
     *
     * @param valueLineAxis value line axis id
     * @return cursor
     */
    @StudioProperty
    public Cursor setValueLineAxis(String valueLineAxis) {
        this.valueLineAxis = valueLineAxis;
        return this;
    }

    /**
     * @return true if value balloon next to value axes labels is displayed
     */
    public Boolean getValueLineBalloonEnabled() {
        return valueLineBalloonEnabled;
    }

    /**
     * Set valueLineBalloonEnabled to true if value balloon next to value axes labels should be displayed. If not set
     * the default value is false.
     *
     * @param valueLineBalloonEnabled valueLineBalloonEnabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setValueLineBalloonEnabled(Boolean valueLineBalloonEnabled) {
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
     * balloons on this line by setting valueLineBalloonEnabled to true. If not set the default value is false.
     *
     * @param valueLineEnabled valueLineEnabled option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setValueLineEnabled(Boolean valueLineEnabled) {
        this.valueLineEnabled = valueLineEnabled;
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
     * @return cursor
     */
    @StudioProperty(defaultValue = "[[category]]")
    public Cursor setCategoryBalloonText(String categoryBalloonText) {
        this.categoryBalloonText = categoryBalloonText;
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
     * @return cursor
     */
    @StudioProperty(defaultValue = "true")
    public Cursor setLeaveAfterTouch(Boolean leaveAfterTouch) {
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
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setLeaveCursor(Boolean leaveCursor) {
        this.leaveCursor = leaveCursor;
        return this;
    }

    /**
     * @return id of a graph
     */
    public String getLimitToGraph() {
        return limitToGraph;
    }

    /**
     * Sets an id of a graph. {@link CategoryAxis} cursor line will be limited to this graph instead of being drawn
     * through full height of plot area. Note, this works with serial chart only. Also, cursorPosition must be set to
     * middle.
     *
     * @param limitToGraph id of a graph
     * @return cursor
     */
    @StudioProperty
    public Cursor setLimitToGraph(String limitToGraph) {
        this.limitToGraph = limitToGraph;
        return this;
    }

    /**
     * @return true if the user can zoom-in value axes of a serial chart
     */
    public Boolean getValueZoomable() {
        return valueZoomable;
    }

    /**
     * Set valueZoomable to true if the user should zoom-in value axes of a serial chart. If not set the default
     * value is false.
     *
     * @param valueZoomable valueZoomable option
     * @return cursor
     */
    @StudioProperty(defaultValue = "false")
    public Cursor setValueZoomable(Boolean valueZoomable) {
        this.valueZoomable = valueZoomable;
        return this;
    }

    /**
     * @return tab index
     */
    public Integer getTabIndex() {
        return tabIndex;
    }

    /**
     * In case you set it to some number, the chart will set focus on chart cursor (works only with serial chart)
     * when user clicks tab key. When a focus is set user can move cursor using cursor keys. Note, not all browsers
     * and readers support this.
     *
     * @param tabIndex tab index
     */
    @StudioProperty
    public void setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
    }
}