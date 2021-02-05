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

package io.jmix.hibernate.impl;

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.impl.EntityListenerType;
import io.jmix.data.impl.converters.AuditConversionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.lang.reflect.Field;
import java.util.*;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

public class HibernateDelegateJmixEntityManager extends SessionDelegatorBaseImpl implements EntityManager {

    private BeanFactory beanFactory;

    private HibernatePersistenceSupport support;
    private ExtendedEntities extendedEntities;
    private Metadata metadata;
    private MetadataTools metadataTools;
    private EntityStates entityStates;
    private EntityListenerManager entityListenerMgr;
    private HibernateEntityChangedEventManager entityChangedEventManager;
    private TimeSource timeSource;
    private AuditInfoProvider auditInfoProvider;
    private AuditConversionService auditConverter;

    private static final Logger log = LoggerFactory.getLogger(HibernateDelegateJmixEntityManager.class);

    public HibernateDelegateJmixEntityManager(SessionImplementor delegate, BeanFactory beanFactory) {
        super(delegate);
        this.beanFactory = beanFactory;

        support = beanFactory.getBean(HibernatePersistenceSupport.class);
        extendedEntities = beanFactory.getBean(ExtendedEntities.class);
        metadataTools = beanFactory.getBean(MetadataTools.class);
        metadata = beanFactory.getBean(Metadata.class);
        entityStates = beanFactory.getBean(EntityStates.class);
        entityListenerMgr = beanFactory.getBean(EntityListenerManager.class);
        entityChangedEventManager = beanFactory.getBean(HibernateEntityChangedEventManager.class);
        timeSource = beanFactory.getBean(TimeSource.class);
        auditInfoProvider = beanFactory.getBean(AuditInfoProvider.class);
        auditConverter = beanFactory.getBean(AuditConversionService.class);
    }

    @Override
    public void persist(Object entity) {
        internalPersist(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object merge(Object object) {
        log.debug("merge {}", object);

        if (!(object instanceof Entity)) {
            return delegate.merge(object);
        }

        if (entityStates.isManaged(object)) {
            return object;
        }

        String storeName = support.getStorageName(delegate.unwrap(Session.class));
        entityListenerMgr.fireListener(object, EntityListenerType.BEFORE_ATTACH, storeName);

        if ((entityStates.isNew(object) || !entityStates.isDetached(object)) && EntityValues.getId(object) != null) {
            // if a new instance is passed to merge(), we suppose it is persistent but "not detached"
            Object destEntity = findOrCreate(object.getClass(), EntityValues.getId(object));
            deepCopyIgnoringNulls(object, destEntity, Sets.newIdentityHashSet());
            return destEntity;
        }

        Object merged = internalMerge(object);
        support.registerInstance(merged, delegate());
        return merged;
    }

    @Override
    public void remove(Object entity) {
        log.debug("remove {}", entity);

        if (!(entity instanceof Entity)) {
            delegate.remove(entity);
            return;
        }

        if (entityStates.isDetached(entity) || !delegate.contains(entity)) {
            entity = internalMerge(entity);
        }

        if (EntityValues.isSoftDeletionSupported(entity) && PersistenceHints.isSoftDeletion(delegate)) {
            Class<?> deletedDateClass = EntitySystemAccess.getDeletedDateClass(entity);
            Class<?> deletedByClass = EntitySystemAccess.getDeletedByClass(entity);

            if (deletedDateClass != null) {
                EntityValues.setDeletedDate(entity,
                        auditConverter.convert(timeSource.currentTimestamp(), deletedDateClass));
            }

            if (deletedByClass != null) {
                EntityValues.setDeletedBy(entity,
                        auditConverter.convert(auditInfoProvider.getCurrentUser(), deletedByClass));
            }
        } else {
            delegate.remove(entity);
            getEntityEntry(entity).setRemoved(true);
        }
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return internalFind(entityClass, primaryKey, LockModeType.NONE, Collections.emptyMap());
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return internalFind(entityClass, primaryKey, LockModeType.NONE, properties);
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return internalFind(entityClass, primaryKey, lockMode, Collections.emptyMap());
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return internalFind(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        //noinspection unchecked
        Class<T> effectiveClass = extendedEntities.getEffectiveClass(entityClass);

        T reference = delegate.getReference(effectiveClass, primaryKey);
        getEntityEntry(reference).setNew(false);
        return reference;
    }

    @Override
    public Object getDelegate() {
        return delegate();
    }

    @Override
    public void flush() {
        entityChangedEventManager.beforeFlush((SessionImplementor) getDelegate(), support.getInstances(this));
        support.processFlush(delegate(), false);
        delegate.flush();
    }

    @Override
    public void detach(Object entity) {
        delegate.detach(entity);
        if (entity instanceof Entity) {
            support.detach(delegate(), entity);
        }
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        if (PersistenceHints.SOFT_DELETION.equals(propertyName)) {
            Preconditions.checkNotNullArgument(value, "soft deletion value must not be null");
            setSoftDeletion((Boolean) value);
        } else {
            delegate.setProperty(propertyName, value);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public JmixHibernateQuery createQuery(String qlString) {
        return new JmixHibernateQuery<>(delegate, getQueryPlan(qlString, false).getParameterMetadata(), beanFactory, false, qlString, null);
    }

    @Override
    public <T> JmixHibernateQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return new JmixHibernateQuery<>(delegate, getQueryPlan(qlString, false).getParameterMetadata(), beanFactory, false, qlString, resultClass);
    }

    protected HQLQueryPlan getQueryPlan(String query, boolean shallow) throws HibernateException {
        return delegate.getFactory().getQueryPlanCache().getHQLQueryPlan(query, shallow, getLoadQueryInfluencers().getEnabledFilters());
    }


    private void internalPersist(Object entity) {
        delegate.persist(entity);
        if (entity instanceof Entity) {
            support.registerInstance(entity, delegate());
        }
    }

    private void setSoftDeletion(boolean softDeletion) {
        delegate.setProperty(PersistenceHints.SOFT_DELETION, softDeletion);
        if (softDeletion) {
            delegate.enableFilter(SoftDeletionFilterDefinition.NAME);
        } else {
            delegate.disableFilter(SoftDeletionFilterDefinition.NAME);
        }
    }

    private boolean isSoftDeletion(Map<String, Object> properties) {
        Boolean softDeletionInProps = properties == null ? null : (Boolean) properties.get(PersistenceHints.SOFT_DELETION);
        return (softDeletionInProps == null || softDeletionInProps) && PersistenceHints.isSoftDeletion(delegate);
    }

    @Nullable
    private <T> T internalFind(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(primaryKey, "primaryKey is null");

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(entityClass);

        Collection<FetchPlan> fetchPlans = PersistenceHints.getFetchPlans(properties);
        if (!fetchPlans.isEmpty()) {
            return findPartial(metaClass, primaryKey, fetchPlans);
        }

        Object realId = primaryKey;
        log.debug("find {} by id={}", entityClass.getSimpleName(), realId);
        Class<T> javaClass = metaClass.getJavaClass();

        T entity = delegate.find(javaClass, realId, lockMode, properties);

        if (entity != null && EntityValues.isSoftDeleted((Entity) entity)
                && isSoftDeletion(properties))
            return null; // in case of entity cache
        else
            return entity;
    }

    private <T> T findPartial(MetaClass metaClass, Object id, Collection<FetchPlan> fetchPlans) {
        Object realId = id;
        log.debug("find {} by id={}, fetchPlans={}", metaClass.getJavaClass().getSimpleName(), realId, fetchPlans);

        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName == null)
            throw new IllegalStateException("Cannot determine PK name for entity " + metaClass);

        JmixHibernateQuery query = (JmixHibernateQuery) createQuery(String.format("select e from %s e where e.%s = ?1", metaClass.getName(), pkName));
        query.setSingleResultExpected(true);
        query.setParameter(1, realId);
        query.setHint(PersistenceHints.FETCH_PLAN, fetchPlans);

        //noinspection unchecked
        return (T) query.getSingleResultOrNull();
    }

    private Object internalMerge(Object entity) {
        try {
            Object merged = delegate.merge(entity);
            getEntityEntry(merged).setNew(false);
            // copy non-persistent attributes to the resulting merged instance
            for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
                if (!metadataTools.isPersistent(property) && !property.isReadOnly()) {
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
            boolean softDeletion = PersistenceHints.isSoftDeletion(delegate);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T findOrCreate(Class<T> entityClass, Object id) {
        Object reloadedRef = find(entityClass, id);
        if (reloadedRef == null) {
            reloadedRef = metadata.create(entityClass);
            EntityValues.setId(reloadedRef, id);
            internalPersist(reloadedRef);
        }
        return (T) reloadedRef;
    }

    /**
     * Copies all property values from source to dest excluding null values.
     */
    protected void deepCopyIgnoringNulls(Object source, Object dest, Set<Object> visited) {
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

            Object value = EntityValues.getValue(source, name);
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
                    Collection<Object> srcCollection = (Collection) value;
                    Collection<Object> dstCollection = EntityValues.getValue(dest, name);
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
                        for (Object srcRef : srcCollection) {
                            Object reloadedRef = findOrCreate(srcRef.getClass(), EntityValues.getId(srcRef));
                            dstCollection.add(reloadedRef);
                            deepCopyIgnoringNulls(srcRef, reloadedRef, visited);
                        }
                    }
                } else {
                    Object destRef = EntityValues.getValue(dest, name);
                    if (value.equals(destRef)) {
                        deepCopyIgnoringNulls(value, destRef, visited);
                    } else {
                        Object reloadedRef = findOrCreate(value.getClass(), EntityValues.getId(value));
                        EntityValues.setValue(dest, name, reloadedRef);
                        deepCopyIgnoringNulls(value, reloadedRef, visited);
                    }
                }
            } else if (metadataTools.isEmbedded(srcProperty)) {
                Object destRef = EntityValues.getValue(dest, name);
                if (destRef != null) {
                    deepCopyIgnoringNulls(value, destRef, visited);
                } else {
                    Object newRef = metadata.create(srcProperty.getRange().asClass().getJavaClass());
                    EntityValues.setValue(dest, name, newRef);
                    deepCopyIgnoringNulls(value, newRef, visited);
                }
            } else {
                EntityValues.setValue(dest, name, value);
            }
        }
    }
}
