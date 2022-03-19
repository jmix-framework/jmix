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

package io.jmix.charts.widget.amcharts;

import com.google.gson.*;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Extension;
import com.vaadin.ui.UI;
import io.jmix.ui.widget.WebJarResource;
import io.jmix.charts.model.settings.Settings;
import io.jmix.charts.widget.amcharts.serialization.ChartSettingsSerializer;
import io.jmix.charts.widget.client.amcharts.state.JmixAmchartsIntegrationState;

import java.util.*;

@WebJarResource(value = {
        "amcharts:amcharts.js",
        "amcharts:funnel.js",
        "amcharts:gauge.js",
        "amcharts:pie.js",
        "amcharts:radar.js",
        "amcharts:serial.js",
        "amcharts:xy.js",
        "amcharts:gantt.js",
        "amcharts:amstock.js",

        "amcharts:themes/black.js",
        "amcharts:themes/chalk.js",
        "amcharts:themes/dark.js",
        "amcharts:themes/light.js",
        "amcharts:themes/patterns.js",

        "amcharts:plugins/export/export.min.js",

        "amcharts:plugins/export/export.css"
}, overridePath = "amcharts/")
public class JmixAmchartsIntegration extends AbstractExtension {

    protected final static Gson gson = new Gson();

    protected Settings settings;
    protected ChartSettingsSerializer serializer;

    protected Locale locale;

    public JmixAmchartsIntegration() {
        serializer = new ChartSettingsSerializer();
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        applySettings();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void applySettings() {
        getState().version++;
    }

    public Map<String, String> getChartMessages(String localeCode) {
        if (getState(false).chartMessages == null) {
            return Collections.emptyMap();
        }

        String jsonLocaleMap = getState(false).chartMessages.get(localeCode);
        //noinspection unchecked
        Map<String, String> localeMap = gson.fromJson(jsonLocaleMap, Map.class);
        return Collections.unmodifiableMap(localeMap);
    }

    public void setChartMessages(String localeCode, Map<String, Object> localeMap) {
        if (getState(false).chartMessages == null) {
            getState().chartMessages = new HashMap<>();
        }

        JsonObject jsonLocaleMap = new JsonObject();
        for (Map.Entry<String, Object> localeEntry : localeMap.entrySet()) {
            JsonElement element;
            if (localeEntry.getValue() instanceof List) {
                List list = (List) localeEntry.getValue();
                JsonArray array = new JsonArray();
                for (Object value : list) {
                    array.add(new JsonPrimitive((String) value));
                }
                element = array;
            } else {
                element = new JsonPrimitive((String) localeEntry.getValue());
            }
            jsonLocaleMap.add(localeEntry.getKey(), element);
        }

        getState().chartMessages.put(localeCode, gson.toJson(jsonLocaleMap));
    }

    public Map<String, String> getExportMessages(String localeCode) {
        if (getState(false).exportMessages == null) {
            return Collections.emptyMap();
        }

        String jsonLocaleMap = getState(false).exportMessages.get(localeCode);
        //noinspection unchecked
        Map<String, String> localeMap = new Gson().fromJson(jsonLocaleMap, Map.class);
        return Collections.unmodifiableMap(localeMap);
    }

    public void setExportMessages(String localeCode, Map<String, String> localeMap) {
        if (getState(false).exportMessages == null) {
            getState().exportMessages = new HashMap<>();
        }

        JsonObject jsonLocaleMap = new JsonObject();
        for (Map.Entry<String, String> localeEntry : localeMap.entrySet()) {
            jsonLocaleMap.addProperty(localeEntry.getKey(), localeEntry.getValue());
        }

        getState().exportMessages.put(localeCode, gson.toJson(jsonLocaleMap));
    }

    @Override
    protected JmixAmchartsIntegrationState getState() {
        return (JmixAmchartsIntegrationState) super.getState();
    }

    @Override
    protected JmixAmchartsIntegrationState getState(boolean markAsDirty) {
        return (JmixAmchartsIntegrationState) super.getState(markAsDirty);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (settings != null) {
            getState().json = serializer.serialize(settings);
        }
    }

    public static JmixAmchartsIntegration get(UI ui) {
        JmixAmchartsIntegration optioner = null;

        // Search singleton optioner
        for (Extension extension : ui.getExtensions()) {
            if (extension instanceof JmixAmchartsIntegration) {
                optioner = (JmixAmchartsIntegration) extension;
                break;
            }
        }

        // Create new optioner if not found
        if (optioner == null) {
            optioner = new JmixAmchartsIntegration();
            optioner.extend(ui);
        }

        return optioner;

    }

    public static JmixAmchartsIntegration get() {
        UI ui = UI.getCurrent();

        if (ui == null) {
            throw new IllegalStateException(
                    "This method must be used from UI thread");
        }
        return get(ui);
    }
}