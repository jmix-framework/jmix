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

package com.haulmont.cuba.core.sys;

import io.jmix.core.EnvironmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides access to file-based properties.
 */
@Component(AppProperties.NAME)
public class AppProperties {

    public static final String NAME = "jmix_AppProperties";

    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

    private Map<String, Object> properties = new ConcurrentHashMap<>();

    @Autowired
    private Environment environment;

    @PostConstruct
    protected void init() {
        if (!(environment instanceof ConfigurableEnvironment)) {
            log.warn("{} is not a ConfigurableEnvironment, cannot register CUBA mutable property source", environment);
            return;
        }
        MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
        sources.addFirst(new MapPropertySource("CUBA mutable properties", properties));
    }

    public String[] getPropertyNames() {
        return EnvironmentUtils.getPropertyNames(environment).toArray(new String[0]);
    }

    @Nullable
    public String getProperty(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key passed as parameter");
        }

        return environment.getProperty(key);
    }

    /**
     * Set property value. The new value will be accessible at the runtime through {@link Environment}.
     * @param key       property key
     * @param value     property value. If null, the property will be removed.
     */
    public void setProperty(String key, @Nullable String value) {
        if (value == null)
            properties.remove(key);
        else
            properties.put(key, value);
    }
}