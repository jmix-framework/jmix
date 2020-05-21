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

import io.jmix.core.Metadata;
import io.jmix.core.cluster.ClusterListenerAdapter;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.common.util.Preconditions;
import com.haulmont.cuba.core.entity.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Supports configuration parameters framework functionality.
 */
@Component(ConfigStorageAPI.NAME)
public class ConfigStorage implements ConfigStorageAPI {

    @Autowired
    private Metadata metadata;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    protected TransactionTemplate transaction;

    protected ClusterManager clusterManager;

    protected Map<String, String> cache;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    protected Lock readLock = lock.readLock();
    protected Lock writeLock = lock.writeLock();

    private static final Logger log = LoggerFactory.getLogger(ConfigStorageAPI.class);

    private static class InvalidateCacheMsg implements Serializable {
        private static final long serialVersionUID = -3116358584797500962L;
    }

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
    }

    @Autowired
    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        clusterManager.addListener(InvalidateCacheMsg.class, new ClusterListenerAdapter<InvalidateCacheMsg>() {
            @Override
            public void receive(InvalidateCacheMsg message) {
                internalClearCache();
            }
        });
    }

    @Override
    public void clearCache() {
        internalClearCache();
        clusterManager.send(new InvalidateCacheMsg());
    }

    private void internalClearCache() {
        writeLock.lock();
        try {
            cache = null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Map<String, String> getDbProperties() {
        readLock.lock();
        try {
            loadCache();
            return new HashMap<>(cache);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getDbProperty(String name) {
        readLock.lock();
        try {
            loadCache();
            return cache.get(name);
        } finally {
            readLock.unlock();
        }
    }

    protected void loadCache() {
        if (cache == null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (cache == null) {
                    log.info("Loading DB-stored app properties cache");
                    transaction.executeWithoutResult(transactionStatus -> {
                        // Don't use transactions here because of loop possibility from EntityLog
                        try {
                            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                            cache = jdbcTemplate.query("select NAME, VALUE_ from SYS_CONFIG", new Object[]{},
                                    (ResultSetExtractor<Map<String, String>>) rs -> {
                                        HashMap<String, String> map = new HashMap<>();
                                        while (rs.next()) {
                                            map.put(rs.getString(1), rs.getString(2));
                                        }
                                        return map;
                                    }
                            );
                        } catch (DataAccessException e) {
                            throw new RuntimeException("Error loading DB-stored app properties cache", e);
                        }
                    });
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
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
}
