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


import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.legend.AbstractLegend;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * StockLegend is a legend of {@link StockPanel}.
 * <br>
 * See documentation for properties of StockLegend JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/StockLegend">http://docs.amcharts.com/3/javascriptstockchart/StockLegend</a>
 */
@StudioElement(
        caption = "StockLegend",
        xmlElement = "stockLegend",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class StockLegend extends AbstractLegend<StockLegend> {

    private static final long serialVersionUID = -5155850515331784520L;

    private String periodValueTextComparing;

    private String periodValueTextRegular;

    private String valueTextComparing;

    private String valueTextRegular;

    /**
     * @return period value text comparing
     */
    public String getPeriodValueTextComparing() {
        return periodValueTextComparing;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when user is not hovering above any
     * data point and the data sets are compared. The tags should be made out of two parts - the name of a field
     * (value / open / close / high / low) and the value of the period you want to be show - open / close / high /
     * low / sum / average / count. For example: [[value.sum]] means that sum of all data points of value field in
     * the selected period will be displayed. In case you want to display percent values, you should add "percent"
     * string in front of a tag, for example: [[percents.value.close]] means that last percent value of a period will
     * be displayed.
     *
     * @param periodValueTextComparing period value text comparing
     * @return stock legend
     */
    @StudioProperty
    public StockLegend setPeriodValueTextComparing(String periodValueTextComparing) {
        this.periodValueTextComparing = periodValueTextComparing;
        return this;
    }

    /**
     * @return period value text regular
     */
    public String getPeriodValueTextRegular() {
        return periodValueTextRegular;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when user is not hovering above any
     * data point. The tags should be made out of two parts - the name of a field (value / open / close / high / low)
     * and the value of the period you want to be show - open / close / high / low / sum / average / count. For
     * example: [[value.sum]] means that sum of all data points of value field in the selected period will be displayed.
     *
     * @param periodValueTextRegular period value text regular
     * @return stock legend
     */
    @StudioProperty
    public StockLegend setPeriodValueTextRegular(String periodValueTextRegular) {
        this.periodValueTextRegular = periodValueTextRegular;
        return this;
    }

    /**
     * @return value text comparing
     */
    public String getValueTextComparing() {
        return valueTextComparing;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when graph is comparable and at least
     * one data set is selected for comparing. You can use tags like [[value]], [[open]], [[high]], [[low]], [[close]],
     * [[percents.value/open/close/low/high]], [[description]]. If not set the default value is "[[percents.value]]%".
     *
     * @param valueTextComparing value text comparing
     * @return stock legend
     */
    @StudioProperty(defaultValue = "[[percents.value]]%")
    public StockLegend setValueTextComparing(String valueTextComparing) {
        this.valueTextComparing = valueTextComparing;
        return this;
    }

    /**
     * @return value text regular
     */
    public String getValueTextRegular() {
        return valueTextRegular;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend. You can use tags like [[value]],
     * [[open]], [[high]], [[low]], [[close]], [[percents]], [[description]]. If not set the default value is
     * "[[value]]".
     *
     * @param valueTextRegular value text regular
     * @return stock legend
     */
    @StudioProperty(defaultValue = "[[value]]")
    public StockLegend setValueTextRegular(String valueTextRegular) {
        this.valueTextRegular = valueTextRegular;
        return this;
    }
}
