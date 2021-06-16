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

package io.jmix.charts.model.stock;

import io.jmix.charts.model.Color;
import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.graph.AbstractGraph;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.charts.model.JsFunction;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioCollection;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

/**
 * StockGraph displays graphs on {@link StockPanel}.
 * <br>
 * See documentation for properties of StockGraph JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/StockGraph">http://docs.amcharts.com/3/javascriptstockchart/StockGraph</a>
 */
@StudioElement(
        caption = "StockGraph",
        xmlElement = "stockGraph",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class StockGraph extends AbstractGraph<StockGraph> {

    private static final long serialVersionUID = -1746419165781920815L;

    private Boolean comparable;

    private String compareField;

    private Boolean compareFromStart;

    private Graph compareGraph;

    private Color compareGraphBalloonColor;

    private JsFunction compareGraphBalloonFunction;

    private String compareGraphBalloonText;

    private String compareGraphBullet;

    private Double compareGraphBulletBorderAlpha;

    private Color compareGraphBulletBorderColor;

    private Integer compareGraphBulletBorderThickness;

    private Color compareGraphBulletColor;

    private Integer compareGraphBulletSize;

    private Integer compareGraphCornerRadiusTop;

    private Integer compareGraphDashLength;

    private Double compareGraphFillAlphas;

    private List<Color> compareGraphFillColors;

    private Double compareGraphLineAlpha;

    private Color compareGraphLineColor;

    private Integer compareGraphLineThickness;

    private GraphType compareGraphType;

    private Boolean compareGraphVisibleInLegend;

    private StockGraphValue periodValue;

    private StockGraphValue recalculateValue;

    private Boolean showEventsOnComparedGraphs;

    private Boolean useDataSetColors;

    /**
     * @return true if this graph is compared
     */
    public Boolean getComparable() {
        return comparable;
    }

    /**
     * Sets comparable to true if this graph should be compared if some data set is selected for comparing. If not
     * set the default value is false.
     *
     * @param comparable comparable option
     * @return stock graph
     */
    @StudioProperty(defaultValue = "false")
    public StockGraph setComparable(Boolean comparable) {
        this.comparable = comparable;
        return this;
    }

    /**
     * @return field that is used to generate comparing the graph
     */
    public String getCompareField() {
        return compareField;
    }

    /**
     * Sets the field to be used to generate comparing the graph. Note, this field is not the one used in your data
     * provider, but toField from FieldMapping object.
     *
     * @param compareField compare field
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareField(String compareField) {
        this.compareField = compareField;
        return this;
    }

    /**
     * @return true if compareFromStart is enabled
     */
    public Boolean getCompareFromStart() {
        return compareFromStart;
    }

    /**
     * Set compareFromStart to true if the graphs should use first value as a base value instead of using the first
     * value of selected period when data sets are compared. If not set the default value is false.
     *
     * @param compareFromStart compareFromStart option
     * @return stock graph
     */
    @StudioProperty(defaultValue = "false")
    public StockGraph setCompareFromStart(Boolean compareFromStart) {
        this.compareFromStart = compareFromStart;
        return this;
    }

    /**
     * @return config of compared graphs
     */
    public Graph getCompareGraph() {
        return compareGraph;
    }

    /**
     * Sets config of compared graphs. This allows you to set any of {@link Graph} properties on compared graphs
     * instead of using old-style properties like compareGraphBulletBorderThickness.
     *
     * @param compareGraph graph
     * @return stock graph
     */
    @StudioElement(caption = "Compare Graph", xmlElement = "compareGraph")
    public StockGraph setCompareGraph(Graph compareGraph) {
        this.compareGraph = compareGraph;
        return this;
    }

    /**
     * @return balloon color of comparing graph
     */
    public Color getCompareGraphBalloonColor() {
        return compareGraphBalloonColor;
    }

    /**
     * Sets balloon color of comparing graph.
     *
     * @param compareGraphBalloonColor color
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public StockGraph setCompareGraphBalloonColor(Color compareGraphBalloonColor) {
        this.compareGraphBalloonColor = compareGraphBalloonColor;
        return this;
    }

    /**
     * @return JS function
     */
    public JsFunction getCompareGraphBalloonFunction() {
        return compareGraphBalloonFunction;
    }

    /**
     * Sets JS function. The graph will call it and pass GraphDataItem and AmGraph object to
     * it. This function should return a string which will be displayed in a balloon. This will be used for graphs from
     * compared data sets only. Use balloonFunction for main data set's graphs.
     *
     * @param compareGraphBalloonFunction JS function
     * @return stock graph
     */
    public StockGraph setCompareGraphBalloonFunction(JsFunction compareGraphBalloonFunction) {
        this.compareGraphBalloonFunction = compareGraphBalloonFunction;
        return this;
    }

    /**
     * @return balloon text of comparing graph
     */
    public String getCompareGraphBalloonText() {
        return compareGraphBalloonText;
    }

    /**
     * Sets balloon text of comparing graph.
     *
     * @param compareGraphBalloonText text
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphBalloonText(String compareGraphBalloonText) {
        this.compareGraphBalloonText = compareGraphBalloonText;
        return this;
    }

    /**
     * @return bullet of comparing graph
     */
    public String getCompareGraphBullet() {
        return compareGraphBullet;
    }

    /**
     * Sets bullet of comparing graph. Possible values are: round, square, diamond, triangleUp, triangleDown,
     * triangleLeft, triangleRight, bubble
     *
     * @param compareGraphBullet compare graph bullet string
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {
            "round", "square", "diamond", "triangleUp", "triangleDown", "triangleLeft", "triangleRight", "bubble"
    })
    public StockGraph setCompareGraphBullet(String compareGraphBullet) {
        this.compareGraphBullet = compareGraphBullet;
        return this;
    }

    /**
     * @return opacity of bullet border of comparing graph
     */
    public Double getCompareGraphBulletBorderAlpha() {
        return compareGraphBulletBorderAlpha;
    }

    /**
     * Sets opacity of bullet border of comparing graph.
     *
     * @param compareGraphBulletBorderAlpha opacity
     * @return stock graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public StockGraph setCompareGraphBulletBorderAlpha(Double compareGraphBulletBorderAlpha) {
        this.compareGraphBulletBorderAlpha = compareGraphBulletBorderAlpha;
        return this;
    }

    /**
     * @return color of bullet border of comparing graph
     */
    public Color getCompareGraphBulletBorderColor() {
        return compareGraphBulletBorderColor;
    }

    /**
     * Sets color of bullet border of comparing graph.
     *
     * @param compareGraphBulletBorderColor color
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public StockGraph setCompareGraphBulletBorderColor(Color compareGraphBulletBorderColor) {
        this.compareGraphBulletBorderColor = compareGraphBulletBorderColor;
        return this;
    }

    /**
     * @return thickness of bullet border of comparing graph
     */
    public Integer getCompareGraphBulletBorderThickness() {
        return compareGraphBulletBorderThickness;
    }

    /**
     * Sets thickness of bullet border of comparing graph.
     *
     * @param compareGraphBulletBorderThickness thickness
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphBulletBorderThickness(Integer compareGraphBulletBorderThickness) {
        this.compareGraphBulletBorderThickness = compareGraphBulletBorderThickness;
        return this;
    }

    /**
     * @return color of compared graphs bullets
     */
    public Color getCompareGraphBulletColor() {
        return compareGraphBulletColor;
    }

    /**
     * Sets color of compared graphs bullets.
     *
     * @param compareGraphBulletColor color
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public StockGraph setCompareGraphBulletColor(Color compareGraphBulletColor) {
        this.compareGraphBulletColor = compareGraphBulletColor;
        return this;
    }

    /**
     * @return bullet size of comparing graph
     */
    public Integer getCompareGraphBulletSize() {
        return compareGraphBulletSize;
    }

    /**
     * Sets bullet size of comparing graph.
     *
     * @param compareGraphBulletSize bullet size
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphBulletSize(Integer compareGraphBulletSize) {
        this.compareGraphBulletSize = compareGraphBulletSize;
        return this;
    }

    /**
     * @return corner radius of comparing graph
     */
    public Integer getCompareGraphCornerRadiusTop() {
        return compareGraphCornerRadiusTop;
    }

    /**
     * Sets corner radius of comparing graph (if type is "column").
     *
     * @param compareGraphCornerRadiusTop corner radius
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphCornerRadiusTop(Integer compareGraphCornerRadiusTop) {
        this.compareGraphCornerRadiusTop = compareGraphCornerRadiusTop;
        return this;
    }

    /**
     * @return dash length of compare graph
     */
    public Integer getCompareGraphDashLength() {
        return compareGraphDashLength;
    }

    /**
     * Sets dash length of compare graph.
     *
     * @param compareGraphDashLength dash length
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphDashLength(Integer compareGraphDashLength) {
        this.compareGraphDashLength = compareGraphDashLength;
        return this;
    }

    /**
     * @return fill alpha of comparing graph
     */
    public Double getCompareGraphFillAlphas() {
        return compareGraphFillAlphas;
    }

    /**
     * Sets fill alpha of comparing graph.
     *
     * @param compareGraphFillAlphas fill alpha
     * @return stock graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public StockGraph setCompareGraphFillAlphas(Double compareGraphFillAlphas) {
        this.compareGraphFillAlphas = compareGraphFillAlphas;
        return this;
    }

    /**
     * @return list of fill colors of comparing graph
     */
    public List<Color> getCompareGraphFillColors() {
        return compareGraphFillColors;
    }

    /**
     * Sets list of fill colors of comparing graph.
     *
     * @param compareGraphFillColors list of fill colors
     * @return stock graph
     */
    @StudioCollection(xmlElement = "compareGraphFillColors",
            itemXmlElement = "color",
            itemCaption = "Compare Graph Fill Color",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.ENUMERATION,
                            options = {"@link io.jmix.charts.model.Color"})
            })
    public StockGraph setCompareGraphFillColors(List<Color> compareGraphFillColors) {
        this.compareGraphFillColors = compareGraphFillColors;
        return this;
    }

    /**
     * @return opacity of comparing graph line
     */
    public Double getCompareGraphLineAlpha() {
        return compareGraphLineAlpha;
    }

    /**
     * Sets opacity of comparing graph line.
     *
     * @param compareGraphLineAlpha opacity
     * @return stock graph
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public StockGraph setCompareGraphLineAlpha(Double compareGraphLineAlpha) {
        this.compareGraphLineAlpha = compareGraphLineAlpha;
        return this;
    }

    /**
     * @return color of compare graph
     */
    public Color getCompareGraphLineColor() {
        return compareGraphLineColor;
    }

    /**
     * Sets color of compare graph (by default data set color is used).
     *
     * @param compareGraphLineColor color
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public StockGraph setCompareGraphLineColor(Color compareGraphLineColor) {
        this.compareGraphLineColor = compareGraphLineColor;
        return this;
    }

    /**
     * @return thickness of compare graph
     */
    public Integer getCompareGraphLineThickness() {
        return compareGraphLineThickness;
    }

    /**
     * Sets thickness of compare graph.
     *
     * @param compareGraphLineThickness thickness
     * @return stock graph
     */
    @StudioProperty
    public StockGraph setCompareGraphLineThickness(Integer compareGraphLineThickness) {
        this.compareGraphLineThickness = compareGraphLineThickness;
        return this;
    }

    /**
     * @return type of comparing graph
     */
    public GraphType getCompareGraphType() {
        return compareGraphType;
    }

    /**
     * Sets type of comparing graph. Possible values are: "line", "column", "step", "smoothedLine". If not set the
     * default value is LINE.
     *
     * @param compareGraphType type
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "LINE")
    public StockGraph setCompareGraphType(GraphType compareGraphType) {
        this.compareGraphType = compareGraphType;
        return this;
    }

    /**
     * @return true if compare graph is visible in legend
     */
    public Boolean getCompareGraphVisibleInLegend() {
        return compareGraphVisibleInLegend;
    }

    /**
     * Set compareGraphVisibleInLegend to false if compare graph shouldn't be visible in legend. If not set the
     * default value is true.
     *
     * @param compareGraphVisibleInLegend compareGraphVisibleInLegend option
     * @return stock graph
     */
    @StudioProperty(defaultValue = "true")
    public StockGraph setCompareGraphVisibleInLegend(Boolean compareGraphVisibleInLegend) {
        this.compareGraphVisibleInLegend = compareGraphVisibleInLegend;
        return this;
    }

    /**
     * @return period value
     */
    public StockGraphValue getPeriodValue() {
        return periodValue;
    }

    /**
     * Sets period value. When data is grouped to periods, the graph must know which period value should be used.
     *
     * @param periodValue period value
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public StockGraph setPeriodValue(StockGraphValue periodValue) {
        this.periodValue = periodValue;
        return this;
    }

    /**
     * @return recalculate value
     */
    public StockGraphValue getRecalculateValue() {
        return recalculateValue;
    }

    /**
     * Sets recalculate value. Possible values are "open", "close", "high", "low", "average" and "sum". There is no
     * default value set – graph uses its periodValue when calculating changes. For example, the graph’s periodValue
     * is "close". This means that when data is grouped to longer periods (months for example) when recalculating, the
     * graph will use "close" value of the first period of the selection as base value and will compare each months
     * "close" value to it. If you set recalculateValue to "open", the first value of a month will be used as base
     * value.
     *
     * @param recalculateValue recalculate value
     * @return stock graph
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public StockGraph setRecalculateValue(StockGraphValue recalculateValue) {
        this.recalculateValue = recalculateValue;
        return this;
    }

    /**
     * @return true if events of compared graphs is shown
     */
    public Boolean getShowEventsOnComparedGraphs() {
        return showEventsOnComparedGraphs;
    }

    /**
     * Set showEventsOnComparedGraphs to true if events of compared graphs should be shown. If not set the default
     * value is false.
     *
     * @param showEventsOnComparedGraphs showEventsOnComparedGraphs option
     * @return stock graph
     */
    @StudioProperty(defaultValue = "false")
    public StockGraph setShowEventsOnComparedGraphs(Boolean showEventsOnComparedGraphs) {
        this.showEventsOnComparedGraphs = showEventsOnComparedGraphs;
        return this;
    }

    /**
     * @return true if data set color is used as this graph's lineColor
     */
    public Boolean getUseDataSetColors() {
        return useDataSetColors;
    }

    /**
     * Specifies whether data set color should be used as this graph's lineColor. By default all graphs from the same
     * data set will have a color from its relative DataSet (either auto-assigned by chart or set by color parameter).
     * The graph's color properties will be ignored. To disable this behavior, set useDataSetColors to false. This
     * way the chart will use graph's own color settings, such as "lineColor" or auto-assign the color if those are
     * not set. If not set the default value is true.
     *
     * @param useDataSetColors useDataSetColors option
     * @return stock graph
     */
    @StudioProperty(defaultValue = "true")
    public StockGraph setUseDataSetColors(Boolean useDataSetColors) {
        this.useDataSetColors = useDataSetColors;
        return this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (StringUtils.isNotEmpty(getCompareField())) {
            wiredFields.add(getCompareField());
        }

        return wiredFields;
    }
}
