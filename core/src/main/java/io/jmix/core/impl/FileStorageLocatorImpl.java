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

package io.jmix.core.impl;

import io.jmix.core.CoreProperties;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class FileStorageLocatorImpl implements FileStorageLocator {

    @Autowired(required = false)
    private Map<String, FileStorage> storages = Collections.emptyMap();

    @Autowired
    private CoreProperties properties;

    @Override
    public <T extends FileStorage> T get(String beanName) {
        FileStorage fileStorage = storages.get(beanName);
        if (fileStorage == null) {
            throw new IllegalArgumentException(beanName);
        }
        //noinspection unchecked
        return (T) fileStorage;
    }

    @Override
    public <T extends FileStorage> T getDefault() {
        String defaultFileStorage = properties.getDefaultFileStorage();
        if (defaultFileStorage != null) {
            return get(defaultFileStorage);
        } else {
            if (storages.size() == 1) {
                //noinspection unchecked
                return (T) storages.values().iterator().next();
            } else if (storages.isEmpty()) {
                throw new IllegalStateException("No FileStorage beans registered");
            } else {
                throw new IllegalStateException("There are more than one FileStorage beans registered, " +
                        "set 'jmix.core.defaultFileStorage' property");
            }
        }
    }
}
