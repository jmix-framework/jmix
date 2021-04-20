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

public class JsRefreshEvent extends JavaScriptObject {

    protected JsRefreshEvent() {
    }

    public final native List<String> getRows() /*-{
        return this.rows;
    }-*/;

    public final native List<String> getCols() /*-{
        return this.cols;
    }-*/;

    public final native String getRenderer() /*-{
        var localeMapping = $wnd.$.pivotUtilities.locales[this.localeCode].renderersLocaleMapping;
        return @io.jmix.pivottable.widget.client.utils.JsUtils::getKeyByValue(*)(localeMapping, this.rendererName);
    }-*/;

    public final native String getAggregation() /*-{
        // if we define custom aggregation list, we use unique ids
        // to identify certain aggregator in case of using the same
        // aggregation mode for different aggregators
        if (this.aggregatorsIds) {
            return this.aggregatorsIds[this.aggregatorName];
        } else {
            var localeMapping = $wnd.$.pivotUtilities.locales[this.localeCode].aggregatorsLocaleMapping;
            return @io.jmix.pivottable.widget.client.utils.JsUtils::getKeyByValue(*)(localeMapping, this.aggregatorName);
        }
    }-*/;

    public final native List<String> getAggregationProperties() /*-{
        return this.vals;
    }-*/;

    public final native Map<String, List<String>> getInclusions() /*-{
        return this.inclusions;
    }-*/;

    public final native Map<String, List<String>> getExclusions() /*-{
        return this.exclusions;
    }-*/;

    public final native String getColumnOrder() /*-{
        return this.colOrder;
    }-*/;

    public final native String getRowOrder() /*-{
        return this.rowOrder;
    }-*/;
}
