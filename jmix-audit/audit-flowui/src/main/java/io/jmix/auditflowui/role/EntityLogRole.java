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

package io.jmix.auditflowui.role;

import io.jmix.audit.entity.EntityLogAttr;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.audit.entity.LoggedAttribute;
import io.jmix.audit.entity.LoggedEntity;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Entity log: configure and view entity logs", code = EntityLogRole.CODE, scope = SecurityScope.UI)
public interface EntityLogRole {
    String CODE = "entity-log";

    @EntityPolicy(entityClass = LoggedEntity.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = LoggedAttribute.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = EntityLogAttr.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = EntityLogItem.class, actions = {EntityPolicyAction.READ})

    @EntityAttributePolicy(entityClass = LoggedEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = LoggedAttribute.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = EntityLogAttr.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = EntityLogItem.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)

    @ViewPolicy(viewIds = {"entityLog.view"})
    @MenuPolicy(menuIds = {"entityLog.view"})
    void entityLog();
}
