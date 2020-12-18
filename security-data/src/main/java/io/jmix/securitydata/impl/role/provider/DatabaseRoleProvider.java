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

package io.jmix.securitydata.impl.role.provider;

import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.RoleProvider;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.RoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Role provider that gets role from the database from the {@link RoleEntity}.
 */
@Component("sec_DatabaseRoleProvider")
public class DatabaseRoleProvider implements RoleProvider {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ScriptEvaluator scriptEvaluator;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected SecureOperations entityOperations;

    @Autowired
    protected Metadata metadata;

    @Override
    public Collection<Role> getAllRoles() {
        return dataManager.load(RoleEntity.class)
//                .query("select r from sec_RoleEntity r")
                .fetchPlan(fetchPlanBuilder -> {
                    fetchPlanBuilder
                            .addAll("name", "code", "scope", "roleType", "childRoles")
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
                            .addAll("name", "code", "roleType", "childRoles")
                            .add("rowLevelPolicies", FetchPlan.BASE)
                            .add("resourcePolicies", FetchPlan.BASE);
                })
                .optional()
                .map(this::buildRole)
                .orElse(null);
    }

    @Override
    public boolean deleteRole(Role role) {
        if (!entityOperations.isEntityDeletePermitted(metadata.getClass(RoleEntity.class), policyStore)) {
            return false;
        }
        String roleDatabaseId = role.getCustomProperties().get("databaseId");
        RoleEntity roleEntity;
        if (Strings.isNullOrEmpty(roleDatabaseId)) {
            throw new IllegalArgumentException(String.format("Database ID of role with code \"%s\" is empty", role.getCode()));
        } else {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.getReference(RoleEntity.class, roleEntityId);
            dataManager.remove(roleEntity);
        }
        return true;
    }

    protected Role buildRole(RoleEntity roleEntity) {
        Role role = new Role();
        role.setName(roleEntity.getName());
        role.setCode(roleEntity.getCode());
        role.setSource(RoleSource.DATABASE);
        role.setRoleType(roleEntity.getRoleType());
        role.setChildRoles(roleEntity.getChildRoles());
        role.getCustomProperties().put("databaseId", roleEntity.getId().toString());

        List<ResourcePolicyEntity> resourcePolicyEntities = roleEntity.getResourcePolicies();
        if (resourcePolicyEntities != null) {
            List<ResourcePolicy> resourcePolicies = resourcePolicyEntities.stream()
                    .map(entity -> {
                        String id = entity.getId().toString();
                        Map<String, String> customProperties = new HashMap<>();
                        customProperties.put("databaseId", id);
                        customProperties.put("uniqueKey", id);
                        return ResourcePolicy.builder(entity.getType(), entity.getResource())
                                .withAction(entity.getAction())
                                .withEffect(entity.getEffect())
                                .withScope(entity.getScope())
                                .withPolicyGroup(entity.getPolicyGroup())
                                .withCustomProperties(customProperties)
                                .build();
                    })
                    .collect(Collectors.toList());
            role.setResourcePolicies(resourcePolicies);
        }

        List<RowLevelPolicyEntity> rowLevelPolicyEntities = roleEntity.getRowLevelPolicies();
        if (rowLevelPolicyEntities != null) {
            List<RowLevelPolicy> rowLevelPolicies = rowLevelPolicyEntities.stream()
                    .map(policyEntity -> {
                                String id = policyEntity.getId().toString();
                                Map<String, String> customProperties = new HashMap<>();
                                customProperties.put("databaseId", id);
                                customProperties.put("uniqueKey", id);
                                switch (policyEntity.getType()) {
                                    case JPQL:
                                        return new RowLevelPolicy(policyEntity.getEntityName(),
                                                policyEntity.getWhereClause(),
                                                policyEntity.getJoinClause(),
                                                customProperties);
                                    case PREDICATE:
                                        return new RowLevelPolicy(policyEntity.getEntityName(),
                                                policyEntity.getAction(),
                                                createPredicateFromScript(policyEntity.getScript()),
                                                customProperties);
                                    default:
                                        throw new RuntimeException("Unknown row level policy type " + policyEntity.getType());
                                }
                            }
                    )
                    .collect(Collectors.toList());
            role.setRowLevelPolicies(rowLevelPolicies);
        }
        return role;
    }

    protected Predicate<Object> createPredicateFromScript(String script) {
        return entity -> {
            String modifiedScript = script.replace("{E}", "__entity__");
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("__entity__", entity);
            return (boolean) scriptEvaluator.evaluate(new StaticScriptSource(modifiedScript), arguments);
        };
    }
}