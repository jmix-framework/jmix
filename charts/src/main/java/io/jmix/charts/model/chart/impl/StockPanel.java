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

package io.jmix.charts.model.chart.impl;

import io.jmix.charts.model.Color;
import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.SeriesBasedChartModel;
import io.jmix.charts.model.chart.StockChartModel;
import io.jmix.charts.model.stock.StockGraph;
import io.jmix.charts.model.stock.StockLegend;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;

/**
 * Creates stock panels (charts). {@link StockChartModel} can have multiple Stock panels.
 * <br>
 * See documentation for properties of StockPanel JS object. <br>
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/StockPanel">http://docs.amcharts.com/3/javascriptstockchart/StockPanel</a>
 */
@StudioElement(
        caption = "StockPanel",
        xmlElement = "panel",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class StockPanel extends AbstractSerialChart<StockPanel> implements SeriesBasedChartModel<StockPanel> {

    private static final long serialVersionUID = 3129940127141352054L;

    private String id;

    private Boolean allowTurningOff;

    private Boolean drawingIconsEnabled;

    private ValueAxis drawOnAxis;

    private Boolean eraseAll;

    private Integer iconSize;

    private Integer percentHeight;

    private Date recalculateFromDate;

    private String recalculateToPercents;

    private Boolean showCategoryAxis;

    private Boolean showComparedOnTop;

    private List<StockGraph> stockGraphs;

    private StockLegend stockLegend;

    private String title;

    private Double trendLineAlpha;

    private Color trendLineColor;

    private Integer trendLineDashLength;

    private Integer trendLineThickness;

    public StockPanel() {
        super(null);
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public StockPanel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return true if x button is displayed near the panel
     */
    public Boolean getAllowTurningOff() {
        return allowTurningOff;
    }

    /**
     * Set allowTurningOff to true if x button should be displayed near the panel. This button allows turning panel off.
     * If not set the default value is false.
     *
     * @param allowTurningOff allowTurningOff option
     * @return stock panel
     */
    @StudioProperty(defaultValue = "false")
    public StockPanel setAllowTurningOff(Boolean allowTurningOff) {
        this.allowTurningOff = allowTurningOff;
        return this;
    }

    /**
     * @return true if drawing icons is displayed in top-right corner
     */
    public Boolean getDrawingIconsEnabled() {
        return drawingIconsEnabled;
    }

    /**
     * Set drawingIconsEnabled to true if drawing icons should be displayed in top-right corner. If not set the
     * default value is false.
     *
     * @param drawingIconsEnabled drawingIconsEnabled option
     * @return stock panel
     */
    @StudioProperty(defaultValue = "false")
    public StockPanel setDrawingIconsEnabled(Boolean drawingIconsEnabled) {
        this.drawingIconsEnabled = drawingIconsEnabled;
        return this;
    }

    /**
     * @return value axis
     */
    public ValueAxis getDrawOnAxis() {
        return drawOnAxis;
    }

    /**
     * Sets value axis. Specifies on which value axis user can draw trend lines. Set drawingIconsEnabled to true if
     * you want drawing icons to be visible. First value axis will be used if not set here. You can use a reference
     * to the value axis object or id of value axis.
     *
     * @param drawOnAxis value axis
     * @return stock panel
     */
    @StudioElement(caption = "Draw on Axis", xmlElement = "drawOnAxis")
    public StockPanel setDrawOnAxis(ValueAxis drawOnAxis) {
        this.drawOnAxis = drawOnAxis;
        return this;
    }

    /**
     * @return true if all trend lines are erased
     */
    public Boolean getEraseAll() {
        return eraseAll;
    }

    /**
     * Set eraseAll to true if all trend lines should be erased when erase button is clicked. If eraseAll is set to
     * false trend lines will be erased one by one. If not set the default value is false.
     *
     * @param eraseAll eraseAll option
     * @return stock panel
     */
    @StudioProperty(defaultValue = "false")
    public StockPanel setEraseAll(Boolean eraseAll) {
        this.eraseAll = eraseAll;
        return this;
    }

    /**
     * @return size of trend line drawing icons
     */
    public Integer getIconSize() {
        return iconSize;
    }

    /**
     * Sets size of trend line drawing icons. If you change this size, you should update icon images if you want
     * them to look properly. If not set the default value is 18.
     *
     * @param iconSize icon size
     * @return stock panel
     */
    @StudioProperty(defaultValue = "18")
    public StockPanel setIconSize(Integer iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    /**
     * @return relative height of panel
     */
    public Integer getPercentHeight() {
        return percentHeight;
    }

    /**
     * Sets relative height of panel. Possible values 0 - 100.
     *
     * @param percentHeight percent height
     * @return stock panel
     */
    @StudioProperty
    @Max(100)
    @Min(0)
    public StockPanel setPercentHeight(Integer percentHeight) {
        this.percentHeight = percentHeight;
        return this;
    }

    /**
     * @return from which date's value is used as a base when recalculating values to percent
     */
    public Date getRecalculateFromDate() {
        return recalculateFromDate;
    }

    /**
     * Sets from which date's value should be used as a base when recalculating values to percent.
     *
     * @param recalculateFromDate date
     * @return stock panel
     */
    @StudioProperty
    public StockPanel setRecalculateFromDate(Date recalculateFromDate) {
        this.recalculateFromDate = recalculateFromDate;
        return this;
    }

    /**
     * @return recalculate to percents value
     */
    public String getRecalculateToPercents() {
        return recalculateToPercents;
    }

    /**
     * Specifies when values should be recalculated to percents. Possible values are: "never", "always",
     * "whenComparing". If not set the default value is "whenComparing".
     *
     * @param recalculateToPercents recalculate to percents value
     * @return stock panel
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"never", "always", "whenComparing"},
            defaultValue = "whenComparing")
    public StockPanel setRecalculateToPercents(String recalculateToPercents) {
        this.recalculateToPercents = recalculateToPercents;
        return this;
    }

    /**
     * @return true of this panel shows category axis
     */
    public Boolean getShowCategoryAxis() {
        return showCategoryAxis;
    }

    /**
     * Set showCategoryAxis to false if this panel should't show category axis. If not set the default value is true.
     *
     * @param showCategoryAxis showCategoryAxis option
     * @return stock panel
     */
    @StudioProperty(defaultValue = "true")
    public StockPanel setShowCategoryAxis(Boolean showCategoryAxis) {
        this.showCategoryAxis = showCategoryAxis;
        return this;
    }

    /**
     * @return true if compared graphs are shown above or behind the main graph
     */
    public Boolean getShowComparedOnTop() {
        return showComparedOnTop;
    }

    /**
     * Set showComparedOnTop to false if compared graphs shouldn't be shown above or behind the main graph. If not
     * set the default value is true.
     *
     * @param showComparedOnTop showComparedOnTop option
     * @return stock panel
     */
    @StudioProperty(defaultValue = "true")
    public StockPanel setShowComparedOnTop(Boolean showComparedOnTop) {
        this.showComparedOnTop = showComparedOnTop;
        return this;
    }

    /**
     * @return list of stock graphs
     */
    public List<StockGraph> getStockGraphs() {
        return stockGraphs;
    }

    /**
     * Sets list of stock graphs.
     *
     * @param stockGraphs list of stock graphs
     * @return stock panel
     */
    @StudioElementsGroup(caption = "Stock Graphs", xmlElement = "stockGraphs")
    public StockPanel setStockGraphs(List<StockGraph> stockGraphs) {
        this.stockGraphs = stockGraphs;
        return this;
    }

    /**
     * Adds stock graphs
     *
     * @param stockGraphs stock graphs
     * @return stock panel
     */
    public StockPanel addStockGraphs(StockGraph... stockGraphs) {
        if (stockGraphs != null) {
            if (this.stockGraphs == null) {
                this.stockGraphs = new ArrayList<>();
            }
            this.stockGraphs.addAll(Arrays.asList(stockGraphs));
        }
        return this;
    }

    /**
     * @return stock chart legend.
     */
    public StockLegend getStockLegend() {
        return stockLegend;
    }

    /**
     * Sets stock chart legend.
     *
     * @param stockLegend stock legend
     * @return stock panel
     */
    @StudioElement
    public StockPanel setStockLegend(StockLegend stockLegend) {
        this.stockLegend = stockLegend;
        return this;
    }

    /**
     * @return title of a panel
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets a title of a panel. Note, {@link StockLegend} should be added in order title to be displayed.
     *
     * @param title title
     * @return stock panel
     */
    @StudioProperty
    public StockPanel setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * @return trend line opacity
     */
    public Double getTrendLineAlpha() {
        return trendLineAlpha;
    }

    /**
     * Sets trend line opacity. If not set the default value is 1.
     *
     * @param trendLineAlpha opacity
     * @return stock panel
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public StockPanel setTrendLineAlpha(Double trendLineAlpha) {
        this.trendLineAlpha = trendLineAlpha;
        return this;
    }

    /**
     * @return trend line color
     */
    public Color getTrendLineColor() {
        return trendLineColor;
    }

    /**
     * Sets trend line color. If not set the default value is #00CC00.
     *
     * @param trendLineColor color
     * @return stock panel
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#00CC00")
    public StockPanel setTrendLineColor(Color trendLineColor) {
        this.trendLineColor = trendLineColor;
        return this;
    }

    /**
     * @return trend line dash length
     */
    public Integer getTrendLineDashLength() {
        return trendLineDashLength;
    }

    /**
     * Sets trend line dash length. If not set the default value is 0.
     *
     * @param trendLineDashLength dash length
     * @return stock panel
     */
    @StudioProperty(defaultValue = "0")
    public StockPanel setTrendLineDashLength(Integer trendLineDashLength) {
        this.trendLineDashLength = trendLineDashLength;
        return this;
    }

    /**
     * @return trend line thickness
     */
    public Integer getTrendLineThickness() {
        return trendLineThickness;
    }

    /**
     * Sets trend line thickness. If not set the default value is 2.
     *
     * @param trendLineThickness thickness
     * @return stock panel
     */
    @StudioProperty(defaultValue = "2")
    public StockPanel setTrendLineThickness(Integer trendLineThickness) {
        this.trendLineThickness = trendLineThickness;
        return this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (stockGraphs != null) {
            for (StockGraph g : stockGraphs) {
                wiredFields.addAll(g.getWiredFields());
            }
        }

        return wiredFields;
    }
}