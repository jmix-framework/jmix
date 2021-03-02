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

package io.jmix.pivottable.widget.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import io.jmix.pivottable.widget.client.events.JsCellClickEvent;
import io.jmix.pivottable.widget.client.events.JsRefreshEvent;

import java.util.function.Consumer;

public class JmixPivotTableJsOverlay {
    protected JavaScriptObject pivotTable;

    public JmixPivotTableJsOverlay(JavaScriptObject pivotTable) {
        this.pivotTable = pivotTable;
    }

    public static JmixPivotTableJsOverlay makePivot(Element placeHolder, JavaScriptObject configObject,
                                                    Consumer<JsRefreshEvent> refreshHandler,
                                                    Consumer<JsCellClickEvent> cellClickHandler,
                                                    boolean enabled) {
        return new JmixPivotTableJsOverlay(makeJsPivotTable(placeHolder, configObject,
                refreshHandler, cellClickHandler, enabled));
    }

    protected static native JavaScriptObject makeJsPivotTable(Element placeHolder, JavaScriptObject configObject,
                                                              Consumer<JsRefreshEvent> refreshHandler,
                                                              Consumer<JsCellClickEvent> cellClickHandler,
                                                              boolean enabled) /*-{
        if (refreshHandler) {
            configObject.options["onRefresh"] = $entry(function (config) {
                refreshHandler.@java.util.function.Consumer::accept(*)(config);
            });
        }

        if (cellClickHandler) {
            var cfg = {
                rendererOptions: {
                    table: {
                        clickCallback: $entry(function (e, value, filters, pivotData) {
                            var dataItemKeys = [];
                            var populateDataItemKeys = function (item) {
                                dataItemKeys.push(item["$k"]);
                            };
                            pivotData.forEachMatchingRecord(filters, populateDataItemKeys);
                            var event = {
                                value: value,
                                filters: filters,
                                dataItemKeys: dataItemKeys
                            };
                            cellClickHandler.@java.util.function.Consumer::accept(*)(event);
                        })
                    }
                }
            };
            @io.jmix.pivottable.widget.client.utils.JsUtils::merge(*)(configObject.options, cfg);
        }

        var pivot;
        if (configObject.options.editable) {
            // The 3rd input parameter is `overwrite`, which controls what happens
            // if `pivotUI` is called repeatedly on the same element. We set `overwrite=true`
            // so that the options object overwrites the current state of the UI.
            pivot = $wnd.jQuery(placeHolder).pivotUI(configObject.data,
                configObject.options, true, configObject.options.localeCode);

            if (!enabled) {
                pivot.find("select").attr('disabled', 'disabled');
            }

        } else {
            pivot = $wnd.jQuery(placeHolder).pivot(configObject.data, configObject.options);
        }
        pivot.attr('empty-data-message', configObject.emptyDataMessage);

        return pivot;
    }-*/;
}
