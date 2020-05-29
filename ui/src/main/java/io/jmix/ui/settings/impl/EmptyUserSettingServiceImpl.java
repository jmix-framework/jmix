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

package io.jmix.ui.settings.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.ui.settings.UserSettingService;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Stub. By default, UI does not provide persistence functionality for settings. To save/load settings add
 * "ui-persistence" add-on.
 */
@Service(UserSettingService.NAME)
public class EmptyUserSettingServiceImpl implements UserSettingService {

    @Nullable
    @Override
    public String loadSetting(String name) {
        return null;
    }

    @Override
    public void saveSetting(String name, String value) {
        // do nothing
    }

    @Override
    public void deleteSettings(String name) {
        // do nothing
    }

    @Override
    public void copySettings(BaseUser fromUser, BaseUser toUser) {
        // do nothing
    }

    @Override
    public void deleteScreenSettings(Set<String> screens) {
        // do nothing
    }
}
