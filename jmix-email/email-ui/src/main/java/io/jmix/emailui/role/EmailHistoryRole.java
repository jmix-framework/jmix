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

package io.jmix.emailui.role;

import io.jmix.email.entity.SendingMessage;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = EmailHistoryRole.CODE, name = "Email: view history", scope = SecurityScope.UI)
public interface EmailHistoryRole {

    String CODE = "email-history";

    @ScreenPolicy(screenIds = {"email_SendingMessage.browse", "ResendMessage"})
    @MenuPolicy(menuIds = {"email_SendingMessage.browse"})
    @EntityPolicy(entityClass = SendingMessage.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = SendingMessage.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void emailHistory();
}
