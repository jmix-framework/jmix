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

package io.jmix.charts.component.impl;


import io.jmix.core.DevelopmentException;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.data.impl.HasMetaClass;
import io.jmix.charts.component.SeriesBasedChart;
import io.jmix.charts.model.Scrollbar;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.chart.impl.AbstractSerialChart;
import io.jmix.charts.model.date.DayOfWeek;
import io.jmix.charts.widget.amcharts.events.category.CategoryItemClickListener;
import io.jmix.charts.widget.amcharts.events.zoom.ZoomListener;
import io.jmix.charts.widget.amcharts.serialization.ChartJsonSerializationContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class SeriesBasedChartImpl<T extends SeriesBasedChart, M extends AbstractSerialChart> extends RectangularChartImpl<T, M>
        implements SeriesBasedChart<T> {

    protected ZoomListener zoomHandler;

    protected CategoryItemClickListener categoryItemClickHandler;

    @Override
    protected void setupDefaults(M chart) {
        super.setupDefaults(chart);

        setupSerialChartDefaults(chart);
    }

    protected void setupSerialChartDefaults(AbstractSerialChart chart) {
        chart.setDataDateFormat(ChartJsonSerializationContext.DEFAULT_JS_DATE_FORMAT);

        String format = messages.getMessage("amcharts.serialChart.balloonDateFormat");
        chart.setBalloonDateFormat(format);
    }



    protected void detectDateBasedCategoryAxis() {
        MetaClass metaClass = getDataProvider() instanceof HasMetaClass ?
                ((HasMetaClass) getDataProvider()).getMetaClass() : null;

        if (metaClass != null
                && StringUtils.isNotEmpty(getCategoryField())
                && getCategoryAxis() != null
                && getCategoryAxis().getParseDates() == null) {
            MetaProperty property = metaClass.getProperty(getCategoryField());
            if (property == null) {
                throw new DevelopmentException(
                        String.format("Unable to find metaproperty '%s' for class '%s'", getCategoryField(), metaClass));
            }
            if (Date.class.isAssignableFrom(property.getJavaType())) {
                getCategoryAxis().setParseDates(true);
            }
        }
    }

    @Override
    public CategoryAxis getCategoryAxis() {
        return getModel().getCategoryAxis();
    }

    @Override
    public T setCategoryAxis(CategoryAxis categoryAxis) {
        if (categoryAxis != null && categoryAxis.getFirstDayOfWeek() == null) {
            String firstDayOfWeek = messages.getMessage("amcharts.firstDayOfWeek");
            categoryAxis.setFirstDayOfWeek(DayOfWeek.valueOf(firstDayOfWeek));
        }

        getModel().setCategoryAxis(categoryAxis);
        detectDateBasedCategoryAxis();
        return (T) this;
    }

    @Override
    public String getCategoryField() {
        return getModel().getCategoryField();
    }

    @Override
    public T setCategoryField(String categoryField) {
        getModel().setCategoryField(categoryField);
        detectDateBasedCategoryAxis();
        return (T) this;
    }

    @Override
    public String getBalloonDateFormat() {
        return getModel().getBalloonDateFormat();
    }

    @Override
    public T setBalloonDateFormat(String balloonDateFormat) {
        getModel().setBalloonDateFormat(balloonDateFormat);
        return (T) this;
    }

    @Override
    public Integer getColumnSpacing3D() {
        return getModel().getColumnSpacing3D();
    }

    @Override
    public T setColumnSpacing3D(Integer columnSpacing3D) {
        getModel().setColumnSpacing3D(columnSpacing3D);
        return (T) this;
    }

    @Override
    public Integer getColumnSpacing() {
        return getModel().getColumnSpacing();
    }

    @Override
    public T setColumnSpacing(Integer columnSpacing) {
        getModel().setColumnSpacing(columnSpacing);
        return (T) this;
    }

    @Override
    public Double getColumnWidth() {
        return getModel().getColumnWidth();
    }

    @Override
    public T setColumnWidth(Double columnWidth) {
        getModel().setColumnWidth(columnWidth);
        return (T) this;
    }

    @Override
    public String getDataDateFormat() {
        return getModel().getDataDateFormat();
    }

    @Override
    public T setDataDateFormat(String dataDateFormat) {
        getModel().setDataDateFormat(dataDateFormat);
        return (T) this;
    }

    @Override
    public Integer getMaxSelectedSeries() {
        return getModel().getMaxSelectedSeries();
    }

    @Override
    public T setMaxSelectedSeries(Integer maxSelectedSeries) {
        getModel().setMaxSelectedSeries(maxSelectedSeries);
        return (T) this;
    }

    @Override
    public Long getMaxSelectedTime() {
        return getModel().getMaxSelectedTime();
    }

    @Override
    public T setMaxSelectedTime(Long maxSelectedTime) {
        getModel().setMaxSelectedTime(maxSelectedTime);
        return (T) this;
    }

    @Override
    public Long getMinSelectedTime() {
        return getModel().getMinSelectedTime();
    }

    @Override
    public T setMinSelectedTime(Long minSelectedTime) {
        getModel().setMinSelectedTime(minSelectedTime);
        return (T) this;
    }

    @Override
    public Boolean getMouseWheelScrollEnabled() {
        return getModel().getMouseWheelScrollEnabled();
    }

    @Override
    public T setMouseWheelScrollEnabled(Boolean mouseWheelScrollEnabled) {
        getModel().setMouseWheelScrollEnabled(mouseWheelScrollEnabled);
        return (T) this;
    }

    @Override
    public Boolean getRotate() {
        return getModel().getRotate();
    }

    @Override
    public T setRotate(Boolean rotate) {
        getModel().setRotate(rotate);
        return (T) this;
    }

    @Override
    public Boolean getZoomOutOnDataUpdate() {
        return getModel().getZoomOutOnDataUpdate();
    }

    @Override
    public T setZoomOutOnDataUpdate(Boolean zoomOutOnDataUpdate) {
        getModel().setZoomOutOnDataUpdate(zoomOutOnDataUpdate);
        return (T) this;
    }

    @Override
    public Boolean getMouseWheelZoomEnabled() {
        return getModel().getMouseWheelZoomEnabled();
    }

    @Override
    public T setMouseWheelZoomEnabled(Boolean mouseWheelZoomEnabled) {
        getModel().setMouseWheelZoomEnabled(mouseWheelZoomEnabled);
        return (T) this;
    }

    @Override
    public Scrollbar getValueScrollbar() {
        return getModel().getValueScrollbar();
    }

    @Override
    public T setValueScrollbar(Scrollbar valueScrollbar) {
        getModel().setValueScrollbar(valueScrollbar);
        return (T) this;
    }

    @Override
    public Boolean getSynchronizeGrid() {
        return getModel().getSynchronizeGrid();
    }

    @Override
    public T setSynchronizeGrid(Boolean synchronizeGrid) {
        getModel().setSynchronizeGrid(synchronizeGrid);
        return (T) this;
    }

    @Override
    public void zoomOut() {
        component.zoomOut();
    }

    @Override
    public void zoomToIndexes(int start, int end) {
        component.zoomToIndexes(start, end);
    }

    @Override
    public void zoomToDates(Date start, Date end) {
        component.zoomToDates(start, end);
    }

    @Override
    public Subscription addZoomListener(Consumer<ZoomEvent> listener) {
        if (zoomHandler == null) {
            zoomHandler = this::onZoomEvent;
            component.addZoomListener(zoomHandler);
        }

        return getEventHub().subscribe(ZoomEvent.class, listener);
    }

    protected void onZoomEvent(io.jmix.charts.widget.amcharts.events.zoom.ZoomEvent e) {
        publish(ZoomEvent.class,
                new ZoomEvent(this, e.getStartIndex(), e.getEndIndex(), e.getStartDate(),
                        e.getEndDate(), e.getStartValue(), e.getEndValue()));
    }

    @Override
    public Subscription addCategoryItemClickListener(Consumer<CategoryItemClickEvent> listener) {
        if (categoryItemClickHandler == null) {
            categoryItemClickHandler = this::onCategoryItemClick;
            component.addCategoryItemClickListener(categoryItemClickHandler);
        }

        return getEventHub().subscribe(CategoryItemClickEvent.class, listener);
    }

    protected void onCategoryItemClick(io.jmix.charts.widget.amcharts.events.category.CategoryItemClickEvent e) {
        publish(CategoryItemClickEvent.class,
                new CategoryItemClickEvent(this, e.getValue(), e.getX(), e.getY(),
                        e.getOffsetX(), e.getOffsetY(), e.getXAxis(), e.getYAxis()));
    }
}