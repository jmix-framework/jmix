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

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.ResourceRoleProvider;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Role provider that gets resource roles from the database from the {@link ResourceRoleEntity}.
 */
@Component("sec_DatabaseResourceRoleProvider")
public class DatabaseResourceRoleProvider extends BaseDatabaseRoleProvider<ResourceRole>
        implements ResourceRoleProvider {

    @Override
    protected Class<?> getRoleClass() {
        return ResourceRoleEntity.class;
    }

    @Override
    protected void buildFetchPlan(FetchPlanBuilder fetchPlanBuilder) {
        fetchPlanBuilder
                .addAll("name", "code", "description", "childRoles", "scopes", "sysTenantId")
                .add("resourcePolicies", FetchPlan.BASE);
    }

    protected ResourceRole buildRole(Object entity) {
        ResourceRoleEntity roleEntity = (ResourceRoleEntity) entity;

        ResourceRole role = new ResourceRole();
        role.setName(roleEntity.getName());
        role.setCode(roleEntity.getCode());
        role.setDescription(roleEntity.getDescription());
        role.setSource(RoleSource.DATABASE);
        role.setChildRoles(roleEntity.getChildRoles());
        role.getCustomProperties().put("databaseId", roleEntity.getId().toString());
        role.setScopes(roleEntity.getScopes() == null ? Collections.emptySet() : roleEntity.getScopes());
        role.setTenantId(roleEntity.getSysTenantId());

        List<ResourcePolicyEntity> resourcePolicyEntities = roleEntity.getResourcePolicies();
        if (resourcePolicyEntities != null) {
            List<ResourcePolicy> resourcePolicies = resourcePolicyEntities.stream()
                    .map(e -> {
                        Map<String, String> customProperties = new HashMap<>();
                        customProperties.put("databaseId", e.getId().toString());
                        return ResourcePolicy.builder(e.getType(), e.getResource())
                                .withAction(e.getAction())
                                .withEffect(e.getEffect())
                                .withPolicyGroup(e.getPolicyGroup())
                                .withCustomProperties(customProperties)
                                .build();
                    })
                    .collect(Collectors.toList());
            role.setResourcePolicies(resourcePolicies);
        }

        return role;
    }
}

