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

package io.jmix.restds.impl;

import io.jmix.core.DataStore;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.core.datastore.security.DataStoreCrudListener;
import io.jmix.restds.impl.security.RestDataStoreInMemoryCrudListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("restds_RestDataStoreCustomizer")
public class RestDataStoreCustomizer implements DataStoreCustomizer {

    @Autowired
    private DataStoreCrudListener crudEntityDataStoreListener;
    @Autowired
    private RestDataStoreInMemoryCrudListener restDataStoreInMemoryCrudListener;

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof RestDataStore restDataStore) {
            restDataStore.registerInterceptor(crudEntityDataStoreListener);
            restDataStore.registerInterceptor(restDataStoreInMemoryCrudListener);
        }
    }
}
