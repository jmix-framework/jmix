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


import org.junit.Assert;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * Used by functional tests.
 * Fakes real JavaMailSender.
 *
 */
@Component
public class TestMailSender extends JavaMailSenderImpl {
    private List<MimeMessage> myMessages = new ArrayList<>();

    private boolean mustFail;

    public void clearBuffer() {
        myMessages.clear();
    }

    public int getBufferSize() {
        return myMessages.size();
    }

    public MimeMessage fetchSentEmail() {

        Assert.assertFalse(myMessages.isEmpty());

        return myMessages.remove(0);
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        if (mustFail) {
            throw new MailSendException("Smtp server not available");
        }
        myMessages.add(mimeMessage);
    }

    public boolean isEmpty() {
        return myMessages.isEmpty();
    }

    public void failPlease() {
        this.mustFail = true;
    }

    public void workNormallyPlease() {
        this.mustFail = false;
    }
}
