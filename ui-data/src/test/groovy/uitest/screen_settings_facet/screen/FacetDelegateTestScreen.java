/*
 * Copyright 2020 Haulmont.
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

package uitest.screen_settings_facet.screen;

import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("facet-delegate-test-screen.xml")
public class FacetDelegateTestScreen extends Screen {

    @Autowired
    public ScreenSettingsFacet facet;

    public int calls = 0;

    @Install(to="facet", subject = "saveSettingsDelegate")
    protected void saveSettings(ScreenSettingsFacet.SettingsContext settings) {
        ++calls;
    }

    @Install(to="facet", subject = "applyDataLoadingSettingsDelegate")
    protected void applyDataLoadingSettings(ScreenSettingsFacet.SettingsContext settings) {
        ++calls;
    }

    @Install(to="facet", subject = "applySettingsDelegate")
    protected void appplySettings(ScreenSettingsFacet.SettingsContext settings) {
        ++calls;
    }
}
