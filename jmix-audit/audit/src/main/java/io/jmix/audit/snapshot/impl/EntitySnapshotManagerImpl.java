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

package io.jmix.audit.snapshot.impl;

import io.jmix.audit.snapshot.EntitySnapshotManager;
import io.jmix.audit.snapshot.datastore.EntitySnapshotDataStore;
import io.jmix.audit.snapshot.model.EntitySnapshotModel;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;

@Component("audit_EntitySnapshotManager")
public class EntitySnapshotManagerImpl implements EntitySnapshotManager {

    private final ExtendedEntities extendedEntities;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final FetchPlans fetchPlans;
    private final MetadataTools metadataTools;
    private final Metadata metadata;
    private final ReferenceToEntitySupport referenceToEntitySupport;
    private final TimeSource timeSource;
    private final CurrentAuthentication currentAuthentication;
    private final EntitySerialization entitySerialization;
    private final FetchPlanSerialization fetchPlanSerialization;
    private final EntitySnapshotDataStore entitySnapshotDataStore;

    public EntitySnapshotManagerImpl(ExtendedEntities extendedEntities,
                                     UnconstrainedDataManager unconstrainedDataManager,
                                     FetchPlans fetchPlans,
                                     MetadataTools metadataTools,
                                     Metadata metadata,
                                     ReferenceToEntitySupport referenceToEntitySupport,
                                     TimeSource timeSource,
                                     CurrentAuthentication currentAuthentication,
                                     EntitySerialization entitySerialization,
                                     FetchPlanSerialization fetchPlanSerialization,
                                     EntitySnapshotDataStore entitySnapshotDataStore) {
        this.extendedEntities = extendedEntities;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.fetchPlans = fetchPlans;
        this.metadataTools = metadataTools;
        this.metadata = metadata;
        this.referenceToEntitySupport = referenceToEntitySupport;
        this.timeSource = timeSource;
        this.currentAuthentication = currentAuthentication;
        this.entitySerialization = entitySerialization;
        this.fetchPlanSerialization = fetchPlanSerialization;
        this.entitySnapshotDataStore = entitySnapshotDataStore;
    }

    @Override
    public List<EntitySnapshotModel> getSnapshots(MetaClass metaClass, Object id) {
        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        FetchPlan localFetchPlan = fetchPlans.builder(metaClass.getJavaClass())
                .addFetchPlan(FetchPlan.LOCAL)
                .build();
        Object entity = unconstrainedDataManager.load(new LoadContext<>(metaClass).setId(id).setFetchPlan(localFetchPlan));
        if (entity == null) {
            throw new RuntimeException("Entity instance not found");
        }
        checkCompositePrimaryKey(metaClass, entity);
        return entitySnapshotDataStore.findEntitySnapshotByMetaClassAndEntity(entity, metaClass);
    }

    @Override
    public List<EntitySnapshotModel> getSnapshots(Object entity) {
        MetaClass entityMetaClass = metadata.getClass(entity);
        Object entityId = EntityValues.getId(entity);
        if (entityId == null) {
            throw new RuntimeException("Cannot evaluate entity id for " + entity);
        }
        return getSnapshots(entityMetaClass, entityId);
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan) {
        return createSnapshot(entity, fetchPlan, timeSource.currentTimestamp());
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate) {
        String username = currentAuthentication.getUser().getUsername();
        return createSnapshot(entity, fetchPlan, snapshotDate, username);
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        EntitySnapshotModel snapshot = createEntitySnapshot(entity, fetchPlan, snapshotDate, authorUsername);
        return entitySnapshotDataStore.save(snapshot);
    }

    @Override
    public Object extractEntity(EntitySnapshotModel snapshot) {
        String rawResult = snapshot.getSnapshotXml();
        return entitySerialization.entityFromJson(rawResult, metadata.getClass(snapshot.getEntityMetaClass()));
    }

    @Override
    public FetchPlan extractFetchPlan(EntitySnapshotModel snapshot) {
        String rawResult = snapshot.getFetchPlanXml();
        return fetchPlanSerialization.fromJson(rawResult);
    }

    @Nullable
    @Override
    public EntitySnapshotModel getLastEntitySnapshot(Object entity) {
        MetaClass entityMetaClass = metadata.getClass(entity);
        checkCompositePrimaryKey(entityMetaClass, entity);
        return entitySnapshotDataStore.findLastSnapshot(entity, entityMetaClass);
    }

    @Nullable
    @Override
    public EntitySnapshotModel getLastEntitySnapshot(MetaClass metaClass, Object referenceId) {
        if (referenceId instanceof Entity) {
            throw new IllegalArgumentException(format("Reference id can not be an entity: %s", referenceId.getClass()));
        }
        return entitySnapshotDataStore.findLastSnapshotById(referenceId, metaClass);
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan) {
        return createTempSnapshot(entity, fetchPlan, timeSource.currentTimestamp());
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate) {
        String username = currentAuthentication.getUser().getUsername();
        return createTempSnapshot(entity, fetchPlan, snapshotDate, username);
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        return createEntitySnapshot(entity, fetchPlan, snapshotDate, authorUsername);
    }

    private void checkCompositePrimaryKey(MetaClass metaClass, Object entity) {
        if (metadataTools.hasCompositePrimaryKey(metaClass) && !EntityValues.isUuidSupported(entity)) {
            throw new UnsupportedOperationException(format("Entity %s has no persistent UUID attribute", entity));
        }
    }

    private EntitySnapshotModel createEntitySnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        Preconditions.checkNotNullArgument(entity);
        Preconditions.checkNotNullArgument(fetchPlan);
        Preconditions.checkNotNullArgument(snapshotDate);
        MetaClass entityMetaClass = metadata.getClass(entity);
        checkCompositePrimaryKey(entityMetaClass, entity);

        Class fetchPlanEntityClass = fetchPlan.getEntityClass();
        Class entityClass = entity.getClass();

        if (!fetchPlanEntityClass.isAssignableFrom(entityClass)) {
            throw new IllegalStateException("FetchPlan could not be used with this propertyValue");
        }

        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity));

        EntitySnapshotModel snapshot = metadata.create(EntitySnapshotModel.class);
        snapshot.setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
        snapshot.setEntityMetaClass(metaClass.getName());
        snapshot.setFetchPlanXml(fetchPlanSerialization.toJson(fetchPlan, FetchPlanSerializationOption.COMPACT_FORMAT));
        snapshot.setSnapshotXml(entitySerialization.toJson(entity));
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setAuthorUsername(authorUsername);

        return snapshot;
    }
}
