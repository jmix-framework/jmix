/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.Id;
import org.jspecify.annotations.Nullable;

/**
 * A record to delete from the index, with the tenant it belonged to.
 * <p>
 * The tenant cannot be determined after the fact: by the time a deletion is processed the record is gone from the
 * database, so it has to travel with the deletion from the moment it is requested.
 * <p>
 * A {@code tenantId} of {@code null} stands for three different things:
 * <ul>
 *     <li>the entity is not split by tenants, so there is one shared index to delete from;</li>
 *     <li>the entity is split and the record held no tenant - everything an administrator creates is like that,
 *     and such a record is indexed nowhere (decision 0013);</li>
 *     <li>nobody looked: the deletion was requested by id alone, see {@link #tenantUnknown(Id)}.</li>
 * </ul>
 * The last two are indistinguishable once the deletion is queued, so both are sent to every index of the entity.
 * Deleting a document that is not there is not a failure.
 *
 * @param entityId id of the record whose document is deleted
 * @param tenantId tenant the record belonged to, or {@code null} in any of the three cases above
 */
public record EntityDeletionTarget(Id<?> entityId, @Nullable String tenantId) {

    /**
     * A deletion requested by id alone, with nothing read from the record.
     */
    public static EntityDeletionTarget tenantUnknown(Id<?> entityId) {
        return new EntityDeletionTarget(entityId, null);
    }
}
