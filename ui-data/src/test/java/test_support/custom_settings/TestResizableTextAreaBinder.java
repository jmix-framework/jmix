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

package test_support.custom_settings;

import io.jmix.ui.component.ResizableTextArea;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.ResizableTextAreaSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.binder.ResizableTextAreaSettingsBinder;

import java.util.Objects;

public class TestResizableTextAreaBinder extends ResizableTextAreaSettingsBinder {

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return TestResizableTextAreaSettings.class;
    }

    @Override
    public void applySettings(ResizableTextArea textArea, SettingsWrapper wrapper) {
        super.applySettings(textArea, wrapper);

        TestResizableTextAreaSettings settings = wrapper.getSettings();
        if (settings.getText() != null) {
            textArea.setValue(settings.getText());
        }
    }

    @Override
    public boolean saveSettings(ResizableTextArea textArea, SettingsWrapper wrapper) {
        boolean changed = super.saveSettings(textArea, wrapper);
        TestResizableTextAreaSettings settings = wrapper.getSettings();

        // test only with String
        String value = (String) textArea.getValue();
        if (!Objects.equals(textArea.getValue(), settings.getText())) {
            settings.setText(value);
            changed = true;
        }

        return changed;
    }

    @Override
    public TestResizableTextAreaSettings getSettings(ResizableTextArea textArea) {
        TestResizableTextAreaSettings settings = (TestResizableTextAreaSettings) super.getSettings(textArea);
        if (textArea.getValue() != null) {
            settings.setText(textArea.getValue().toString());
        }
        return settings;
    }

    @Override
    protected ResizableTextAreaSettings createSettings() {
        return new TestResizableTextAreaSettings();
    }
}
