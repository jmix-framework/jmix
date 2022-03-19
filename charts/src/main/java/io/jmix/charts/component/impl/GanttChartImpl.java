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


import io.jmix.charts.component.GanttChart;
import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.impl.GanttChartModelImpl;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.graph.Graph;

import java.util.Date;
import java.util.List;

public class GanttChartImpl extends SeriesBasedChartImpl<GanttChart, GanttChartModelImpl>
        implements GanttChart {

    @Override
    protected GanttChartModelImpl createChartConfiguration() {
        return new GanttChartModelImpl();
    }

    @Override
    public Integer getBrightnessStep() {
        return getModel().getBrightnessStep();
    }

    @Override
    public GanttChart setBrightnessStep(Integer brightnessStep) {
        getModel().setBrightnessStep(brightnessStep);
        return this;
    }

    @Override
    public String getColorField() {
        return getModel().getColorField();
    }

    @Override
    public GanttChart setColorField(String colorField) {
        getModel().setColorField(colorField);
        return this;
    }

    @Override
    public String getColumnWidthField() {
        return getModel().getColumnWidthField();
    }

    @Override
    public GanttChart setColumnWidthField(String columnWidthField) {
        getModel().setColumnWidthField(columnWidthField);
        return this;
    }

    @Override
    public String getDurationField() {
        return getModel().getDurationField();
    }

    @Override
    public GanttChart setDurationField(String durationField) {
        getModel().setDurationField(durationField);
        return this;
    }

    @Override
    public String getEndDateField() {
        return getModel().getEndDateField();
    }

    @Override
    public GanttChart setEndDateField(String endDateField) {
        getModel().setEndDateField(endDateField);
        return this;
    }

    @Override
    public String getEndField() {
        return getModel().getEndField();
    }

    @Override
    public GanttChart setEndField(String endField) {
        getModel().setEndField(endField);
        return this;
    }

    @Override
    public Graph getGraph() {
        return getModel().getGraph();
    }

    @Override
    public GanttChart setGraph(Graph graph) {
        getModel().setGraph(graph);
        return this;
    }

    @Override
    public DatePeriod getPeriod() {
        return getModel().getPeriod();
    }

    @Override
    public GanttChart setPeriod(DatePeriod period) {
        getModel().setPeriod(period);
        return this;
    }

    @Override
    public String getSegmentsField() {
        return getModel().getSegmentsField();
    }

    @Override
    public GanttChart setSegmentsField(String segmentsField) {
        getModel().setSegmentsField(segmentsField);
        return this;
    }

    @Override
    public Date getStartDate() {
        return getModel().getStartDate();
    }

    @Override
    public GanttChart setStartDate(Date startDate) {
        getModel().setStartDate(startDate);
        return this;
    }

    @Override
    public String getStartDateField() {
        return getModel().getStartDateField();
    }

    @Override
    public GanttChart setStartDateField(String startDateField) {
        getModel().setStartDateField(startDateField);
        return this;
    }

    @Override
    public String getStartField() {
        return getModel().getStartField();
    }

    @Override
    public GanttChart setStartField(String startField) {
        getModel().setStartField(startField);
        return this;
    }

    @Override
    public ValueAxis getValueAxis() {
        return getModel().getValueAxis();
    }

    @Override
    public GanttChart setValueAxis(ValueAxis valueAxis) {
        getModel().setValueAxis(valueAxis);
        return this;
    }

    @Override
    public List<String> getAdditionalSegmentFields() {
        return getModel().getAdditionalSegmentFields();
    }

    @Override
    public GanttChart setAdditionalSegmentFields(List<String> additionalSegmentFields) {
        getModel().setAdditionalSegmentFields(additionalSegmentFields);
        return this;
    }

    @Override
    public GanttChart addAdditionalSegmentFields(String... fields) {
        getModel().addAdditionalSegmentFields(fields);
        return this;
    }
}