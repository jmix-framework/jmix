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

import io.jmix.core.*;
import io.jmix.core.datastore.DataStoreEntityLoadingEvent;
import io.jmix.core.datastore.DataStoreEntitySavingEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.hibernate.impl.HibernateUtils;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Component("hibernate_HibernateUnproxyListener")
public class HibernateDatastoreListener implements DataStoreEventListener {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected DataManager dataManager;

    @Override
    public void entitySaving(DataStoreEntitySavingEvent event) {
        Set<Object> visited = new LinkedHashSet<>();
        for (Object entity : event.getEntities()) {
            EntityPreconditions.checkEntityType(entity);
            traverseEntities(entity, visited, (e, reference, propertyName) -> {
                if (reference instanceof HibernateProxy) {
                    LazyInitializer initializer = ((HibernateProxy) reference).getHibernateLazyInitializer();
                    reference = HibernateUtils.initializeAndUnproxy(reference);
                    if (reference == null) {
                        reference = dataManager.getReference(initializer.getPersistentClass(), initializer.getIdentifier());
                    }
                    EntityValues.setValue(e, propertyName, reference);
                }
                return reference;
            });
        }
    }

    @Override
    public void entityLoading(DataStoreEntityLoadingEvent event) {
        Set<Object> visited = new LinkedHashSet<>();
        for (Object entity : event.getEntities()) {
            EntityPreconditions.checkEntityType(entity);
            traverseEntities(entity, visited, (e, reference, propertyName) -> {
                if (reference instanceof HibernateProxy) {
                    reference = HibernateUtils.initializeAndUnproxy(reference);
                    EntityValues.setValue(e, propertyName, reference);
                }
                return reference;
            });
        }
    }

    protected void traverseEntities(Object entity, Set<Object> visited, Visitor visitor) {
        if (visited.contains(entity)) {
            return;
        }

        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (isPersistentEntityProperty(property) && entityStates.isLoaded(entity, property.getName())) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value instanceof Collection<?>) {
                    //noinspection unchecked
                    for (Object item : (Collection<Object>) value) {
                        item = visitor.visit(entity, item, property.getName());
                        if (item != null) {
                            traverseEntities(item, visited, visitor);
                        }
                    }
                } else if (value instanceof Entity) {
                    value = visitor.visit(entity, value, property.getName());
                    if (value != null) {
                        traverseEntities(value, visited, visitor);
                    }
                }
            }
        }
    }

    protected boolean isPersistentEntityProperty(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass() && metadataTools.isJpa(metaProperty);
    }

    protected interface Visitor {
        Object visit(Object entity, Object reference, String propertyName);
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE - 10;
    }
}
