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

package overridden_settings.test_support;

import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;
import io.jmix.flowui.facet.settings.component.binder.JmixDetailsSettingsBinder;

import java.util.Objects;

public class TestJmixDetailsSettingsBinder extends JmixDetailsSettingsBinder {

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return TestJmixDetailsSettings.class;
    }

    @Override
    public void applySettings(JmixDetails component, JmixDetailsSettings settings) {
        super.applySettings(component, settings);

        TestJmixDetailsSettings testSettings = settings.as();
        if (testSettings.getSummary() != null) {
            component.setSummaryText(testSettings.getSummary());
        }
    }

    @Override
    public boolean saveSettings(JmixDetails component, JmixDetailsSettings settings) {
        boolean changed = super.saveSettings(component, settings);

        TestJmixDetailsSettings testSettings = settings.as();
        if (!Objects.equals(component.getSummaryText(), testSettings.getSummary())) {
            testSettings.setSummary(component.getSummaryText());
            changed = true;
        }
        return changed;
    }

    @Override
    public JmixDetailsSettings getSettings(JmixDetails component) {
        JmixDetailsSettings settings = super.getSettings(component);
        TestJmixDetailsSettings testSettings = settings.as();
        testSettings.setSummary(component.getSummaryText());
        return testSettings;
    }

    @Override
    protected JmixDetailsSettings createSettings() {
        return new TestJmixDetailsSettings();
    }
}
