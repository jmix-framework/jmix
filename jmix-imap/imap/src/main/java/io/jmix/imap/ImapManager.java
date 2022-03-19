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

import io.jmix.imap.dto.ImapConnectResult;
import io.jmix.imap.dto.ImapFolderDto;
import io.jmix.imap.dto.ImapMessageDto;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.flags.ImapFlag;

import java.util.Collection;
import java.util.List;

/**
 * Gateway for E-mail servers communication via IMAP protocol
 * <br>
 * Provides operations for
 * <ul>
 *     <li>
 *     IMAP <strong>mailbox</strong> (in terms of IMAP it is <i>server</i>)
 *     <strong>folders</strong> (in terms of IMAP it is <i>mailbox</i>) retrieval
 *     </li>
 *     <li>
 *     Messages retrieval
 *     </li>
 *     <li>
 *     Messages modification
 *     </li>
 * </ul>
 * <br>
 * Connection details for mailbox are specified in {@link ImapMailBox} object.
 * <br>
 * Folder is uniquely defined by its mailbox and full name (considering tree structure of folders),
 * it is represented by {@link io.jmix.imap.entity.ImapFolder} object
 * <br>
 * Message is uniquely defined by its folder and UID, it is represented by {@link ImapMessage} object,
 * UID is specified in {@link ImapMessage#getMsgUid()} property
 *
 */
public interface ImapManager {

    /**
     * Check a connection for specified mail box
     * @param box mail box to connect
     * @return result of set connection
     */
    ImapConnectResult testConnection(ImapMailBox box);

    /**
     * Retrieve all folders of mailbox preserving tree structure
     *
     * @param mailBox IMAP mailbox connection details
     * @return        root folders of IMAP mailbox, each folder can contain nested child folders forming tree structure
     */
    Collection<ImapFolderDto> fetchFolders(ImapMailBox mailBox);
    /**
     * Retrieve folders of mailbox with specified full names, result is presented in flat structure
     * hiding parent\child relationship
     *
     * @param mailBox       IMAP mailbox connection details
     * @param folderNames   full names of folders to retrieve
     * @return              folders of IMAP mailbox with specified full names,
     *                      result is ordered according to order of names input
     */
    List<ImapFolderDto> fetchFolders(ImapMailBox mailBox, String... folderNames);

    /**
     * Retrieve single message
     *
     * @param message reference object for IMAP message
     * @return        fully fetched message or null if there is no message with such UID in corresponding folder
     * @throws io.jmix.imap.exception.ImapException if wrong folder or mailbox connection details are specified in parameter
     */
    ImapMessageDto fetchMessage(ImapMessage message);


    /**
     * Move message in different folder, if folder is the same - nothing changed,
     * if folder with specified full name doesn't exist - results in throwing {@link io.jmix.imap.exception.ImapException}
     *
     * @param message       reference object for IMAP message
     * @param folderName    full name of new folder
     * @throws io.jmix.imap.exception.ImapException for wrong folder
     */
    void moveMessage(ImapMessage message, String folderName);
    /**
     * Delete message
     *
     * @param message       reference object for IMAP message
     */
    void deleteMessage(ImapMessage message);
    /**
     * Change meta data flag for message, flag can be either standard or custom one
     *
     * @param message       reference object for IMAP message
     * @param flag          flag to change
     * @param set           if true - set the flag, if false - clear the flag
     */
    void setFlag(ImapMessage message, ImapFlag flag, boolean set);
}
