/*
 * Copyright 2026 Haulmont.
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

package io.jmix.quartz.autoconfigure;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to set into application properties required property '{@code org.quartz.jobStore.driverDelegateClass}' that
 * used by Quartz to understand the particular ‘dialects’ of varies database systems. It allows not to carry about setting that property into certain project.
 * <p>
 * Logic of proper choice for value of that property based on actual value of database connection URL property. This URL is specific for particular DBs,
 * so it allows defining which DB is used by the project and setting up a proper driver delegate class.
 * <p>
 * Note, that for HSQLDB and MySQL we don't need to use specific driver delegate, standard one will be used.
 *
 * @see org.quartz.impl.jdbcjobstore.StdJDBCDelegate
 */
public class QuartzEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(QuartzEnvironmentPostProcessor.class);

    private static final String QUARTZ_PROPERTY_SOURCE = "quartzPropertySource";

    /**
     * Required for any Jmix project property that contains URL for DB connection
     */
    private static final String JMIX_MAIN_DATASOURCE_URL_PROPERTY = "main.datasource.url";

    private static final String DATASOURCE_URL_POSTGRES_STARTS_WITH = "jdbc:postgresql:";
    private static final String DATASOURCE_URL_MS_SQL_STARTS_WITH = "jdbc:sqlserver:";
    private static final String DATASOURCE_URL_ORACLE_STARTS_WITH = "jdbc:oracle:";

    /**
     * Required Quartz property in order to understand the particular ‘dialects’ of varies database systems
     */
    private static final String SPRING_QUARTZ_PROPERTY_JOB_STORE_DRIVER_DELEGATE_CLASS =
            "spring.quartz.properties.org.quartz.jobStore.driverDelegateClass";
    private static final String QUARTZ_POSTGRES_DRIVER_DELEGATE_CLASS = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
    private static final String QUARTZ_MS_SQL_DRIVER_DELEGATE_CLASS = "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
    private static final String QUARTZ_ORACLE_DRIVER_DELEGATE_CLASS = "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";

    private static final String SPRING_QUARTZ_JOB_STORE_TYPE_PROPERTY = "spring.quartz.job-store-type";
    private static final String JDBC_JOB_STORE_TYPE = "jdbc";

    /**
     * With the JDBC job store the TRIGGER_ACCESS lock must be a database row lock ({@code StdRowLockSemaphore}):
     * the default in-JVM semaphore is released after every job store operation while transactional job store
     * writes stay uncommitted, so a saving transaction and the scheduler polling thread deadlock on databases
     * where writers block readers (HSQLDB, SQL Server in lock-based read committed mode).
     */
    private static final String SPRING_QUARTZ_PROPERTY_JOB_STORE_USE_DB_LOCKS =
            "spring.quartz.properties.org.quartz.jobStore.useDBLocks";

    /**
     * The Jmix job store honors 'useDBLocks' on HSQLDB, which Spring's {@code LocalDataSourceJobStore}
     * unconditionally downgrades to the deadlock-prone in-JVM semaphore.
     */
    private static final String SPRING_QUARTZ_PROPERTY_JOB_STORE_CLASS =
            "spring.quartz.properties.org.quartz.jobStore.class";
    private static final String JMIX_JOB_STORE_CLASS = "io.jmix.quartz.impl.JmixLocalDataSourceJobStore";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> quartzProperties = new HashMap<>();

        //if driverDelegateClass is not defined 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate' will be used by default
        String driverDelegateClass = resolveDriverDelegateClass(environment);
        if (!Strings.isNullOrEmpty(driverDelegateClass)) {
            log.debug("Property '{}' will have the value '{}'",
                    SPRING_QUARTZ_PROPERTY_JOB_STORE_DRIVER_DELEGATE_CLASS, driverDelegateClass);
            quartzProperties.put(SPRING_QUARTZ_PROPERTY_JOB_STORE_DRIVER_DELEGATE_CLASS, driverDelegateClass);
        }

        if (isJdbcJobStore(environment)) {
            if (environment.getProperty(SPRING_QUARTZ_PROPERTY_JOB_STORE_USE_DB_LOCKS) == null) {
                log.debug("Property '{}' will have the value 'true'", SPRING_QUARTZ_PROPERTY_JOB_STORE_USE_DB_LOCKS);
                quartzProperties.put(SPRING_QUARTZ_PROPERTY_JOB_STORE_USE_DB_LOCKS, "true");
            }
            if (environment.getProperty(SPRING_QUARTZ_PROPERTY_JOB_STORE_CLASS) == null) {
                log.debug("Property '{}' will have the value '{}'",
                        SPRING_QUARTZ_PROPERTY_JOB_STORE_CLASS, JMIX_JOB_STORE_CLASS);
                quartzProperties.put(SPRING_QUARTZ_PROPERTY_JOB_STORE_CLASS, JMIX_JOB_STORE_CLASS);
            }
        }

        if (quartzProperties.isEmpty()) {
            return;
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(QUARTZ_PROPERTY_SOURCE)) {
            PropertySource<?> propertySource = propertySources.get(QUARTZ_PROPERTY_SOURCE);
            if (propertySource instanceof MapPropertySource) {
                ((MapPropertySource) propertySource).getSource().putAll(quartzProperties);
            }
        } else {
            propertySources.addLast(new MapPropertySource(QUARTZ_PROPERTY_SOURCE, quartzProperties));
        }
    }

    @Nullable
    private String resolveDriverDelegateClass(ConfigurableEnvironment environment) {
        //get value of 'main.datasource.url' application property
        Object datasourceUrlObj = environment.getPropertySources().stream()
                .filter(propertySource -> propertySource.containsProperty(JMIX_MAIN_DATASOURCE_URL_PROPERTY))
                .map(propertySource -> propertySource.getProperty(JMIX_MAIN_DATASOURCE_URL_PROPERTY))
                .findFirst().orElse(null);

        if (datasourceUrlObj == null) {
            log.warn("Property '{}' not found in application properties", JMIX_MAIN_DATASOURCE_URL_PROPERTY);
            return null;
        }

        //define value for driverDelegateClass based on value of main datasource url
        String datasourceUrl = (String) datasourceUrlObj;
        String driverDelegateClass = null;
        if (datasourceUrl.startsWith(DATASOURCE_URL_POSTGRES_STARTS_WITH)) {
            driverDelegateClass = QUARTZ_POSTGRES_DRIVER_DELEGATE_CLASS;
        } else if (datasourceUrl.startsWith(DATASOURCE_URL_MS_SQL_STARTS_WITH)) {
            driverDelegateClass = QUARTZ_MS_SQL_DRIVER_DELEGATE_CLASS;
        } else if (datasourceUrl.startsWith(DATASOURCE_URL_ORACLE_STARTS_WITH)) {
            driverDelegateClass = QUARTZ_ORACLE_DRIVER_DELEGATE_CLASS;
        }
        return driverDelegateClass;
    }

    /**
     * Database locking and the Jmix job store are set up for the JDBC job store only, unless the user
     * configured them explicitly. The module default job store type is JDBC ('module.properties' is not
     * loaded at this point, so an absent property means the default); for the RAM job store the properties
     * are not applicable and would break the scheduler initialization.
     */
    private boolean isJdbcJobStore(ConfigurableEnvironment environment) {
        String jobStoreType = environment.getProperty(SPRING_QUARTZ_JOB_STORE_TYPE_PROPERTY, JDBC_JOB_STORE_TYPE);
        return JDBC_JOB_STORE_TYPE.equalsIgnoreCase(jobStoreType);
    }

}
