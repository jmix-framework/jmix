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

import com.sun.mail.imap.IMAPStore;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.protocol.ThreadExtension;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("imap_ImapHelper")
public class ImapHelper {

    private final static Logger log = LoggerFactory.getLogger(ImapHelper.class);

    protected final ConcurrentMap<UUID, Boolean> supportThreading = new ConcurrentHashMap<>();

    @Autowired
    protected ImapStoreBuilder imapStoreBuilder;

    @Autowired
    protected ImapDataProvider imapDataProvider;

    public IMAPStore getStore(ImapMailBox box) throws MessagingException {
        log.debug("Accessing imap store for {}", box);

        String persistedPassword = imapDataProvider.getPersistedPassword(box);
        return Objects.equals(box.getAuthentication().getPassword(), persistedPassword)
                ? buildStore(box) : buildStore(box, box.getAuthentication().getPassword());
    }

    public Flags jmixFlags(ImapMailBox mailBox) {
        return new Flags(mailBox.getJmixFlag());
    }

    @SuppressWarnings("SameParameterValue")
    boolean supportsThreading(ImapMailBox mailBox) {
        return Boolean.TRUE.equals(supportThreading.get(mailBox.getId()));
    }

    public Body getBody(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/*")) {
            return getSinglePartBody(message);
        } else if (message.isMimeType("multipart/*")) {
            return getMultipartBody(message);
        }

        return EMPTY;
    }

    protected Body getMultipartBody(Part p) throws MessagingException, IOException {
        Object content = p.getContent();
        if (content instanceof InputStream) {
            return new Body(IOUtils.toString((InputStream) p.getContent(), StandardCharsets.UTF_8), false);
        } else if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            Body body = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/html")) {
                    Body b = getSinglePartBody(bp);
                    if (b != EMPTY) {
                        return b;
                    }
                } else if (bp.isMimeType("multipart/*")) {
                    if (body == null || body == EMPTY) {
                        body = getMultipartBody(bp);
                    }
                } else {
                    if (body == null || body == EMPTY) {
                        body = getSinglePartBody(bp);
                    }
                }
            }
            return body;
        }
        return EMPTY;
    }

    protected Body getSinglePartBody(Part p) throws MessagingException, IOException {
        if (!p.isMimeType("text/*") || Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
            return EMPTY;
        }
        Object content = p.getContent();
        String body = content instanceof InputStream
                ? IOUtils.toString((InputStream) p.getContent(), StandardCharsets.UTF_8)
                : content.toString();
        return new Body(body, p.isMimeType("text/html"));
    }

    protected IMAPStore buildStore(ImapMailBox box) throws MessagingException {
        IMAPStore store = imapStoreBuilder.build(box, box.getAuthentication().getPassword(), true);

        supportThreading.put(box.getId(), store.hasCapability(ThreadExtension.CAPABILITY_NAME));
        return store;
    }

    protected IMAPStore buildStore(ImapMailBox box, String password) throws MessagingException {
        IMAPStore store = imapStoreBuilder.build(box, password, false);

        supportThreading.put(box.getId(), store.hasCapability(ThreadExtension.CAPABILITY_NAME));
        return store;
    }

    static boolean canHoldMessages(Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
    }

    static boolean canHoldFolders(Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_FOLDERS) != 0;
    }

    public static class Body {
        protected final String text;
        protected final boolean html;

        Body(String text, boolean html) {
            this.text = text;
            this.html = html;
        }

        public String getText() {
            return text;
        }

        public boolean isHtml() {
            return html;
        }
    }

    protected static final Body EMPTY = new Body("", false);

}
