/*
 * Copyright 2026 Haulmont.
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

package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.UiMinimalPolicies;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants editing a resource role in its detail view and no other view, so that the dialog of the localized values
 * opens only as {@link UiMinimalPolicies} grants it. The code has no test prefix, so the role is not among the test
 * roles whose order the tests check.
 */
@ResourceRole(name = "Role editor", code = TestRoleEditorRole.CODE)
public interface TestRoleEditorRole extends UiMinimalPolicies {

    String CODE = "role-editor";

    @EntityPolicy(entityClass = ResourceRoleModel.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = ResourceRoleModel.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @ViewPolicy(viewIds = "sec_ResourceRoleModel.detail")
    void editRoles();
}
