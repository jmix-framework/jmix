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


import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UiController("ResendMessage")
@UiDescriptor("resend-message.xml")
public class ResendMessage extends Screen {
    protected SendingMessage message;

    @Autowired
    protected Emailer emailer;

    protected FileStorage fileStorage;

    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected TextField<String> emailTextField;
    @Autowired
    protected TextField<String> ccTextField;
    @Autowired
    protected TextField<String> bccTextField;
    @Autowired
    private CheckBox importanceField;

    @Autowired
    protected MessageBundle messageBundle;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (message != null) {
            emailTextField.setValue(message.getAddress());
            ccTextField.setValue(message.getCc());
            bccTextField.setValue(message.getBcc());
        }
    }

    public void setMessage(SendingMessage message) {
        this.message = message;
    }

    @Subscribe("resendEmailBtn")
    protected void onResendEmailBtnClick(Button.ClickEvent event) {
        if (message != null) {
            EmailInfo emailInfo = EmailInfoBuilder.create()
                    .setAddresses(emailTextField.getValue())
                    .setSubject(message.getSubject())
                    .setBody(emailBody(message))
                    .setFrom(message.getFrom())
                    .setBodyContentType(message.getBodyContentType())
                    .setAttachments(getEmailAttachments(message.getAttachments()))
                    .setBcc(bccTextField.getValue())
                    .setCc(ccTextField.getValue())
                    .setHeaders(parseHeadersString(message.getHeaders()))
                    .setImportant(importanceField.getValue() != null && importanceField.getValue())
                    .build();
            try {
                emailer.sendEmail(emailInfo);
            } catch (EmailException e) {
                throw new RuntimeException("Something went wrong during email resending", e);
            }
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("notificationCaption"))
                    .withDescription(messageBundle.getMessage("notificationDescription"))
                    .show();
            this.closeWithDefaultAction();
        }
    }

    protected String emailBody(SendingMessage message) {
        if (message.getContentTextFile() != null) {
            if (fileStorage == null) {
                fileStorage = fileStorageLocator.getDefault();
            }
            try (InputStream inputStream = fileStorage.openStream(message.getContentTextFile())) {
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

    protected List<EmailAttachment> getEmailAttachments(List<SendingAttachment> sendingAttachments) {
        return sendingAttachments
                .stream()
                .map(this::convertToEmailAttachment)
                .collect(Collectors.toList());
    }

    protected EmailAttachment convertToEmailAttachment(SendingAttachment sendingAttachment) {
        return new EmailAttachment(
                attachmentBody(sendingAttachment),
                sendingAttachment.getName(),
                sendingAttachment.getContentId(),
                sendingAttachment.getDisposition(),
                sendingAttachment.getEncoding()
        );
    }

    protected byte[] attachmentBody(SendingAttachment attachment) {
        if (attachment.getContentFile() != null) {
            try (InputStream inputStream = fileStorage.openStream(attachment.getContentFile())) {
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Can't read attachment body from the file", e);
            }
        }
        return attachment.getContent();
    }
}
