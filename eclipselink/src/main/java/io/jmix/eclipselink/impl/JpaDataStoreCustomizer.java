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

package io.jmix.eclipselink.impl;

import io.jmix.core.DataStore;
import io.jmix.core.JmixOrder;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.data.impl.DataStoreCrudListener;
import io.jmix.data.impl.DataStoreCrudValuesListener;
import io.jmix.data.impl.DataStoreInMemoryCrudListener;
import io.jmix.eclipselink.impl.lazyloading.JpaLazyLoadingListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("eclipselink_JpaDataStoreCustomizer")
@Order(JmixOrder.HIGHEST_PRECEDENCE)
public class JpaDataStoreCustomizer implements DataStoreCustomizer {
    @Autowired
    protected DataStoreCrudListener crudListener;
    @Autowired
    protected DataStoreInMemoryCrudListener inMemoryCrudListener;
    @Autowired
    protected JpaLazyLoadingListener lazyLoadingListener;
    @Autowired
    protected DataStoreCrudValuesListener crudValuesListener;

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof JpaDataStore) {
            AbstractDataStore abstractStore = (AbstractDataStore) dataStore;
            abstractStore.registerInterceptor(crudListener);
            abstractStore.registerInterceptor(inMemoryCrudListener);
            abstractStore.registerInterceptor(lazyLoadingListener);
            abstractStore.registerInterceptor(crudValuesListener);
        }
    }
}
