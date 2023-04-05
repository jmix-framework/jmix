/*
 * Copyright 2023 Haulmont.
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

package io.jmix.emailflowui.view.sendingmessage.resend;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import io.jmix.core.FileStorageLocator;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ViewController("email_resendMessageView")
@ViewDescriptor("resend-message-view.xml")
@DialogMode(width = "50em", height = "AUTO")
public class ResendMessageView extends StandardView {

    protected SendingMessage sendingMessage;

    @ViewComponent
    protected TypedTextField<String> emailTextField;
    @ViewComponent
    protected TypedTextField<String> ccTextField;
    @ViewComponent
    protected TypedTextField<String> bccTextField;
    @ViewComponent
    protected JmixCheckbox importanceField;

    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Emailer emailer;

    @Subscribe("resendEmailBtn")
    public void onResendEmailBtnClick(ClickEvent<JmixButton> event) {
        if (sendingMessage == null) {
            return;
        }

        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses(emailTextField.getValue())
                .setSubject(sendingMessage.getSubject())
                .setBody(getEmailBody(sendingMessage))
                .setFrom(sendingMessage.getFrom())
                .setBodyContentType(sendingMessage.getBodyContentType())
                .setAttachments(getEmailAttachments(sendingMessage.getAttachments()))
                .setBcc(bccTextField.getValue())
                .setCc(ccTextField.getValue())
                .setHeaders(parseHeadersString(sendingMessage.getHeaders()))
                .setImportant(importanceField.getValue())
                .build();

        try {
            emailer.sendEmail(emailInfo);
        } catch (EmailException e) {
            throw new RuntimeException("Something went wrong during email resending", e);
        }

        notifications.create(
                        messageBundle.getMessage("resendMessageView.resendingSuccessNotification.header"),
                        messageBundle.getMessage("resendMessageView.resendingSuccessNotification.message")
                )
                .withType(Notifications.Type.SUCCESS)
                .show();

        this.closeWithDefaultAction();
    }

    public void setMessage(SendingMessage sendingMessage) {
        this.sendingMessage = sendingMessage;

        emailTextField.setTypedValue(sendingMessage.getAddress());
        ccTextField.setTypedValue(sendingMessage.getCc());
        bccTextField.setTypedValue(sendingMessage.getBcc());
    }

    protected List<EmailHeader> parseHeadersString(String headersString) {
        if (!Strings.isNullOrEmpty(headersString)) {
            List<EmailHeader> emailHeadersList = new ArrayList<>();

            for (String header : headersString.split("\n")) {
                emailHeadersList.add(EmailHeader.parse(header));
            }

            return emailHeadersList;
        }

        return Collections.emptyList();
    }

    protected String getEmailBody(SendingMessage message) {
        if (message.getContentTextFile() != null) {
            try (InputStream inputStream = fileStorageLocator.getDefault().openStream(message.getContentTextFile())) {
                return IOUtils.toString(inputStream, Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException("Can't read message body from the file", e);
            }
        }

        return message.getContentText();
    }

    protected List<EmailAttachment> getEmailAttachments(List<SendingAttachment> sendingAttachments) {
        return sendingAttachments.stream()
                .map(this::convertToEmailAttachment)
                .collect(Collectors.toUnmodifiableList());
    }

    protected EmailAttachment convertToEmailAttachment(SendingAttachment sendingAttachment) {
        return new EmailAttachment(
                getAttachmentBody(sendingAttachment),
                sendingAttachment.getName(),
                sendingAttachment.getContentId(),
                sendingAttachment.getDisposition(),
                sendingAttachment.getEncoding()
        );
    }

    protected byte[] getAttachmentBody(SendingAttachment attachment) {
        if (attachment.getContentFile() != null) {
            try (InputStream inputStream = fileStorageLocator.getDefault().openStream(attachment.getContentFile())) {
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Can't read attachment body from the file", e);
            }
        }

        return attachment.getContent();
    }
}
