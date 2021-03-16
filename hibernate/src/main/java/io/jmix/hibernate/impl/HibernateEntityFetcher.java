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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.impl.EntityFetcher;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;

public class HibernateEntityFetcher extends EntityFetcher {

    private static final Logger log = LoggerFactory.getLogger(HibernateEntityFetcher.class);

    @Nullable
    protected Object unproxy(@Nullable Object value) {
        if (value != null && !(value instanceof Collection)) {
            value = HibernateUtils.initializeAndUnproxy(value);
        }

        return value;
    }

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

        MetaClass metaClass = metadata.getClass(entity.getClass());
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
                    Map<Object, Object> itemsToErase = new HashMap<>();
                    for (Object item : new ArrayList(((Collection) value))) {
                        if (item instanceof Entity) {
                            Object result = item;
                            if (result instanceof HibernateProxy) {
                                result = unproxy(result);
                                if (EntityValues.isSoftDeleted(result)) {
                                    itemsToErase.put(EntityValues.getId(result), item);
                                    continue;
                                }
                                setResultToList(value, item, result);
                            }
                            if (entityStates.isDetached(result)) {
                                Object finalResult = result;
                                fetchReloaded(result, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                                    setResultToList(value, finalResult, managed);
                                });
                            } else {
                                fetch(item, propertyFetchPlan, visited, optimizeForDetached);
                            }
                        }
                    }
                    for (Map.Entry entry : itemsToErase.entrySet()) {
                        removeItem(value, entry.getValue());
                        EntitySystemAccess.getSecurityState(entity).addErasedId(property.getName(), entry.getKey());
                    }
                } else {
                    Object result = value;
                    if (result instanceof HibernateProxy) {
                        result = unproxy(result);
                        if (result != null && EntityValues.isSoftDeleted(result)) {
                            result = null;
                        }
                        EntityValues.setValue(entity, property.getName(), result);
                    }
                    if (result instanceof Entity) {
                        boolean isEmbeddable = EntitySystemAccess.isEmbeddable(result);
                        if (!metaProperty.isReadOnly() && entityStates.isDetached(result) && !isEmbeddable) {
                            fetchReloaded(result, propertyFetchPlan, visited, optimizeForDetached, managed -> {
                                EntityValues.setValue(entity, property.getName(), managed);
                            });
                        } else {
                            fetch(result, propertyFetchPlan, visited, optimizeForDetached);
                        }
                    }
                }
            }
        }
    }

    private void setResultToList(Object value, Object item, Object result) {
        if (value instanceof List) {
            List list = (List) value;
            list.set(list.indexOf(item), result);
        } else {
            Collection collection = (Collection) value;
            collection.remove(item);
            collection.add(result);
        }
    }

    private void removeItem(Object value, Object item) {
        if (value instanceof List) {
            List list = (List) value;
            list.remove(list.indexOf(item));
        } else {
            Collection collection = (Collection) value;
            collection.remove(item);
        }
    }
}
