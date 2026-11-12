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

package io.jmix.core.datastore.security;

import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.datastore.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;

public abstract class BaseDataStoreInMemoryCrudListener implements DataStoreEventListener {

    private static final Logger log = LoggerFactory.getLogger(BaseDataStoreInMemoryCrudListener.class);

    private static final String ERASED_REFERENCES_COLLECTORS = "erasedReferencesCollectors";

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected ApplicationContext applicationContext;

    protected abstract EntityAttributesEraser getEntityAttributesEraser();

    public void beforeEntityLoad(DataStoreBeforeEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();

        if (hasInMemoryRead(context)) {
            context.setLoadPartialEntities(false);
        }
    }

    public void beforeEntityCount(DataStoreBeforeEntityCountEvent event) {
        LoadContext<?> context = event.getLoadContext();

        if (hasInMemoryRead(context)) {
            event.setCountByItems();
        }
    }

    @Override
    public void beforeEntitySave(DataStoreBeforeEntitySaveEvent event) {
        SaveContext context = event.getSaveContext();

        for (Object entity : context.getEntitiesToSave()) {
            if (!entityStates.isNew(entity)) {
                getEntityAttributesEraser().restoreAttributes(entity);
            }
        }

        for (Object entity : context.getEntitiesToRemove()) {
            getEntityAttributesEraser().restoreAttributes(entity);
        }
    }

    public void entityLoading(DataStoreEntityLoadingEvent event) {
        LoadContext<?> context = event.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyConstraints(crudContext, context.getAccessConstraints());

        List<Object> entities = new ArrayList<>();

        for (Object entity : event.getResultEntities()) {
            if (!crudContext.isReadPermitted(entity)) {
                log.debug("Reading entity {} is not permitted by access constraints", entity);
                event.excludeEntity(entity);
            } else {
                entities.add(entity);
            }
        }

        EntityAttributesEraser.ReferencesCollector references = getEntityAttributesEraser().collectErasingReferences(entities,
                entity -> {
                    InMemoryCrudEntityContext childCrudContext =
                            new InMemoryCrudEntityContext(metadata.getClass(entity), applicationContext);
                    accessManager.applyConstraints(childCrudContext, context.getAccessConstraints());
                    boolean readPermitted = childCrudContext.isReadPermitted(entity);
                    if (!readPermitted) {
                        log.debug("Reading entity {} is not permitted by access constraints", entity);
                    }
                    return readPermitted;
                });
        // A paged load fires this event once per query, so the references of all queries are kept.
        List<EntityAttributesEraser.ReferencesCollector> collectors = getReferencesCollectors(event.getEventState());
        if (collectors.isEmpty()) {
            event.getEventState().setValue(ERASED_REFERENCES_COLLECTORS, collectors);
        }
        collectors.add(references);
    }

    @Override
    public void afterEntityLoad(DataStoreAfterEntityLoadEvent event) {
        List<EntityAttributesEraser.ReferencesCollector> collectors = getReferencesCollectors(event.getEventState());
        if (collectors.size() == 1) {
            // One query: all permitted entities it loaded are returned.
            getEntityAttributesEraser().eraseReferences(collectors.get(0));
        } else if (collectors.size() > 1) {
            // Several queries of a paged load: entities outside the page are not returned and must stay unchanged.
            Set<Object> returnedEntities = collectReachableEntities(event.getResultEntities());
            EntityAttributesEraser.ReferencesCollector returnedReferences = new EntityAttributesEraser.ReferencesCollector();
            for (EntityAttributesEraser.ReferencesCollector collector : collectors) {
                for (Object entity : collector.getEntities()) {
                    if (returnedEntities.contains(entity)) {
                        for (String attribute : collector.getAttributes(entity)) {
                            for (Object reference : collector.getReferencesByAttribute(entity, attribute)) {
                                returnedReferences.addReference(entity, reference, attribute);
                            }
                        }
                    }
                }
            }
            getEntityAttributesEraser().eraseReferences(returnedReferences);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<EntityAttributesEraser.ReferencesCollector> getReferencesCollectors(EventSharedState eventState) {
        Object collectors = eventState.getValue(ERASED_REFERENCES_COLLECTORS);
        return collectors == null ? new ArrayList<>() : (List<EntityAttributesEraser.ReferencesCollector>) collectors;
    }

    /**
     * Returns the given entities and all entities reachable from them through loaded reference attributes.
     * Entities are compared by identity. Attributes that are not loaded are skipped, so nothing is loaded lazily.
     */
    protected Set<Object> collectReachableEntities(Collection<?> entities) {
        Set<Object> reachable = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Object> toVisit = new ArrayDeque<>(entities);
        while (!toVisit.isEmpty()) {
            Object entity = toVisit.pop();
            if (!reachable.add(entity)) {
                continue;
            }
            for (MetaProperty property : metadata.getClass(entity).getProperties()) {
                if (property.getRange().isClass() && entityStates.isLoaded(entity, property.getName())) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value instanceof Collection<?> collection) {
                        for (Object item : collection) {
                            if (item != null) {
                                toVisit.push(item);
                            }
                        }
                    } else if (value != null) {
                        toVisit.push(value);
                    }
                }
            }
        }
        return reachable;
    }

    @Override
    public void entitySaving(DataStoreEntitySavingEvent event) {
        SaveContext context = event.getSaveContext();

        for (Object entity : event.getEntities()) {
            MetaClass metaClass = metadata.getClass(entity);

            InMemoryCrudEntityContext entityContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyConstraints(entityContext, context.getAccessConstraints());

            if (isNew(context, entity)) {
                if (!entityContext.isCreatePermitted(entity)) {
                    throw new AccessDeniedException("entity", entity.toString(), "create");
                }
            } else {
                if (!entityContext.isUpdatePermitted(entity)) {
                    throw new AccessDeniedException("entity", entity.toString(), "update");
                }
            }
        }
    }

    @Override
    public void entityDeleting(DataStoreEntityDeletingEvent event) {
        SaveContext context = event.getSaveContext();

        for (Object entity : event.getEntities()) {
            MetaClass metaClass = metadata.getClass(entity);

            InMemoryCrudEntityContext entityContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyConstraints(entityContext, context.getAccessConstraints());

            if (!entityContext.isDeletePermitted(entity)) {
                throw new AccessDeniedException("entity", entity.toString(), "delete");
            }
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 10;
    }

    protected boolean hasInMemoryRead(LoadContext<?> context) {
        return collectEntityClasses(context).stream()
                .anyMatch(entityClass -> {
                    InMemoryCrudEntityContext crudContext = new InMemoryCrudEntityContext(entityClass, applicationContext);
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
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (propertyFetchPlan != null) {
                entityClasses.addAll(collectEntityClasses(propertyFetchPlan, visited));
            }
        }
        return entityClasses;
    }

    protected boolean isNew(SaveContext saveContext, Object entity) {
        Object entityToSave = saveContext.getEntitiesToSave().stream()
                .filter(e -> Objects.equals(e, entity))
                .findFirst()
                .orElse(null);
        return entityToSave != null && entityStates.isNew(entityToSave);
    }
}
