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

package io.jmix.eclipselink.impl;

import io.jmix.core.MetadataTools;
import io.jmix.eclipselink.impl.support.JmixIsNullExpressionOperator;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Map;

public class JmixEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory delegate;

    private ListableBeanFactory beanFactory;

    private MetadataTools metadataTools;

    public JmixEntityManagerFactory(EntityManagerFactory delegate, ListableBeanFactory beanFactory, MetadataTools metadataTools) {
        this.delegate = delegate;
        this.beanFactory = beanFactory;
        this.metadataTools = metadataTools;
    }

    private EntityManager createJmixEntityManager(EntityManager delegate) {
        return new JmixEntityManager(delegate, beanFactory);
    }

    @Override
    public EntityManager createEntityManager() {
        EntityManager entityManager = createJmixEntityManager(delegate.createEntityManager());

        Map<Integer, ExpressionOperator> operators = ((EntityManagerImpl) entityManager.getDelegate()).getSession()
                .getPlatform().getPlatformOperators();

        if (!(operators.get(ExpressionOperator.IsNull) instanceof JmixIsNullExpressionOperator)) {
            operators.put(ExpressionOperator.IsNull, new JmixIsNullExpressionOperator(metadataTools));
        }

        return entityManager;
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return createJmixEntityManager(delegate.createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createJmixEntityManager(delegate.createEntityManager(synchronizationType));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return createJmixEntityManager(delegate.createEntityManager(synchronizationType, map));
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        delegate.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        delegate.addNamedEntityGraph(graphName, entityGraph);
    }
}
