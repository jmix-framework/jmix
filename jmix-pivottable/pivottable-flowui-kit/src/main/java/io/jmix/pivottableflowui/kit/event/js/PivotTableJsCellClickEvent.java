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

package io.jmix.pivottableflowui.kit.event.js;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;

@DomEvent(PivotTableJsCellClickEvent.EVENT_NAME)
public class PivotTableJsCellClickEvent extends PivotTableAbstractJsEvent {

    public static final String EVENT_NAME = "jmix-pivottable:cellclick";

    public PivotTableJsCellClickEvent(JmixPivotTable pivotTable, boolean fromClient,
                                      @EventData("event.detail") JsonObject params) {
        super(pivotTable, fromClient, params);
    }
}
