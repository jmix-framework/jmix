/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.security.roles;

import com.haulmont.cuba.security.entity.SearchFolder;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "CUBA compatibility: creating and editing search folders",
        code = "search-folders",
        scope = SecurityScope.UI)
public interface CubaSearchFoldersRole {

    @EntityPolicy(entityClass = SearchFolder.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = SearchFolder.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void entityPermissions();
}
