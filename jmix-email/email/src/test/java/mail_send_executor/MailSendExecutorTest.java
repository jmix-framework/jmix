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
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.email.EmailerConfigPropertiesAccess;
import io.jmix.email.EmailerProperties;
import io.jmix.email.SendingStatus;
import io.jmix.email.entity.SendingMessage;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EmailTestConfiguration;
import test_support.TestEmailQueueProcessor;
import test_support.TestMailSender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that the emailer sends queued emails with its own executor without exposing it as a bean, while keeping
 * the thread names and metrics of the former {@code mailSendTaskExecutor} bean.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class})
@Tag("slowTests")
class MailSendExecutorTest {

    @Autowired
    ApplicationContext applicationContext;

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

    @Autowired
    MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        EmailerConfigPropertiesAccess.setScheduledSendingDelayCallCount(emailerProperties, 0);
        emailQueueProcessor.processQueuedEmailsAndWait();
        testMailSender.clearBuffer();
    }

    @Test
    void emailAddOn_registersNoExecutorBean() {
        // An Executor bean would make Spring Boot skip its applicationTaskExecutor, and a TaskExecutor bean
        // would be picked up by Vaadin. Core's task scheduler is ignored by both, so it is not counted.
        List<String> executorBeanNames = Arrays.stream(applicationContext.getBeanNamesForType(Executor.class))
                .filter(name -> !applicationContext.isTypeMatch(name, TaskScheduler.class))
                .toList();

        assertEquals(List.of(), executorBeanNames);
    }

    @Test
    void processQueuedEmails_sendsOnOwnExecutor() {
        SendingMessage message = emailer.sendEmailAsync(createEmailInfo());

        emailQueueProcessor.processQueuedEmailsAndWait();

        assertEquals(SendingStatus.SENT, dataManager.load(SendingMessage.class).id(message.getId()).one().getStatus());
        assertTrue(testMailSender.getLastSendThreadName().startsWith("mailSendTaskExecutor-"),
                "Unexpected sending thread: " + testMailSender.getLastSendThreadName());
    }

    @Test
    void mailSendTaskExecutor_publishesExecutorMetrics() {
        Gauge maxPoolSize = meterRegistry.find("executor.pool.max").tag("name", "mailSendTaskExecutor").gauge();

        assertNotNull(maxPoolSize);
        assertEquals(10, maxPoolSize.value());
    }

    static EmailInfo createEmailInfo() {
        return EmailInfoBuilder.create()
                .setAddresses("recipient@example.com")
                .setSubject("Test")
                .setBody("Test Body")
                .build();
    }
}
