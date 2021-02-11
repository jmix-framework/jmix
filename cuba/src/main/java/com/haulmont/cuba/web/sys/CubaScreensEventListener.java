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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.settings.Settings;
import com.haulmont.cuba.settings.SettingsImpl;
import io.jmix.ui.Screens;
import io.jmix.ui.event.screen.CloseWindowsInternalEvent;
import io.jmix.ui.screen.Screen;
import com.haulmont.cuba.settings.CubaLegacySettings;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component(CubaScreensEventListener.NAME)
public class CubaScreensEventListener {

    public static final String NAME = "cuba_CubaScreensEventListener";

    @EventListener
    public void onAppCloseWindowsInternal(CloseWindowsInternalEvent event) {
        saveScreenSettings(event.getSource());
    }

    public void saveScreenSettings(Screens screens) {
        Screens.OpenedScreens openedScreens = screens.getOpenedScreens();

        Screen rootScreen = openedScreens.getRootScreen();
        if (rootScreen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) rootScreen).saveSettings();
        }

        for (Screen screen : openedScreens.getWorkAreaScreens()) {
            if (screen instanceof CubaLegacySettings) {
                ((CubaLegacySettings) screen).saveSettings();
            }
        }

        for (Screen screen : openedScreens.getDialogScreens()) {
            if (screen instanceof CubaLegacySettings) {
                ((CubaLegacySettings) screen).saveSettings();
            }
        }
    }

    protected Settings getSettingsImpl(String id) {
        return new SettingsImpl(id);
    }
}
