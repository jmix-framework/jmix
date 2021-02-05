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

import io.jmix.hibernate.impl.types.date.JmixDateType;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.AbstractDelegatingMetadataBuilderImplementor;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuilderImplementor;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.springframework.beans.factory.BeanFactory;

import static io.jmix.hibernate.impl.HibernateJmixPersistenceProvider.BEAN_FACTORY_PROPERTY;

public class JmixDelegatingMetadataBuilder extends AbstractDelegatingMetadataBuilderImplementor {
    public JmixDelegatingMetadataBuilder(MetadataSources metadatasources, MetadataBuilderImplementor delegate) {
        super(delegate);

        ConfigurationService cfgService = getBootstrapContext()
                .getServiceRegistry()
                .getService(ConfigurationService.class);

        BeanFactory beanFactory = cfgService.getSetting(BEAN_FACTORY_PROPERTY, BeanFactory.class, null);

        if (beanFactory.getBean(HibernateDataProperties.class).isUseDateInsteadOfTimestamp()) {
            getBootstrapContext().getTypeConfiguration().getTypeResolver().registerTypeOverride(JmixDateType.INSTANCE);
        }
    }

    @Override
    protected MetadataBuilderImplementor getThis() {
        return this;
    }

    @Override
    public BootstrapContext getBootstrapContext() {
        return delegate().getBootstrapContext();
    }
}
