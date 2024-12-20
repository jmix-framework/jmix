/*
 * Copyright 2024 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.entity.LoadedPropertiesInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Creates {@link CachingLoadedPropertiesInfo} instances to be used in JPA entities.
 */
@Component("core_LoadedPropertiesInfoFactory")
public class LoadedPropertiesInfoFactory {

    @Value("${jmix.core.disable-caching-loaded-properties:false}")
    private Boolean disableCaching;

    @Nullable
    public LoadedPropertiesInfo create() {
        if (disableCaching) {
            return null;
        } else {
            return new CachingLoadedPropertiesInfo();
        }
    }
}
