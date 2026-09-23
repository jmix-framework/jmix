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

package email_connection;

import io.jmix.email.impl.EmailConnectionTesterImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class EmailConnectionTesterTest {

    @Test
    void testDelegatesToJavaMailSender() throws MessagingException {
        TestConnectionMailSender mailSender = new TestConnectionMailSender();
        EmailConnectionTesterImpl tester = new EmailConnectionTesterImpl(mailSender);

        tester.testConnection();

        assertTrue(mailSender.connectionTested);
    }

    @Test
    void testConnectionFailureIsPropagated() {
        TestConnectionMailSender mailSender = new TestConnectionMailSender();
        mailSender.failure = new MessagingException("535 5.7.8 Authentication failed");
        EmailConnectionTesterImpl tester = new EmailConnectionTesterImpl(mailSender);

        MessagingException exception = assertThrows(MessagingException.class, tester::testConnection);
        assertTrue(exception.getMessage().contains("535"));
    }

    @Test
    void testUnsupportedSenderProducesMeaningfulError() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailConnectionTesterImpl tester = new EmailConnectionTesterImpl(mailSender);

        IllegalStateException exception = assertThrows(IllegalStateException.class, tester::testConnection);
        assertTrue(exception.getMessage().contains("not supported"));
    }

    static class TestConnectionMailSender extends JavaMailSenderImpl {

        boolean connectionTested;
        MessagingException failure;

        @Override
        public void testConnection() throws MessagingException {
            if (failure != null) {
                throw failure;
            }
            connectionTested = true;
        }
    }
}
