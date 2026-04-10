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
import io.jmix.core.datastore.DataStoreAfterEntityLoadEvent;
import io.jmix.core.datastore.DataStoreBeforeEntityLoadEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.StoreDescriptor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractDataStoreLoadLifecycleTest {

    @Test
    void testLoadByItemsAppliesAfterLoadReorderingBeforePaging() {
        TestDataStore dataStore = new TestDataStore(List.of("a", "b", "c", "d", "e"));
        List<Integer> afterLoadSizes = new ArrayList<>();

        dataStore.registerInterceptor(new DataStoreEventListener() {
            @Override
            public void beforeEntityLoad(DataStoreBeforeEntityLoadEvent event) {
                event.setLoadByItems(2);
            }

            @Override
            public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
                afterLoadSizes.add(event.getResultEntities().size());
                List<Object> result = new ArrayList<>(event.getResultEntities());
                Collections.reverse(result);
                event.setResultEntities(result);
            }
        });

        LoadContext<Object> context = new LoadContext<>(new TestMetaClass("TestEntity"));
        context.setQuery(new LoadContext.Query("select e from TestEntity e")
                .setFirstResult(1)
                .setMaxResults(2));

        List<Object> result = dataStore.loadList(context);

        assertEquals(List.of("d", "c"), result);
        assertEquals(List.of(5), afterLoadSizes);
        assertEquals(List.of(
                new BatchRequest(0, 2),
                new BatchRequest(2, 2),
                new BatchRequest(4, 2)
        ), dataStore.batchRequests);
    }

    @Test
    void testAfterEntityLoadCanReplaceResultList() {
        TestDataStore dataStore = new TestDataStore(List.of("a", "b", "c"));

        dataStore.registerInterceptor(new DataStoreEventListener() {
            @Override
            public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
                event.setResultEntities(List.of("c", "a"));
            }
        });

        LoadContext<Object> context = new LoadContext<>(new TestMetaClass("TestEntity"));
        context.setQuery(new LoadContext.Query("select e from TestEntity e"));

        assertEquals(List.of("c", "a"), dataStore.loadList(context));
    }

    private static class TestDataStore extends AbstractDataStore {
        private final List<Object> items;
        private final List<BatchRequest> batchRequests = new ArrayList<>();

        private TestDataStore(List<Object> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        protected Object loadOne(LoadContext<?> context) {
            return null;
        }

        @Override
        protected List<Object> loadAll(LoadContext<?> context) {
            LoadContext.Query query = context.getQuery();
            int firstResult = query != null ? query.getFirstResult() : 0;
            int maxResults = query != null ? query.getMaxResults() : 0;
            batchRequests.add(new BatchRequest(firstResult, maxResults));

            if (maxResults == 0) {
                return new ArrayList<>(items);
            }

            int fromIndex = Math.min(firstResult, items.size());
            int toIndex = Math.min(firstResult + maxResults, items.size());
            return new ArrayList<>(items.subList(fromIndex, toIndex));
        }

        @Override
        protected long countAll(LoadContext<?> context) {
            return items.size();
        }

        @Override
        protected Set<Object> saveAll(SaveContext context) {
            return Collections.emptySet();
        }

        @Override
        protected Set<Object> deleteAll(SaveContext context) {
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
            return new Object();
        }

        @Override
        protected Object beginSaveTransaction(boolean joinTransaction) {
            return new Object();
        }

        @Override
        protected void commitTransaction(Object transaction) {
        }

        @Override
        protected void rollbackTransaction(Object transaction) {
        }

        @Override
        protected TransactionContextState getTransactionContextState(boolean isJoinTransaction) {
            return new TransactionContextState() {
            };
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public void setName(String name) {
        }
    }

    private record BatchRequest(int firstResult, int maxResults) {
    }

    private static class TestMetaClass implements MetaClass {
        private final String name;

        private TestMetaClass(String name) {
            this.name = name;
        }

        @Override
        public @Nullable Session getSession() {
            return null;
        }

        @Override
        public <T> Class<T> getJavaClass() {
            @SuppressWarnings("unchecked")
            Class<T> javaClass = (Class<T>) Object.class;
            return javaClass;
        }

        @Override
        public @Nullable MetaClass getAncestor() {
            return null;
        }

        @Override
        public List<MetaClass> getAncestors() {
            return List.of();
        }

        @Override
        public Collection<MetaClass> getDescendants() {
            return List.of();
        }

        @Override
        public @Nullable MetaProperty findProperty(String name) {
            return null;
        }

        @Override
        public MetaProperty getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable MetaPropertyPath getPropertyPath(String propertyPath) {
            return null;
        }

        @Override
        public Collection<MetaProperty> getOwnProperties() {
            return List.of();
        }

        @Override
        public Collection<MetaProperty> getProperties() {
            return List.of();
        }

        @Override
        public Store getStore() {
            return new Store() {
                @Override
                public String getName() {
                    return "test";
                }

                @Override
                public StoreDescriptor getDescriptor() {
                    return new StoreDescriptor() {
                        @Override
                        public String getBeanName() {
                            return "test";
                        }

                        @Override
                        public boolean isJpa() {
                            return false;
                        }
                    };
                }

                @Override
                public boolean isNullsLastSorting() {
                    return true;
                }

                @Override
                public boolean supportsLobSortingAndFiltering() {
                    return true;
                }
            };
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, Object> getAnnotations() {
            return Map.of();
        }
    }
}
