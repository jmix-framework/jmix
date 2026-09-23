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

package io.jmix.email.impl;

import io.jmix.email.EmailConnectionTester;
import jakarta.mail.MessagingException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@NullMarked
@Component("email_EmailConnectionTester")
public class EmailConnectionTesterImpl implements EmailConnectionTester {

    private static final Logger log = LoggerFactory.getLogger(EmailConnectionTesterImpl.class);

    protected final JavaMailSender mailSender;

    public EmailConnectionTesterImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void testConnection() throws MessagingException {
        if (!(mailSender instanceof JavaMailSenderImpl javaMailSenderImpl)) {
            throw new IllegalStateException("Connection test is not supported by "
                    + mailSender.getClass().getName());
        }
        log.debug("Testing connection to {}:{}", javaMailSenderImpl.getHost(), javaMailSenderImpl.getPort());
        javaMailSenderImpl.testConnection();
        log.info("Mail server connection test passed");
    }
}
