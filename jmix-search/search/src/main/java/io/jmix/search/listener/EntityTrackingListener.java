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

package io.jmix.search.listener;

import io.jmix.core.DataStore;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.DataStoreBeforeEntitySaveEvent;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component("search_EntityTrackingListener")
public class EntityTrackingListener implements DataStoreEventListener, DataStoreCustomizer {

    private boolean isContextReadyForListening = false;
    private final ApplicationContext applicationContext;
    private EntityTrackingListenerDelegate entityTrackingListenerDelegate;

    public EntityTrackingListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        isContextReadyForListening = true;
    }

    private void runDelegateWithinContext(Consumer<EntityTrackingListenerDelegate> delegateConsumer) {
        if (!isContextReadyForListening) {
            return;
        }
        if (entityTrackingListenerDelegate == null) {
            entityTrackingListenerDelegate = applicationContext.getBean(EntityTrackingListenerDelegate.class);
        }
        // переделать на то, чтобы не слушать события от
        delegateConsumer.accept(entityTrackingListenerDelegate);
    }


    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof AbstractDataStore) {
            AbstractDataStore abstractStore = (AbstractDataStore) dataStore;
            abstractStore.registerInterceptor(this);
        }
    }

    @Override
    public void beforeEntitySave(DataStoreBeforeEntitySaveEvent event) {

        /*
            This event is used only for resolving indexing entity instances dependent on some removed entity instance.
            Dependencies are found before performing removal because it's required to keep all links between
            instances involved in affected relationship.

            Attempt to load dependencies within processing of EntityChangedEvent requires another transaction to access
            before-removal database state, but it can lead to deadlock on some databases (like MSSQL, HyperSQL) without
            additional configuration.

            Found dependencies are stored into short-term in-memory cache from which they will be retrieved and enqueued
            within processing of EntityChangedEvent.
         */
        runDelegateWithinContext(entityTrackingListenerDelegate -> {
            entityTrackingListenerDelegate.doOnBeforeEntitySave(event);
        });
    }

    @EventListener
    public void onEntityChangedBeforeCommit(EntityChangedEvent<?> event) {
        runDelegateWithinContext(entityTrackingListenerDelegate -> {
            entityTrackingListenerDelegate.doOnEntityChangedBeforeCommit(event);
        });
    }


}
