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

package io.jmix.flowuidata.settings;

import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.exception.IllegalConcurrentAccessException;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("flowui_UserSettingsCacheImpl")
public class UserSettingsCacheImpl implements UserSettingsCache {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsCacheImpl.class);

    protected static final String ATTRIBUTE_NAME = "flowui_UserSettingsCacheImpl";

    protected UserSettingsService userSettingsService;

    public UserSettingsCacheImpl(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @Nullable
    @Override
    public String get(String key) {
        Preconditions.checkNotNullArgument(key);

        Map<String, Optional<String>> settings = getCache();
        Optional<String> cached = settings.getOrDefault(key, Optional.empty());

        if (cached.isPresent()) {
            String value = cached.get();

            log.trace("Got from cache by key: '{}', value: '{}'", key, value);

            return value;
        }

        String value = userSettingsService.load(key).orElse(null);

        settings.put(key, Optional.ofNullable(value));

        log.trace("Cache does not contain key: '{}', loaded from store value: '{}'", key, value);

        return value;
    }

    @Override
    public void put(String key, @Nullable String value) {
        Preconditions.checkNotNullArgument(key);

        getCache().put(key, Optional.ofNullable(value));

        userSettingsService.save(key, value);
    }

    @Override
    public void delete(String key) {
        Preconditions.checkNotNullArgument(key);

        getCache().put(key, Optional.empty());

        userSettingsService.delete(key);
    }

    @Override
    public void clear() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        session.setAttribute(ATTRIBUTE_NAME, null);
    }

    protected Map<String, Optional<String>> getCache() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || !session.hasLock()) {
            throw new IllegalConcurrentAccessException("Illegal access to settings client from background thread");
        }

        @SuppressWarnings("unchecked")
        Map<String, Optional<String>> settings = (Map<String, Optional<String>>) session.getAttribute(ATTRIBUTE_NAME);
        if (settings == null) {
            settings = new HashMap<>();
            session.setAttribute(ATTRIBUTE_NAME, settings);
        }
        return settings;
    }
}
