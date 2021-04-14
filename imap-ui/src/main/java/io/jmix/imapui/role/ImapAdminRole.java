/*
 * Copyright 2020 Haulmont.
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

package io.jmix.imapui.role;

import io.jmix.imap.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = ImapAdminRole.CODE, name = "IMAP: administration", scope = SecurityScope.UI)
public interface ImapAdminRole {

    String CODE = "imap-admin";

    @ScreenPolicy(screenIds = {
            "imap_Folder.lookup",
            "imap_MailBox.edit",
            "imap_MailBox.browse",
            "imap_Message.browse",
            "imap_Message.edit",
            "imap_FolderEvent.edit"})
    @MenuPolicy(menuIds = {
            "imap-component",
            "imap_MailBox.browse",
            "imap_Message.browse"})
    @EntityPolicy(entityClass = ImapSimpleAuthentication.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapProxy.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapMessageSync.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapMessageAttachment.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapMessage.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapFolderEvent.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapFolder.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapEventHandler.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ImapMailBox.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = ImapMailBox.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapSimpleAuthentication.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapProxy.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapMessageSync.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapMessageAttachment.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapMessage.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapFolderEvent.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapFolder.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapEventHandler.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ImapMailBox.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    void imapAdmin();
}
