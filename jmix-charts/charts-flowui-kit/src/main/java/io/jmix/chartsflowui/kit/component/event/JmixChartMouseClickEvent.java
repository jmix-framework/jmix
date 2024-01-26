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
import io.jmix.chartsflowui.kit.component.JmixChart;


public class JmixChartMouseClickEvent<T> extends JmixChartMouseEvent {

    protected String value;

    public JmixChartMouseClickEvent(JmixChart source, boolean fromClient,
                                    JsonObject detail, String value, Class<T> detailClass) {
        super(source, fromClient, detail, detailClass);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}