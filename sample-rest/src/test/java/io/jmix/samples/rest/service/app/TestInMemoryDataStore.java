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

package io.jmix.samples.rest.service.app;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("test_InMemoryDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestInMemoryDataStore implements DataStore {

    private String name;

    private Map<String, Map<Object, Object>> entities = new ConcurrentHashMap<>();

    @Autowired
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
    public <E> E load(LoadContext<E> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        if (instances == null)
            return null;
        else
            return (E) instances.get(context.getId());
    }

    @Override
    public <E> List<E> loadList(LoadContext<E> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        if (instances == null)
            return Collections.emptyList();
        else
            return new ArrayList(instances.values());
    }

    @Override
    public long getCount(LoadContext<?> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        if (instances == null)
            return 0;
        else
            return instances.size();
    }

    @Override
    public Set<Object> save(SaveContext context) {
        Set<Object> result = new HashSet<>();

        for (Object entity : context.getEntitiesToSave()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Object> instances = entities.get(metaClassName);
            if (instances == null) {
                instances = new ConcurrentHashMap<>();
                entities.put(metaClassName, instances);
            }
            instances.put(EntityValues.getId(entity), entity);
            result.add(entity);
        }
        for (Object entity : context.getEntitiesToRemove()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Object> instances = entities.get(metaClassName);
            if (instances != null) {
                instances.remove(EntityValues.getId(entity));
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
