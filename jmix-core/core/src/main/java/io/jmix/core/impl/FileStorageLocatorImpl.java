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

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("core_FileStorageLocator")
public class FileStorageLocatorImpl implements FileStorageLocator {

    @Autowired(required = false)
    private Map<String, FileStorage> storagesByBeanNames = Collections.emptyMap();

    private Map<String, FileStorage> storagesByNames;

    @Autowired
    private CoreProperties properties;

    @PostConstruct
    protected void initStoragesByNames() {
        if (storagesByBeanNames.isEmpty()) {
            storagesByNames = Collections.emptyMap();
        } else {
            storagesByNames = storagesByBeanNames.values().stream()
                    .collect(Collectors.toMap(FileStorage::getStorageName, Function.identity(),
                            (fileStorage, fileStorageWithTheSameName) -> {
                                throw new IllegalStateException("There are more than one FileStorage beans registered" +
                                        " with the same storageName: " + fileStorage.getStorageName());
                            }));
        }
    }

    protected <T extends FileStorage> T getByBeanName(String beanName) {
        FileStorage fileStorage = storagesByBeanNames.get(beanName);
        if (fileStorage == null) {
            throw new IllegalArgumentException("FileStorage not found: " + beanName);
        }
        //noinspection unchecked
        return (T) fileStorage;
    }

    @Override
    public <T extends FileStorage> T getByName(String storageName) {
        FileStorage fileStorage = storagesByNames.get(storageName);
        if (fileStorage == null) {
            throw new IllegalArgumentException("FileStorage not found: " + storageName);
        }
        //noinspection unchecked
        return (T) fileStorage;
    }

    @Override
    public <T extends FileStorage> T getDefault() {
        String defaultFileStorage = properties.getDefaultFileStorage();
        if (defaultFileStorage != null) {
            return getByName(defaultFileStorage);
        } else {
            if (storagesByBeanNames.size() == 1) {
                //noinspection unchecked
                return (T) storagesByBeanNames.values().iterator().next();
            } else if (storagesByBeanNames.isEmpty()) {
                throw new IllegalStateException("No FileStorage beans registered");
            } else {
                throw new IllegalStateException("There are more than one FileStorage beans registered, " +
                        "set 'jmix.core.default-file-storage' property");
            }
        }
    }
}
