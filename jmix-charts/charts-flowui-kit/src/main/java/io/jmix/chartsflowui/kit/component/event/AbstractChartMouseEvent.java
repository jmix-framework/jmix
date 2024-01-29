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

import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.event.dto.BaseChartEventDetail;

public class AbstractChartMouseEvent<T extends BaseChartEventDetail> extends AbstractChartEvent<T> {

    protected String value;

    protected AbstractChartMouseEvent(JmixChart source, boolean fromClient,
                                      JsonObject detail, JsonValue value, Class<T> detailClass) {
        super(source, fromClient, detail, detailClass);
        if (value instanceof JsonObject) {
            this.value = value.toJson();
        } else {
            if (value != null) {
                this.value = value.asString();
            }
        }
    }

    public String getValue() {
        return value;
    }

}
