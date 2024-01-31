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

public class JmixChartDataEventDetail extends BaseChartEventDetail {

    protected Integer seriesIndex;

    protected Integer dataIndexInside;

    protected Integer dataIndex;

    protected Boolean escapeConnect;

    protected Boolean notBlur;

    public Integer getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(Integer seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public Integer getDataIndexInside() {
        return dataIndexInside;
    }

    public void setDataIndexInside(Integer dataIndexInside) {
        this.dataIndexInside = dataIndexInside;
    }

    public Integer getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(Integer dataIndex) {
        this.dataIndex = dataIndex;
    }

    public Boolean getEscapeConnect() {
        return escapeConnect;
    }

    public void setEscapeConnect(Boolean escapeConnect) {
        this.escapeConnect = escapeConnect;
    }

    public Boolean getNotBlur() {
        return notBlur;
    }

    public void setNotBlur(Boolean notBlur) {
        this.notBlur = notBlur;
    }
}
