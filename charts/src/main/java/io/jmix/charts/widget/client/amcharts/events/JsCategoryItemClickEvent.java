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

public class JsCategoryItemClickEvent extends JavaScriptObject {

    protected JsCategoryItemClickEvent() {
    }

    public final native String getValue() /*-{
        if (this.value) {
            return this.value;
        }
        return null;
    }-*/;

    public final native int getX() /*-{
        return this.event.x;
    }-*/;

    public final native int getY() /*-{
        return this.event.y;
    }-*/;

    public final native int getOffsetX() /*-{
        return this.event.offsetX;
    }-*/;

    public final native int getOffsetY() /*-{
        return this.event.offsetY;
    }-*/;

    public final native int getXAxis() /*-{
        return this.axis.x;
    }-*/;

    public final native int getYAxis() /*-{
        return this.axis.y;
    }-*/;
}
