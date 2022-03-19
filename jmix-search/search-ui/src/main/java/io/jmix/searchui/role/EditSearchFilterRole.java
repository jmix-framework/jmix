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

package io.jmix.searchui.role;

import io.jmix.searchui.entity.FullTextFilterCondition;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

/**
 * Grants permissions to add full text search condition to filter
 */
@ResourceRole(name = "Search: edit filter", code = "search-edit-filter", scope = SecurityScope.UI)
public interface EditSearchFilterRole {

    @ScreenPolicy(screenIds = "search_FullTextFilterCondition.edit")
    @EntityPolicy(entityClass = FullTextFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FullTextFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void editSearchFilter();
}