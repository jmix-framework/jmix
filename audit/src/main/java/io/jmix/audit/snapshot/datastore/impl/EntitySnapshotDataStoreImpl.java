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

package io.jmix.audit.snapshot.datastore.impl;

import io.jmix.audit.entity.EntitySnapshot;
import io.jmix.audit.snapshot.datastore.EntitySnapshotDataStore;
import io.jmix.audit.snapshot.model.EntitySnapshotModel;
import io.jmix.audit.snapshot.model.EntitySnapshotModelConverter;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

@Component("audit_EntitySnapshotDataStore")
public class EntitySnapshotDataStoreImpl implements EntitySnapshotDataStore {

    private final ReferenceToEntitySupport referenceToEntitySupport;
    private final Metadata metadata;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final ExtendedEntities extendedEntities;
    private final EntitySnapshotModelConverter entitySnapshotModelConverter;

    public EntitySnapshotDataStoreImpl(ReferenceToEntitySupport referenceToEntitySupport,
                                       Metadata metadata,
                                       UnconstrainedDataManager unconstrainedDataManager,
                                       ExtendedEntities extendedEntities,
                                       EntitySnapshotModelConverter entitySnapshotModelConverter) {
        this.referenceToEntitySupport = referenceToEntitySupport;
        this.metadata = metadata;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.extendedEntities = extendedEntities;
        this.entitySnapshotModelConverter = entitySnapshotModelConverter;
    }

    @Override
    public List<EntitySnapshotModel> findEntitySnapshotByMetaClassAndEntity(Object entity, MetaClass entityMetaClass) {
        String query = format(
                "select s from audit_EntitySnapshot s where s.entity.%s = :entityId and s.entityMetaClass = :metaClass " +
                        "order by s.snapshotDate desc", referenceToEntitySupport.getReferenceIdPropertyName(entityMetaClass));
        LoadContext<EntitySnapshot> entitySnapshotLoadContext = new LoadContext<EntitySnapshot>(metadata.getClass(EntitySnapshot.class))
                .setQuery(new LoadContext.Query(query)
                        .setParameter("entityId", referenceToEntitySupport.getReferenceId(entity))
                        .setParameter("metaClass", entityMetaClass.getName())
                );
        return entitySnapshotModelConverter.createEntitySnapshotModels(unconstrainedDataManager.loadList(entitySnapshotLoadContext));
    }

    @Override
    public EntitySnapshotModel findLastSnapshot(Object entity, MetaClass entityMetaClass) {
        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(entityMetaClass);
        LoadContext<EntitySnapshot> lx = new LoadContext<EntitySnapshot>(metadata.getClass(EntitySnapshot.class))
                .setQuery(new LoadContext.Query(format("select e from audit_EntitySnapshot e where e.entityMetaClass = :metaClass and"
                                + " e.entity.%s = :entityId order by e.snapshotDate desc",
                        referenceToEntitySupport.getReferenceIdPropertyName(metaClass)))
                        .setParameter("metaClass", metaClass.getName())
                        .setParameter("entityId", referenceToEntitySupport.getReferenceId(entity))
                        .setMaxResults(1));
        return entitySnapshotModelConverter.createEntitySnapshotModel(unconstrainedDataManager.load(lx));
    }

    @Override
    public EntitySnapshotModel findLastSnapshotById(Object referenceId, MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        LoadContext<EntitySnapshot> entitySnapshotLoadContext = new LoadContext<EntitySnapshot>(metadata.getClass(EntitySnapshot.class))
                .setQuery(new LoadContext.Query(String.format("select e from audit_EntitySnapshot e where e.entityMetaClass = :metaClass and"
                                + " e.entity.%s = :entityId order by e.snapshotDate desc",
                        referenceToEntitySupport.getReferenceIdPropertyName(originalMetaClass)))
                        .setParameter("metaClass", originalMetaClass.getName())
                        .setParameter("entityId", referenceId)
                        .setMaxResults(1)
                );
        return entitySnapshotModelConverter.createEntitySnapshotModel(unconstrainedDataManager.load(entitySnapshotLoadContext));
    }

    @Override
    public void saveSnapshot(Collection<EntitySnapshotModel> entitySnapshots) {
        unconstrainedDataManager.save(
                entitySnapshots.stream()
                .map(entitySnapshotModelConverter::createEntitySnapshot)
                .toArray()
        );
    }

    @Override
    public EntitySnapshotModel save(EntitySnapshotModel entitySnapshotModel) {
        EntitySnapshot entitySnapshot = entitySnapshotModelConverter.createEntitySnapshot(entitySnapshotModel);
        entitySnapshot = unconstrainedDataManager.save(entitySnapshot);
        return entitySnapshotModelConverter.createEntitySnapshotModel(entitySnapshot);
    }
}
