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

import com.vaadin.ui.Button;
import io.jmix.ui.component.FileUploadField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class WebFileUploadField extends WebAbstractSingleFileUploadField<byte[]>
        implements FileUploadField {

    private static final String DEFAULT_FILENAME = "attachment";

    protected ByteArrayOutputStream outputStream;

    @Override
    protected void onFileNameClick(Button.ClickEvent e) {
        byte[] value = getValue();
        if (value == null) {
            return;
        }
        String name = getFileName();
        String fileName = StringUtils.isEmpty(name) ? DEFAULT_FILENAME : name;
        exportDisplay.show(this::getFileContent, fileName);
    }

    @Override
    protected OutputStream receiveUpload(String fileName, String MIMEType) {
        try {
            outputStream = new ByteArrayOutputStream();
            return outputStream;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to receive file '%s' of MIME type: %s",
                    fileName, MIMEType), e);
        }
    }

    @Override
    protected void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        setValue(outputStream.toByteArray());
        super.onUploadSucceeded(event);
    }

    @Override
    protected String convertToPresentation(byte[] modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }
        return StringUtils.isEmpty(fileName)
                ? String.format(DEFAULT_FILENAME + " (%dKb)", DataSize.ofBytes(modelValue.length).toKilobytes())
                : fileName;
    }

    @Override
    public String getFileName() {
        return super.getFileName();
    }

    @Override
    public void setFileName(String filename) {
        this.fileName = filename;
        setValueToPresentation(convertToPresentation(getValue()));
    }

    @Override
    public InputStream getFileContent() {
        if (contentProvider != null) {
            return contentProvider.get();
        }

        if (getValue() != null) {
            return new ByteArrayInputStream(getValue());
        }

        return null;
    }

}