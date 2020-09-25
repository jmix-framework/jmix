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

package io.jmix.core.impl;

import io.jmix.core.JmixOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures default logging levels defined in properties with {@code jmix.logging.level} prefixes
 * (usually in {@code module.properties} files).
 * <p>
 * If an application defines a level for the same logger, the default level is ignored.
 */
@Component("core_LoggingConfigurer")
public class LoggingConfigurer {

    public static final String LOGGING_LEVEL_PROP_PREFIX = "jmix.logging.level.";

    private static final Logger log = LoggerFactory.getLogger(LoggingConfigurer.class);

    @Autowired
    private Environment environment;

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
    public void initDefaultLogLevels(ContextRefreshedEvent event) {
        LoggingSystem loggingSystem = LoggingSystem.get(this.getClass().getClassLoader());

        for (String loggerName : getDefinedLoggers()) {
            LoggerConfiguration loggerConfiguration = null;
            try {
                loggerConfiguration = loggingSystem.getLoggerConfiguration(loggerName);
            } catch (Exception e) {
                log.debug("Cannot get logger configuration for {}: {}", loggerName, e.toString());
            }
            if (loggerConfiguration == null) {
                String value = environment.getProperty(LOGGING_LEVEL_PROP_PREFIX + loggerName);
                loggingSystem.setLogLevel(loggerName, LogLevel.valueOf(value));
            }
        }
    }

    private List<String> getDefinedLoggers() {
        List<String> list = new ArrayList<>();
        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                for (String propertyName : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                    if (propertyName.startsWith(LOGGING_LEVEL_PROP_PREFIX)) {
                        list.add(propertyName.substring(LOGGING_LEVEL_PROP_PREFIX.length()));
                    }
                }
            }
        }
        return list;
    }
}
