/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.widget.client.amstockcharts.events;

import com.google.gwt.core.client.JavaScriptObject;
import io.jmix.ui.widget.client.JsDate;

public class JsStockPanelZoomEvent extends JavaScriptObject {

    protected JsStockPanelZoomEvent() {
    }

    public final native String getPeriod() /*-{
        if (this.period) {
            return this.period;
        }
        return null;
    }-*/;

    public final native JsDate getStartDate() /*-{
        return this.startDate;
    }-*/;

    public final native JsDate getEndDate() /*-{
        return this.endDate;
    }-*/;
}
