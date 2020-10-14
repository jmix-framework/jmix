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

package io.jmix.emailui.screen.sendingmessage.resend;


import io.jmix.core.FileStorageLocator;
import io.jmix.core.Messages;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.fsfilestorage.FileSystemFileStorage;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@UiController("ResendMessage")
@UiDescriptor("resend-message.xml")
public class ResendMessage extends Screen {
    protected SendingMessage message;

    @Autowired
    protected Emailer emailer;
    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileSystemFileStorage fileStorage;
    
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected TextField<String> emailTextField;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (message != null) {
            emailTextField.setValue(message.getAddress());
        }

        fileStorage = fileStorageLocator.getDefault();
    }


    public void setMessage(SendingMessage message) {
        this.message = message;
    }

    @Subscribe("resendEmailBtn")
    protected void onResendEmailBtnClick(Button.ClickEvent event) {
        if (message != null) {
            EmailInfo emailInfo = EmailInfoBuilder.create()
                    .setAddresses(emailTextField.getValue())
                    .setCaption(message.getCaption())
                    .setBody(emailBody(message))
                    .setFrom(message.getFrom())
                    .setBodyContentType(message.getBodyContentType())
                    .setAttachments(getAttachmentsArray(message.getAttachments()))
                    .setBcc(message.getBcc())
                    .setCc(message.getCc())
                    .setHeaders(parseHeadersString(message.getHeaders()))
                    .build();
            try {
                emailer.sendEmail(emailInfo);
            } catch (EmailException e) {
                throw new RuntimeException("Something went wrong during email resending", e);
            }
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage(getClass(), "notificationCaption"))
                    .withDescription(messages.getMessage(getClass(), "notificationDescription"))
                    .show();
            this.closeWithDefaultAction();
        }
    }

    protected String emailBody(SendingMessage message) {
        if (message.getContentTextFile() != null) {
            try (InputStream inputStream = fileStorage.openStream(message.getContentTextFile());) {
                return IOUtils.toString(inputStream, Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException("Can't read message body from the file", e);
            }
        }
        return message.getContentText();
    }

    protected List<EmailHeader> parseHeadersString(String headersString) {
        List<EmailHeader> emailHeadersList = new ArrayList<>();
        if (headersString != null) {
            for (String header : headersString.split("\n")) {
                emailHeadersList.add(EmailHeader.parse(header));
            }
        }
        return emailHeadersList;
    }

    protected EmailAttachment[] getAttachmentsArray(List<SendingAttachment> sendingAttachments) {
        EmailAttachment[] emailAttachments = new EmailAttachment[sendingAttachments.size()];
        for (int i = 0; i < sendingAttachments.size(); i++) {
            SendingAttachment sendingAttachment = sendingAttachments.get(i);
            EmailAttachment emailAttachment = new EmailAttachment(
                    sendingAttachment.getContent(),
                    sendingAttachment.getName(),
                    sendingAttachment.getContentId(),
                    sendingAttachment.getDisposition(),
                    sendingAttachment.getEncoding()
            );
            emailAttachments[i] = emailAttachment;
        }
        return emailAttachments;
    }
}
