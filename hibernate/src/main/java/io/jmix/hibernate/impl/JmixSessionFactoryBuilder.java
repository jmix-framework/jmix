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

import io.jmix.hibernate.impl.metadata.CompositeSessionFactoryEnhancer;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.AbstractDelegatingSessionFactoryBuilderImplementor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.bytecode.internal.SessionFactoryObserverForBytecodeEnhancer;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

import static io.jmix.hibernate.impl.HibernateJmixPersistenceProvider.BEAN_FACTORY_PROPERTY;

public class JmixSessionFactoryBuilder extends AbstractDelegatingSessionFactoryBuilderImplementor<JmixSessionFactoryBuilder> {

    private BeanFactory beanFactory;

    private final MetadataImplementor metadata;

    public JmixSessionFactoryBuilder(
            MetadataImplementor metadata,
            SessionFactoryBuilderImplementor delegate) {
        super(delegate);
        Map<String, FilterDefinition> filterDefinitions = metadata.getFilterDefinitions();
        if (filterDefinitions.get(SoftDeletionFilterDefinition.NAME) == null) {
            filterDefinitions.put(SoftDeletionFilterDefinition.NAME, new SoftDeletionFilterDefinition());
        }
        this.metadata = metadata;

        // initialize customSetting, maybe based on config settings...
        ConfigurationService cfgService = metadata.getMetadataBuildingOptions()
                .getServiceRegistry()
                .getService(ConfigurationService.class);

        this.beanFactory = cfgService.getSetting(BEAN_FACTORY_PROPERTY, BeanFactory.class, null);
    }

    public JmixSessionFactoryBuilder withBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        return this;
    }

    @Override
    protected JmixSessionFactoryBuilder getThis() {
        return this;
    }

    @Override
    public SessionFactory build() {
        JmixSessionFactoryOptions options = new JmixSessionFactoryOptions(
                buildSessionFactoryOptions(),
                beanFactory,
                beanFactory.getBean(JmixHibernateInterceptor.class, metadata)
        );
        metadata.validate();
        final StandardServiceRegistry serviceRegistry = metadata.getMetadataBuildingOptions().getServiceRegistry();

        BytecodeProvider bytecodeProvider = serviceRegistry.getService(BytecodeProvider.class);
        addSessionFactoryObservers(new SessionFactoryObserverForBytecodeEnhancer(bytecodeProvider));

        SessionFactoryImplementor sessionFactory = new JmixHibernateSessionFactory(new SessionFactoryImpl(metadata, options, HQLQueryPlan::new), beanFactory);
        CompositeSessionFactoryEnhancer sessionFactoryEnhancer = beanFactory.getBean(CompositeSessionFactoryEnhancer.class);
        sessionFactoryEnhancer.enhance(sessionFactory);

        return sessionFactory;

    }

}

