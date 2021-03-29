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

package test_support;

import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class})
@Tag("slowTests")
public class EmailerTest {

    @Autowired
    private Emailer emailer;

    @Autowired
    private EmailDataProvider emailDataProvider;

    @Autowired
    private TestMailSender testMailSender;

    @Autowired
    private TimeSource timeSource;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EmailerProperties emailerProperties;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @Autowired
    private Metadata metadata;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @BeforeEach
    public void setUp() throws Exception {
        EmailerConfigPropertiesAccess.setScheduledSendingDelayCallCount(emailerProperties, 0);

        // send pending emails which might be in the queue
        emailer.processQueuedEmails();
        testMailSender.clearBuffer();
    }

    @Test
    public void testSynchronous() throws Exception {
        doTestSynchronous(false);
    }

    @Test
    public void testSynchronousWithMultipleRecipients() throws Exception {
        doTestWithMultipleRecipientsSynchronous(false);
    }

    @Test
    public void testSynchronousFS() throws Exception {
        doTestSynchronous(true);
    }

    /*
     * Test single recipient, text body, subject.
     */
    private void doTestSynchronous(boolean useFs) throws Exception {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("testemail@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .build();
        emailer.sendEmail(myInfo);

        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertEquals(1, msg.getAllRecipients().length);
        assertEquals("testemail@example.com", msg.getAllRecipients()[0].toString());

        assertEquals("Test Email", msg.getSubject());
        assertEquals("Test Body", getBody(msg));
        assertTrue(getBodyContentType(msg).startsWith("text/plain;"));
    }

    /*
     * Test single recipient, text body, subject.
     */
    private void doTestWithMultipleRecipientsSynchronous(boolean useFs) throws Exception {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("testemail@example.com,testemail2@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .setCc("testemail3@example.com,testemail4@example.com")
                .setBcc("testemail5@example.com,testemail6@example.com")
                .build();
        emailer.sendEmail(myInfo);

        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertEquals(2, msg.getRecipients(Message.RecipientType.TO).length);
        assertEquals("testemail@example.com", msg.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("testemail2@example.com", msg.getRecipients(Message.RecipientType.TO)[1].toString());

        assertEquals(2, msg.getRecipients(Message.RecipientType.CC).length);
        assertEquals("testemail3@example.com", msg.getRecipients(Message.RecipientType.CC)[0].toString());
        assertEquals("testemail4@example.com", msg.getRecipients(Message.RecipientType.CC)[1].toString());

        assertEquals(2, msg.getRecipients(Message.RecipientType.BCC).length);
        assertEquals("testemail5@example.com", msg.getRecipients(Message.RecipientType.BCC)[0].toString());
        assertEquals("testemail6@example.com", msg.getRecipients(Message.RecipientType.BCC)[1].toString());

        assertEquals("Test Email", msg.getSubject());
        assertEquals("Test Body", getBody(msg));
        assertTrue(getBodyContentType(msg).startsWith("text/plain;"));
    }

    /*
     * Test EmailInfoBuilder.create() with parameter list.
     */
    @Test
    public void testSimpleParamList() throws Exception {
        testMailSender.clearBuffer();

        EmailInfo emailInfo = EmailInfoBuilder.create("myemail@example.com", "Test Email", "Test Body 2").build();
        emailer.sendEmail(emailInfo);

        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertEquals(1, msg.getAllRecipients().length);
        assertEquals("myemail@example.com", msg.getAllRecipients()[0].toString());

        assertEquals("Test Email", msg.getSubject());
        assertEquals("Test Body 2", getBody(msg));
        assertTrue(getBodyContentType(msg).startsWith("text/plain;"));
    }

    @Test
    public void testAsynchronous() throws Exception {
        doTestAsynchronous(false);
    }

    @Test
    public void testAsynchronousFS() throws Exception {
        doTestAsynchronous(true);
    }

    @Test
    public void testFileStorageEmailBodyReturningToDbColumn() {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, true);
        testMailSender.clearBuffer();

        String body = "Test Email Body";
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(myInfo);
        assertNotNull(message);

        // not sent yet
        SendingMessage sendingMsg = reload(message, "sendingMessage.loadFromQueue");
        assertNotNull(sendingMsg.getContentTextFile());
        assertNull(sendingMsg.getContentText());             // null

        // run scheduler
        emailer.processQueuedEmails();

        sendingMsg = reload(message, "sendingMessage.loadFromQueue");
        assertNotNull(sendingMsg.getContentTextFile());
        assertNull(sendingMsg.getContentText());             // null??
    }

    private void doTestAsynchronous(boolean useFs) throws Exception {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        String body = "Test Email Body";
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(myInfo);
        assertNotNull(message);

        // not sent yet
        assertTrue(testMailSender.isEmpty());
        SendingMessage sendingMsg = reload(message);
        assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

        // run scheduler
        emailer.processQueuedEmails();

        // check
        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertEquals(1, msg.getAllRecipients().length);
        assertEquals("recipient@example.com", msg.getAllRecipients()[0].toString());

        assertEquals("Test", msg.getSubject());
        assertEquals(body, getBody(msg));
        assertTrue(getBodyContentType(msg).startsWith("text/plain;"));

        sendingMsg = reload(message);
        assertEquals(SendingStatus.SENT, sendingMsg.getStatus());
    }

    @Test
    public void testHtmlContent() throws Exception {
        testMailSender.clearBuffer();

        String body = "<html><body><b>Hi</b></body></html>";
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        emailer.sendEmail(myInfo);

        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertTrue(getBodyContentType(msg).startsWith("text/html;"));
    }

    @Test
    public void testImplicitFromAddress() throws Exception {
        EmailInfo myInfo;

        // synchronous
        EmailerConfigPropertiesAccess.setFromAddress(emailerProperties, "implicit@example.com");
        myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .build();
        emailer.sendEmail(myInfo);
        assertEquals("implicit@example.com", testMailSender.fetchSentEmail().getFrom()[0].toString());

        // asynchronous
        EmailerConfigPropertiesAccess.setFromAddress(emailerProperties, "implicit2@example.com");
        myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .build();
        emailer.sendEmailAsync(myInfo);

        emailer.processQueuedEmails();
        assertEquals("implicit2@example.com", testMailSender.fetchSentEmail().getFrom()[0].toString());
    }

    @Test
    public void testExplicitFromAddress() throws Exception {
        EmailInfo myInfo;
        MimeMessage msg;

        // synchronous
        myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .setFrom("explicit@example.com")
                .build();
        emailer.sendEmail(myInfo);
        msg = testMailSender.fetchSentEmail();
        assertEquals("explicit@example.com", msg.getFrom()[0].toString());

        // asynchronous
        myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test Email")
                .setBody("Test Body")
                .setFrom("explicit2@example.com")
                .build();
        emailer.sendEmailAsync(myInfo);

        emailer.processQueuedEmails();
        msg = testMailSender.fetchSentEmail();
        assertEquals("explicit2@example.com", msg.getFrom()[0].toString());
    }

    @Test
    public void testSynchronousFail() throws Exception {
        testMailSender.clearBuffer();

        testMailSender.failPlease();
        try {
            EmailInfo emailInfo = EmailInfoBuilder.create("myemail@example.com", "Test Email", "Test Body 2").build();
            emailer.sendEmail(emailInfo);
            fail("Must fail with EmailException");
        } catch (EmailException e) {
            assertEquals(1, e.getFailedAddresses().size());
            assertEquals("myemail@example.com", e.getFailedAddresses().get(0));
            assertTrue(testMailSender.isEmpty());
        } finally {
            testMailSender.workNormallyPlease();
        }
    }

    @Test
    public void testAsynchronousAttemptLimit() throws Exception {
        testMailSender.clearBuffer();

        String body = "Test Email Body";
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(myInfo, 2, getDeadlineWhichDoesntMatter());
        assertNotNull(message);

        // not sent yet
        assertTrue(testMailSender.isEmpty());
        SendingMessage sendingMsg = reload(message);
        assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

        // will fail
        testMailSender.failPlease();
        try {
            // try once
            emailer.processQueuedEmails();
            sendingMsg = reload(sendingMsg);
            assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

            // try second time
            emailer.processQueuedEmails();
            sendingMsg = reload(sendingMsg);
            assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

        } finally {
            testMailSender.workNormallyPlease();
        }

        // marks as not-sent in the next tick
        emailer.processQueuedEmails();
        sendingMsg = reload(sendingMsg);
        assertEquals(SendingStatus.NOT_SENT, sendingMsg.getStatus());
        assertEquals(2, sendingMsg.getAttemptsLimit().intValue());
    }

    @Test
    public void testSentFromSecondAttempt() throws Exception {
        doTestSentFromSecondAttempt(false);
    }

    @Test
    public void testSentFromSecondAttemptFS() throws Exception {
        doTestSentFromSecondAttempt(true);
    }

    private void doTestSentFromSecondAttempt(boolean useFs) {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        String body = "Test Email Body";
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(myInfo, 2, getDeadlineWhichDoesntMatter());
        assertNotNull(message);

        // not sent yet
        assertTrue(testMailSender.isEmpty());
        SendingMessage sendingMsg = reload(message);
        assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

        // will fail
        testMailSender.failPlease();
        try {
            // try once
            emailer.processQueuedEmails();
            sendingMsg = reload(sendingMsg);
            assertEquals(SendingStatus.QUEUE, sendingMsg.getStatus());

        } finally {
            testMailSender.workNormallyPlease();
        }

        // success now
        emailer.processQueuedEmails();
        sendingMsg = reload(sendingMsg);
        assertEquals(SendingStatus.SENT, sendingMsg.getStatus());
        assertEquals(2, sendingMsg.getAttemptsLimit().intValue());
    }

    @Test
    public void testSeveralRecipients() throws Exception {
        doTestSeveralRecipients(false);
    }

    @Test
    public void testSeveralRecipientsFS() throws Exception {
        doTestSeveralRecipients(true);
    }

    private void doTestSeveralRecipients(boolean useFs) throws MessagingException {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        String body = "Test Email Body";
        String recipients = "misha@example.com,kolya@example.com;tanya@example.com;"; // 3 recipients
        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses(recipients)
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(myInfo);
        assertNotNull(message);

        assertTrue(testMailSender.isEmpty());
        emailer.processQueuedEmails();
        
        // check
        assertEquals(1, testMailSender.getBufferSize());
        MimeMessage msg = testMailSender.fetchSentEmail();

        assertEquals(3, msg.getAllRecipients().length);
        assertEquals("misha@example.com", msg.getAllRecipients()[0].toString());
        assertEquals("kolya@example.com", msg.getAllRecipients()[1].toString());
        assertEquals("tanya@example.com", msg.getAllRecipients()[2].toString());
    }

    @Test
    public void testSendAllToAdmin() throws Exception {
        EmailerConfigPropertiesAccess.setSendAllToAdmin(emailerProperties, true);
        EmailerConfigPropertiesAccess.setAdminAddress(emailerProperties, "admin@example.com");
        try {
            EmailInfo emailInfo1 = EmailInfoBuilder.create("michael@example.com", "Test Email 5", "Test Body 5").build();
            emailer.sendEmail(emailInfo1);

            EmailInfo emailInfo2 = EmailInfoBuilder.create("nikolay@example.com", "Test Email 6", "Test Body 6").build();
            emailer.sendEmailAsync(emailInfo2);
            emailer.processQueuedEmails();

            for (int i = 0; i < 2; i++) {
                MimeMessage msg = testMailSender.fetchSentEmail();
                assertEquals(1, msg.getAllRecipients().length);
                assertEquals("admin@example.com", msg.getAllRecipients()[0].toString());
            }

        } finally {
            EmailerConfigPropertiesAccess.setSendAllToAdmin(emailerProperties, false);
        }
    }

    @Test
    public void testTextAttachment() throws Exception {
        doTestTextAttachment(false);
    }

    @Test
    public void testTextAttachmentFS() throws Exception {
        doTestTextAttachment(true);
    }

    private void doTestTextAttachment(boolean useFs) throws IOException, MessagingException {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        String attachmentText = "Test Attachment Text";
        EmailAttachment textAttach = EmailAttachment.createTextAttachment(attachmentText, "ISO-8859-1", "test.txt");

        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test")
                .setBody("Test")
                .setAttachments(textAttach)
                .build();
        emailer.sendEmailAsync(myInfo);

        emailer.processQueuedEmails();

        MimeMessage msg = testMailSender.fetchSentEmail();
        MimeBodyPart firstAttachment = getFirstAttachment(msg);

        // check content bytes
        Object content = firstAttachment.getContent();
        assertTrue(content instanceof InputStream);
        byte[] data = IOUtils.toByteArray((InputStream) content);
        assertEquals(attachmentText, new String(data, StandardCharsets.ISO_8859_1));

        // disposition
        assertEquals(Part.ATTACHMENT, firstAttachment.getDisposition());

        // charset header
        String contentType = firstAttachment.getContentType();
        assertTrue(contentType.toLowerCase().contains("charset=iso-8859-1"));
    }

    @Test
    public void testInlineImage() throws Exception {
        doTestInlineImage(false);
    }

    @Test
    public void testInlineImageFS() throws Exception {
        doTestInlineImage(true);
    }

    private void doTestInlineImage(boolean useFs) throws IOException, MessagingException {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        String fileName = "logo.png";
        EmailAttachment imageAttach = new EmailAttachment(imageBytes, fileName, "logo");

        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test")
                .setBody("Test")
                .setAttachments(imageAttach)
                .build();
        emailer.sendEmailAsync(myInfo);

        emailer.processQueuedEmails();

        MimeMessage msg = testMailSender.fetchSentEmail();
        MimeBodyPart attachment = getInlineAttachment(msg);

        // check content bytes
        InputStream content = (InputStream) attachment.getContent();
        byte[] data = IOUtils.toByteArray(content);
        assertByteArrayEquals(imageBytes, data);

        // disposition
        assertEquals(Part.INLINE, attachment.getDisposition());

        // mime type
        String contentType = attachment.getContentType();
        assertTrue(contentType.contains("image/png"));
    }

    @Test
    public void testPdfAttachment() throws Exception {
        doTestPdfAttachment(false);
    }

    @Test
    public void testPdfAttachmentFS() throws Exception {
        doTestPdfAttachment(true);
    }

    private void doTestPdfAttachment(boolean useFs) throws IOException, MessagingException {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);
        testMailSender.clearBuffer();

        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 6};
        String fileName = "invoice.pdf";
        EmailAttachment pdfAttach = new EmailAttachment(pdfBytes, fileName);

        EmailInfo myInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test")
                .setBody("Test")
                .setAttachments(pdfAttach)
                .build();
        emailer.sendEmailAsync(myInfo);

        emailer.processQueuedEmails();

        MimeMessage msg = testMailSender.fetchSentEmail();
        MimeBodyPart attachment = getFirstAttachment(msg);

        // check content bytes
        InputStream content = (InputStream) attachment.getContent();
        byte[] data = IOUtils.toByteArray(content);
        assertByteArrayEquals(pdfBytes, data);

        // disposition
        assertEquals(Part.ATTACHMENT, attachment.getDisposition());

        // mime type
        String contentType = attachment.getContentType();
        assertTrue(contentType.contains("application/pdf"));
    }

    @Test
    public void testLoadBody() throws Exception {
        doTestLoadBody(false);
    }

    @Test
    public void testLoadBodyFS() throws Exception {
        doTestLoadBody(true);
    }

    private void doTestLoadBody(boolean useFs) throws Exception {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, useFs);

        String body = "Hi! This is test email. Bye.";
        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test")
                .setBody(body)
                .build();
        SendingMessage message = emailer.sendEmailAsync(emailInfo);

        SendingMessage msg = reload(message);

        String actualBody = emailDataProvider.loadContentText(msg);
        assertEquals(body, actualBody);
    }

    @Test
    public void testMigration() throws Exception {
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, false);

        byte[] expectedBytes = new byte[]{1, 2, 3, 4, 6};
        EmailAttachment fileAttachment = new EmailAttachment(expectedBytes, "invoice.pdf");

        String body = "Hi! This is test email. Bye.";
        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses("test@example.com")
                .setSubject("Test")
                .setBody(body)
                .setAttachments(fileAttachment)
                .build();

        SendingMessage message = emailer.sendEmailAsync(emailInfo);
        SendingMessage msg;
        SendingAttachment attachment;

        // check DB storage
        msg = reload(message, "sendingMessage.loadFromQueue");
        attachment = msg.getAttachments().get(0);

        assertNotNull(msg.getContentText());
        assertNull(msg.getContentTextFile());
        assertNotNull(attachment.getContent());
        assertNull(attachment.getContentFile());

        emailDataProvider.migrateEmailsToFileStorage(Lists.newArrayList(msg));
        emailDataProvider.migrateAttachmentsToFileStorage(Lists.newArrayList(attachment));

        // check file storage
        msg = reload(msg, "sendingMessage.loadFromQueue");
        attachment = msg.getAttachments().get(0);

        assertNull(msg.getContentText());
        assertNotNull(msg.getContentTextFile());
        assertEquals(body, emailDataProvider.loadContentText(msg));

        assertNull(attachment.getContent());
        assertNotNull(attachment.getContentFile());
        FileStorage fileStorage = fileStorageLocator.getDefault();
        byte[] actualBytes = IOUtils.toByteArray(fileStorage.openStream(attachment.getContentFile()));
        assertByteArrayEquals(expectedBytes, actualBytes);
    }

    /* Utility */
    private Date getDeadlineWhichDoesntMatter() {
        return DateUtils.addHours(timeSource.currentTimestamp(), 2);
    }

    private String getBody(MimeMessage msg) throws Exception {
        MimeBodyPart textPart = getTextPart(msg);
        return (String) textPart.getContent();
    }

    private String getBodyContentType(MimeMessage msg) throws Exception {
        MimeBodyPart textPart = getTextPart(msg);
        return textPart.getContentType();
    }

    private MimeBodyPart getTextPart(MimeMessage msg) throws IOException, MessagingException {
        assertTrue(msg.getContent() instanceof MimeMultipart);
        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();

        Object content2 = mimeMultipart.getBodyPart(0).getContent();
        assertTrue(content2 instanceof MimeMultipart);
        MimeMultipart textBodyPart = (MimeMultipart) content2;

        return (MimeBodyPart) textBodyPart.getBodyPart(0);
    }

    private MimeBodyPart getFirstAttachment(MimeMessage msg) throws IOException, MessagingException {
        assertTrue(msg.getContent() instanceof MimeMultipart);
        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
        return (MimeBodyPart) mimeMultipart.getBodyPart(1);
    }

    private MimeBodyPart getInlineAttachment(MimeMessage msg) throws IOException, MessagingException {
        assertTrue(msg.getContent() instanceof MimeMultipart);
        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();

        Object content2 = mimeMultipart.getBodyPart(0).getContent();
        assertTrue(content2 instanceof MimeMultipart);
        MimeMultipart textBodyPart = (MimeMultipart) content2;

        return (MimeBodyPart) textBodyPart.getBodyPart(1);
    }

    private SendingMessage reload(SendingMessage sendingMessage) {
        return reload(sendingMessage, null);
    }

    private SendingMessage reload(SendingMessage sendingMessage, String fetchPlanName) {
        MetaClass metaClass = metadata.getClass(SendingMessage.class);
        LoadContext<SendingMessage> loadContext = new LoadContext<>(metaClass);
        loadContext.setId(sendingMessage.getId());
        if (StringUtils.isNotEmpty(fetchPlanName)) {
            FetchPlan fetchPlan = fetchPlanRepository.findFetchPlan(metaClass, fetchPlanName);
            if (fetchPlan != null) {
                loadContext.setFetchPlan(fetchPlan);
            }
        }
        return dataManager.load(loadContext);
    }

    private void assertByteArrayEquals(byte[] expected, byte[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}