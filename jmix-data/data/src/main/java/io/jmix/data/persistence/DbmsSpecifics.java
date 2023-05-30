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

package io.jmix.data.persistence;

import io.jmix.core.JmixModulesAwareBeanSelector;
import io.jmix.core.Stores;
import io.jmix.core.common.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Factory for obtaining implementations of DBMS-specific objects, particularly {@link DbmsFeatures},
 * {@link SequenceSupport} and {@link DbTypeConverter}.
 */
@Component("data_DbmsSpecifics")
public class DbmsSpecifics {

    @Autowired
    protected DbmsType dbmsType;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected JmixModulesAwareBeanSelector beanSelector;

    protected Map<String, DbmsFeatures> dbmsFeaturesByStore = new ConcurrentHashMap<>(4);
    protected Map<String, SequenceSupport> sequenceSupportByStore = new ConcurrentHashMap<>(4);
    protected Map<String, DbTypeConverter> dbTypeConverterByStore = new ConcurrentHashMap<>(4);

    public DbmsFeatures getDbmsFeatures() {
        return get(DbmsFeatures.class, Stores.MAIN);
    }

    public DbmsFeatures getDbmsFeatures(String storeName) {
        return dbmsFeaturesByStore.computeIfAbsent(storeName, s -> get(DbmsFeatures.class, s));
    }

    public SequenceSupport getSequenceSupport() {
        return get(SequenceSupport.class, Stores.MAIN);
    }

    public SequenceSupport getSequenceSupport(String storeName) {
        return sequenceSupportByStore.computeIfAbsent(storeName, s -> get(SequenceSupport.class, s));
    }

    public DbTypeConverter getDbTypeConverter() {
        return get(DbTypeConverter.class, Stores.MAIN);
    }

    public DbTypeConverter getDbTypeConverter(String storeName) {
        return dbTypeConverterByStore.computeIfAbsent(storeName, s -> get(DbTypeConverter.class, s));
    }

    public <T> T get(Class<T> intf) {
        return get(intf, Stores.MAIN);
    }

    public <T> T get(Class<T> intf, String storeName) {
        return get(intf, dbmsType.getType(storeName), StringUtils.trimToEmpty(dbmsType.getVersion(storeName)));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> intf, String dbmsType, String dbmsVersion) {
        T bean = null;
        String typeVersionId = getTypeVersionId(dbmsType, dbmsVersion);
        String typeId = getTypeId(dbmsType);
        if (DbmsFeatures.class.isAssignableFrom(intf)) {
            bean = (T) findDbmsFeatures(typeVersionId);
            if (bean == null) {
                bean = (T) findDbmsFeatures(typeId);
            }
        } else if (DbTypeConverter.class.isAssignableFrom(intf)) {
            bean = (T) findDbTypeConverter(typeVersionId);
            if (bean == null) {
                bean = (T) findDbTypeConverter(typeId);
            }
        } else {
            try {
                String name = typeVersionId + intf.getSimpleName();
                bean = (T) applicationContext.getBean(name);
            } catch (NoSuchBeanDefinitionException e) {
                String name = typeId + intf.getSimpleName();
                bean = (T) applicationContext.getBean(name);
            }
        }
        return bean;
    }

    protected String getTypeVersionId(String dbmsType, String dbmsVersion) {
        return StringHelper.underscoreToCamelCase(dbmsType) + StringUtils.capitalize(dbmsVersion);
    }

    protected String getTypeId(String dbmsType) {
        return StringHelper.underscoreToCamelCase(dbmsType);
    }

    @Nullable
    protected DbmsFeatures findDbmsFeatures(String typeVersion) {
        Collection<DbmsFeatures> beans = applicationContext.getBeansOfType(DbmsFeatures.class).values().stream()
                .filter(features -> features.getTypeAndVersion().equals(typeVersion))
                .collect(Collectors.toList());
        return beanSelector.selectFrom(beans);
    }

    @Nullable
    protected DbTypeConverter findDbTypeConverter(String typeVersion) {
        Collection<DbTypeConverter> beans = applicationContext.getBeansOfType(DbTypeConverter.class).values().stream()
                .filter(converter -> converter.getTypeAndVersion().equals(typeVersion))
                .collect(Collectors.toList());
        return beanSelector.selectFrom(beans);
    }
}
