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

import io.jmix.chartsflowui.kit.component.event.ChartDataZoomEvent;

import java.util.List;

/**
 * @see ChartDataZoomEvent
 */
public class ChartDataZoomEventDetail extends BaseChartEventDetail {

    protected String dataZoomId;

    protected String from;

    protected Integer start;

    protected Integer end;

    protected Integer startValue;

    protected Integer endValue;

    protected List<ChartDataZoomEventDetail> batch;

    public String getDataZoomId() {
        return dataZoomId;
    }

    public void setDataZoomId(String dataZoomId) {
        this.dataZoomId = dataZoomId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<ChartDataZoomEventDetail> getBatch() {
        return batch;
    }

    public void setBatch(List<ChartDataZoomEventDetail> batch) {
        this.batch = batch;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getStartValue() {
        return startValue;
    }

    public void setStartValue(Integer startValue) {
        this.startValue = startValue;
    }

    public Integer getEndValue() {
        return endValue;
    }

    public void setEndValue(Integer endValue) {
        this.endValue = endValue;
    }
}
