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


import io.jmix.charts.model.Scrollbar;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.SeriesBasedChartModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * See documentation for properties of AmSerialChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmSerialChart">http://docs.amcharts.com/3/javascriptcharts/AmSerialChart</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSerialChart<T extends AbstractSerialChart> extends RectangularChartModelImpl<T>
        implements SeriesBasedChartModel<T> {

    private static final long serialVersionUID = 4097450050182930159L;

    private String balloonDateFormat;

    private CategoryAxis categoryAxis;

    private String categoryField;

    private Integer columnSpacing;

    private Integer columnSpacing3D;

    private Double columnWidth;

    private String dataDateFormat;

    private Integer maxSelectedSeries;

    private Long maxSelectedTime;

    private Long minSelectedTime;

    private Boolean mouseWheelScrollEnabled;

    private Boolean mouseWheelZoomEnabled;

    private Boolean rotate;

    private Boolean synchronizeGrid;

    private Scrollbar valueScrollbar;

    private Boolean zoomOutOnDataUpdate;

    protected AbstractSerialChart(ChartType type) {
        super(type);
    }

    @Override
    public CategoryAxis getCategoryAxis() {
        return categoryAxis;
    }

    @Override
    public T setCategoryAxis(CategoryAxis categoryAxis) {
        this.categoryAxis = categoryAxis;
        return (T) this;
    }

    @Override
    public String getCategoryField() {
        return categoryField;
    }

    @Override
    public T setCategoryField(String categoryField) {
        this.categoryField = categoryField;
        return (T) this;
    }

    @Override
    public String getBalloonDateFormat() {
        return balloonDateFormat;
    }

    @Override
    public T setBalloonDateFormat(String balloonDateFormat) {
        this.balloonDateFormat = balloonDateFormat;
        return (T) this;
    }

    @Override
    public Integer getColumnSpacing3D() {
        return columnSpacing3D;
    }

    @Override
    public T setColumnSpacing3D(Integer columnSpacing3D) {
        this.columnSpacing3D = columnSpacing3D;
        return (T) this;
    }

    @Override
    public Integer getColumnSpacing() {
        return columnSpacing;
    }

    @Override
    public T setColumnSpacing(Integer columnSpacing) {
        this.columnSpacing = columnSpacing;
        return (T) this;
    }

    @Override
    public Double getColumnWidth() {
        return columnWidth;
    }

    @Override
    public T setColumnWidth(Double columnWidth) {
        this.columnWidth = columnWidth;
        return (T) this;
    }

    @Override
    public String getDataDateFormat() {
        return dataDateFormat;
    }

    @Override
    public T setDataDateFormat(String dataDateFormat) {
        this.dataDateFormat = dataDateFormat;
        return (T) this;
    }

    @Override
    public Integer getMaxSelectedSeries() {
        return maxSelectedSeries;
    }

    @Override
    public T setMaxSelectedSeries(Integer maxSelectedSeries) {
        this.maxSelectedSeries = maxSelectedSeries;
        return (T) this;
    }

    @Override
    public Long getMaxSelectedTime() {
        return maxSelectedTime;
    }

    @Override
    public T setMaxSelectedTime(Long maxSelectedTime) {
        this.maxSelectedTime = maxSelectedTime;
        return (T) this;
    }

    @Override
    public Long getMinSelectedTime() {
        return minSelectedTime;
    }

    @Override
    public T setMinSelectedTime(Long minSelectedTime) {
        this.minSelectedTime = minSelectedTime;
        return (T) this;
    }

    @Override
    public Boolean getMouseWheelScrollEnabled() {
        return mouseWheelScrollEnabled;
    }

    @Override
    public T setMouseWheelScrollEnabled(Boolean mouseWheelScrollEnabled) {
        this.mouseWheelScrollEnabled = mouseWheelScrollEnabled;
        return (T) this;
    }

    @Override
    public Boolean getRotate() {
        return rotate;
    }

    @Override
    public T setRotate(Boolean rotate) {
        this.rotate = rotate;
        return (T) this;
    }

    @Override
    public Boolean getZoomOutOnDataUpdate() {
        return zoomOutOnDataUpdate;
    }

    @Override
    public T setZoomOutOnDataUpdate(Boolean zoomOutOnDataUpdate) {
        this.zoomOutOnDataUpdate = zoomOutOnDataUpdate;
        return (T) this;
    }

    @Override
    public Boolean getMouseWheelZoomEnabled() {
        return mouseWheelZoomEnabled;
    }

    @Override
    public T setMouseWheelZoomEnabled(Boolean mouseWheelZoomEnabled) {
        this.mouseWheelZoomEnabled = mouseWheelZoomEnabled;
        return (T) this;
    }

    @Override
    public Scrollbar getValueScrollbar() {
        return valueScrollbar;
    }

    @Override
    public T setValueScrollbar(Scrollbar valueScrollbar) {
        this.valueScrollbar = valueScrollbar;
        return (T) this;
    }

    @Override
    public Boolean getSynchronizeGrid() {
        return synchronizeGrid;
    }

    @Override
    public T setSynchronizeGrid(Boolean synchronizeGrid) {
        this.synchronizeGrid = synchronizeGrid;
        return (T) this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());
        if (StringUtils.isNotEmpty(categoryField)) {
            wiredFields.add(categoryField);
        }
        if (categoryAxis != null) {
            if (StringUtils.isNotEmpty(categoryAxis.getForceShowField())) {
                wiredFields.add(categoryAxis.getForceShowField());
            }

            if (StringUtils.isNotEmpty(categoryAxis.getLabelColorField())) {
                wiredFields.add(categoryAxis.getLabelColorField());
            }
        }

        return wiredFields;
    }
}