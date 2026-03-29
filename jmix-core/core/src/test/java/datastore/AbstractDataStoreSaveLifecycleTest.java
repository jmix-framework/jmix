/*
 * Copyright 2026 Haulmont.
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

package datastore;

import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.DataStoreBeforeSaveCommitEvent;
import io.jmix.core.datastore.DataStoreEntityDeletingEvent;
import io.jmix.core.datastore.DataStoreEntitySavingEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractDataStoreSaveLifecycleTest {

    @Test
    void testBeforeSaveCommitEventOrderAndDiscardSaved() {
        Object entity = new Object();
        SaveContext saveContext = new SaveContext().saving(entity).setDiscardSaved(true);
        List<String> eventOrder = new ArrayList<>();
        TestDataStore dataStore = new TestDataStore(entity, eventOrder);

        dataStore.registerInterceptor(new DataStoreEventListener() {
            @Override
            public void entitySaving(DataStoreEntitySavingEvent event) {
                eventOrder.add("entitySaving");
                assertFalse(dataStore.beforeSaveTransactionCommitCalled);
                assertFalse(dataStore.commitCalled);
            }

            @Override
            public void entityDeleting(DataStoreEntityDeletingEvent event) {
                eventOrder.add("entityDeleting");
            }

            @Override
            public void beforeSaveCommit(DataStoreBeforeSaveCommitEvent event) {
                eventOrder.add("beforeSaveCommit");
                assertSame(saveContext, event.getSaveContext());
                assertEquals(List.of(entity), event.getSavedEntities());
                assertTrue(event.getRemovedEntities().isEmpty());
                assertTrue(dataStore.beforeSaveTransactionCommitCalled);
                assertFalse(dataStore.commitCalled);
            }
        });

        Set<?> result = dataStore.save(saveContext);

        assertTrue(result.isEmpty());
        assertEquals(List.of(
                "saveAll",
                "entitySaving",
                "deleteAll",
                "entityDeleting",
                "beforeSaveTransactionCommit",
                "beforeSaveCommit",
                "commit"
        ), eventOrder);
        assertTrue(dataStore.rollbackCalled);
    }

    private static class TestDataStore extends AbstractDataStore {
        private final Object savedEntity;
        private final List<String> eventOrder;
        private boolean beforeSaveTransactionCommitCalled;
        private boolean commitCalled;
        private boolean rollbackCalled;

        private TestDataStore(Object savedEntity, List<String> eventOrder) {
            this.savedEntity = savedEntity;
            this.eventOrder = eventOrder;
        }

        @Override
        protected Object loadOne(LoadContext<?> context) {
            return null;
        }

        @Override
        protected List<Object> loadAll(LoadContext<?> context) {
            return Collections.emptyList();
        }

        @Override
        protected long countAll(LoadContext<?> context) {
            return 0;
        }

        @Override
        protected Set<Object> saveAll(SaveContext context) {
            eventOrder.add("saveAll");
            return Set.of(savedEntity);
        }

        @Override
        protected Set<Object> deleteAll(SaveContext context) {
            eventOrder.add("deleteAll");
            return Collections.emptySet();
        }

        @Override
        protected List<Object> loadAllValues(ValueLoadContext context) {
            return Collections.emptyList();
        }

        @Override
        protected long countAllValues(ValueLoadContext context) {
            return 0;
        }

        @Override
        protected Object beginLoadTransaction(boolean joinTransaction) {
            return new Transaction();
        }

        @Override
        protected Object beginSaveTransaction(boolean joinTransaction) {
            return new Transaction();
        }

        @Override
        protected void commitTransaction(Object transaction) {
            eventOrder.add("commit");
            commitCalled = true;
        }

        @Override
        protected void rollbackTransaction(Object transaction) {
            rollbackCalled = true;
        }

        @Override
        protected TransactionContextState getTransactionContextState(boolean isJoinTransaction) {
            return new TransactionContextState() {
            };
        }

        @Override
        protected void beforeSaveTransactionCommit(SaveContext context, java.util.Collection<Object> savedEntities,
                                                   java.util.Collection<Object> removedEntities) {
            eventOrder.add("beforeSaveTransactionCommit");
            beforeSaveTransactionCommitCalled = true;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public void setName(String name) {
        }

        private static class Transaction {
        }
    }
}
