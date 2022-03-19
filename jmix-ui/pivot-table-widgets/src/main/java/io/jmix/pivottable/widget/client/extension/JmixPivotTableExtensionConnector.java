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

package io.jmix.pivottable.widget.client.extension;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.pivottable.widget.JmixPivotTableExtension;
import io.jmix.pivottable.widget.client.JmixPivotTableSceneState;
import io.jmix.pivottable.widget.client.JmixPivotTableSceneWidget;

import java.util.Iterator;
import java.util.Map;

@Connect(JmixPivotTableExtension.class)
public class JmixPivotTableExtensionConnector extends AbstractExtensionConnector {

    @Override
    protected void extend(ServerConnector target) {
        final JmixPivotTableSceneWidget pivotWidget = (JmixPivotTableSceneWidget) ((ComponentConnector) target).getWidget();
        JmixPivotTableSceneState pivotState = (JmixPivotTableSceneState) ((ComponentConnector) target).getState();

        JsPivotExtensionParser parser = JsPivotExtensionParser.create();

        pivotWidget.setRefreshHandler(jsRefreshEvent -> {
            parser.setDateTimeParseFormat(parser, getState().dateTimeParseFormat);
            parser.setDateParseFormat(parser, getState().dateParseFormat);
            parser.setTimeParseFormat(parser, getState().timeParseFormat);
            parser.setAggregation(parser, jsRefreshEvent.getAggregation());

            // consider that map contains only current locale values
            if (pivotState.localeMap != null) {
                Iterator<Map.Entry<String, String>> iterator = pivotState.localeMap.entrySet().iterator();
                if (iterator.hasNext()) {
                    JavaScriptObject pivotMessages = getJsonAsObject(iterator.next().getValue());
                    parser.setPivotMessages(parser, pivotMessages);
                }
            }

            String json = parser.parsePivotTableToJson(parser, pivotWidget.getElement());
            getRpcProxy(JmixPivotTableExtensionServerRpc.class).updatePivotDataJSON(json);

            if (jsRefreshEvent != null) {
                getRpcProxy(JmixPivotTableExtensionServerRpc.class).updateCurrentRenderer(jsRefreshEvent.getRenderer());
            }
        });
    }

    @Override
    public JmixPivotTableExtensionState getState() {
        return (JmixPivotTableExtensionState) super.getState();
    }

    protected JavaScriptObject getJsonAsObject(String json) {
        return JSONParser.parseStrict(json).isObject().getJavaScriptObject();
    }
}