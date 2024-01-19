/*
 * Copyright 2022 Haulmont.
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

public class JmixClickEventDetail {
    private String componentType;
    private Integer componentIndex;
    private Integer xAxisIndex;
    private String targetType;
    private String value;
    private Integer tickIndex;
    private Integer dataIndex;
    private String type;

    private TestDTO testDTO;

    public TestDTO getTestDTO() {
        return testDTO;
    }

    public void setTestDTO(TestDTO testDTO) {
        this.testDTO = testDTO;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Integer getComponentIndex() {
        return componentIndex;
    }

    public void setComponentIndex(Integer componentIndex) {
        this.componentIndex = componentIndex;
    }

    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
