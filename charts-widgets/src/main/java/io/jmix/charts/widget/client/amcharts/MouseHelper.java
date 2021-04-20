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

package io.jmix.charts.widget.client.amcharts;

import com.google.gwt.dom.client.NativeEvent;

public class MouseHelper {

    public static native int getX(NativeEvent e) /*-{
        return (typeof e.offsetX == "number") ? e.offsetX : e.layerX || 0;
    }-*/;

    public static native int getY(NativeEvent e) /*-{
        return (typeof e.offsetY == "number") ? e.offsetY : e.layerY || 0;
    }-*/;
}