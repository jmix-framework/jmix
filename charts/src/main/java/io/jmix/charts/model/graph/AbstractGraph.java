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

package io.jmix.charts.model.graph;

import io.jmix.charts.model.*;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.chart.impl.CoordinateChartModelImpl;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.date.DateFormat;
import io.jmix.charts.model.legend.AbstractLegend;
import io.jmix.charts.model.legend.Legend;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.meta.*;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the visualization of the data in following types: line, column, step line, smoothed line, olhc and
 * candlestick.
 * <br>
 * See documentation for properties of AmGraph JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmGraph">http://docs.amcharts.com/3/javascriptcharts/AmGraph</a>
 */
@SuppressWarnings("unchecked")
@StudioProperties(groups = {
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "alphaField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "bulletField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "bulletSizeField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "classNameField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "closeField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "colorField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "customBulletField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "dashLengthField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "descriptionField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "errorField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "fillColorsField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "gapField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "highField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "labelColorField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "lineColorField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "lowField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "openField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "patternField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "urlField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "valueField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "xField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "yField"})
})
public class AbstractGraph<T extends AbstractGraph> extends AbstractChartObject {

    private static final long serialVersionUID = 3973480345155361978L;

    private String accessibleLabel;

    private String alphaField;

    private Boolean animationPlayed;

    private Balloon balloon;

    private Color balloonColor;

    private JsFunction balloonFunction;

    private String balloonText;

    private Boolean behindColumns;

    private BulletType bullet;

    private Double bulletAlpha;

    private String bulletAxis;

    private Double bulletBorderAlpha;

    private Color bulletBorderColor;

    private Integer bulletBorderThickness;

    private Color bulletColor;

    private String bulletField;

    private Integer bulletHitAreaSize;

    private Integer bulletOffset;

    private Integer bulletSize;

    private String bulletSizeField;

    private String classNameField;

    private String closeField;

    private Boolean clustered;

    private Color color;

    private String colorField;

    private String columnIndexField;

    private Double columnWidth;

    private Boolean connect;

    private Integer cornerRadiusTop;

    private Double cursorBulletAlpha;

    private String customBullet;

    private String customBulletField;

    private String customMarker;

    private Integer dashLength;

    private String dashLengthField;

    private DateFormat dateFormat;

    private String descriptionField;

    private String errorField;

    private Double fillAlphas;

    private List<Color> fillColors;

    private String fillColorsField;

    private String fillToAxis;

    private String fillToGraph;

    private Integer fixedColumnWidth;

    private Integer fontSize;

    private Boolean forceGap;

    private String gapField;

    private Double gapPeriod;

    private GradientOrientation gradientOrientation;

    private Boolean hidden;

    private Integer hideBulletsCount;

    private String highField;

    private String id;

    private Boolean includeInMinMax;

    private String labelAnchor;

    private String labelColorField;

    private JsFunction labelFunction;

    private Integer labelOffset;

    private ValueLabelPosition labelPosition;

    private Integer labelRotation;

    private String labelText;

    private Double legendAlpha;

    private Color legendColor;

    private JsFunction legendColorFunction;

    private String legendPeriodValueText;

    private String legendValueText;

    private Double lineAlpha;

    private Color lineColor;

    private String lineColorField;

    private Integer lineThickness;

    private String lowField;

    private MarkerType markerType;

    private Integer maxBulletSize;

    private Integer minBulletSize;

    private Integer minDistance;

    private Double negativeBase;

    private Double negativeFillAlphas;

    private List<Color> negativeFillColors;

    private Double negativeLineAlpha;

    private Color negativeLineColor;

    private Boolean newStack;

    private Boolean noStepRisers;

    private String openField;

    private Pattern pattern;

    private String patternField;

    private Integer periodSpan;

    private PointPosition pointPosition;

    private Integer precision;

    private Boolean proCandlesticks;

    private Boolean showAllValueLabels;

    private Boolean showBalloon;

    private ShowPositionOnCandle showBalloonAt;

    private ShowPositionOnCandle showBulletsAt;

    private Boolean showHandOnHover;

    private Boolean showOnAxis;

    private Boolean stackable;

    private StepDirection stepDirection;

    private Boolean switchable;

    private Integer tabIndex;

    private String title;

    private Integer topRadius;

    private GraphType type;

    private String urlField;

    private String urlTarget;

    private Boolean useLineColorForBulletBorder;

    private Boolean useNegativeColorIfDown;

    private String valueAxis;

    private String valueField;

    private Boolean visibleInLegend;

    private String xAxis;

    private String xField;

    private String yAxis;

    private String yField;

    /**
     * @return true if animation played is enabled
     */
    public Boolean getAnimationPlayed() {
        return animationPlayed;
    }

    /**
     * If you set animationPlayed to true before chart is drawn, the animation of this graph won't be played. If not
     * set the default value is false.
     *
     * @param animationPlayed animation played option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setAnimationPlayed(Boolean animationPlayed) {
        this.animationPlayed = animationPlayed;
        return (T) this;
    }

    /**
     * @return graph title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets graph title
     *
     * @param title graph title string
     * @return graph
     */
    @StudioProperty
    public T setTitle(String title) {
        this.title = title;
        return (T) this;
    }

    /**
     * @return graph type
     */
    public GraphType getType() {
        return type;
    }

    /**
     * Sets type of the graph. Possible values are: "line", "column", "step", "smoothedLine", "candlestick", "ohlc". XY
     * and Radar charts can only display "line" type graphs. If not set the default value is LINE.
     *
     * @param type graph type
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "LINE")
    public T setType(GraphType type) {
        this.type = type;
        return (T) this;
    }

    /**
     * @return value field
     */
    public String getValueField() {
        return valueField;
    }

    /**
     * Sets name of the value field in your data provider.
     *
     * @param valueField value field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setValueField(String valueField) {
        this.valueField = valueField;
        return (T) this;
    }

    /**
     * @return name of the X field
     */
    public String getXField() {
        return xField;
    }

    /**
     * Sets name of the X field in your data provider. XY chart only.
     *
     * @param xField X field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setXField(String xField) {
        this.xField = xField;
        return (T) this;
    }

    /**
     * @return name of the Y field
     */
    public String getYField() {
        return yField;
    }

    /**
     * Sets name of the Y field in your data provider. XY chart only.
     *
     * @param yField name of the Y field
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setYField(String yField) {
        this.yField = yField;
        return (T) this;
    }

    /**
     * @return alpha field
     */
    public String getAlphaField() {
        return alphaField;
    }

    /**
     * Sets name of the alpha field from your data provider.
     *
     * @param alphaField alpha field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setAlphaField(String alphaField) {
        this.alphaField = alphaField;
        return (T) this;
    }

    /**
     * @return value balloon color
     */
    public Color getBalloonColor() {
        return balloonColor;
    }

    /**
     * Sets value balloon color. Will use graph or data item color if not set.
     *
     * @param balloonColor value balloon color
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setBalloonColor(Color balloonColor) {
        this.balloonColor = balloonColor;
        return (T) this;
    }

    /**
     * @return balloon text
     */
    public String getBalloonText() {
        return balloonText;
    }

    /**
     * Sets balloon text. You can use tags like [[value]], [[description]], [[percents]], [[open]], [[category]] or any
     * other field name from your data provider. HTML tags can also be used. If not set the default value is
     * "[[value]]".
     *
     * @param balloonText balloon text
     * @return graph
     */
    @StudioProperty(defaultValue = "[[value]]")
    public T setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return (T) this;
    }

    /**
     * @return true if the line graph is placed behind column graphs
     */
    public Boolean getBehindColumns() {
        return behindColumns;
    }

    /**
     * Set behindColumns to true if the line graph should be placed behind column graphs. If not set the default
     * value is false;
     *
     * @param behindColumns behind columns option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setBehindColumns(Boolean behindColumns) {
        this.behindColumns = behindColumns;
        return (T) this;
    }

    /**
     * @return bullet type
     */
    public BulletType getBullet() {
        return bullet;
    }

    /**
     * Sets type of the bullets. If not set the default value is NONE.
     *
     * @param bullet bullet type
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NONE")
    public T setBullet(BulletType bullet) {
        this.bullet = bullet;
        return (T) this;
    }

    /**
     * @return opacity of bullets
     */
    public Double getBulletAlpha() {
        return bulletAlpha;
    }

    /**
     * Sets opacity of bullets. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param bulletAlpha opacity of bullets
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public T setBulletAlpha(Double bulletAlpha) {
        this.bulletAlpha = bulletAlpha;
        return (T) this;
    }

    /**
     * @return bullet border opacity
     */
    public Double getBulletBorderAlpha() {
        return bulletBorderAlpha;
    }

    /**
     * Sets	bullet border opacity. If not set the default value is 0.
     *
     * @param bulletBorderAlpha bullet border opacity
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public T setBulletBorderAlpha(Double bulletBorderAlpha) {
        this.bulletBorderAlpha = bulletBorderAlpha;
        return (T) this;
    }

    /**
     * @return bullet border color
     */
    public Color getBulletBorderColor() {
        return bulletBorderColor;
    }

    /**
     * Sets bullet border color. Will use lineColor if not set.
     *
     * @param bulletBorderColor bullet border color
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setBulletBorderColor(Color bulletBorderColor) {
        this.bulletBorderColor = bulletBorderColor;
        return (T) this;
    }

    /**
     * @return bullet border thickness
     */
    public Integer getBulletBorderThickness() {
        return bulletBorderThickness;
    }

    /**
     * Sets bullet border thickness. If not set the default value is 2.
     *
     * @param bulletBorderThickness bullet border thickness
     * @return graph
     */
    @StudioProperty(defaultValue = "2")
    public T setBulletBorderThickness(Integer bulletBorderThickness) {
        this.bulletBorderThickness = bulletBorderThickness;
        return (T) this;
    }

    /**
     * @return bullet color
     */
    public Color getBulletColor() {
        return bulletColor;
    }

    /**
     * Sets bullet color. Will use lineColor if not set.
     *
     * @param bulletColor bullet color
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setBulletColor(Color bulletColor) {
        this.bulletColor = bulletColor;
        return (T) this;
    }

    /**
     * @return bullet field
     */
    public String getBulletField() {
        return bulletField;
    }

    /**
     * Sets name of the bullet field from your data provider.
     *
     * @param bulletField bullet field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setBulletField(String bulletField) {
        this.bulletField = bulletField;
        return (T) this;
    }

    /**
     * @return distance from the actual data point to the bullet
     */
    public Integer getBulletOffset() {
        return bulletOffset;
    }

    /**
     * Sets bullet offset. Distance from the actual data point to the bullet. Can be used to place custom bullets above
     * the columns. If not set the default value is 0.
     *
     * @param bulletOffset bullet offset
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setBulletOffset(Integer bulletOffset) {
        this.bulletOffset = bulletOffset;
        return (T) this;
    }

    /**
     * @return bullet size
     */
    public Integer getBulletSize() {
        return bulletSize;
    }

    /**
     * Sets bullet size. If not set the default value is 8.
     *
     * @param bulletSize bullet size
     * @return graph
     */
    @StudioProperty(defaultValue = "8")
    public T setBulletSize(Integer bulletSize) {
        this.bulletSize = bulletSize;
        return (T) this;
    }

    /**
     * @return bullet size field
     */
    public String getBulletSizeField() {
        return bulletSizeField;
    }

    /**
     * Sets name of the bullet size field from your data provider.
     *
     * @param bulletSizeField bullet size field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setBulletSizeField(String bulletSizeField) {
        this.bulletSizeField = bulletSizeField;
        return (T) this;
    }

    /**
     * @return close field
     */
    public String getCloseField() {
        return closeField;
    }

    /**
     * Sets name of the close field (used by candlesticks and ohlc) from your data provider.
     *
     * @param closeField close field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setCloseField(String closeField) {
        this.closeField = closeField;
        return (T) this;
    }

    /**
     * @return true if clustered is enabled
     */
    public Boolean getClustered() {
        return clustered;
    }

    /**
     * Set clustered to false if you want to place this graph's columns in front of other columns. If you set to
     * true, the columns will be clustered next to each other. Note, clustering works only for graphs of type "column".
     * If not set the default value is true.
     *
     * @param clustered clustered option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setClustered(Boolean clustered) {
        this.clustered = clustered;
        return (T) this;
    }

    /**
     * @return color of value labels
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of value labels. Will use chart's color if not set.
     *
     * @param color color of value labels
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setColor(Color color) {
        this.color = color;
        return (T) this;
    }

    /**
     * @return color field
     */
    public String getColorField() {
        return colorField;
    }

    /**
     * Sets name of the color field in your data provider.
     *
     * @param colorField color field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setColorField(String colorField) {
        this.colorField = colorField;
        return (T) this;
    }

    /**
     * @return column relative width
     */
    public Double getColumnWidth() {
        return columnWidth;
    }

    /**
     * Sets column width for each graph individually. Value range is 0 - 1 (we set relative width, not pixel width here).
     *
     * @param columnWidth column relative width
     * @return graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public T setColumnWidth(Double columnWidth) {
        this.columnWidth = columnWidth;
        return (T) this;
    }

    /**
     * @return true if connect is enabled
     */
    public Boolean getConnect() {
        return connect;
    }

    /**
     * Specifies whether to connect data points if data is missing. This feature does not work with XY chart. If not
     * set the default value is true.
     *
     * @param connect connect option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setConnect(Boolean connect) {
        this.connect = connect;
        return (T) this;
    }

    /**
     * @return corner radius of column
     */
    public Integer getCornerRadiusTop() {
        return cornerRadiusTop;
    }

    /**
     * Sets corner radius of column. It can be set both, in pixels or in percents. The chart's depth and angle styles
     * must be set to 0. The default value is 0. Note, cornerRadiusTop will be applied for all corners of the column.
     * If not set the default value is 0.
     *
     * @param cornerRadiusTop corner radius
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setCornerRadiusTop(Integer cornerRadiusTop) {
        this.cornerRadiusTop = cornerRadiusTop;
        return (T) this;
    }

    /**
     * @return opacity of each graphs bullet
     */
    public Double getCursorBulletAlpha() {
        return cursorBulletAlpha;
    }

    /**
     * Sets opacity of each graphs bullet. If bulletsEnabled of Cursor is true, a bullet on each graph follows the
     * cursor. In case you want to disable these bullets for a certain graph, set opacity to 0. If not set the
     * default value is 1.
     *
     * @param cursorBulletAlpha opacity of each graphs bullet
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public T setCursorBulletAlpha(Double cursorBulletAlpha) {
        this.cursorBulletAlpha = cursorBulletAlpha;
        return (T) this;
    }

    /**
     * @return path to image of custom bullet
     */
    public String getCustomBullet() {
        return customBullet;
    }

    /**
     * Sets path to the image of custom bullet.
     *
     * @param customBullet path to image of custom bullet
     * @return graph
     */
    @StudioProperty
    public T setCustomBullet(String customBullet) {
        this.customBullet = customBullet;
        return (T) this;
    }

    /**
     * @return custom bullet field
     */
    public String getCustomBulletField() {
        return customBulletField;
    }

    /**
     * Sets name of the custom bullet field in your data provider.
     *
     * @param customBulletField custom bullet field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setCustomBulletField(String customBulletField) {
        this.customBulletField = customBulletField;
        return (T) this;
    }

    /**
     * @return path to the image for legend marker
     */
    public String getCustomMarker() {
        return customMarker;
    }

    /**
     * Sets path to the image for legend marker.
     *
     * @param customMarker path to the image
     * @return graph
     */
    @StudioProperty
    public T setCustomMarker(String customMarker) {
        this.customMarker = customMarker;
        return (T) this;
    }

    /**
     * @return dash length
     */
    public Integer getDashLength() {
        return dashLength;
    }

    /**
     * Sets dash length. If you set it to a value greater than 0, the graph line (or columns border) will be dashed.
     * If not set the default value is 0.
     *
     * @param dashLength dash length
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setDashLength(Integer dashLength) {
        this.dashLength = dashLength;
        return (T) this;
    }

    /**
     * @return dash length field
     */
    public String getDashLengthField() {
        return dashLengthField;
    }

    /**
     * Sets name of the dash length field in your data provider. dashLengthField adds a possibility to change graphs
     * line from solid to dashed on any data point. You can also make columns border dashed using this setting. Note,
     * this won't work with smoothedLine graph.
     *
     * @param dashLengthField dash length field
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setDashLengthField(String dashLengthField) {
        this.dashLengthField = dashLengthField;
        return (T) this;
    }

    /**
     * @return description field
     */
    public String getDescriptionField() {
        return descriptionField;
    }

    /**
     * Sets name of the description field in your dataProvider.
     *
     * @param descriptionField description field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setDescriptionField(String descriptionField) {
        this.descriptionField = descriptionField;
        return (T) this;
    }

    /**
     * @return error field
     */
    public String getErrorField() {
        return errorField;
    }

    /**
     * Sets name of error value field in your data provider.
     *
     * @param errorField error field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setErrorField(String errorField) {
        this.errorField = errorField;
        return (T) this;
    }

    /**
     * @return opacity of fill
     */
    public Double getFillAlphas() {
        return fillAlphas;
    }

    /**
     * Sets opacity of fill.
     *
     * @param fillAlphas opacity of fill
     * @return graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public T setFillAlphas(Double fillAlphas) {
        this.fillAlphas = fillAlphas;
        return (T) this;
    }

    /**
     * @return list of fill colors
     */
    public List<Color> getFillColors() {
        return fillColors;
    }

    /**
     * Sets list of fill colors. Will use lineColor if not set. Set more than one color to generate the gradient.
     *
     * @param fillColors list of fill colors
     * @return graph
     */
    @StudioCollection(xmlElement = "fillColors",
            itemXmlElement = "color",
            itemCaption = "Fill Color",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.ENUMERATION,
                            options = {"@link io.jmix.charts.model.Color"})
            })
    public T setFillColors(List<Color> fillColors) {
        this.fillColors = fillColors;
        return (T) this;
    }

    /**
     * @return name of the fill colors field
     */
    public String getFillColorsField() {
        return fillColorsField;
    }

    /**
     * Sets name of the fill colors field in your data provider. fillColorsField adds a possibility to change line
     * graphs fill color on any data point to create highlighted sections of the graph. Works only with
     * {@link SerialChartModelImpl}.
     *
     * @param fillColorsField name of the fill colors field
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setFillColorsField(String fillColorsField) {
        this.fillColorsField = fillColorsField;
        return (T) this;
    }

    /**
     * @return font size of value labels text
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets	size of value labels text. Will use chart's fontSize if not set.
     *
     * @param fontSize font size
     * @return graph
     */
    @StudioProperty
    public T setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return (T) this;
    }

    /**
     * @return true if forceGap is enabled
     */
    public Boolean getForceGap() {
        return forceGap;
    }

    /**
     * Set forceGap to true if you want the graph should always break the line if the distance in time between two
     * adjacent data points is bigger than "gapPeriod * minPeriod", even if connect property is set to true. If not
     * set the default value is false.
     *
     * @param forceGap force gap
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setForceGap(Boolean forceGap) {
        this.forceGap = forceGap;
        return (T) this;
    }

    /**
     * @return gradient orientation
     */
    public GradientOrientation getGradientOrientation() {
        return gradientOrientation;
    }

    /**
     * Sets orientation of the gradient fills (only for "column" graph type). Possible values are "vertical" and
     * "horizontal". If not set the default value is VERTICAL.
     *
     * @param gradientOrientation gradient orientation
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "VERTICAL")
    public T setGradientOrientation(GradientOrientation gradientOrientation) {
        this.gradientOrientation = gradientOrientation;
        return (T) this;
    }

    /**
     * @return true if graph is hidden
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * Set hidden to true if the graph should be hidden. If not set the default value is false.
     *
     * @param hidden hidden option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setHidden(Boolean hidden) {
        this.hidden = hidden;
        return (T) this;
    }

    /**
     * @return hide bullets count
     */
    public Integer getHideBulletsCount() {
        return hideBulletsCount;
    }

    /**
     * Sets hide bullets count. If there are more data points than hideBulletsCount, the bullets will not be shown. 0
     * means the bullets will always be visible. If not set the default value is 0.
     *
     * @param hideBulletsCount hide bullets count
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setHideBulletsCount(Integer hideBulletsCount) {
        this.hideBulletsCount = hideBulletsCount;
        return (T) this;
    }

    /**
     * @return high field
     */
    public String getHighField() {
        return highField;
    }

    /**
     * Sets name of the high field (used by candlesticks and ohlc) in your data provider.
     *
     * @param highField high field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setHighField(String highField) {
        this.highField = highField;
        return (T) this;
    }

    /**
     * @return id of the graph
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of a graph. It is not required to set one, unless you want to use this graph for as your
     * scrollbar's graph and need to indicate which graph should be used.
     *
     * @param id id of the graph
     * @return graph
     */
    @StudioProperty
    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

    /**
     * @return true if includeInMinMax is enabled
     */
    public Boolean getIncludeInMinMax() {
        return includeInMinMax;
    }

    /**
     * Set includeInMinMax to true if this graph should be included when calculating minimum and maximum value of the
     * axis. If not set the default value is true.
     *
     * @param includeInMinMax include in min max option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setIncludeInMinMax(Boolean includeInMinMax) {
        this.includeInMinMax = includeInMinMax;
        return (T) this;
    }

    /**
     * @return label color field
     */
    public String getLabelColorField() {
        return labelColorField;
    }

    /**
     * Sets name of label color field in your data provider.
     *
     * @param labelColorField label color field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setLabelColorField(String labelColorField) {
        this.labelColorField = labelColorField;
        return (T) this;
    }

    /**
     * @return value label position
     */
    public ValueLabelPosition getLabelPosition() {
        return labelPosition;
    }

    /**
     * Sets the position of value label. Possible values are: "bottom", "top", "right", "left", "inside", "middle".
     * Sometimes position is changed by the chart, depending on a graph type, rotation, etc. If not set the default
     * value is TOP.
     *
     * @param labelPosition value label position
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "TOP")
    public T setLabelPosition(ValueLabelPosition labelPosition) {
        this.labelPosition = labelPosition;
        return (T) this;
    }

    /**
     * @return value label text
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Sets value label text. You can use tags like [[value]], [[description]], [[percents]], [[open", [[category".
     *
     * @param labelText value label text
     * @return graph
     */
    @StudioProperty
    public T setLabelText(String labelText) {
        this.labelText = labelText;
        return (T) this;
    }

    /**
     * @return legend marker opacity
     */
    public Double getLegendAlpha() {
        return legendAlpha;
    }

    /**
     * Sets legend marker opacity. Will use lineAlpha if not set. Value range is 0 - 1. If not set the default value
     * is 1.
     *
     * @param legendAlpha legend marker opacity
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public T setLegendAlpha(Double legendAlpha) {
        this.legendAlpha = legendAlpha;
        return (T) this;
    }

    /**
     * @return legend marker color
     */
    public Color getLegendColor() {
        return legendColor;
    }

    /**
     * Sets legend marker color. Will use lineColor if not set.
     *
     * @param legendColor legend marker color
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setLegendColor(Color legendColor) {
        this.legendColor = legendColor;
        return (T) this;
    }

    /**
     * @return legend period value text
     */
    public String getLegendPeriodValueText() {
        return legendPeriodValueText;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when user is not hovering above any
     * data point. The tags should be made out of two parts - the name of a field (value / open / close / high / low)
     * and the value of the period you want to be show - open / close / high / low / sum / average / count. For
     * example: [[value.sum]] means that sum of all data points of value field in the selected period will be displayed.
     *
     * @param legendPeriodValueText legend period value text
     * @return graph
     */
    @StudioProperty
    public T setLegendPeriodValueText(String legendPeriodValueText) {
        this.legendPeriodValueText = legendPeriodValueText;
        return (T) this;
    }

    /**
     * @return legend value text
     */
    public String getLegendValueText() {
        return legendValueText;
    }

    /**
     * Sets legend value text. You can use tags like [[value]], [[description]], [[percents]], [[open]], [[category]]
     * You can also use custom fields from your data provider. If not set, uses {@link AbstractLegend#valueText}.
     *
     * @param legendValueText legend value text
     * @return graph
     */
    @StudioProperty
    public T setLegendValueText(String legendValueText) {
        this.legendValueText = legendValueText;
        return (T) this;
    }

    /**
     * @return opacity of the line (or column border)
     */
    public Double getLineAlpha() {
        return lineAlpha;
    }

    /**
     * Sets	opacity of the line (or column border). Value range is 0 - 1.
     *
     * @param lineAlpha opacity of the line (or column border)
     * @return graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public T setLineAlpha(Double lineAlpha) {
        this.lineAlpha = lineAlpha;
        return (T) this;
    }

    /**
     * @return color of the line (or column border)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets color of the line (or column border). If you do not set any, the color from
     * {@link CoordinateChartModelImpl#colors} list will be used for each subsequent graph.
     *
     * @param lineColor color of the line (or column border)
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        return (T) this;
    }

    /**
     * @return name of the line color field
     */
    public String getLineColorField() {
        return lineColorField;
    }

    /**
     * Sets name of the line color field in your data provider. lineColorField adds a possibility to change graphs’ line
     * color on any data point to create highlighted sections of the graph. Works only with
     * {@link SerialChartModelImpl}.
     *
     * @param lineColorField name of the line color field
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setLineColorField(String lineColorField) {
        this.lineColorField = lineColorField;
        return (T) this;
    }

    /**
     * @return thickness of the graph line (or column border)
     */
    public Integer getLineThickness() {
        return lineThickness;
    }

    /**
     * Sets thickness of the graph line (or column border). If not set the default value is 1.
     *
     * @param lineThickness thickness of the graph line (or column border)
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    public T setLineThickness(Integer lineThickness) {
        this.lineThickness = lineThickness;
        return (T) this;
    }

    /**
     * @return low field
     */
    public String getLowField() {
        return lowField;
    }

    /**
     * Sets name of the low field (used by candlesticks and ohlc) in your data provider.
     *
     * @param lowField low field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setLowField(String lowField) {
        this.lowField = lowField;
        return (T) this;
    }

    /**
     * @return legend marker type
     */
    public MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * Sets legend marker type. You can set legend marker (key) type for individual graphs. Possible values are:
     * square, circle, diamond, triangleUp, triangleDown, triangleLeft, triangleDown, bubble, line, none.
     *
     * @param markerType legend marker type
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public T setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
        return (T) this;
    }

    /**
     * @return maximum bullet size
     */
    public Integer getMaxBulletSize() {
        return maxBulletSize;
    }

    /**
     * Sets size of the bullet which value is the biggest (XY chart). If not set the default value is 50.
     *
     * @param maxBulletSize maximum bullet size
     * @return graph
     */
    @StudioProperty(defaultValue = "50")
    public T setMaxBulletSize(Integer maxBulletSize) {
        this.maxBulletSize = maxBulletSize;
        return (T) this;
    }

    /**
     * @return minimum bullet size
     */
    public Integer getMinBulletSize() {
        return minBulletSize;
    }

    /**
     * Sets minimum size of the bullet (XY chart). If not set the default value is 4.
     *
     * @param minBulletSize minimum bullet size
     * @return graph
     */
    @StudioProperty(defaultValue = "4")
    public T setMinBulletSize(Integer minBulletSize) {
        this.minBulletSize = minBulletSize;
        return (T) this;
    }

    /**
     * @return negative base
     */
    public Double getNegativeBase() {
        return negativeBase;
    }

    /**
     * Sets different base value at which colors should be changed to negative colors if you use different colors
     * for your negative values. If not set the default value is 0.
     *
     * @param negativeBase negative base
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setNegativeBase(Double negativeBase) {
        this.negativeBase = negativeBase;
        return (T) this;
    }

    /**
     * @return opacity of negative part of the graph
     */
    public Double getNegativeFillAlphas() {
        return negativeFillAlphas;
    }

    /**
     * Sets fill opacity of negative part of the graph. Will use fillAlphas if not set.
     *
     * @param negativeFillAlphas negative fill alphas
     * @return graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public T setNegativeFillAlphas(Double negativeFillAlphas) {
        this.negativeFillAlphas = negativeFillAlphas;
        return (T) this;
    }

    /**
     * @return negative fill colors
     */
    public List<Color> getNegativeFillColors() {
        return negativeFillColors;
    }

    /**
     * Sets fill color of negative part of the graph. Will use fillColors if not set.
     *
     * @param negativeFillColors negative fill colors
     * @return graph
     */
    @StudioCollection(xmlElement = "negativeFillColors",
            itemXmlElement = "color",
            itemCaption = "Negative Fill Color",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.ENUMERATION,
                            options = {"@link io.jmix.charts.model.Color"})
            })
    public T setNegativeFillColors(List<Color> negativeFillColors) {
        this.negativeFillColors = negativeFillColors;
        return (T) this;
    }

    /**
     * @return opacity of the negative portion of the line (or column border)
     */
    public Double getNegativeLineAlpha() {
        return negativeLineAlpha;
    }

    /**
     * Sets opacity of the negative portion of the line (or column border). Value range is 0 - 1. If not set the
     * default value is 1.
     *
     * @param negativeLineAlpha opacity
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public T setNegativeLineAlpha(Double negativeLineAlpha) {
        this.negativeLineAlpha = negativeLineAlpha;
        return (T) this;
    }

    /**
     * @return color of the line (or column) when the values are negative
     */
    public Color getNegativeLineColor() {
        return negativeLineColor;
    }

    /**
     * Sets color of the line (or column) when the values are negative. In case the graph type is candlestick or ohlc,
     * negativeLineColor is used when close value is less then open value.
     *
     * @param negativeLineColor color
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public T setNegativeLineColor(Color negativeLineColor) {
        this.negativeLineColor = negativeLineColor;
        return (T) this;
    }

    /**
     * @return true if noStepRisers is enabled
     */
    public Boolean getNoStepRisers() {
        return noStepRisers;
    }

    /**
     * Set noStepRisers to true if you want to have a step line graph without risers. If not set the default value is
     * false.
     *
     * @param noStepRisers noStepRisers option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setNoStepRisers(Boolean noStepRisers) {
        this.noStepRisers = noStepRisers;
        return (T) this;
    }

    /**
     * @return open field
     */
    public String getOpenField() {
        return openField;
    }

    /**
     * Sets name of the open field (used by floating columns, candlesticks and ohlc) in your data provider.
     *
     * @param openField open field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setOpenField(String openField) {
        this.openField = openField;
        return (T) this;
    }

    /**
     * @return pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets pattern to the graph. If you want to have individual patterns for each column, define patterns in data
     * provider and set graph patternField property. Check amcharts/patterns folder for some patterns. You can create
     * your own patterns and use them. 3D bar/pie charts won't work properly with patterns.
     *
     * @param pattern pattern
     * @return graph
     */
    @StudioElement
    public T setPattern(Pattern pattern) {
        this.pattern = pattern;
        return (T) this;
    }

    /**
     * @return pattern field
     */
    public String getPatternField() {
        return patternField;
    }

    /**
     * Sets field name in your data provider which holds pattern information. Check amcharts/patterns folder for
     * some patterns. You can create your own patterns and use them. 3D bar/Pie charts won't work properly with
     * patterns.
     *
     * @param patternField pattern field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setPatternField(String patternField) {
        this.patternField = patternField;
        return (T) this;
    }

    /**
     * @return period span
     */
    public Integer getPeriodSpan() {
        return periodSpan;
    }

    /**
     * Sets how many periods one horizontal line should span. periodSpan can be used by step graphs. If not set the
     * default value is 1.
     *
     * @param periodSpan period span
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    public T setPeriodSpan(Integer periodSpan) {
        this.periodSpan = periodSpan;
        return (T) this;
    }

    /**
     * @return point position
     */
    public PointPosition getPointPosition() {
        return pointPosition;
    }

    /**
     * Specifies where data points should be placed - on the beginning of the period (day, hour, etc) or in the
     * middle (only when parseDates property of categoryAxis is set to true). This setting affects Serial chart only.
     * Possible values are "start", "middle" and "end". If not set the default value is MIDDLE.
     *
     * @param pointPosition point position
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "MIDDLE")
    public T setPointPosition(PointPosition pointPosition) {
        this.pointPosition = pointPosition;
        return (T) this;
    }

    /**
     * @return true if showAllValueLabels is enabled
     */
    public Boolean getShowAllValueLabels() {
        return showAllValueLabels;
    }

    /**
     * If graph's type is column and labelText is set, graph hides labels which do not fit into the column's space
     * or go outside plot area. If you don't want these labels to be hidden, set this to true. If not set the
     * default value is false.
     *
     * @param showAllValueLabels show all value labels option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setShowAllValueLabels(Boolean showAllValueLabels) {
        this.showAllValueLabels = showAllValueLabels;
        return (T) this;
    }

    /**
     * @return true if showBalloon is enabled
     */
    public Boolean getShowBalloon() {
        return showBalloon;
    }

    /**
     * Set showBalloon to true if the value balloon of this graph should be shown when mouse is over data item or
     * chart's indicator is over some series. If not set the default value is true.
     *
     * @param showBalloon show balloon option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setShowBalloon(Boolean showBalloon) {
        this.showBalloon = showBalloon;
        return (T) this;
    }

    /**
     * @return graphs value at which cursor is shown
     */
    public ShowPositionOnCandle getShowBalloonAt() {
        return showBalloonAt;
    }

    /**
     * Specifies graphs value at which cursor is shown. This is only important for candlestick and ohlc charts, also
     * if column chart has "open" value. Possible values are: "open", "close", "high", "low". "top" and "bottom"
     * values will glue the balloon to top/bottom of the plot area. If not set the default value is CLOSE.
     *
     * @param showBalloonAt graphs value at which cursor is shown
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "CLOSE")
    public T setShowBalloonAt(ShowPositionOnCandle showBalloonAt) {
        this.showBalloonAt = showBalloonAt;
        return (T) this;
    }

    /**
     * @return true if mouse pointer changes to hand when hovering the graph
     */
    public Boolean getShowHandOnHover() {
        return showHandOnHover;
    }

    /**
     * Set showHandOnHover to true if you want mouse pointer to change to hand when hovering the graph. If not set
     * the default value is false.
     *
     * @param showHandOnHover showHandOnHover option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setShowHandOnHover(Boolean showHandOnHover) {
        this.showHandOnHover = showHandOnHover;
        return (T) this;
    }

    /**
     * @return true if this graph included to stacking.
     */
    public Boolean getStackable() {
        return stackable;
    }

    /**
     * Set stackable to false if you want exclude this graph from stacking. Note, the value axis of this graph should
     * has stack types like "regular" or "100%". If not set the default value is true.
     *
     * @param stackable stackable option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setStackable(Boolean stackable) {
        this.stackable = stackable;
        return (T) this;
    }

    /**
     * @return step direction
     */
    public StepDirection getStepDirection() {
        return stepDirection;
    }

    /**
     * Specifies to which direction step should be drawn. Step graph only. If not set the default value is RIGHT.
     *
     * @param stepDirection step direction
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "RIGHT")
    public T setStepDirection(StepDirection stepDirection) {
        this.stepDirection = stepDirection;
        return (T) this;
    }

    /**
     * @return the URL field
     */
    public String getUrlField() {
        return urlField;
    }

    /**
     * Sets name of the URL field in your data provider.
     *
     * @param urlField the URL field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setUrlField(String urlField) {
        this.urlField = urlField;
        return (T) this;
    }

    /**
     * @return the URL target
     */
    public String getUrlTarget() {
        return urlTarget;
    }

    /**
     * Sets target to open URLs in, i.e. "_blank", "_top", etc.
     *
     * @param urlTarget the URL target
     * @return graph
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"_blank", "_parent", "_self", "_top"})
    public T setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
        return (T) this;
    }

    /**
     * @return true if graphs uses negative colors for lines, bullets or columns if previous value is bigger than
     * current value
     */
    public Boolean getUseNegativeColorIfDown() {
        return useNegativeColorIfDown;
    }

    /**
     * If negativeLineColor and/or negativeFillColors are set and useNegativeColorIfDown is set to true, the line,
     * step and column graphs will use these colors for lines, bullets or columns if previous value is bigger than
     * current value. In case you set openField for the graph, the graph will compare current value with openField
     * value instead of comparing to previous value. If not set the default value is false.
     *
     * @param useNegativeColorIfDown useNegativeColorIfDown option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setUseNegativeColorIfDown(Boolean useNegativeColorIfDown) {
        this.useNegativeColorIfDown = useNegativeColorIfDown;
        return (T) this;
    }

    /**
     * @return true if the graph should be shown in the legend
     */
    public Boolean getVisibleInLegend() {
        return visibleInLegend;
    }

    /**
     * Set visibleInLegend to false if the graph shouldn't be shown in the {@link Legend}. If not set the default
     * value is true.
     *
     * @param visibleInLegend visible in legend option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setVisibleInLegend(Boolean visibleInLegend) {
        this.visibleInLegend = visibleInLegend;
        return (T) this;
    }

    /**
     * @return bullet axis
     */
    public String getBulletAxis() {
        return bulletAxis;
    }

    /**
     * bulletAxis value is used when you are building error chart. Error chart is a regular serial or XY chart with
     * bullet type set to "xError" or "yError". The graph should know which axis should be used to determine the size
     * of this bullet - that's when bulletAxis should be set. Besides that, you should also set graph errorField. You
     * can also use other bullet types with this feature too. For example, if you set bulletAxis for XY chart, the
     * size of a bullet will change as you zoom the chart.
     *
     * @param bulletAxis bullet axis
     * @return graph
     */
    @StudioProperty
    public T setBulletAxis(String bulletAxis) {
        this.bulletAxis = bulletAxis;
        return (T) this;
    }

    /**
     * @return value axis id
     */
    public String getValueAxis() {
        return valueAxis;
    }

    /**
     * Specifies which value axis the graph will use. Will use the first value axis if not set. You can set value
     * axis id.
     *
     * @param valueAxis value axis id
     * @return graph
     */
    @StudioProperty
    public T setValueAxis(String valueAxis) {
        this.valueAxis = valueAxis;
        return (T) this;
    }

    /**
     * @return horizontal value axis id
     */
    public String getXAxis() {
        return xAxis;
    }

    /**
     * Sets a horizontal value axis to attach graph to. XY chart only.
     *
     * @param xAxis horizontal value axis id
     * @return graph
     */
    @StudioProperty
    public T setXAxis(String xAxis) {
        this.xAxis = xAxis;
        return (T) this;
    }

    /**
     * @return vertical value axis id
     */
    public String getYAxis() {
        return yAxis;
    }

    /**
     * Sets a vertical value axis id to attach graph to. XY chart only.
     *
     * @param yAxis vertical value axis id
     * @return graph
     */
    @StudioProperty
    public T setYAxis(String yAxis) {
        this.yAxis = yAxis;
        return (T) this;
    }

    /**
     * @return JS function, the graph will call
     */
    public JsFunction getBalloonFunction() {
        return balloonFunction;
    }

    /**
     * Sets JS function, the graph will call it and pass GraphDataItem and AmGraph objects to it. This
     * function should return a string which will be displayed in a balloon.
     *
     * @param balloonFunction JS function
     * @return graph
     */
    public T setBalloonFunction(JsFunction balloonFunction) {
        this.balloonFunction = balloonFunction;
        return (T) this;
    }

    /**
     * @return fill to graph
     */
    public String getFillToGraph() {
        return fillToGraph;
    }

    /**
     * You can set another graph here and if fillAlpha is greater than 0 , the area from this graph to fillToGraph will
     * be filled (instead of filling the area to the X axis). This feature is not supported by smoothedLine graphs
     * and Radar chart.
     *
     * @param fillToGraph fill to graph
     * @return graph
     */
    @StudioProperty
    public T setFillToGraph(String fillToGraph) {
        this.fillToGraph = fillToGraph;
        return (T) this;
    }

    /**
     * @return id of axis
     */
    public String getFillToAxis() {
        return fillToAxis;
    }

    /**
     * XY chart only. If you set this property to id of your X or Y axis, and the fillAlphas is greater than 0, the
     * area between graph and axis will be filled with color.
     *
     * @param fillToAxis id of axis
     * @return graph
     */
    @StudioProperty
    public T setFillToAxis(String fillToAxis) {
        this.fillToAxis = fillToAxis;
        return (T) this;
    }

    /**
     * @return column width, in pixels
     */
    public Integer getFixedColumnWidth() {
        return fixedColumnWidth;
    }

    /**
     * Sets column width, in pixels. If you set this property, columns will be of a fixed width and won't adjust to the
     * available space.
     *
     * @param fixedColumnWidth column width
     * @return graph
     */
    @StudioProperty
    public T setFixedColumnWidth(Integer fixedColumnWidth) {
        this.fixedColumnWidth = fixedColumnWidth;
        return (T) this;
    }

    /**
     * @return gap field
     */
    public String getGapField() {
        return gapField;
    }

    /**
     * Sets name of the gap field in your data provider. You can force graph to show gap at a desired data point using
     * this feature. This feature does not work with XY chart.
     *
     * @param gapField gap field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setGapField(String gapField) {
        this.gapField = gapField;
        return (T) this;
    }

    /**
     * @return gap period
     */
    public Double getGapPeriod() {
        return gapPeriod;
    }

    /**
     * Specifies when graph should display gap - if the time difference between data points is bigger than duration
     * of "minPeriod * gapPeriod", and connect property of a graph is set to false, graph will display gap. If not
     * set the default value is 1.1.
     *
     * @param gapPeriod gap period
     * @return graph
     */
    @StudioProperty(defaultValue = "1.1")
    public T setGapPeriod(Double gapPeriod) {
        this.gapPeriod = gapPeriod;
        return (T) this;
    }

    /**
     * @return label anchor
     */
    public String getLabelAnchor() {
        return labelAnchor;
    }

    /**
     * Sets data label text anchor. If not set the default value is "auto".
     *
     * @param labelAnchor label anchor string
     * @return graph
     */
    @StudioProperty(defaultValue = "auto")
    public T setLabelAnchor(String labelAnchor) {
        this.labelAnchor = labelAnchor;
        return (T) this;
    }

    /**
     * @return label function
     */
    public JsFunction getLabelFunction() {
        return labelFunction;
    }

    /**
     * Sets JS function to format labels of data items in any way you want. Graph will call this function and pass
     * reference to GraphDataItem and formatted text as attributes. This function should return string which will be
     * displayed as label.
     *
     * @param labelFunction label function
     * @return graph
     */
    public T setLabelFunction(JsFunction labelFunction) {
        this.labelFunction = labelFunction;
        return (T) this;
    }

    public Integer getLabelOffset() {
        return labelOffset;
    }

    /**
     * Sets offset of data label. If not set the default value is 0.
     *
     * @param labelOffset label offset
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setLabelOffset(Integer labelOffset) {
        this.labelOffset = labelOffset;
        return (T) this;
    }

    /**
     * @return minimum distance, in pixels
     */
    public Integer getMinDistance() {
        return minDistance;
    }

    /**
     * Sets minimum distance. Based on this property the graph will omit some of the lines (if the distance between
     * points is less that minDistance, in pixels). It is useful if you have really lots of data points. This will
     * not affect the bullets or indicator in anyway, so the user will not see any difference (unless you set
     * minValue to a bigger value, let say 5), but will increase performance as less lines will be drawn. By setting
     * value to a bigger number you can also make your lines look less jagged. If not set the default value is 1.
     *
     * @param minDistance minimum distance, in pixels
     * @return graph
     */
    @StudioProperty(defaultValue = "1")
    public T setMinDistance(Integer minDistance) {
        this.minDistance = minDistance;
        return (T) this;
    }

    /**
     * @return true if newStack is enabled
     */
    public Boolean getNewStack() {
        return newStack;
    }

    /**
     * Set newStack to true if column chart should begin new stack. This allows having Clustered and Stacked
     * column/bar chart. If not set the default value is false.
     *
     * @param newStack newStack option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setNewStack(Boolean newStack) {
        this.newStack = newStack;
        return (T) this;
    }

    /**
     * @return position on candle
     */
    public ShowPositionOnCandle getShowBulletsAt() {
        return showBulletsAt;
    }

    /**
     * Works with candlestick graph type, you can set it to open, close, high, low. If you set it to high, the
     * events will be shown at the tip of the high line. If not set the default value is CLOSE.
     *
     * @param showBulletsAt position on candle
     * @return graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "CLOSE")
    public T setShowBulletsAt(ShowPositionOnCandle showBulletsAt) {
        this.showBulletsAt = showBulletsAt;
        return (T) this;
    }

    /**
     * @return true if showOnAxis is enabled
     */
    public Boolean getShowOnAxis() {
        return showOnAxis;
    }

    /**
     * If you set showOnAxis to true, the cylinder will be lowered down so that the center of it's bottom circle
     * would be right on category axis. It can only be used together with topRadius (when columns look like cylinders).
     * If not set the default value is false.
     *
     * @param showOnAxis showOnAxis option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setShowOnAxis(Boolean showOnAxis) {
        this.showOnAxis = showOnAxis;
        return (T) this;
    }

    /**
     * @return true if the graph will be hidden by user clicks on legend entry
     */
    public Boolean getSwitchable() {
        return switchable;
    }

    /**
     * If you set switchable to false, the graph will not be hidden when user clicks on legend entry. If not set the
     * default value is true.
     *
     * @param switchable switchable option
     * @return graph
     */
    @StudioProperty(defaultValue = "true")
    public T setSwitchable(Boolean switchable) {
        this.switchable = switchable;
        return (T) this;
    }

    /**
     * @return top radius
     */
    public Integer getTopRadius() {
        return topRadius;
    }

    /**
     * If you set this to 1, columns will become cylinders (must set depth3D and angle properties of a chart to
     * greater than 0 values in order this to be visible). you can make columns look like cones (set topRadius to 0)
     * or even like some glasses (set to bigger than 1). It is strongly recommend setting grid opacity to 0 in order
     * this to look good.
     *
     * @param topRadius top radius
     * @return graph
     */
    @StudioProperty
    public T setTopRadius(Integer topRadius) {
        this.topRadius = topRadius;
        return (T) this;
    }

    /**
     * @return date format for balloons
     */
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format. Used to format balloons if value axis is date-based. If not set the default value is "MMM
     * DD, YYYY".
     *
     * @param dateFormat date format
     * @return graph
     */
    public T setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        return (T) this;
    }

    /**
     * @return label rotation
     */
    public Integer getLabelRotation() {
        return labelRotation;
    }

    /**
     * Sets the rotation of a data label. If not set the default value is 0.
     *
     * @param labelRotation label rotation
     * @return graph
     */
    @StudioProperty(defaultValue = "0")
    public T setLabelRotation(Integer labelRotation) {
        this.labelRotation = labelRotation;
        return (T) this;
    }

    /**
     * @return precision of values
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * Sets precision of values. Will use chart's precision if not set any.
     *
     * @param precision precision
     * @return graph
     */
    @StudioProperty
    public T setPrecision(Integer precision) {
        this.precision = precision;
        return (T) this;
    }

    /**
     * @return true if proCandlesticks is enabled
     */
    public Boolean getProCandlesticks() {
        return proCandlesticks;
    }

    /**
     * If proCandlesticks is set to true, candlesticks will be colored in a different manner - if current close is
     * less than current open, the candlestick will be empty, otherwise - filled with color. If previous close is
     * less than current close, the candlestick will use positive color, otherwise - negative color. If not set the
     * default value is false.
     *
     * @param proCandlesticks proCandlesticks option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setProCandlesticks(Boolean proCandlesticks) {
        this.proCandlesticks = proCandlesticks;
        return (T) this;
    }

    /**
     * @return true if bullet border should take the same color as graph line
     */
    public Boolean getUseLineColorForBulletBorder() {
        return useLineColorForBulletBorder;
    }

    /**
     * If set to true, the bullet border will take the same color as graph line. If not set the default value is false.
     *
     * @param useLineColorForBulletBorder useLineColorForBulletBorder option
     * @return graph
     */
    @StudioProperty(defaultValue = "false")
    public T setUseLineColorForBulletBorder(Boolean useLineColorForBulletBorder) {
        this.useLineColorForBulletBorder = useLineColorForBulletBorder;
        return (T) this;
    }

    /**
     * @return accessible label
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }

    /**
     * Sets text which screen readers will read if user rolls-over the bullet/column or sets focus using tab key
     * (this is possible only if tabIndex property of graph is set to some number). Text is added as aria-label.
     * Note - not all screen readers and browsers support this. If not set the default value is "[[title]]
     * [[category]] [[value]]".
     *
     * @param accessibleLabel accessible label string
     * @return graph
     */
    @StudioProperty(defaultValue = "[[title]] [[category]] [[value]]")
    public T setAccessibleLabel(String accessibleLabel) {
        this.accessibleLabel = accessibleLabel;
        return (T) this;
    }

    /**
     * @return balloon of the graph
     */
    public Balloon getBalloon() {
        return balloon;
    }

    /**
     * Sets the balloon to the graph. Allows customizing graphs balloons individually (only when {@link Cursor} is
     * used).
     *
     * @param balloon balloon
     * @return graph
     */
    @StudioElement
    public T setBalloon(Balloon balloon) {
        this.balloon = balloon;
        return (T) this;
    }

    /**
     * @return bullet hit area size
     */
    public Integer getBulletHitAreaSize() {
        return bulletHitAreaSize;
    }

    /**
     * Sets bullet hit area size. Useful for touch devices - if you set it to 20 or so, the bullets of a graph will
     * have invisible circle around the actual bullet (bullets should still be enabled), which will be easier to
     * touch (bullets usually are smaller and hard to hit).
     *
     * @param bulletHitAreaSize bullet hit area size
     * @return graph
     */
    @StudioProperty
    public T setBulletHitAreaSize(Integer bulletHitAreaSize) {
        this.bulletHitAreaSize = bulletHitAreaSize;
        return (T) this;
    }

    /**
     * @return CSS class name field
     */
    public String getClassNameField() {
        return classNameField;
    }

    /**
     * Sets CSS class name field. If classNameField is set and addClassNames is enabled, the chart will look for a CSS
     * class name string in data using this setting and apply additional class names to elements of the particular data
     * points, such as bullets.
     *
     * @param classNameField class name field string
     * @return graph
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public T setClassNameField(String classNameField) {
        this.classNameField = classNameField;
        return (T) this;
    }

    /**
     * @return fields of the {@link DataItem} that are bound to the graph
     */
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>();

        if (StringUtils.isNotEmpty(getValueField())) {
            wiredFields.add(getValueField());
        }

        if (StringUtils.isNotEmpty(getAlphaField())) {
            wiredFields.add(getAlphaField());
        }

        if (StringUtils.isNotEmpty(getBulletField())) {
            wiredFields.add(getBulletField());
        }

        if (StringUtils.isNotEmpty(getBulletSizeField())) {
            wiredFields.add(getBulletSizeField());
        }

        if (StringUtils.isNotEmpty(getCloseField())) {
            wiredFields.add(getCloseField());
        }

        if (StringUtils.isNotEmpty(getColorField())) {
            wiredFields.add(getColorField());
        }

        if (StringUtils.isNotEmpty(getCustomBulletField())) {
            wiredFields.add(getCustomBulletField());
        }

        if (StringUtils.isNotEmpty(getDashLengthField())) {
            wiredFields.add(getDashLengthField());
        }

        if (StringUtils.isNotEmpty(getDescriptionField())) {
            wiredFields.add(getDescriptionField());
        }

        if (StringUtils.isNotEmpty(getErrorField())) {
            wiredFields.add(getErrorField());
        }

        if (StringUtils.isNotEmpty(getFillColorsField())) {
            wiredFields.add(getFillColorsField());
        }

        if (StringUtils.isNotEmpty(getGapField())) {
            wiredFields.add(getGapField());
        }

        if (StringUtils.isNotEmpty(getHighField())) {
            wiredFields.add(getHighField());
        }

        if (StringUtils.isNotEmpty(getLabelColorField())) {
            wiredFields.add(getLabelColorField());
        }

        if (StringUtils.isNotEmpty(getLineColorField())) {
            wiredFields.add(getLineColorField());
        }

        if (StringUtils.isNotEmpty(getLowField())) {
            wiredFields.add(getLowField());
        }

        if (StringUtils.isNotEmpty(getOpenField())) {
            wiredFields.add(getOpenField());
        }

        if (StringUtils.isNotEmpty(getPatternField())) {
            wiredFields.add(getPatternField());
        }

        if (StringUtils.isNotEmpty(getUrlField())) {
            wiredFields.add(getUrlField());
        }

        if (StringUtils.isNotEmpty(getXField())) {
            wiredFields.add(getXField());
        }

        if (StringUtils.isNotEmpty(getYField())) {
            wiredFields.add(getYField());
        }

        if (StringUtils.isNotEmpty(getClassNameField())) {
            wiredFields.add(getClassNameField());
        }

        return wiredFields;
    }

    /**
     * @return JS function to handle legend marker color
     */
    public JsFunction getLegendColorFunction() {
        return legendColorFunction;
    }

    /**
     * Sets JS function to handle legend marker color. It is called and the following attributes are passed: dataItem,
     * formattedText, periodValues, periodPercentValues. It should return hex color code which will be used for
     * legend marker.
     *
     * @param legendColorFunction legend color function
     * @return graph
     */
    public T setLegendColorFunction(JsFunction legendColorFunction) {
        this.legendColorFunction = legendColorFunction;
        return (T) this;
    }

    /**
     * @return tab index
     */
    public Integer getTabIndex() {
        return tabIndex;
    }

    /**
     * In case you set it to some number, the chart will set focus on bullet/column (starting from first) when user
     * clicks tab key. When a focus is set, screen readers like NVDA Screen reader will read label which is set using
     * accessibleLabel property of graph. Note, not all browsers and readers support this.
     *
     * @param tabIndex tab index
     * @return graph
     */
    @StudioProperty
    public T setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
        return (T) this;
    }

    /**
     * @return column index field
     */
    public String getColumnIndexField() {
        return columnIndexField;
    }

    /**
     * Specifies order of columns of each category (starting from 0).You can use this property with non-stacked column
     * graphs. Important, this feature does not work in stacked columns scenarios as well as with graph toggling
     * enabled in legend.
     *
     * @param columnIndexField column index field string
     * @return graph
     */
    @StudioProperty
    public T setColumnIndexField(String columnIndexField) {
        this.columnIndexField = columnIndexField;
        return (T) this;
    }
}