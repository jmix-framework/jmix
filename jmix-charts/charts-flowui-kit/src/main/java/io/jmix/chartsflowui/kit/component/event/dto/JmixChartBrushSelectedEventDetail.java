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

public class JmixChartBrushSelectedEventDetail extends JmixChartEventDetail {

    private String brushId;

    private Integer brushIndex;

    private String brushName;

    private List<JmixBrushArea> areas;

    private List<JmixBrushSelected> selected;

    private List<JmixChartBrushSelectedEventDetail> batch;

    public String getBrushId() {
        return brushId;
    }

    public void setBrushId(String brushId) {
        this.brushId = brushId;
    }

    public Integer getBrushIndex() {
        return brushIndex;
    }

    public void setBrushIndex(Integer brushIndex) {
        this.brushIndex = brushIndex;
    }

    public String getBrushName() {
        return brushName;
    }

    public void setBrushName(String brushName) {
        this.brushName = brushName;
    }

    public List<JmixBrushArea> getAreas() {
        return areas;
    }

    public void setAreas(List<JmixBrushArea> areas) {
        this.areas = areas;
    }

    public List<JmixBrushSelected> getSelected() {
        return selected;
    }

    public void setSelected(List<JmixBrushSelected> selected) {
        this.selected = selected;
    }

    public List<JmixChartBrushSelectedEventDetail> getBatch() {
        return batch;
    }

    public void setBatch(List<JmixChartBrushSelectedEventDetail> batch) {
        this.batch = batch;
    }
}
