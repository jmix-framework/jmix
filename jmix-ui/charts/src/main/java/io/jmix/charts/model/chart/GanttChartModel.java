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

package io.jmix.charts.model.chart;


import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.graph.Graph;
import io.jmix.ui.meta.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

@StudioProperties(groups = {
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "colorField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "columnWidthField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "durationField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "endDateField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "endField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "segmentsField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "startDateField"}),
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "startField"})
})
public interface GanttChartModel<T extends GanttChartModel> extends SeriesBasedChartModel<T> {
    /**
     * @return brightness step
     */
    Integer getBrightnessStep();

    /**
     * Sets brightness step. Lightness increase of each subsequent bar of one series. Value range is from -255 to 255.
     *
     * @param brightnessStep brightness step
     * @return  gantt chart model
     */
    @StudioProperty
    @Max(255)
    @Min(-255)
    T setBrightnessStep(Integer brightnessStep);

    /**
     * @return segment color field
     */
    String getColorField();

    /**
     * Sets segment color field from your data provider.
     *
     * @param colorField color field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setColorField(String colorField);

    /**
     * @return column width field
     */
    String getColumnWidthField();

    /**
     * Sets field of column width of a segments from your data provider.
     *
     * @param columnWidthField column width field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setColumnWidthField(String columnWidthField);

    /**
     * @return duration field
     */
    String getDurationField();

    /**
     * Sets duration field. Instead of specifying end date or end value in your data, you can specify duration of a
     * segment.
     *
     * @param durationField duration field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setDurationField(String durationField);

    /**
     * @return end date field
     */
    String getEndDateField();

    /**
     * Sets end date field from your data provider which holds end date of a segment.
     *
     * @param endDateField end date field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setEndDateField(String endDateField);

    /**
     * @return end field
     */
    String getEndField();

    /**
     * Sets end field from your data provider which holds end value of a segment. If your data is date-based, you
     * should use endDateField instead, unless you specified startDate and period values. In this case you can use
     * endField and set number of periods instead of providing exact end date. If not set the default value is "ss".
     *
     * @param endField end field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setEndField(String endField);

    /**
     * @return graph
     */
    Graph getGraph();

    /**
     * Sets graph of a Gantt chart. Gantt chart actually creates multiple graphs (separate for each segment).
     * Properties of this graph are passed to each of the created graphs - this allows you to control the look of
     * segments.
     *
     * @param graph the graph
     * @return  gantt chart model
     */
    @StudioElement
    T setGraph(Graph graph);

    /**
     * @return data period
     */
    DatePeriod getPeriod();

    /**
     * Sets data period. Used only value axis is date-based.
     *
     * @param period the period
     * @return  gantt chart model
     */
    T setPeriod(DatePeriod period);

    /**
     * @return segments field
     */
    String getSegmentsField();

    /**
     * Sets segments field in your data provider.
     *
     * @param segmentsField segments field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setSegmentsField(String segmentsField);

    /**
     * @return start date
     */
    Date getStartDate();

    /**
     * Sets initial date of value axis. If you set this date, then you can set start, end, duration of segments using
     * number of periods instead of providing exact dates.
     *
     * @param startDate the start date
     * @return  gantt chart model
     */
    @StudioProperty
    T setStartDate(Date startDate);

    /**
     * @return start date field
     */
    String getStartDateField();

    /**
     * Sets date field from your data provider which holds start date of a segment.
     *
     * @param startDateField start date field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setStartDateField(String startDateField);

    /**
     * @return start field
     */
    String getStartField();

    /**
     * Sets start field from your data provider which holds start value of a segment. If your data is date-based, you
     * should use startDateField instead, unless you specified startDate and period values. In this case you can use
     * startField and set number of periods instead of providing exact start date.
     *
     * @param startField start field string
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setStartField(String startField);

    /**
     * @return value axis
     */
    ValueAxis getValueAxis();

    /**
     * Sets value axis of Gantt chart. Set it's type to "date" if your data is date or time based.
     *
     * @param valueAxis the value axis
     * @return  gantt chart model
     */
    @StudioElement
    T setValueAxis(ValueAxis valueAxis);

    /**
     * @return additional segment fields that should be fetched from the data provider
     */
    List<String> getAdditionalSegmentFields();

    /**
     * Sets the list of additional segment fields that should be fetched from the data provider, similarly to the
     * {@link AbstractChart#additionalFields additionalFields} attribute.
     *
     * @param additionalSegmentFields list of additional segment fields
     * @return  gantt chart model
     */
    @StudioProperty(type = PropertyType.STRING)
    T setAdditionalSegmentFields(List<String> additionalSegmentFields);

    /**
     * Adds additional segment fields.
     *
     * @param fields the fields
     * @return  gantt chart model
     */
    T addAdditionalSegmentFields(String... fields);
}