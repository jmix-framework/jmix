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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.datastore.*;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataStoreInMemoryCRUDInterceptor implements DataStoreInterceptor {

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected EntityAttributesEraser entityAttributesEraser;

    public void beforeEntityLoad(BeforeEntityLoadEvent beforeLoadEvent) {
        LoadContext<?> context = beforeLoadEvent.getLoadContext();


        if (hasInMemoryRead(context)) {
            context.setLoadPartialEntities(false);
        }
    }

    public void beforeEntityCount(BeforeEntityCountEvent beforeCountEvent) {
        LoadContext<?> context = beforeCountEvent.getLoadContext();

        if (hasInMemoryRead(context)) {
            beforeCountEvent.setCountByItems();
        }
    }

    public void entityLoaded(EntityLoadedEvent loadedEvent) {
        LoadContext<?> context = loadedEvent.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass);
        accessManager.applyConstraints(crudContext, context.getAccessConstraints());

        List<Object> entities = new ArrayList<>();

        for (Object entity : loadedEvent.getResultEntities()) {
            if (!crudContext.isReadPermitted(entity)) {
                loadedEvent.excludeEntity(entity);
            } else {
                entities.add(entity);
            }
        }

        EntityAttributesEraser.ReferencesCollector references = entityAttributesEraser.collectErasingReferences(entities,
                entity -> {
                    InMemoryCrudEntityContext childCrudContext =
                            new InMemoryCrudEntityContext(metadata.getClass(entity.getClass()));
                    accessManager.applyConstraints(childCrudContext, context.getAccessConstraints());
                    return childCrudContext.isReadPermitted(entity);
                });
        loadedEvent.getEventState().setValue("erasedReferences", references);
    }

    @Override
    public void afterEntityLoad(AfterEntityLoadEvent event) {
        EntityAttributesEraser.ReferencesCollector references =
                (EntityAttributesEraser.ReferencesCollector) event.getEventState().getValue("erasedReferences");
        if (references != null) {
            entityAttributesEraser.eraseReferences(references);
        }
    }

    protected boolean hasInMemoryRead(LoadContext<?> context) {
        return collectEntityClasses(context).stream()
                .anyMatch(entityClass -> {
                    InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(entityClass);
                    accessManager.applyConstraints(crudContext, context.getAccessConstraints());
                    return crudContext.readPredicate() != null;
                });
    }

    protected Collection<MetaClass> collectEntityClasses(LoadContext<?> context) {
        if (context.getFetchPlan() == null) {
            return Collections.singletonList(extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass()));
        }
        return collectEntityClasses(context.getFetchPlan(), new HashSet<>());
    }

    protected Collection<MetaClass> collectEntityClasses(FetchPlan fetchPlan, Set<FetchPlan> visited) {
        if (visited.contains(fetchPlan)) {
            return Collections.emptySet();
        } else {
            visited.add(fetchPlan);
        }

        Set<MetaClass> entityClasses = new HashSet<>();
        entityClasses.add(metadata.getClass(fetchPlan.getEntityClass()));
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            if (property.getFetchPlan() != null) {
                entityClasses.addAll(collectEntityClasses(property.getFetchPlan(), visited));
            }
        }
        return entityClasses;
    }
}
