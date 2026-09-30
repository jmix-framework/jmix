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

package mail_send_executor;

import io.jmix.core.DataManager;
import io.jmix.email.Emailer;
import io.jmix.email.EmailerConfigPropertiesAccess;
import io.jmix.email.EmailerProperties;
import io.jmix.email.SendingStatus;
import io.jmix.email.entity.SendingMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EmailTestConfiguration;
import test_support.TestEmailQueueProcessor;
import test_support.TestMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that an application-defined {@code mailSendTaskExecutor} bean, which earlier versions used for sending
 * queued emails, no longer replaces the emailer's own executor.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class, IgnoredMailSendExecutorBeanTest.Config.class})
@Tag("slowTests")
class IgnoredMailSendExecutorBeanTest {

    @Autowired
    Emailer emailer;

    @Autowired
    EmailerProperties emailerProperties;

    @Autowired
    TestMailSender testMailSender;

    @Autowired
    TestEmailQueueProcessor emailQueueProcessor;

    @Autowired
    DataManager dataManager;

    @BeforeEach
    void setUp() {
        EmailerConfigPropertiesAccess.setScheduledSendingDelayCallCount(emailerProperties, 0);
        emailQueueProcessor.processQueuedEmailsAndWait();
        testMailSender.clearBuffer();
    }

    @Test
    void processQueuedEmails_withMailSendTaskExecutorBean_sendsOnOwnExecutor() {
        SendingMessage message = emailer.sendEmailAsync(MailSendExecutorTest.createEmailInfo());

        emailQueueProcessor.processQueuedEmailsAndWait();

        assertEquals(SendingStatus.SENT, dataManager.load(SendingMessage.class).id(message.getId()).one().getStatus());
        assertTrue(testMailSender.getLastSendThreadName().startsWith("mailSendTaskExecutor-"),
                "Unexpected sending thread: " + testMailSender.getLastSendThreadName());
    }

    @Configuration
    static class Config {

        @Bean("mailSendTaskExecutor")
        TaskExecutor mailSendTaskExecutor() {
            return new SyncTaskExecutor();
        }
    }
}
