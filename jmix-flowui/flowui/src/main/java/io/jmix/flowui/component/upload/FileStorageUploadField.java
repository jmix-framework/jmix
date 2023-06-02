/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.upload;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.upload.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.upload.receiver.FileTemporaryStorageBuffer;
import io.jmix.flowui.component.upload.receiver.TemporaryStorageFileData;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.upload.FileStoragePutMode;
import io.jmix.flowui.kit.component.upload.JmixFileStorageUploadField;
import io.jmix.flowui.kit.component.upload.JmixUploadI18N;
import io.jmix.flowui.kit.component.upload.event.FileUploadFileRejectedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.upload.TemporaryStorage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class FileStorageUploadField extends JmixFileStorageUploadField<FileStorageUploadField, FileRef>
        implements SupportsValueSource<FileRef>, SupportsValidation<FileRef>, HasRequired,
        SupportsStatusChangeHandler<FileStorageUploadField>, ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(FileStorageUploadField.class);

    protected ApplicationContext applicationContext;
    protected TemporaryStorage temporaryStorage;
    protected FileStorageLocator fileStorageLocator;
    protected Downloader downloader;
    protected Notifications notifications;
    protected Messages messages;
    protected ObjectProvider<MultipartProperties> multipartPropertiesProvider;

    protected FieldDelegate<FileStorageUploadField, FileRef, FileRef> fieldDelegate;

    protected FileStorage fileStorage;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        temporaryStorage = applicationContext.getBean(TemporaryStorage.class);
        fileStorageLocator = applicationContext.getBean(FileStorageLocator.class);
        downloader = applicationContext.getBean(Downloader.class);
        messages = applicationContext.getBean(Messages.class);
        notifications = applicationContext.getBean(Notifications.class);
        multipartPropertiesProvider = applicationContext.getBeanProvider(MultipartProperties.class);
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();

        uploadButton.setReceiver(applicationContext.getBean(FileTemporaryStorageBuffer.class));

        setComponentClickListener(fileNameComponent, this::onFileNameClick);
        setComponentText(fileNameComponent, generateFileName());
        setComponentText(uploadButton.getUploadButton(), getDefaultUploadText());

        multipartPropertiesProvider.ifAvailable(properties ->
                setMaxFileSize((int) properties.getMaxFileSize().toBytes()));

        applyI18nDefaults();

        attachValueChangeListener(this::onValueChange);

        attachUploadEvents(uploadButton);
    }

    protected FieldDelegate<FileStorageUploadField, FileRef, FileRef> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public Registration addValidator(Validator<? super FileRef> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Nullable
    @Override
    public ValueSource<FileRef> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<FileRef> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<FileStorageUploadField>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    /**
     * Specify the maximum file size in bytes allowed to upload. Notice that it is a client-side constraint,
     * which will be checked before sending the request.
     * <p>
     * <strong>Note</strong> if {@link MultipartProperties} is available, the default value will be set from
     * {@link MultipartProperties#getMaxFileSize()} that is equal to 1Mb. To increase maximum file size for all
     * fields in the application use {@link MultipartProperties#getMaxFileSize()} property.
     *
     * @param maxFileSize the maximum file size in bytes
     * @see Upload#setMaxFileSize(int)
     * @see MultipartProperties#getMaxFileSize()
     * @see MultipartProperties#getMaxRequestSize()
     */
    @Override
    public void setMaxFileSize(int maxFileSize) {
        multipartPropertiesProvider.ifAvailable(properties -> {
            if (maxFileSize > properties.getMaxFileSize().toBytes()
                    && !properties.getMaxFileSize().isNegative()) {
                log.warn("The provided maximum file size '{}B' is greater than server can accept ({}B) that may" +
                                " lead to unhandled uploading errors. It is recommended to use the same value for upload field" +
                                " and for server maximum file size value (see" +
                                " 'org.springframework.boot.autoconfigure.web.servlet.MultipartProperties#maxFileSize').",
                        maxFileSize, properties.getMaxFileSize().toBytes());
            }
        });

        super.setMaxFileSize(maxFileSize);
    }

    /**
     * Adds a succeeded listener that is informed on upload succeeded.
     * <p>
     * For instance, if component has {@link FileStoragePutMode#MANUAL},
     * we can handle the uploading, like the following:
     * <pre>
     *     manuallyControlledField.addFileUploadSucceededListener(event -&gt; {
     *          FileTemporaryStorageBuffer receiver = event.getReceiver();
     *          File file = temporaryStorage.getFile(receiver.getFileData().getFileInfo().getId());
     *          if (file != null) {
     *              notifications.create("File is uploaded to temporary storage at " + file.getAbsolutePath())
     *                      .show();
     *          }
     *
     *          FileRef fileRef = temporaryStorage.putFileIntoStorage(receiver.getFileData().getFileInfo().getId(), event.getFileName());
     *          manuallyControlledField.setValue(fileRef);
     *
     *          notifications.create("Uploaded file: " + event.getFileName())
     *                  .show();
     *      });
     * </pre>
     *
     * @param listener listener to add
     * @return registration for removal of listener
     * @see FileTemporaryStorageBuffer
     */
    @Override
    public Registration addFileUploadSucceededListener(
            ComponentEventListener<FileUploadSucceededEvent<FileStorageUploadField>> listener) {
        return super.addFileUploadSucceededListener(listener);
    }

    @Override
    protected void onSucceededEvent(SucceededEvent event) {
        saveFile(event);

        super.onSucceededEvent(event);
    }

    protected void saveFile(SucceededEvent event) {
        Receiver receiver = event.getUpload().getReceiver();

        if (receiver instanceof FileTemporaryStorageBuffer) {
            FileTemporaryStorageBuffer storageReceiver = (FileTemporaryStorageBuffer) receiver;

            if (getFileStoragePutMode() == FileStoragePutMode.IMMEDIATE) {
                checkFileStorageInitialized();

                TemporaryStorageFileData fileData = storageReceiver.getFileData();

                FileRef fileRef = temporaryStorage.putFileIntoStorage(
                        fileData.getFileInfo().getId(),
                        fileData.getFileName(),
                        fileStorage);

                setInternalValue(fileRef, true);
            } else {
                // clear previous value silently
                internalValue = null;
                setPresentationValue(null);
                // set default file name label
                setComponentText(fileNameComponent, generateFileName());
            }
        } else {
            throw new IllegalStateException("Unsupported receiver: " + receiver.getClass().getName());
        }
    }

    @Override
    protected String generateFileName() {
        if (getValue() == null) {
            // Invoked from constructor, messages can be null
            return messages != null && Strings.isNullOrEmpty(getFileNotSelectedText())
                    ? messages.getMessage("fileStorageUploadField.fileNotSelected")
                    : super.generateFileName();
        }
        return getValue().getFileName();
    }

    @Override
    protected String getDefaultUploadText() {
        return messages != null
                ? messages.getMessage("fileStorageUploadField.upload.text")
                : super.getDefaultUploadText();
    }

    protected void checkFileStorageInitialized() {
        if (fileStorage == null) {
            if (StringUtils.isNotEmpty(fileStorageName)) {
                fileStorage = fileStorageLocator.getByName(fileStorageName);
            } else {
                fileStorage = fileStorageLocator.getDefault();
            }
        }
    }

    protected void onFileNameClick(ClickEvent<?> clickEvent) {
        if (!isEnabled()) {
            return;
        }

        FileRef value = getValue();
        if (value != null) {
            downloader.download(value);
        }
    }

    @Override
    protected void handleJmixUploadInternalError(String fileName) {
        showUploadErrorNotification(fileName);
    }

    protected void showUploadErrorNotification(String fileName) {
        notifications.create(
                        messages.getMessage("fileStorageUploadField.uploadInternalError.notification.title"),
                        messages.formatMessage("", "fileStorageUploadField.uploadInternalError.notification.message",
                                fileName))
                .withType(Notifications.Type.ERROR)
                .show();
    }

    @Override
    protected boolean valueEquals(FileRef value1, FileRef value2) {
        return Objects.equals(value1, value2);
    }

    protected void attachValueChangeListener(
            ValueChangeListener<ComponentValueChangeEvent<FileStorageUploadField, FileRef>> listener) {
        addValueChangeListener(listener);
    }

    protected void onValueChange(ComponentValueChangeEvent<FileStorageUploadField, FileRef> event) {
        isInvalid();
    }

    @Override
    protected void onFileRejectedEvent(FileRejectedEvent event) {
        if (!getEventBus().hasListener(FileUploadFileRejectedEvent.class)) {
            notifications.create(event.getErrorMessage())
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
        super.onFileRejectedEvent(event);
    }

    @Override
    protected void onFailedEvent(FailedEvent event) {
        deleteTempFile();

        super.onFailedEvent(event);
    }

    protected void deleteTempFile() {
        Receiver receiver = uploadButton.getReceiver();
        if (receiver instanceof FileTemporaryStorageBuffer) {
            UUID tempFileId = ((FileTemporaryStorageBuffer) receiver).getFileData().getFileInfo().getId();
            try {
                temporaryStorage.deleteFile(tempFileId);
            } catch (Exception e) {
                if (e instanceof FileStorageException) {
                    FileStorageException fse = (FileStorageException) e;
                    if (fse.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                        log.warn(String.format("Could not remove temp file %s after broken uploading", tempFileId));
                    }
                }
                log.warn(String.format("Error while delete temp file %s", tempFileId));
            }
        } else {
            throw new IllegalStateException("Unsupported receiver: " + receiver.getClass().getName());
        }
    }

    protected void applyI18nDefaults() {
        JmixUploadI18N i18nDefaults = applicationContext.getBean(UploadFieldI18NSupport.class)
                .getI18nFileStorageUploadField();
        setI18n(i18nDefaults);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        isInvalid();
    }
}
