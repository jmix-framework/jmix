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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.StoreAwareLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Consumer;

/**
 * Fetches entities by fetch plans by accessing reference attributes.
 */
@Component("data_EntityFetcher")
public class EntityFetcher {

    private static final Logger log = LoggerFactory.getLogger(EntityFetcher.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    /**
     * Fetch instance by fetch plan.
     *
     * @param fetchPlan if null, nothing happens
     */
    public void fetch(Object instance, @Nullable FetchPlan fetchPlan) {
        if (fetchPlan == null)
            return;
        fetch(instance, fetchPlan, new HashMap<>(), false);
    }

    /**
     * Fetch instance by fetch plan.
     *
     * @param fetchPlanName if null, nothing happens
     */
    public void fetch(Object instance, @Nullable String fetchPlanName) {
        if (fetchPlanName == null)
            return;
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(instance.getClass(), fetchPlanName);
        fetch(instance, fetchPlan, new HashMap<>(), false);
    }

    /**
     * Fetch instance by fetch plan.
     *
     * @param fetchPlan           if null, nothing happens
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Object instance, @Nullable FetchPlan fetchPlan, boolean optimizeForDetached) {
        if (fetchPlan == null)
            return;
        fetch(instance, fetchPlan, new HashMap<>(), optimizeForDetached);
    }

    /**
     * Fetch instance by fetch plan.
     *
     * @param fetchPlanName       if null, nothing happens
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Object instance, @Nullable String fetchPlanName, boolean optimizeForDetached) {
        if (fetchPlanName == null)
            return;
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(instance.getClass(), fetchPlanName);
        fetch(instance, fetchPlan, new HashMap<>(), optimizeForDetached);
    }

    @SuppressWarnings("unchecked")
    protected void fetch(Object entity, FetchPlan fetchPlan, Map<Object, Set<FetchPlan>> visited, boolean optimizeForDetached) {
        Set<FetchPlan> fetchPlans = visited.get(entity);
        if (fetchPlans == null) {
            fetchPlans = new HashSet<>();
            visited.put(entity, fetchPlans);
        } else if (fetchPlans.contains(fetchPlan)) {
            return;
        }
        fetchPlans.add(fetchPlan);

        if (log.isTraceEnabled()) log.trace("Fetching instance " + entity);

        MetaClass metaClass = metadata.getClass(entity);
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metaClass.getProperty(property.getName());
            if (!metaProperty.getRange().isClass() && !isLazyFetchedLocalAttribute(metaProperty)
                    || !metadataTools.isJpa(metaProperty))
                continue;

            if (log.isTraceEnabled()) log.trace("Fetching property " + property.getName());

            Object value = EntityValues.getValue(entity, property.getName());
            FetchPlan propertyFetchPlan = property.getFetchPlan();
            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    for (Object item : new ArrayList(((Collection) value))) {
                        if (item instanceof Entity) {
                            if (entityStates.isDetached(item)) {
                                fetchReloaded(item, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                                    if (value instanceof List) {
                                        List list = (List) value;
                                        list.set(list.indexOf(item), managed);
                                    } else {
                                        Collection collection = (Collection) value;
                                        collection.remove(item);
                                        collection.add(managed);
                                    }
                                });
                            } else {
                                fetch(item, propertyFetchPlan, visited, optimizeForDetached);
                            }
                        }
                    }
                } else if (value instanceof Entity) {
                    boolean isEmbeddable = EntitySystemAccess.isEmbeddable(value);
                    if (!metaProperty.isReadOnly() && entityStates.isDetached(value) && !isEmbeddable) {
                        fetchReloaded(value, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                            EntityValues.setValue(entity, property.getName(), managed);
                        });
                    } else {
                        fetch(value, propertyFetchPlan, visited, optimizeForDetached);
                    }
                }
            }
        }
    }

    protected void fetchReloaded(Object entity, FetchPlan fetchPlan, Map<Object, Set<FetchPlan>> visited, boolean optimizeForDetached,
                                 Consumer<Object> managedEntityConsumer) {
        if (!optimizeForDetached || needReloading(entity, fetchPlan)) {
            if (log.isTraceEnabled()) {
                log.trace("Object " + entity + " is detached, loading it");
            }
            String storeName = metadata.getClass(entity).getStore().getName();
            storeAwareLocator.getTransactionTemplate(storeName).executeWithoutResult(transactionStatus -> {
                EntityManager em = storeAwareLocator.getEntityManager(storeName);
                Object managed = em.find(entity.getClass(), EntityValues.getId(entity));
                if (managed != null) { // the instance here can be null if it has been deleted
                    managedEntityConsumer.accept(managed);
                    fetch(managed, fetchPlan, visited, optimizeForDetached);
                }
            });
        }
    }

    protected boolean needReloading(Object entity, FetchPlan fetchPlan) {
        return !entityStates.isLoadedWithFetchPlan(entity, fetchPlan);
    }

    protected boolean isLazyFetchedLocalAttribute(MetaProperty metaProperty) {
        AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();
        Basic annotation = annotatedElement.getAnnotation(Basic.class);
        return annotation != null && annotation.fetch() == FetchType.LAZY;
    }
}
