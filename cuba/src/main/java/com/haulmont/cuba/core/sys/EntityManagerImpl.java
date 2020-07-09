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
package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.TypedQuery;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.JmixEntityManager;
import io.jmix.data.impl.JmixQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;
import java.sql.Connection;

@Component(EntityManager.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityManagerImpl implements EntityManager {

    private JmixEntityManager delegate;

    @Autowired
    private BeanLocator beanLocator;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    private static final Logger log = LoggerFactory.getLogger(EntityManagerImpl.class);

    private EntityManagerImpl(javax.persistence.EntityManager jpaEntityManager) {
        this.delegate = (JmixEntityManager) jpaEntityManager;
    }

    @Override
    public javax.persistence.EntityManager getDelegate() {
        return delegate;
    }

    @Override
    public boolean isSoftDeletion() {
        return PersistenceHints.isSoftDeletion(delegate);
    }

    @Override
    public void setSoftDeletion(boolean softDeletion) {
        delegate.setProperty(PersistenceHints.SOFT_DELETION, softDeletion);
    }

    @Override
    public void persist(JmixEntity entity) {
        delegate.persist(entity);
    }

    @Override
    public <T extends JmixEntity> T merge(T entity) {
        return delegate.merge(entity);
    }

    @Override
    @Deprecated
    public <T extends JmixEntity> T merge(T entity, @Nullable FetchPlan fetchPlan) {
        T managed = merge(entity);
        if (fetchPlan != null) {
            metadataTools.traverseAttributesByView(fetchPlan, managed, (e, p) -> { /* do nothing, just fetch */ });
        }
        return managed;
    }

    @Override
    @Deprecated
    public <T extends JmixEntity> T merge(T entity, @Nullable String fetchPlanName) {
        if (fetchPlanName != null) {
            return merge(entity, fetchPlanRepository.getFetchPlan(entity.getClass(), fetchPlanName));
        } else {
            return merge(entity);
        }
    }

    @Override
    public void remove(JmixEntity entity) {
        delegate.remove(entity);
    }

    @Override
    public <T extends JmixEntity, K> T find(Class<T> entityClass, K id) {
        return delegate.find(entityClass, id);
    }

    @Nullable
    @Override
    public <T extends JmixEntity, K> T find(Class<T> entityClass, K id, FetchPlan... fetchPlans) {
        return delegate.find(entityClass, id, PersistenceHints.builder().withFetchPlans(fetchPlans).build());
    }

    @Nullable
    @Override
    public <T extends JmixEntity, K> T find(Class<T> entityClass, K id, String... fetchPlanNames) {
        FetchPlan[] fetchPlanArray = new FetchPlan[fetchPlanNames.length];
        for (int i = 0; i < fetchPlanNames.length; i++) {
            fetchPlanArray[i] = fetchPlanRepository.getFetchPlan(entityClass, fetchPlanNames[i]);
        }
        return find(entityClass, id, fetchPlanArray);
    }

    @Override
    public <T extends JmixEntity, K> T getReference(Class<T> clazz, K id) {
        return delegate.getReference(clazz, id);
    }

    @SuppressWarnings("unchecked")
    private <T> TypedQuery<T> createQueryInstance(boolean isNative, @Nullable Class<T> resultClass) {
        JmixQuery query = isNative ?
                (JmixQuery) delegate.createNativeQuery("", resultClass) :
                (JmixQuery) delegate.createQuery("", resultClass);
        return (TypedQuery<T>) beanLocator.getPrototype(Query.NAME, query, resultClass);
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
    public <T extends JmixEntity> TypedQuery<T> createNativeQuery(String sql, Class<T> resultClass) {
        TypedQuery<T> query = createQueryInstance(true, resultClass);
        query.setQueryString(sql);
        return query;
    }

    @Override
    @Deprecated
    public void fetch(JmixEntity entity, FetchPlan fetchPlan) {
    }

    @Nullable
    @Override
    public <T extends JmixEntity, K> T reload(Class<T> entityClass, K id, String... fetchPlanNames) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(id, "id is null");

        T entity = find(entityClass, id, fetchPlanNames);
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends JmixEntity> T reload(T entity, String... fetchPlanNames) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        JmixEntity resultEntity = find(entity.getClass(), EntityValues.getId(entity), fetchPlanNames);
        return (T) resultEntity;
    }

    @Override
    public <T extends JmixEntity> T reloadNN(T entity, String... fetchPlanNames) {
        T reloaded = reload(entity, fetchPlanNames);
        if (reloaded == null)
            throw new EntityNotFoundException("Entity " + entity + " has been deleted");
        return reloaded;
    }

    @Override
    public void flush() {
        log.debug("flush");
        delegate.flush();
    }

    @Override
    public void detach(JmixEntity entity) {
        delegate.detach(entity);
    }

    @Override
    public Connection getConnection() {
        return delegate.unwrap(Connection.class);
    }
}
