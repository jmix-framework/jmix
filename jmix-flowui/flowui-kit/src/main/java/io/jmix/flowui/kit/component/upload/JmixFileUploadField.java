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

package io.jmix.flowui.kit.component.upload;

import com.google.common.base.Strings;
import com.vaadin.flow.server.streams.UploadHandler;
import io.jmix.flowui.kit.component.upload.handler.SupportUploadSuccessHandler.UploadSuccessContext;
import org.jspecify.annotations.Nullable;
import org.apache.commons.io.FileUtils;

import java.util.Arrays;

/**
 * A component for uploading files with support for displaying the uploaded file name and handling file upload events.
 *
 * @param <C> the type of the inheriting component that extends {@code AbstractSingleUploadField}
 */
public class JmixFileUploadField<C extends AbstractSingleUploadField<C, byte[], byte[]>>
        extends AbstractSingleUploadField<C, byte[], byte[]> {

    private static final String DEFAULT_FILENAME = "attachment";

    protected String fileName;
    protected String uploadedFileName;

    public JmixFileUploadField() {
        this(null);
    }

    public JmixFileUploadField(byte[] defaultValue) {
        super(defaultValue);
    }

    /**
     * @return file name to be shown in the component next to upload button
     */
    @Nullable
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets file name to be shown in the component next to upload button.
     * The file name of the newly uploaded file will rewrite the caption.
     * <p>
     * The default value is "attachment (file_size Kb)". See also message key "{@code fileUploadField.noFileName}".
     *
     * @param fileName file name to show
     */
    public void setFileName(@Nullable String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return name of the uploaded file or {@code null} if no file was uploaded using "Upload" button
     */
    @Nullable
    public String getUploadedFileName() {
        return uploadedFileName;
    }

    @Override
    protected String generateFileName() {
        if (getValue() == null) {
            return Strings.isNullOrEmpty(getFileNotSelectedText())
                    ? FILE_NOT_SELECTED
                    : getFileNotSelectedText();
        }
        if (!Strings.isNullOrEmpty(uploadedFileName)) {
            return uploadedFileName;
        }
        return Strings.isNullOrEmpty(fileName)
                ? convertValueToFileName(getValue())
                : fileName;
    }

    protected String convertValueToFileName(byte[] value) {
        return String.format(DEFAULT_FILENAME + " (%s)", FileUtils.byteCountToDisplaySize(value.length));
    }

    @Override
    protected String getDefaultUploadText() {
        return UPLOAD;
    }

    @Override
    protected void onSucceeded(UploadSuccessContext<byte[]> context) {
        saveFile(context);

        super.onSucceeded(context);
    }

    protected void saveFile(UploadSuccessContext<byte[]> context) {
        uploadedFileName = context.uploadMetadata().fileName();
        setInternalValue(context.data(), true);
    }

    @Override
    protected void setInternalValue(@Nullable byte[] value, boolean fromClient) {
        if (!fromClient) {
            // clear uploaded file name if file uploaded not by this component
            uploadedFileName = null;
        }

        super.setInternalValue(value, fromClient);
    }

    protected boolean valueEquals(@Nullable byte[] a, @Nullable byte[] b) {
        return Arrays.equals(a, b);
    }

    @Override
    protected UploadHandler createUploadHandler() {
        return UploadHandler.inMemory((metadata, data) ->
                        onSucceeded(new UploadSuccessContext<>(metadata, data)),
                createDefaultTransferProgressListener());
    }

    @Override
    protected String getContentType(String fileName) {
        return "unknown";
    }
}
