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

package io.jmix.search.index.queue;

import io.jmix.core.annotation.Internal;
import io.jmix.search.index.queue.entity.EnqueueingSession;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Allows to load ids of entity instances according to enqueueing session.
 */
@Internal
public interface EntityIdsLoader {

    /**
     * Loads next batch of instances related to provided session.
     *
     * @param session   enqueueing session
     * @param batchSize batch size
     * @return loading result
     */
    ResultHolder loadNextIds(EnqueueingSession session, int batchSize);

    /**
     * Keeps result of ids loading.
     */
    class ResultHolder {
        private final List<?> ids;
        private final Object lastOrderingValue;

        public static final ResultHolder EMPTY = new ResultHolder(Collections.emptyList(), null);

        public ResultHolder(List<?> ids, @Nullable Object lastOrderingValue) {
            this.ids = ids;
            this.lastOrderingValue = lastOrderingValue;
        }

        public List<?> getIds() {
            return ids;
        }

        @Nullable
        public Object getLastOrderingValue() {
            return lastOrderingValue;
        }
    }
}
