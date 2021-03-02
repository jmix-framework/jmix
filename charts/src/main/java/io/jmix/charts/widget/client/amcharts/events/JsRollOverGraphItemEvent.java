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

package io.jmix.charts.widget.client.amcharts.events;

import com.google.gwt.core.client.JavaScriptObject;

public class JsRollOverGraphItemEvent extends JavaScriptObject {

    protected JsRollOverGraphItemEvent() {
    }

    public final native String getGraphId() /*-{
        if (this.graph) {
            return this.graph.id;
        }
        return null;
    }-*/;

    public final native int getIndex() /*-{
        return this.index;
    }-*/;

    public final native String getItemKey() /*-{
        if (this.chart.type == "gantt") {
            if (this.graph && this.graph.customData && this.chart.dataProvider[this.index]) {
                if (typeof(this.graph.customData.$i) === "undefined"
                    || typeof(this.chart.dataProvider[this.index].$k) == "undefined") {
                    return null;
                }
                //noinspection JSUnresolvedVariable
                return this.chart.dataProvider[this.index].$k + ":" + this.graph.customData.$i;
            }
        } else if (this.item && this.item.dataContext) {
            //noinspection JSUnresolvedVariable
            if (!this.item.dataContext.$k) {
                return null;
            }

            return this.item.dataContext.$k;
        }
        return null;
    }-*/;
}
