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

import io.jmix.core.EnvironmentUtils;
import io.jmix.hibernate.impl.listeners.HibernateIntegratorProvider;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.Map;

@Component("hibernate_JmixHibernateJpaVendorAdapter")
public class JmixHibernateJpaVendorAdapter extends HibernateJpaVendorAdapter {

    protected final HibernateJpaDialect jpaDialect;

    protected final Environment environment;

    protected final PersistenceProvider persistenceProvider;

    protected final HibernateIntegratorProvider integratorProvider;

    protected BeanFactory beanFactory;

    protected HibernateDataProperties dataProperties;

    @Autowired
    public JmixHibernateJpaVendorAdapter(Environment environment,
                                         HibernateJpaDialect jpaDialect,
                                         HibernateIntegratorProvider integratorProvider,
                                         HibernateDataProperties dataProperties,
                                         BeanFactory beanFactory) {
        this.environment = environment;
        this.jpaDialect = jpaDialect;
        this.beanFactory = beanFactory;
        this.integratorProvider = integratorProvider;
        this.dataProperties = dataProperties;
        this.persistenceProvider = new HibernateJmixPersistenceProvider(beanFactory);

        setGenerateDdl(false);
        setShowSql(true);
    }

    @Override
    public PersistenceProvider getPersistenceProvider() {
        return persistenceProvider;
    }

    @Override
    public Map<String, Object> getJpaPropertyMap() {
        return buildPropertiesMap(super.getJpaPropertyMap());
    }

    @Override
    public Map<String, Object> getJpaPropertyMap(PersistenceUnitInfo pui) {
        return buildPropertiesMap(super.getJpaPropertyMap(pui));
    }

    private Map<String, Object> buildPropertiesMap(Map<String, Object> map) {
        map.put("javax.persistence.validation.mode", "NONE");
        map.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, true);
        map.put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER, integratorProvider);

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("hibernate.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("javax.persistence.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("spring.jpa")) {
                map.put(name, environment.getProperty(name));
            }
        }

        if (dataProperties.isRuntimeEntityEnhancement() && isLoadTimeWeaverAvailable()) {
            map.putIfAbsent(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_DIRTY_TRACKING, true);
            map.putIfAbsent(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_LAZY_INITIALIZATION, true);
            map.putIfAbsent(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_ASSOCIATION_MANAGEMENT, true);
        }

        return map;
    }

    private boolean isLoadTimeWeaverAvailable() {
        LoadTimeWeaver loadTimeWeaver = beanFactory.getBeanProvider(LoadTimeWeaver.class).getIfAvailable();
        return loadTimeWeaver != null;
    }

    @Override
    public HibernateJpaDialect getJpaDialect() {
        return jpaDialect;
    }


}
