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

import io.jmix.chartsflowui.kit.component.event.ChartLegendSelectChangedEvent;

import java.util.Map;

/**
 * @see ChartLegendSelectChangedEvent
 */
public class ChartLegendSelectChangedEventDetail extends BaseChartEventDetail {

    protected String name;

    protected Map<String, Boolean> selected;

    protected Boolean isFromClick;

    protected Map<String, Object> fromActionPayload;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getSelected() {
        return selected;
    }

    public void setSelected(Map<String, Boolean> selected) {
        this.selected = selected;
    }

    public Boolean getIsFromClick() {
        return isFromClick;
    }

    public void setIsFromClick(Boolean fromClick) {
        isFromClick = fromClick;
    }

    public Map<String, Object> getFromActionPayload() {
        return fromActionPayload;
    }

    public void setFromActionPayload(Map<String, Object> fromActionPayload) {
        this.fromActionPayload = fromActionPayload;
    }

}
