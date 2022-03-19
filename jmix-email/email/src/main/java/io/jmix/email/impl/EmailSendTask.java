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

package io.jmix.email.impl;

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.EmailerProperties;
import io.jmix.email.entity.SendingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("email_EmailSendTask")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailSendTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EmailSendTask.class);

    @Autowired
    private SystemAuthenticator authenticator;

    @Autowired
    private EmailerImpl emailer;

    @Autowired
    private EmailerProperties emailerProperties;

    private SendingMessage sendingMessage;

    public EmailSendTask(SendingMessage sendingMessage) {
        this.sendingMessage = sendingMessage;
    }

    @Override
    public void run() {
        try {
            authenticator.begin(emailerProperties.getAsyncSendingUsername());
            try {
                emailer.sendSendingMessage(sendingMessage);
            } finally {
                authenticator.end();
            }
        } catch (Exception e) {
            log.error("Exception while sending email to '{}': ", sendingMessage.getAddress(), e);
        }
    }
}
