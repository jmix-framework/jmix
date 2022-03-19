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

package io.jmix.charts.model.period;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

@StudioElement(
        caption = "Period",
        xmlElement = "period",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Period extends AbstractChartObject {

    private static final long serialVersionUID = 1078558318840294205L;

    private PeriodType period;
    
    private Integer count;
    
    private String label;
    
    private Boolean selected;

    public PeriodType getPeriod() {
        return period;
    }

    @StudioProperty(type = PropertyType.ENUMERATION)
    public Period setPeriod(PeriodType period) {
        this.period = period;
        return this;
    }

    /**
     * @return  count of periods
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets how many periods button will select.
     *
     * @param count count
     * @return period
     */
    @StudioProperty
    public Period setCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * @return text label that is displayed on the button
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets text label that will be displayed on the button.
     *
     * @param label label string
     * @return period
     */
    @StudioProperty
    public Period setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * @return true if this button is selected when the chart is initialized
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Set selected to true if this button should be selected when the chart is initialized.
     *
     * @param selected selected option
     * @return period
     */
    @StudioProperty
    public Period setSelected(Boolean selected) {
        this.selected = selected;
        return this;
    }
}