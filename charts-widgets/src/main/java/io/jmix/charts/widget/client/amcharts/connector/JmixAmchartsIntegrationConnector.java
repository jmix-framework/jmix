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

package io.jmix.charts.widget.client.amcharts.connector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.charts.widget.amcharts.JmixAmchartsIntegration;
import io.jmix.charts.widget.client.amcharts.state.JmixAmchartsIntegrationState;

import java.util.Map;

@Connect(JmixAmchartsIntegration.class)
public class JmixAmchartsIntegrationConnector extends AbstractExtensionConnector {

    @Override
    public JmixAmchartsIntegrationState getState() {
        return (JmixAmchartsIntegrationState) super.getState();
    }

    @Override
    protected void extend(ServerConnector target) {
        // do nothing
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("version") && getState().json != null) {
            JavaScriptObject options = JSONParser.parseLenient(getState().json).isObject().getJavaScriptObject();
            applyOptions(options);
        }

        if (stateChangeEvent.hasPropertyChanged("chartMessages") && getState().chartMessages != null) {
            Map<String, String> chartMessages = getState().chartMessages;
            for (final Map.Entry<String, String> entry : chartMessages.entrySet()) {
                JavaScriptObject chartLocalization = JSONParser.parseLenient(entry.getValue()).isObject().getJavaScriptObject();
                applyChartMessages(entry.getKey(), chartLocalization);
            }
        }

        if (stateChangeEvent.hasPropertyChanged("exportMessages") && getState().exportMessages != null) {
            Map<String, String> exportMessages = getState().exportMessages;
            for (final Map.Entry<String, String> entry : exportMessages.entrySet()) {
                JavaScriptObject exportLocalization = JSONParser.parseLenient(entry.getValue()).isObject().getJavaScriptObject();
                applyExportMessages(entry.getKey(), exportLocalization);
            }
        }
    }

    private native void applyChartMessages(String localeCode, JavaScriptObject chartMessages) /*-{
        $wnd.AmCharts.translations[localeCode] = chartMessages;
    }-*/;

    private native void applyExportMessages(String localeCode, JavaScriptObject exportMessages) /*-{
        $wnd.AmCharts.translations['export'][localeCode] = exportMessages;
    }-*/;

    private native void applyOptions(JavaScriptObject options) /*-{
        var merge = function (dst, src) {
            for (var property in src) {
                if (src.hasOwnProperty(property)) {
                    if (src[property] && typeof src[property] === "object") {
                        if (!dst[property]) {
                            dst[property] = src[property];
                        } else {
                            arguments.callee(dst[property], src[property]);
                        }
                    } else {
                        dst[property] = src[property];
                    }
                }
            }
        };
        merge($wnd.AmCharts, options);
    }-*/;
}