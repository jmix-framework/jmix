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

package io.jmix.data;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Basic;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Consumer;

/**
 * Fetches entities by fetch plans by accessing reference attributes.
 */
@Component(EntityFetcher.NAME)
public class EntityFetcher {

    public static final String NAME = "jmix_EntityFetcher";

    private static final Logger log = LoggerFactory.getLogger(EntityFetcher.class);

    @Inject
    protected Metadata metadata;

    @Inject
    protected FetchPlanRepository viewRepository;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected StoreAwareLocator storeAwareLocator;

    /**
     * Fetch instance by fetch plan.
     */
    public void fetch(Entity instance, FetchPlan fetchPlan) {
        if (fetchPlan == null)
            return;
        fetch(instance, fetchPlan, new HashMap<>(), false);
    }

    /**
     * Fetch instance by fetch plan.
     */
    public void fetch(Entity instance, String fetchPlanName) {
        if (fetchPlanName == null)
            return;
        FetchPlan fetchPlan = viewRepository.getFetchPlan(instance.getClass(), fetchPlanName);
        fetch(instance, fetchPlan, new HashMap<>(), false);
    }

    /**
     * Fetch instance by fetch plan.
     *
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Entity instance, FetchPlan fetchPlan, boolean optimizeForDetached) {
        if (fetchPlan == null)
            return;
        fetch(instance, fetchPlan, new HashMap<>(), optimizeForDetached);
    }

    /**
     * Fetch instance by fetch plan.
     *
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Entity instance, String fetchPlanName, boolean optimizeForDetached) {
        if (fetchPlanName == null)
            return;
        FetchPlan fetchPlan = viewRepository.getFetchPlan(instance.getClass(), fetchPlanName);
        fetch(instance, fetchPlan, new HashMap<>(), optimizeForDetached);
    }

    @SuppressWarnings("unchecked")
    protected void fetch(Entity entity, FetchPlan fetchPlan, Map<Entity, Set<FetchPlan>> visited, boolean optimizeForDetached) {
        Set<FetchPlan> fetchPlans = visited.get(entity);
        if (fetchPlans == null) {
            fetchPlans = new HashSet<>();
            visited.put(entity, fetchPlans);
        } else if (fetchPlans.contains(fetchPlan)) {
            return;
        }
        fetchPlans.add(fetchPlan);

        if (log.isTraceEnabled()) log.trace("Fetching instance " + entity);

        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metaClass.getProperty(property.getName());
            if (!metaProperty.getRange().isClass() && !isLazyFetchedLocalAttribute(metaProperty))
                continue;

            if (log.isTraceEnabled()) log.trace("Fetching property " + property.getName());

            Object value = EntityValues.getValue(entity, property.getName());
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    for (Object item : new ArrayList(((Collection) value))) {
                        if (item instanceof Entity) {
                            Entity e = (Entity) item;
                            if (entityStates.isDetached(e)) {
                                fetchReloaded(e, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                                    if (value instanceof List) {
                                        List list = (List) value;
                                        list.set(list.indexOf(e), managed);
                                    } else {
                                        Collection collection = (Collection) value;
                                        collection.remove(e);
                                        collection.add(managed);
                                    }
                                });
                            } else {
                                fetch((Entity) item, propertyFetchPlan, visited, optimizeForDetached);
                            }
                        }
                    }
                } else if (value instanceof Entity) {
                    Entity e = (Entity) value;
                    boolean isEmbeddable =  e.__getEntityEntry().isEmbeddable();
                    if (!metaProperty.isReadOnly() && entityStates.isDetached(value) && !isEmbeddable) {
                        fetchReloaded(e, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                            EntityValues.setValue(entity, property.getName(), managed);
                        });
                    } else {
                        fetch(e, propertyFetchPlan, visited, optimizeForDetached);
                    }
                }
            }
        }
    }

    protected void fetchReloaded(Entity entity, FetchPlan fetchPlan, Map<Entity, Set<FetchPlan>> visited, boolean optimizeForDetached,
                                 Consumer<Entity> managedEntityConsumer) {
        if (!optimizeForDetached || needReloading(entity, fetchPlan)) {
            if (log.isTraceEnabled()) {
                log.trace("Object " + entity + " is detached, loading it");
            }
            String storeName = metadataTools.getStoreName(metadata.getClass(entity));
            if (storeName != null) {
                storeAwareLocator.getTransactionTemplate(storeName).executeWithoutResult(transactionStatus -> {
                    EntityManager em = storeAwareLocator.getEntityManager(storeName);
                    Entity managed = em.find(entity.getClass(), EntityValues.getId(entity));
                    if (managed != null) { // the instance here can be null if it has been deleted
                        managedEntityConsumer.accept(managed);
                        fetch(managed, fetchPlan, visited, optimizeForDetached);
                    }
                });
            }
        }
    }

    protected boolean needReloading(Entity entity, FetchPlan fetchPlan) {
        return !entityStates.isLoadedWithFetchPlan(entity, fetchPlan);
    }

    protected boolean isLazyFetchedLocalAttribute(MetaProperty metaProperty) {
        AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();
        Basic annotation = annotatedElement.getAnnotation(Basic.class);
        return annotation != null && annotation.fetch() == FetchType.LAZY;
    }
}
