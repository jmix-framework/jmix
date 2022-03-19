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

package io.jmix.uidata;

import com.vaadin.server.VaadinSession;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.executor.IllegalConcurrentAccessException;
import io.jmix.ui.settings.UiSettingsCache;
import io.jmix.ui.settings.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User settings provider. Caches settings in HTTP session.
 */
@Internal
public class UiSettingsCacheImpl implements UiSettingsCache {

    public static final String ATTR_NAME = "ui_UiSettingsCache";

    @Autowired
    protected UserSettingService userSettingService;

    @Nullable
    @Override
    public String getSetting(String name) {
        Preconditions.checkNotNullArgument(name);

        Map<String, Optional<String>> settings = getCache();
        Optional<String> cached = settings.getOrDefault(name, Optional.empty());
        if (cached.isPresent()) {
            return cached.get();
        }

        String setting = userSettingService.loadSetting(name);
        settings.put(name, Optional.ofNullable(setting));

        return setting;
    }

    @Override
    public void setSetting(String name, @Nullable String value) {
        Preconditions.checkNotNullArgument(name);

        getCache().put(name, Optional.ofNullable(value));
        userSettingService.saveSetting(name, value);
    }

    @Override
    public void deleteSettings(String name) {
        Preconditions.checkNotNullArgument(name);

        getCache().put(name, Optional.empty());
        userSettingService.deleteSettings(name);
    }

    @Override
    public void clear() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        session.setAttribute(ATTR_NAME, null);
    }

    protected Map<String, Optional<String>> getCache() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        @SuppressWarnings("unchecked")
        Map<String, Optional<String>> settings = (Map<String, Optional<String>>) session.getAttribute(ATTR_NAME);
        if (settings == null) {
            settings = new HashMap<>();
            session.setAttribute(ATTR_NAME, settings);
        }
        return settings;
    }
}
