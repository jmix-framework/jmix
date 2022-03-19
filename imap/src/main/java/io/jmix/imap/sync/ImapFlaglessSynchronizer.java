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


package io.jmix.imap.sync;


import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.entity.ImapMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.List;

@Component("imap_FlaglessSynchronizer")
public class ImapFlaglessSynchronizer extends ImapSynchronizer {
    private final static Logger log = LoggerFactory.getLogger(ImapSynchronizer.class);

    protected void handleNewMessages(List<ImapMessage> checkAnswers,
                                     List<ImapMessage> missedMessages,
                                     ImapFolder jmixFolder,
                                     IMAPFolder imapFolder) throws MessagingException {
        ImapMailBox mailBox = jmixFolder.getMailBox();

        Integer lastMessageNumber = imapDataProvider.findLastMessageNumber(jmixFolder);
        if (lastMessageNumber != null) {
            lastMessageNumber = lastMessageNumber - missedMessages.size();
        }

        List<IMAPMessage> imapMessages = imapOperations.search(imapFolder, lastMessageNumber, mailBox);
        if (CollectionUtils.isNotEmpty(imapMessages)) {
            for (IMAPMessage imapMessage : imapMessages) {
                log.debug("[{}]insert message with uid {} to db after changing flags on server",
                        jmixFolder, imapFolder.getUID(imapMessage));
                ImapMessage jmixMessage = createMessage(imapMessage, jmixFolder);
                if (jmixMessage != null && jmixMessage.getReferenceId() != null) {
                    checkAnswers.add(jmixMessage);
                }
            }
        }
    }
}
