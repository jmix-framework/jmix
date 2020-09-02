/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.core.entity.annotation.SystemLevel;

import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@ModelObject(name = "report$SerialChartDescription")
@SystemLevel
public class SerialChartDescription extends AbstractChartDescription {
    @ModelProperty(mandatory = true)
    protected String bandName;
    @ModelProperty(mandatory = true)
    protected String categoryField;
    @ModelProperty
    protected String categoryAxisCaption;
    @ModelProperty
    protected String valueAxisCaption;
    @ModelProperty
    protected String valueAxisUnits;
    @ModelProperty
    protected String valueStackType;
    @OrderBy("order")
    @ModelProperty
    protected List<ChartSeries> series = new ArrayList<>();
    @ModelProperty
    protected Integer categoryAxisLabelRotation = 0;

    public SerialChartDescription() {
        super(ChartType.SERIAL.getId());
    }

    public String getCategoryAxisCaption() {
        return categoryAxisCaption;
    }

    public void setCategoryAxisCaption(String categoryAxisCaption) {
        this.categoryAxisCaption = categoryAxisCaption;
    }

    public String getValueAxisCaption() {
        return valueAxisCaption;
    }

    public void setValueAxisCaption(String valueAxisCaption) {
        this.valueAxisCaption = valueAxisCaption;
    }

    public String getValueAxisUnits() {
        return valueAxisUnits;
    }

    public void setValueAxisUnits(String valueAxisUnits) {
        this.valueAxisUnits = valueAxisUnits;
    }

    public StackType getValueStackType() {
        return StackType.fromId(valueStackType);
    }

    public void setValueStackType(StackType valueStackType) {
        this.valueStackType = valueStackType != null ? valueStackType.getId() : null;
    }

    public List<ChartSeries> getSeries() {
        return series;
    }

    public void setSeries(List<ChartSeries> series) {
        this.series = series;
    }

    public String getCategoryField() {
        return categoryField;
    }

    public void setCategoryField(String categoryField) {
        this.categoryField = categoryField;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public Integer getCategoryAxisLabelRotation() {
        return categoryAxisLabelRotation;
    }

    public void setCategoryAxisLabelRotation(Integer categoryAxisLabelRotation) {
        this.categoryAxisLabelRotation = categoryAxisLabelRotation;
    }
}
