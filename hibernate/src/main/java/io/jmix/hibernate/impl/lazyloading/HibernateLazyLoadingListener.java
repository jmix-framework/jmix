/*
 * Copyright 2021 Haulmont.
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

package io.jmix.hibernate.impl.lazyloading;

import io.jmix.core.Entity;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.constraint.InMemoryConstraint;
import io.jmix.core.datastore.DataStoreAfterEntityLoadEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component("hibernate_LazyLoadingListener")
public class HibernateLazyLoadingListener implements DataStoreEventListener {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PersistentAttributesLoadChecker persistentAttributesLoadChecker;

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();

        for (Object entity : event.getResultEntities()) {
            if (entity instanceof Entity) {
                processExtraStates((Entity) entity, context);
            }
        }
    }

    public void processExtraStates(Entity entity, LoadContext<?> loadContext) {
        Map<String, Object> hints = loadContext.getHints();
        Map<String, Serializable> serializableHints = new HashMap<>();

        for (Map.Entry<String, Object> entry : hints.entrySet()) {
            if (entry.getValue() instanceof Serializable) {
                serializableHints.put(entry.getKey(), (Serializable) entry.getValue());
            }
        }

        Set<Entity> loadedEntities = new HashSet<>();
        traverseEntities(entity, loadedEntities);

        LoadOptionsState.Builder stateBuilder = LoadOptionsState.builder()
                .softDeletion(loadContext.isSoftDeletion())
                .accessConstraints(loadContext.getAccessConstraints().stream()
                        .filter(c -> c instanceof InMemoryConstraint)
                        .collect(Collectors.toList()))
                .hints(serializableHints);

        for (Entity loadedEntity : loadedEntities) {
            if (EntitySystemAccess.getExtraState(loadedEntity, LoadOptionsState.class) == null) {
                EntitySystemAccess.addExtraState(loadedEntity, stateBuilder.build(EntitySystemAccess.getEntityEntry(loadedEntity)));
            }
        }
    }

    protected void traverseEntities(Entity entity, Set<Entity> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (property.getRange().isClass()) {
                if (persistentAttributesLoadChecker.isLoaded(entity, property.getName())) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value != null) {
                        if (value instanceof Collection) {
                            for (Object item : ((Collection) value)) {
                                traverseEntities((Entity) item, visited);
                            }
                        } else {
                            traverseEntities((Entity) value, visited);
                        }
                    }
                }
            }
        }
    }
}
