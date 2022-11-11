/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.pessimisticlocking.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.pessimisticlocking.*;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("core_LockManager")
public class LockManagerImpl implements LockManager {

    public static class LockKey implements Serializable {
        private static final long serialVersionUID = -79055072974087187L;

        private final String name;
        private final String id;

        private LockKey(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LockKey key = (LockKey) o;

            if (!Objects.equals(id, key.id)) return false;
            return name.equals(key.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", name, id);
        }
    }

    protected static final Logger log = LoggerFactory.getLogger(LockManagerImpl.class);

    protected volatile Map<String, LockDescriptor> config;

    protected Cache locks;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CacheOperations cacheOperations;

    @Autowired
    protected List<LockDescriptorProvider> lockDescriptorProviders = new ArrayList<>();

    @PostConstruct
    protected void init() {
        locks = cacheManager.getCache(LOCKS_CACHE_NAME);
        if (locks == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", LOCKS_CACHE_NAME));
        }
    }

    protected Map<String, LockDescriptor> getConfig() {
        if (this.config == null) {
            Map<String, LockDescriptor> config = new ConcurrentHashMap<>();
            synchronized (this) {
                for (LockDescriptorProvider provider : lockDescriptorProviders) {
                    provider.getLockDescriptors()
                            .forEach(descriptor -> config.put(descriptor.getName(), descriptor));
                }
                if (this.config == null) {
                    this.config = config;
                }
            }
        }
        return config;
    }

    @Override
    public LockInfo lock(String name, String id) {
        LockKey key = new LockKey(name, id);

        LockInfo lockInfo = locks.get(key, LockInfo.class);
        if (lockInfo != null) {
            log.debug("Already locked: {}", lockInfo);
            return lockInfo;
        }

        LockDescriptor ld = getConfig().get(name);
        if (ld == null) {
            return new LockNotSupported();
        }

        UserDetails user = currentAuthentication.getUser();
        lockInfo = new LockInfo(user.getUsername(), name, id, timeSource.currentTimestamp());
        locks.put(key, lockInfo);
        log.debug("Locked {}/{}", name, id);

        return null;
    }

    @Nullable
    @Override
    public LockInfo lock(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity);
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        return lock(originalMetaClass.getName(), EntityValues.getId(entity).toString());
    }

    @Override
    public void unlock(String name, String id) {
        if (locks.evictIfPresent(new LockKey(name, id))) {
            log.debug("Unlocked {}/{}", name, id);
        }
    }

    @Override
    public void unlock(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity);
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        unlock(originalMetaClass.getName(), EntityValues.getId(entity).toString());
    }

    @Override
    public LockInfo getLockInfo(String name, String id) {
        LockDescriptor ld = getConfig().get(name);
        if (ld == null) {
            return new LockNotSupported();
        }

        return locks.get(new LockKey(name, id), LockInfo.class);
    }

    @Override
    public Collection<LockInfo> getCurrentLocks() {
        if (cacheOperations.isIterableCache(locks)) {
            return cacheOperations.getValues(locks);
        } else {
            log.debug("Current locks list operation is unsupported by cache provider");
            return Collections.emptyList();
        }
    }

    @Override
    public void expireLocks() {
        if (cacheOperations.isIterableCache(locks)) {
            Collection<LockKey> keys = cacheOperations.getKeys(locks);
            for (LockKey key : keys) {
                LockInfo lockInfo = locks.get(key, LockInfo.class);
                if (lockInfo != null) {
                    LockDescriptor ld = getConfig().get(key.name);
                    if (ld == null) {
                        log.debug("Lock {}/{} configuration not found, remove it", key.name, key.id);
                        locks.evict(key);
                    } else {
                        Integer timeoutSec = ld.getTimeoutSec();
                        if (timeoutSec != null && timeoutSec > 0) {
                            Date since = lockInfo.getSince();
                            if (since.getTime() + timeoutSec * 1000 < timeSource.currentTimestamp().getTime()) {
                                log.debug("Lock {}/{} expired", key.name, key.id);
                                locks.evict(key);
                            }
                        }
                    }
                }
            }
        } else {
            log.debug("Expiring locks operation is unsupported by cache provider");
        }
    }

    @Override
    public void reloadConfiguration() {
        config = null;
    }
}
