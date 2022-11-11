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

package io.jmix.emailui.screen.sendingmessage;

import io.jmix.core.*;
import io.jmix.email.EmailDataProvider;
import io.jmix.email.EmailerProperties;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.emailui.screen.sendingmessage.attachments.SendingMessageAttachments;
import io.jmix.emailui.screen.sendingmessage.resend.ResendMessage;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.download.FileDataProvider;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@UiController("email_SendingMessage.browse")
@UiDescriptor("sending-message-browse.xml")
public class SendingMessageBrowser extends Screen {

    @Autowired
    protected CollectionContainer<SendingMessage> sendingMessageDc;

    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected EmailDataProvider emailDataProvider;
    @Autowired
    protected EmailerProperties emailerProperties;
    @Autowired
    protected Form fg;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Table<SendingMessage> sendingMessageTable;
    @Autowired
    protected TemporaryStorage temporaryStorage;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Downloader downloader;

    protected FileStorage fileStorage;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @Autowired
    protected Messages messages;
    @Autowired
    protected TextField<String> bodyContentType;

    protected Button showContentButton;

    protected TextArea<String> contentTextArea;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected MessageBundle messageBundle;

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        fg.add(buildContentTextField(), 0, 5);

        sendingMessageDc.addItemChangeListener(e -> selectedItemChanged(e.getItem()));
    }

    protected Component buildContentTextField() {
        VBoxLayout contentArea = uiComponents.create(VBoxLayout.class);
        contentArea.setSpacing(true);
        contentArea.setCaption(messages.getMessage(SendingMessage.class, "SendingMessage.contentText"));

        contentTextArea = uiComponents.create(TextArea.NAME);
        contentTextArea.setWidth("100%");
        contentTextArea.setEditable(false);
        contentTextArea.setHeight("350px");
        contentTextArea.setEditable(false);

        showContentButton = uiComponents.create(Button.class);
        showContentButton.setAction(new AbstractAction("") {
            @Override
            public void actionPerform(Component component) {
                String textAreaValue = contentTextArea.getValue();
                if (textAreaValue != null) {
                    ByteArrayDataProvider dataProvider = new ByteArrayDataProvider(textAreaValue.getBytes(StandardCharsets.UTF_8),
                            uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());

                    String type = bodyContentType.getRawValue();
                    if (StringUtils.containsIgnoreCase(type, DownloadFormat.HTML.getContentType())) {
                        downloader.download(dataProvider, "email-preview.html", DownloadFormat.HTML);
                    } else {
                        downloader.download(dataProvider, "email-preview.txt", DownloadFormat.TEXT);
                    }
                }
            }
        });
        showContentButton.setEnabled(false);
        showContentButton.setCaption(messageBundle.getMessage("showContent"));

        contentArea.add(contentTextArea);
        contentArea.add(showContentButton);

        return contentArea;
    }


    protected void selectedItemChanged(SendingMessage item) {
        String contentText = null;
        if (item != null) {
            contentText = emailDataProvider.loadContentText(item);
        }

        showContentButton.setEnabled(StringUtils.isNotEmpty(contentText));

        contentTextArea.setValue(contentText);
    }

    @Subscribe("sendingMessageTable.downloadAttachment")
    public void download(Action.ActionPerformedEvent event) {
        SendingMessage message = sendingMessageTable.getSingleSelected();
        if (message != null) {
            List<SendingAttachment> attachments = getAttachments(message);
            if (CollectionUtils.isNotEmpty(attachments)) {
                if (attachments.size() == 1) {
                    exportFile(attachments.get(0));
                } else {
                    selectAttachmentDialog(message);
                }
            } else {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("noAttachments"))
                        .show();
            }
        }
    }

    @Subscribe("sendingMessageTable.resendEmail")
    public void resendEmail(Action.ActionPerformedEvent event) {
        ResendMessage resendMessage = screenBuilders.screen(this)
                .withOpenMode(OpenMode.DIALOG)
                .withScreenClass(ResendMessage.class)
                .build();
        resendMessage.setMessage(sendingMessageTable.getSingleSelected());
        resendMessage.show();
    }

    protected void selectAttachmentDialog(SendingMessage message) {
        SendingMessageAttachments sendingMessageAttachments = screenBuilders.lookup(SendingAttachment.class, this)
                .withScreenClass(SendingMessageAttachments.class)
                .withOpenMode(OpenMode.DIALOG)
                .withSelectHandler(items -> {
                    if (items.size() == 1) {
                        exportFile(IterableUtils.get(items, 0));
                    }
                })
                .build();
        sendingMessageAttachments.setMessage(message);
        sendingMessageAttachments.show();
    }

    protected List<SendingAttachment> getAttachments(SendingMessage message) {
        SendingMessage msg = dataManager.load(SendingMessage.class)
                .id(message.getId())
                .fetchPlan("sendingMessage.loadFromQueue")
                .one();
        return msg.getAttachments();
    }

    protected FileRef getReference(SendingAttachment attachment) {
        UUID uuid = temporaryStorage.saveFile(attachment.getContent());
        return temporaryStorage.putFileIntoStorage(uuid, attachment.getName());
    }

    protected void exportFile(SendingAttachment attachment) {
        FileRef fileRef;

        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }

        if (emailerProperties.isUseFileStorage()
                && attachment.getContentFile() != null
                && fileStorage.fileExists(attachment.getContentFile())) {
            fileRef = attachment.getContentFile();
        } else {
            fileRef = getReference(attachment);
        }

        downloader.download(new FileDataProvider(fileRef, fileStorage), attachment.getName(), DownloadFormat.OCTET_STREAM);
    }
}