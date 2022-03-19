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

package com.haulmont.cuba.core.sys.config;

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.config.ConfigPersister;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.app.ConfigStorageAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class ConfigPersisterImpl implements ConfigPersister {

    protected static final Logger log = LoggerFactory.getLogger(ConfigPersisterImpl.class);

    protected ApplicationContext applicationContext;

    public ConfigPersisterImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getProperty(SourceType sourceType, String name) {
        log.trace("Getting property '{}', source={}", name, sourceType.name());
        String value;
        switch (sourceType) {
            case SYSTEM:
                value = System.getProperty(name);
                break;
            case APP:
                value = applicationContext.getEnvironment().getProperty(name);
                break;
            case DATABASE:
                value = applicationContext.getEnvironment().getProperty(name);
                if (StringUtils.isEmpty(value)) {
                    value = getConfigStorage().getDbProperty(name);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported config source type: " + sourceType);
        }
        return value;
    }

    @Override
    public void setProperty(SourceType sourceType, String name, String value) {
        log.debug("Setting property '{}' to '{}', source={}", name, value, sourceType.name());
        switch (sourceType) {
            case SYSTEM:
                System.setProperty(name, value);
                break;
            case APP:
                AppContext.setProperty(name, value);
                break;
            case DATABASE:
                getConfigStorage().setDbProperty(name, value);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported config source type: " + sourceType);
        }
    }

    protected ConfigStorageAPI getConfigStorage() {
        return (ConfigStorageAPI) applicationContext.getBean(ConfigStorageAPI.NAME);
    }
}