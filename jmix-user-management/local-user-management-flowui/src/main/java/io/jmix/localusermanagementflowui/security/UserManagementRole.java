/*
 * Copyright 2022 Haulmont.
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

package io.jmix.localusermanagementflowui.security;

import io.jmix.localusermanagement.entity.UserSubstitutionEntity;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "UserManagement", code = UserManagementRole.CODE)
public interface UserManagementRole {
    String CODE = "user-management";

    @ViewPolicy(viewIds = {"changePasswordView", "sec_UserSubstitution.detail", "sec_UserSubstitution.view", "resetPasswordView"})
    void screens();

    @EntityAttributePolicy(entityClass = UserSubstitutionEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = UserSubstitutionEntity.class, actions = EntityPolicyAction.ALL)
    void userSubstitutionEntity();
}