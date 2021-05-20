/*
 * Copyright (c) 2008-2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.multitenancy.security.role;

import io.jmix.multitenancy.entity.Tenant;
import io.jmix.multitenancy.entity.TenantAssigmentEntity;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Multitenancy: tenant admin role", code = TenantsAdminRole.CODE, description = "Enables access to multitenancy", scope = SecurityScope.API)
public interface TenantsAdminRole {

    String CODE = "tenant-admin-role";

    @EntityPolicy(entityClass = RoleAssignmentEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ResourceRoleEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = RowLevelRoleEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = Tenant.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TenantAssigmentEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Tenant.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = TenantAssigmentEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RoleAssignmentEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ResourceRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RowLevelRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = {
            "mten_Tenant.browse",
            "mten_Tenant.edit",
            "mten_TenantAssigmentEntity.browse",
            "mten_TenantAssigmentEntity.edit",
            "sec_RoleAssignmentFragment"})
    @MenuPolicy(menuIds = {
            "mten_Tenant.browse",
            "mten_TenantAssigmentEntity.browse"})
    void multitenancyAccess();

}
