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

package io.jmix.imap.data;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.SaveContext;
import io.jmix.imap.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component("imap_ImapDataProvider")
public class ImapDataProvider {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    public List<ImapMailBox> findMailBoxes() {
        return dataManager.load(ImapMailBox.class)
                .query("select distinct b from imap_MailBox b")
                .fetchPlan("imap-mailbox-edit")
                .list();
    }

    public ImapMailBox findMailBox(UUID id) {
        return dataManager.load(ImapMailBox.class)
                .id(id)
                .fetchPlan("imap-mailbox-edit")
                .optional()
                .orElse(null);
    }

    public String getPersistedPassword(ImapMailBox mailBox) {
        ImapSimpleAuthentication persisted = dataManager.load(ImapSimpleAuthentication.class)
                .id(mailBox.getAuthentication().getId())
                .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.add("password"))
                .optional()
                .orElse(null);
        return persisted != null ? persisted.getPassword() : null;

    }

    public ImapFolder findFolder(ImapFolder folder) {
        return dataManager.load(ImapFolder.class)
                .id(folder.getId())
                .fetchPlan("imap-folder-full")
                .optional()
                .orElse(null);

    }

    public ImapMessage findMessageById(UUID messageId) {
        return dataManager.load(ImapMessage.class)
                .id(messageId)
                .fetchPlan("imap-msg-full")
                .optional()
                .orElse(null);
    }

    public Integer findLastMessageNumber(ImapFolder imapFolder) {
        return dataManager.loadValue("select max(m.msgNum) from imap_Message m where m.folder.id = :folderId", Integer.class)
                .parameter("folderId", imapFolder.getId())
                .optional()
                .orElse(null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ImapMessage findMessageByUid(ImapFolder mailFolder, long messageUid) {
        return dataManager.load(ImapMessage.class)
                .query("select m from imap_Message m where m.msgUid = :msgUid and m.folder.id = :mailFolderId")
                .parameter("mailFolderId", mailFolder.getId())
                .parameter("msgUid", messageUid)
                .fetchPlan("imap-msg-full")
                .optional()
                .orElse(null);
    }

    public ImapMessage findMessageByImapMessageId(ImapMessage imapMessage) {
        return dataManager.load(ImapMessage.class)
                .query("select m from imap_Message m where m.messageId = :imapMessageId and " +
                        "m.folder.id in (select f.id from imap_Folder f where f.mailBox.id = :mailBoxId)")
                .parameter("mailBoxId",  imapMessage.getFolder().getMailBox().getId())
                .parameter("imapMessageId", imapMessage.getReferenceId())
                .fetchPlan("imap-msg-full")
                .optional()
                .orElse(null);
    }

    public Collection<ImapMessageAttachment> findAttachments(ImapMessage imapMessage) {
        return dataManager.load(ImapMessageAttachment.class)
                .query("select a from imap_MessageAttachment a where a.imapMessage.id = :msg")
                .parameter("msg", imapMessage.getId())
                .fetchPlan("imap-msg-attachment-full")
                .list();
    }

    public void saveMessage(ImapMessage message) {
        dataManager.save(message);
    }

    public void saveAttachments(ImapMessage msg, Collection<ImapMessageAttachment> attachments) {
        SaveContext saveContext = new SaveContext();
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(ImapMessageAttachment.class, "imap-msg-attachment-full");
        attachments.forEach(attachment -> {
            attachment.setImapMessage(msg);
            saveContext.saving(attachment, fetchPlan);
        });
        msg.setAttachmentsLoaded(true);
        saveContext.saving(msg,  fetchPlanRepository.getFetchPlan(ImapMessage.class, "imap-msg-full"));

        dataManager.save(saveContext);
    }
}
