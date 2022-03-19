/*
 * Copyright 2021 Haulmont.
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

package email_cleaner

import io.jmix.core.FileRef
import io.jmix.core.Metadata
import io.jmix.core.TimeSource
import io.jmix.core.UnconstrainedDataManager
import io.jmix.data.PersistenceHints
import io.jmix.email.*
import io.jmix.email.entity.SendingAttachment
import io.jmix.email.entity.SendingMessage
import org.springframework.beans.factory.annotation.Autowired
import test_support.EmailSpecification
import test_support.TestFileStorage

class EmailCleanerTest extends EmailSpecification {
    @Autowired
    EmailCleaner emailCleaner

    @Autowired
    Metadata metadata

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    EmailerProperties emailerProperties

    @Autowired
    TimeSource timeSource

    @Autowired
    TestFileStorage fileStorage

    @Autowired
    EmailDataProvider emailDataProvider

    @Autowired
    Emailer emailer

    def setup() {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, true)
        EmailerConfigPropertiesAccess.setMaxAgeOfImportantMessages(emailerProperties, 1)
        EmailerConfigPropertiesAccess.setMaxAgeOfNonImportantMessages(emailerProperties, 1)
        prepareTestData()
    }

    def 'nothing to delete'() {
        when:
        def amountOfDeletedMessages = emailCleaner.deleteOldEmails()

        then:
        amountOfDeletedMessages == 0
    }

    def 'important messages, their attachments and files from fs should be deleted'() {
        EmailerConfigPropertiesAccess.setCleanFileStorage(emailerProperties, true)

        when:
        def sendingMessages = loadAllSendingMessages().findAll {it.important}
        def fileRefs = collectFileRefs(sendingMessages)
        def importantMessagesToDelete = sendingMessages
                .findAll { it.important }
                .each { it -> it.createTs = Date.from(timeSource.now().minusHours(25).toInstant()) }
                .toArray()
        dataManager.save(importantMessagesToDelete)
        emailCleaner.deleteOldEmails()

        then:
        loadAllSendingMessages().each { !it.important }
        fileRefs.every { (!fileStorage.fileExists(it)) }
    }

    def 'all messages and their attachments should be deleted, except the files from fs'() {
        EmailerConfigPropertiesAccess.setCleanFileStorage(emailerProperties, false)

        when:
        def fileRefs = collectFileRefs(loadAllSendingMessages())
        def messagesToDelete = loadAllSendingMessages()
                .each { it.setCreateTs(Date.from(timeSource.now().minusHours(25).toInstant())) }
                .toArray()
        dataManager.save(messagesToDelete)
        emailCleaner.deleteOldEmails()

        then:
        loadAllSendingMessages().isEmpty()
        fileRefs.every { fileStorage.fileExists(it) }
    }

    private List<SendingMessage> loadAllSendingMessages() {
        return dataManager.load(SendingMessage).all().hint(PersistenceHints.SOFT_DELETION, false).list();
    }

    private static List<FileRef> collectFileRefs(List<SendingMessage> sendingMessages) {
        def fileRefs = sendingMessages.collect { it.contentTextFile }
        fileRefs.addAll(sendingMessages.collect { it.attachments }
                .flatten()
                .collect { (it as SendingAttachment).contentFile })
        return fileRefs
    }

    private void prepareTestData() {
        def messagesToSend = []

        EmailInfo emailInfo1 = createEmailInfo(false)
        addEmailAttachment(emailInfo1)
        messagesToSend << emailInfo1

        EmailInfo emailInfo2 = createEmailInfo(true)
        addEmailAttachment(emailInfo2)
        messagesToSend << emailInfo2

        EmailInfo emailInfo3 = createEmailInfo(false)
        messagesToSend << emailInfo3

        EmailInfo emailInfo4 = createEmailInfo(true)
        messagesToSend << emailInfo4

        messagesToSend.each { emailer.sendEmailAsync(it as EmailInfo) }
    }

    private static EmailInfo createEmailInfo(boolean important) {
        EmailInfoBuilder
                .create("address", "subject", "body")
                .setImportant(important)
                .build()
    }

    private static void addEmailAttachment(EmailInfo emailInfo) {
        List<EmailAttachment> attachments = emailInfo.attachments
        EmailAttachment emailAttachment = new EmailAttachment("someContent".bytes, "someName")
        if (attachments == null) {
            attachments = new ArrayList<>()
            attachments.add(emailAttachment)
            emailInfo.attachments = attachments
        }
        attachments.add(emailAttachment)
    }
}
