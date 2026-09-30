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

package io.jmix.core.impl;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Checks on startup that the {@code jmix.core.temp-dir} directory can be written to.
 * <p>
 * If the directory is not writable and the property has the default value from the core module, the property is
 * replaced with a directory under {@code java.io.tmpdir}. If the property is set explicitly, it is kept as is and a
 * warning is logged.
 */
public class TempDirProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

    private static final Logger log = LoggerFactory.getLogger(TempDirProcessor.class);

    private static final String TEMP_DIR_PROPERTY = "jmix.core.temp-dir";

    // The name of the property source that holds the core module's module.properties
    private static final String CORE_PROPERTY_SOURCE = "io.jmix.core";

    private static final String FALLBACK_PROPERTY_SOURCE = "jmix.core.tempDirFallback";

    private ConfigurableEnvironment environment;

    @NullMarked
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String tempDir = Binder.get(environment).bind(TEMP_DIR_PROPERTY, String.class).orElse(null);
        if (tempDir == null || isWritable(tempDir)) {
            return;
        }

        if (!isDefaultValue()) {
            log.warn("Temporary directory {} is not writable. Downloads and exports of large files and file uploads " +
                    "will fail. Set the {} property to a writable directory", tempDir, TEMP_DIR_PROPERTY);
            return;
        }

        String fallbackDir;
        try {
            fallbackDir = new ApplicationTemp().getDir("jmix").getAbsolutePath();
        } catch (RuntimeException e) {
            log.warn("Temporary directory {} is not writable, and a directory under java.io.tmpdir cannot be " +
                    "created: {}. Set the {} property to a writable directory", tempDir, e, TEMP_DIR_PROPERTY);
            return;
        }

        environment.getPropertySources().addFirst(
                new MapPropertySource(FALLBACK_PROPERTY_SOURCE, Map.of(TEMP_DIR_PROPERTY, fallbackDir)));

        log.warn("Temporary directory {} is not writable, using {} instead. To use another directory, set the {} " +
                "property or the JMIX_CORE_TEMPDIR environment variable", tempDir, fallbackDir, TEMP_DIR_PROPERTY);
    }

    @NullMarked
    protected boolean isWritable(String dir) {
        try {
            Path path = Path.of(dir);
            Files.createDirectories(path);
            Path probeFile = Files.createTempFile(path, "probe", null);
            Files.delete(probeFile);
            return true;
        } catch (IOException | InvalidPathException | SecurityException e) {
            log.debug("Temporary directory {} is not writable", dir, e);
            return false;
        }
    }

    /**
     * Returns true if the property value comes from the core module's {@code module.properties}, that is,
     * the application does not set it.
     */
    @NullMarked
    protected boolean isDefaultValue() {
        ConfigurationPropertyName name = ConfigurationPropertyName.of(TEMP_DIR_PROPERTY);
        for (ConfigurationPropertySource source : ConfigurationPropertySources.from(environment.getPropertySources())) {
            if (source.getConfigurationProperty(name) != null) {
                return source.getUnderlyingSource() instanceof PropertySource<?> propertySource
                        && CORE_PROPERTY_SOURCE.equals(propertySource.getName());
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // Before PropertySourcesPlaceholderConfigurer and other post-processors that can read the property
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @NullMarked
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
