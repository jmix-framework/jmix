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

import io.jmix.core.Stores;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * INTERNAL.
 * System level wrapper around DBMS-related application properties.
 *
 * <p>For data conversion on the middleware use {@link DbTypeConverter} obtained from
 * {@link io.jmix.data.Persistence} bean.
 * <p>If your client code needs to know what DBMS is currently in use, call {@code getDbmsType()} and
 * {@code getDbmsVersion()} methods of {@link com.haulmont.cuba.core.app.PersistenceManagerService}.
 *
 */
@Component(DbmsType.NAME)
public class DbmsType {

    public static final String NAME = "jmix_DbmsType";

    @Inject
    protected Environment environment;

    public String getType() {
        return getType(Stores.MAIN);
    }

    public String getType(String storeName) {
        String propName = "jmix.dbmsType";
        if (!Stores.isMain(storeName))
            propName = propName + "_" + storeName;

        String id = environment.getProperty(propName);
        if (StringUtils.isBlank(id))
            throw new IllegalStateException("Property " + propName + " is not set");
        return id;
    }

    public String getVersion() {
        return getVersion(Stores.MAIN);
    }

    public String getVersion(String storeName) {
        String propName = "jmix.dbmsVersion";
        if (!Stores.isMain(storeName))
            propName = propName + "_" + storeName;

        return StringUtils.trimToEmpty(environment.getProperty(propName));
    }
}