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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.vaadin.client.BrowserInfo;
import io.jmix.charts.widget.client.utils.JsUtils;

public class AmchartsConfig extends JavaScriptObject {

    protected AmchartsConfig() {
    }

    public static AmchartsConfig fromServerConfig(String config, String json) {
        String configJson = config != null ? config : "{}";
        AmchartsConfig configObject = (AmchartsConfig) JSONParser.parseLenient(configJson).isObject().getJavaScriptObject();
        parseDefs(configObject);
        JsUtils.applyCustomJson(configObject, json);
        JsUtils.activateFunctions(configObject, false);
        parseConfigDateProperties(configObject);
        if (BrowserInfo.get().isIE() && BrowserInfo.get().getIEVersion() < 10) {
            disableExportFeatures(configObject);
        }
        return configObject;
    }

    private static native String getDefs(JavaScriptObject config) /*-{
        return config.defs;
    }-*/;

    private static native void setDefs(JavaScriptObject config, JavaScriptObject defsObject) /*-{
        config.defs = defsObject;
    }-*/;

    protected static void parseDefs(JavaScriptObject config) {
        String defs = getDefs(config);
        if (defs != null) {
            setDefs(config, JSONParser.parseLenient(defs).isObject().getJavaScriptObject());
        }
    }

    private static native void disableExportFeatures(JavaScriptObject config) /*-{
        config['export'] = undefined;
    }-*/;

    private static native void parseConfigDateProperties(JavaScriptObject config) /*-{
        var DEFAULT_JS_DATE_FORMAT = "YYYY-MM-DD JJ:NN:SS:QQQ";

        var convertStringToDate = function (object, property) {
            if (typeof object[property] == "string") {
                object[property] = $wnd.AmCharts.stringToDate(object[property], DEFAULT_JS_DATE_FORMAT);
            }
        };

        var parseGuides = function (guides) {
            for (var i = 0; i < guides.length; i++) {
                var guide = guides[i];
                convertStringToDate(guide, "date");
                convertStringToDate(guide, "toDate");

                convertStringToDate(guide, "value");
                convertStringToDate(guide, "toValue");
            }
        };

        (function () {
            if (config.guides) {
                if (config.guides) {
                    parseGuides(config.guides);
                }
            }
        })();

        (function () {
            if (config.categoryAxis) {
                if (config.categoryAxis.guides) {
                    parseGuides(config.categoryAxis.guides);
                }
            }
        })();

        (function () {
            if (config.trendLines) {
                for (var i = 0; i < config.trendLines.length; i++) {
                    var trendLine = config.trendLines[i];
                    convertStringToDate(trendLine, "finalDate");
                    convertStringToDate(trendLine, "initialDate");
                }
            }
        })();

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
            if (config.valueAxes) {
                parseValueAxes(config.valueAxes);
            }
        })();

        (function () {
            if (config.valueAxis) {
                parseValueAxis(config.valueAxis);
            }
        })();

        (function () {
            if (config.startDate) {
                config.startDate = $wnd.AmCharts.stringToDate(config.startDate, DEFAULT_JS_DATE_FORMAT);
            }
            if (config.endDate) {
                config.endDate = $wnd.AmCharts.stringToDate(config.endDate, DEFAULT_JS_DATE_FORMAT);
            }
        })();
    }-*/;

    public final native String getChartType() /*-{
        return this.type;
    }-*/;

    public final native boolean hasLegend() /*-{
        if (this.legend) {
            return true;
        }
        return false;
    }-*/;

    public final native boolean hasCursor() /*-{
        if (this.chartCursor) {
            return true;
        }
        return false;
    }-*/;
}