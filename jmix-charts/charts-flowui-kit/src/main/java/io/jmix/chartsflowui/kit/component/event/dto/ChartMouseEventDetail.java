/*
 * Copyright 2024 Haulmont.
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

package io.jmix.chartsflowui.kit.component.event.dto;

import java.util.List;
import java.util.Map;

public class ChartMouseEventDetail extends ChartComponentEventDetail {

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected String name;

    protected Object value;

    protected String targetType;

    protected Double percent;

    protected String color;

    protected List<String> dimensionNames;

    protected Map<String, Object> encode;

    protected List<Object> $vars;

    protected Integer tickIndex;

    protected Integer dataIndex;

    protected Map<String, Object> data;

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getDimensionNames() {
        return dimensionNames;
    }

    public void setDimensionNames(List<String> dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    public Integer getxAxisIndex() {
        return xAxisIndex;
    }

    public void setxAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
    }

    public Integer getyAxisIndex() {
        return yAxisIndex;
    }

    public void setyAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }

    public Map<String, Object> getEncode() {
        return encode;
    }

    public void setEncode(Map<String, Object> encode) {
        this.encode = encode;
    }

    public List<Object> get$vars() {
        return $vars;
    }

    public void set$vars(List<Object> $vars) {
        this.$vars = $vars;
    }

    public Integer getTickIndex() {
        return tickIndex;
    }

    public void setTickIndex(Integer tickIndex) {
        this.tickIndex = tickIndex;
    }

    public Integer getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(Integer dataIndex) {
        this.dataIndex = dataIndex;
    }

    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
    }

    public Integer getYAxisIndex() {
        return yAxisIndex;
    }

    public void setYAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
