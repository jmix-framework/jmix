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

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Base detail for mouse events.
 */
public abstract class AbstractChartMouseEventDetail extends AbstractChartComponentEventDetail {

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

    protected Integer singleData;

    @Nullable
    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Nullable
    public List<String> getDimensionNames() {
        return dimensionNames;
    }

    public void setDimensionNames(List<String> dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    @Nullable
    public Map<String, Object> getEncode() {
        return encode;
    }

    public void setEncode(Map<String, Object> encode) {
        this.encode = encode;
    }

    @Nullable
    public List<Object> get$vars() {
        return $vars;
    }

    public void set$vars(List<Object> $vars) {
        this.$vars = $vars;
    }

    @Nullable
    public Integer getTickIndex() {
        return tickIndex;
    }

    public void setTickIndex(Integer tickIndex) {
        this.tickIndex = tickIndex;
    }

    @Nullable
    public Integer getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(Integer dataIndex) {
        this.dataIndex = dataIndex;
    }

    @Nullable
    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
    }

    @Nullable
    public Integer getYAxisIndex() {
        return yAxisIndex;
    }

    public void setYAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Nullable
    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    @Nullable
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Nullable
    public Integer getSingleData() {
        return singleData;
    }

    public void setSingleData(Integer singleData) {
        this.singleData = singleData;
    }
}
