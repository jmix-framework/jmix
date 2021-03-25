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

package io.jmix.search.index;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.search.index.queue.entity.QueueItem;

import java.util.Collection;

/**
 * Provides functionality for documents indexing.
 */
public interface EntityIndexer {

    /**
     * Stores documents defined in provided queue items to index.
     *
     * @param queueItems collection of queue items
     */
    void indexQueueItems(Collection<QueueItem> queueItems);

    /**
     * Stores single document to index using primary key.
     *
     * @param metaClass  entity meta class
     * @param entityPk   entity primary key
     * @param changeType type of performed operation
     */
    void indexEntityByPk(MetaClass metaClass, String entityPk, EntityOp changeType);

    /**
     * Stores multiple documents to index using primary keys
     *
     * @param metaClass  entity meta class
     * @param entityPks  entity primary keys
     * @param changeType type of performed operation
     */
    void indexEntitiesByPks(MetaClass metaClass, Collection<String> entityPks, EntityOp changeType);
}
