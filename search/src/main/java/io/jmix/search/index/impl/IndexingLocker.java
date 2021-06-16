/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index.impl;

import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component("search_IndexingLocker")
public class IndexingLocker {

    protected final ReentrantLock indexingQueueProcessingLock = new ReentrantLock();
    protected final ReentrantLock reindexingLock = new ReentrantLock();
    protected final Map<String, ReentrantLock> enqueueAllLocks;

    protected final IndexConfigurationManager indexConfigurationManager;

    @Autowired
    public IndexingLocker(IndexConfigurationManager indexConfigurationManager) {
        Map<String, ReentrantLock> tmpEnqueueAllLocks = new ConcurrentHashMap<>();
        indexConfigurationManager.getAllIndexedEntities().forEach(entity -> {
            tmpEnqueueAllLocks.put(entity, new ReentrantLock());
        });
        this.enqueueAllLocks = tmpEnqueueAllLocks;
        this.indexConfigurationManager = indexConfigurationManager;
    }

    public boolean tryLockQueueProcessing() {
        return indexingQueueProcessingLock.tryLock();
    }

    public boolean tryLockQueueProcessing(long timeout, TimeUnit unit) throws InterruptedException {
        return indexingQueueProcessingLock.tryLock(timeout, unit);
    }

    public void unlockQueueProcessing() {
        indexingQueueProcessingLock.unlock();
    }

    public boolean isQueueProcessingLocked() {
        return indexingQueueProcessingLock.isLocked();
    }

    public boolean tryLockReindexing() {
        return reindexingLock.tryLock();
    }

    public boolean tryLockReindexing(long timeout, TimeUnit unit) throws InterruptedException {
        return reindexingLock.tryLock(timeout, unit);
    }

    public void unlockReindexing() {
        reindexingLock.unlock();
    }

    public boolean isReindexingLocked() {
        return reindexingLock.isLocked();
    }

    public boolean tryLockEntityForEnqueueIndexAll(String entityName) {
        checkEntityInIndexingScope(entityName);
        ReentrantLock lock = enqueueAllLocks.computeIfAbsent(entityName, key -> new ReentrantLock());
        return lock.tryLock();
    }

    public void unlockEntityForEnqueueIndexAll(String entityName) {
        checkEntityInIndexingScope(entityName);
        ReentrantLock lock = enqueueAllLocks.get(entityName);
        if (lock != null) {
            lock.unlock();
        }
    }

    protected void checkEntityInIndexingScope(String entityName) {
        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            throw new IllegalArgumentException(String.format("Entity '%s' is not configured for indexing", entityName));
        }
    }
}
