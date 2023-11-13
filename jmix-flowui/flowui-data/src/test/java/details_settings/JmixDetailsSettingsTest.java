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

package details_settings;

import details_settings.view.JmixDetailsSettingsTestView;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;
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

@UiTest(viewBasePackages = {"details_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class JmixDetailsSettingsTest extends AbstractSettingsTest {

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
        // Open and close View with Details component to save settings
        JmixDetailsSettingsTestView view = navigateTo(JmixDetailsSettingsTestView.class);
        view.closeWithDefaultAction();

        // Settings of Details should be saved
        JmixDetailsSettings detailsSettings = loadSettings(view.getId().get())
                .getSettings(view.details.getId().get(), JmixDetailsSettings.class)
                .orElse(null);

        assertNotNull(detailsSettings);
        assertNotNull(detailsSettings.getOpened());
        assertFalse(detailsSettings.getOpened());

        // Open View again, open Details and close View
        view = navigateTo(JmixDetailsSettingsTestView.class);;
        view.details.setOpened(true);
        view.closeWithDefaultAction();

        // "Open" state should be saved
        detailsSettings = loadSettings(view.getId().get())
                .getSettings(view.details.getId().get(), JmixDetailsSettings.class)
                .orElse(null);

        assertNotNull(detailsSettings);
        assertNotNull(detailsSettings.getOpened());
        assertTrue(detailsSettings.getOpened());
    }

    @Test
    @DisplayName("Apply settings")
    public void applySettingsTest() {
        /*
         * Open View with Details, change "open" state to "true" and close View
         * to save settings
         */
        JmixDetailsSettingsTestView view = navigateTo(JmixDetailsSettingsTestView.class);
        view.details.setOpened(true); // false by default
        view.closeWithDefaultAction();

        // Reopen View, Details should be opened
        view = navigateTo(JmixDetailsSettingsTestView.class);

        assertTrue(view.details.isOpened());
    }
}
