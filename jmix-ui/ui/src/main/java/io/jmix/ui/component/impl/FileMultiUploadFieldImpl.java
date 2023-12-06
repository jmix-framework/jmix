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

package io.jmix.ui.component.impl;

import com.vaadin.ui.Component;
import io.jmix.core.FileStorageException;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.FileMultiUploadField;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;
import static io.jmix.ui.upload.FileUploadTypesHelper.convertToMIME;

public class FileMultiUploadFieldImpl extends AbstractComponent<JmixFileUpload>
        implements FileMultiUploadField, InitializingBean {

    protected static final int BYTES_IN_MEGABYTE = 1048576;

    protected final Map<UUID, String> files = new LinkedHashMap<>();

    protected long fileSizeLimit = 0;
    protected Set<String> permittedExtensions;
    protected DropZone dropZone;
    protected ComponentContainer pasteZone;
    protected String dropZonePrompt;

    protected UiComponentProperties componentProperties;
    protected Messages messages;
    protected TemporaryStorage temporaryStorage;
    protected UUID tempFileId;
    protected String accept;

    public FileMultiUploadFieldImpl() {
        component = createComponent();
    }

    @Autowired
    public void setTemporaryStorage(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected JmixFileUpload createComponent() {
        return new JmixFileUpload();
    }

    protected void initComponent(JmixFileUpload impl) {
        impl.setMultiSelect(true);

        impl.setProgressWindowCaption(messages.getMessage("upload.uploadingProgressTitle"));
        impl.setUnableToUploadFileMessage(messages.getMessage("upload.unableToUploadFile"));
        impl.setCancelButtonCaption(messages.getMessage("upload.cancel"));
        impl.setCaption(messages.getMessage("upload.submit"));
        impl.setDropZonePrompt(messages.getMessage("upload.dropZonePrompt"));
        impl.setDescription(null);

        int maxUploadSizeMb = componentProperties.getUploadFieldMaxUploadSizeMb();
        int maxSizeBytes = maxUploadSizeMb * BYTES_IN_MEGABYTE;

        impl.setFileSizeLimit(maxSizeBytes);

        impl.setReceiver(this::receiveUpload);

        impl.addStartedListener(this::onUploadStarted);

        impl.addQueueUploadFinishedListener(this::onQueueUploadFinished);

        impl.addSucceededListener(this::onUploadSucceeded);
        impl.addFailedListener(this::onUploadFailed);
        impl.addFileSizeLimitExceededListener(this::onFileSizeLimitExceeded);
        impl.addFileExtensionNotAllowedListener(this::onFileExtensionNotAllowed);
    }

    private OutputStream receiveUpload(String fileName, String MIMEType) {
        FileOutputStream outputStream;
        try {
            TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();
            tempFileId = fileInfo.getId();
            File tmpFile = fileInfo.getFile();
            outputStream = new FileOutputStream(tmpFile);
        } catch (Exception e) {
            throw new RuntimeException("Unable to receive file", e);
        }
        return outputStream;
    }

    private void onUploadStarted(JmixFileUpload.StartedEvent event) {
        fireFileUploadStart(event.getFileName(), event.getContentLength());
    }

    private void onQueueUploadFinished(JmixFileUpload.QueueFinishedEvent event) {
        fireQueueUploadComplete();
    }

    private void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        files.put(tempFileId, event.getFileName());

        fireFileUploadFinish(event.getFileName(), event.getContentLength());
    }

    private void onUploadFailed(JmixFileUpload.FailedEvent event) {
        try {
            // close and remove temp file
            temporaryStorage.deleteFile(tempFileId);
            tempFileId = null;
        } catch (Exception e) {
            if (e instanceof FileStorageException) {
                FileStorageException fse = (FileStorageException) e;
                if (fse.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                    LoggerFactory.getLogger(FileMultiUploadFieldImpl.class)
                            .warn("Could not remove temp file {} after broken uploading", tempFileId);
                }
            }
            LoggerFactory.getLogger(FileMultiUploadFieldImpl.class)
                    .warn("Error while delete temp file {}", tempFileId);
        }

        fireFileUploadError(event.getFileName(), event.getContentLength(), event.getReason());
    }

    private void onFileSizeLimitExceeded(JmixFileUpload.FileSizeLimitExceededEvent e) {
        Notifications notifications = getScreenContext(this).getNotifications();

        notifications.create(NotificationType.WARNING)
                .withCaption(
                        messages.formatMessage("", "multiupload.filesizeLimitExceed",
                                e.getFileName(), getFileSizeLimitString())
                )
                .show();
    }

    private void onFileExtensionNotAllowed(JmixFileUpload.FileExtensionNotAllowedEvent e) {
        Notifications notifications = getScreenContext(this).getNotifications();

        notifications.create(NotificationType.WARNING)
                .withCaption(messages.formatMessage("", "upload.fileIncorrectExtension.message", e.getFileName()))
                .show();
    }

    /**
     * Get uploads map
     *
     * @return Map (UUID - Id of file in Temporary storage, String - FileName )
     */
    @Override
    public Map<UUID, String> getUploadsMap() {
        return Collections.unmodifiableMap(files);
    }

    @Override
    public void clearUploads() {
        files.clear();
    }

    @Override
    public Subscription addQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener) {
        return getEventHub().subscribe(QueueUploadCompleteEvent.class, listener);
    }

    @Override
    public void setIcon(@Nullable String icon) {
        this.icon = icon;

        if (!StringUtils.isEmpty(icon)) {
            component.setIcon(getIconResource(icon));
        } else {
            component.setIcon(null);
        }
    }

    @Nullable
    @Override
    public String getAccept() {
        return accept;
    }

    @Override
    public void setAccept(@Nullable String accept) {
        if (!Objects.equals(accept, getAccept())) {
            this.accept = accept;
            component.setAccept(convertToMIME(accept));
        }
    }

    @Nullable
    @Override
    public DropZone getDropZone() {
        return dropZone;
    }

    @Override
    public void setDropZone(@Nullable DropZone dropZone) {
        this.dropZone = dropZone;

        if (dropZone == null) {
            component.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();

            Component vComponent = target.unwrapComposition(Component.class);
            this.component.setDropZone(vComponent);
        }
    }

    @Nullable
    @Override
    public ComponentContainer getPasteZone() {
        return pasteZone;
    }

    @Override
    public void setPasteZone(@Nullable ComponentContainer pasteZone) {
        this.pasteZone = pasteZone;

        if (pasteZone == null) {
            component.setPasteZone(null);
        } else {
            Component vComponent = pasteZone.unwrapComposition(Component.class);
            component.setPasteZone(vComponent);
        }
    }

    @Nullable
    @Override
    public String getDropZonePrompt() {
        return dropZonePrompt;
    }

    @Override
    public void setDropZonePrompt(@Nullable String dropZonePrompt) {
        this.dropZonePrompt = dropZonePrompt;

        component.setDropZonePrompt(dropZonePrompt);
    }

    protected void fireFileUploadStart(String fileName, long contentLength) {
        FileUploadStartEvent event = new FileUploadStartEvent(this, fileName, contentLength);
        publish(FileUploadStartEvent.class, event);
    }

    protected void fireFileUploadFinish(String fileName, long contentLength) {
        FileUploadFinishEvent event = new FileUploadFinishEvent(this, fileName, contentLength);
        publish(FileUploadFinishEvent.class, event);
    }

    protected void fireFileUploadError(String fileName, long contentLength, Exception cause) {
        FileUploadErrorEvent event = new FileUploadErrorEvent(this, fileName, contentLength, cause);
        publish(FileUploadErrorEvent.class, event);
    }

    protected void fireQueueUploadComplete() {
        QueueUploadCompleteEvent event = new QueueUploadCompleteEvent(this);
        publish(QueueUploadCompleteEvent.class, event);
    }

    @Override
    public long getFileSizeLimit() {
        return fileSizeLimit;
    }

    @Override
    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;

        this.component.setFileSizeLimit(fileSizeLimit);
    }

    @Nullable
    @Override
    public Set<String> getPermittedExtensions() {
        return permittedExtensions;
    }

    @Override
    public void setPermittedExtensions(@Nullable Set<String> permittedExtensions) {
        if (permittedExtensions != null) {
            this.permittedExtensions = permittedExtensions.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } else {
            this.permittedExtensions = null;
        }

        this.component.setPermittedExtensions(this.permittedExtensions);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    protected String getFileSizeLimitString() {
        String fileSizeLimitString;
        if (fileSizeLimit > 0) {
            if (fileSizeLimit % BYTES_IN_MEGABYTE == 0) {
                fileSizeLimitString = String.valueOf(fileSizeLimit / BYTES_IN_MEGABYTE);
            } else {
                DatatypeRegistry datatypeRegistry = applicationContext.getBean(DatatypeRegistry.class);
                Datatype<Double> doubleDatatype = datatypeRegistry.get(Double.class);
                double fileSizeInMb = fileSizeLimit / ((double) BYTES_IN_MEGABYTE);

                CurrentAuthentication currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
                fileSizeLimitString = doubleDatatype.format(fileSizeInMb, currentAuthentication.getLocale());
            }
        } else {
            fileSizeLimitString = String.valueOf(componentProperties.getUploadFieldMaxUploadSizeMb());
        }
        return fileSizeLimitString;
    }

    @Override
    public Subscription addFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        return getEventHub().subscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        return getEventHub().subscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        return getEventHub().subscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void setTotalProgressDisplayEnabled(boolean totalProgressDisplayEnabled) {
        component.setTotalProgressDisplayEnabled(totalProgressDisplayEnabled);
    }

    @Override
    public boolean isTotalProgressDisplayEnabled() {
        return component.isTotalProgressDisplayEnabled();
    }
}
