/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.DataStore;
import io.jmix.core.datastore.DataStoreCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * INTERNAL.
 * Factory of {@link DataStore} implementations.
 */
@Component("core_DataStoreFactory")
public class DataStoreFactory {

    protected Map<String, DataStore> dataStores = new ConcurrentHashMap<>();

    @Autowired
    protected StoreDescriptorsRegistry descriptorsRegistry;

    @Autowired
    protected ApplicationContext applicationContext;

    public DataStore get(String name) {
        String beanName = descriptorsRegistry.getStoreDescriptor(name).getBeanName();
        return dataStores.computeIfAbsent(name, key -> {
            DataStore dataStore = (DataStore) applicationContext.getBean(beanName);
            dataStore.setName(name);

            applicationContext.getBeanProvider(DataStoreCustomizer.class).stream()
                    .forEach(customizer -> customizer.customize(dataStore));

            return dataStore;
        });
    }
}
