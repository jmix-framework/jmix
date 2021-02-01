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

package io.jmix.search.index.queue;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.EntityChangeType;

import java.util.Collection;

public interface QueueService {

    void enqueue(MetaClass entityMetaClass, String entityId, EntityChangeType entityChangeType);

    void enqueue(MetaClass entityMetaClass, Collection<String> entityIds, EntityChangeType entityChangeType);

    void enqueue(QueueItem queueItem);

    void enqueue(Collection<QueueItem> queueItems);

    void processQueue();
}
