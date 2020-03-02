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

package io.jmix.data.impl;

import com.google.common.collect.Sets;
import io.jmix.core.entity.Entity;
import io.jmix.data.EntityChangeType;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component(PersistenceLifecycleListenerManager.NAME)
public class PersistenceLifecycleListenerManager {
    public static final String NAME = "jmix_PersistenceLifecycleListenerManager";

    protected Set<PersistenceLifecycleListener> listeners;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Register a {@link PersistenceLifecycleListener}.
     *
     * @param listener persistence lifecycle listener
     */
    public void addListener(PersistenceLifecycleListener listener) {
        lock.writeLock().lock();
        try {
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
            }
            listeners.add(listener);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Unregister a {@link PersistenceLifecycleListener}.
     *
     * @param listener persistence lifecycle listener
     */
    public void removeListener(PersistenceLifecycleListener listener) {
        lock.writeLock().lock();
        try {
            if (listeners != null) {
                listeners.remove(listener);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void fireFlush(String storeName) {
        lock.readLock().lock();
        try {
            if (listeners == null) {
                return;
            }
            for (PersistenceLifecycleListener listener : listeners) {
                listener.onFlush(storeName);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void fireEntityChange(Entity entity, EntityChangeType type, EntityAttributeChanges changes) {
        lock.readLock().lock();
        try {
            if (listeners == null) {
                return;
            }
            for (PersistenceLifecycleListener listener : listeners) {
                listener.onEntityChange(entity, type, changes);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
