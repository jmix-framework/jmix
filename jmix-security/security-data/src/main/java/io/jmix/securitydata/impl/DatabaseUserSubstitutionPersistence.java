/*
 * Copyright 2024 Haulmont.
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

package io.jmix.securitydata.impl;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.security.usersubstitution.UserSubstitutionModel;
import io.jmix.security.usersubstitution.UserSubstitutionPersistence;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component("sec_DatabaseUserSubstitutionPersistence")
public class DatabaseUserSubstitutionPersistence implements UserSubstitutionPersistence {

    private final Metadata metadata;
    private final AccessManager accessManager;
    private final DataManager dataManager;
    private final EntityStates entityStates;

    public DatabaseUserSubstitutionPersistence(Metadata metadata, AccessManager accessManager, DataManager dataManager, EntityStates entityStates) {
        this.metadata = metadata;
        this.accessManager = accessManager;
        this.dataManager = dataManager;
        this.entityStates = entityStates;
    }

    @Override
    public boolean isViewPermitted() {
        CrudEntityContext userSubstitutionContext = new CrudEntityContext(metadata.getClass(UserSubstitutionEntity.class));
        accessManager.applyRegisteredConstraints(userSubstitutionContext);
        return userSubstitutionContext.isReadPermitted();
    }

    @Override
    public List<UserSubstitutionModel> loadSubstitutionsOf(String username) {
        return dataManager.load(UserSubstitutionEntity.class)
                .query("e.username = ?1", username)
                .list()
                .stream()
                .map(this::entityToModel)
                .toList();
    }

    @Override
    public void remove(Collection<UserSubstitutionModel> userSubstitutionModels) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
        for (UserSubstitutionModel model : userSubstitutionModels) {
            saveContext.removing(modelToEntity(model));
        }
        dataManager.save(saveContext);
    }

    @Override
    public UserSubstitutionModel load(UUID id) {
        UserSubstitutionEntity entity = dataManager.load(UserSubstitutionEntity.class).id(id).one();
        return entityToModel(entity);
    }

    @Override
    public UserSubstitutionModel save(UserSubstitutionModel userSubstitutionModel) {
        UserSubstitutionEntity entity = modelToEntity(userSubstitutionModel);
        UserSubstitutionEntity savedEntity = dataManager.save(entity);
        return entityToModel(savedEntity);
    }

    private UserSubstitutionModel entityToModel(UserSubstitutionEntity entity) {
        UserSubstitutionModel model = metadata.create(UserSubstitutionModel.class);
        model.setId(entity.getId());
        model.setVersion(entity.getVersion());
        model.setUsername(entity.getUsername());
        model.setSubstitutedUsername(entity.getSubstitutedUsername());
        model.setStartDate(entity.getStartDate());
        model.setEndDate(entity.getEndDate());
        entityStates.setNew(model, false);
        return model;
    }

    private UserSubstitutionEntity modelToEntity(UserSubstitutionModel model) {
        UserSubstitutionEntity entity = metadata.create(UserSubstitutionEntity.class);
        entity.setId(model.getId());
        entity.setVersion(model.getVersion());
        entity.setUsername(model.getUsername());
        entity.setSubstitutedUsername(model.getSubstitutedUsername());
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }
}
