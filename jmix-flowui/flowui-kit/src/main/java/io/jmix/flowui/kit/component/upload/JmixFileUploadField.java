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
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class JmixFileUploadField<C extends AbstractSingleUploadField<C, byte[]>>
        extends AbstractSingleUploadField<C, byte[]> {

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
    protected void onSucceededEvent(SucceededEvent event) {
        saveFile(event);

        super.onSucceededEvent(event);
    }

    protected void saveFile(SucceededEvent event) {
        Upload upload = event.getUpload();
        Receiver receiver = upload.getReceiver();

        if (receiver instanceof MemoryBuffer) {
            uploadedFileName = event.getFileName();

            InputStream inputStream = ((MemoryBuffer) receiver).getInputStream();
            byte[] value;
            try {
                value = IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Cannot upload file: " + event.getFileName());
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            setInternalValue(value, true);
        } else {
            throw new IllegalStateException("Unsupported receiver: " + receiver.getClass().getName());
        }
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
}
