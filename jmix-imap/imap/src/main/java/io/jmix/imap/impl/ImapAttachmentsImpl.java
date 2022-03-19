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


import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.imap.ImapAttachments;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.entity.ImapMessageAttachment;
import io.jmix.imap.exception.ImapException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component("imap_ImapAttachments")
public class ImapAttachmentsImpl implements ImapAttachments {
    private final static Logger log = LoggerFactory.getLogger(ImapAttachmentsImpl.class);

    @Autowired
    protected ImapHelper imapHelper;
    @Autowired
    protected ImapDataProvider imapDataProvider;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected Metadata metadata;

    @Override
    public Collection<ImapMessageAttachment> fetchAttachments(ImapMessage message) {
        log.info("fetch attachments for message {}", message);
        ImapMessage msg = imapDataProvider.findMessageById(message.getId());
        if (msg == null) {
            throw new RuntimeException("Can't find msg#" + message.getId());
        }

        if (Boolean.TRUE.equals(msg.getAttachmentsLoaded())) {
            log.debug("attachments for message {} were loaded, reading from database", msg);
            return imapDataProvider.findAttachments(message);
        }

        log.debug("attachments for message {} were not loaded, reading from IMAP server and cache in database", msg);
        ImapMailBox mailBox = msg.getFolder().getMailBox();
        String folderName = msg.getFolder().getName();

        try {
            IMAPStore store = imapHelper.getStore(mailBox);
            try {
                IMAPFolder imapFolder = (IMAPFolder) store.getFolder(folderName);
                imapFolder.open(Folder.READ_ONLY);
                IMAPMessage imapMsg = (IMAPMessage) imapFolder.getMessageByUID(msg.getMsgUid());
                Collection<ImapMessageAttachment> attachments = makeAttachments(imapMsg);

                imapDataProvider.saveAttachments(msg, attachments);
                return attachments;

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(e);
        }

    }

    protected Collection<ImapMessageAttachment> makeAttachments(IMAPMessage msg) throws MessagingException {
        log.debug("make attachments for message {}", msg);

        if (!msg.getContentType().contains("multipart")) {
            return Collections.emptyList();
        }

        Multipart multipart;
        try {
            msg.setPeek(true);
            multipart = (Multipart) msg.getContent();
        } catch (IOException e) {
            log.warn("can't extract attachments:", e);

            return Collections.emptyList();
        }

        List<ImapMessageAttachment> result = new ArrayList<>();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                    StringUtils.isBlank(bodyPart.getFileName())) {
                continue; // dealing with attachments only
            }
            log.trace("processing attachment#{} with name {} for message {}", i, bodyPart.getFileName(), msg);
            ImapMessageAttachment attachment = createImapMessageAttachment(i, bodyPart);
            result.add(attachment);
        }

        return result;
    }

    protected ImapMessageAttachment createImapMessageAttachment(int i, BodyPart bodyPart) throws MessagingException {
        ImapMessageAttachment attachment = metadata.create(ImapMessageAttachment.class);
        String name = bodyPart.getFileName();
        try {
            name = MimeUtility.decodeText(name);
        } catch (UnsupportedEncodingException e) {
            log.warn("Can't decode name of attachment", e);
        }
        attachment.setName(name);
        attachment.setFileSize((long) bodyPart.getSize());
        attachment.setCreatedTs(timeSource.currentTimestamp());
        attachment.setOrderNumber(i);
        return attachment;
    }

    @Override
    public InputStream openStream(ImapMessageAttachment attachment) {
        log.info("Open stream for attachment {}", attachment);
        return new ByteArrayInputStream(loadFile(attachment));
    }

    @Override
    public byte[] loadFile(ImapMessageAttachment attachment) {
        log.info("load attachment {}", attachment);

        ImapMessage msg = attachment.getImapMessage();
        ImapMailBox mailBox = msg.getFolder().getMailBox();
        String folderName = msg.getFolder().getName();

        try {
            IMAPStore store = imapHelper.getStore(mailBox);
            try {
                IMAPFolder imapFolder = (IMAPFolder) store.getFolder(folderName);
                imapFolder.open(Folder.READ_ONLY);
                IMAPMessage imapMessage = (IMAPMessage) imapFolder.getMessageByUID(msg.getMsgUid());
                imapMessage.setPeek(true);
                try {
                    Multipart multipart = (Multipart) imapMessage.getContent();

                    BodyPart imapAttachment = multipart.getBodyPart(attachment.getOrderNumber());

                    return IOUtils.toByteArray(imapAttachment.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException("Can't read content of attachment/message", e);
                }

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(e);
        }
    }
}
