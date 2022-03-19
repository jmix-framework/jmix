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

import com.google.gson.annotations.Expose;
import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.GanttChartModel;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * See documentation for properties of AmGanttChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmGanttChart">http://docs.amcharts.com/3/javascriptcharts/AmGanttChart</a>
 */
public class GanttChartModelImpl extends AbstractSerialChart<GanttChartModelImpl> implements GanttChartModel<GanttChartModelImpl> {

    private static final long serialVersionUID = 5489743047811238869L;

    private Integer brightnessStep;

    private String colorField;

    private String columnWidthField;

    private String durationField;

    private String endDateField;

    private String endField;

    private Graph graph;

    private DatePeriod period;

    private String segmentsField;

    private Date startDate;

    private String startDateField;

    private String startField;

    private ValueAxis valueAxis;

    @Expose(serialize = false, deserialize = false)
    private List<String> additionalSegmentFields;

    public GanttChartModelImpl() {
        super(ChartType.GANTT);
    }

    @Override
    public Integer getBrightnessStep() {
        return brightnessStep;
    }

    @Override
    public GanttChartModelImpl setBrightnessStep(Integer brightnessStep) {
        this.brightnessStep = brightnessStep;
        return this;
    }

    @Override
    public String getColorField() {
        return colorField;
    }

    @Override
    public GanttChartModelImpl setColorField(String colorField) {
        this.colorField = colorField;
        return this;
    }

    @Override
    public String getColumnWidthField() {
        return columnWidthField;
    }

    @Override
    public GanttChartModelImpl setColumnWidthField(String columnWidthField) {
        this.columnWidthField = columnWidthField;
        return this;
    }

    @Override
    public String getDurationField() {
        return durationField;
    }

    @Override
    public GanttChartModelImpl setDurationField(String durationField) {
        this.durationField = durationField;
        return this;
    }

    @Override
    public String getEndDateField() {
        return endDateField;
    }

    @Override
    public GanttChartModelImpl setEndDateField(String endDateField) {
        this.endDateField = endDateField;
        return this;
    }

    @Override
    public String getEndField() {
        return endField;
    }

    @Override
    public GanttChartModelImpl setEndField(String endField) {
        this.endField = endField;
        return this;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public GanttChartModelImpl setGraph(Graph graph) {
        this.graph = graph;
        return this;
    }

    @Override
    public DatePeriod getPeriod() {
        return period;
    }

    @Override
    public GanttChartModelImpl setPeriod(DatePeriod period) {
        this.period = period;
        return this;
    }

    @Override
    public String getSegmentsField() {
        return segmentsField;
    }

    @Override
    public GanttChartModelImpl setSegmentsField(String segmentsField) {
        this.segmentsField = segmentsField;
        return this;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public GanttChartModelImpl setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    @Override
    public String getStartDateField() {
        return startDateField;
    }

    @Override
    public GanttChartModelImpl setStartDateField(String startDateField) {
        this.startDateField = startDateField;
        return this;
    }

    @Override
    public String getStartField() {
        return startField;
    }

    @Override
    public GanttChartModelImpl setStartField(String startField) {
        this.startField = startField;
        return this;
    }

    @Override
    public ValueAxis getValueAxis() {
        return valueAxis;
    }

    @Override
    public GanttChartModelImpl setValueAxis(ValueAxis valueAxis) {
        this.valueAxis = valueAxis;
        return this;
    }

    @Override
    public List<String> getAdditionalSegmentFields() {
        return additionalSegmentFields;
    }

    @Override
    public GanttChartModelImpl setAdditionalSegmentFields(List<String> additionalSegmentFields) {
        this.additionalSegmentFields = additionalSegmentFields;
        return this;
    }

    @Override
    public GanttChartModelImpl addAdditionalSegmentFields(String... fields) {
        if (additionalSegmentFields == null) {
            additionalSegmentFields = new ArrayList<>();
        }
        additionalSegmentFields.addAll(Arrays.asList(fields));
        return this;
    }
}
