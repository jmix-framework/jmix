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

public class JmixChartLegendScrollEventDetail extends BaseChartEventDetail {

    protected Integer scrollDataIndex;

    protected String legendId;

    public Integer getScrollDataIndex() {
        return scrollDataIndex;
    }

    public void setScrollDataIndex(Integer scrollDataIndex) {
        this.scrollDataIndex = scrollDataIndex;
    }

    public String getLegendId() {
        return legendId;
    }

    public void setLegendId(String legendId) {
        this.legendId = legendId;
    }
}
