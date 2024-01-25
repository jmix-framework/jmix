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

import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;
import io.jmix.chartsflowui.kit.component.JmixChart;


public class JmixChartEvent extends ComponentEvent<JmixChart> {

    public static final String EVENT_NAME_PREFIX = "jmixchart";

    protected JsonObject detailJson;
    protected Object detail = null;
    protected String value;

    public JmixChartEvent(JmixChart source, boolean fromClient,
                          JsonObject detail) {
        super(source, fromClient);
        this.detailJson = detail;
    }

    public JsonObject getDetailJson() {
        return detailJson;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public void setDetailJson(JsonObject detailJson) {
        this.detailJson = detailJson;
    }
}