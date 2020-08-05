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

package io.jmix.ui.sys;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side caching proxy for the <code>PersistenceManagerService</code>.
 * <p>
 * Caches the PersistenceManager information for the whole life time of the client application.
 * The web-client's <code>Caching</code> MBean contains a method to clear this cache.
 * </p>
 */

// todo dummy implementation; see DbmsFeatures

@Component(PersistenceManagerClient.NAME)
@Primary
public class PersistenceManagerClient {

    public static final String NAME = "jmix_PersistenceManagerClient";

    public boolean useLookupScreen(String entityName) {
        return false;
    }

    public int getFetchUI(String entityName) {
        return 50;
    }

    public int getMaxFetchUI(String entityName) {
        return 10000;
    }


    public boolean isNullsLastSorting() {
        return false;
    }

    public boolean supportsLobSortingAndFiltering(String storeName) {
        return true;
    }

}
