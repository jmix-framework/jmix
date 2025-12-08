/*
 * Copyright 2025 Haulmont.
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
package io.jmix.reportsflowui.test_support.role;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test role 4",
        code = TestResourceRole4.CODE,
        scope = SecurityScope.UI,
        description = "Role without access rights")
public interface TestResourceRole4 {
    String CODE = "role-4";

//    @EntityPolicy(entityClass = Report.class, actions = {EntityPolicyAction.DELETE})
//    void entityPolicy();
//
//    @EntityAttributePolicy(entityClass = Report.class, attributes = "*", action = VIEW)
//    void entityAttributePolicy();
}
