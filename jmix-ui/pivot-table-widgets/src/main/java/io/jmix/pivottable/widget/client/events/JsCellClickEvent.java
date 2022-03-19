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

package io.jmix.pivottable.widget.client.events;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.List;
import java.util.Map;

public class JsCellClickEvent extends JavaScriptObject {
    protected JsCellClickEvent() {
    }

    public final native Double getValue() /*-{
        return this.value;
    }-*/;

    public final native Map<String, String> getFilters() /*-{
        return this.filters;
    }-*/;

    public final native List<String> getDataItemKeys() /*-{
        return this.dataItemKeys;
    }-*/;
}
