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

package test_support;

import io.jmix.uidata.UiSettingsCacheImpl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestUiSettingsCache extends UiSettingsCacheImpl {

    protected Map<String, Optional<String>> cache = new ConcurrentHashMap<>();

    @Override
    protected Map<String, Optional<String>> getCache() {
        return cache;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
