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

package io.jmix.imap.impl;

import io.jmix.core.Metadata;
import io.jmix.imap.ImapManager;
import io.jmix.imap.dto.ImapConnectResult;
import io.jmix.imap.dto.ImapFolderDto;
import io.jmix.imap.dto.ImapMessageDto;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.exception.ImapException;
import io.jmix.imap.flags.ImapFlag;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.mail.*;
import jakarta.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("imap_ImapManager")
public class ImapManagerImpl implements ImapManager {

    private final static Logger log = LoggerFactory.getLogger(ImapManager.class);

    @Autowired
    protected ImapHelper imapHelper;
    @Autowired
    protected ImapOperations imapOperations;
    @Autowired
    protected Metadata metadata;

    @Override
    public ImapConnectResult testConnection(ImapMailBox box) {
        log.info("Check connection for {}", box);

        ImapConnectResult result = new ImapConnectResult(box);
        try {
            IMAPStore store = imapHelper.getStore(box);
            try {
                result.setAllFolders(imapOperations.fetchFolders(store));
                result.setCustomFlagSupported(imapOperations.supportsCustomFlag(store));
                result.setSuccess(true);
            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            result.setSuccess(false);
            result.setFailure(new ImapException(e));
        }

        return result;
    }

    @Override
    public Collection<ImapFolderDto> fetchFolders(ImapMailBox box) {
        log.debug("fetch folders for box {}", box);

        try {
            IMAPStore store = imapHelper.getStore(box);
            try {
                return imapOperations.fetchFolders(store);

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(e);
        }

    }

    @Override
    public List<ImapFolderDto> fetchFolders(ImapMailBox box, String... folderNames) {
        log.debug("fetch folders {} for box {}", folderNames, box);

        List<ImapFolderDto> allFolders = ImapFolderDto.flattenList(fetchFolders(box));
        for (ImapFolderDto allFolder : allFolders) {
            allFolder.setParent(null);
            allFolder.setChildren(Collections.emptyList());
        }
        if (ArrayUtils.isEmpty(folderNames)) {
            return allFolders;
        }

        Map<String, ImapFolderDto> foldersByFullNames = allFolders.stream()
                .collect(Collectors.toMap(ImapFolderDto::getFullName, Function.identity()));

        return Arrays.stream(folderNames).map(foldersByFullNames::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public ImapMessageDto fetchMessage(ImapMessage message) {
        log.debug("fetch message {}", message);

        return consumeMessage(message, nativeMessage -> {
            ImapMailBox mailBox = message.getFolder().getMailBox();

            return toDto(mailBox, message, nativeMessage);

        }, "fetch and transform message with uid " + message.getMsgUid());
    }

    protected ImapMessageDto toDto(ImapMailBox mailBox, ImapMessage imapMessage, IMAPMessage nativeMessage) throws MessagingException {
        if (nativeMessage == null) {
            return null;
        }

        ImapMessageDto dto = metadata.create(ImapMessageDto.class);
        dto.setUid(imapMessage.getMsgUid());
        dto.setFrom(getAddressList(nativeMessage.getFrom()).get(0));
        dto.setToList(getAddressList(nativeMessage.getRecipients(Message.RecipientType.TO)));
        dto.setCcList(getAddressList(nativeMessage.getRecipients(Message.RecipientType.CC)));
        dto.setBccList(getAddressList(nativeMessage.getRecipients(Message.RecipientType.BCC)));
        dto.setSubject(nativeMessage.getSubject());
        dto.setFlagsList(getFlags(nativeMessage));
        dto.setDate(nativeMessage.getReceivedDate());
        dto.setFolderName(imapMessage.getFolder().getName());
        dto.setMailBox(mailBox);
        try {
            nativeMessage.setPeek(true);
            ImapHelper.Body body = imapHelper.getBody(nativeMessage);
            dto.setBody(body.getText());
            dto.setHtml(body.isHtml());
        } catch (IOException e) {
            log.warn("Can't extract body:", e);
        }
        return dto;
    }

    @Override
    public void deleteMessage(ImapMessage message) {
        log.info("delete message {}", message);
        ImapMailBox mailBox = message.getFolder().getMailBox();

        if (mailBox.getTrashFolderName() != null) {
            doMove(message, mailBox.getTrashFolderName(), mailBox);
        } else {
            consumeMessage(message, msg -> {
                msg.setFlag(Flags.Flag.DELETED, true);
                msg.getFolder().expunge();
                return null;
            }, "Mark message with uid " + message.getMsgUid() + " as DELETED");
        }
    }

    @Override
    public void moveMessage(ImapMessage msg, String folderName) {
        log.info("move message {} to folder {}", msg, folderName);
        ImapMailBox mailBox = msg.getFolder().getMailBox();
        doMove(msg, folderName, mailBox);
    }

    protected void doMove(ImapMessage msg, String newFolderName, ImapMailBox mailBox) {
        String oldFolderName = msg.getFolder().getName();
        if (oldFolderName.equals(newFolderName)) {
            return;
        }

        try {
            IMAPStore store = imapHelper.getStore(mailBox);
            try {
                IMAPFolder oldImapFolder = (IMAPFolder) store.getFolder(oldFolderName);
                oldImapFolder.open(Folder.READ_WRITE);
                IMAPFolder newImapFolder = (IMAPFolder) store.getFolder(newFolderName);
                newImapFolder.open(Folder.READ_WRITE);

                Message m = oldImapFolder.getMessageByUID(msg.getMsgUid());
                oldImapFolder.setFlags(new Message[]{m}, new Flags(Flags.Flag.DELETED), true);
                newImapFolder.appendMessages(new Message[]{m});
                oldImapFolder.expunge();

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(e);
        }

    }

    @Override
    public void setFlag(ImapMessage message, ImapFlag flag, boolean set) {
        log.info("set flag {} for message {} to {}", message, flag, set);
        consumeMessage(message, msg -> {
            msg.setFlags(flag.imapFlags(), set);
            return null;
        }, "Set flag " + flag + " of message with uid " + message.getMsgUid() + " to " + set);
    }

    protected <T> T consumeMessage(ImapMessage msg, ImapFunction<IMAPMessage, T> consumer, String actionDescription) {
        log.trace("perform {} on message {} ", actionDescription, msg);
        ImapMailBox mailBox = msg.getFolder().getMailBox();
        String folderName = msg.getFolder().getName();
        long uid = msg.getMsgUid();

        try {
            IMAPStore store = imapHelper.getStore(mailBox);
            try {
                IMAPFolder imapFolder = (IMAPFolder) store.getFolder(folderName);
                imapFolder.open(Folder.READ_WRITE);
                return consumer.apply((IMAPMessage) imapFolder.getMessageByUID(uid));

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(e);
        }

    }

    protected List<String> getAddressList(Address[] addresses) {
        if (addresses == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(addresses)
                .map(Object::toString)
                .map(address -> {
                    try {
                        return MimeUtility.decodeText(address);
                    } catch (UnsupportedEncodingException e) {
                        return address;
                    }
                }).collect(Collectors.toList());
    }

    protected List<String> getFlags(Message message) throws MessagingException {
        Flags flags = message.getFlags();
        List<String> flagNames = new ArrayList<>();
        if (flags.contains(Flags.Flag.ANSWERED)) {
            flagNames.add("ANSWERED");
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            flagNames.add("DELETED");
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            flagNames.add("DRAFT");
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            flagNames.add("FLAGGED");
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            flagNames.add("RECENT");
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            flagNames.add("SEEN");
        }
        if (flags.contains(Flags.Flag.USER)) {
            flagNames.add("USER");
        }
        if (flags.getUserFlags() != null) {
            Collections.addAll(flagNames, flags.getUserFlags());
        }

        return flagNames;
    }

    @FunctionalInterface
    protected interface ImapFunction<INPUT, OUTPUT> {
        OUTPUT apply(INPUT input) throws MessagingException;
    }

}
