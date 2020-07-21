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

import io.jmix.core.EnvironmentUtils;
import io.jmix.data.persistence.JmixIsNullExpressionOperator;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.persistence.spi.PersistenceProvider;
import java.util.Map;

@Component(JmixEclipseLinkJpaVendorAdapter.NAME)
public class JmixEclipseLinkJpaVendorAdapter extends EclipseLinkJpaVendorAdapter {

    public static final String NAME = "data_JmixEclipseLinkJpaVendorAdapter";

    protected final EclipseLinkJpaDialect jpaDialect;

    protected final Environment environment;

    protected final PersistenceProvider persistenceProvider;

    @Autowired
    public JmixEclipseLinkJpaVendorAdapter(Environment environment,
                                           JmixEclipseLinkJpaDialect jpaDialect,
                                           BeanFactory beanFactory) {
        this.environment = environment;
        this.jpaDialect = jpaDialect;
        this.persistenceProvider = new JmixPersistenceProvider(beanFactory);

        ExpressionOperator.addOperator(new JmixIsNullExpressionOperator());
        setGenerateDdl(false);
        setShowSql(true);
    }

    @Override
    public PersistenceProvider getPersistenceProvider() {
        return persistenceProvider;
    }

    @Override
    public Map<String, Object> getJpaPropertyMap() {
        Map<String, Object> map = super.getJpaPropertyMap();

        map.put("eclipselink.session-event-listener", EclipseLinkSessionEventListener.class.getName());
        map.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        map.put("eclipselink.cache.coordination.protocol", "io.jmix.data.impl.entitycache.EntityCacheTransportManager");
        map.put("eclipselink.cache.coordination.propagate-asynchronously", "false");
        map.put("eclipselink.weaving", "static");
        map.put("eclipselink.flush-clear.cache", "Merge");
        map.put("eclipselink.cache.shared.default", "false");

        map.put("javax.persistence.validation.mode", "NONE");

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("eclipselink.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("javax.persistence.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        return map;
    }

    @Override
    public EclipseLinkJpaDialect getJpaDialect() {
        return jpaDialect;
    }
}
