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
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityui.model.*;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Multitenancy: administration", code = MultitenancyAdminRole.CODE)
public interface MultitenancyAdminRole {

    String CODE = "multitenancy-admin-role";

    @EntityPolicy(entityClass = Tenant.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = BaseRoleModel.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ResourcePolicyEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ResourceRoleEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = RowLevelPolicyEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = RowLevelRoleEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ResourcePolicyModel.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ResourceRoleModel.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = RowLevelRoleModel.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = RowLevelPolicyModel.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Tenant.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = BaseRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ResourcePolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ResourceRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RowLevelPolicyEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RowLevelRoleEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ResourcePolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ResourceRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RowLevelRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = RowLevelPolicyModel.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)

    @ScreenPolicy(screenIds = {
            "mten_Tenant.browse",
            "mten_Tenant.edit",
            "sec_ResourceRoleModel.browse",
            "sec_ResourceRoleModel.edit",
            "sec_ResourceRoleModel.lookup",
            "sec_RowLevelRoleModel.browse",
            "sec_RowLevelRoleModel.edit",
            "sec_RowLevelRoleModel.lookup",
            "sec_RowLevelPolicyModel.edit"
    })
    @MenuPolicy(menuIds = {
            "mten_Tenant.browse",
            "sec_ResourceRoleModel.browse",
            "sec_ResourceRoleModel.edit",
            "sec_RowLevelRoleModel.browse",
    })
    void multitenancyAccess();

}
