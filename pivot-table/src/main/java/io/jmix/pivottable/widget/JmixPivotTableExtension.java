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

package io.jmix.pivottable.widget;

import com.google.gson.Gson;
import com.vaadin.server.AbstractExtension;
import io.jmix.pivottable.model.Renderer;
import io.jmix.pivottable.model.extension.PivotData;
import io.jmix.pivottable.widget.client.extension.JmixPivotTableExtensionServerRpc;
import io.jmix.pivottable.widget.client.extension.JmixPivotTableExtensionState;

public class JmixPivotTableExtension extends AbstractExtension {

    public static final Gson gson = new Gson();

    protected JmixPivotTable pivotTable;

    protected String pivotDataJSON = null;

    protected Renderer currentRenderer;

    public JmixPivotTableExtension(JmixPivotTable pivotTable) {
        this.pivotTable = pivotTable;

        extend(this.pivotTable);

        JmixPivotTableExtensionServerRpc serverRpc = new JmixPivotTableExtensionServerRpc() {
            @Override
            public void updatePivotDataJSON(String json) {
                pivotDataJSON = json;
            }

            @Override
            public void updateCurrentRenderer(String renderer) {
                currentRenderer = Renderer.fromId(renderer);
            }
        };

        registerRpc(serverRpc);
    }

    public String getPivotDataJSON() {
        return pivotDataJSON;
    }

    public PivotData getPivotData() {
        return gson.fromJson(pivotDataJSON, PivotData.class);
    }

    @Override
    protected JmixPivotTableExtensionState getState() {
        return (JmixPivotTableExtensionState) super.getState();
    }

    @Override
    protected JmixPivotTableExtensionState getState(boolean markAsDirty) {
        return (JmixPivotTableExtensionState) super.getState(markAsDirty);
    }

    public Renderer getCurrentRenderer() {
        return currentRenderer;
    }

    public String getDateTimeParseFormat() {
        return getState(false).dateTimeParseFormat;
    }

    public void setDateTimeParseFormat(String dateTimeParseFormat) {
        getState().dateTimeParseFormat = dateTimeParseFormat;
    }

    public String getDateParseFormat() {
        return getState(false).dateParseFormat;
    }

    public String setDateParseFormat(String dateParseFormat) {
        return getState().dateParseFormat = dateParseFormat;
    }

    public String getTimeParseFormat() {
        return getState(false).timeParseFormat;
    }

    public String setTimeParseFormat(String timeParseFormat) {
        return getState().timeParseFormat = timeParseFormat;
    }
}