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

package io.jmix.securitydata.impl.role.assignment;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentModel;
import io.jmix.security.role.assignment.RoleAssignmentPersistence;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component("sec_DatabaseRoleAssignmentPersistence")
public class DatabaseRoleAssignmentPersistence implements RoleAssignmentPersistence {

    protected static final String ROLE_CODE_PROPERTY = "roleCode";

    private final DataManager dataManager;
    private final EntityStates entityStates;

    public DatabaseRoleAssignmentPersistence(DataManager dataManager, EntityStates entityStates) {
        this.dataManager = dataManager;
        this.entityStates = entityStates;
    }


    @Override
    public List<String> getExcludedUsernames(String roleCode) {
        return dataManager.load(RoleAssignmentEntity.class)
                .condition(PropertyCondition.equal(ROLE_CODE_PROPERTY, roleCode).skipNullOrEmpty())
                .list()
                .stream()
                .map(RoleAssignmentEntity::getUsername)
                .toList();
    }

    @Override
    public void save(List<RoleAssignment> roleAssignments) {
        List<RoleAssignmentEntity> entities = roleAssignments.stream()
                .map(roleAssignment -> {
                    RoleAssignmentEntity entity = dataManager.create(RoleAssignmentEntity.class);
                    entity.setRoleCode(roleAssignment.getRoleCode());
                    entity.setRoleType(roleAssignment.getRoleType());
                    entity.setUsername(roleAssignment.getUsername());
                    return entity;
                })
                .toList();

        SaveContext saveContext = new SaveContext().setDiscardSaved(true).saving(entities);
        dataManager.save(saveContext);
    }

    @Override
    public void save(Collection<RoleAssignmentModel> toSave, Collection<RoleAssignmentModel> toRemove) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);

        toSave.stream().
                map(this::modelToEntity)
                .forEach(saveContext::saving);
        toRemove.stream().
                map(this::modelToEntity)
                .forEach(saveContext::removing);

        dataManager.save(saveContext);
    }

    @Override
    public List<RoleAssignmentModel> loadRoleAssignments(String username, String roleType) {
        return dataManager.load(RoleAssignmentEntity.class)
                .query("e.username = ?1 and e.roleType = ?2", username, roleType)
                .list()
                .stream()
                .map(this::entityToModel)
                .toList();
    }

    private RoleAssignmentModel entityToModel(RoleAssignmentEntity entity) {
        RoleAssignmentModel model = dataManager.create(RoleAssignmentModel.class);
        model.setId(entity.getId());
        model.setVersion(entity.getVersion());
        model.setRoleCode(entity.getRoleCode());
        model.setRoleType(entity.getRoleType());
        model.setUsername(entity.getUsername());
        entityStates.setNew(model, false);
        return model;
    }

    private RoleAssignmentEntity modelToEntity(RoleAssignmentModel model) {
        RoleAssignmentEntity entity = dataManager.create(RoleAssignmentEntity.class);
        entity.setId(model.getId());
        entity.setVersion(model.getVersion());
        entity.setRoleCode(model.getRoleCode());
        entity.setRoleType(model.getRoleType());
        entity.setUsername(model.getUsername());
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }

}
