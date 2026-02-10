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
import io.jmix.core.annotation.Internal;
import io.jmix.data.StoreAwareLocator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.jspecify.annotations.Nullable;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * INTERNAL.
 * System level wrapper around DBMS-related application properties.
 *
 * <p>For data conversion on the middleware use {@link DbTypeConverter} obtained from
 * {@link DbmsSpecifics} bean.
 */
@Internal
@Component("data_DbmsType")
public class DbmsType {

    private static final Logger log = LoggerFactory.getLogger(DbmsType.class);

    @Autowired
    protected Environment environment;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    protected Map<String, String> types = new ConcurrentHashMap<>(4);

    private static final Map<Predicate<String>, Database> PRODUCT_NAME_MAPPING =
            Map.of(
                    name -> name.contains("h2"), Database.H2,
                    name -> name.contains("hsql"), Database.HSQL,
                    name -> name.contains("postgres"), Database.POSTGRESQL,
                    name -> name.contains("oracle"), Database.ORACLE,
                    name -> name.contains("sql server"), Database.SQL_SERVER,
                    name -> name.contains("mysql") || name.contains("mariadb"), Database.MYSQL
            );

    public String getType() {
        return getType(Stores.MAIN);
    }

    public String getType(String storeName) {
        return types.computeIfAbsent(storeName, s -> {
            DataSource dataSource = storeAwareLocator.getDataSource(s);
            Database database = getDatabase(dataSource);
            if (database != null) {
                return database.name();
            }

            String propName = "jmix.data.dbms-type";
            if (!Stores.isMain(storeName))
                propName = propName + "-" + storeName;

            String id = environment.getProperty(propName);
            if (StringUtils.isBlank(id))
                throw new IllegalStateException("Supported database is not determined from JDBC URL and " + propName + " property is not set");
            return id;
        });
    }

    public String getVersion() {
        return getVersion(Stores.MAIN);
    }

    public String getVersion(String storeName) {
        String propName = "jmix.data.dbms-version";
        if (!Stores.isMain(storeName))
            propName = propName + "-" + storeName;

        return StringUtils.trimToEmpty(environment.getProperty(propName));
    }

    @Nullable
    protected static Database getDatabase(DataSource dataSource) {
        try {
            return JdbcUtils.extractDatabaseMetaData(
                    dataSource,
                    meta -> resolveDatabaseByProductName(meta.getDatabaseProductName())
            );
        } catch (MetaDataAccessException ex) {
            log.warn("Unable to determine JDBC URL from DataSource", ex);
        }
        return null;
    }

    @Nullable
    protected static Database resolveDatabaseByProductName(String productName) {
        String normalized = productName.toLowerCase();

        return PRODUCT_NAME_MAPPING.entrySet().stream()
                .filter(e -> e.getKey().test(normalized))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
