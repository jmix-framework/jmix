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

import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitoolsflowui.view.aiconversation.AiConversationDetailView;
import io.jmix.aitoolsflowui.view.aiconversation.AiConversationListView;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.aitoolsflowui.view.chatmessage.ChatMessageDetailView;
import io.jmix.aitoolsflowui.view.chatmessage.ChatMessageListView;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants full access to the AI Tools add-on: chatting, browsing all conversations and chat
 * messages of every user, and managing them. Intended for administrators.
 */
@ResourceRole(name = "AI Tools: full access", code = AiToolsFullAccessRole.CODE, scope = SecurityScope.UI)
public interface AiToolsFullAccessRole {

    String CODE = "aitools-full-access";

    @EntityPolicy(entityClass = AiConversation.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = ChatMessage.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = AiConversation.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = ChatMessage.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void entityPolicies();

    @ViewPolicy(viewClasses = {
            AiChatHubView.class,
            AiChatView.class,
            AiConversationListView.class,
            AiConversationDetailView.class,
            ChatMessageListView.class,
            ChatMessageDetailView.class
    })
    @MenuPolicy(menuIds = {"aitols_AiChatHubView", "aitols_AiConversation.list"})
    void screenPolicies();
}
