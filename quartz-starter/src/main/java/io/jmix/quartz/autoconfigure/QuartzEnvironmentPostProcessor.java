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

import java.util.HashMap;
import java.util.Map;

/**
 * Used to set into application properties required property '{@code org.quartz.jobStore.driverDelegateClass}' that
 * used by Quartz to understand the particular ‘dialects’ of varies database systems. It allows not to carry about setting that property into certain project.
 * <p>
 * Logic of proper choice for value of that property based on actual value of database connection URL property. This URL is specific for certain DBs,
 * so it allows to define which DB is used by the project and set up proper driver delegate class.
 * <p>
 * Note, that for HSQLDB and MySQL we don't need to use specific driver delegate, standard one will be used.
 *
 * @see org.quartz.impl.jdbcjobstore.StdJDBCDelegate
 */
public class QuartzEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(QuartzEnvironmentPostProcessor.class);

    private static final String QUARTZ_PROPERTY_SOURCE = "quartzPropertySource";

    /**
     * Required Jmix property that contains URL for DB connection
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

    private static final String DRIVER_DELEGATE_CLASS_FOR_POSTGRES =
            "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";

    private static final String DRIVER_DELEGATE_CLASS_FOR_MS_SQL =
            "org.quartz.impl.jdbcjobstore.MSSQLDelegate";

    private static final String DRIVER_DELEGATE_CLASS_FOR_ORACLE =
            "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Object datasourceUrlObj = propertySources.stream()
                .filter(propertySource -> propertySource.containsProperty(JMIX_MAIN_DATASOURCE_URL_PROPERTY))
                .map(propertySource -> propertySource.getProperty(JMIX_MAIN_DATASOURCE_URL_PROPERTY))
                .findFirst().orElse(null);

        if (datasourceUrlObj == null) {
            log.warn("Property '{}' not found in application properties", JMIX_MAIN_DATASOURCE_URL_PROPERTY);
            return;
        }

        String datasourceUrl = (String) datasourceUrlObj;
        String driverDelegateClass = null;
        if (datasourceUrl.startsWith(DATASOURCE_URL_POSTGRES_STARTS_WITH)) {
            driverDelegateClass = DRIVER_DELEGATE_CLASS_FOR_POSTGRES;
        } else if (datasourceUrl.startsWith(DATASOURCE_URL_MS_SQL_STARTS_WITH)) {
            driverDelegateClass = DRIVER_DELEGATE_CLASS_FOR_MS_SQL;
        } else if (datasourceUrl.startsWith(DATASOURCE_URL_ORACLE_STARTS_WITH)) {
            driverDelegateClass = DRIVER_DELEGATE_CLASS_FOR_ORACLE;
        }

        if (!Strings.isNullOrEmpty(driverDelegateClass)) {
            log.debug("Property '{}' will have the value '{}'",
                    SPRING_QUARTZ_PROPERTY_JOB_STORE_DRIVER_DELEGATE_CLASS, driverDelegateClass);

            Map<String, Object> driverDelegatePropMap = new HashMap<>();
            driverDelegatePropMap.put(SPRING_QUARTZ_PROPERTY_JOB_STORE_DRIVER_DELEGATE_CLASS, driverDelegateClass);
            if (propertySources.contains(QUARTZ_PROPERTY_SOURCE)) {
                PropertySource<?> propertySource = propertySources.get(QUARTZ_PROPERTY_SOURCE);
                if (propertySource instanceof MapPropertySource) {
                    ((MapPropertySource) propertySource).getSource().putAll(driverDelegatePropMap);
                }
            } else {
                propertySources.addLast(new MapPropertySource(QUARTZ_PROPERTY_SOURCE, driverDelegatePropMap));
            }
        }
    }

}
