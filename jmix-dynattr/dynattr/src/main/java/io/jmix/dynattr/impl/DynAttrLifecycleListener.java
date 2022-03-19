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
import io.jmix.core.SaveContext;
import io.jmix.core.datastore.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.DynamicAttributesState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static io.jmix.core.entity.EntitySystemAccess.getExtraState;

@Component("dynat_DynAttrLifecycleListener")
public class DynAttrLifecycleListener implements DataStoreEventListener, DataStoreCustomizer {

    @Autowired
    protected DynAttrManager dynAttrManager;

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();
        Map<String, Object> hints = context.getHints();
        if (Boolean.TRUE.equals(hints.get(DynAttrQueryHints.LOAD_DYN_ATTR))) {
            dynAttrManager.loadValues(event.getResultEntities(), context.getFetchPlan(), context.getAccessConstraints());
        } else {
            dynAttrManager.addDynamicAttributesState(event.getResultEntities(), context.getFetchPlan());
        }
    }

    @Override
    public void entitySaving(DataStoreEntitySavingEvent event) {
        dynAttrManager.storeValues(event.getSaveContext().getEntitiesToSave(), event.getSaveContext().getAccessConstraints());
    }

    @Override
    public void entityReload(DataStoreEntityReloadEvent event) {
        LoadContext<?> loadContext = event.getLoadContext();
        SaveContext saveContext = event.getSaveContext();

        Object entity = saveContext.getEntitiesToSave().stream()
                .filter(e -> Objects.equals(EntityValues.getId(e), loadContext.getId()))
                .findFirst()
                .orElse(null);

        DynamicAttributesState state = getExtraState(entity, DynamicAttributesState.class);
        if (state != null) {
            DynamicAttributes dynamicAttributes = state.getDynamicAttributes();
            if (dynamicAttributes != null) {
                loadContext.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
            }
        }
    }

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof AbstractDataStore) {
            ((AbstractDataStore) dataStore).registerInterceptor(this);
        }
    }
}
