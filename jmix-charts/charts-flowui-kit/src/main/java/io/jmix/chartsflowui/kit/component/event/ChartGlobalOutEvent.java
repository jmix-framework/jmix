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

package io.jmix.chartsflowui.kit.component.event;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.event.dto.ChartGlobalOutEventDetail;

/**
 * Event fired if the mouse cursor moves outside the component.
 */
@DomEvent(ChartGlobalOutEvent.EVENT_NAME)
public class ChartGlobalOutEvent extends AbstractChartEvent<ChartGlobalOutEventDetail> {

    public static final String EVENT_NAME = EVENT_NAME_PREFIX + "globalout";

    public ChartGlobalOutEvent(JmixChart source, boolean fromClient,
                               @EventData("event.detail") JsonObject detail) {
        super(source, fromClient, detail, ChartGlobalOutEventDetail.class);
    }
}
