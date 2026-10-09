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
import org.jspecify.annotations.Nullable;
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
    protected final Map<String, ReentrantLock> enqueueingSessionOperationLocks;

    protected final IndexConfigurationManager indexConfigurationManager;

    /**
     * Asks the index configurations nothing: both maps fill themselves on first use.
     * <p>
     * They used to be prefilled, one lock per indexed entity. That stopped working when the unit of work became
     * the pair of an entity and a tenant - a prefilled lock is keyed by the entity name alone and no longer
     * matches what a caller looks up - and it was redundant even before, because every lookup goes through
     * {@code computeIfAbsent}.
     * <p>
     * It was also not free. Asking for the indexed entities builds the index definitions, and a bean constructor
     * runs before Liquibase has created the schema. An application that has the Dynamic Attributes module could
     * not start on a database without the attribute tables: resolving the dynamic attributes of an entity reads
     * them, and here that read happened too early.
     */
    @Autowired
    public IndexingLocker(IndexConfigurationManager indexConfigurationManager) {
        this.enqueueAllLocks = new ConcurrentHashMap<>();
        this.enqueueingSessionOperationLocks = new ConcurrentHashMap<>();
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

    /**
     * Keeps two bulk enqueueings of the same records from running at once, which would fill the queue with
     * duplicates and move the session's position twice.
     * <p>
     * The records are those of one entity of one tenant, so that work on one tenant does not exclude work on
     * another. An entity that is not split has a null tenant and is keyed by its name alone, as before.
     */
    public boolean tryLockEntityForEnqueueIndexAll(String entityName, @Nullable String tenantId) {
        checkEntityInIndexingScope(entityName);
        ReentrantLock lock = enqueueAllLocks.computeIfAbsent(sessionKey(entityName, tenantId),
                key -> new ReentrantLock());
        return lock.tryLock();
    }

    public void unlockEntityForEnqueueIndexAll(String entityName, @Nullable String tenantId) {
        ReentrantLock lock = enqueueAllLocks.get(sessionKey(entityName, tenantId));
        if (lock != null) {
            lock.unlock();
        }
    }

    public boolean tryLockEnqueueingSession(String entityName, @Nullable String tenantId) {
        ReentrantLock lock = acquireEnqueueingSessionLock(entityName, tenantId);
        return lock.tryLock();
    }

    public boolean tryLockEnqueueingSession(String entityName, @Nullable String tenantId,
                                            long timeout, TimeUnit unit) {
        ReentrantLock lock = acquireEnqueueingSessionLock(entityName, tenantId);
        try {
            return lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock of enqueueing session failed", e);
        }
    }

    public void unlockEnqueueingSession(String entityName, @Nullable String tenantId) {
        ReentrantLock lock = enqueueingSessionOperationLocks.get(sessionKey(entityName, tenantId));
        if (lock != null) {
            lock.unlock();
        }
    }

    /**
     * Asks nothing about the entity: the caller decides whether it has to be in the indexed set.
     * <p>
     * An operation that removes what an entity left behind runs for an entity that is no longer indexed, and it
     * needs this lock just as much as the operations that create work.
     */
    protected ReentrantLock acquireEnqueueingSessionLock(String entityName, @Nullable String tenantId) {
        return enqueueingSessionOperationLocks.computeIfAbsent(sessionKey(entityName, tenantId),
                key -> new ReentrantLock());
    }

    /**
     * An enqueueing session belongs to one entity of one tenant, so operations on the sessions of different
     * tenants of the same entity have nothing to exclude each other from.
     */
    protected String sessionKey(String entityName, @Nullable String tenantId) {
        return tenantId == null ? entityName : entityName + '/' + tenantId;
    }

    /**
     * Guards taking a lock, not releasing it: a release runs in a {@code finally} block, where a throw would leave
     * the lock held and would replace the exception that is being propagated.
     */
    protected void checkEntityInIndexingScope(String entityName) {
        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            throw new IllegalArgumentException(String.format("Entity '%s' is not configured for indexing", entityName));
        }
    }
}
