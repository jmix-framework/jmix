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

package test_support;

import io.jmix.core.*;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("test_InMemoryDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestInMemoryDataStore implements DataStore {

    private String name;

    private Map<String, Map<Object, Entity>> entities = new ConcurrentHashMap<>();

    @Inject
    private Metadata metadata;

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
    public <E extends Entity> E load(LoadContext<E> context) {
        Map<Object, Entity> instances = entities.get(context.getMetaClass());
        if (instances == null)
            return null;
        else
            return (E) instances.get(context.getId());
    }

    @Override
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        Map<Object, Entity> instances = entities.get(context.getMetaClass());
        if (instances == null)
            return Collections.emptyList();
        else
            return new ArrayList(instances.values());
    }

    @Override
    public long getCount(LoadContext<? extends Entity> context) {
        Map<Object, Entity> instances = entities.get(context.getMetaClass());
        if (instances == null)
            return 0;
        else
            return instances.size();
    }

    @Override
    public Set<Entity> save(SaveContext context) {
        Set<Entity> result = new HashSet<>();

        for (Entity entity : context.getEntitiesToSave()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Entity> instances = entities.get(metaClassName);
            if (instances == null) {
                instances = new ConcurrentHashMap<>();
                entities.put(metaClassName, instances);
            }
            instances.put(entity.getId(), entity);
            result.add(entity);
        }
        for (Entity entity : context.getEntitiesToRemove()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Entity> instances = entities.get(metaClassName);
            if (instances != null) {
                instances.remove(entity.getId());
            }
            result.add(entity);
        }

        return result;
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return new ArrayList<>();
    }

    public void clear() {
        entities.clear();
    }
}
