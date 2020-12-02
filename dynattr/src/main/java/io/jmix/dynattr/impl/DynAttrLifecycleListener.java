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

package io.jmix.dynattr.impl;

import io.jmix.core.DataStore;
import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.AfterEntityLoadEvent;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.core.datastore.DataStoreInterceptor;
import io.jmix.data.impl.JpaDataStoreListener;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.dynattr.DynAttrQueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component("dynattr_DynAttrLifecycleListener")
public class DynAttrLifecycleListener implements JpaDataStoreListener, DataStoreInterceptor, DataStoreCustomizer {

    @Autowired
    protected DynAttrManager dynAttrManager;

    @Override
    public void afterEntityLoad(AfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();
        Map<String, Object> hints = context.getHints();
        if (hints != null && Boolean.TRUE.equals(hints.get(DynAttrQueryHints.LOAD_DYN_ATTR))) {
            dynAttrManager.loadValues(event.getResultEntities(), context.getFetchPlan(), context.getAccessConstraints());
        }
    }

    @Override
    public void onSave(Collection<Object> entities, SaveContext saveContext) {
        dynAttrManager.storeValues(entities, saveContext.getAccessConstraints());
    }

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof AbstractDataStore) {
            ((AbstractDataStore) dataStore).registerInterceptor(this);
        }
    }
}
