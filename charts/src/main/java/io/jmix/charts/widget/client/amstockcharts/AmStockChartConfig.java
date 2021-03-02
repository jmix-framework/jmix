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

package io.jmix.charts.widget.client.amstockcharts;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.vaadin.client.BrowserInfo;
import io.jmix.charts.widget.client.utils.JsUtils;

public class AmStockChartConfig extends JavaScriptObject {

    protected AmStockChartConfig() {
    }

    public static AmStockChartConfig fromServerConfig(String config, String json) {
        String configJson = config != null ? config : "{}";
        AmStockChartConfig configObject = (AmStockChartConfig) JSONParser.parseLenient(configJson).isObject().getJavaScriptObject();
        parseDefs(configObject);
        JsUtils.applyCustomJson(configObject, json);
        JsUtils.activateFunctions(configObject, false);
        parseConfigDateProperties(configObject);
        copyFromDataSets(configObject);
        assignDataSetForItems(configObject);
        if (BrowserInfo.get().isIE() && BrowserInfo.get().getIEVersion() < 10) {
            disableExportFeatures(configObject);
        }
        return configObject;
    }

    protected static native String getDefs(JavaScriptObject config) /*-{
        return config.defs;
    }-*/;

    protected static native void setDefs(JavaScriptObject config, JavaScriptObject defsObject) /*-{
        config.defs = defsObject;
    }-*/;

    protected static void parseDefs(JavaScriptObject config) {
        String defs = getDefs(config);
        if (defs != null) {
            setDefs(config, JSONParser.parseLenient(defs).isObject().getJavaScriptObject());
        }
    }

    // required for proper event handling
    protected static native void assignDataSetForItems(JavaScriptObject config) /*-{
        if (config.dataSets) {
            for (var i = 0; i < config.dataSets.length; i++) {
                var dataSet = config.dataSets[i];
                if (dataSet.dataProvider) {
                    for (var j = 0; j < dataSet.dataProvider.length; j++) {
                        dataSet.dataProvider[j].$d = dataSet.id;
                    }
                }
            }
        }
    }-*/;

    protected static native void disableExportFeatures(JavaScriptObject config) /*-{
        config['export'] = undefined;
    }-*/;

    protected static native void parseConfigDateProperties(JavaScriptObject config) /*-{
        var DEFAULT_JS_DATE_FORMAT = "YYYY-MM-DD JJ:NN:SS:QQQ";

        var convertStringToDate = function (object, property) {
            if (typeof object[property] == "string") {
                object[property] = $wnd.AmCharts.stringToDate(object[property], DEFAULT_JS_DATE_FORMAT);
            }
        };

        (function () {
            if (config.dataSets) {
                for (var dataSetIndex = 0; dataSetIndex < config.dataSets.length; dataSetIndex++) {
                    var dataSet = config.dataSets[dataSetIndex];
                    if (dataSet.stockEvents) {
                        for (var stockEventIndex = 0; stockEventIndex < dataSet.stockEvents.length; stockEventIndex++) {
                            var stockEvent = dataSet.stockEvents[stockEventIndex];
                            convertStringToDate(stockEvent, "date");
                        }
                    }
                }
            }
        })();

        var parseGuides = function (guides) {
            for (var i = 0; i < guides.length; i++) {
                var guide = guides[i];
                convertStringToDate(guide, "date");
                convertStringToDate(guide, "toDate");

                convertStringToDate(guide, "value");
                convertStringToDate(guide, "toValue");
            }
        };

        var parseCategoryAxis = function (categoryAxis) {
            if (categoryAxis.guides) {
                parseGuides(categoryAxis.guides);
            }
        };

        var parseTrendLines = function (trendLines) {
            for (var i = 0; i < trendLines.length; i++) {
                var trendLine = trendLines[i];
                convertStringToDate(trendLine, "finalDate");
                convertStringToDate(trendLine, "initialDate");
            }
        };

        var parseValueAxis = function (valueAxis) {
            convertStringToDate(valueAxis, "maximumDate");
            convertStringToDate(valueAxis, "minimumDate");
            if (valueAxis.guides) {
                parseGuides(valueAxis.guides);
            }
        };

        var parseValueAxes = function (valueAxes) {
            for (var i = 0; i < valueAxes.length; i++) {
                var valueAxis = valueAxes[i];
                parseValueAxis(valueAxis);
            }
        };

        (function () {
            if (config.panels) {
                for (var panelIndex = 0; panelIndex < config.panels.length; panelIndex++) {
                    var panel = config.panels[panelIndex];

                    convertStringToDate(panel, "recalculateFromDate");
                    if (panel.drawOnAxis) {
                        parseValueAxis(panel.drawOnAxis);
                    }
                    if (panel.categoryAxis) {
                        parseCategoryAxis(panel.categoryAxis);
                    }
                    if (panel.trendLines) {
                        parseTrendLines(panel.trendLines);
                    }
                    if (panel.guides) {
                        parseGuides(panel.guides);
                    }
                    if (panel.valueAxes) {
                        parseValueAxes(panel.valueAxes);
                    }
                }
            }
        })();
    }-*/;

    protected static native void copyFromDataSets(JavaScriptObject config) /*-{
        if (config.dataSets) {
            var findDataSetById = function (id) {
                for (var i = 0; i < config.dataSets.length; i++) {
                    var dataSet = config.dataSets[i];
                    if (dataSet.id == id) {
                        return dataSet;
                    }
                }
                return undefined;
            };

            if (typeof config.mainDataSet == "string") {
                config.mainDataSet = findDataSetById(config.mainDataSet);
            }

            if (config.comparedDataSets) {
                for (var i = 0; i < config.comparedDataSets.length; i++) {
                    var dataSet = config.comparedDataSets[i];
                    if (typeof dataSet == "string") {
                        dataSet = findDataSetById(dataSet);
                    }
                }
            }
        }
    }-*/;

    public final native String getChartType() /*-{
        return this.type;
    }-*/;

    public final native boolean hasStockEvents() /*-{
        if (this.dataSets) {
            for (var i = 0; i < this.dataSets.length; i++) {
                if (this.dataSets[i].stockEvents) {
                    return true;
                }
            }
        }
        return false;
    }-*/;

    public final native boolean hasPeriodSelector() /*-{
        if (this.periodSelector) {
            return true;
        }
        return false;
    }-*/;

    public final native boolean hasDataSetSelector() /*-{
        if (this.dataSetSelector) {
            return true;
        }
        return false;
    }-*/;
}