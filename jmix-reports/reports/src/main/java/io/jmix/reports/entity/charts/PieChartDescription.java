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

package io.jmix.reports.entity.charts;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

@JmixEntity(name = "report_PieChartDescriptor")
@SystemLevel
public class PieChartDescription extends AbstractChartDescription {
    @JmixProperty(mandatory = true)
    protected String bandName;
    @JmixProperty(mandatory = true)
    protected String titleField;
    @JmixProperty(mandatory = true)
    protected String valueField;
    @JmixProperty
    protected String colorField;
    @JmixProperty
    protected String units;

    public PieChartDescription() {
        super(ChartType.PIE.getId());
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public String getTitleField() {
        return titleField;
    }

    public void setTitleField(String titleField) {
        this.titleField = titleField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getColorField() {
        return colorField;
    }

    public void setColorField(String colorField) {
        this.colorField = colorField;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
