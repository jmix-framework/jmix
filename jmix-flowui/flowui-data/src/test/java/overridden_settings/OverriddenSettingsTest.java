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

package overridden_settings;

import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import overridden_settings.test_support.TestJmixDetailsSettings;
import overridden_settings.view.OverriddenSettingsTestView;
import test_support.AbstractSettingsTest;
import test_support.FlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"overridden_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class OverriddenSettingsTest extends AbstractSettingsTest {

    @Autowired
    UserSettingsCache userSettingsCache;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from FLOWUI_USER_SETTINGS");
        userSettingsCache.clear();
    }

    @Test
    @DisplayName("Save overridden component settings")
    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    public void saveOverriddenSettingsTest() {
        var summary = "Settings text";

        // Set new summary and close View
        OverriddenSettingsTestView view = navigateTo(OverriddenSettingsTestView.class);
        view.details.setSummaryText(summary);
        view.details.setOpened(true); // default false

        view.closeWithDefaultAction();

        // Check that summary is saved
        var settings = loadSettings(view.getId().get())
                .getSettings("details", TestJmixDetailsSettings.class)
                .orElse(null);

        assertNotNull(settings);
        assertNotNull(settings.getOpened());
        assertEquals(summary, settings.getSummary());
        assertTrue(settings.getOpened());
    }

    @Test
    @DisplayName("Apply overridden component settings")
    public void applyOverriddenSettingsTest() {
        var summary = "Settings text";

        // Set summary and close View
        OverriddenSettingsTestView view = navigateTo(OverriddenSettingsTestView.class);
        view.details.setSummaryText(summary);
        view.details.setOpened(true); // default false

        view.closeWithDefaultAction();

        // JmixDetails should have summary from settings
        view = navigateTo(OverriddenSettingsTestView.class);

        assertEquals(summary, view.details.getSummaryText());
        assertTrue(view.details.isOpened());
    }
}
