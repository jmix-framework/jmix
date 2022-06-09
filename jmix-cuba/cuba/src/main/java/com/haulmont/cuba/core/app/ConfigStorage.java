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
package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.entity.Config;
import io.jmix.core.CacheOperations;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Supports configuration parameters framework functionality.
 */
@Component(ConfigStorageAPI.NAME)
public class ConfigStorage implements ConfigStorageAPI {

    @Autowired
    protected Metadata metadata;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CacheOperations cacheOperations;

    protected TransactionTemplate transaction;

    protected Cache cache;

    private static final Logger log = LoggerFactory.getLogger(ConfigStorageAPI.class);

    public static final String CONFIG_STORAGE_CACHE_NAME = "cuba-config-storage-cache";

    @PostConstruct
    protected void init() {
        cache = cacheManager.getCache(CONFIG_STORAGE_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", CONFIG_STORAGE_CACHE_NAME));
        }
    }

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
    }

    @Override
    public void clearCache() {
        cache.invalidate();
    }

    @Override
    public Map<String, String> getDbProperties() {
        return loadDbProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
    }

    @Override
    public String getDbProperty(String name) {
        ValueHolder valueHolder = cacheOperations.get(cache, name, () -> loadDbProperty(name));
        return valueHolder == null ? null : valueHolder.getValue();
    }

    protected ValueHolder loadDbProperty(String name) {
        log.debug("Loading DB-stored app property {}", name);
        return transaction.execute(transactionStatus -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                return new ValueHolder(jdbcTemplate.queryForObject(
                        "select VALUE_ from SYS_CONFIG where NAME = ?", String.class, name));
            } catch (EmptyResultDataAccessException e) {
                return new ValueHolder(null);
            } catch (DataAccessException e) {
                throw new RuntimeException("Error loading DB-stored app properties cache", e);
            }
        });
    }

    protected Map<String, Object> loadDbProperties() {
        log.debug("Loading DB-stored app properties");
        return transaction.execute(transactionStatus -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                return jdbcTemplate.queryForMap(
                        "select NAME, VALUE_ from SYS_CONFIG where NAME = ?");
            } catch (DataAccessException e) {
                throw new RuntimeException("Error loading DB-stored app properties cache", e);
            }
        });
    }

    @Override
    public void setDbProperty(String name, String value) {
        Preconditions.checkNotNullArgument(name, "name is null");

        transaction.executeWithoutResult(transactionStatus -> {
            Config instance = getConfigInstance(name);
            if (value != null) {
                if (instance == null) {
                    instance = metadata.create(Config.class);
                    instance.setName(name.trim());
                    instance.setValue(value.trim());
                    entityManager.persist(instance);
                } else {
                    instance.setValue(value);
                }
            } else {
                if (instance != null)
                    entityManager.remove(instance);
            }

        });
        clearCache();
    }

    @Nullable
    private Config getConfigInstance(String name) {
        TypedQuery<Config> query = entityManager.createQuery("select c from sys$Config c where c.name = ?1", Config.class);
        query.setParameter(1, name);
        List<Config> list = query.getResultList();
        if (list.isEmpty())
            return null;
        else
            return list.get(0);
    }

    protected static class ValueHolder implements Serializable {
        private static final long serialVersionUID = 5115145223092395387L;

        private final String value;

        public ValueHolder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
