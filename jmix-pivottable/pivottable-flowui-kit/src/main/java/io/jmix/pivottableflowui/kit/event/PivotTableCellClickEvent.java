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
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;

import java.util.LinkedList;
import java.util.List;

/**
 * Pivot table cell click event sent from the client.
 */
@DomEvent(PivotTableCellClickEvent.EVENT_NAME)
public class PivotTableCellClickEvent<T> extends ComponentEvent<JmixPivotTable<T>> {

    public static final String EVENT_NAME = "jmix-pivottable:cellclick";

    protected JsonObject detailJson;
    protected PivotTableCellClickEventDetail detail;

    public PivotTableCellClickEvent(JmixPivotTable<T> pivotTable, boolean fromClient,
                                    @EventData("event.detail") JsonObject detailJson) {
        super(pivotTable, fromClient);
        this.detailJson = detailJson;
    }

    public PivotTableCellClickEventDetail getDetail() {
        if (detail == null) {
            JmixPivotTableSerializer serializer = new JmixPivotTableSerializer();
            detail = (PivotTableCellClickEventDetail) serializer.deserialize(
                    detailJson, PivotTableCellClickEventDetail.class);

            List<T> clickedItems = new LinkedList<>();
            JsonArray dataItemsKeys = detailJson.getArray("itemsKeys");
            if (dataItemsKeys != null) {
                for (int i = 0; i < dataItemsKeys.length(); i++) {
                    String key = dataItemsKeys.get(i).asString();
                    T item = getSource().getItems().getItem(key);
                    if (item != null) {
                        clickedItems.add(item);
                    }
                }
            }
            detail.setItems(clickedItems);
        }
        return detail;
    }
}
