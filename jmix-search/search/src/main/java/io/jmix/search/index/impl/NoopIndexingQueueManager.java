/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.Id;
import io.jmix.search.index.queue.IndexingQueueManager;

import java.util.Collection;
import java.util.List;

public class NoopIndexingQueueManager implements IndexingQueueManager {
    @Override
    public int emptyQueue() {
        return 0;
    }

    @Override
    public int emptyQueue(String entityName) {
        return 0;
    }

    @Override
    public int enqueueIndex(Object entityInstance) {
        return 0;
    }

    @Override
    public int enqueueIndexCollection(Collection<Object> entityInstances) {
        return 0;
    }

    @Override
    public int enqueueIndexByEntityId(Id<?> entityId) {
        return 0;
    }

    @Override
    public int enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return 0;
    }

    @Override
    public int enqueueIndexAll() {
        return 0;
    }

    @Override
    public int enqueueIndexAll(String entityName) {
        return 0;
    }

    @Override
    public List<String> getEntityNamesOfEnqueueingSessions() {
        return List.of();
    }

    @Override
    public void initAsyncEnqueueIndexAll() { }

    @Override
    public boolean initAsyncEnqueueIndexAll(String entityName) {
        return false;
    }

    @Override
    public void suspendAsyncEnqueueIndexAll() { }

    @Override
    public boolean suspendAsyncEnqueueIndexAll(String entityName) {
        return false;
    }

    @Override
    public void resumeAsyncEnqueueIndexAll() { }

    @Override
    public boolean resumeAsyncEnqueueIndexAll(String entityName) {
        return false;
    }

    @Override
    public void terminateAsyncEnqueueIndexAll() { }

    @Override
    public boolean terminateAsyncEnqueueIndexAll(String entityName) {
        return false;
    }

    @Override
    public int processNextEnqueueingSession() {
        return 0;
    }

    @Override
    public int processNextEnqueueingSession(int batchSize) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName, int batchSize) {
        return 0;
    }

    @Override
    public int enqueueDelete(Object entityInstance) {
        return 0;
    }

    @Override
    public int enqueueDeleteCollection(Collection<Object> entityInstances) {
        return 0;
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId) {
        return 0;
    }

    @Override
    public int enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return 0;
    }

    @Override
    public int processNextBatch() {
        return 0;
    }

    @Override
    public int processNextBatch(int batchSize) {
        return 0;
    }

    @Override
    public int processEntireQueue() {
        return 0;
    }

    @Override
    public int processEntireQueue(int batchSize) {
        return 0;
    }
}
