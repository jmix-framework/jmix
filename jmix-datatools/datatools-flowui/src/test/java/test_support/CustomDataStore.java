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

package test_support;

import io.jmix.core.DataStore;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory store that behaves the way a store without JPQL support behaves: a single entity can
 * only be found by its id, and a query string means nothing to it and is ignored.
 */
public class CustomDataStore implements DataStore {

    protected String name;

    protected final Map<Object, Object> storage = new LinkedHashMap<>();

    /**
     * Puts an entity into the store and marks it as loaded, the way a real store does when it reads
     * an existing row.
     */
    public void put(Object entity) {
        EntitySystemAccess.getEntityEntry(entity).setNew(false);
        storage.put(EntityValues.getId(entity), entity);
    }

    public void clear() {
        storage.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        if (context.getId() == null) {
            throw new IllegalArgumentException("Entity id is null");
        }
        return storage.get(context.getId());
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        // The query string is deliberately ignored, as it is by a store that does not speak JPQL.
        Class<?> javaClass = context.getEntityMetaClass().getJavaClass();
        List<Object> result = new ArrayList<>();
        for (Object entity : storage.values()) {
            if (javaClass.isInstance(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public long getCount(LoadContext<?> context) {
        return loadList(context).size();
    }

    @Override
    public Set<?> save(SaveContext context) {
        for (Object entity : context.getEntitiesToSave()) {
            put(entity);
        }
        for (Object entity : context.getEntitiesToRemove()) {
            storage.remove(EntityValues.getId(entity));
        }
        return Set.copyOf(context.getEntitiesToSave());
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return new ArrayList<>();
    }

    @Override
    public long getCount(ValueLoadContext context) {
        return 0;
    }
}
