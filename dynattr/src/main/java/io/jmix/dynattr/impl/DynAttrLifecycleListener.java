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
import io.jmix.core.LoadContext;
import io.jmix.core.datastore.*;
import io.jmix.data.impl.JpaDataStoreListener;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.DynamicAttributesState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.jmix.core.entity.EntitySystemAccess.addExtraState;
import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

@Component("dynattr_DynAttrLifecycleListener")
public class DynAttrLifecycleListener implements JpaDataStoreListener, DataStoreEventListener, DataStoreCustomizer {

    @Autowired
    protected DynAttrManager dynAttrManager;

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();
        Map<String, Object> hints = context.getHints();
        if (hints != null && Boolean.TRUE.equals(hints.get(DynAttrQueryHints.LOAD_DYN_ATTR))) {
            dynAttrManager.loadValues(event.getResultEntities(), context.getFetchPlan(), context.getAccessConstraints());
        } else {
            for (Object entity : event.getResultEntities()) {
                DynamicAttributesState state = new DynamicAttributesState(getEntityEntry(entity));
                state.setDynamicAttributes(new DynamicAttributes());
                addExtraState(entity, state);
            }
        }
    }

    @Override
    public void entitySaving(DataStoreEntitySavingEvent event) {
        dynAttrManager.storeValues(event.getSaveContext().getEntitiesToSave(), event.getSaveContext().getAccessConstraints());
    }

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof AbstractDataStore) {
            ((AbstractDataStore) dataStore).registerInterceptor(this);
        }
    }
}
