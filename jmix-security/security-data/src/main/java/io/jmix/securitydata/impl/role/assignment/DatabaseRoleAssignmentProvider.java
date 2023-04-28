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

package io.jmix.securitydata.impl.role.assignment;

import io.jmix.core.FetchPlan;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentProvider;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Role assignment provider that gets role assignments from the database.
 */
@Component("sec_DatabaseRoleAssignmentProvider")
public class DatabaseRoleAssignmentProvider implements RoleAssignmentProvider {

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Override
    public Collection<RoleAssignment> getAllAssignments() {
        return dataManager.load(RoleAssignmentEntity.class)
                .all()
                .fetchPlan(FetchPlan.BASE)
                .list()
                .stream()
                .map(this::buildRoleAssignment)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RoleAssignment> getAssignmentsByUsername(String username) {
        return dataManager.load(RoleAssignmentEntity.class)
                .query("e.username = :username")
                .parameter("username", username)
                .list()
                .stream()
                .map(this::buildRoleAssignment)
                .collect(Collectors.toList());
    }

    protected RoleAssignment buildRoleAssignment(RoleAssignmentEntity roleAssignmentEntity) {
        return new RoleAssignment(roleAssignmentEntity.getUsername(), roleAssignmentEntity.getRoleCode(), roleAssignmentEntity.getRoleType());
    }
}
