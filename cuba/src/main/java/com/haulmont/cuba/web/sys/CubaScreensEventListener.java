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

import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.ui.Screens;
import io.jmix.ui.component.WindowContext;
import io.jmix.ui.event.screen.AfterShowScreenEvent;
import io.jmix.ui.event.screen.BeforeShowScreenEvent;
import io.jmix.ui.event.screen.CloseWindowsInternalEvent;
import io.jmix.ui.screen.Screen;
import com.haulmont.cuba.settings.CubaLegacySettings;
import com.haulmont.cuba.settings.Settings;
import com.haulmont.cuba.settings.SettingsImpl;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component(CubaScreensEventListener.NAME)
public class CubaScreensEventListener {

    public static final String NAME = "cuba_CubaScreensEventListener";

    @EventListener
    public void onAfterShowScreen(AfterShowScreenEvent event) {
        Screen screen = event.getSource();
        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applySettings(getSettingsImpl(screen.getId()));
        }

        if (screen instanceof LegacyFrame) {
            WindowContext windowContext = screen.getWindow().getContext();
            if (!WindowParams.DISABLE_RESUME_SUSPENDED.getBool(windowContext)) {
                DsContext dsContext = ((LegacyFrame) screen).getDsContext();
                if (dsContext != null) {
                    ((DsContextImplementation) dsContext).resumeSuspended();
                }
            }
        }
    }

    @EventListener
    public void onBeforeShowWindow(BeforeShowScreenEvent event) {
        Screen screen = event.getSource();
        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applyDataLoadingSettings(getSettingsImpl(screen.getId()));
        }
    }

    @EventListener
    public void onAppCloseWindowsInternal(CloseWindowsInternalEvent event) {
        saveScreenHistory();
        saveScreenSettings(event.getSource());
    }

    public void saveScreenHistory() {
        // todo screen history
        /*getOpenedWorkAreaScreensStream().forEach(s ->
                screenHistorySupport.saveScreenHistory(s)
        );

        getDialogScreensStream().forEach(s ->
                screenHistorySupport.saveScreenHistory(s)
        );*/
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
