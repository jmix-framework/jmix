/*
 * Copyright 2023 Haulmont.
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

package generic_filter_settings;

import generic_filter_settings.view.GenericFilterSettingsTestView;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.AbstractSettingsTest;
import test_support.FlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UiTest(viewBasePackages = {"generic_filter_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class GenericFilterSettingsTest extends AbstractSettingsTest {

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
    @DisplayName("Save settings")
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void saveSettingsTest() {
        // Open and close View with GenericFilter component to save settings
        GenericFilterSettingsTestView view = navigateTo(GenericFilterSettingsTestView.class);
        view.closeWithDefaultAction();

        // Settings of GenericFilter should be saved
        GenericFilterSettings settings = loadSettings(view.getId().get())
                .getSettings(view.genericFilter.getId().get(), GenericFilterSettings.class)
                .orElse(null);

        assertNotNull(settings);
        assertNotNull(settings.getOpened());
        assertTrue(settings.getOpened());

        // Open View again, open GenericFilter and close View
        view = navigateTo(GenericFilterSettingsTestView.class);;
        view.genericFilter.setOpened(false);
        view.closeWithDefaultAction();

        // "Open" state should be saved
        settings = loadSettings(view.getId().get())
                .getSettings(view.genericFilter.getId().get(), GenericFilterSettings.class)
                .orElse(null);

        assertNotNull(settings);
        assertNotNull(settings.getOpened());
        assertFalse(settings.getOpened());
    }

    @Test
    @DisplayName("Apply settings")
    public void applySettingsTest() {
        /*
         * Open View with GenericFilter, change "open" state to "false" and close View
         * to save settings
         */
        GenericFilterSettingsTestView view = navigateTo(GenericFilterSettingsTestView.class);
        view.genericFilter.setOpened(false); // true by default
        view.closeWithDefaultAction();

        // Reopen View, Details should be opened
        view = navigateTo(GenericFilterSettingsTestView.class);

        assertFalse(view.genericFilter.isOpened());
    }
}
