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

import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.export.ExportFormat;
import io.jmix.core.CoreProperties;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Messages;
import io.jmix.email.EmailDataProvider;
import io.jmix.email.EmailerProperties;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.emailui.screen.sendingmessage.attachments.SendingMessageAttachments;
import io.jmix.emailui.screen.sendingmessage.resend.ResendMessage;
import io.jmix.fsfilestorage.FileSystemFileStorage;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.download.FileDataProvider;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@UiController("email_SendingMessage.browse")
@UiDescriptor("sending-message-browse.xml")
public class SendingMessageBrowser extends Screen {

    protected static final String CONTENT_TEXT = "contentText";

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
    protected ThemeConstants themeConstants;
    @Autowired
    protected Table<SendingMessage> sendingMessageTable;
    @Autowired
    protected TemporaryStorage fileUploading;
    @Autowired
    protected DataSupplier dataSupplier;
    @Autowired
    protected Downloader exportDisplay;
    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected FileSystemFileStorage fileStorage;
    @Autowired
    protected Messages messages;

    @Named("fg.bodyContentType")
    protected TextField<String> bodyContentTypeField;

    protected Button showContentButton;

    protected TextArea<String> contentTextArea;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Notifications notifications;

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        fileStorage = fileStorageLocator.getDefault();

        fg.add(buildContextTextField(), 1, 4);

        sendingMessageDc.addItemChangeListener(e -> selectedItemChanged(e.getItem()));
    }

    protected Component buildContextTextField() {
        VBoxLayout contentArea = uiComponents.create(VBoxLayout.class);
        contentArea.setSpacing(true);

        contentTextArea = uiComponents.create(TextArea.NAME);
        contentTextArea.setWidth("100%");
        contentTextArea.setEditable(false);
        contentTextArea.setHeight(themeConstants.get("cuba.gui.SendingMessageBrowser.contentTextArea.height"));
        contentTextArea.setEditable(false);

        showContentButton = uiComponents.create(Button.class);
        showContentButton.setAction(new AbstractAction("") {
            @Override
            public void actionPerform(Component component) {
                String textAreaValue = contentTextArea.getValue();
                if (textAreaValue != null) {
                    ByteArrayDataProvider dataProvider = new ByteArrayDataProvider(textAreaValue.getBytes(StandardCharsets.UTF_8),
                            uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());

                    String type = bodyContentTypeField.getRawValue();
                    if (StringUtils.containsIgnoreCase(type, ExportFormat.HTML.getContentType())) {
                        exportDisplay.download(dataProvider, "email-preview.html", DownloadFormat.HTML);
                    } else {
                        exportDisplay.download(dataProvider, "email-preview.txt", DownloadFormat.TEXT);
                    }
                }
            }
        });
        showContentButton.setEnabled(false);
        showContentButton.setCaption(messages.getMessage(getClass(), "sendingMessage.showContent"));

        contentArea.add(contentTextArea);
        contentArea.add(showContentButton);

        return contentArea;
    }

    protected void selectedItemChanged(SendingMessage item) {
        String contentText = null;
        if (item != null) {
            contentText = emailDataProvider.loadContentText(item);
        }

        if (StringUtils.isNotEmpty(contentText)) {
            showContentButton.setEnabled(true);
        } else {
            showContentButton.setEnabled(false);
        }

        contentTextArea.setValue(contentText);
    }

    @Subscribe("sendingMessageTable.downloadAttachment")
    public void download() {
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
                        .withCaption(messages.getMessage(getClass(), "noAttachments"))
                        .show();
            }
        }
    }

    @Subscribe("sendingMessageTable.resendEmail")
    public void resendEmail() {
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
        return dataSupplier.reload(message, "sendingMessage.loadFromQueue").getAttachments();
    }

    protected URI getReference(SendingAttachment attachment) {
        UUID uuid = fileUploading.saveFile(attachment.getContent());
        URI reference = fileStorage.createReference(attachment.getName());
        fileUploading.putFileIntoStorage(uuid, reference);
        return reference;
    }

    protected void exportFile(SendingAttachment attachment) {
        URI fd;

        if (emailerProperties.isFileStorageUsed()
                && attachment.getContentFile() != null
                && fileStorage.fileExists(attachment.getContentFile())) {
            fd = attachment.getContentFile();
        } else {
            fd = getReference(attachment);
        }

        exportDisplay.download(new FileDataProvider<>(fd, fileStorage), attachment.getName(), DownloadFormat.OCTET_STREAM);
    }
}