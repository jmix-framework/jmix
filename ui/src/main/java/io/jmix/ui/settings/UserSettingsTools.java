/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.settings;

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.AppWorkArea;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Utility bean for work with user settings on web client tier.
 */
@Component(UserSettingsTools.NAME)
public class UserSettingsTools {

    public static final String NAME = "cuba_UserSettingsTools";

    public static class FoldersState {

        public final boolean visible;
        public final int horizontalSplit;
        public final int verticalSplit;

        public FoldersState(boolean visible, int horizontalSplit, int verticalSplit) {
            this.horizontalSplit = horizontalSplit;
            this.verticalSplit = verticalSplit;
            this.visible = visible;
        }
    }

    @Inject
    protected UserSettingService userSettingService;

    @Inject
    protected UiProperties uiProperties;

    public AppWorkArea.Mode loadAppWindowMode() {
        String s = userSettingService.loadSetting("appWindowMode");
        if (s != null) {
            if (AppWorkArea.Mode.SINGLE.name().equals(s)) {
                return AppWorkArea.Mode.SINGLE;
            } else if (AppWorkArea.Mode.TABBED.name().equals(s)) {
                return AppWorkArea.Mode.TABBED;
            }
        }
        return AppWorkArea.Mode.valueOf(uiProperties.getAppWindowMode().toUpperCase());
    }

    public void saveAppWindowMode(AppWorkArea.Mode mode) {
        Preconditions.checkNotNullArgument(mode);

        userSettingService.saveSetting("appWindowMode", mode.name());
    }

    public String loadTheme() {
        String s = userSettingService.loadSetting("theme");
        if (s != null) {
            return s;
        }
        return uiProperties.getTheme();
    }

    public void saveAppWindowTheme(String theme) {
        userSettingService.saveSetting("theme", theme);
    }

    @Nullable
    public FoldersState loadFoldersState() {
        String s = userSettingService.loadSetting("foldersState");
        if (s == null)
            return null;

        String[] parts = s.split(",");
        if (parts.length != 3)
            return null;

        try {
            return new FoldersState(Boolean.parseBoolean(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return null;
        }
    }

    /* todo folders panel
    public void saveFoldersState(boolean visible, int horizontalSplit, int verticalSplit) {
        userSettingService.saveSetting(ClientType.WEB, "foldersState",
                String.valueOf(visible) + ","
                + String.valueOf(horizontalSplit) + ","
                + String.valueOf(verticalSplit)
        );
    }*/
}