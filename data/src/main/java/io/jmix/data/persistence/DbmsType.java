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
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    protected static final Map<DatabaseDriver, Database> driverToDbMap = new EnumMap<>(DatabaseDriver.class);;

    static {
        driverToDbMap.put(DatabaseDriver.HSQLDB, Database.HSQL);
        driverToDbMap.put(DatabaseDriver.MYSQL, Database.MYSQL);
        driverToDbMap.put(DatabaseDriver.MARIADB, Database.MYSQL);
        driverToDbMap.put(DatabaseDriver.ORACLE, Database.ORACLE);
        driverToDbMap.put(DatabaseDriver.POSTGRESQL, Database.POSTGRESQL);
        driverToDbMap.put(DatabaseDriver.SQLSERVER, Database.SQL_SERVER);
    }

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
            propName = propName + "_" + storeName;

        return StringUtils.trimToEmpty(environment.getProperty(propName));
    }

    @Nullable
    protected static Database getDatabase(DataSource dataSource) {
        try {
            String url = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getURL);
            DatabaseDriver driver = DatabaseDriver.fromJdbcUrl(url);
            return driverToDbMap.get(driver);
        } catch (MetaDataAccessException ex) {
            log.warn("Unable to determine JDBC URL from DataSource", ex);
        }
        return null;
    }
}
