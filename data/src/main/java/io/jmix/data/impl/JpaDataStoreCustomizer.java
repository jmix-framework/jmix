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

package io.jmix.data.impl;

import io.jmix.core.DataStore;
import io.jmix.core.JmixOrder;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.data.impl.lazyloading.JpaLazyLoadingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(JmixOrder.HIGHEST_PRECEDENCE)
public class JpaDataStoreCustomizer implements DataStoreCustomizer {
    @Autowired
    protected DataStoreCrudInterceptor crudInterceptor;
    @Autowired
    protected DataStoreInMemoryCrudInterceptor inMemoryCRUDInterceptor;
    @Autowired
    protected JpaLazyLoadingInterceptor lazyLoadingInterceptor;

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof JpaDataStore) {
            AbstractDataStore abstractStore = (AbstractDataStore) dataStore;
            abstractStore.registerInterceptor(crudInterceptor);
            abstractStore.registerInterceptor(inMemoryCRUDInterceptor);
            abstractStore.registerInterceptor(lazyLoadingInterceptor);
        }
    }
}
