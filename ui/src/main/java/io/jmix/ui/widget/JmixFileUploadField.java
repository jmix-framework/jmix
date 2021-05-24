/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.themes.ValoTheme;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Set;

public class JmixFileUploadField extends CustomField<String> {
    protected static final String FILE_UPLOAD_WRAPPER_STYLENAME = "jmix-fileupload-wrapper";
    protected static final String EMPTY_VALUE_STYLENAME = "jmix-fileupload-empty";

    protected CssLayout container;
    protected Button fileNameButton;
    protected Button clearButton;
    protected JmixFileUpload uploadButton;

    protected boolean showFileName = false;
    protected boolean showClearButton = false;

    protected String fileName;
    protected String fileNotSelectedMessage = "";

    protected String value;

    public JmixFileUploadField() {
        uploadButton = createUploadButton();
        setPrimaryStyleName(FILE_UPLOAD_WRAPPER_STYLENAME);
        initLayout();
    }

    protected JmixFileUpload createUploadButton() {
        return new JmixFileUpload();
    }

    @Override
    protected Component initContent() {
        return container;
    }

    public String getFileNotSelectedMessage() {
        return fileNotSelectedMessage;
    }

    public void setFileNotSelectedMessage(String fileNotSelectedMessage) {
        this.fileNotSelectedMessage = fileNotSelectedMessage;
    }

    @Override
    protected void doSetValue(String presentationValue) {
        this.value = presentationValue;

        setFileNameButtonCaption(presentationValue);

        onSetInternalValue(presentationValue);
    }

    @Override
    public String getValue() {
        return value;
    }

    protected void onSetInternalValue(Object newValue) {
    }

    private void updateComponentWidth() {
        if (container == null)
            return;

        if (getWidth() >= 0) {
            container.setWidth(100, Unit.PERCENTAGE);
            if (isShowFileName()) {
                fileNameButton.setWidth(100, Unit.PERCENTAGE);
                uploadButton.setWidthUndefined();
                clearButton.setWidthUndefined();
            } else {
                fileNameButton.setWidthUndefined();
                if (isShowClearButton() && !isRequiredIndicatorVisible()) {
                    uploadButton.setWidth(100, Unit.PERCENTAGE);
                    clearButton.setWidth(100, Unit.PERCENTAGE);
                } else {
                    uploadButton.setWidth(100, Unit.PERCENTAGE);
                }
            }
        } else {
            container.setWidthUndefined();
            fileNameButton.setWidthUndefined();
            uploadButton.setWidthUndefined();
            clearButton.setWidthUndefined();
        }
    }

    private void updateComponentHeight() {
        if (container == null)
            return;

        if (getHeight() >= 0) {
            container.setHeight(100, Unit.PERCENTAGE);
            fileNameButton.setHeight(100, Unit.PERCENTAGE);
            uploadButton.setHeight(100, Unit.PERCENTAGE);
            clearButton.setHeight(100, Unit.PERCENTAGE);
        } else {
            container.setHeightUndefined();
            fileNameButton.setHeightUndefined();
            uploadButton.setHeightUndefined();
            clearButton.setHeightUndefined();
        }
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        updateComponentWidth();
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        updateComponentHeight();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        updateButtonsVisibility();
        updateComponentWidth();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        super.setRequiredIndicatorVisible(visible);

        updateButtonsVisibility();
        updateComponentWidth();
    }

    @Override
    public void focus() {
        super.focus();
        if (uploadButton != null) {
            uploadButton.focus();
        }
    }

    protected void updateButtonsVisibility() {
        uploadButton.setVisible(!isReadOnly());

        clearButton.setVisible(!isReadOnly() && !isRequiredIndicatorVisible() && showClearButton);
    }

    protected void initLayout() {
        container = new CssLayout();
        container.addStyleName("jmix-fileupload-container");

        fileNameButton = new JmixButton();
        fileNameButton.setWidth(100, Unit.PERCENTAGE);
        fileNameButton.addStyleName(ValoTheme.BUTTON_LINK);
        fileNameButton.addStyleName("jmix-fileupload-filename");
        setFileNameButtonCaption(null);
        container.addComponent(fileNameButton);

        container.addComponent(uploadButton);

        clearButton = new JmixButton("");
        clearButton.setStyleName("jmix-fileupload-clear");
        container.addComponent(clearButton);
        setShowClearButton(showClearButton);

        setShowFileName(false);
        setWidthUndefined();
    }

    public boolean isShowFileName() {
        return showFileName;
    }

    public void setShowFileName(boolean showFileName) {
        this.showFileName = showFileName;
        fileNameButton.setVisible(showFileName);

        updateComponentWidth();
    }

    public void setFileNameButtonCaption(@Nullable String title) {
        this.fileName = title;

        if (StringUtils.isNotEmpty(title)) {
            fileNameButton.setCaption(title);
            fileNameButton.removeStyleName(EMPTY_VALUE_STYLENAME);
        } else {
            fileNameButton.setCaption(fileNotSelectedMessage);
            fileNameButton.addStyleName(EMPTY_VALUE_STYLENAME);
        }
    }

    public void addFileNameClickListener(Button.ClickListener clickListener) {
        fileNameButton.addClickListener(clickListener);
    }

    public void removeFileNameClickListener(Button.ClickListener clickListener) {
        fileNameButton.removeClickListener(clickListener);
    }

    public void setFileNameButtonEnabled(boolean enabled) {
        fileNameButton.setEnabled(enabled);
    }

    public void setClearButtonEnabled(boolean enabled) {
        clearButton.setEnabled(enabled);
    }

    public void setUploadButtonEnabled(boolean enabled) {
        uploadButton.setEnabled(enabled);
    }

    public void setUploadButtonDescription(@Nullable String description) {
        uploadButton.setDescription(description);
    }

    @Nullable
    public String getUploadButtonDescription() {
        return uploadButton.getDescription();
    }

    public void setUploadButtonCaption(@Nullable String caption) {
        uploadButton.setCaption(caption);
    }

    @Nullable
    public String getUploadButtonCaption() {
        return uploadButton.getCaption();
    }

    public void setUploadButtonIcon(@Nullable Resource icon) {
        uploadButton.setIcon(icon);
    }

    @Nullable
    public String getUploadButtonIcon() {
        return uploadButton.getIcon() != null
                ? uploadButton.getIcon().toString()
                : null;
    }

    public boolean isShowClearButton() {
        return showClearButton;
    }

    public void setShowClearButton(boolean showClearButton) {
        this.showClearButton = showClearButton;

        updateButtonsVisibility();
        updateComponentWidth();
    }

    public void setClearButtonCaption(@Nullable String caption) {
        clearButton.setCaption(caption);
    }

    @Nullable
    public String getClearButtonCaption() {
        return clearButton.getCaption();
    }

    public void setClearButtonIcon(@Nullable Resource icon) {
        clearButton.setIcon(icon);
    }

    @Nullable
    public String getClearButtonIcon() {
        return clearButton.getIcon().toString();
    }

    public void setClearButtonListener(Button.ClickListener listener) {
        clearButton.addClickListener(listener);
    }

    public void removeClearButtonAction(Button.ClickListener listener) {
        clearButton.removeClickListener(listener);
    }

    public void setClearButtonDescription(String description) {
        clearButton.setDescription(description);
    }

    public String getClearButtonDescription() {
        return clearButton.getDescription();
    }

    @Override
    public int getTabIndex() {
        return uploadButton.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        uploadButton.setTabIndex(tabIndex);
        clearButton.setTabIndex(tabIndex);
    }

    //----JmixFileUpload delegate methods

    public void setProgressWindowCaption(String progressWindowCaption) {
        uploadButton.setProgressWindowCaption(progressWindowCaption);
    }

    public void setUnableToUploadFileMessage(String message) {
        uploadButton.setUnableToUploadFileMessage(message);
    }

    public void setCancelButtonCaption(String cancelButtonCaption) {
        uploadButton.setCancelButtonCaption(cancelButtonCaption);
    }

    public void setDropZonePrompt(@Nullable String dropZonePrompt) {
        uploadButton.setDropZonePrompt(dropZonePrompt);
    }

    public void setFileSizeLimit(long fileSizeLimit) {
        uploadButton.setFileSizeLimit(fileSizeLimit);
    }

    public void setUploadReceiver(JmixFileUpload.Receiver receiver) {
        uploadButton.setReceiver(receiver);
    }

    public void addUploadStartedListener(JmixFileUpload.StartedListener listener) {
        uploadButton.addStartedListener(listener);
    }

    public void addUploadFinishedListener(JmixFileUpload.FinishedListener listener) {
        uploadButton.addFinishedListener(listener);
    }

    public void addUploadSucceededListener(JmixFileUpload.SucceededListener listener) {
        uploadButton.addSucceededListener(listener);
    }

    public void addUploadFailedListener(JmixFileUpload.FailedListener listener) {
        uploadButton.addFailedListener(listener);
    }

    public void addFileSizeLimitExceededListener(JmixFileUpload.FileSizeLimitExceededListener listener) {
        uploadButton.addFileSizeLimitExceededListener(listener);
    }

    public void addFileExtensionNotAllowedListener(JmixFileUpload.FileExtensionNotAllowedListener listener) {
        uploadButton.addFileExtensionNotAllowedListener(listener);
    }

    public void setAccept(@Nullable String accept) {
        uploadButton.setAccept(accept);
    }

    public void setDropZone(@Nullable Component component) {
        uploadButton.setDropZone(component);
    }

    public void setPasteZone(@Nullable Component component) {
        uploadButton.setPasteZone(component);
    }

    public void setPermittedExtensions(@Nullable Set<String> permittedExtensions) {
        uploadButton.setPermittedExtensions(permittedExtensions);
    }
}
