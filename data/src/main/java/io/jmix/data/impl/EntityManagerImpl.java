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

import com.google.common.collect.Sets;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.EntityManager;
import io.jmix.data.Query;
import io.jmix.data.TypedQuery;
import io.jmix.core.*;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.persistence.internal.helper.CubaUtil;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;

@Component(EntityManager.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityManagerImpl implements EntityManager {

    protected javax.persistence.EntityManager delegate;

    @Inject
    protected BeanLocator beanLocator;
    @Inject
    protected Metadata metadata;
    @Inject
    private MetadataTools metadataTools;
    @Inject
    protected EntityListenerManager entityListenerMgr;
    @Inject
    protected EntityPersistingEventManager entityPersistingEventMgr;
    @Inject
    protected PersistenceSupport support;
    @Inject
    protected AuditInfoProvider auditInfoProvider;
    @Inject
    protected TimeSource timeSource;
    @Inject
    protected EntityStates entityStates;
    @Inject
    protected ExtendedEntities extendedEntities;
    @Inject
    protected FetchPlanRepository viewRepository;

    protected boolean softDeletion = true;

    private static final Logger log = LoggerFactory.getLogger(EntityManagerImpl.class);

    protected EntityManagerImpl(javax.persistence.EntityManager jpaEntityManager) {
        this.delegate = jpaEntityManager;
    }

    @Override
    public javax.persistence.EntityManager getDelegate() {
        return delegate;
    }

    @Override
    public boolean isSoftDeletion() {
        return softDeletion;
    }

    @Override
    public void setSoftDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
        CubaUtil.setSoftDeletion(softDeletion);
        CubaUtil.setOriginalSoftDeletion(softDeletion);
    }

    @Override
    public void persist(Entity entity) {
        entityPersistingEventMgr.publishEvent(entity);
        internalPersist(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> T merge(T entity) {
        log.debug("merge {}", entity);

        if (entityStates.isManaged(entity))
            return entity;

        String storeName = support.getStorageName(delegate.unwrap(UnitOfWork.class));
        entityListenerMgr.fireListener(entity, EntityListenerType.BEFORE_ATTACH, storeName);

        if ((entityStates.isNew(entity) || !entityStates.isDetached(entity)) && entity.getId() != null) {
            // if a new instance is passed to merge(), we suppose it is persistent but "not detached"
            Entity destEntity = findOrCreate(entity.getClass(), entity.getId());
            deepCopyIgnoringNulls(entity, destEntity, Sets.newIdentityHashSet());
            if (entityStates.isNew(destEntity)) {
                entityPersistingEventMgr.publishEvent(entity);
            }
            return (T) destEntity;
        }

        T merged = internalMerge(entity);
        support.registerInstance(merged, this);
        return merged;
    }

    @Override
    @Deprecated
    public <T extends Entity> T merge(T entity, @Nullable FetchPlan fetchPlan) {
        T managed = merge(entity);
        if (fetchPlan != null) {
            metadataTools.traverseAttributesByView(fetchPlan, managed, (e, p) -> { /* do nothing, just fetch */ });
        }
        return managed;
    }

    @Override
    @Deprecated
    public <T extends Entity> T merge(T entity, @Nullable String fetchPlanName) {
        if (fetchPlanName != null) {
            return merge(entity, viewRepository.getFetchPlan(entity.getClass(), fetchPlanName));
        } else {
            return merge(entity);
        }
    }

    @Override
    public void remove(Entity entity) {
        log.debug("remove {}", entity);

        if (entityStates.isDetached(entity)) {
            entity = internalMerge(entity);
        }
        if (entity instanceof SoftDelete && softDeletion) {
            ((SoftDelete) entity).setDeleteTs(timeSource.currentTimestamp());
            ((SoftDelete) entity).setDeletedBy(auditInfoProvider.getCurrentUserLogin());
        } else {
            delegate.remove(entity);
            if (entity instanceof BaseGenericIdEntity) {
                BaseEntityInternalAccess.setRemoved((BaseGenericIdEntity) entity, true);
            }
        }
    }

    @Override
    public <T extends Entity<K>, K> T find(Class<T> entityClass, K id) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(id, "id is null");

        Object realId = getRealId(id);
        log.debug("find {} by id={}", entityClass.getSimpleName(), realId);
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(entityClass);
        Class<T> javaClass = metaClass.getJavaClass();

        T entity = delegate.find(javaClass, realId);
        if (entity instanceof SoftDelete && ((SoftDelete) entity).isDeleted() && isSoftDeletion())
            return null; // in case of entity cache
        else
            return entity;
    }

    @Nullable
    @Override
    public <T extends Entity<K>, K> T find(Class<T> entityClass, K id, FetchPlan... fetchPlans) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(id, "id is null");

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(entityClass);
        return findWithViews(metaClass, id, Arrays.asList(fetchPlans));
    }

    @Nullable
    @Override
    public <T extends Entity<K>, K> T find(Class<T> entityClass, K id, String... fetchPlanNames) {
        FetchPlan[] viewArray = new FetchPlan[fetchPlanNames.length];
        for (int i = 0; i < fetchPlanNames.length; i++) {
            viewArray[i] = viewRepository.getFetchPlan(entityClass, fetchPlanNames[i]);
        }
        return find(entityClass, id, viewArray);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Entity> T findWithViews(MetaClass metaClass, Object id, List<FetchPlan> views) {
        Object realId = getRealId(id);
        log.debug("find {} by id={}, views={}", metaClass.getJavaClass().getSimpleName(), realId, views);

        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName == null)
            throw new IllegalStateException("Cannot determine PK name for entity " + metaClass);

        Query query = createQuery(String.format("select e from %s e where e.%s = ?1", metaClass.getName(), pkName));
        ((QueryImpl) query).setSingleResultExpected(true);
        query.setParameter(1, realId);
        for (FetchPlan view : views) {
            query.addView(view);
        }
        return (T) query.getFirstResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity<K>, K> T getReference(Class<T> clazz, K id) {
        Class<T> effectiveClass = extendedEntities.getEffectiveClass(clazz);

        T reference = delegate.getReference(effectiveClass, getRealId(id));
        BaseEntityInternalAccess.setNew((BaseGenericIdEntity) reference, false);
        return reference;
    }

    @SuppressWarnings("unchecked")
    protected <T> TypedQuery<T> createQueryInstance(boolean isNative, Class<T> resultClass) {
        return (TypedQuery<T>) beanLocator.getPrototype(Query.NAME,this, isNative, resultClass);
    }

    @Override
    public Query createQuery() {
        return createQueryInstance(false, null);
    }

    @Override
    public Query createQuery(String qlStr) {
        Query query = createQueryInstance(false, null);
        query.setQueryString(qlStr);
        return query;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        TypedQuery<T> query = createQueryInstance(false, resultClass);
        query.setQueryString(qlString);
        return query;
    }

    @Override
    public Query createNativeQuery() {
        return createQueryInstance(true, null);
    }

    @Override
    public Query createNativeQuery(String sql) {
        Query query = createQueryInstance(true, null);
        query.setQueryString(sql);
        return query;
    }

    @Override
    public <T extends Entity> TypedQuery<T> createNativeQuery(String sql, Class<T> resultClass) {
        TypedQuery<T> query = createQueryInstance(true, resultClass);
        query.setQueryString(sql);
        return query;
    }

    @Override
    @Deprecated
    public void fetch(Entity entity, FetchPlan fetchPlan) {
    }

    @Nullable
    @Override
    public <T extends Entity<K>, K> T reload(Class<T> entityClass, K id, String... fetchPlanNames) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(id, "id is null");

        if (id instanceof IdProxy && ((IdProxy) id).get() == null) {
            return null;
        }

        T entity = find(entityClass, id, fetchPlanNames);
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends Entity> T reload(T entity, String... fetchPlanNames) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        if (entity.getId() instanceof IdProxy && ((IdProxy) entity.getId()).get() == null) {
            return null;
        }

        Entity resultEntity = find(entity.getClass(), entity.getId(), fetchPlanNames);
        return (T) resultEntity;
    }

    @Override
    public <T extends Entity> T reloadNN(T entity, String... fetchPlanNames) {
        T reloaded = reload(entity, fetchPlanNames);
        if (reloaded == null)
            throw new EntityNotFoundException("Entity " + entity + " has been deleted");
        return reloaded;
    }

    @Override
    public void flush() {
        log.debug("flush");
        support.processFlush(this, false);
        delegate.flush();
    }

    @Override
    public void detach(Entity entity) {
        delegate.detach(entity);
        support.detach(this, entity);
    }

    @Override
    public Connection getConnection() {
        return delegate.unwrap(Connection.class);
    }

    /**
     * Copies all property values from source to dest excluding null values.
     */
    protected void deepCopyIgnoringNulls(Entity source, Entity dest, Set<Entity> visited) {
        if (visited.contains(source))
            return;
        visited.add(source);

        for (MetaProperty srcProperty : metadata.getClass(source).getProperties()) {
            String name = srcProperty.getName();

            if (!entityStates.isLoaded(source, name)) {
                continue;
            }

            if (srcProperty.isReadOnly()) {
                continue;
            }

            Object value = source.getValue(name);
            if (value == null) {
                continue;
            }

            if (srcProperty.getRange().isClass() && !metadataTools.isEmbedded(srcProperty)) {
                if (!metadataTools.isOwningSide(srcProperty))
                    continue;

                Class refClass = srcProperty.getRange().asClass().getJavaClass();
                if (!metadataTools.isPersistent(refClass))
                    continue;

                if (srcProperty.getRange().getCardinality().isMany()) {
                    if (!metadataTools.isOwningSide(srcProperty)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Collection<Entity> srcCollection = (Collection) value;
                    Collection<Entity> dstCollection = dest.getValue(name);
                    if (dstCollection == null)
                        throw new RuntimeException("Collection is null: " + srcProperty);
                    boolean equal = srcCollection.size() == dstCollection.size();
                    if (equal) {
                        if (srcProperty.getRange().isOrdered()) {
                            equal = Arrays.equals(srcCollection.toArray(), dstCollection.toArray());
                        } else {
                            equal = CollectionUtils.isEqualCollection(srcCollection, dstCollection);
                        }
                    }
                    if (!equal) {
                        dstCollection.clear();
                        for (Entity srcRef : srcCollection) {
                            Entity reloadedRef = findOrCreate(srcRef.getClass(), srcRef.getId());
                            dstCollection.add(reloadedRef);
                            deepCopyIgnoringNulls(srcRef, reloadedRef, visited);
                        }
                    }
                } else {
                    Entity srcRef = (Entity) value;
                    Entity destRef = dest.getValue(name);
                    if (srcRef.equals(destRef)) {
                        deepCopyIgnoringNulls(srcRef, destRef, visited);
                    } else {
                        Entity reloadedRef = findOrCreate(srcRef.getClass(), srcRef.getId());
                        dest.setValue(name, reloadedRef);
                        deepCopyIgnoringNulls(srcRef, reloadedRef, visited);
                    }
                }
            } else if (metadataTools.isEmbedded(srcProperty)) {
                Entity srcRef = (Entity) value;
                Entity destRef = dest.getValue(name);
                if (destRef != null) {
                    deepCopyIgnoringNulls(srcRef, destRef, visited);
                } else {
                    Entity newRef = metadata.create(srcProperty.getRange().asClass().getJavaClass());
                    dest.setValue(name, newRef);
                    deepCopyIgnoringNulls(srcRef, newRef, visited);
                }
            } else {
                dest.setValue(name, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Entity> T findOrCreate(Class<T> entityClass, Object id) {
        Entity reloadedRef = find(entityClass, id);
        if (reloadedRef == null) {
            reloadedRef = metadata.create(entityClass);
            if (reloadedRef instanceof BaseGenericIdEntity) {
                ((BaseGenericIdEntity) reloadedRef).setId(id);
            }
            internalPersist(reloadedRef);
        }
        return (T) reloadedRef;
    }

    protected <T extends Entity> T internalMerge(T entity) {
        try {
            CubaUtil.setSoftDeletion(false);
            CubaUtil.setOriginalSoftDeletion(false);

            UUID uuid = null;
            if (entity.getId() instanceof IdProxy) {
                uuid = ((IdProxy) entity.getId()).getUuid();
            }

            T merged = delegate.merge(entity);

            if (entity.getId() instanceof IdProxy
                    && uuid != null
                    && !uuid.equals(((IdProxy) merged.getId()).getUuid())) {
                ((IdProxy) merged.getId()).setUuid(uuid);
            }

            // copy non-persistent attributes to the resulting merged instance
            for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
                if (metadataTools.isNotPersistent(property) && !property.isReadOnly()) {
                    // copy using reflection to avoid executing getter/setter code
                    Field field = FieldUtils.getField(entity.getClass(), property.getName(), true);
                    if (field != null) {
                        try {
                            Object value = FieldUtils.readField(field, entity);
                            if (value != null) {
                                FieldUtils.writeField(field, merged, value);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Error copying non-persistent attribute value to merged instance", e);
                        }
                    }
                }
            }

            return merged;
        } finally {
            CubaUtil.setSoftDeletion(softDeletion);
            CubaUtil.setOriginalSoftDeletion(softDeletion);
        }
    }

    protected void internalPersist(Entity entity) {
        delegate.persist(entity);
        support.registerInstance(entity, this);
    }

    protected Object getRealId(Object id) {
        return id instanceof IdProxy ? ((IdProxy) id).getNN() : id;
    }
}
