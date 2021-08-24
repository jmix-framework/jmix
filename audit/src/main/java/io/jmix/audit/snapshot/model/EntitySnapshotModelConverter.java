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

package io.jmix.audit.snapshot.model;

import io.jmix.audit.entity.EntitySnapshot;
import io.jmix.core.Metadata;
import io.jmix.data.entity.ReferenceToEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class is used for converting {@link io.jmix.audit.entity.EntitySnapshot} objects
 * into non-persistent {@link EntitySnapshotModel} entities which may be
 * displayed in UI
 */
@Component("audit_EntitySnapshotModelConverter")
public class EntitySnapshotModelConverter {

    private final Metadata metadata;

    public EntitySnapshotModelConverter(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<EntitySnapshotModel> createEntitySnapshotModels(Collection<EntitySnapshot> entitySnapshots) {
        if (entitySnapshots.isEmpty()) {
            return Collections.emptyList();
        }
        return entitySnapshots.stream()
                .map(this::createEntitySnapshotModel)
                .collect(Collectors.toList());
    }

    public EntitySnapshotModel createEntitySnapshotModel(EntitySnapshot entitySnapshot) {
        if (entitySnapshot == null) {
            return null;
        }
        EntitySnapshotModel entitySnapshotModel = metadata.create(EntitySnapshotModel.class);
        entitySnapshotModel.setEntityId(entitySnapshot.getEntity().getEntityId());
        entitySnapshotModel.setIntEntityId(entitySnapshot.getEntity().getIntEntityId());
        entitySnapshotModel.setLongEntityId(entitySnapshot.getEntity().getLongEntityId());
        entitySnapshotModel.setStringEntityId(entitySnapshot.getEntity().getStringEntityId());

        entitySnapshotModel.setSnapshotXml(entitySnapshot.getSnapshotXml());
        entitySnapshotModel.setSnapshotDate(entitySnapshot.getSnapshotDate());
        entitySnapshotModel.setCreatedBy(entitySnapshot.getCreatedBy());
        entitySnapshotModel.setCreatedDate(entitySnapshot.getCreatedDate());
        entitySnapshotModel.setSysTenantId(entitySnapshot.getSysTenantId());
        entitySnapshotModel.setFetchPlanXml(entitySnapshot.getFetchPlanXml());
        entitySnapshotModel.setEntityMetaClass(entitySnapshot.getEntityMetaClass());
        entitySnapshotModel.setAuthorUsername(entitySnapshot.getAuthorUsername());
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("databaseId",entitySnapshot.getId().toString());
        entitySnapshotModel.setCustomProperties(customProperties);
        return entitySnapshotModel;
    }

    public EntitySnapshot createEntitySnapshot(EntitySnapshotModel entitySnapshotModel) {
        if (entitySnapshotModel == null) {
            return null;
        }
        EntitySnapshot entitySnapshot = metadata.create(EntitySnapshot.class);
        ReferenceToEntity referenceToEntity = metadata.create(ReferenceToEntity.class);
        referenceToEntity.setEntityId(entitySnapshotModel.getEntityId());
        referenceToEntity.setIntEntityId(entitySnapshotModel.getIntEntityId());
        referenceToEntity.setLongEntityId(entitySnapshotModel.getLongEntityId());
        referenceToEntity.setStringEntityId(entitySnapshotModel.getStringEntityId());
        entitySnapshot.setEntity(referenceToEntity);

        entitySnapshot.setSnapshotXml(entitySnapshotModel.getSnapshotXml());
        entitySnapshot.setSnapshotDate(entitySnapshotModel.getSnapshotDate());
        entitySnapshot.setCreatedBy(entitySnapshotModel.getCreatedBy());
        entitySnapshot.setCreatedDate(entitySnapshotModel.getCreatedDate());
        entitySnapshot.setSysTenantId(entitySnapshotModel.getSysTenantId());
        entitySnapshot.setFetchPlanXml(entitySnapshotModel.getFetchPlanXml());
        entitySnapshot.setEntityMetaClass(entitySnapshotModel.getEntityMetaClass());
        entitySnapshot.setAuthorUsername(entitySnapshotModel.getAuthorUsername());
        return entitySnapshot;
    }
}
