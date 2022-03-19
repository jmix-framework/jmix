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

package com.haulmont.cuba.web.app.settings;

import javax.annotation.Nullable;

@Deprecated
public class UserSettingsToolsImpl extends io.jmix.uidata.UserSettingsToolsImpl implements UserSettingsTools {

    @Override
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

    @Override
    public void saveFoldersState(boolean visible, int horizontalSplit, int verticalSplit) {
        userSettingService.saveSetting("foldersState",
                visible + ","
                        + horizontalSplit + ","
                        + verticalSplit
        );
    }
}
