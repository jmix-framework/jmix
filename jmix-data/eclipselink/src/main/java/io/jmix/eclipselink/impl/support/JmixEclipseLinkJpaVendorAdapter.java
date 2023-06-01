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
package io.jmix.eclipselink.impl.support;

import io.jmix.core.EnvironmentUtils;
import io.jmix.core.MetadataTools;
import io.jmix.eclipselink.impl.JmixPersistenceProvider;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.validation.ValidatorFactory;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.coordination.CommandProcessor;
import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("eclipselink_JmixEclipseLinkJpaVendorAdapter")
public class JmixEclipseLinkJpaVendorAdapter extends EclipseLinkJpaVendorAdapter {

    protected final EclipseLinkJpaDialect jpaDialect;

    protected final Environment environment;

    protected final PersistenceProvider persistenceProvider;

    protected final JmixEclipseLinkSessionEventListener sessionEventListener;

    protected final ObjectProvider<JmixEclipseLinkTransportManager> transportManagerProvider;

    protected final ValidatorFactory validatorFactory;

    @Autowired
    public JmixEclipseLinkJpaVendorAdapter(Environment environment,
                                           JmixEclipseLinkJpaDialect jpaDialect,
                                           JmixEclipseLinkSessionEventListener sessionEventListener,
                                           ObjectProvider<JmixEclipseLinkTransportManager> transportManagerProvider,
                                           ListableBeanFactory beanFactory,
                                           MetadataTools metadataTools,
                                           ValidatorFactory validatorFactory) {
        this.environment = environment;
        this.jpaDialect = jpaDialect;
        this.persistenceProvider = new JmixPersistenceProvider(beanFactory, metadataTools);
        this.sessionEventListener = sessionEventListener;
        this.transportManagerProvider = transportManagerProvider;
        this.validatorFactory = validatorFactory;

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

        map.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        map.put("eclipselink.weaving", "static");
        map.put("eclipselink.flush-clear.cache", "Merge");
        map.put("eclipselink.cache.shared.default", "false");

        map.put("jakarta.persistence.validation.mode", "NONE");
        map.put("jakarta.persistence.validation.factory", validatorFactory);

        map.put("eclipselink.session.customizer", new JmixEclipseLinkSessionCustomizer());
        map.put("eclipselink.application-id", Integer.toString(System.identityHashCode(this)));

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("eclipselink.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("jakarta.persistence.")) {
                map.put(name, environment.getProperty(name));
            }
        }

        return map;
    }

    @Override
    public EclipseLinkJpaDialect getJpaDialect() {
        return jpaDialect;
    }

    protected class JmixEclipseLinkSessionCustomizer implements SessionCustomizer {
        @Override
        public void customize(Session session) throws Exception {
            session.getEventManager().addListener(sessionEventListener);
            if (session instanceof CommandProcessor) {
                RemoteCommandManager rcm = new RemoteCommandManager((CommandProcessor) session);
                rcm.setTransportManager(transportManagerProvider.getObject(rcm));
                rcm.setShouldPropagateAsynchronously(false);
                ((CommandProcessor) session).setCommandManager(rcm);
                if (session instanceof AbstractSession) {
                    ((AbstractSession) session).setShouldPropagateChanges(true);
                }
            }
        }
    }
}
