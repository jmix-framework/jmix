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

package io.jmix.securitydata.role.provider;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.provider.RoleProvider;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.RoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Role provider that gets role from the database from the {@link RoleEntity}.
 */
@Component("sec_DatabaseRoleProvider")
public class DatabaseRoleProvider implements RoleProvider {

    @Autowired
    protected DataManager dataManager;

    @Override
    public Collection<Role> getAllRoles() {
        return dataManager.load(RoleEntity.class)
//                .query("select r from sec_RoleEntity r")
                .fetchPlan(fetchPlanBuilder -> {
                    fetchPlanBuilder
                            .addAll("name", "code", "scope")
                            .add("rowLevelPolicies", FetchPlan.BASE)
                            .add("resourcePolicies", FetchPlan.BASE);
                })
                .list()
                .stream()
                .map(this::buildRole)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Role getRoleByCode(String code) {
        return dataManager.load(RoleEntity.class)
                .query("where e.code = :code")
                .parameter("code", code)
                .fetchPlan(fetchPlanBuilder -> {
                    fetchPlanBuilder
                            .addAll("name", "code", "scope")
                            .add("rowLevelPolicies", FetchPlan.BASE)
                            .add("resourcePolicies", FetchPlan.BASE);
                })
                .optional()
                .map(this::buildRole)
                .orElse(null);
    }

    protected Role buildRole(RoleEntity roleEntity) {
        Role role = new Role();
        role.setName(roleEntity.getName());
        role.setCode(roleEntity.getCode());
        role.setSource(RoleSource.DATABASE);
        role.getCustomProperties().put("databaseId", roleEntity.getId().toString());

        List<ResourcePolicyEntity> resourcePolicyEntities = roleEntity.getResourcePolicies();
        if (resourcePolicyEntities != null) {
            List<ResourcePolicy> resourcePolicies = resourcePolicyEntities.stream()
                    .map(entity -> new ResourcePolicy(entity.getType(),
                            entity.getResource(),
                            entity.getAction(),
                            entity.getEffect(),
                            Collections.singletonMap("databaseId", entity.getId().toString())))
                    .collect(Collectors.toList());
            role.setResourcePolicies(resourcePolicies);
        }

        List<RowLevelPolicyEntity> rowLevelPolicyEntities = roleEntity.getRowLevelPolicies();
        if (rowLevelPolicyEntities != null) {
            List<RowLevelPolicy> rowLevelPolicies = rowLevelPolicyEntities.stream()
                    .map(entity ->
                            new RowLevelPolicy(entity.getEntityName(), entity.getWhereClause(), entity.getJoinClause(),
                                    Collections.singletonMap("databaseId", entity.getId().toString()))
                    )
                    .collect(Collectors.toList());
            role.setRowLevelPolicies(rowLevelPolicies);
        }
        return role;
    }
}
