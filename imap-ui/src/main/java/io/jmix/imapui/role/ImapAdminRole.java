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

import io.jmix.imap.role.ImapAdminCoreRole;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = "imap-admin-role", name = "IMAP Admin Role")
public interface ImapAdminRole extends ImapAdminCoreRole {

    @ScreenPolicy(screenIds = {
            "imap_Folder.lookup",
            "imap_MailBox.edit",
            "imap_MailBox.browse",
            "imap_Message.browse",
            "imap_Message.edit",
            "imap_FolderEvent.edit"})
    void screens();

    @MenuPolicy(menuIds = {"administration",
            "imap-component",
            "imap_MailBox.browse",
            "imap_Message.browse"})
    void menus();
}
