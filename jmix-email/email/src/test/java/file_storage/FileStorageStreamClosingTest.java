/*
 * Copyright 2026 Haulmont.
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

package file_storage;

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.*;
import io.jmix.email.entity.SendingMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EmailTestConfiguration;
import test_support.TestFileStorage;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class})
public class FileStorageStreamClosingTest {

    @Autowired
    Emailer emailer;

    @Autowired
    EmailDataProvider emailDataProvider;

    @Autowired
    TestFileStorage fileStorage;

    @Autowired
    EmailerProperties emailerProperties;

    @Autowired
    SystemAuthenticator authenticator;

    boolean previousUseFileStorage;

    @BeforeEach
    void setUp() {
        previousUseFileStorage = emailerProperties.isUseFileStorage();
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, true);
        authenticator.begin();
    }

    @AfterEach
    void tearDown() {
        authenticator.end();
        EmailerConfigPropertiesAccess.setUseFileStorage(emailerProperties, previousUseFileStorage);
    }

    @Test
    void testStorageStreamsAreClosedAfterReading() {
        EmailInfo emailInfo = EmailInfoBuilder.create("recipient@example.com", "Stream closing test", "Body text")
                .setBodyContentType("text/plain; charset=UTF-8")
                .setAttachments(new EmailAttachment(
                        "attachment content".getBytes(StandardCharsets.UTF_8), "attachment.txt"))
                .build();
        SendingMessage message = emailer.sendEmailAsync(emailInfo);

        fileStorage.resetOpenStreamCount();

        List<SendingMessage> messagesToSend = emailDataProvider.loadEmailsToSend();
        assertFalse(messagesToSend.isEmpty());
        assertEquals(0, fileStorage.getOpenStreamCount(),
                "loadEmailsToSend must close all file storage streams");

        String contentText = emailDataProvider.loadContentText(message);
        assertEquals("Body text", contentText);
        assertEquals(0, fileStorage.getOpenStreamCount(),
                "loadContentText must close the file storage stream");
    }
}
