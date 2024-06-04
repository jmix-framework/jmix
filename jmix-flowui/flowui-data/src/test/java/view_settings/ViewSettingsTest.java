/*
 * Copyright 2022 Haulmont.
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

package view_settings;

import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.ViewSettingsJson;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AbstractSettingsTest;
import test_support.FlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FlowuiDataTestConfiguration.class)
public class ViewSettingsTest extends AbstractSettingsTest {

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from FLOWUI_USER_SETTINGS");
    }

    @Test
    @DisplayName("Save and load primitives")
    public void saveAndLoadPrimitives() {
        // Put primitives to the ViewSettings and save it
        ViewSettings viewSettings = new ViewSettingsJson("testViewId");
        var compId = "compId";

        viewSettings
                .put(compId, "string", "string")
                .put(compId, "int", 1)
                .put(compId, "long", 1L)
                .put(compId, "double", 1d)
                .put(compId, "boolean", true);

        saveSettings(viewSettings);

        // Load ViewSettings, values should be the same
        var loadedSettings = loadSettings(viewSettings.getViewId());

        assertEquals("string", loadedSettings.getString(compId, "string").orElse(null));
        assertEquals(1, loadedSettings.getInteger(compId, "int").orElse(null));
        assertEquals(1L, loadedSettings.getLong(compId, "long").orElse(null));
        assertEquals(1d, loadedSettings.getDouble(compId, "double").orElse(null));
        assertTrue(loadedSettings.getBoolean(compId, "boolean").orElse(false));
    }

    @Test
    @DisplayName("Save and load component settings class")
    public void saveAndLoadComponentSettingsClass() {
        // Put component settings to ViewSettings and save it
        ViewSettings viewSettings = new ViewSettingsJson("testViewId");
        var compId = "compId";

        var componentSettings = new JmixDetailsSettings();
        componentSettings.setId(compId);
        componentSettings.setOpened(true);

        viewSettings.put(componentSettings);

        saveSettings(viewSettings);

        // Load ViewSettings, component settings should be the same
        ViewSettings loadedSettings = loadSettings(viewSettings.getViewId());

        JmixDetailsSettings detailsSettings =
                loadedSettings.getSettings(compId, JmixDetailsSettings.class)
                        .orElse(null);

        assertNotNull(detailsSettings);
        assertEquals(true, detailsSettings.getOpened());
    }

    @Test
    @DisplayName("Remove component settings")
    public void removeComponentSettings() {
        // Save primitive and component settings
        var primId = "primitiveId";
        var compId = "componentId";

        ViewSettings viewSettings = new ViewSettingsJson("testViewId");
        viewSettings.put(primId, "string", "string");

        var compSettings = new JmixDetailsSettings();
        compSettings.setId(compId);
        viewSettings.put(compSettings);

        saveSettings(viewSettings);

        // Remove settings and save again
        var loadedSettings = loadSettings(viewSettings.getViewId());

        var primitiveVal = loadedSettings.getString(primId, "string").orElse(null);
        assertEquals("string", primitiveVal);

        var loadedComponentSettings = loadedSettings.getSettings(compId, JmixDetailsSettings.class).orElse(null);
        assertNotNull(loadedComponentSettings);

        loadedSettings.delete(primId);
        loadedSettings.delete(compId);

        saveSettings(loadedSettings);

        // Reload settings, settings of primitiveId should not exist
        loadedSettings = loadSettings(loadedSettings.getViewId());

        assertFalse(loadedSettings.getString(primId, "string").isPresent());
        assertFalse(loadedSettings.getSettings(compId, JmixDetailsSettings.class).isPresent());
    }

    @Test
    @DisplayName("Remove component property settings")
    public void removeComponentPropertySettings() {
        // Save primitive and component settings
        var primId = "primitiveId";

        ViewSettings viewSettings = new ViewSettingsJson("testViewId");
        viewSettings.put(primId, "string", "string");

        saveSettings(viewSettings);

        // Reload settings and remove key
        var loadedSettings = loadSettings(viewSettings.getViewId());

        loadedSettings.delete(primId, "string");

        saveSettings(loadedSettings);

        // Reload settings again, key should not exist
        loadedSettings = loadSettings(viewSettings.getViewId());

        assertFalse(loadedSettings.getString(primId, "string").isPresent());
    }
}
