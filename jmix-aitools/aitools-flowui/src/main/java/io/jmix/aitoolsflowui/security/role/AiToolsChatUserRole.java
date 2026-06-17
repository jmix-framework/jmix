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

package io.jmix.aitoolsflowui.security.role;

import io.jmix.aitoolsflowui.model.UserAiConversation;
import io.jmix.aitoolsflowui.model.UserAiMessage;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants an end user access to the AI chat: the chat hub and the conversation view, and management
 * of their own conversations and messages (start, continue, rename and delete chats).
 * <p>
 * Policies are granted on the UI models the chat works with; the persistent entities behind them are
 * an internal detail of the data module and are not exposed to this role.
 */
@ResourceRole(name = "AI Tools: chat user", code = AiToolsChatUserRole.CODE, scope = SecurityScope.UI)
public interface AiToolsChatUserRole {

    String CODE = "aitools-chat-user";

    @EntityPolicy(entityClass = UserAiConversation.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = UserAiMessage.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = UserAiConversation.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = UserAiMessage.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void entityPolicies();

    @ViewPolicy(viewClasses = {AiChatHubView.class, AiChatView.class})
    @MenuPolicy(menuIds = "aitls_AiChatHubView")
    void screenPolicies();
}
