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

package io.jmix.imapui.screen.message;

import io.jmix.core.CoreProperties;
import io.jmix.imap.ImapAttachments;
import io.jmix.imap.ImapManager;
import io.jmix.imap.dto.ImapMessageDto;
import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.entity.ImapMessageAttachment;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@UiController("imap_Message.edit")
@UiDescriptor("imap-message-edit.xml")
@EditedEntityContainer("imapMessageDc")
public class ImapMessageEdit extends StandardEditor<ImapMessage> {

    @Autowired
    protected ImapManager imapManager;
    @Autowired
    protected ImapAttachments imapAttachments;
    @Autowired
    protected Table<ImapMessageAttachment> attachmentsTable;
    @Autowired
    protected CollectionContainer<ImapMessageAttachment> imapDemoAttachmentsDc;
    @Autowired
    protected InstanceContainer<ImapMessageDto> imapMessageDtoDc;
    @Autowired
    protected Label bodyContent;
    @Autowired
    protected ScrollBoxLayout bodyContentScroll;
    @Autowired
    protected BrowserFrame bodyContentHtml;
    @Autowired
    protected ProgressBar progressBar;
    @Autowired
    protected Button downloadBtn;
    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected DataLoader imapDemoAttachmentsLoader;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;

    @Override
    public void setEntityToEdit(ImapMessage item) {
        super.setEntityToEdit(item);

        AtomicInteger loadProgress = new AtomicInteger(0);
        initBody(item, loadProgress);
        initAttachments(item, loadProgress);
    }

    @Subscribe("tabSheet")
    protected void selectedTabChanged(TabSheet.SelectedTabChangeEvent event) {
        downloadBtn.setEnabled(imapDemoAttachmentsDc.getItems().size() > 0);
    }

    @Subscribe("attachmentsTable.download")
    public void downloadAttachment(Action.ActionPerformedEvent event) {
        attachmentsTable.getSelected().forEach(attachment -> {
            byte[] bytes = imapAttachments.loadFile(attachment);
            downloader.download(new ByteArrayDataProvider(bytes, uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                    coreProperties.getTempDir()), attachment.getName());

        });
    }

    protected void initBody(ImapMessage msg, AtomicInteger loadProgress) {
        BackgroundTaskHandler taskHandler = backgroundWorker.handle(new InitBodyTask(msg, loadProgress));
        taskHandler.execute();
    }

    protected void initAttachments(ImapMessage msg, AtomicInteger loadProgress) {
        if (!Boolean.TRUE.equals(msg.getAttachmentsLoaded())) {
            BackgroundTaskHandler taskHandler = backgroundWorker.handle(new InitAttachmentTask(msg, loadProgress));
            taskHandler.execute();
        } else {
            hideProgressBar(loadProgress);
        }
    }

    protected void hideProgressBar(AtomicInteger loadProgress) {
        if (loadProgress.incrementAndGet() == 2) {
            progressBar.setVisible(false);
        }
    }

    protected class InitBodyTask extends BackgroundTask<Integer, ImapMessageDto> {
        protected final ImapMessage message;
        protected final AtomicInteger loadProgress;

        InitBodyTask(ImapMessage message, AtomicInteger loadProgress) {
            super(0, ImapMessageEdit.this);
            this.message = message;
            this.loadProgress = loadProgress;
        }

        @Override
        public ImapMessageDto run(TaskLifeCycle<Integer> taskLifeCycle) {
            return imapManager.fetchMessage(message);
        }

        @Override
        public void canceled() {
            // Do something in UI thread if the task is canceled
        }

        @Override
        public void done(ImapMessageDto dto) {
            imapMessageDtoDc.setItem(dto);
            bodyContent.setHtmlEnabled(dto.getHtml());
            if (Boolean.TRUE.equals(dto.getHtml())) {
                byte[] bytes = dto.getBody().getBytes(StandardCharsets.UTF_8);
                bodyContentHtml.setSource(io.jmix.ui.component.StreamResource.class)
                        .setStreamSupplier(() -> new ByteArrayInputStream(bytes))
                        .setMimeType("text/html");
                bodyContent.setHtmlEnabled(true);
                bodyContent.setValue(dto.getBody());
                bodyContentHtml.setVisible(true);
            } else {
                bodyContent.setValue(dto.getBody());
                bodyContentScroll.setVisible(true);
            }
            hideProgressBar(loadProgress);
        }

        @Override
        public void progress(List<Integer> changes) {
            // Show current progress in UI thread
        }
    }

    protected class InitAttachmentTask extends BackgroundTask<Integer, Integer> {
        protected final ImapMessage msg;
        protected final AtomicInteger loadProgress;

        InitAttachmentTask(ImapMessage msg, AtomicInteger loadProgress) {
            super(0, ImapMessageEdit.this);
            this.msg = msg;
            this.loadProgress = loadProgress;
        }

        @Override
        public Integer run(TaskLifeCycle<Integer> taskLifeCycle) {
            return imapAttachments.fetchAttachments(msg).size();
        }

        @Override
        public void canceled() {
            // Do something in UI thread if the task is canceled
        }

        @Override
        public void done(Integer attachmentCount) {
            imapDemoAttachmentsLoader.load();
            hideProgressBar(loadProgress);
        }

        @Override
        public void progress(List<Integer> changes) {
            // Show current progress in UI thread
        }
    }
}