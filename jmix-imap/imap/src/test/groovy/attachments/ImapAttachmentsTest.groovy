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

package attachments

import com.icegreen.greenmail.store.StoredMessage
import com.icegreen.greenmail.user.GreenMailUser
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.imap.ImapAttachments
import io.jmix.imap.entity.*
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import test_support.ImapTestConfiguration

import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

//todo restore ImapAttachmentsTest
@Ignore
@ContextConfiguration(classes = [ImapTestConfiguration])
class ImapAttachmentsTest extends Specification {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String USER_PASSWORD = "abcdef123"
    @SuppressWarnings("SpellCheckingInspection")
    private static final String USER_NAME = "hascode"
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost"
    private static final String LOCALHOST = "127.0.0.1"

    private static final String EMAIL_TO = "someone@localhost.com"
    private static final String EMAIL_SUBJECT = "Test E-Mail"
    private static final String EMAIL_TEXT = "This is a test e-mail."
    private static final long EMAIL_UID = 1L

    private static final AtomicInteger counter = new AtomicInteger(0)
    public static final String ATTACHMENT1_CONTENT = "This is attachment 0"
    public static final String ATTACHMENT1_NAME = "Attachment 0.txt"
    public static final String ATTACHMENT2_CONTENT = "This is attachment 1"
    public static final String ATTACHMENT2_NAME = "Attachment-1.txt"

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ImapAttachments imapAttachments

    protected GreenMail mailServer
    protected GreenMailUser user
    protected ImapMailBox mailBoxConfig

    @Autowired
    protected DataManager dataManager;

    void setup() {
        mailServer = new GreenMail(new ServerSetup(3143 + counter.incrementAndGet(), null, ServerSetup.PROTOCOL_IMAP))
        mailServer.start()
        user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD)

        mailBoxConfig = mailbox(mailServer, user)
    }

    void cleanup() {
        mailServer.stop()
    }

    def "fetch attachments of message"() {
        given: "message in INBOX with 2 attachments"
        deliverDefaultMessage()
        ImapMessage message = defaultMessage()
        dataManager.save(message.folder, message)

        when: "fetch attachments of message"
        def attachments = imapAttachments.fetchAttachments(message)

        then: "have all of them"
        attachments.size() == 2
        attachments[0].name == ATTACHMENT1_NAME
        attachments[1].name == ATTACHMENT2_NAME
        attachments.every { it.imapMessage == message }
    }

    def "fetch attachments content"() {
        given: "message in INBOX with 2 attachments and fetch its attachments in place"
        deliverDefaultMessage()
        ImapMessage message = defaultMessage()
        dataManager.save(message.folder, message)

        def attachments = imapAttachments.fetchAttachments(message)

        when: "get stream for content of one attachment"
        def attachment1ContentStream = imapAttachments.openStream(attachments[0])

        then: "content matches"
        IOUtils.toString(attachment1ContentStream, StandardCharsets.UTF_8) == ATTACHMENT1_CONTENT

        when: "get content bytes of one attachment"
        def attachment2ContentBytes = imapAttachments.openStream(attachments[1])

        then: "content matches"
        IOUtils.toString(attachment2ContentBytes, StandardCharsets.UTF_8) == ATTACHMENT2_CONTENT

    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void deliverDefaultMessage() {
        MimeMessage message = new MimeMessage((Session) null)
        message.from = new InternetAddress(EMAIL_TO)
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS))
        message.subject = EMAIL_SUBJECT
        message.text = EMAIL_TEXT

        MimeBodyPart attachment1 = new MimeBodyPart()
        attachment1.setContent(ATTACHMENT1_CONTENT, "text/plain")
        attachment1.fileName = ATTACHMENT1_NAME

        MimeBodyPart attachment2 = new MimeBodyPart()
        attachment2.setContent(ATTACHMENT2_CONTENT, "text/plain")
        attachment2.fileName = ATTACHMENT2_NAME

        Multipart mp = new MimeMultipart()
        mp.addBodyPart(attachment1)
        mp.addBodyPart(attachment2)

        message.content = mp

        user.deliver(new StoredMessage.UidAwareMimeMessage(message, EMAIL_UID, new Date()))
    }

    @SuppressWarnings("GroovyAccessibility")
    ImapMessage defaultMessage() {
        ImapMessage imapMessage = metadata.create(ImapMessage)
        imapMessage.msgUid = EMAIL_UID
        ImapFolder imapFolder = metadata.create(ImapFolder)
        imapFolder.name = "INBOX"
        imapFolder.mailBox = mailBoxConfig
        imapMessage.folder = imapFolder
        imapMessage.msgNum = 1
        imapMessage.caption = EMAIL_SUBJECT

        return imapMessage
    }

    @SuppressWarnings("GroovyAccessibility")
    ImapMailBox mailbox(mailServer, user) {
        ImapMailBox mailBox = metadata.create(ImapMailBox)
        mailBox.host = LOCALHOST
        mailBox.port = mailServer.getImap().port
        mailBox.setAuthenticationMethod(ImapAuthenticationMethod.SIMPLE)
        mailBox.authentication = metadata.create(ImapSimpleAuthentication)
        mailBox.authentication.password = user.password
        mailBox.authentication.username = user.login
        mailBox.name = "$LOCALHOST:${mailBox.port}"

        dataManager.save(mailBox.authentication, mailBox)

        return mailBox
    }
}