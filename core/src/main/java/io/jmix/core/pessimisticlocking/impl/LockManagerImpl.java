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
import io.jmix.core.cluster.ClusterListener;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.pessimisticlocking.*;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(LockManager.NAME)
public class LockManagerImpl implements LockManager, ClusterListener<LockInfo> {

    protected static class LockKey {

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
    }

    protected static final Logger log = LoggerFactory.getLogger(LockManagerImpl.class);

    protected volatile Map<String, LockDescriptor> config;

    protected Map<LockKey, LockInfo> locks = new ConcurrentHashMap<>();

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
    protected List<LockDescriptorProvider> lockDescriptorProviders = new ArrayList<>();

    protected ClusterManager clusterManager;

    @Autowired
    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.clusterManager.addListener(LockInfo.class, this);
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

        LockInfo lockInfo = locks.get(key);
        if (lockInfo != null) {
            log.debug("Already locked: " + lockInfo);
            return lockInfo;
        }

        LockDescriptor ld = getConfig().get(name);
        if (ld == null) {
            return new LockNotSupported();
        }

        BaseUser user = currentAuthentication.getUser();
        lockInfo = new LockInfo(user.getKey(), user.getUsername(), name, id, timeSource.currentTimestamp());
        locks.put(key, lockInfo);
        log.debug("Locked " + name + "/" + id);

        clusterManager.send(lockInfo);

        return null;
    }

    @Nullable
    @Override
    public LockInfo lock(JmixEntity entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity.getClass());
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        return lock(originalMetaClass.getName(), EntityValues.getId(entity).toString());
    }

    @Override
    public void unlock(String name, String id) {
        LockInfo lockInfo = locks.remove(new LockKey(name, id));
        if (lockInfo != null) {
            log.debug("Unlocked " + name + "/" + id);

            clusterManager.send(new LockInfo(null, null, name, id, timeSource.currentTimestamp()));
        }
    }

    @Override
    public void unlock(JmixEntity entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity.getClass());
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        unlock(originalMetaClass.getName(), EntityValues.getId(entity).toString());
    }

    @Override
    public LockInfo getLockInfo(String name, String id) {
        LockDescriptor ld = getConfig().get(name);
        if (ld == null) {
            return new LockNotSupported();
        }

        return locks.get(new LockKey(name, id));
    }

    @Override
    public List<LockInfo> getCurrentLocks() {
        return new ArrayList<>(locks.values());
    }

    @Override
    public void expireLocks() {
        log.debug("Expiring locks");
        ArrayList<LockKey> list = new ArrayList<>(locks.keySet());
        for (LockKey key : list) {
            LockInfo lockInfo = locks.get(key);
            if (lockInfo != null) {
                LockDescriptor ld = getConfig().get(key.name);
                if (ld == null) {
                    log.debug("Lock " + key.name + "/" + key.id + " configuration not found, remove it");
                    locks.remove(key);
                } else {
                    Integer timeoutSec = ld.getTimeoutSec();
                    if (timeoutSec != null && timeoutSec > 0) {
                        Date since = lockInfo.getSince();
                        if (since.getTime() + timeoutSec * 1000 < timeSource.currentTimestamp().getTime()) {
                            log.debug("Lock " + key.name + "/" + key.id + " expired");
                            locks.remove(key);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reloadConfiguration() {
        config = null;
    }

    @Override
    public void receive(LockInfo message) {
        LockKey key = new LockKey(message.getObjectType(), message.getObjectId());
        if (message.getUserKey() != null) {
            LockInfo lockInfo = locks.get(key);
            if (lockInfo == null || lockInfo.getSince().before(message.getSince())) {
                locks.put(key, message);
            }
        } else {
            locks.remove(key);
        }
    }

    @Override
    public byte[] getState() {
        List<LockInfo> list = new ArrayList<>(locks.values());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
        } catch (IOException e) {
            log.error("Error serializing LockInfo list", e);
            return new byte[0];
        }
        return bos.toByteArray();
    }

    @Override
    public void setState(byte[] state) {
        if (state == null || state.length == 0)
            return;

        List<LockInfo> list;
        ByteArrayInputStream bis = new ByteArrayInputStream(state);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            list = (List<LockInfo>) ois.readObject();
        } catch (Exception e) {
            log.error("Error deserializing LockInfo list", e);
            return;
        }

        for (LockInfo lockInfo : list) {
            receive(lockInfo);
        }
    }
}
