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

public class JmixChartMouseEventDetail extends JmixChartEventDetail {
    private String componentType;
    private String componentSubType;
    private Integer componentIndex;
    private Integer seriesIndex;
    private Integer xAxisIndex;
    private String seriesName;
    private String name;
    private String seriesType;
    private String targetType;
    private String value;
    private String color;
    private List<String> dimensionNames;
    private Map<String, String> encode;
    private List<String> $vars;
    private Integer tickIndex;
    private Integer dataIndex;
    private Map<String, String> data;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public Map<String, String> getEncode() {
        return encode;
    }

    public void setEncode(Map<String, String> encode) {
        this.encode = encode;
    }

    public List<String> get$vars() {
        return $vars;
    }

    public void set$vars(List<String> $vars) {
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

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentSubType() {
        return componentSubType;
    }

    public void setComponentSubType(String componentSubType) {
        this.componentSubType = componentSubType;
    }

    public Integer getComponentIndex() {
        return componentIndex;
    }

    public void setComponentIndex(Integer componentIndex) {
        this.componentIndex = componentIndex;
    }

    public Integer getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(Integer seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
