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

package events

import com.icegreen.greenmail.imap.ImapHostManager
import com.icegreen.greenmail.store.StoredMessage
import com.icegreen.greenmail.user.GreenMailUser
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.sun.mail.imap.IMAPFolder
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.imap.ImapEventsTestListener
import io.jmix.imap.ImapManager
import io.jmix.imap.data.ImapDataProvider
import io.jmix.imap.entity.*
import io.jmix.imap.events.*
import io.jmix.imap.flags.ImapFlag
import io.jmix.imap.impl.ImapOperations
import io.jmix.imap.sync.ImapSynchronizer
import io.jmix.imap.sync.events.ImapEvents
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.URLName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import test_support.ImapTestConfiguration

import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.concurrent.atomic.AtomicInteger

//todo restore ImapEventsTest
@Ignore
@ContextConfiguration(classes = [ImapTestConfiguration])
class ImapEventsTest extends Specification {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String USER_PASSWORD = "abcdef123"
    @SuppressWarnings("SpellCheckingInspection")
    private static final String USER_NAME = "hascode"
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost"
    private static final String LOCALHOST = "127.0.0.1"

    private static final String EMAIL_TO = "someone@localhost.com"
    private static final String EMAIL_SUBJECT = "Test E-Mail"
    private static final String EMAIL_TEXT = "This is a test e-mail."
    private static final long START_EMAIL_UID = 1L
    private static final String JMIX_FLAG = "jmix-flag"

    private static final AtomicInteger counter = new AtomicInteger(0)

    @Autowired
    protected ImapEventsTestListener eventListener
    @Autowired
    protected ImapEvents imapEvents
    @Autowired
    protected ImapSynchronizer imapSynchronizer;
    @Autowired
    protected ImapDataProvider imapDataProvider
    @Autowired
    protected ImapManager imapManager;
    @Autowired
    protected DataManager dataManager;

    protected GreenMail mailServer
    protected GreenMailUser user
    protected ImapMailBox mailBoxConfig
    protected ImapFolder INBOX

    @Autowired
    protected Metadata metadata;

    void setup() {
        mailServer = new GreenMail(new ServerSetup(9143 + counter.incrementAndGet(), null, ServerSetup.PROTOCOL_IMAP))
        mailServer.start()
        user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD)

        mailBoxConfig = mailbox(mailServer, user)
    }

    void cleanup() {
        mailServer.stop()
    }

    def "new message events"() {
        given: "3 messages in INBOX, one of which contains our preconfigured custom flag"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID)
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, new Flags(JMIX_FLAG))
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2)
        and: "INBOX is configured to handle new message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.NEW_EMAIL])
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for new messages"
        eventListener.events.clear()
        imapEvents.handleNewMessages(INBOX)

        then: "2 new messages are in database"
        ImapMessage newMessage1 = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID)
        newMessage1 != null
        newMessage1.getImapFlags().contains(JMIX_FLAG)
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1) == null
        ImapMessage newMessage2 = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2)
        newMessage2 != null
        newMessage2.getImapFlags().contains(JMIX_FLAG)

        and: "2 events with type 'NEW_EMAIL' are fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 2
        imapEvents.every { it instanceof NewEmailImapEvent }
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID } == 1
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID + 2 } == 1
    }

    def "folder events are not generated"() {
        given: "3 messages in INBOX, one of which contains our preconfigured custom flag"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID)
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, new Flags(JMIX_FLAG))
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2)
        and: "INBOX is configured not to handle new message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.NEW_EMAIL], false, false)
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for new messages"
        eventListener.events.clear()
        imapEvents.handleNewMessages(INBOX)

        then: "2 new messages are in database"
        ImapMessage newMessage1 = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID)
        newMessage1 != null
        newMessage1.getImapFlags().contains(JMIX_FLAG)
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1) == null
        ImapMessage newMessage2 = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2)
        newMessage2 != null
        newMessage2.getImapFlags().contains(JMIX_FLAG)

        and: "2 events with type 'NEW_EMAIL' are not fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 0
    }

    def "message has been read"() {
        given: "3 messages in INBOX, two of which are seen"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID, new Flags(Flags.Flag.SEEN))
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, new Flags(Flags.Flag.SEEN))
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2)
        and: "INBOX is configured to handle seen message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.EMAIL_SEEN])
        and: "2 messages in database, first of them is marked as seen"
        ImapMessage message1 = defaultMessage(START_EMAIL_UID, EMAIL_SUBJECT + 0, INBOX)
        message1.setImapFlags(new Flags(Flags.Flag.SEEN))
        ImapMessage message2 = defaultMessage(START_EMAIL_UID + 1, EMAIL_SUBJECT + 1, INBOX)
        message2.msgNum = 2
        message2.setImapFlags(new Flags(JMIX_FLAG))
        dataManager.save(message1, message2)

        and: "sync was initialized"
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for modified messages"
        eventListener.events.clear()
        imapEvents.handleChangedMessages(INBOX)

        then: "both messages have 'SEEN' flag in database"
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID).getImapFlags().contains(Flags.Flag.SEEN)
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1).getImapFlags().contains(Flags.Flag.SEEN)

        and: "1 event with type 'EMAIL_SEEN' is fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 1
        imapEvents.every { it instanceof EmailSeenImapEvent }
        imapEvents[0].message.folder == INBOX
        imapEvents[0].message.msgUid == START_EMAIL_UID + 1
    }

    def "message has been answered"() {
        given: "3 messages in INBOX, all of them are answered"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID, new Flags(Flags.Flag.ANSWERED))
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, new Flags(Flags.Flag.ANSWERED))
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2, new Flags(Flags.Flag.ANSWERED))
        and: "INBOX is configured to handle answer message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.NEW_ANSWER])
        and: "2 messages in database, first of them is answered as answered"
        ImapMessage message1 = defaultMessage(START_EMAIL_UID + 1, EMAIL_SUBJECT + 1, INBOX)
        message1.setImapFlags(new Flags(Flags.Flag.ANSWERED))
        message1.msgNum = 2
        ImapMessage message2 = defaultMessage(START_EMAIL_UID + 2, EMAIL_SUBJECT + 2, INBOX)
        message2.msgNum = 3
        message2.setImapFlags(new Flags(JMIX_FLAG))
        dataManager.save(message1, message2)
        and: "sync was initialized"
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for modified messages"
        eventListener.events.clear()
        imapEvents.handleChangedMessages(INBOX)

        then: "both message have 'ANSWERED' flag in database"
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1).getImapFlags().contains(Flags.Flag.ANSWERED)
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2).getImapFlags().contains(Flags.Flag.ANSWERED)

        and: "1 event with type 'NEW_ANSWER' is fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 1
        imapEvents.every { it instanceof EmailAnsweredImapEvent }
        imapEvents[0].message.folder == INBOX
        imapEvents[0].message.msgUid == START_EMAIL_UID + 2
    }

    def "message has been deleted"() {
        given: "1 messages in INBOX"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID)
        and: "INBOX is configured to handle deleted message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.EMAIL_DELETED])
        and: "3 messages in database"
        ImapMessage message1 = defaultMessage(START_EMAIL_UID, EMAIL_SUBJECT + 0, INBOX)
        message1.setImapFlags(new Flags(JMIX_FLAG))
        message1.msgNum = 1
        ImapMessage message2 = defaultMessage(START_EMAIL_UID + 1, EMAIL_SUBJECT + 1, INBOX)
        message2.msgNum = 2
        message2.setImapFlags(new Flags(JMIX_FLAG))
        ImapMessage message3 = defaultMessage(START_EMAIL_UID + 2, EMAIL_SUBJECT + 2, INBOX)
        message3.msgNum = 3
        message2.setImapFlags(new Flags(JMIX_FLAG))
        dataManager.save(message1, message2, message3)

        and: "sync was initialized"
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for missed messages"
        eventListener.events.clear()
        imapEvents.handleMissedMessages(INBOX)

        then: "there is only 1 message remaining in database"
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID) != null
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1) == null
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2) == null

        and: "2 events with type 'EMAIL_DELETED' are fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 2
        imapEvents.every { it instanceof EmailDeletedImapEvent }
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID + 1 } == 1
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID + 2 } == 1
    }

    def "message flags are changed"() {
        given: "1st message in INBOX with SEEN and JMIX flag"
        Flags flags = new Flags(Flags.Flag.SEEN)
        flags.add(JMIX_FLAG)
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID, flags)
        and: "2nd message in INBOX with JMIX flag"
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, new Flags(JMIX_FLAG))
        and: "3rd message in INBOX with ANSWERED, SEEN, JMIX and some other 1 custom flags"
        flags = new Flags(Flags.Flag.SEEN)
        flags.add(JMIX_FLAG)
        flags.add(Flags.Flag.ANSWERED)
        String newCustomFlag = "i-am-new-here"
        flags.add(newCustomFlag)
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2, flags)

        and: "INBOX is configured to handle update message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.FLAGS_UPDATED])

        and: "1st message in database has only JMIX flag"
        ImapMessage message1 = defaultMessage(START_EMAIL_UID, EMAIL_SUBJECT + 0, INBOX)
        message1.setImapFlags(new Flags(JMIX_FLAG))
        dataManager.save(message1)

        and: "2nd message in database has JMIX, FLAGGED and some other old custom flags"
        ImapMessage message2 = defaultMessage(START_EMAIL_UID + 1, EMAIL_SUBJECT + 1, INBOX)
        message2.msgNum = 2
        flags = new Flags(JMIX_FLAG)
        flags.add(Flags.Flag.FLAGGED)
        String oldCustomFlag = "i-am-old"
        flags.add(oldCustomFlag)
        message2.setImapFlags(flags)
        dataManager.save(message2)

        and: "3rd message in database has JMIX, SEEN and FLAGGED flags"
        ImapMessage message3 = defaultMessage(START_EMAIL_UID + 2, EMAIL_SUBJECT + 2, INBOX)
        message3.msgNum = 3
        flags = new Flags(JMIX_FLAG)
        flags.add(Flags.Flag.FLAGGED)
        flags.add(Flags.Flag.SEEN)
        message3.setImapFlags(flags)
        dataManager.save(message3)

        and: "sync was initialized"
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for modified messages"
        eventListener.events.clear()
        imapEvents.handleChangedMessages(INBOX)
        Flags msg1Flags = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID).getImapFlags()
        Flags msg2Flags = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1).getImapFlags()
        Flags msg3Flags = imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2).getImapFlags()
        Collection<BaseImapEvent> imapEvents = eventListener.events

        then: "1st message has JMIX and SEEN flags"
        msg1Flags.contains(Flags.Flag.SEEN)
        msg1Flags.contains(JMIX_FLAG)
        msg1Flags.systemFlags.length == 1
        msg1Flags.userFlags.length == 1

        and: "2nd message has only JMIX flag"
        msg2Flags.contains(JMIX_FLAG)
        msg2Flags.systemFlags.length == 0
        msg2Flags.userFlags.length == 1

        then: "3rd message has JMIX, SEEN, ANSWERED and new custom flags"
        msg3Flags.contains(Flags.Flag.SEEN)
        msg3Flags.contains(Flags.Flag.ANSWERED)
        msg3Flags.contains(JMIX_FLAG)
        msg3Flags.contains(newCustomFlag)
        msg3Flags.systemFlags.length == 2
        msg3Flags.userFlags.length == 2

        and: "3 event with type 'FLAGS_UPDATED' are fired for all messages"
        imapEvents.size() == 3
        imapEvents.every { it instanceof EmailFlagChangedImapEvent }
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID } == 1
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID + 1 } == 1
        imapEvents.count { it.message.folder == INBOX && it.message.msgUid == START_EMAIL_UID + 2 } == 1

        and: "event for 1st message contains only SEEN set"
        Map<ImapFlag, Boolean> msg1ChangedFlags = imapEvents.find { it.message.msgUid == START_EMAIL_UID }.changedFlagsWithNewValue
        msg1ChangedFlags.size() == 1
        msg1ChangedFlags.containsKey(ImapFlag.SEEN)
        msg1ChangedFlags.get(ImapFlag.SEEN)

        and: "event for 2nd message contains unset for FLAGGED and old custom flags"
        Map<ImapFlag, Boolean> msg2ChangedFlags = imapEvents.find { it.message.msgUid == START_EMAIL_UID + 1 }.changedFlagsWithNewValue
        msg2ChangedFlags.size() == 2
        msg2ChangedFlags.containsKey(ImapFlag.IMPORTANT)
        !msg2ChangedFlags.get(ImapFlag.IMPORTANT)
        msg2ChangedFlags.containsKey(new ImapFlag(oldCustomFlag))
        !msg2ChangedFlags.get(new ImapFlag(oldCustomFlag))

        and: "event for 3rd message contains unset for FLAGGED flag and set for ANSWERED and new custom flags"
        Map<ImapFlag, Boolean> msg3ChangedFlags = imapEvents.find { it.message.msgUid == START_EMAIL_UID + 2 }.changedFlagsWithNewValue
        msg3ChangedFlags.size() == 3
        msg3ChangedFlags.containsKey(ImapFlag.IMPORTANT)
        !msg3ChangedFlags.get(ImapFlag.IMPORTANT)
        msg3ChangedFlags.containsKey(new ImapFlag(newCustomFlag))
        msg3ChangedFlags.get(new ImapFlag(newCustomFlag))
        msg3ChangedFlags.containsKey(ImapFlag.ANSWERED)
        msg3ChangedFlags.get(ImapFlag.ANSWERED)
    }

    @SuppressWarnings("GroovyAccessibility")
    def "message has been moved"() {
        given: "other folder and trash folder exist for mailbox"
        ImapHostManager imapHostManager = mailServer.managers.imapHostManager
        imapHostManager.createMailbox(user, "other-folder")
        imapHostManager.createMailbox(user, "trash-folder")
        and: "mailbox has trash folder configured"
        mailBoxConfig.trashFolderName = "trash-folder"

        dataManager.save(mailBoxConfig)

        and: "INBOX is configured to handle moved and deleted message events"
        INBOX = inbox(mailBoxConfig, [ImapEventType.EMAIL_MOVED, ImapEventType.EMAIL_DELETED], true)
        and: "other folder is configured"
        def otherFolder = imapFolder(mailBoxConfig, "other-folder", true)
        and: "1 message in INBOX, 1 message in other folder and 1 message in trash-folder folder"
        deliverDefaultMessage(EMAIL_SUBJECT + 0, START_EMAIL_UID)
        deliverDefaultMessage(EMAIL_SUBJECT + 1, START_EMAIL_UID + 1, null, "moved-message")
        deliverDefaultMessage(EMAIL_SUBJECT + 2, START_EMAIL_UID + 2, null, "deleted-message")
        def imapMessages = getDefaultMessages("INBOX")

        ImapMessage message1 = defaultMessage(START_EMAIL_UID, EMAIL_SUBJECT + 0, INBOX)
        message1.msgNum = 1
        ImapMessage message2 = defaultMessage(START_EMAIL_UID + 1, EMAIL_SUBJECT + 1, INBOX)
        message2.msgNum = 2
        message2.messageId = imapMessages.find { it.messageNumber == 2 }.getHeader(ImapOperations.MESSAGE_ID_HEADER)[0]
        ImapMessage message3 = defaultMessage(START_EMAIL_UID + 2, EMAIL_SUBJECT + 2, INBOX)
        message3.msgNum = 3
        message3.messageId = imapMessages.find { it.messageNumber == 3 }.getHeader(ImapOperations.MESSAGE_ID_HEADER)[0]
        dataManager.save(message1, message2, message3)

        imapManager.moveMessage(message2, "other-folder")
        imapManager.moveMessage(message3, "trash-folder")

        INBOX.deleted = false
        otherFolder.deleted = false
        dataManager.save(INBOX, otherFolder)


        INBOX = dataManager.load(ImapFolder)
                .id(INBOX.getId())
                .fetchPlan("imap-folder-full")
                .one();

        and: "sync was initialized"
        imapEvents.init(mailBoxConfig)
        imapSynchronizer.synchronize(mailBoxConfig)

        when: "check for missed messages"
        eventListener.events.clear()
        imapEvents.handleMissedMessages(INBOX)

        then: "there is only 1 message remaining in database"
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID) != null
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 1) == null
        imapDataProvider.findMessageByUid(INBOX, START_EMAIL_UID + 2) == null

        and: "1 event with type 'EMAIL_DELETED' and 1 event with type 'EMAIL_MOVED' are fired"
        def imapEvents = eventListener.events
        imapEvents.size() == 2
        imapEvents.count {
            it instanceof EmailDeletedImapEvent &&
                    it.message.folder == INBOX &&
                    it.message.msgUid == START_EMAIL_UID + 2
        } == 1
        imapEvents.count {
            it instanceof EmailMovedImapEvent &&
                    it.message.folder.name == "other-folder" &&
                    it.message.msgUid == START_EMAIL_UID &&
                    it.oldFolder.name == "INBOX"
        } == 1
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void deliverDefaultMessage(subject, uid, flags = null, messageId = null) {
        MimeMessage message = new MimeMessage((Session) null)
        message.from = new InternetAddress(EMAIL_TO)
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS))
        message.subject = subject
        message.text = EMAIL_TEXT
        if (flags != null) {
            message.setFlags(flags, true)
        }
        if (messageId != null) {
            message.setHeader(ImapOperations.MESSAGE_ID_HEADER, messageId)
        }

        user.deliver(new StoredMessage.UidAwareMimeMessage(message, uid, new Date()))
    }

    @SuppressWarnings("GroovyAccessibility")
    ImapMessage defaultMessage(uid, subject, folder = INBOX) {
        ImapMessage imapMessage = metadata.create(ImapMessage)
        imapMessage.msgUid = uid

        imapMessage.msgNum = 1
        imapMessage.caption = subject
        imapMessage.folder = folder

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
        mailBox.jmixFlag = JMIX_FLAG
        mailBox.name = "$LOCALHOST:${mailBox.port}"
        mailBox.flagsSupported = true

        dataManager.save(mailBox.authentication, mailBox)

        return mailBox
    }

    @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyAccessibility"])
    ImapFolder inbox(ImapMailBox mailBox, eventTypes, deleted = false, enabled = true) {
        ImapFolder imapFolder = imapFolder(mailBox, "INBOX", deleted)

        def events = eventTypes.collect {
            ImapFolderEvent event = metadata.create(ImapFolderEvent)
            event.folder = imapFolder
            event.event = it
            event.eventHandlers = Collections.emptyList()
            event.enabled = enabled

            return event
        }

        events.forEach{dataManager.save(it)}

        imapFolder.setEvents(events)

        return imapFolder
    }

    @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyAccessibility"])
    ImapFolder imapFolder(ImapMailBox mailBox, folderName, deleted = false) {
        ImapFolder imapFolder = metadata.create(ImapFolder)
        imapFolder.name = folderName
        imapFolder.mailBox = mailBox
        imapFolder.enabled = true
        imapFolder.deleted = deleted

        dataManager.save(imapFolder)

        if (mailBox.folders == null) {
            mailBox.folders = new ArrayList<>()
        }
        mailBox.folders.add(imapFolder)

        return imapFolder
    }


    ///

    Message[] getDefaultMessages(folderName) {
        Folder folder = getDefaultImapFolder(folderName)
        return folder.getMessages()
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    IMAPFolder getDefaultImapFolder(folderName) {
        Properties props = new Properties()
        Session session = Session.getInstance(props)
        URLName urlName = new URLName("imap", LOCALHOST,
                mailBoxConfig.port, null, user.getLogin(),
                user.getPassword())
        Store store = session.getStore(urlName)
        store.connect()
        IMAPFolder folder = store.getFolder(folderName)
        folder.open(Folder.READ_ONLY)
        return folder
    }
}