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

package io.jmix.pivottableflowui.kit.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;

/**
 * Describes the PivotTable refresh event. Fires each time the state of the pivot table changes.
 */
@DomEvent(PivotTableRefreshEvent.EVENT_NAME)
public class PivotTableRefreshEvent extends ComponentEvent<JmixPivotTable> {

    public static final String EVENT_NAME = "jmix-pivottable:refresh";

    protected JsonObject detailJson;
    protected PivotTableRefreshEventDetail detail;

    public PivotTableRefreshEvent(JmixPivotTable pivotTable, boolean fromClient,
                                  @EventData("event.detail") JsonObject detailJson) {
        super(pivotTable, fromClient);
        this.detailJson = detailJson;
    }

    public PivotTableRefreshEventDetail getDetail() {
        if (detail == null) {
            JmixPivotTableSerializer serializer = new JmixPivotTableSerializer();
            detail = (PivotTableRefreshEventDetail) serializer.deserialize(
                    detailJson, PivotTableRefreshEventDetail.class);
        }
        return detail;
    }
}
