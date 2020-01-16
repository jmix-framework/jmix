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

import io.jmix.data.SequenceSupport;
import io.jmix.core.Stores;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Factory for obtaining implementations of DBMS-specific objects, particularly {@link DbmsFeatures},
 * {@link SequenceSupport} and {@link DbTypeConverter}.
 *
 */
@Component(DbmsSpecifics.NAME)
public class DbmsSpecifics {

    public static final String NAME = "jmix_DbmsSpecifics";

    @Inject
    protected DbmsType dbmsType;

    @Inject
    protected ApplicationContext applicationContext;

    public DbmsFeatures getDbmsFeatures() {
        return get(DbmsFeatures.class, Stores.MAIN);
    }

    public DbmsFeatures getDbmsFeatures(String storeName) {
        return get(DbmsFeatures.class, storeName);
    }

    public SequenceSupport getSequenceSupport() {
        return get(SequenceSupport.class, Stores.MAIN);
    }

    public SequenceSupport getSequenceSupport(String storeName) {
        return get(SequenceSupport.class, storeName);
    }

    public DbTypeConverter getDbTypeConverter() {
        return get(DbTypeConverter.class, Stores.MAIN);
    }

    public DbTypeConverter getDbTypeConverter(String storeName) {
        return get(DbTypeConverter.class, storeName);
    }

    public <T> T get(Class<T> intf) {
        return get(intf, Stores.MAIN);
    }

    public <T> T get(Class<T> intf, String storeName) {
        return get(intf, dbmsType.getType(storeName), StringUtils.trimToEmpty(dbmsType.getVersion(storeName)));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> intf, String dbmsType, String dbmsVersion) {
        String name = dbmsType + StringUtils.capitalize(dbmsVersion) + intf.getSimpleName();
        return (T) applicationContext.getBean(name);
    }
}
