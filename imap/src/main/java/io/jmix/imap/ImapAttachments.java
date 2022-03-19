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


package io.jmix.imap;


import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.entity.ImapMessageAttachment;

import java.io.InputStream;
import java.util.Collection;

/**
 * Provide operations to load IMAP message attachments
 */
public interface ImapAttachments {

    /**
     * Retrieve and cache attachments for message
     *
     * @param message reference object for IMAP message
     * @return        reference objects for message attachments
     */
    Collection<ImapMessageAttachment> fetchAttachments(ImapMessage message);

    /**
     * Return an input stream to load a message attachment content
     * @param attachment            IMAP message attachment reference object
     * @return                      input stream, must be closed after use
     */
    InputStream openStream(ImapMessageAttachment attachment);

    /**
     * Load a file message attachment content into byte array.
     * @param attachment            IMAP message attachment reference object
     * @return                      attachment content
     */
    byte[] loadFile(ImapMessageAttachment attachment);
}
