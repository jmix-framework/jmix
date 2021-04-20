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


import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.pivottable.widget.JmixPivotTableExtension;
import io.jmix.pivottable.widget.client.JmixPivotTableSceneWidget;

@Connect(JmixPivotTableExtension.class)
public class JmixPivotTableExtensionConnector extends AbstractExtensionConnector {

    @Override
    protected void extend(ServerConnector target) {
        final JmixPivotTableSceneWidget pivotWidget = (JmixPivotTableSceneWidget) ((ComponentConnector) target).getWidget();

        JmixPivotTableExtensionJsOverlay jsOverlay = new JmixPivotTableExtensionJsOverlay(pivotWidget.getElement());

        pivotWidget.setRefreshHandler(jsRefreshEvent -> {
            JsPivotExtensionOptions options = JsPivotExtensionOptions.get();
            options.setDateTimeParseFormat(options, getState().dateTimeParseFormat);
            options.setDateParseFormat(options, getState().dateParseFormat);
            options.setTimeParseFormat(options, getState().timeParseFormat);

            String json = jsOverlay.convertPivotTableToJson(options);
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
}