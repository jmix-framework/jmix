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

package io.jmix.hibernate.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.jndi.spi.JndiService;
import org.hibernate.engine.spi.SessionFactoryDelegatingImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionFactoryRegistry;
import org.springframework.beans.factory.BeanFactory;

import javax.persistence.EntityManager;
import javax.persistence.SynchronizationType;
import java.util.Map;

public class JmixHibernateSessionFactory extends SessionFactoryDelegatingImpl {

    private BeanFactory beanFactory;

    public JmixHibernateSessionFactory(SessionFactoryImplementor delegate, BeanFactory beanFactory) {
        super(delegate);
        this.beanFactory = beanFactory;

        SessionFactoryRegistry.INSTANCE.addSessionFactory(
                delegate.getUuid(),
                null,
                delegate.getSessionFactoryOptions().isSessionFactoryNameAlsoJndiName(),
                this,
                delegate.getServiceRegistry().getService(JndiService.class)
        );
    }

    @Override
    public Session openSession() throws HibernateException {
        return createJmixEntityManager(super.openSession());
    }

    @Override
    public Session openTemporarySession() throws HibernateException {
        return createJmixEntityManager(super.openTemporarySession());
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return createJmixEntityManager(super.getCurrentSession());
    }

    private Session createJmixEntityManager(EntityManager delegate) {
        if (delegate instanceof HibernateDelegateJmixEntityManager) {
            return (Session) delegate;
        } else {
            return new HibernateDelegateJmixEntityManager((SessionImplementor) delegate, beanFactory);
        }
    }

    @Override
    public EntityManager createEntityManager() {
        return createJmixEntityManager(delegate().createEntityManager(delegate().getProperties()));
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return createJmixEntityManager(delegate().createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createJmixEntityManager(delegate().createEntityManager(synchronizationType));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return createJmixEntityManager(delegate().createEntityManager(synchronizationType, map));
    }
}
