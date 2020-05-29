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

package io.jmix.ui.settings;

import com.vaadin.server.VaadinSession;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.executor.IllegalConcurrentAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User settings provider. Caches settings in HTTP session.
 */
@Component(UiSettingsCache.NAME)
public class UiSettingsCache {

    public static final String NAME = "cuba_SettingsClient";

    @Autowired
    protected UserSettingService userSettingService;

    @Nullable
    public String getSetting(String name) {
        Preconditions.checkNotNullArgument(name);

        Map<String, Optional<String>> settings = getCache();
        Optional<String> cached = settings.get(name);
        if (cached != null) {
            return cached.orElse(null);
        }

        String setting = userSettingService.loadSetting(name);
        settings.put(name, Optional.ofNullable(setting));

        return setting;
    }

    public void setSetting(String name, @Nullable String value) {
        Preconditions.checkNotNullArgument(name);

        getCache().put(name, Optional.ofNullable(value));
        userSettingService.saveSetting(name, value);
    }

    public void deleteSettings(String name) {
        Preconditions.checkNotNullArgument(name);

        getCache().put(name, Optional.empty());
        userSettingService.deleteSettings(name);
    }

    /**
     * Clears cache.
     */
    public void clear() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        session.setAttribute(NAME, null);
    }

    protected Map<String, Optional<String>> getCache() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        @SuppressWarnings("unchecked")
        Map<String, Optional<String>> settings = (Map<String, Optional<String>>) session.getAttribute(NAME);
        if (settings == null) {
            settings = new HashMap<>();
            session.setAttribute(NAME, settings);
        }
        return settings;
    }
}
