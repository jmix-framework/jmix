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

package test_support;

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.ViewSettingsComponentManager;
import io.jmix.flowui.facet.settings.ViewSettingsJson;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractSettingsTest {

    @Autowired
    protected UserSettingsService userSettingsService;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected ViewSettingsComponentManager settingsManager;
    @Autowired
    protected ViewNavigators viewNavigators;

    protected void saveSettings(ViewSettings viewSettings) {
        authenticator.runWithSystem(() -> settingsManager.saveSettings(Collections.emptyList(), viewSettings));
    }

    protected ViewSettings loadSettings(String viewId) {
        String loadedRawSettings = authenticator.withSystem(() ->
                userSettingsService.load(viewId).orElse(null));

        assertNotNull(loadedRawSettings);

        ViewSettings loadedSettings = new ViewSettingsJson(viewId);
        loadedSettings.initialize(loadedRawSettings);
        return loadedSettings;
    }

    protected <T extends View<?>> T navigateTo(Class<T> view) {
        viewNavigators.view(view)
                .navigate();
        return UiTestUtils.getCurrentView();
    }
}
