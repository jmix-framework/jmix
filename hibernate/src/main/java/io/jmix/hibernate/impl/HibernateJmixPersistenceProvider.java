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

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.BeanFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.Map;

public class HibernateJmixPersistenceProvider extends HibernatePersistenceProvider {

    public final static String BEAN_FACTORY_PROPERTY = "jmix.beanFactory";

    private BeanFactory beanFactory;

    public HibernateJmixPersistenceProvider(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        properties.put(BEAN_FACTORY_PROPERTY, beanFactory);
        return super.createContainerEntityManagerFactory(new JmixPersistenceUnitInfo(info, beanFactory), properties);
    }
}
