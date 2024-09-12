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

package io.jmix.restds.util;

import io.jmix.core.DataStore;
import io.jmix.core.impl.DataStoreFactory;
import io.jmix.restds.impl.RestDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Utility methods for tasks related to RestDataStore.
 */
@Component("restds_RestDataStoreUtils")
public class RestDataStoreUtils {

    @Autowired
    private DataStoreFactory dataStoreFactory;

    /**
     * Returns RestClient used by the specified data store.
     */
    public RestClient getRestClient(String dataStoreName) {
        DataStore dataStore = dataStoreFactory.get(dataStoreName);
        if (dataStore instanceof RestDataStore restDataStore) {
            return restDataStore.getRestInvoker().getRestClient();
        } else {
            throw new IllegalArgumentException("Not a RestDataStore: " + dataStoreName);
        }
    }
}
