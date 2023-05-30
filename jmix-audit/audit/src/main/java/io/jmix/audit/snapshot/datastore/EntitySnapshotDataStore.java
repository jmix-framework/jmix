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

package io.jmix.audit.snapshot.datastore;

import io.jmix.audit.snapshot.model.EntitySnapshotModel;
import io.jmix.core.metamodel.model.MetaClass;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Interface defining methods for creating and getting operations on entity snapshots.
 * <p>
 * Implementation of this interface defined method and location entity snapshots storage.
 * The default behavior is to save entity snapshots as JPA entities in one table.
 * If you want to redefinition default behavior you must create your own implementation of the interface.
 */
public interface EntitySnapshotDataStore {

    /**
     * Loads list of snapshots specific entity.
     *
     * @param entityMetaClass {@link MetaClass} object, defining the type of snapshots
     * @param entity {@link Object} object, defining entity id what will be used for load snapshots
     * @return list of snapshots, or an empty list if not found
     */
    List<EntitySnapshotModel> findEntitySnapshotByMetaClassAndEntity(Object entity, MetaClass entityMetaClass);

    /**
     * Loads last snapshot of specific entity.
     *
     * @param entityMetaClass {@link MetaClass} object, defining the type of the snapshot
     * @param entity {@link Object} object, defining entity id what will be used for load snapshot
     * @return load snapshot, or null if not found
     */
    @Nullable
    EntitySnapshotModel findLastSnapshot(Object entity, MetaClass entityMetaClass);

    /**
     * Loads last snapshot of specific entity.
     *
     * @param entityMetaClass {@link MetaClass} object, defining the type of the snapshot
     * @param referenceId {@link Object} object, defining entity id what will be used for load snapshot
     * @return load snapshot, or null if not found
     */
    @Nullable
    EntitySnapshotModel findLastSnapshotById(Object referenceId, MetaClass entityMetaClass);

    /**
     * Save collection of entity snapshots.
     *
     * @param entitySnapshots {@link EntitySnapshotModel} snapshots for saving
     */
    void saveSnapshot(Collection<EntitySnapshotModel> entitySnapshots);

    /**
     * Save entity snapshot.
     *
     * @param entitySnapshotModel {@link EntitySnapshotModel} snapshot for saving
     * @return saved instance
     */
    EntitySnapshotModel save(EntitySnapshotModel entitySnapshotModel);
}
