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

package io.jmix.flowui.download;

import io.jmix.core.CoreProperties;
import io.jmix.core.UuidProvider;
import io.jmix.flowui.UiProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

public class ByteArrayDownloadDataProvider implements DownloadDataProvider {

    private static final Logger log = LoggerFactory.getLogger(ByteArrayDownloadDataProvider.class);

    protected Supplier<InputStream> supplier;

    /**
     * Constructor.
     *
     * @param data                                    byte array
     * @param saveExportedByteArrayDataThresholdBytes threshold in bytes on which downloaded byte array will be saved to
     *                                                a temporary file to prevent HTTP session memory leaks. Use {@link UiProperties#getSaveExportedByteArrayDataThresholdBytes()}.
     * @param tempDir                                 where to store the temporary file if {@code saveExportedByteArrayDataThresholdBytes} is exceeded.
     *                                                Use {@link CoreProperties#getTempDir()}.
     */
    public ByteArrayDownloadDataProvider(byte[] data, int saveExportedByteArrayDataThresholdBytes, String tempDir) {
        if (data.length >= saveExportedByteArrayDataThresholdBytes) {
            // save to temp
            File file = saveToTempStorage(data, tempDir);
            this.supplier = () -> readFromTempStorage(file);
        } else {
            this.supplier = () -> new ByteArrayInputStream(data);
        }
    }

    protected File saveToTempStorage(byte[] data, String tempDir) {
        UUID uuid = UuidProvider.createUuid();

        File dir = new File(tempDir);
        File file = new File(dir, uuid.toString());

        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write byte data to temp file", e);
        }

        log.debug("Stored {} bytes of data to temporary file {}", data.length, file.getAbsolutePath());

        return file;
    }

    @Nullable
    protected InputStream readFromTempStorage(File file) {
        try {
            return new FileInputStream(file) {
                @Override
                public void close() throws IOException {
                    super.close();
                    try {
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        log.warn("Unable to delete temp file " + file.getAbsolutePath());
                    }
                }
            };
        } catch (FileNotFoundException e) {
            log.warn("Unable to read temp file " + file.getAbsolutePath());
            return null;
        }
    }

    @Override
    public InputStream getStream() {
        return supplier.get();
    }
}
